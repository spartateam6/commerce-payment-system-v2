# 🛒 커머스 결제 시스템 프로젝트

> Spring Boot와 JPA를 활용한 커머스 주문·결제 시스템

회원이 상품을 조회하고 장바구니에 담아 주문·결제·취소까지 진행할 수 있으며,
재고 차감과 복구, 결제 상태 관리를 통해 **데이터 정합성을 보장하는 커머스 백엔드 시스템**입니다.

> 필수 과제에 대한 README 입니다

![Static Badge](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![Static Badge](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Static Badge](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=white)

![Static Badge](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)

![Static Badge](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

## Links

- [API 명세서 확인하기](./API_SPEC.md)
- [회의록 및 팀 컨벤션](https://github.com/spartateam6/commerce-payment-system/wiki)
- [README v1 (기본기 과제 README)](./README.v1.md)

---

## 🚀 시작하기 (Getting Started)

### 요구 사항

| 항목 | 버전 |
|---|---|
| JDK | 17 (Gradle toolchain으로 자동 감지) |
| Gradle | Wrapper 포함 (`./gradlew`, 별도 설치 불필요) |
| MySQL | 8.x (Docker Compose로 제공) |

### 1. 데이터베이스 실행

프로젝트 루트에 포함된 `docker-compose.yaml`로 MySQL 컨테이너를 띄웁니다.

```bash
docker compose up -d
```

| 항목 | 값 |
|---|---|
| DB | `commerce` |
| Host / Port | `localhost:3306` |
| User / Password | `commerce` / `commerce` (root: `root1234`) |

> `application-local.yaml`은 `root` / `root1234` 계정으로 접속하도록 되어 있습니다. 로컬 실행 시 `docker-compose.yaml`의 계정 정보와 맞춰 사용하세요.

### 2. 애플리케이션 실행 (local profile)

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

또는 IDE(IntelliJ)에서 `local` 프로파일을 지정해 `CommercePaymentSystemApplication`을 실행합니다.

`local` 프로파일 기준 주요 설정(`src/main/resources/application-local.yaml`):

- `spring.jpa.hibernate.ddl-auto: update` — 엔티티 기준 자동 스키마 반영
- `spring.sql.init.mode: always` — 기동 시 `data.sql` 초기 데이터 적재
- `jwt.secret`, `jwt.expiration` — JWT 서명 키 및 만료 시간(ms)
- `portone.api-secret` — PortOne 연동용 시크릿 (필수 과제 범위 확장 기능)

> ⚠️ 위 값들은 로컬 개발 편의를 위해 하드코딩된 예시 값입니다. 실제 배포 환경에서는 `application-prod.yaml`처럼 환경변수/파라미터 스토어로 분리해야 합니다.

정적 문서는 [API_SPEC.md](./API_SPEC.md)를 참고하세요.

### 3. 데모 화면 (선택)

`src/main/resources/static/demo/` 하위에 로그인/상품/장바구니/주문/결제 등을 테스트해볼 수 있는 정적 HTML 데모 페이지가 포함되어 있습니다. 서버 기동 후 아래 경로로 접근할 수 있습니다.

```
http://localhost:8080/demo/login.html
```

### 4. 테스트 & 커버리지

```bash
./gradlew test
```

테스트 종료 후 JaCoCo 리포트가 자동 생성됩니다.

```
build/reports/jacoco/test/html/index.html
```

### 5. Docker 이미지 빌드/실행

```bash
docker build -t commerce-payment-system .
docker run -p 8080:8080 --env SPRING_PROFILES_ACTIVE=prod commerce-payment-system
```

---

## 📌 주요 도메인 구조

### 패키지 구조

```
io.github.spartateam6.commercepaymentsystem
├── domain
│   ├── member    # 회원가입 / 로그인 / 내 정보
│   ├── product   # 상품 조회, 카테고리/가격 필터, 페이지네이션
│   ├── cart      # 장바구니 CRUD (담기/조회/수량변경/삭제/비우기)
│   ├── order     # 주문 생성, 재고 선차감, 주문 조회/취소
│   ├── payment   # 모의 결제, PortOne 결제 확인/웹훅 연동
│   ├── refund    # 환불 처리
│   └── point     # 포인트 사용/적립/원장
├── infra
│   └── portone   # PortOne PG 연동 클라이언트/설정/DTO
└── global
    ├── config       # Security, Swagger 등 공통 설정
    ├── security     # JWT 인증/인가
    ├── exception     # 공통 예외 처리
    ├── response      # 공통 응답 포맷(ApiResponse)
    ├── annotation    # 커스텀 어노테이션 (예: 인증 사용자 주입)
    ├── logging       # 컨트롤러 로깅 AOP
    └── entity        # 공통 엔티티(BaseEntity 등)
```

---


## Team Code Convention

### 📌 패키지 구조

```
|- common
   |- dto
   |- response
   |- filter
   |- exception
|- config
|- domain
   |- ...
      |- controller
      |- service
      |- dto
      |- repository
      |- entity
```

### 코드

#### Enum

- Order

| Status | Description |
|---|---|
| `PAYMENT_PENDING` | 주문 생성 후 결제 대기 |
| `CONFIRMED` | 결제 완료 및 주문 확정 |
| `CANCELLED` | 주문 취소 또는 전액 환불 완료 |

- Payment

| Status | Description |
|---|---|
| `PENDING` | 결제 대기 |
| `PAID` | 결제 완료 |
| `FAILED` | 결제 실패 |
| `REFUNDED` | 전액 환불 완료 |

- Refund

| Status | Description |
|---|---|
| `COMPLETED` | 전액 환불 완료 |
| `FAILED` | 전액 환불 실패 |

- Point Transaction

| Type | Description |
|---|---|
| `USE` | 포인트 사용 |
| `EARN` | 포인트 적립 |
| `USE_RESTORE` | 사용 포인트 반환 |
| `EARN_REVOKE` | 적립 포인트 회수 |

- Product

| Status | Description |
|---|---|
| `ON_SALE` | 판매 중 |
| `DISCONTINUED` | 판매 중지 |

---
## 📌 ERD

<img width="917" height="805" alt="커머스 결제 시스템 ERD 필수" src="https://github.com/user-attachments/assets/8204ffce-5dc1-4b36-80b9-6c3529225c96" />


## 📌 Flowchart

## 주문 / 결제 처리 흐름

### 1. 상품 주문 + 재고 선차감

상품 주문 시 재고를 확인하고 비관적 락을 통해 동시 주문 상황에서도
재고 정합성을 보장하며, 결제 이전에 재고를 선차감합니다.

<details>
<summary><b>상품 주문 + 재고 선차감 흐름</b></summary>

<p align="center">
  <img width="1292" height="1286" alt="image" src="https://github.com/user-attachments/assets/60fb3747-bac1-4a70-9c44-07258fb58794" />
</p>

</details>

---

### 2. 일반 카드 결제

PortOne 결제 완료 후 서버에서 실제 결제 상태와 결제 금액을 검증하고,
검증이 완료되면 결제 및 주문 상태를 확정합니다.

<details>
<summary><b>일반 카드 결제 흐름</b></summary>

<p align="center">
  <img width="1214" height="1206" alt="image" src="https://github.com/user-attachments/assets/85110940-ca33-4f7f-8b74-56016acde02b" />
</p>

</details>

---

### 3. 포인트 + 카드 복합 결제

사용 포인트를 제외한 금액만 PG를 통해 결제하고,
결제 금액 검증 후 포인트 사용 및 적립 내역을 함께 처리합니다.

<details>
<summary><b>포인트 + 카드 복합 결제 상세 흐름</b></summary>

<br>

<p align="center">
  <img width="1634" height="1568" alt="image" src="https://github.com/user-attachments/assets/165f748a-315f-4165-aaa8-39cb31279643" />
</p>

</details>

---

### 4. 포인트 전액(PG 0원) 결제

주문 금액 전액을 포인트로 결제하는 경우 PG 결제 과정을 생략하고,
포인트 잔액 차감 및 포인트 원장 기록만으로 주문을 확정합니다.

<details>
<summary><b>포인트 전액(PG 0원) 결제 흐름</b></summary>

<br>

<p align="center">
  <img width="1630" height="1576" alt="image" src="https://github.com/user-attachments/assets/cbc354f7-aa36-4b88-9c91-80c9a291a244" />
</p>

</details>

---

### 5. 포인트 잔액 ↔ 원장 동기화/음수 잔액 정책

환불 및 적립 포인트 회수 과정에서 회원의 현재 포인트 잔액과
포인트 거래 원장의 정합성을 검증하며, 회수할 포인트가 부족한 경우
음수 잔액을 허용하는 정책을 통해 원장과 실제 잔액의 일관성을 유지합니다.

<details>
<summary><b>포인트 회수 및 잔액 동기화 상세 흐름</b></summary>

<br>

<p align="center">
  <img width="1392" height="1486" alt="image" src="https://github.com/user-attachments/assets/468a64e6-6814-4456-99c8-c41e64b60bbe" />
</p>

</details>

---

## 🎬 주요 기능 Demo 영상

https://github.com/user-attachments/assets/6e0e5089-b263-46ab-b101-d76c21cb9ea7


# 🧑‍💻 Contributors

<a href="https://github.com/prjkmo112"><img src="https://github.com/prjkmo112.png?s=50" width="50px" alt="prjkmo112"/></a>&nbsp;&nbsp;&nbsp;&nbsp;
<a href="https://github.com/bomin03"><img src="https://github.com/bomin03.png?s=50" width="50px" alt="bomin03"/></a>&nbsp;&nbsp;&nbsp;&nbsp;
<a href="https://github.com/trex1004"><img src="https://github.com/trex1004.png?s=50" width="50px" alt="trex1004"/></a>&nbsp;&nbsp;&nbsp;&nbsp;
<a href="https://github.com/yulimlvphs"><img src="https://github.com/yulimlvphs.png?s=50" width="50px" alt="yulimlvphs"/></a>&nbsp;&nbsp;&nbsp;&nbsp;
<a href="https://github.com/chaeb0414-collab"><img src="https://github.com/chaeb0414-collab.png?s=50" width="50px" alt="chaeb0414-collab"/></a>&nbsp;&nbsp;&nbsp;&nbsp;
