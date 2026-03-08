-- =====================================================
-- JobTable 스키마 정의
-- MySQL 8.0 기준
-- =====================================================

CREATE DATABASE IF NOT EXISTS jobtable
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE jobtable;

-- =====================================================
-- 1. Action_Master (행위 템플릿)
--    직업과 무관하게 행위의 종류와 입력 폼 스키마를 정의
-- =====================================================
CREATE TABLE IF NOT EXISTS Action_Master (
    id           INT          NOT NULL AUTO_INCREMENT,
    action_code  VARCHAR(20)  NOT NULL COMMENT '행위 식별 코드 (예: AUDIT, SALARY_CHECK)',
    display_name VARCHAR(50)  NOT NULL COMMENT '화면 표시명',
    action_config JSON                 COMMENT '입력 폼 스키마 (fields 배열)',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_action_code (action_code)
) COMMENT = '행위 마스터 테이블';

-- =====================================================
-- 2. Job_Templates (직업)
-- =====================================================
CREATE TABLE IF NOT EXISTS Job_Templates (
    id          INT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50)  NOT NULL COMMENT '직업 이름',
    base_salary INT                   DEFAULT 0 COMMENT '기본 급여',
    color       VARCHAR(10)           COMMENT 'UI 표시 색상 (예: #9C27B0)',
    icon        VARCHAR(10)           COMMENT 'UI 표시 아이콘 (예: 🔍)',
    description VARCHAR(200)          COMMENT '직업 설명',
    max_count   INT                   COMMENT '최대 담당 학생 수',
    is_required BOOLEAN               DEFAULT FALSE COMMENT '필수 직업 여부',
    attributes  JSON                  COMMENT '직업별 자유 속성 (확장용)',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id)
) COMMENT = '직업 템플릿 테이블';

-- =====================================================
-- 3. Students (학생)
-- =====================================================
CREATE TABLE IF NOT EXISTS Students (
    id              INT         NOT NULL AUTO_INCREMENT,
    name            VARCHAR(20) NOT NULL COMMENT '학생 이름',
    current_job_id  INT                  COMMENT '현재 담당 직업 (NULL = 미배정)',
    balance         INT                  DEFAULT 0 COMMENT '잔고',

    PRIMARY KEY (id),
    CONSTRAINT fk_students_job
        FOREIGN KEY (current_job_id)
        REFERENCES Job_Templates (id)
        ON DELETE SET NULL
) COMMENT = '학생 테이블';

-- =====================================================
-- 4. Job_Actions (직업-행위 연결)
--    하나의 직업이 여러 행위를 수행할 수 있음 (중복 허용)
-- =====================================================
CREATE TABLE IF NOT EXISTS Job_Actions (
    id        INT NOT NULL AUTO_INCREMENT,
    job_id    INT NOT NULL COMMENT '직업 ID',
    action_id INT NOT NULL COMMENT '행위 ID',

    PRIMARY KEY (id),
    INDEX idx_job_actions_job_id (job_id),
    CONSTRAINT fk_ja_job
        FOREIGN KEY (job_id)
        REFERENCES Job_Templates (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_ja_action
        FOREIGN KEY (action_id)
        REFERENCES Action_Master (id)
        ON DELETE CASCADE
) COMMENT = '직업-행위 연결 테이블 (중복 허용)';

-- =====================================================
-- 5. Job_Workflow (직업 간 문서 전달 경로)
--    fromJob → toJob 방향으로 문서가 전달됨
--    toJob 직업의 학생은 fromJob의 활동 로그를 "받은 문서"로 조회 가능
-- =====================================================
CREATE TABLE IF NOT EXISTS Job_Workflow (
    id            INT          NOT NULL AUTO_INCREMENT,
    from_job_id   INT          NOT NULL COMMENT '발신 직업',
    to_job_id     INT          NOT NULL COMMENT '수신 직업',
    document_type VARCHAR(50)           COMMENT '전달 문서 종류',
    description   VARCHAR(200)          COMMENT '설명',

    PRIMARY KEY (id),
    CONSTRAINT fk_wf_from_job
        FOREIGN KEY (from_job_id)
        REFERENCES Job_Templates (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_wf_to_job
        FOREIGN KEY (to_job_id)
        REFERENCES Job_Templates (id)
        ON DELETE CASCADE
) COMMENT = '직업 간 워크플로우 테이블';

-- =====================================================
-- 6. Activity_Logs (활동 로그)
--    학생이 직업-행위를 수행한 결과를 JSON으로 기록
-- =====================================================
CREATE TABLE IF NOT EXISTS Activity_Logs (
    id            INT      NOT NULL AUTO_INCREMENT,
    student_id    INT      NOT NULL COMMENT '작성 학생',
    job_action_id INT      NOT NULL COMMENT '수행한 직업-행위',
    content       JSON     NOT NULL COMMENT '실제 입력 데이터 (actionConfig 필드 기준)',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_log_student
        FOREIGN KEY (student_id)
        REFERENCES Students (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_log_job_action
        FOREIGN KEY (job_action_id)
        REFERENCES Job_Actions (id)
        ON DELETE CASCADE
) COMMENT = '활동 로그 테이블';
