-- =====================================================
-- JobTable 스키마 정의
-- PostgreSQL 기준
-- sample.md 기반
-- =====================================================

-- 1. 교사 계정
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
  id              BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  email           TEXT NOT NULL UNIQUE,
  password_hash   TEXT NOT NULL,
  name            TEXT NOT NULL,
  role            TEXT NOT NULL DEFAULT 'TEACHER' CHECK (role IN ('TEACHER')),
  status          TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
  created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 2. 학급/국가
-- =====================================================
CREATE TABLE IF NOT EXISTS worlds (
  id                      BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  owner_user_id           BIGINT NOT NULL REFERENCES users(id),
  name                    TEXT NOT NULL,
  world_code              VARCHAR(12) NOT NULL UNIQUE,
  grade                   SMALLINT,
  class_no                TEXT,
  status                  TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'ARCHIVED')),
  policy_config           JSONB NOT NULL DEFAULT '{"tax_rates":{"income":0,"vat":0,"subscription_interest":0},"fees":{}}'::jsonb,
  feature_flags           JSONB NOT NULL DEFAULT '{"JOB":false,"SEAT":false,"STORE":false,"AVATAR":false,"REAL_ESTATE":false}'::jsonb,
  job_assignment_version  INT NOT NULL DEFAULT 1 CHECK (job_assignment_version > 0),
  flag_image_url          TEXT,
  created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 3. 커리큘럼 단계
-- =====================================================
CREATE TABLE IF NOT EXISTS world_curriculum_stages (
  world_id        BIGINT NOT NULL REFERENCES worlds(id),
  stage_no        SMALLINT NOT NULL CHECK (stage_no BETWEEN 0 AND 4),
  status          TEXT NOT NULL DEFAULT 'LOCKED' CHECK (status IN ('LOCKED', 'UNLOCKED', 'COMPLETED')),
  unlock_config   JSONB NOT NULL DEFAULT '{"featureNames":[]}'::jsonb,
  completed_at    TIMESTAMPTZ,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (world_id, stage_no)
);

-- =====================================================
-- 4. 법전
-- =====================================================
CREATE TABLE IF NOT EXISTS world_laws (
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

-- =====================================================
-- 5. 학생
-- =====================================================
CREATE TABLE IF NOT EXISTS students (
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
  profile_config        JSONB NOT NULL DEFAULT '{"avatar":{"presetId":"basic_01","equippedItemIds":[]}, "ui":{}}'::jsonb,
  last_login_at         TIMESTAMPTZ,
  created_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (world_id, student_no)
);

-- =====================================================
-- 6. 직업
-- =====================================================
CREATE TABLE IF NOT EXISTS jobs (
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

-- =====================================================
-- 7. 직업 배정 이력
-- =====================================================
CREATE TABLE IF NOT EXISTS job_assignments (
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

CREATE UNIQUE INDEX IF NOT EXISTS uq_job_assignments_active_student
  ON job_assignments(student_id)
  WHERE ended_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_job_assignments_active_job
  ON job_assignments(job_id)
  WHERE ended_at IS NULL;

-- =====================================================
-- 8. 자리 레이아웃
-- =====================================================
CREATE TABLE IF NOT EXISTS world_seat_layouts (
  world_id        BIGINT PRIMARY KEY REFERENCES worlds(id),
  rows_cnt        INT NOT NULL CHECK (rows_cnt > 0),
  cols_cnt        INT NOT NULL CHECK (cols_cnt > 0),
  layout_version  INT NOT NULL DEFAULT 1 CHECK (layout_version > 0),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 9. 좌석
-- =====================================================
CREATE TABLE IF NOT EXISTS seats (
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

-- =====================================================
-- 10. 좌석 배정 이력
-- =====================================================
CREATE TABLE IF NOT EXISTS seat_assignments (
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

CREATE UNIQUE INDEX IF NOT EXISTS uq_seat_assignments_active_seat
  ON seat_assignments(seat_id)
  WHERE ended_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_seat_assignments_active_student
  ON seat_assignments(student_id)
  WHERE ended_at IS NULL;

-- =====================================================
-- 11. 계좌
-- =====================================================
CREATE TABLE IF NOT EXISTS accounts (
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

CREATE UNIQUE INDEX IF NOT EXISTS uq_accounts_student_type
  ON accounts(student_id, account_type)
  WHERE student_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_accounts_world_type
  ON accounts(world_id, account_type)
  WHERE student_id IS NULL;

-- =====================================================
-- 12. 원장
-- =====================================================
CREATE TABLE IF NOT EXISTS ledger_entries (
  id                BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  world_id          BIGINT NOT NULL REFERENCES worlds(id),
  from_account_id   BIGINT REFERENCES accounts(id),
  to_account_id     BIGINT REFERENCES accounts(id),
  amount            NUMERIC(18,2) NOT NULL CHECK (amount > 0),
  category          TEXT NOT NULL CHECK (
                      category IN (
                        'TRANSFER', 'TAX', 'STORE_PURCHASE', 'PAYROLL',
                        'TEACHER_ADJUSTMENT', 'REFUND', 'SUBSCRIPTION',
                        'RENT', 'SALE', 'PENALTY', 'REWARD'
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
    OR (actor_type = 'STUDENT' AND actor_student_id IS NOT NULL AND actor_user_id IS NULL)
    OR (actor_type = 'SYSTEM' AND actor_user_id IS NULL AND actor_student_id IS NULL)
  )
);

-- =====================================================
-- 13. 운영 활동 로그
-- =====================================================
CREATE TABLE IF NOT EXISTS activity_logs (
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
    OR (actor_type = 'STUDENT' AND actor_student_id IS NOT NULL AND actor_user_id IS NULL)
    OR (actor_type = 'SYSTEM' AND actor_user_id IS NULL AND actor_student_id IS NULL)
  )
);

-- =====================================================
-- 14. 학생 업무/체크리스트/보고서
-- =====================================================
CREATE TABLE IF NOT EXISTS student_work_items (
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

-- =====================================================
-- 15. 상점 / 인벤토리
-- =====================================================
CREATE TABLE IF NOT EXISTS store_items (
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

CREATE TABLE IF NOT EXISTS student_inventory (
  id              BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  world_id        BIGINT NOT NULL REFERENCES worlds(id),
  student_id      BIGINT NOT NULL REFERENCES students(id),
  store_item_id   BIGINT NOT NULL REFERENCES store_items(id),
  quantity        INT NOT NULL DEFAULT 1 CHECK (quantity > 0),
  state_config    JSONB NOT NULL DEFAULT '{}'::jsonb,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 16. 청약
-- =====================================================
CREATE TABLE IF NOT EXISTS subscription_rounds (
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

CREATE TABLE IF NOT EXISTS subscription_round_seats (
  round_id        BIGINT NOT NULL REFERENCES subscription_rounds(id),
  seat_id         BIGINT NOT NULL REFERENCES seats(id),
  PRIMARY KEY (round_id, seat_id)
);

CREATE TABLE IF NOT EXISTS subscription_applications (
  id              BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  round_id        BIGINT NOT NULL REFERENCES subscription_rounds(id),
  world_id        BIGINT NOT NULL REFERENCES worlds(id),
  student_id      BIGINT NOT NULL REFERENCES students(id),
  seat_id         BIGINT NOT NULL REFERENCES seats(id),
  status          TEXT NOT NULL DEFAULT 'APPLIED' CHECK (status IN ('APPLIED', 'SELECTED', 'FAILED', 'CANCELLED')),
  created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (round_id, student_id)
);

-- =====================================================
-- 17. 매물 / 계약
-- =====================================================
CREATE TABLE IF NOT EXISTS property_listings (
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

CREATE TABLE IF NOT EXISTS property_contracts (
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

-- =====================================================
-- 인덱스
-- =====================================================
CREATE INDEX IF NOT EXISTS idx_students_world_no
  ON students(world_id, student_no);

CREATE INDEX IF NOT EXISTS idx_jobs_world_status
  ON jobs(world_id, status);

CREATE INDEX IF NOT EXISTS idx_seats_world_position
  ON seats(world_id, row_no, col_no);

CREATE INDEX IF NOT EXISTS idx_ledger_entries_world_created
  ON ledger_entries(world_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_ledger_entries_world_category
  ON ledger_entries(world_id, category, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_activity_logs_world_created
  ON activity_logs(world_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_student_work_items_world_student
  ON student_work_items(world_id, student_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_student_work_items_content_gin
  ON student_work_items USING GIN (content);
