# JobTable API

학생 경제 교육 시뮬레이션 백엔드 서버입니다.
학급(World) 단위로 학생을 관리하고, 직업 배정·좌석 배정·경제 활동(원장, 상점, 부동산)을 기록합니다.

## 기술 스택

- Java 17, Spring Boot 3.4.3
- Spring Data JPA, Hibernate 6
- PostgreSQL (JSONB)
- SpringDoc OpenAPI (Swagger UI)

## 실행 방법

```bash
# PostgreSQL 데이터베이스 생성
CREATE DATABASE jobtable;

# schema.sql → dummy-data.sql 순서로 실행 후 애플리케이션 실행
./gradlew bootRun
```

- 기본 포트: `8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## 설계 원칙: RDB + JSONB 하이브리드

### 핵심 철학

> **무결성이 중요한 것은 RDB 컬럼으로, 자주 바뀌는 설정/확장 데이터는 JSONB로.**

속성이 추가될 때마다 `ALTER TABLE`을 치지 않아도 되도록, 가변적인 데이터는 PostgreSQL의 `JSONB` 컬럼에 저장합니다.

### JSONB로 관리하는 필드

| 테이블 | 컬럼 | 이유 |
|--------|------|------|
| `worlds` | `policy_config` | 세율·요금은 학급마다 다르고 자주 바뀜 |
| `worlds` | `feature_flags` | 기능 ON/OFF는 자유 확장이 필요함 |
| `jobs` | `job_config` | 직업별 특수 권한·설정 |
| `students` | `profile_config` | 아바타·UI 설정 |
| `student_work_items` | `content` | 체크리스트·보고서·퀴즈 등 비정형 업무 데이터 |
| `ledger_entries` | `payload` | 거래별 추가 메타데이터 |
| `activity_logs` | `payload` | 이벤트별 추가 컨텍스트 |

### RDB 컬럼으로 관리하는 것

- 학생 잔액 (`accounts.balance`) — 금융 무결성 필수
- 직업·좌석 배정 (`job_assignments`, `seat_assignments`) — FK 관계·이력 추적 필요
- 학생 상태 (`status`, `is_locked`, `must_change_password`) — 조건 검색·인덱스 필요
- 원장 금액·카테고리 (`ledger_entries.amount`, `category`) — 집계·정산에 사용

---

## JSON 처리 방식

### Hibernate 6 이전 (MySQL 시절)

MySQL `JSON` 컬럼을 JPA로 다루려면 `AttributeConverter`를 직접 구현해야 했습니다.

```java
// 개발자가 직접 작성해야 했던 코드
@Converter
public class JsonConverter implements AttributeConverter<Map<String, Object>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> map) {
        return objectMapper.writeValueAsString(map); // 저장 시 직접 직렬화
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String json) {
        return objectMapper.readValue(json, new TypeReference<>() {}); // 조회 시 직접 역직렬화
    }
}
```

엔티티에도 매번 명시해야 했습니다.

```java
@Convert(converter = JsonConverter.class)
@Column(name = "content", columnDefinition = "JSON")
private Map<String, Object> content;
```

### Hibernate 6 + PostgreSQL JSONB (현재)

`@JdbcTypeCode(SqlTypes.JSON)` 어노테이션 하나로 Hibernate + Jackson이 직렬화·역직렬화를 자동 처리합니다.
`JsonConverter.java`는 더 이상 필요하지 않습니다.

```java
@JdbcTypeCode(SqlTypes.JSON)
@Column(columnDefinition = "jsonb")
private Map<String, Object> payload;
```

### Map vs 강타입 클래스

JSONB 필드에 `Map<String, Object>` 대신 특정 클래스를 매핑하는 것도 가능합니다.

```java
// 강타입 예시 — 구조가 항상 고정된 경우
@JdbcTypeCode(SqlTypes.JSON)
@Column(columnDefinition = "jsonb")
private PolicyConfig policyConfig;
```

이 프로젝트에서는 **`Map<String, Object>`를 선택했습니다.** 직업·행위·업무 내용 등은 런타임에 구조가 달라질 수 있어, 강타입으로 고정하면 새 속성 추가 시 클래스 수정이 필요해지기 때문입니다.

| | 강타입 클래스 | `Map<String, Object>` |
|--|--------------|----------------------|
| 컴파일 타임 검증 | O | X |
| IDE 자동완성 | O | X |
| 런타임 구조 변경 | X | O |
| 새 속성 추가 시 | 클래스 수정 필요 | 코드 변경 없음 |
| Swagger 스키마 | 명확 | `object`로만 표시 |

`worlds.policy_config`처럼 구조가 고정된 경우 강타입이 적합하고, `student_work_items.content`처럼 완전히 동적인 경우 `Map`이 적합합니다.

---

## 데이터 모델 관계

```
users (교사)
└── worlds (학급)
    ├── world_curriculum_stages (커리큘럼 단계)
    ├── world_laws (법전)
    ├── world_seat_layouts (자리 레이아웃)
    ├── seats (좌석)
    │   ├── seat_assignments (좌석 배정 이력)
    │   ├── subscription_round_seats
    │   ├── property_listings (매물)
    │   └── property_contracts (계약)
    ├── jobs (직업)
    │   ├── job_assignments (직업 배정 이력)
    │   └── student_work_items (업무/체크리스트)
    ├── students (학생)
    │   ├── accounts (계좌 — WALLET/BANK/TREASURY)
    │   └── student_inventory (인벤토리)
    ├── ledger_entries (원장 — 모든 금융 이력)
    ├── activity_logs (운영 이벤트 로그)
    ├── store_items (상점 아이템)
    └── subscription_rounds (청약 회차)
        └── subscription_applications (청약 신청)
```

---

## 공통 에러 응답

| 상태코드 | 설명 |
|---------|------|
| `400 Bad Request` | 필수값 누락 또는 유효성 검사 실패 |
| `404 Not Found` | 대상 리소스를 찾을 수 없음 |
| `500 Internal Server Error` | 서버 내부 오류 |
