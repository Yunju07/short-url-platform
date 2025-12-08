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

## 4. Redis 캐시 전략

### 4.2 도입 배경

> 단순 Key-Value 캐싱부터 만료 정책 관리까지 안정적으로 지원하고, 
운영 경험과 생태계가 충분히 확보된 인메모리 저장소이기 때문에 Redis를 선택했습니다.
> 

### 4.3 Key–Value 구조 설계

**① Key 설계**

| Key 형태 | 예시 | 비고 |
| --- | --- | --- |
| shorturl:{shortKey} | shorturl:`Q36Jsq` | 중복 없이 명확한 namespace 제공 |

**② Value 구조**

리다이렉트 API에 필요한 정보만 저장해 **최소한의 데이터로 빠르게 반환**하도록 설계했습니다.

```
{
  "originalUrl": String,
  "expiredAt": LocalDateTime
}
```

- 조회 과정에서 `expireAt`을 재검증하여 만료 일관성 유지

### 4.4 TTL

비즈니스 만료 정책과 Redis TTL을 유사하게 설정하여 URL과 캐시의 만료 흐름을 동일하게 유지하였습니다.
또한, 만료 직후 발생할 수 있는 대량의 DB 조회(스탬피드)를 방지하기 위해,  Redis TTL은 expiredAt보다 일정 시간 더 길게 유지합니다.

- **TTL = (expiredAt – 현재 시각) + 버퍼 시간(60초)**

### 4.5 캐시 운영 전략 (Cache Aside)

단축 URL 조회는 일반적인 읽기 중심 구조이므로 가장 안정적이며 범용적인 **Cache-Aside 패턴**을 사용합니다.또한, 생성 시 즉시 Redis에 세팅하는 **Warm cache 전략**을 함께 사용합니다.

**URL 조회 흐름**

```

1) Redis GET(shorturl:{key})
    ├ Hit → Value parsing 후 expireAt 검증
    │       ├ expireAt 지남 → 즉시 410 반환
    │       └ 사용 가능 → originalUrl 반환
    └ Miss → DB 조회
             ├ 없거나 만료 → 410
             └ 있으면 redis.set(key, TTL 계산)
```

- Warm-up 전략으로 **캐시 초기 적중률** 향상
- 만료 직후에도 Redis에서 바로 만료 응답 반환 →  **Cache Stampede** 예방
- 캐시 장애 시 자동으로 **DB fallback**

+) 단축 URL은 본질적으로 생성 후 데이터가 변경되지 않으므로 시와 DB 간 데이터 불일치 이슈가 거의 없습니다. 또한, 현재 단축 URL 삭제 기능이 존재하지 않기 때문에 별도의 동기화 로직이 필요하지 않습니다.

## 4.6  Redis 운영 정책

**① maxmemory: 2GB**

- 2GB 기준 Redis에 저장 가능한 단축 URL 캐시 수는 **약 1,000만 개**(엔트리당 평균 160bytes로 측정)
- **일일 URL 생성량 100만 건**과 대부분의 **TTL이 30일**이라는 가정 하에 최악의 경우(모든 키가 동일하지 않은 경우)에도 **30%** 보장

**② eviction 정책: volatile-lru**

| 정책 | 삭제 대상 기준 | 장점 | 단점 |
| --- | --- | --- | --- |
| **volatile-lru** | TTL이 있는 key 중 오래 사용되지 않은 key 삭제 | 인기 URL은 유지 | TTL 짧아도 자주 조회되면 유지 |
| **volatile-ttl** | TTL이 가장 짧은 key부터 삭제 | 메모리 회수 시점 예측 용이 | 조회 빈도 고려하지 않음 → 인기 URL도 삭제 가능 |
- 오래 사용되지 않은 URL은 가치가 낮아 제거해도 무방
- 만료 직후 요청이 몰릴 수 있어 **TTL에 버퍼 시간**을 두었으며, volatile-ttl은 이런 키를 우선 제거하기 때문에 **버퍼 전략의 효과가 사라져 LRU 정책**이 더 적합

---

## 5. CQRS 패턴 도입

### 5.1 도입 배경

단축 URL 생성과 리다이렉트 조회는 트래픽 성격이 다르며, 특히 **조회 요청은 생성 대비 높은 빈도**로 발생합니다. 단일 DB에서 read/write가 동시에 증가할 경우 **connection pool 포화 및 요청 지연**이 발생할 가능성이 있어 기능별 데이터 접근 경로 분리가 필요했습니다.

### 5.2 설계 방식

- 생성 API는 MySQL을 기준으로 데이터 정합성을 우선 처리
- 리다이렉트 조회는 **MongoDB 기반 조회 모델**로 분리
- URL 데이터는 이벤트 기반으로 MongoDB에 적재하여 일관성 유지

**💡 MongoDB의 구조적 이점**

- 단일 키를 기준으로 단건 조회하기에 최적화
    - `_id` 조회 시 SQL Parsing, Join, ORM 매핑 과정 없음
    - 네트워크 round-trip 비용 최소화
- 실제 저장 형태:
    
    ```
    {
      "shortKey": String,
      "originalUrl": String,
      "expiredAt": LocalDateTime
    }
    ```
    

### 5.3 기대 효과

**(1) 조회 성능 안정화**
: MongoDB 단일 인덱스 기반 조회로 캐시 미스 발생 시에도 안정적인 응답 속도 확보

**(2) 트래픽 증가 대응**
: Redirect API만 Scale-out 가능

**(3) 장애 격리**
: MySQL write 문제 발생 시에도 redirect API는 독립적으로 동작

