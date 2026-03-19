# 하나로 금융몰 (Hanaro Finance Mall)

하나 은행의 금융 상품(정기예금, 적금) 가입 및 관리 서비스 백엔드 API입니다.

---

## 목차

- [프로젝트 소개](#프로젝트-소개)
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [ERD](#erd)
- [API 명세](#api-명세)
- [실행 방법](#실행-방법)
- [테스트 계정](#테스트-계정)

---

## 프로젝트 소개

**하나로 금융몰**은 사용자가 금융 상품(정기예금·적금)을 조회하고 가입할 수 있는 REST API 서비스입니다.

### 주요 기능

| 기능 | 설명 |
|------|------|
| 회원가입 / 로그인 | JWT 기반 인증, 회원가입 시 기본 계좌 자동 생성 |
| 상품 조회 | 정기예금 · 적금 상품 목록 및 상세 조회 |
| 상품 가입 | 기본 계좌 잔액 차감 후 예금/적금 계좌 생성 |
| 계좌 관리 | 계좌 목록/상세 조회, 입금, 이체, 해지 |
| 구독 조회 | 활성화된 금융 상품 구독 목록 조회 |
| 이자 계산 | 만기 이자 / 중도해지 이자 자동 계산 |
| 관리자 기능 | 상품 CRUD, 이미지 관리, 회원 조회, 만기 처리 |

### 비즈니스 규칙

- **정기예금**: 가입 즉시 기본 계좌에서 납입금 차감
- **적금**: 월별/주별 납입일에 기본 계좌 → 적금 계좌 자동 이체
- **만기 이자 계산**:
  - 정기예금: `납입금 × 수익률 × (개월수 / 12)`
  - 적금: `납입금 × sum(1..N개월) × (월 수익률)`
- **중도해지**: 경과 개월수 기준으로 해지 수익률 적용
- **계좌번호**: 11자리 숫자, DB 저장 시 암호화

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 4.0.3 |
| **ORM** | Spring Data JPA + QueryDSL |
| **Database** | MySQL |
| **Security** | Spring Security + JWT (jjwt 0.12.6) |
| **Mapping** | MapStruct 1.6.3 |
| **Validation** | Spring Validation |
| **API Docs** | SpringDoc OpenAPI 3.0.1 (Swagger UI) |
| **Image Processing** | Thumbnailator 0.4.21 |
| **ID Generation** | Hypersistence TSID (User, Account) |
| **Monitoring** | Spring Actuator |
| **Test Coverage** | JaCoCo (브랜치 커버리지 80% 이상) |
| **Logging** | Logback (도메인별 파일 분리) |
| **Build Tool** | Gradle |

---

## 프로젝트 구조

```
src/main/java/com/hana8/hanaro/
├── config/                  # Security, CORS 설정
├── controller/              # REST 컨트롤러
│   ├── AuthController
│   ├── ProductController
│   ├── ProductAdminController
│   ├── ProductImageController
│   ├── ProductImageAdminController
│   ├── AccountController
│   ├── SubscriptionController
│   └── UserAdminController
├── service/                 # 비즈니스 로직
│   ├── AuthService
│   ├── UserService
│   ├── ProductService
│   ├── AccountService
│   ├── SubscriptionService
│   ├── UserAdminService
│   ├── InterestCalculator
│   └── CustomUserDetailsService
├── entity/                  # JPA 엔티티
│   ├── User
│   ├── Account
│   ├── Product
│   ├── ProductImage
│   └── Subscription
├── dto/                     # 요청/응답 DTO
├── mapper/                  # MapStruct 매퍼
├── repository/              # JPA + Custom QueryDSL 레포지토리
├── common/
│   ├── enums/               # Role, ProductType, AccountType, AccountStatus, PaymentCycle
│   ├── exception/           # GlobalExceptionHandler, ErrorCode, SuccessCode
│   ├── converter/           # 계좌번호 암호화 컨버터
│   └── validator/           # @AccountNumber, @ValidPaymentCycle
└── InitLoader.java          # 초기 데이터 로더
```

---

## ERD

```
┌─────────────────────────────────────────────────────────────────────┐
│  User                                                               │
│  ─────────────────────────────────────────────────────────────────  │
│  id (TSID, PK)                                                      │
│  email        VARCHAR UNIQUE                                        │
│  password     VARCHAR (BCrypt)                                      │
│  name         VARCHAR                                               │
│  role         ENUM (ROLE_ADMIN, ROLE_USER)                         │
│  created_at / updated_at                                            │
└──────────────────────┬──────────────────────────────────────────────┘
                       │ 1:N
        ┌──────────────┴───────────────────────────────┐
        ▼                                              ▼
┌───────────────────────────┐          ┌───────────────────────────────────────┐
│  Account                  │          │  Subscription                         │
│  ─────────────────────── │          │  ─────────────────────────────────── │
│  id (TSID, PK)            │          │  id (AUTO_INC, PK)                    │
│  account_num  VARCHAR     │◄─────────│  account_id (FK)                      │
│  account_type ENUM        │          │  user_id    (FK)                      │
│  balance      BIGINT      │          │  product_id (FK)                      │
│  user_id (FK)             │          │  payment_amount    BIGINT             │
│  created_at / updated_at  │          │  payment_cycle     ENUM               │
└───────────────────────────┘          │  payment_day       INT                │
                                       │  payment_day_of_week ENUM            │
                                       │  join_date         DATE               │
                                       │  end_date          DATE               │
                                       │  status            ENUM               │
                                       │  maturity_interest DECIMAL            │
                                       │  matured_at / cancelled_at            │
                                       │  created_at / updated_at              │
                                       └────────────────┬──────────────────────┘
                                                        │ N:1
                                                        ▼
                                       ┌───────────────────────────────────────┐
                                       │  Product                              │
                                       │  ─────────────────────────────────── │
                                       │  id (AUTO_INC, PK)                    │
                                       │  product_name   VARCHAR               │
                                       │  product_type   ENUM (DEPOSIT/SAVINGS)│
                                       │  min / max      BIGINT                │
                                       │  period         INT (개월)            │
                                       │  maturity_yield DECIMAL (%)           │
                                       │  cancel_yield   DECIMAL (%)           │
                                       │  description    TEXT                  │
                                       │  deleted        BOOLEAN               │
                                       │  created_at / updated_at              │
                                       └────────────────┬──────────────────────┘
                                                        │ 1:N
                                                        ▼
                                       ┌───────────────────────────────────────┐
                                       │  ProductImage                         │
                                       │  ─────────────────────────────────── │
                                       │  id (AUTO_INC, PK)                    │
                                       │  org_name   VARCHAR (원본 파일명)     │
                                       │  save_name  VARCHAR (UUID 파일명)     │
                                       │  save_dir   VARCHAR (날짜별 경로)     │
                                       │  deleted    BOOLEAN                   │
                                       │  product_id (FK)                      │
                                       │  created_at / updated_at              │
                                       └───────────────────────────────────────┘
```

### 주요 관계

| 관계 | 설명 |
|------|------|
| User → Account | 1:N (회원 삭제 시 계좌 cascade 삭제) |
| User → Subscription | 1:N (회원 삭제 시 구독 cascade 삭제) |
| Product → ProductImage | 1:N (상품 삭제 시 이미지 cascade 삭제, soft delete) |
| Account ↔ Subscription | 1:1 (가입 1건당 계좌 1개) |
| Product → Subscription | 1:N |

### Enum 목록

| Enum | 값 |
|------|----|
| `Role` | `ROLE_ADMIN`, `ROLE_USER` |
| `ProductType` | `DEPOSIT`(정기예금), `SAVINGS`(적금) |
| `AccountType` | `BASIC`(기본), `DEPOSIT`(예금), `SAVINGS`(적금) |
| `AccountStatus` | `ACTIVE`(활성), `MATURED`(만기), `CANCELLED`(해지) |
| `PaymentCycle` | `MONTHLY`(월별), `WEEKLY`(주별) |

---

## API 명세

Swagger UI: `http://localhost:8080/swagger-ui/index.html`

---

### 인증 (`/api/auth`)

| 메서드 | 경로 | 설명 | 인증 |
|--------|------|------|------|
| `POST` | `/api/auth/signup` | 회원가입 | 불필요 |
| `POST` | `/api/auth/signin` | 로그인 | 불필요 |
| `POST` | `/api/auth/refresh` | 토큰 갱신 | 불필요 |

**POST /api/auth/signup**
```json
// 요청
{
  "email": "user@example.com",
  "passwd": "password123!",
  "name": "홍길동",
  "accountNum": "12345678901"  // 선택, 미입력 시 자동 생성
}

// 응답 200
{
  "id": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "accountId": 2,
  "accountNum": "123****8901"
}
```

**POST /api/auth/signin**
```json
// 요청
{
  "email": "user@example.com",
  "passwd": "password123!"
}

// 응답 200
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ..."
}
```

**POST /api/auth/refresh**
```json
// 요청 Header: Authorization: Bearer {accessToken}
// 요청 Body
{
  "refreshToken": "eyJ..."
}

// 응답 200
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ..."
}
```

---

### 상품 (`/api/products`)

| 메서드 | 경로 | 설명 | 인증 |
|--------|------|------|------|
| `GET` | `/api/products` | 상품 목록 조회 | 불필요 |
| `GET` | `/api/products/{productId}` | 상품 상세 조회 | 불필요 |
| `POST` | `/api/products/{productId}/subscribe` | 상품 가입 | 필요 |

**GET /api/products**
```json
// 응답 200
[
  {
    "id": 1,
    "productName": "하나 안심 정기예금",
    "productType": "DEPOSIT",
    "maturityYield": 3.50,
    "period": 12,
    "thumbImage": { "id": 1, "orgName": "...", "saveName": "...", "saveDir": "..." }
  }
]
```

**GET /api/products/{productId}**
```json
// 응답 200
{
  "id": 1,
  "productName": "하나 안심 정기예금",
  "productType": "DEPOSIT",
  "min": 100000,
  "max": 100000000,
  "period": 12,
  "maturityYield": 3.50,
  "cancelYield": 1.00,
  "description": "안정적인 정기예금 상품입니다.",
  "images": [...]
}
```

**POST /api/products/{productId}/subscribe**
```json
// 요청 (정기예금)
{
  "paymentAmount": 1000000,
  "paymentCycle": null,
  "paymentDay": null,
  "paymentDayOfWeek": null,
  "wishAccountNum": "11012345678"
}

// 요청 (월별 적금)
{
  "paymentAmount": 100000,
  "paymentCycle": "MONTHLY",
  "paymentDay": 25,
  "paymentDayOfWeek": null,
  "wishAccountNum": "11012345678"
}

// 요청 (주별 적금)
{
  "paymentAmount": 50000,
  "paymentCycle": "WEEKLY",
  "paymentDay": null,
  "paymentDayOfWeek": "MONDAY",
  "wishAccountNum": "11012345678"
}

// 응답 200 (body 없음)
```

---

### 상품 이미지 (`/api/products/{productId}/images`)

| 메서드 | 경로 | 설명 | 인증 |
|--------|------|------|------|
| `GET` | `/api/products/{productId}/images/{imageId}` | 이미지 조회 | 불필요 |

> `?inline=true` 파라미터로 브라우저 인라인 표시 가능

---

### 계좌 (`/api/accounts`)

| 메서드 | 경로 | 설명 | 인증 |
|--------|------|------|------|
| `GET` | `/api/accounts` | 계좌 목록 조회 | 필요 |
| `GET` | `/api/accounts/{accountId}` | 계좌 상세 조회 | 필요 |
| `POST` | `/api/accounts/{accountId}/deposit` | 입금 | 필요 |
| `POST` | `/api/accounts/{accountId}/transfer` | 납입일 이체 | 필요 |
| `POST` | `/api/accounts/{accountId}/cancel` | 중도해지 | 필요 |

**GET /api/accounts**
```json
// 응답 200 (기본 계좌 우선 정렬)
[
  {
    "accountId": 1,
    "accountNum": "110****5678",
    "accountType": "BASIC",
    "balance": 10000000,
    "productName": null,
    "joinDate": null,
    "endDate": null,
    "status": null
  },
  {
    "accountId": 2,
    "accountNum": "110****5432",
    "accountType": "DEPOSIT",
    "balance": 5000000,
    "productName": "하나 안심 정기예금",
    "joinDate": "2025-03-19",
    "endDate": "2026-03-19",
    "status": "ACTIVE",
    "paymentAmount": 5000000,
    "maturityYield": 3.50,
    "cancelYield": 1.00,
    "maturityInterest": 175000,
    "cancelInterest": 50000
  }
]
```

**POST /api/accounts/{accountId}/deposit**
```json
// 요청
{ "amount": 100000 }

// 응답 200 (body 없음)
```

---

### 구독 (`/api/subscriptions`)

| 메서드 | 경로 | 설명 | 인증 |
|--------|------|------|------|
| `GET` | `/api/subscriptions` | 구독 목록 조회 | 필요 |

---

### 관리자 - 상품 (`/api/admin/products`)

> 모든 관리자 API는 `ROLE_ADMIN` 권한 필요

| 메서드 | 경로 | 설명 |
|--------|------|------|
| `POST` | `/api/admin/products` | 상품 등록 |
| `PUT` | `/api/admin/products/{productId}` | 상품 수정 |
| `DELETE` | `/api/admin/products/{productId}` | 상품 삭제 (soft delete) |

**POST /api/admin/products**
```json
// 요청
{
  "productName": "신규 정기예금",
  "productType": "DEPOSIT",
  "min": 100000,
  "max": 50000000,
  "period": 6,
  "maturityYield": 3.00,
  "cancelYield": 1.00,
  "description": "상품 설명"
}
```

---

### 관리자 - 상품 이미지 (`/api/admin/products/{productId}/images`)

| 메서드 | 경로 | 설명 |
|--------|------|------|
| `POST` | `/api/admin/products/{productId}/images` | 이미지 업로드 (multipart/form-data) |
| `DELETE` | `/api/admin/products/{productId}/images/{imageId}` | 이미지 삭제 (soft delete) |

> 이미지 업로드 시 썸네일(200x200) 자동 생성, 파일당 최대 2MB

---

### 관리자 - 회원 (`/api/admin/users`)

| 메서드 | 경로 | 설명 |
|--------|------|------|
| `GET` | `/api/admin/users` | 전체 회원 목록 |
| `GET` | `/api/admin/users/search?keyword={keyword}` | 회원 검색 (이름/이메일) |
| `GET` | `/api/admin/users/{userId}/subscriptions` | 회원 구독 조회 |
| `POST` | `/api/admin/users/{userId}/accounts/{accountId}/maturity` | 만기 처리 |

---

### 공통 응답 형식

**성공**
```json
{
  "code": "SUCCESS",
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": { ... }
}
```

**실패**
```json
{
  "code": "AUTH_001",
  "message": "인증이 필요합니다.",
  "data": null
}
```

**주요 에러 코드**

| 코드 | 설명 |
|------|------|
| `AUTH_001` | 인증 필요 |
| `AUTH_002` | 접근 권한 없음 |
| `AUTH_003` | 토큰 만료 |
| `USER_001` | 사용자 없음 |
| `USER_002` | 이메일 중복 |
| `PRODUCT_001` | 상품 없음 |
| `IMAGE_001~007` | 이미지 관련 오류 |
| `ACCOUNT_001~004` | 계좌 관련 오류 |
| `SUB_001~003` | 구독 관련 오류 |

---

## 실행

| 서비스 | URL |
|--------|-----|
| API 서버 | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` |
| Actuator | `http://localhost:8080/actuator` |

---

## Seed 데이터 정보

> 서버 최초 실행 시 `InitLoader`가 자동으로 초기 데이터를 삽입합니다.


### 관리자

| 항목 | 값 |
|------|-----|
| 이메일 | `hanaro@hanaro.com` |
| 비밀번호 | `12345678` |
| 역할 | ADMIN |

### 일반 사용자 1 - 홍길동

| 항목 | 값 |
|------|-----|
| 이메일 | `somi@gmail.com` |
| 비밀번호 | `somihappy2002!` |
| 역할 | USER |

**보유 계좌**

| 계좌번호 | 유형 | 잔액 | 상태 |
|----------|------|------|------|
| `11012345678` | 기본 | ₩10,000,000 | - |
| `11098765432` | 정기예금 | ₩5,000,000 | ACTIVE |
| `11011122233` | 적금 (월 19일) | ₩900,000 | ACTIVE |
| `11055566677` | 적금 (주별) | ₩400,000 | ACTIVE |
| `11044455566` | 적금 (만기 도래) | ₩1,000,000 | ACTIVE |
| `11077788899` | 적금 | ₩0 | MATURED |
| `11088899900` | 적금 | ₩0 | CANCELLED |

### 일반 사용자 2 - 김하나

| 항목 | 값 |
|------|-----|
| 이메일 | `user2@hanaro.com` |
| 비밀번호 | `user1234!` |
| 기본 계좌 | `11022233344` (₩100) |

### 일반 사용자 3 - 이철수

| 항목 | 값 |
|------|-----|
| 이메일 | `user3@hanaro.com` |
| 비밀번호 | `user1234!` |
| 기본 계좌 | `11066677788` (₩3,000,000) |

---

### 초기 등록 상품

| 상품명 | 유형 | 기간 | 만기 수익률 | 해지 수익률 | 최소 | 최대 |
|--------|------|------|------------|------------|------|------|
| 하나 안심 정기예금 | 정기예금 | 12개월 | 3.50% | 1.00% | ₩100,000 | ₩100,000,000 |
| 하나 목돈 마련 적금 | 적금 | 12개월 | 4.20% | 1.50% | ₩10,000 | ₩1,000,000 |
| 하나 주간 적금 | 적금 | 12개월 | 3.80% | 1.20% | ₩10,000 | ₩500,000 |
