# MSA 기반 분산 시스템 플랫폼 구축 (Short URL Platform)

## 1. 프로젝트 소개

이 프로젝트는 **단축 URL을 생성하고 관리하며, 생성된 링크의 접근 통계를 제공하는 플랫폼**입니다. **단일 서비스 형태에서 출발**하여, 트래픽 증가시 병목이 발생하는 구조를 개선하기 위해 **MSA 구조로 확장한 사례**입니다. 기능을 서비스 단위로 분리하며 운영 관점에서 확장성과 안정성을 고려했고, 부하 테스트를 수행하여 병목 지점을 확인하고 개선하는 데 집중했습니다.

---

## 2. MSA 아키텍처 및 서비스 모듈 구성

본 시스템은 기능별 책임 경계를 기준으로 서비스를 분리하여 독립적인 배포·확장 구조를 갖도록 설계하였습니다. 각 서비스는 API 목적, 처리 방식, 데이터 저장 형태가 명확히 구분되며, 서비스 간 통신은 **Kafka 기반 비동기 메시징**을 중심으로 이루어집니다.

### 2.1 전체 아키텍처
<img width="1888" height="1166" alt="스크린샷 2025-12-06 오후 10 56 41" src="https://github.com/user-attachments/assets/b65cf6e3-9c70-4d7c-826b-432c0ca762fb" />


### 2.2 서비스 모듈 구성

**① url-service**

- 단축 URL 생성 및 메타데이터 저장 담당
- URL 생성 API 및 상태 조회 API 제공

**② redirect-service**

- 단축 URL 해석 및 원본 URL 리다이렉트 처리
- Redis 기반 조회 최적화로 초저지연 처리 설계
- 리다이렉트 이벤트를 Kafka로 발행하여 후속 작업 분리

**③ stats-service**

- Kafka 이벤트를 소비하여 클릭 로그 집계 수행
- MySQL 기반 집계 테이블 관리
- 상세 통계 조회 및 Top N 조회 API 제공

### 2.3 서비스 모듈 분리 기준

서비스는 **기능별 책임**을 명확히 하기 위해 분리되었으며, 
특히 처리 **트래픽의 성격**, **사용하는 저장소 구조, 확장 및 장애 전파 특성의 차이**를 기준으로 나누었습니다.

| 기준 | url-service | redirect-service | stats-service |
| --- | --- | --- | --- |
| read/write 특성 | write-heavy | read-heavy | event-driven write |
| 실시간성 | 중요 | 매우 중요 | 낮음 |
| 장애 전파 영향도 | 높음 (요청 실패 발생) | 낮음 (fallback 가능) | 없음 |
| 이벤트 역할 | Producer & Consumer | Producer & Consumer | Consumer |
| 확장 방식 | 수직 확장 중심 | 수평 확장(read throughput 증가) | consumer-group 기반 수평 확장 |
| 데이터 저장소 | MySQL | Redis, MongoDB | MySQL |

**핵심 설계 포인트**

- 생성과 조회 트래픽을 분리하여 병목 제거
- 장애 전파 최소화를 위해 API 호출 단절 구조 설계
- 부하 증가 시 서비스별 독립 확장 가능하도록 설계

---

## 3. 이벤트 기반 통신 (EDA)

MSA 구조에서 서비스 간 결합을 최소화하고 장애 전파를 제어하기 위해 **직접 호출을 하지 않도록 구성**하였습니다. 서비스는 **Kafka 기반 이벤트 통신**을 통해 요청 흐름과 처리 시점을 분리하고, 필요한 데이터만 비동기적으로 전달받아 각자의 책임 범위 내에서 처리하도록 설계하였습니다.

### 3.1 이벤트 종류

**① `click-log`** 

- **목적:**URL이 클릭 되었음을 알리는 이벤트
- **Producer:** redirect-service
- **Consumer:** stats-service
- 처리 방식: stats-service는 해당 클릭 정보를 RDB에 저장하여 집계 데이터의 기반으로 사용

**② `click-resolve`**

- **목적**: 정상적으로 리다이렉트가 수행되었음을 알리는 이벤트
- **Producer**: redirect-service
- **Consumer**: url-service
- **처리 방식**: url-service는 메타데이터(총 클릭 수 증가, 마지막 클릭 시각 변경)를 업데이트하여 조회 API 응답에 반영

**③ `url-created`**

- **목적:** 신규 URL이 생성됨을 알리는 이벤트
- **Producer:** url-service
- **Consumer:** redirect-service
- **처리 방식:** redirect-service는 해당 URL 정보를 캐시에 미리 저장하고 조회용 MongoDB에도 적재하여 이후 요청 시 cache miss를 줄이고 빠른 상세 조회를 가능하게 처리

### 3.2 이벤트 스키마

**① `click-log`** 

```
Key: shortKey
Value:
{
  "shortKey": String,
  "userAgent": String,
  "referrer": String,
  "clickedAt": LocalDateTime
}
```

- DeviceType 파싱 작업은 통계서비스에서 처리하도록 구성

**② `click-resolve`**

```
Key: shortKey
Value:
{
  "shortKey": String,
  "clickedAt": LocalDateTime
}

```

**③ `url-created`**

```
Key: shortKey
Value:
{
  "shortKey": String,
  "originalUrl": String,
  "expiredAt": LocalDateTime
}
```

- epoch로 재계산하지 않고 생성 시점의 만료 값을 그대로 전달하여 시점 차이 없이 정합성을 유지

### 3.3 이벤트 처리 과정 (다이어그램)

<img width="1878" height="1015" alt="EDA1" src="https://github.com/user-attachments/assets/4d6f7eda-856f-4c03-8222-c1f1bf37685a" />


- 리다이렉트 요청 처리와 데이터 적재 시점을 분리하여 **응답 속도를 향상**
- 클릭을 저장하고 처리하는 과정에서 발생할 수 있는 **장애가 리다이렉트 요청에 영향을 주지 않도록 분리**

<img width="1846" height="1027" alt="EDA2" src="https://github.com/user-attachments/assets/463822e6-cd39-4b8d-8992-3b81a26680d8" />


- 조회 성능을 높이기 위한 **캐시 저장소**는 리다이렉트에서 온전히 관리하도록 책임 분리
- **CQRS 패턴**: 조회 모델과 커맨드 모델 간 데이터 동기화

---

