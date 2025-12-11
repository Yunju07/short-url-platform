# MSA 기반 분산 시스템 플랫폼 구축 (Short URL Platform)

## 1. 프로젝트 소개

이 프로젝트는 **단축 URL을 생성하고 관리하며, 생성된 링크의 접근 통계를 제공하는 플랫폼**입니다. **단일 서비스 형태에서 출발**하여, 트래픽 증가시 병목이 발생하는 구조를 개선하기 위해 **MSA 구조로 확장한 사례**입니다. 기능을 서비스 단위로 분리하며 운영 관점에서 확장성과 안정성을 고려했고, **부하 테스트를 수행**하여 병목 지점을 확인하고 개선하는 데 집중했습니다.
### 1.1 단일 서비스(모놀리식 구조)의 한계점

본 프로젝트는 초기에는 **단일 서버(Spring Boot + MySQL 기반)** 에서 모든 기능이 동작하는 모놀리식 구조로 시작했습니다. 개발 초기에는 단순하고 유지보수가 쉬운 장점이 있었지만, 실제 트래픽을 기준으로 부하 테스트를 진행하면서 아래와 같은 구조적 한계가 드러났습니다.

- **기능 간 결합도가 높아 독립 개선이 어려움**

  ex) 통계 로직 수정만으로도 전체 서버를 재배포해야 함
  
- **하나의 기능 장애가 전체 서비스 장애로 확대**
  
  ex) 통계 조회 쿼리가 지연되면 애플리케이션 전체 스레드가 점유되어 리다이렉트 응답까지 영향을 받음
  
- **낮은 확장성** : 특정 기능만 확장할 수 없고 서버 전체를 스케일링해야 함

→ 이런 이유로 기능별 책임을 명확히 분리하고 장애 전파를 차단하기 위해 **MSA 기반 구조**로 확장하였습니다.

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
| read/write 특성 | write-heavy | read-heavy | write-heavy |
| 실시간성 | 중요 | 매우 중요 | 낮음 |
| 장애 전파 영향도 | 높음 (요청 실패 발생) | 낮음 (fallback 가능) | 없음 |
| 이벤트 역할 | Producer & Consumer | Producer & Consumer | Consumer |
| 확장 방식 | 수직 확장 중심 | 수평 확장(read throughput 증가) | consumer-group 기반 수평 확장 |
| 데이터 저장소 | MySQL | Redis, MongoDB | MySQL |

**핵심 설계 포인트**

- 생성과 조회 트래픽을 분리하여 병목 제거
- 장애 전파 최소화를 위해 API 호출 단절 구조 설계
- 높은 트래픽 구간만 선택적으로 확장

---

## 3. 이벤트 기반 통신 (EDA)

MSA 구조에서 서비스 간 결합을 최소화하고 장애 전파를 제어하기 위해 **직접 호출을 하지 않도록 구성**하였습니다. 서비스는 **Kafka 기반 이벤트 통신**을 통해 요청 흐름과 처리 시점을 분리하고, 필요한 데이터만 비동기적으로 전달받아 각자의 책임 범위 내에서 처리하도록 설계하였습니다.

### 3.1 이벤트 종류

**① `click-log`**

- **목적:** URL이 클릭 되었음을 알리는 이벤트
- **Producer:** redirect-service
- **Consumer:** stats-service
- 처리 방식: stats-service는 해당 클릭 정보를 RDB에 저장하여 집계 데이터의 기반으로 사용
- **스키마**:
    
    ```
    Key: shortKey
    Value:
    {
      "shortKey": String,
      "shortUrl": String,
      "originalUrl": String,
      "userAgent": String,
      "referrer": String,
      "clickedAt": LocalDateTime
    }
    ```
    

**② `click-resolve`**

- **목적**: 정상적으로 리다이렉트가 수행되었음을 알리는 이벤트
- **Producer**: redirect-service
- **Consumer**: url-service
- **처리 방식**: url-service는 메타데이터(총 클릭 수 증가, 마지막 클릭 시각 변경)를 업데이트하여 조회 API 응답에 반영
- **스키마**:
    
    ```
    Key: shortKey
    Value:
    {
      "shortKey": String,
      "clickedAtEpochSec": long
    }
    ```
    

**③ `url-created`**

- **목적:** 신규 URL이 생성됨을 알리는 이벤트
- **Producer:** url-service
- **Consumer:** redirect-service
- **처리 방식:** redirect-service는 해당 URL 정보를 캐시에 미리 저장하고 조회용 MongoDB에도 적재하여 이후 요청 시 cache miss를 줄이고 빠른 상세 조회를 가능하게 처리
- **스키마**:
    
    ```
    Key: shortKey
    Value:
    {
      "shortKey": String,
      "originalUrl": String,
      "expiredAt": LocalDateTime
    }
    ```
    
**❓`click-log`과 `click-resolve`의 차이**

→`click-log`는 모든 클릭을 기록하기 위한 이벤트이며, 실패·만료·비정상 조회까지 포함한 전체 클릭 히스토리를 남깁니다. 
반면 `click-resolve`는 정상적으로 리다이렉트된 URL에 한해 메타데이터를 업데이트하기 위한 이벤트입니다.

### 3.2 이벤트 처리 과정 (다이어그램)

<img width="4732" height="2726" alt="EDA1" src="https://github.com/user-attachments/assets/113b423a-9f47-4d42-a337-b56a6cb04bc1" />


- 리다이렉트 요청 처리와 데이터 적재 시점을 분리하여 **응답 속도를 향상**
- 클릭을 저장하고 처리하는 과정에서 발생할 수 있는 **장애가 리다이렉트 요청에 영향을 주지 않도록 분리**

<img width="1846" height="1027" alt="EDA2" src="https://github.com/user-attachments/assets/463822e6-cd39-4b8d-8992-3b81a26680d8" />


- 조회 성능을 높이기 위한 **캐시 저장소**는 리다이렉트에서 온전히 관리하도록 책임 분리
- **CQRS 패턴**: 조회 모델과 커맨드 모델 간 데이터 동기화

### 3.3 이벤트 통신 안정화

**1) 레플리카 수 증가 및 ack 설정 강화로 내구성 향상**

Kafka 메세지 유실 가능성을 낮추기 위해 브로커를 3개로 구성하였으며, 
발행 시의 ACK와 최소 동기 레플리카 수, 재시도를 이벤트 별로 설정하였습니다.

| 항목 | 클릭 관련 이벤트 | URL 생성 이벤트 |
| --- | --- | --- |
| acks | 1 | all |
| min.insync.replicas | 1 | 2 |
| replication.factor | 3 | 3 |
| retries | 1 | 5 |
| retry.backoff | 100ms | 300ms |
| 목적 | 처리량 최우선, 일부 손실 허용 | 손실 방지, 높은 신뢰성 |
- URL 생성 이벤트 → **안정성** 중심
- 클릭 관련 이벤트 → **성능** 중심

**2) 재처리 및 DLQ 설정**

| 이벤트 타입 | 재시도 횟수 | 백오프 간격 | DLQ 토픽 | DLQ 활용 목적 |
| --- | --- | --- | --- | --- |
| click-log | 1회 | 3초 | click-log.dlq | 누락된 클릭 로그를 별도 수집하여 통계에 반영 가능 |
| click-resolve | 3회 | 3초 | click-resolve.dlq | URL 메타데이터를 수동으로 수정하여 일관성 유지 |
| url-created | 5회 | 5초 | url-created.dlq | 리다이렉트에는 영향 없지만, 서비스 이상 징후 감지용 |

운영자가 `url-created` DLQ를 확인하는 시점에는 이미 MySQL 기반으로 요청을 정상 처리하고, 몽고DB에 다시 적재한 상태일 가능성이 높습니다. 따라서 이벤트를 재처리하는 것은 의미가 적고, 조회 모델(MongoDB) 동기화 실패율을 감지하기 위한 용도로 사용합니다.

**📍 DLQ 모니터링 방법**

- 도커 실행 후 kafka-ui 접속
- Topics 메뉴에서 dlq 검색
- kafka-ui: [http://localhost:9000](http://localhost:9000/)

<img width="808" height="281" alt="image" src="https://github.com/user-attachments/assets/7b56cd4f-1e7d-4287-a039-b76762321b13" />


**3) URL 생성 이벤트의 지연/유실 시 대응 전략**

URL 생성 이벤트가 Kafka 구간에서 유실되거나 지연되면, 리다이렉트 서비스는 해당 shortKey를 조회할 때 MongoDB에 데이터가 없는 상태를 맞이할 수 있습니다. 이 문제를 해결하기 위해 **Outbox 방식과 DB Fallback 방식**을 고려하였습니다.

| 구분 | **Outbox 방식** | **DB Fallback 방식**  |
| --- | --- | --- |
| **데이터 흐름** | MySQL 저장 → Outbox 기록 → 스케줄러 → Kafka → Mongo 업데이트 | Mongo 조회 → 없으면 MySQL 조회 → Mongo에 upsert(Self-healing) |
| **실시간성** | **낮음** (Mongo 업데이트가 스케줄러 + Kafka 소비 이후 반영됨) | 높음 (Mongo miss여도 MySQL로 즉시 처리) |
| **구현 난이도** | **높음** (Outbox 테이블, 상태 플래그, Relay 스케줄러, 중복·순서 문제) | **낮음** (기존 ShortUrl 엔티티 그대로 fallback 처리) |
| **이벤트 유실 방지** | 완전 보장 | 완전 보장 불가 |
| **장점** | 이벤트 보장성 최강 | 실시간성 최강, 단순함, 장애에도 자동 self-healing |
| **단점** | **복잡하고 실시간성 부족** | **이벤트 유실이 많으면, MySQL 부하가 증가** |

Outbox 방식은 메시지 전달을 보장한다는 장점이 있지만, 조회 모델 업데이트 시점이 Kafka 처리 상태에 의존하게 되어 초기 요청 처리 성능이 저하될 수 있습니다.

반면 DB fallback 방식은 MongoDB에 데이터가 아직 반영되지 않은 상태에서도 MySQL 기준으로 데이터를 조회한 뒤 MongoDB를 보정할 수 있어 **서비스 응답 성공률과 실시간성 측면에서 안정적입니다.**

<img width="1000" alt="DB fallback" src="https://github.com/user-attachments/assets/685c1371-6bf2-4d69-a60f-f83a314db003" />

**4) 클릭 로그 수집**

앞선 Outbox 전략은 **click-log와 click-resolve 이벤트의 유실이 치명적**인 경우, 안정적으로 저장하기 위한 전략으로 사용할 수 있습니다. 
하지만 해당 전략은 데이터베이스 쓰기 작업을 수반하므로, 응답 속도와 처리량이 중요한 현재의 경우는 적합하지 않습니다. 

---

## 4. Redis 캐시 전략

### 4.1 도입 배경

많은 양의 조회와 반복되는 Hot Key 요청을 캐시로 처리하여, 응답 속도를 높이고 DB 부하를 줄이기 위함입니다.

> 단순 Key-Value를 포함한 다양한 자료구조를 지원하며, 
운영 경험과 생태계가 충분히 확보된 인메모리 저장소이기 때문에 Redis를 선택했습니다.
> 

### 4.2 Key–Value 구조 설계

**① Key 설계**

| Key 형태 | 예시 | 비고 |
| --- | --- | --- |
| shorturl:{shortKey} | shorturl:`Q36Jsq` | 중복 없이 명확한 namespace 제공 |

**② Value 구조**

리다이렉트 API에 필요한 정보만 저장해 **최소한의 데이터로 빠르게 반환**하도록 설계했습니다.

```
{
  "originalUrl": String,
  "expiredAtEpochSec": long
}
```

- 조회 과정에서 `expiredAtEpochSec`을 재검증하여 만료 일관성 유지

### 4.3 TTL

비즈니스 만료 정책과 Redis TTL을 유사하게 설정하여 **URL과 캐시의 만료 흐름을 동일하게 유지**하였습니다.
또한, 만료 직후 발생할 수 있는 대량의 **DB 조회(스탬피드)를 방지**하기 위해,  Redis TTL은 expiredAt보다 일정 시간 더 길게 유지합니다.

- **TTL = (expiredAt – 현재 시각) + 버퍼 시간(60초)**

### 4.4 캐시 운영 전략 (Cache Aside)

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

### 4.5  Redis 운영 정책

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
      "id": String,  // (= shortKey)
      "originalUrl": String,
      "expiredAtEpochSec": long
    }
    ``` 
  → 조회에 불필요한 컬럼까지 매핑되는 ORM 오버헤드를 줄이고 필요한 데이터만 저장

### 5.3 기대 효과

**(1) 조회 성능 안정화**
: MongoDB 단일 인덱스 기반 조회로 캐시 미스 발생 시에도 안정적인 응답 속도 확보

**(2) 트래픽 증가 대응**
: Redirect API만 Scale-out 가능

**(3) 장애 격리**
: MySQL write 문제 발생 시에도 redirect API는 독립적으로 동작

---

## 6. 배치 기반 처리

### 6.1 Write 누적 병합 처리

CQRS 기반으로 리다이렉트 조회 부하는 RDB에서 성공적으로 분리되었지만, 클릭 횟수 및 마지막 클릭 시간 업데이트는 **매 클릭마다 DB에 즉시 반영**되어 여전히 **write 부하**의 부담이 있습니다.
이 문제를 해결하기 위해 **배치 기반 집계 방식**으로 전환하여, 요청 단위 업데이트 부담을 제거하였습니다.

**① 구현 방법**

**1) Local in-memory aggregation** 

- `ConcurrentHashMap`
    
    
    | Map 종류 | 멀티스레드 안전성 | 비고 |
    | --- | --- | --- |
    | `HashMap` | Unsafe | 동시성 문제로 부적합 |
    | `Hashtable` | Safe | Lock이 map 전체에 걸려서 느림 |
    | `ConcurrentHashMap` | Safe | 구간 락(세그먼트 수준)으로 빠름 |

**2) Key 기반 단위 집계** → 동일 URL 1000회 클릭이어도 UPDATE 1회

**3) Batch DB update**

- flush 주기: **5초**

**② 기대효과**

- 일일 1,000만 클릭 발생 시 → 최소 **80% 이상** DB UPDATE가 감소하며, 이는 **모든 요청이 하루 동안 고르게 발생하는 가정**에서의 계산입니다. 실제 운영 환경에서는 개선 효과가 더욱 클 것으로 예상합니다.
- 실제 값이 반영되기까지 최대 **5초의 지연**이 발생하지만, 이는 **사용자 경험에 영향을 주지 않는 수준**이며, 
오히려 **잦은 DB 쓰기**를 줄이는 것이 훨씬 유리하다고 판단하였습니다.

> 🔎 **왜 ‘5초’인가요?**
> 
> → 이벤트가 몰려도 인메모리 스토어에 쌓이는 양이 과도하지 않습니다.
> 
> → 데이터 반영이 지연되는 시간이 과도하지 않습니다.
> 
**⚠️ 유의:** 트래픽 집중 시 인메모리 데이터가 증가하여 메모리 한계에 도달할 가능성이 있습니다. 향후 부하 테스트를 통해 수용 가능한 트래픽 범위를 검증하고, 배치 주기 조정 및 Redis 도입 등으로 고도화를 진행할 예정입니다.

### 6.2 통계 데이터 사전 집계

클릭 로그가 누적될 수록 통계 API가 실시간으로 집계하는 방식은 성능 저하가 발생합니다. 
이를 해결하기 위해 **사전 집계(Pre-Aggregation) 기반의 통계 시스템**을 도입하였습니다.

**① 집계 대상 테이블**

| 테이블명 | 집계 기준 | 저장 내용 |
| --- | --- | --- |
| **url_daily_clicks** | 날짜 | 날짜별 총 클릭 수 |
| **url_device_clicks** | 디바이스 | 디바이스별 총 클릭 수 |
| **url_referrer_clicks** | referrer | referrer별 총 클릭 수 |
| **url_daily_device_clicks** | 날짜 × 디바이스 | 날짜별 디바이스 클릭 |
| **url_daily_referrer_clicks** | 날짜 × referrer | 날짜별 referrer 클릭 |

 → 통계 API가 실행될 때 매번 `GROUP BY`를 계산하지 않도록 필요한 조합을 미리 계산

**② 배치 정보**

- **배치 간격:** 10분
    
    하루 클릭 로그 유입량(**1,000만건**)고려하면, 10분 동안 약 **7만 건 정도**의 클릭 로그가 누적됩니다. 실제 트래픽은 특정 시간대에 몰리는 경향이 있으나, **현재 37만 건**을 안정적으로 처리하는 것을 확인했습니다. 
    향후 통계 활용 목적이나 트래픽 상황에 따라 조정할 수 있습니다.
    
- **배치 상태 저장:** `aggregation_state` 테이블
    
    마지막 집계 시점을 데이터베이스에 기록하여, 이후 배치에서는 **해당 시점 이후의 클릭 로그만 증분 처리**하도록 설계하였습니다. 별도의 **저장소를 마련하지 않고 서비스 재시작 이후에도 안전하게 복구**하기 위해, **MySQL**에 저장하였습니다.
    

**③ 메타데이터 제공**

배치 기반 구조는 실시간성이 낮아지는 단점이 있습니다.
따라서 API 응답에 **메타데이터(마지막 배치 시점, 배치 간격)을** 포함하여, 
사용자는 통계 데이터가 **얼마나 최신 데이터인지 판단할 수 있도록 하였습니다.**

```json
"metadata": {
    "lastUpdatedAt": "2025-12-08T18:07:39.38992",
    "batchIntervalMinutes": 10
}
```

- **lastUpdatedAt** : 배치가 마지막으로 실행되어 집계가 반영된 시점
- **batchIntervalMinutes** : 배치 주기(현재 10분)

---

## 7. 실행 환경 (docker-compose)

본 프로젝트는 각 서비스 및 인프라 구성 요소를 컨테이너 기반으로 실행할 수 있도록 `docker-compose.yml`로 환경을 구성하였습니다. 로컬 환경에서도 동일한 아키텍처 구조를 재현할 수 있으며, 서비스 간 독립 실행이 가능하도록 구성되어 있습니다.

### 7.1 구성 요소

| 구성 | 역할 | 컨테이너 이름 | 호스트 포트 |
| --- | --- | --- | --- |
| **url-service** | 단축 URL 생성 API | `shorturl-url-service` | 8081 |
| **redirect-service** | 단축 URL 리다이렉트 API | `shorturl-redirect-service` | 8082 |
| **stats-service** | 통계 조회 API | `shorturl-stats-service` | 8083 |
| **MySQL** | 서비스 데이터 저장소 | `shorturl-db` | 3306 |
| **MongoDB** | 조회 모델 저장소 | `shorturl-mongodb` | 27017 |
| **Redis** | 캐싱 저장소 | `shorturl-redis` | 6379 |
| **Kafka Broker #1** | 이벤트 브로커 | `shorturl-kafka-1` | 19093 |
| **Kafka Broker #2** | 이벤트 브로커 | `shorturl-kafka-2` | 19094 |
| **Kafka Broker #3** | 이벤트 브로커 | `shorturl-kafka-3` | 19095 |
| **Zookeeper** | Kafka 메타데이터 관리 | `shorturl-zookeeper` | 2181 |
| **Kafka UI** | 이벤트 모니터링 도구 | `shorturl-kafka-ui` | 8085 |

→ 향후 인증·인가, 트래픽 제어, 라우팅 기능을 제공하는 **Gateway 모듈**을 도입할 가능성을 고려하여, 기본 포트인 8080은 비워두도록 구성했습니다.

### 7.2 실행 방법

프로젝트 루트 경로에서 아래 명령을 실행합니다.

```bash
# 초기 1회 실행 (docker-init 프로필로 테이블 자동 생성)
docker-compose -f docker-compose.yml -f docker-compose-init.yml up -d

# 재실행: 전체 컨테이너 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 전체 종료 및 중지
docker-compose down
```

---

## 8. 테스트 및 검증

### 8.1 테스트 환경

- **테스트 도구:** JMeter
- **실행 환경:** docker-compose 기반 서비스 컨테이너에 직접 부하 전송
- **트래픽 기준:** 요구사항 기반 일일 트래픽을 평균 RPS로 환산
    
    
    | 항목 | 일일량 | 목표 |
    | --- | --- | --- |
    | URL 생성 | 1,000,000 | ≤ 50ms |
    | 리다이렉트 | 10,000,000 | ≤ 10ms |
    | RPS 환산 | 생성 약 12RPS / 조회 약 115RPS | 평균 트래픽 기반 테스트 수행 |

### 8.2 테스트 상세

**1) Baseline 성능**

**목적:** 단일 서비스 구조(Spring Boot + MySQL)의 **기본 처리 성능 파악** 및 **개선 전 기준선 확보**

**측정 결과**

| API | Threads 수 | 평균(ms) | P95(ms) | 처리량(RPS) | 비고 |
| --- | --- | --- | --- | --- | --- |
| 생성 | 12 | 34 | 66 | 346 | 안정적 |
| 리다이렉트 | 115 | 29 | 64 | 3,886 | 목표(10ms) 대비 느림 |
- 조회 성능 향상을 위한 **캐시 도입 및 클릭 로그 이벤트 처리** 전략 도입

**2) 리다이렉트 성능 개선 (Redis + Kafka 적용)**

**목적:** 리다이렉트 기능의 **캐싱 전략**과 **이벤트 기반 클릭 처리 로직 분리**의 효과 검증

**측정 결과**

| 시나리오 | Threads 수 | 평균(ms) | P95(ms) | Throughput(RPS) |
| --- | --- | --- | --- | --- |
| 기존 | 115 | 29 | 64 | 3,886 |
| 개선 후 | 115 | **3** | 6 | **30,388** |
| 개선 후 ×2 | 230 | 7 | 13 | 28,822 |
| 개선 후 ×4 | 460 | 15 | 26 | 29,270 |
- 실제 트래픽이 **특정 시간대에 요청이 집중되는 경향**을 고려하여, 점차 요청 수를 늘려가며 진행
- 기존 대비 평균 응답시간 기준 약 **90% 이상 감소(29ms → 3ms)**

**3) 통계 API 사전 집계 성능**

**목적:** 대량 클릭 로그의 **사전 집계(Pre-aggregation)전략** 효과 검증

**데이터 규모**

| 구분 | 건수 |
| --- | --- |
| 전체 클릭 로그 수 | **200만** (2,112,849건) |
| 단일 URL 클릭 로그 수 | **37만** (376,038건) |
| 일일 클릭 로그 수 | **200만** (2,112,849건) |

**측정 결과 (20 Threads)**

| API | 개선 전 | 개선 후 | 개선 폭 |
| --- | --- | --- | --- |
| 단일 URL 통계 | 4,690ms | **2ms** | 약 **2,345배** |
| 일간 Top N | 63,045ms | **3ms** | 약 **21,000배** |
| 처리량 | 0.28–4 RPS | **5,200–7,800 RPS** | 수천 배 증가 |
- **배치에서 생성된 사전 집계 테이블만 단일 조회**하므로 응답 시간이 ms 단위로 단축

### 8.3 실행 방법

1. JMeter 설치 및 실행
    
    ```
    cd apache-jmeter-5.6.3/bin
    ./jmeter
    ```
    
2. Docker 환경 실행
    
    ```
    docker-compose up -d
    ```
    
3. 테스트 플랜 열기
    - **traffic-generator/** 경로에서 원하는 테스트 `.jmx`파일(test plan) 열기
    
    ```
    traffic-generator/01.baseline/create-url-baseline.jmx
    traffic-generator/02.redirect-cache-event/redirect-cahce-event.jmx
    ```
    
    - 테스트 수행 전에 **shortKey 파라미터를 수정**
4. 테스트 실행
    - JMeter 상단 메뉴에서 **Run ▷ Start** 실행
    - Summary Report 또는 View Results Tree에서 응답 결과 확인 가능

### 8.4 산출물

```
traffic-generator/
 ├── 01.baseline/
 ├── 02.redirect-cache-event/
 ├── 03.stats-preAggregation/
 └── data/
       ├── shortkeys_1000.csv
       └── shortkeys_10000.csv
```

---

## 9. API 명세 (Swagger UI)

각 서비스는 독립적으로 API 명세를 제공합니다.
서비스 실행 후 아래 URL에서 확인할 수 있습니다.

**Swagger UI 주소**

- **url-service:** [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)
- **redirect-service:** [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html)
- **stats-service:** [http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html)

---

## 10. 확장 전략

### 10.1 인프라 고도화

**① 모듈별 스케일링**

현재는 서비스 간 분리와 확장 가능한 구조 설계에 집중하여 모듈별 독립적인 스케일링이 가능한 기반을 마련하였습니다. 앞으로 실제 부하 테스트를 통해 각 서비스의 처리량/자원 사용량 등의 지표를 수집하고, 이를 바탕으로 서비스별 인스턴스 수와 확장 전략을 최적화할 계획입니다.

**② MySQL 이중화**

현재는 단일 MySQL 인스턴스만으로 구성되어 있습니다. 향후 읽기 전용 Replica를 도입하여, 집계용 대량 읽기 트래픽을 분산시킬 수 있습니다. 

### 10.2 이벤트 통신 고도화

**① DLQ 기반 자동 재처리 파이프라인**

현재는 카프카 DLQ를 수동 보정 및 모니터링 용도로 마련하였습니다. 향후, DLQ를 기반으로 **재처리 파이프라인**을 구축할 수 있습니다. 실패 원인을 함께 수집하여, 원인별 재처리 정책을 마련할 수 있습니다.

**② Consumer Lag 모니터링 및 자동 Scale-out**

현재는 Kafka consumer-group의 Lag이 시스템 병목의 지표가 됩니다. 향후에는 Lag 증가 시, stats-service 인스턴스 자동 scale-out 전략을 사용할 수 있습니다. 

### 10.3 캐시 운영 고도화

**① 캐시 TTL 정책 개선** 

현재는 TTL 버퍼를 고정값으로 사용하고 있습니다. 향후에는 운영 데이터에 기반하여 조정할 수 있습니다. 
- ex) 인기있는 key는 더 긴 버퍼 부여
