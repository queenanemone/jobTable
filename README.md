# JobTable API

학생 경제 교육 시뮬레이션 백엔드 서버입니다.
학생에게 직업을 부여하고, 직업별 행위(장부)를 수행한 결과를 기록·공유합니다.

## 기술 스택

- Java 17, Spring Boot 3.4.3
- Spring Data JPA, MySQL 8.0
- SpringDoc OpenAPI (Swagger UI)

## 실행 방법

```bash
# MySQL 데이터베이스 생성
CREATE DATABASE jobtable;

# 애플리케이션 실행
./gradlew bootRun
```

- 기본 포트: `8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Spring Security 임시 계정: `admin` / `admin`

---

## API 명세

**Base URL:** `http://localhost:8080/api`
**Content-Type:** `application/json`

---

### 1. 행위 마스터 (Action)

행위 템플릿을 관리합니다. 각 행위는 `actionConfig`로 입력 폼 스키마를 정의합니다.

#### `GET /api/actions` — 행위 목록 조회

**Response 200**
```json
[
  {
    "id": 1,
    "actionCode": "SALARY_CHECK",
    "displayName": "급여 확인",
    "actionConfig": {
      "fields": [
        { "key": "amount", "label": "금액", "type": "number" }
      ]
    },
    "createdAt": "2025-01-01T00:00:00"
  }
]
```

#### `GET /api/actions/{id}` — 행위 단건 조회

#### `POST /api/actions` — 행위 생성

**Request Body**
```json
{
  "actionCode": "SALARY_CHECK",
  "displayName": "급여 확인",
  "actionConfig": {
    "fields": [
      { "key": "amount", "label": "금액", "type": "number" },
      { "key": "reason", "label": "사유",  "type": "text"   }
    ]
  }
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|:----:|------|
| `actionCode` | String(20) | O | 자동으로 대문자 저장, 고유값 |
| `displayName` | String(50) | O | 화면 표시명 |
| `actionConfig` | Object | X | 입력 폼 스키마 |

**Response 201**

#### `PUT /api/actions/{id}` — 행위 수정

Request Body — 생성과 동일한 구조
**Response 200**

#### `DELETE /api/actions/{id}` — 행위 삭제

**Response 204**

---

### 2. 직업 (Job)

#### `GET /api/jobs` — 직업 목록 조회

**Response 200**
```json
[
  {
    "id": 1,
    "name": "은행원",
    "baseSalary": 50000,
    "attributes": { "maxStudents": 3 },
    "jobActions": [
      {
        "id": 1,
        "actionCode": "DEPOSIT",
        "displayName": "입금 처리",
        "actionConfig": { "fields": [ "..." ] }
      }
    ],
    "createdAt": "2025-01-01T00:00:00"
  }
]
```

#### `GET /api/jobs/{id}` — 직업 단건 조회

#### `POST /api/jobs` — 직업 생성

**Request Body**
```json
{
  "name": "은행원",
  "baseSalary": 50000,
  "attributes": { "maxStudents": 3 }
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|:----:|------|
| `name` | String | O | 직업 이름 |
| `baseSalary` | Integer | X | 기본 급여 (기본값 0) |
| `attributes` | Object | X | 자유 속성 JSON |

**Response 201**

#### `PUT /api/jobs/{id}` — 직업 수정

Request Body — 생성과 동일
**Response 200**

#### `DELETE /api/jobs/{id}` — 직업 삭제

연결된 `JobAction`, `JobWorkflow`도 함께 삭제됩니다.
**Response 204**

#### `GET /api/jobs/{id}/actions` — 직업의 행위 목록 조회

폼 렌더링용 스키마를 반환합니다.

**Response 200**
```json
[
  {
    "id": 1,
    "actionId": 3,
    "actionCode": "DEPOSIT",
    "displayName": "입금 처리",
    "actionConfig": { "fields": [ "..." ] }
  }
]
```

#### `POST /api/jobs/{id}/actions` — 직업에 행위 연결

**Request Body**
```json
{ "actionId": 3 }
```

| 필드 | 타입 | 필수 |
|------|------|:----:|
| `actionId` | Integer | O |

**Response 201** — 동일한 행위를 중복 연결할 수 있습니다.

---

### 3. 직업-행위 연결 (JobAction)

#### `GET /api/job-actions/{id}` — 직업-행위 단건 조회

활동 로그 입력 폼을 렌더링할 때 `actionConfig`를 가져오는 용도입니다.

**Response 200**
```json
{
  "id": 1,
  "actionId": 3,
  "actionCode": "DEPOSIT",
  "displayName": "입금 처리",
  "actionConfig": { "fields": [ "..." ] }
}
```

#### `DELETE /api/job-actions/{id}` — 직업-행위 연결 삭제

**Response 204**

---

### 4. 학생 (Student)

#### `GET /api/students` — 학생 목록 조회

**Response 200**
```json
[
  {
    "id": 1,
    "name": "홍길동",
    "balance": 10000,
    "currentJobId": 2,
    "currentJobName": "은행원",
    "createdAt": "2025-01-01T00:00:00"
  }
]
```

#### `GET /api/students/{id}` — 학생 단건 조회

#### `POST /api/students` — 학생 생성

**Request Body**
```json
{
  "name": "홍길동",
  "balance": 0,
  "currentJobId": null
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|:----:|------|
| `name` | String(20) | O | 학생 이름 |
| `balance` | Integer | X | 잔고 (기본값 0) |
| `currentJobId` | Integer | X | 현재 직업 ID |

**Response 201**

#### `PUT /api/students/{id}` — 학생 정보 수정

Request Body — 생성과 동일
**Response 200**

#### `DELETE /api/students/{id}` — 학생 삭제

**Response 204**

#### `PATCH /api/students/{id}/job` — 학생 직업 변경

**Request Body**
```json
{ "jobId": 2 }
```

> `jobId`를 `null`로 보내면 직업이 해제됩니다.

**Response 200**

---

### 5. 활동 로그 (ActivityLog)

학생이 직업에서 수행한 행위 결과를 기록합니다.

#### `GET /api/activity-logs` — 활동 로그 조회

| 파라미터 | 위치 | 타입 | 필수 | 설명 |
|---------|------|------|:----:|------|
| `studentId` | query | Integer | X | 없으면 전체 조회 |

**Response 200**
```json
[
  {
    "id": 1,
    "studentId": 1,
    "studentName": "홍길동",
    "jobActionId": 2,
    "actionCode": "DEPOSIT",
    "jobName": "은행원",
    "content": { "amount": 50000, "reason": "급여" },
    "createdAt": "2025-01-01T12:00:00"
  }
]
```

#### `GET /api/activity-logs/by-job-action` — 직업-행위별 로그 조회

| 파라미터 | 위치 | 타입 | 필수 |
|---------|------|------|:----:|
| `jobActionId` | query | Integer | O |

**Response 200** — 위와 동일한 배열

#### `GET /api/activity-logs/received` — 받은 문서 조회

학생의 현재 직업으로 워크플로우가 연결된 상위 직업들이 작성한 로그를 반환합니다.

| 파라미터 | 위치 | 타입 | 필수 |
|---------|------|------|:----:|
| `studentId` | query | Integer | O |

**Response 200** — 위와 동일한 배열

#### `POST /api/activity-logs` — 활동 로그 저장

**Request Body**
```json
{
  "studentId": 1,
  "jobActionId": 2,
  "content": { "amount": 50000, "reason": "급여 지급" }
}
```

| 필드 | 타입 | 필수 |
|------|------|:----:|
| `studentId` | Integer | O |
| `jobActionId` | Integer | O |
| `content` | Object | O |

**Response 201**

---

### 6. 워크플로우 (Workflow)

직업 간 문서 전달 경로를 정의합니다.
워크플로우를 설정하면 수신 직업의 학생이 "받은 문서"를 통해 발신 직업의 활동 로그를 조회할 수 있습니다.

#### `GET /api/workflows` — 워크플로우 조회

| 파라미터 | 위치 | 타입 | 필수 | 설명 |
|---------|------|------|:----:|------|
| `fromJobId` | query | Integer | X | 발신 직업 기준 필터 |
| `toJobId` | query | Integer | X | 수신 직업 기준 필터 |

파라미터 없으면 전체 조회.

**Response 200**
```json
[
  {
    "id": 1,
    "fromJobId": 1,
    "fromJobName": "은행원",
    "toJobId": 2,
    "toJobName": "감사관",
    "documentType": "입금내역",
    "description": "은행원이 감사관에게 입금 내역 전달"
  }
]
```

#### `POST /api/workflows` — 워크플로우 생성

**Request Body**
```json
{
  "fromJobId": 1,
  "toJobId": 2,
  "documentType": "입금내역",
  "description": "은행원이 감사관에게 입금 내역 전달"
}
```

| 필드 | 타입 | 필수 |
|------|------|:----:|
| `fromJobId` | Integer | O |
| `toJobId` | Integer | O |
| `documentType` | String | X |
| `description` | String | X |

**Response 201**

#### `DELETE /api/workflows/{id}` — 워크플로우 삭제

**Response 204**

---

## 공통 에러 응답

| 상태코드 | 설명 |
|---------|------|
| `400 Bad Request` | 필수값 누락 또는 유효성 검사 실패 |
| `404 Not Found` | 대상 리소스를 찾을 수 없음 |
| `500 Internal Server Error` | 서버 내부 오류 |

---

## 데이터 모델 관계

```
ActionMaster (행위 템플릿)
    └── JobAction (직업-행위 연결, N:M)
            └── ActivityLog (학생이 수행한 결과 기록)

JobTemplate (직업)
    ├── JobAction
    ├── JobWorkflow fromJob (발신)
    └── JobWorkflow toJob  (수신)

Student (학생)
    ├── currentJob → JobTemplate
    └── ActivityLog
```
