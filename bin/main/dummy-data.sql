-- =====================================================
-- 학급 경제 교육 플랫폼 더미 데이터
-- PostgreSQL 기준
-- =====================================================

TRUNCATE TABLE activity_logs  RESTART IDENTITY CASCADE;
TRUNCATE TABLE job_workflow    RESTART IDENTITY CASCADE;
TRUNCATE TABLE job_actions     RESTART IDENTITY CASCADE;
TRUNCATE TABLE students        RESTART IDENTITY CASCADE;
TRUNCATE TABLE action_master   RESTART IDENTITY CASCADE;
TRUNCATE TABLE job_templates   RESTART IDENTITY CASCADE;

-- =====================================================
-- 1. job_templates
-- =====================================================
INSERT INTO job_templates (id, name, base_salary, color, icon, description, max_count, is_required) VALUES
(1, '감사원',       1500, '#9C27B0', '🔍', '필수 직업 장부 감사 및 학급 회의 주최', 2, true),
(2, '은행원',       1200, '#2196F3', '🏦', '월급 확인 및 저축 상품 관리',           1, true),
(3, '통계청',       1100, '#4CAF50', '📊', '날짜별 제출물 현황 통계표 작성',         1, true),
(4, '경찰',         1000, '#F44336', '👮', '학급 질서 관리 및 벌금 대장 작성',       1, true),
(5, '신용평가위원', 1300, '#FF9800', '⭐', '신용등급 책정 및 신용평가 보고서 제출',  1, true),
(6, '노동청',       1100, '#795548', '🏛', '선택 직업 역할 수행 관리',               1, true),
(7, '국세청',       1400, '#607D8B', '💰', '학급 세금 수입/지출 관리',               1, true);

SELECT setval('job_templates_id_seq', (SELECT MAX(id) FROM job_templates));

-- =====================================================
-- 2. action_master (action_config 없음 — 폼 스키마는 job_actions로 이동)
-- =====================================================
INSERT INTO action_master (id, action_code, display_name) VALUES
( 1, 'AUDIT',         '감사'),
( 2, 'MEETING',       '회의 주최'),
( 3, 'SALARY_CHECK',  '월급 확인'),
( 4, 'DEPOSIT',       '예금 판매'),
( 5, 'STATISTICS',    '통계 작성'),
( 6, 'FINE',          '벌금 부과'),
( 7, 'CREDIT_REPORT', '신용평가'),
( 8, 'COMPLAINT',     '민원 처리'),
( 9, 'JOB_CHECK',     '업무 점검'),
(10, 'TAX_LEDGER',    '세수 관리');

SELECT setval('action_master_id_seq', (SELECT MAX(id) FROM action_master));

-- =====================================================
-- 3. students
-- =====================================================
INSERT INTO students (id, name, current_job_id, balance) VALUES
(1,  '김민준', 1, 3200),
(2,  '이서연', 1, 2900),
(3,  '박지훈', 2, 4100),
(4,  '최예은', 3, 3500),
(5,  '정도윤', 4, 2800),
(6,  '강하은', 5, 3700),
(7,  '윤시우', 6, 3300),
(8,  '한지아', 7, 5200),
(9,  '조현우', NULL, 1500),
(10, '임나은', NULL, 1500);

SELECT setval('students_id_seq', (SELECT MAX(id) FROM students));

-- =====================================================
-- 4. job_actions (action_config = 직업별 입력 폼 스키마 JSONB)
--    같은 action_code라도 직업마다 다른 폼 설정 가능
--    예: job_action 8, 10 모두 COMPLAINT지만 노동청(10)은 "관련 직업" 필드 추가
-- =====================================================
INSERT INTO job_actions (id, job_id, action_id, action_config) VALUES
( 1, 1,  1, '{
  "title": "필수 직업 감사 대장",
  "fields": [
    {"name":"직업명",    "type":"select", "options":["은행원","통계청","경찰","신용평가위원","노동청","국세청"]},
    {"name":"감사 항목", "type":"text"},
    {"name":"이상 여부", "type":"select", "options":["정상","이상 있음"]},
    {"name":"특이사항",  "type":"text"},
    {"name":"날짜",      "type":"date"}
  ]
}'),
( 2, 1,  2, '{
  "title": "학급 회의 기록",
  "fields": [
    {"name":"회의 날짜", "type":"date"},
    {"name":"참석 직업", "type":"text"},
    {"name":"안건",      "type":"text"},
    {"name":"결과",      "type":"text"},
    {"name":"비고",      "type":"text"}
  ]
}'),
( 3, 2,  3, '{
  "title": "월급 확인 체크리스트",
  "fields": [
    {"name":"직업명",    "type":"select", "options":["감사원","은행원","통계청","경찰","신용평가위원","노동청","국세청"]},
    {"name":"기본급",    "type":"number", "min":0},
    {"name":"세율(%)",   "type":"number", "min":0, "max":100},
    {"name":"실지급액",  "type":"number", "min":0},
    {"name":"지급 확인", "type":"select", "options":["O","X"]}
  ]
}'),
( 4, 2,  4, '{
  "title": "예금 판매 대장",
  "fields": [
    {"name":"가입자",       "type":"text"},
    {"name":"신용등급",     "type":"select", "options":["A+","A","B+","B","C+","C","D"]},
    {"name":"가입기간(일)", "type":"select", "options":["14","28"]},
    {"name":"원금",         "type":"number", "min":0},
    {"name":"이율(%)",      "type":"number", "min":0},
    {"name":"만기일",       "type":"date"}
  ]
}'),
( 5, 3,  5, '{
  "title": "제출물 현황 통계표",
  "fields": [
    {"name":"날짜",          "type":"date"},
    {"name":"일기",          "type":"select", "options":["O","X"]},
    {"name":"숙제",          "type":"select", "options":["O","X"]},
    {"name":"작품",          "type":"select", "options":["O","X"]},
    {"name":"가정통신문",    "type":"select", "options":["O","X"]},
    {"name":"미제출자 명단", "type":"text"}
  ]
}'),
( 6, 4,  6, '{
  "title": "벌금 대장",
  "fields": [
    {"name":"납부자",    "type":"text"},
    {"name":"벌금 종류", "type":"select", "options":["지각","수업 중 휴대폰 사용","청소 불이행","급식 규칙 위반","기타"]},
    {"name":"금액",      "type":"number", "min":0},
    {"name":"부과 날짜", "type":"date"}
  ]
}'),
( 7, 5,  7, '{
  "title": "신용 평가 보고서",
  "fields": [
    {"name":"평가 대상",     "type":"text"},
    {"name":"통계 점수",     "type":"number", "min":0, "max":100},
    {"name":"벌금 횟수",     "type":"number", "min":0},
    {"name":"최종 신용등급", "type":"select", "options":["A+","A","B+","B","C+","C","D"]},
    {"name":"특이사항",      "type":"text"}
  ]
}'),
( 8, 5,  8, '{
  "title": "민원 처리 대장",
  "fields": [
    {"name":"민원인",    "type":"text"},
    {"name":"민원 내용", "type":"text"},
    {"name":"처리 결과", "type":"select", "options":["수용","기각","보류"]},
    {"name":"처리 날짜", "type":"date"}
  ]
}'),
( 9, 6,  9, '{
  "title": "선택 직업 업무 수행 점검표",
  "fields": [
    {"name":"직업명",         "type":"text"},
    {"name":"담당 학생",      "type":"text"},
    {"name":"업무 수행 여부", "type":"select", "options":["O","X"]},
    {"name":"점수",           "type":"number", "min":0, "max":10},
    {"name":"비고",           "type":"text"}
  ]
}'),
(10, 6,  8, '{
  "title": "민원 처리 대장 (노동청)",
  "fields": [
    {"name":"민원인",    "type":"text"},
    {"name":"민원 내용", "type":"text"},
    {"name":"관련 직업", "type":"text"},
    {"name":"처리 결과", "type":"select", "options":["수용","기각","보류"]},
    {"name":"처리 날짜", "type":"date"}
  ]
}'),
(11, 7, 10, '{
  "title": "세수 입출금 대장",
  "fields": [
    {"name":"항목", "type":"text"},
    {"name":"유형", "type":"select", "options":["수입","지출"]},
    {"name":"금액", "type":"number", "min":0},
    {"name":"날짜", "type":"date"},
    {"name":"비고", "type":"text"}
  ]
}');

SELECT setval('job_actions_id_seq', (SELECT MAX(id) FROM job_actions));

-- =====================================================
-- 5. job_workflow
-- =====================================================
INSERT INTO job_workflow (from_job_id, to_job_id, document_type, description) VALUES
(4, 7, '벌금신고',       '경찰 → 국세청: 징수한 벌금 신고'),
(4, 5, '벌금대장',       '경찰 → 신용평가위원: 벌금 부과 내역 제출'),
(3, 5, '통계표',         '통계청 → 신용평가위원: 주간 제출물 현황 통계표 제출'),
(6, 5, '업무수행대장',   '노동청 → 신용평가위원: 선택 직업 점검 결과 제출'),
(5, 1, '신용평가보고서', '신용평가위원 → 감사원: 신용 평가 결과 공유');

-- =====================================================
-- 6. activity_logs (content = JSONB 문서)
--    직업별로 스키마가 다른 자유형식 데이터를 JSONB로 저장
-- =====================================================
INSERT INTO activity_logs (student_id, job_action_id, content) VALUES
(5,  6, '{"납부자":"조현우","벌금 종류":"수업 중 휴대폰 사용","금액":"200","부과 날짜":"2026-03-03"}'),
(5,  6, '{"납부자":"임나은","벌금 종류":"청소 불이행","금액":"300","부과 날짜":"2026-03-04"}'),
(5,  6, '{"납부자":"조현우","벌금 종류":"지각","금액":"100","부과 날짜":"2026-03-05"}'),
(4,  5, '{"날짜":"2026-03-03","일기":"O","숙제":"O","작품":"X","가정통신문":"O","미제출자 명단":"박지훈"}'),
(4,  5, '{"날짜":"2026-03-04","일기":"O","숙제":"O","작품":"O","가정통신문":"O","미제출자 명단":"없음"}'),
(3,  3, '{"직업명":"감사원","기본급":"1500","세율(%)":"10","실지급액":"1350","지급 확인":"O"}'),
(3,  3, '{"직업명":"경찰","기본급":"1000","세율(%)":"10","실지급액":"900","지급 확인":"O"}'),
(3,  4, '{"가입자":"조현우","신용등급":"A","가입기간(일)":"14","원금":"500","이율(%)":"5","만기일":"2026-03-17"}'),
(8, 11, '{"항목":"소득세 징수","유형":"수입","금액":"2400","날짜":"2026-03-03","비고":"3월 1주차 월급 지급분"}'),
(8, 11, '{"항목":"쓰레기봉투 구매","유형":"지출","금액":"300","날짜":"2026-03-04","비고":"20L 3장"}'),
(1,  1, '{"직업명":"경찰","감사 항목":"벌금 대장 작성 여부","이상 여부":"정상","특이사항":"없음","날짜":"2026-03-05"}'),
(1,  1, '{"직업명":"통계청","감사 항목":"통계표 제출 여부","이상 여부":"정상","특이사항":"없음","날짜":"2026-03-05"}'),
(6,  7, '{"평가 대상":"조현우","통계 점수":"70","벌금 횟수":"2","최종 신용등급":"B","특이사항":"지각 및 휴대폰 사용으로 등급 하락"}'),
(6,  7, '{"평가 대상":"임나은","통계 점수":"60","벌금 횟수":"1","최종 신용등급":"B+","특이사항":"청소 불이행 1회"}');
