좋은 피드백입니다. 방향은 맞습니다. 다만 **“최종안”이라고 부르기엔 아직 API 명세와 몇 군데 핵심적으로 안 맞습니다.**

한 줄로 정리하면 이렇습니다.

**설정/프로필/가변 payload 는 JSONB가 맞고, 돈/배치/소유관계/로그인은 컬럼과 별도 테이블로 빼야 합니다.**

특히 아래 3개는 그대로 가면 API와 충돌합니다.

1. `students.balance`
    
    → API는 이미 **지갑/통장/국고/원장** 구조로 가고 있어서 단일 balance 로는 안 맞습니다.
    
2. `activities` 에 `LEDGER` 까지 합친 구조
    
    → “한 타임라인에서 보고 싶다”는 의도는 좋지만, **금융 데이터는 JSONB 한 칸에 넣으면 안 됩니다.**
    
    원장/잔액/이체 무결성이 가장 중요합니다.
    
3. `seats.econ_config` 안에 `owner_id`, `contract` 넣는 구조
    
    → API에서 요구한 **좌석 배치, 청약, 매물, 계약, 정산**을 처리하기엔 FK/조회/검증이 너무 약합니다.
    

---

# 먼저 판단부터: 어디는 맞고, 어디는 안 맞는지

| 영역 | 판단 | 코멘트 |
| --- | --- | --- |
| `users` | 대체로 맞음 | 교사 계정 테이블로 충분 |
| `worlds.policy_config` | 부분 적합 | 세율/요금은 괜찮지만 **잔고(balance)** 는 넣으면 안 됨 |
| `worlds.feature_flags` | 적합 | 기능 온오프는 JSONB로 두기 좋음 |
| `jobs.job_config` | 적합 | 직업별 특수 권한/액션은 JSONB가 잘 맞음 |
| `students.profile_config` | 적합 | 아바타/UI 설정은 JSONB로 두기 좋음 |
| `students.login_id` | 불일치 | API는 `worldCode + studentNo + password` 로그인이라 `login_id`는 안 맞음 |
| `students.balance` | 불일치 | API의 account/ledger 구조와 충돌 |
| `students.job_id` | 부분 적합 | 현재 직업만 보면 되지만 **배정 이력**과 batch 저장을 생각하면 별도 assignment가 더 안전 |
| `activities` 통합 | 방향은 좋지만 수정 필요 | 체크리스트/보고서 통합은 좋지만 **ledger까지 합치면 안 됨** |
| `seats.econ_config` | 불일치 | 좌석 활성/비활성, 배정 이력, 소유권, 계약을 JSONB 한 칸에 다 넣기엔 무리 |
| 상점/인벤토리 | 누락 | API와 맞추려면 `store_items`, `student_inventory` 필요 |
| 청약/부동산 계약 | 누락 | `subscription_rounds`, `applications`, `property_listings`, `property_contracts` 필요 |
| 활동 로그 | 누락 | 교사/시스템 이벤트를 담을 `activity_logs`가 따로 필요 |

---

# 결론

받은 피드백의 **하이브리드 철학 자체는 좋습니다.**

하지만 실제로는 이렇게 수정해야 API와 맞습니다.

## 유지해도 되는 JSONB

- `worlds.policy_config`
- `worlds.feature_flags`
- `jobs.job_config`
- `students.profile_config`
- `student_work_items.content`
- `store_items.item_config`
- `activity_logs.payload`
- `property_contracts.terms`

## JSONB에서 빼야 하는 것

- 학생 돈(`balance`)
- 국고 잔액
- 원장 amount / from / to / category
- 좌석 소유자 FK
- 청약 신청자/당첨 관계
- 현재 좌석 배정
- 현재 직업 배정
- `mustChangePassword`, `isLocked`, `status`

---

# API 명세와 맞춘 수정안

## 1) DB에서 꼭 바꿔야 하는 부분

### A. 학생 로그인

기존 피드백:

```sql
login_id VARCHAR(50) NOT NULL
```

권장:

- `login_id` 제거
- 학생 로그인 키는 `world_id + student_no`
- API의 `POST /auth/student/login` 과 그대로 맞춤

### B. 학생 돈

기존 피드백:

```sql
balance DECIMAL(18,2) NOT NULL DEFAULT 0.00
```

권장:

- `students.balance` 제거
- `accounts` 테이블 신설
- `ledger_entries` 테이블 신설
- `accounts.balance` 는 현재 잔액 snapshot
- `ledger_entries` 는 이력

### C. activities 통합

기존 피드백:

```sql
type = 'LEDGER' | 'CHECKLIST' | 'REPORT'
content JSONB
```

권장:

- `LEDGER` 는 분리
- `CHECKLIST/REPORT/QUIZ/...` 류만 `student_work_items` 로 유지
- 운영 이벤트는 `activity_logs` 로 분리

### D. 좌석/부동산

기존 피드백:

```sql
seats.econ_config = {"owner_id": 1, "contract": {...}}
```

권장:

- 좌석 자체는 `seats`
- 현재 배정/이력은 `seat_assignments`
- 청약은 `subscription_rounds`, `subscription_applications`
- 매물/계약은 `property_listings`, `property_contracts`

---

# 수정된 PostgreSQL 하이브리드 스키마

아래 스키마는 **제가 앞서 정리한 API 명세와 맞춘 버전**입니다.

핵심은:

- **무결성이 중요한 것**: RDB 테이블/컬럼
- **자주 바뀌는 설정/확장 payload**: JSONB

## 0. 공통

```sql
-- updated_at 은 trigger 로 갱신 권장
-- PostgreSQL에서는 uuid-ossp를 안 쓸 거면 굳이 extension이 필요 없습니다.
```

## 1. 교사 계정

```sql
CREATE TABLE users (
  id              BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  email           TEXT NOT NULL UNIQUE,
  password_hash   TEXT NOT NULL,
  name            TEXT NOT NULL,
  role            TEXT NOT NULL DEFAULT 'TEACHER' CHECK (role IN ('TEACHER')),
  status          TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
  created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## 2. 학급/국가

```sql
CREATE TABLE worlds (
  id                      BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  owner_user_id           BIGINT NOT NULL REFERENCES users(id),
  name                    TEXT NOT NULL,
  world_code              VARCHAR(12) NOT NULL UNIQUE,
  grade                   SMALLINT,
  class_no                TEXT,
  status                  TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'ARCHIVED')),

  -- 정책/요금/세율만 저장
  policy_config           JSONB NOT NULL DEFAULT
    '{"tax_rates":{"income":0,"vat":0,"subscription_interest":0},"fees":{}}'::jsonb,

  -- 기능 ON/OFF
  feature_flags           JSONB NOT NULL DEFAULT
    '{"JOB":false,"SEAT":false,"STORE":false,"AVATAR":false,"REAL_ESTATE":false}'::jsonb,

  -- 직업 batch 저장 버전 관리
  job_assignment_version  INT NOT NULL DEFAULT 1 CHECK (job_assignment_version > 0),

  flag_image_url          TEXT,
  created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## 3. 커리큘럼 단계

```sql
CREATE TABLE world_curriculum_stages (
  world_id        BIGINT NOT NULL REFERENCES worlds(id),
  stage_no        SMALLINT NOT NULL CHECK (stage_no BETWEEN 0 AND 4),
  status          TEXT NOT NULL DEFAULT 'LOCKED' CHECK (status IN ('LOCKED', 'UNLOCKED', 'COMPLETED')),
  unlock_config   JSONB NOT NULL DEFAULT '{"featureNames":[]}'::jsonb,
  completed_at    TIMESTAMPTZ,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (world_id, stage_no)
);
```

## 4. 법전

```sql
CREATE TABLE world_laws (
  id              BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  world_id        BIGINT NOT NULL REFERENCES worlds(id),
  title           TEXT NOT NULL,
  description     TEXT,
  fine_amount     NUMERIC(18,2) NOT NULL DEFAULT 0 CHECK (fine_amount >= 0),
  is_active       BOOLEAN NOT NULL DEFAULT TRUE,
  display_order   INT NOT NULL DEFAULT 0,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## 5. 학생

```sql
CREATE TABLE students (
  id                    BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  world_id              BIGINT NOT NULL REFERENCES worlds(id),
  student_no            INT NOT NULL CHECK (student_no > 0),
  name                  TEXT NOT NULL,
  password_hash         TEXT NOT NULL,

  must_change_password  BOOLEAN NOT NULL DEFAULT TRUE,
  status                TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
  is_locked             BOOLEAN NOT NULL DEFAULT FALSE,

  credit_rating         TEXT,
  credit_score          INT NOT NULL DEFAULT 0,

  -- 아바타 / UI 설정
  profile_config        JSONB NOT NULL DEFAULT
    '{"avatar":{"presetId":"basic_01","equippedItemIds":[]}, "ui":{}}'::jsonb,

  last_login_at         TIMESTAMPTZ,
  created_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  UNIQUE (world_id, student_no)
);
```

## 6. 직업

```sql
CREATE TABLE jobs (
  id              BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  world_id        BIGINT NOT NULL REFERENCES worlds(id),
  name            TEXT NOT NULL,
  job_type        TEXT NOT NULL CHECK (job_type IN ('REQUIRED', 'OPTIONAL')),
  description     TEXT,
  salary          NUMERIC(18,2) NOT NULL DEFAULT 0 CHECK (salary >= 0),
  max_slots       INT NOT NULL DEFAULT 1 CHECK (max_slots > 0),
  guide_url       TEXT,
  job_config      JSONB NOT NULL DEFAULT '{}'::jsonb,
  status          TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
  created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (world_id, name)
);
```

## 7. 직업 배정 이력

```sql
CREATE TABLE job_assignments (
  id                  BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  world_id            BIGINT NOT NULL REFERENCES worlds(id),
  student_id          BIGINT NOT NULL REFERENCES students(id),
  job_id              BIGINT NOT NULL REFERENCES jobs(id),
  assigned_by_user_id BIGINT REFERENCES users(id),
  assigned_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ended_at            TIMESTAMPTZ,
  payload             JSONB NOT NULL DEFAULT '{}'::jsonb,
  CHECK (ended_at IS NULL OR ended_at >= assigned_at)
);

CREATE UNIQUE INDEX uq_job_assignments_active_student
  ON job_assignments(student_id)
  WHERE ended_at IS NULL;

CREATE INDEX idx_job_assignments_active_job
  ON job_assignments(job_id)
  WHERE ended_at IS NULL;
```

이렇게 해야 API의 **1인 1역할**이 DB 레벨에서도 보장됩니다.

## 8. 자리 레이아웃

```sql
CREATE TABLE world_seat_layouts (
  world_id        BIGINT PRIMARY KEY REFERENCES worlds(id),
  rows_cnt        INT NOT NULL CHECK (rows_cnt > 0),
  cols_cnt        INT NOT NULL CHECK (cols_cnt > 0),
  layout_version  INT NOT NULL DEFAULT 1 CHECK (layout_version > 0),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## 9. 좌석

```sql
CREATE TABLE seats (
  id              BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  world_id        BIGINT NOT NULL REFERENCES worlds(id),
  row_no          INT NOT NULL CHECK (row_no > 0),
  col_no          INT NOT NULL CHECK (col_no > 0),
  label           TEXT,
  is_active       BOOLEAN NOT NULL DEFAULT TRUE,
  seat_config     JSONB NOT NULL DEFAULT '{}'::jsonb,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (world_id, row_no, col_no)
);
```

## 10. 좌석 배정 이력

```sql
CREATE TABLE seat_assignments (
  id                  BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  world_id            BIGINT NOT NULL REFERENCES worlds(id),
  seat_id             BIGINT NOT NULL REFERENCES seats(id),
  student_id          BIGINT NOT NULL REFERENCES students(id),
  assigned_by_user_id BIGINT REFERENCES users(id),
  assigned_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ended_at            TIMESTAMPTZ,
  payload             JSONB NOT NULL DEFAULT '{}'::jsonb,
  CHECK (ended_at IS NULL OR ended_at >= assigned_at)
);

CREATE UNIQUE INDEX uq_seat_assignments_active_seat
  ON seat_assignments(seat_id)
  WHERE ended_at IS NULL;

CREATE UNIQUE INDEX uq_seat_assignments_active_student
  ON seat_assignments(student_id)
  WHERE ended_at IS NULL;
```

이 구조가 있어야 API의

- `GET /seats/grid`
- `PUT /seats/assignments/batch`
- `random-assign`
- `unassignedStudents`

를 제대로 구현할 수 있습니다.

## 11. 계좌

```sql
CREATE TABLE accounts (
  id              BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  world_id        BIGINT NOT NULL REFERENCES worlds(id),
  student_id      BIGINT REFERENCES students(id),
  account_type    TEXT NOT NULL CHECK (
                    account_type IN (
                      'WALLET',
                      'BANK_GENERAL',
                      'BANK_SAVINGS',
                      'BANK_SUBSCRIPTION',
                      'TREASURY',
                      'STORE'
                    )
                  ),
  balance         NUMERIC(18,2) NOT NULL DEFAULT 0 CHECK (balance >= 0),
  metadata        JSONB NOT NULL DEFAULT '{}'::jsonb,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CHECK (
    (student_id IS NOT NULL AND account_type IN ('WALLET', 'BANK_GENERAL', 'BANK_SAVINGS', 'BANK_SUBSCRIPTION'))
    OR
    (student_id IS NULL AND account_type IN ('TREASURY', 'STORE'))
  )
);

CREATE UNIQUE INDEX uq_accounts_student_type
  ON accounts(student_id, account_type)
  WHERE student_id IS NOT NULL;

CREATE UNIQUE INDEX uq_accounts_world_type
  ON accounts(world_id, account_type)
  WHERE student_id IS NULL;
```

## 12. 원장

```sql
CREATE TABLE ledger_entries (
  id                BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  world_id          BIGINT NOT NULL REFERENCES worlds(id),

  from_account_id   BIGINT REFERENCES accounts(id),
  to_account_id     BIGINT REFERENCES accounts(id),

  amount            NUMERIC(18,2) NOT NULL CHECK (amount > 0),
  category          TEXT NOT NULL CHECK (
                      category IN (
                        'TRANSFER',
                        'TAX',
                        'STORE_PURCHASE',
                        'PAYROLL',
                        'TEACHER_ADJUSTMENT',
                        'REFUND',
                        'SUBSCRIPTION',
                        'RENT',
                        'SALE',
                        'PENALTY',
                        'REWARD'
                      )
                    ),
  memo              TEXT,
  reference_type    TEXT,
  reference_id      TEXT,

  actor_type        TEXT NOT NULL CHECK (actor_type IN ('TEACHER', 'STUDENT', 'SYSTEM')),
  actor_user_id     BIGINT REFERENCES users(id),
  actor_student_id  BIGINT REFERENCES students(id),

  payload           JSONB NOT NULL DEFAULT '{}'::jsonb,
  created_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CHECK (from_account_id IS NOT NULL OR to_account_id IS NOT NULL),
  CHECK (
    (actor_type = 'TEACHER' AND actor_user_id IS NOT NULL AND actor_student_id IS NULL)
    OR
    (actor_type = 'STUDENT' AND actor_student_id IS NOT NULL AND actor_user_id IS NULL)
    OR
    (actor_type = 'SYSTEM' AND actor_user_id IS NULL AND actor_student_id IS NULL)
  )
);
```

이 테이블이 API의

- `POST /economy/transfers`
- `POST /economy/adjustments`
- `GET /worlds/{worldId}/ledgers`
- `GET /students/me/ledgers`

를 받칩니다.

## 13. 운영 활동 로그

```sql
CREATE TABLE activity_logs (
  id                BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  world_id          BIGINT NOT NULL REFERENCES worlds(id),

  actor_type        TEXT NOT NULL CHECK (actor_type IN ('TEACHER', 'STUDENT', 'SYSTEM')),
  actor_user_id     BIGINT REFERENCES users(id),
  actor_student_id  BIGINT REFERENCES students(id),

  action_type       TEXT NOT NULL,
  target_type       TEXT,
  target_id         TEXT,
  summary           TEXT NOT NULL,
  payload           JSONB NOT NULL DEFAULT '{}'::jsonb,
  created_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CHECK (
    (actor_type = 'TEACHER' AND actor_user_id IS NOT NULL AND actor_student_id IS NULL)
    OR
    (actor_type = 'STUDENT' AND actor_student_id IS NOT NULL AND actor_user_id IS NULL)
    OR
    (actor_type = 'SYSTEM' AND actor_user_id IS NULL AND actor_student_id IS NULL)
  )
);
```

이게 있어야

- 비밀번호 초기화
- 로그인
- 기능 토글
- 차시 완료
- 좌석 저장
- 직업 배정
- 법 수정

같은 이벤트를 제대로 남길 수 있습니다.

## 14. 학생 업무/체크리스트/보고서

기존 `activities` 의 좋은 의도는 살리되, 이름과 책임만 정리합니다.

```sql
CREATE TABLE student_work_items (
  id                  BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  world_id            BIGINT NOT NULL REFERENCES worlds(id),
  student_id          BIGINT NOT NULL REFERENCES students(id),
  job_id              BIGINT REFERENCES jobs(id),

  work_type           TEXT NOT NULL CHECK (work_type IN ('CHECKLIST', 'REPORT', 'QUIZ', 'FORM')),
  title               TEXT NOT NULL,
  status              TEXT NOT NULL DEFAULT 'PENDING' CHECK (
                        status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'SUBMITTED', 'REJECTED')
                      ),

  content             JSONB NOT NULL DEFAULT '{}'::jsonb,
  due_at              TIMESTAMPTZ,
  created_by_user_id  BIGINT REFERENCES users(id),
  created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

이 테이블은 기존 피드백의 “업무 통합” 철학을 살리면서도,

원장까지 억지로 합치지 않게 해줍니다.

---

# 2차 확장 테이블도 API와 맞춰 두는 게 좋습니다

## 상점 / 인벤토리

```sql
CREATE TABLE store_items (
  id              BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  world_id        BIGINT NOT NULL REFERENCES worlds(id),
  name            TEXT NOT NULL,
  description     TEXT,
  category        TEXT NOT NULL CHECK (category IN ('COUPON', 'SNACK', 'AVATAR', 'PROPERTY')),
  price           NUMERIC(18,2) NOT NULL CHECK (price >= 0),
  stock_quantity  INT NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
  is_active       BOOLEAN NOT NULL DEFAULT TRUE,
  item_config     JSONB NOT NULL DEFAULT '{"allowDuplicatePurchase":true}'::jsonb,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE student_inventory (
  id              BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  world_id        BIGINT NOT NULL REFERENCES worlds(id),
  student_id      BIGINT NOT NULL REFERENCES students(id),
  store_item_id   BIGINT NOT NULL REFERENCES store_items(id),
  quantity        INT NOT NULL DEFAULT 1 CHECK (quantity > 0),
  state_config    JSONB NOT NULL DEFAULT '{}'::jsonb,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## 청약

```sql
CREATE TABLE subscription_rounds (
  id              BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  world_id        BIGINT NOT NULL REFERENCES worlds(id),
  title           TEXT NOT NULL,
  apply_start_at  TIMESTAMPTZ NOT NULL,
  apply_end_at    TIMESTAMPTZ NOT NULL,
  status          TEXT NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'OPEN', 'CLOSED', 'DRAWN')),
  config          JSONB NOT NULL DEFAULT '{}'::jsonb,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE subscription_round_seats (
  round_id        BIGINT NOT NULL REFERENCES subscription_rounds(id),
  seat_id         BIGINT NOT NULL REFERENCES seats(id),
  PRIMARY KEY (round_id, seat_id)
);

CREATE TABLE subscription_applications (
  id              BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  round_id        BIGINT NOT NULL REFERENCES subscription_rounds(id),
  world_id        BIGINT NOT NULL REFERENCES worlds(id),
  student_id      BIGINT NOT NULL REFERENCES students(id),
  seat_id         BIGINT NOT NULL REFERENCES seats(id),
  status          TEXT NOT NULL DEFAULT 'APPLIED' CHECK (status IN ('APPLIED', 'SELECTED', 'FAILED', 'CANCELLED')),
  created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (round_id, student_id)
);
```

## 매물 / 계약

```sql
CREATE TABLE property_listings (
  id                BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  world_id          BIGINT NOT NULL REFERENCES worlds(id),
  seat_id           BIGINT NOT NULL REFERENCES seats(id),
  seller_student_id BIGINT NOT NULL REFERENCES students(id),
  listing_type      TEXT NOT NULL CHECK (listing_type IN ('SALE', 'JEONSE', 'MONTHLY_RENT')),
  status            TEXT NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'CLOSED', 'CANCELLED')),
  price             NUMERIC(18,2) DEFAULT 0 CHECK (price >= 0),
  deposit           NUMERIC(18,2) DEFAULT 0 CHECK (deposit >= 0),
  rent              NUMERIC(18,2) DEFAULT 0 CHECK (rent >= 0),
  config            JSONB NOT NULL DEFAULT '{}'::jsonb,
  expires_at        TIMESTAMPTZ,
  created_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE property_contracts (
  id                BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  world_id          BIGINT NOT NULL REFERENCES worlds(id),
  listing_id        BIGINT REFERENCES property_listings(id),
  seat_id           BIGINT NOT NULL REFERENCES seats(id),
  owner_student_id  BIGINT NOT NULL REFERENCES students(id),
  tenant_student_id BIGINT NOT NULL REFERENCES students(id),
  contract_type     TEXT NOT NULL CHECK (contract_type IN ('SALE', 'JEONSE', 'MONTHLY_RENT')),
  deposit           NUMERIC(18,2) NOT NULL DEFAULT 0 CHECK (deposit >= 0),
  rent              NUMERIC(18,2) NOT NULL DEFAULT 0 CHECK (rent >= 0),
  status            TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'ENDED', 'CANCELLED')),
  terms             JSONB NOT NULL DEFAULT '{}'::jsonb,
  signed_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ended_at          TIMESTAMPTZ
);
```

---

# 이 스키마가 API 명세와 맞는 방식

아래처럼 1:1로 대응됩니다.

| API | DB |
| --- | --- |
| `POST /auth/student/login` | `worlds.world_code + students.student_no + password_hash` |
| `GET /worlds/{worldId}/features` | `worlds.feature_flags` |
| `PUT /worlds/{worldId}/features/{featureName}` | `worlds.feature_flags` + `activity_logs` |
| `GET /worlds/{worldId}/curriculum/stages` | `world_curriculum_stages` |
| `GET /worlds/{worldId}/laws` | `world_laws` |
| `GET /worlds/{worldId}/students` | `students` |
| `GET /worlds/{worldId}/students/{studentId}` | `students + accounts + active job + active seat` |
| `GET /seats/grid` | `world_seat_layouts + seats + active seat_assignments + students.profile_config` |
| `PUT /seats/assignments/batch` | `seat_assignments` + `layout_version` |
| `GET /jobs` | `jobs + active job_assignments` |
| `PUT /jobs/assignments/batch` | `job_assignments + worlds.job_assignment_version` |
| `POST /economy/transfers` | `accounts + ledger_entries + activity_logs` |
| `GET /ledgers` | `ledger_entries` |
| `GET /activity-logs` | `activity_logs` |
| `POST /store/items/{itemId}/buy` | `store_items + student_inventory + accounts + ledger_entries` |
| `POST /subscriptions/.../apply` | `subscription_applications` |
| `POST /property-listings/.../contracts` | `property_contracts + ledger_entries(reference_*)` |

---

# API 명세 쪽에서 같이 손보면 좋은 부분

DB를 위처럼 바꾸면 API는 크게 흔들 필요는 없습니다.

다만 아래는 맞춰 두는 게 좋습니다.

## 유지

- 학생 로그인: `worldCode`, `studentNo`, `password`
- 자산 조회: `accounts/summary`
- ledger/activity log 분리

## 수정 권장

1. `GET /api/v1/students/me/assets`
    
    → `GET /api/v1/students/me/accounts/summary` 로 유지하는 쪽이 더 명확
    
2. `POST /api/v1/economy/transfer`
    
    → `POST /api/v1/economy/transfers` 처럼 컬렉션형이 더 자연스러움
    
3. `GET /api/v1/worlds/{worldId}/summary` 응답의 `treasuryBalance`
    
    → `worlds.policy_config` 가 아니라 `accounts(account_type='TREASURY')` 에서 계산/조회
    
4. `GET /api/v1/worlds/{worldId}/jobs`
    
    → `students.job_id` 직조회가 아니라 `job_assignments ended_at IS NULL` 기준
    
5. `GET /api/v1/worlds/{worldId}/seats/grid`
    
    → `seats.econ_config.owner_id` 같은 JSON parsing 이 아니라 `seat_assignments` / `property_*` 기준
    

---

# activities를 완전히 버릴 필요는 없습니다

피드백에서 말한 “학생의 모든 활동을 타임라인으로 보고 싶다”는 목적은 좋습니다.

다만 **한 테이블에 다 몰지 말고**, 아래처럼 뷰로 합치면 됩니다.

```sql
CREATE VIEW v_world_timeline AS
SELECT
  world_id,
  created_at,
  'LEDGER'::TEXT AS event_type,
  id::TEXT AS ref_id,
  COALESCE(memo, category) AS title,
  payload
FROM ledger_entries

UNION ALL

SELECT
  world_id,
  created_at,
  'ACTIVITY'::TEXT AS event_type,
  id::TEXT AS ref_id,
  summary AS title,
  payload
FROM activity_logs

UNION ALL

SELECT
  world_id,
  created_at,
  work_type::TEXT AS event_type,
  id::TEXT AS ref_id,
  title,
  content AS payload
FROM student_work_items;
```

이렇게 하면:

- **원장 무결성은 지키고**
- **타임라인 UX도 살릴 수 있습니다**

---

# 인덱스도 조금 수정하는 게 좋습니다

받은 피드백의 GIN 인덱스는 방향은 맞지만, 지금은 약간 과합니다.

## 바로 필요한 인덱스

```sql
CREATE INDEX idx_students_world_no
  ON students(world_id, student_no);

CREATE INDEX idx_jobs_world_status
  ON jobs(world_id, status);

CREATE INDEX idx_seats_world_position
  ON seats(world_id, row_no, col_no);

CREATE INDEX idx_ledger_entries_world_created
  ON ledger_entries(world_id, created_at DESC);

CREATE INDEX idx_ledger_entries_world_category
  ON ledger_entries(world_id, category, created_at DESC);

CREATE INDEX idx_activity_logs_world_created
  ON activity_logs(world_id, created_at DESC);

CREATE INDEX idx_student_work_items_world_student
  ON student_work_items(world_id, student_id, created_at DESC);
```

## JSONB 인덱스는 선택적으로

```sql
CREATE INDEX idx_student_work_items_content_gin
  ON student_work_items USING GIN (content);
```

### 굳이 지금 안 해도 되는 것

- `students.profile_config` GIN
- `worlds.feature_flags` GIN

이 둘은 보통 `world_id` 나 `student_id` 로 먼저 row를 찾고 읽기 때문에, 초기에 효과가 크지 않습니다.

---

# 이 피드백에서 제일 좋았던 점 / 제일 아쉬운 점

## 좋았던 점

- JSONB를 **설정/확장 포인트**에 쓰려는 발상
- 직업 권한을 `job_config` 로 뺀 것
- 체크리스트/보고서 같은 비정형 업무를 하나의 구조로 보려는 접근

## 아쉬운 점

- 금융 원장까지 JSONB 통합 테이블에 넣으려 한 것
- 단일 `balance` 유지
- 좌석 소유권/계약을 JSONB 안에 넣은 것
- 학생 로그인 정책(`studentNo`)과 스키마(`login_id`)가 안 맞는 것

---

# 최종 판단

**하이브리드 방향은 맞습니다.**

하지만 지금 받은 안은 **API 명세와 100% 맞는 설계는 아니고, “설정은 잘 풀었지만 핵심 관계를 너무 JSONB 쪽으로 밀어넣은 상태”** 에 가깝습니다.

가장 안전한 수정은 이겁니다.

- `worlds.policy_config`, `feature_flags` 유지
- `jobs.job_config`, `students.profile_config` 유지
- `activities` 는 `student_work_items` 로 축소
- `students.balance` 제거 → `accounts`
- `LEDGER` 분리 → `ledger_entries`
- `seat owner / contract` 분리 → `property_*`
- `job/seat current state` 는 assignment history 테이블로 관리

원하시면 다음 단계로는 이 스키마를 기준으로 **엔드포인트별 request/response DTO와 실제 SQL 조회 예시**까지 바로 맞춰드리겠습니다.