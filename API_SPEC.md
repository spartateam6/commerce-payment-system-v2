# API 명세서 — commerce-payment-system

`dev` 브랜치(`9d118c5`) 기준으로 작성되었습니다.

## 목차

- [공통 사항](#공통-사항)
- [인증 (Auth)](#1-인증-auth)
- [회원 (Member)](#2-회원-member)
- [상품 (Product)](#3-상품-product)
- [장바구니 (Cart)](#4-장바구니-cart)
- [주문 (Order)](#5-주문-order)
- [결제 (Payment)](#6-결제-payment)
- [포인트 (Point)](#7-포인트-point)
- [환불 (Refund)](#8-환불-refund)
- [에러 코드](#에러-코드)

---

## 공통 사항

### Base URL

```
/api
```

### 공통 응답 포맷

모든 응답은 아래 형태로 감싸져 내려갑니다. (`ApiResponse<T>`)

**성공**

```json
{
  "code": "SUCCESS",
  "data": { }
}
```

**실패**

```json
{
  "code": "PRODUCT_002",
  "message": "재고가 부족합니다."
}
```

> `data`, `message`는 값이 없으면 응답 JSON에서 생략됩니다. (`@JsonInclude(NON_NULL)`)

### 인증 방식

로그인 이후 발급받은 JWT를 `Authorization` 헤더에 `Bearer` 스킴으로 담아 요청합니다.

```
Authorization: Bearer {accessToken}
```

- 아래 경로는 인증 없이 호출 가능합니다: `/api/auth/**`, `/api/products/**`
- 그 외 모든 API는 인증이 필요하며, `memberId`는 **서버가 JWT에서 직접 추출**합니다. 클라이언트가 body/param으로 넘긴 값은 신뢰하지 않습니다.
- 인증 실패 시 `401 Unauthorized` (`AUTH_001` / `AUTH_002`)가 반환됩니다.

---

## 1. 인증 (Auth)

### 1-1. 회원가입

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| URL | `/api/auth/signup` |
| 인증 | 불필요 |
| 성공 응답 | `201 Created` |

**Request Body**

| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| `email` | String | O | 이메일 형식, 최대 255자 |
| `password` | String | O | 영문+숫자+특수문자 포함 8자 이상 |
| `name` | String | O | 공백 불가 |
| `phoneNumber` | String | O | `010-0000-0000` 형식 |

```json
{
  "email": "user@example.com",
  "password": "abcd1234!",
  "name": "홍길동",
  "phoneNumber": "010-1234-5678"
}
```

**Response Body**

```json
{
  "code": "SUCCESS"
}
```

---

### 1-2. 로그인

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| URL | `/api/auth/login` |
| 인증 | 불필요 |
| 성공 응답 | `200 OK` |

**Request Body**

| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| `email` | String | O | 이메일 형식, 최대 255자 |
| `password` | String | O | 영문+숫자+특수문자 포함 8자 이상 |

```json
{
  "email": "user@example.com",
  "password": "abcd1234!"
}
```

**Response Body**

```json
{
  "code": "SUCCESS",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs..."
  }
}
```

---

## 2. 회원 (Member)

### 2-1. 내 정보 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| URL | `/api/members/me` |
| 인증 | 필요 |
| 성공 응답 | `200 OK` |

**Response Body**

```json
{
  "code": "SUCCESS",
  "data": {
    "email": "user@example.com",
    "name": "홍길동",
    "phoneNumber": "010-1234-5678"
  }
}
```

---

## 3. 상품 (Product)

### 3-1. 상품 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| URL | `/api/products` |
| 인증 | 불필요 |
| 성공 응답 | `200 OK` |

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---|---|---|---|---|
| `category` | String | X | - | 카테고리 필터 |
| `minPrice` | Integer | X | - | 최소 가격 필터 |
| `maxPrice` | Integer | X | - | 최대 가격 필터 |
| `saleStatus` | String | X | - | 판매 상태 필터. `ON_SALE`, `DISCONTINUED` |
| `soldOut` | Boolean | X | - | 품절 여부 필터 |
| `sort` | String | X | `latest` | 정렬 조건. `latest`(최신순), `priceAsc`(낮은 가격순), `priceDesc`(높은 가격순) |
| `page` | int | X | `1` | 페이지 번호 (1부터 시작) |
| `size` | int | X | `10` | 페이지 크기 (최대 100) |

예: `GET /api/products?category=food&minPrice=1000&maxPrice=50000&saleStatus=ON_SALE&sort=priceAsc&page=1&size=10`

**Response Body**

```json
{
  "code": "SUCCESS",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "상품명",
        "price": 10000,
        "stock": 25,
        "description": "상품 설명",
        "category": "food",
        "saleStatus": "ON_SALE",
        "soldOut": false,
        "createdAt": "2026-08-01T10:00:00",
        "updatedAt": "2026-08-01T10:00:00"
      }
    ],
    "page": 1,
    "size": 10,
    "totalElements": 42,
    "totalPages": 5
  }
}
```

> `saleStatus`는 `ON_SALE`(판매중) / `DISCONTINUED`(단종) 중 하나입니다. `soldOut`은 재고 0 여부를 나타내는 계산값입니다.

---

### 3-2. 상품 단건 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| URL | `/api/products/{productId}` |
| 인증 | 불필요 |
| 성공 응답 | `200 OK` |

**Path Variable**

| 변수 | 타입 | 설명 |
|---|---|---|
| `productId` | Long | 상품 ID |

**Response Body**

```json
{
  "code": "SUCCESS",
  "data": {
    "id": 1,
    "name": "상품명",
    "price": 10000,
    "stock": 25,
    "description": "상품 설명",
    "category": "food",
    "saleStatus": "ON_SALE",
    "soldOut": false,
    "createdAt": "2026-08-01T10:00:00",
    "updatedAt": "2026-08-01T10:00:00"
  }
}
```

---

## 4. 장바구니 (Cart)

### 4-1. 장바구니 상품 추가

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| URL | `/api/cart/items` |
| 인증 | 필요 |
| 성공 응답 | `201 Created` |

**Request Body**

| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| `productId` | Long | O | - |
| `quantity` | Integer | O | 1 이상 |

```json
{
  "productId": 1,
  "quantity": 2
}
```

> 동일 상품을 다시 추가하면 기존 행에 수량이 합산됩니다(중복 행 생성 안 됨).

**Response Body**

```json
{
  "code": "SUCCESS",
  "data": {
    "cartItemId": 10,
    "productId": 1,
    "productName": "상품명",
    "price": 10000,
    "quantity": 2,
    "totalPrice": 20000
  }
}
```

---

### 4-2. 장바구니 상품 수량 변경

| 항목 | 내용 |
|---|---|
| Method | `PATCH` |
| URL | `/api/cart/items/{cartItemId}` |
| 인증 | 필요 (본인 소유 검증) |
| 성공 응답 | `200 OK` |

**Path Variable**

| 변수 | 타입 | 설명 |
|---|---|---|
| `cartItemId` | Long | 장바구니 항목 ID |

**Request Body**

| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| `quantity` | Integer | O | - |

```json
{
  "quantity": 3
}
```

**Response Body**

```json
{
  "code": "SUCCESS",
  "data": {
    "cartItemId": 10,
    "productId": 1,
    "productName": "상품명",
    "price": 10000,
    "quantity": 3,
    "totalPrice": 30000
  }
}
```

---

### 4-3. 장바구니 상품 삭제

| 항목 | 내용 |
|---|---|
| Method | `DELETE` |
| URL | `/api/cart/items/{cartItemId}` |
| 인증 | 필요 (본인 소유 검증) |
| 성공 응답 | `204 No Content` |

**Path Variable**

| 변수 | 타입 | 설명 |
|---|---|---|
| `cartItemId` | Long | 장바구니 항목 ID |

**Request / Response Body**: 없음

---

### 4-4. 장바구니 비우기

| 항목 | 내용 |
|---|---|
| Method | `DELETE` |
| URL | `/api/cart/items` |
| 인증 | 필요 |
| 성공 응답 | `204 No Content` |

**Request / Response Body**: 없음

---

### 4-5. 장바구니 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| URL | `/api/cart/items` |
| 인증 | 필요 |
| 성공 응답 | `200 OK` |

**Response Body**

```json
{
  "code": "SUCCESS",
  "data": {
    "cartId": 5,
    "items": [
      {
        "cartItemId": 10,
        "productId": 1,
        "productName": "상품명",
        "price": 10000,
        "quantity": 2,
        "totalPrice": 20000
      }
    ],
    "totalPrice": 20000
  }
}
```

---

## 5. 주문 (Order)

### 5-1. 주문 미리보기

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| URL | `/api/orders/preview` |
| 인증 | 필요 |
| 성공 응답 | `200 OK` |

**Request Body**

| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| `cartItemIds` | List\<Long\> | O | 각 원소 양수 |

```json
{
  "cartItemIds": [10, 11]
}
```

**Response Body**

```json
{
  "code": "SUCCESS",
  "data": {
    "items": [
      {
        "cartItemId": 10,
        "productId": 1,
        "productName": "상품명",
        "unitPrice": 10000,
        "quantity": 2,
        "lineAmount": 20000
      }
    ],
    "totalAmount": 20000
  }
}
```

---

### 5-2. 주문 생성

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| URL | `/api/orders` |
| 인증 | 필요 |
| 성공 응답 | `201 Created` (`Location: /api/orders/{orderId}`) |

**Request Body**

| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| `cartItemIds` | List\<Long\> | O | 각 원소 양수 |
| `pointToUse` | Integer | X | 0 이상, 생략 시 `0`. 사용 포인트는 `totalAmount`를 초과할 수 없음 |

```json
{
  "cartItemIds": [10, 11],
  "pointToUse": 5000
}
```

**동작 개요**: 재고 검증 → 선차감 → `Order`/`OrderItem` 스냅샷 생성 → 포인트 사용 검증 → `Payment(PENDING)` 생성(PG 결제 대상 금액 `pgAmount = totalAmount - pointToUse`). 하나라도 재고가 부족하면 전체 롤백됩니다. 주문 생성만으로는 장바구니가 비워지지 않습니다.

**Response Body**

```json
{
  "code": "SUCCESS",
  "data": {
    "orderId": 100,
    "orderNumber": "ORD-20260819103000-A1B2C3D4E5F6",
    "totalAmount": 20000,
    "pointUsedAmount": 5000,
    "portonePaymentId": "payment-uuid",
    "pgAmount": 15000
  }
}
```

> `pgAmount`가 0이면(포인트 전액 결제) 결제 확정 시 PG 승인 없이 바로 성공 처리됩니다.

---

### 5-3. 주문 상세 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| URL | `/api/orders/{orderId}` |
| 인증 | 필요 (본인 소유 검증) |
| 성공 응답 | `200 OK` |

**Path Variable**

| 변수 | 타입 | 설명 |
|---|---|---|
| `orderId` | Long | 주문 ID |

**Response Body**

```json
{
  "code": "SUCCESS",
  "data": {
    "orderId": 100,
    "orderNumber": "ORD-20260819103000-A1B2C3D4E5F6",
    "totalAmount": 20000,
    "pointUsedAmount": 5000,
    "orderStatus": "PAYMENT_PENDING",
    "createdAt": "2026-08-19T10:00:00",
    "updatedAt": "2026-08-19T10:00:00",
    "orderItems": [
      {
        "orderItemId": 200,
        "productId": 1,
        "productName": "상품명",
        "unitPrice": 10000,
        "quantity": 2,
        "lineAmount": 20000,
        "createdAt": "2026-08-19T10:00:00"
      }
    ],
    "payment": {
      "paymentId": 300,
      "amount": 20000,
      "status": "PENDING",
      "completedAt": null,
      "createdAt": "2026-08-19T10:00:00"
    }
  }
}
```

> `orderStatus`는 `PAYMENT_PENDING` / `CONFIRMED` / `CANCELLED` / `FAILED` 중 하나입니다.

---

### 5-4. 주문 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| URL | `/api/orders` |
| 인증 | 필요 |
| 성공 응답 | `200 OK` |

**Query Parameters** (Spring `Pageable`)

| 파라미터 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| `page` | int | `0` | 페이지 번호 (0부터 시작) |
| `size` | int | `20` | 페이지 크기 |
| `sort` | String | `createdAt,DESC` | 정렬 기준 |

**Response Body**

응답의 각 항목은 5-3 주문 상세 조회와 동일한 구조입니다.

```json
{
  "code": "SUCCESS",
  "data": [
    {
      "orderId": 100,
      "orderNumber": "ORD-20260819103000-A1B2C3D4E5F6",
      "totalAmount": 20000,
      "pointUsedAmount": 5000,
      "orderStatus": "CONFIRMED",
      "createdAt": "2026-08-19T10:00:00",
      "updatedAt": "2026-08-19T10:05:00",
      "orderItems": [ ],
      "payment": { }
    }
  ]
}
```

---

### 5-5. 주문 취소

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| URL | `/api/orders/{orderId}/cancel` |
| 인증 | 필요 (본인 소유 검증) |
| 성공 응답 | `200 OK` |

**Path Variable**

| 변수 | 타입 | 설명 |
|---|---|---|
| `orderId` | Long | 주문 ID |

**동작 개요**: 결제 전(`PAYMENT_PENDING`) 주문만 취소할 수 있습니다. `Order = CANCELLED`, `Payment = FAILED`로 변경되고 재고가 전체 복구됩니다.

- 이미 취소된 주문은 `409 Conflict` (`ORDER_010`)로 거부되어 중복 복구를 방지합니다.
- 결제 완료(`CONFIRMED`) 주문은 이 API로 취소할 수 없으며 `409 Conflict` (`ORDER_012`)가 반환됩니다. 결제 완료 주문은 [환불 요청](#8-1-환불-요청)을 사용해야 합니다.

**Request / Response Body**

```json
{
  "code": "SUCCESS"
}
```

---

## 6. 결제 (Payment)

### 6-1. 결제 확정

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| URL | `/api/payments/confirm` |
| 인증 | 필요 (본인 주문 검증) |
| 성공 응답 | `200 OK` |

**Request Body**

| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| `orderNumber` | String | O | 공백 불가 |
| `price` | Integer | O | 1 이상 |

```json
{
  "orderNumber": "ORD-20260819103000-A1B2C3D4E5F6",
  "price": 20000
}
```

> `price`는 `Order.totalAmount`와의 검증용 값이며, 실제 PG 승인 여부는 PortOne 결제 게이트웨이 조회 결과로 판단합니다.

**동작 개요**:

1. 본인 주문/결제인지, `Order`가 결제대기 상태인지, `Payment`가 `PENDING`인지, 요청 `price`가 `Payment.orderAmount`와 일치하는지 검증
2. 이미 `PAID` + `CONFIRMED` 상태면 PG 재조회 없이 그대로 200 응답 (멱등 처리)
3. `pgAmount`가 0(포인트 전액 결제)이면 PG 조회 없이 바로 성공 처리
4. 그 외에는 PortOne에서 결제 정보를 조회해 승인 여부·금액 일치 여부를 확인 후 성공/실패 처리 (금액 불일치 시 PG 결제를 보상 취소한 뒤 실패 처리)

| 결과 | Payment | Order | 재고 | 장바구니 |
|---|---|---|---|---|
| 성공 | `PENDING → PAID`, `completedAt` 기록 | `PAYMENT_PENDING → CONFIRMED` | 변경 없음 | 이번 주문으로 구매된 항목만 제거 |
| 실패 | `PENDING → FAILED` | `PAYMENT_PENDING → FAILED` | 전체 복구 | 유지 |

**Response Body**

```json
{
  "code": "SUCCESS",
  "data": {
    "paymentId": 300,
    "portonePaymentId": "payment-uuid",
    "amount": 20000,
    "paymentStatus": "PAID"
  }
}
```

> 실패 시에도 HTTP 200으로 응답하며 `paymentStatus`가 `"FAILED"`로, `paymentId`/`portonePaymentId`/`amount`는 `null`로 내려갑니다.

---

## 7. 포인트 (Point)

### 7-1. 포인트 잔액 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| URL | `/api/points` |
| 인증 | 필요 |
| 성공 응답 | `200 OK` |

**Response Body**

```json
{
  "code": "SUCCESS",
  "data": {
    "balance": 15000
  }
}
```

---

### 7-2. 포인트 거래 내역 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| URL | `/api/points/transactions` |
| 인증 | 필요 |
| 성공 응답 | `200 OK` |

**Query Parameters** (Spring `Pageable`)

| 파라미터 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| `page` | int | `0` | 페이지 번호 (0부터 시작) |
| `size` | int | `20` | 페이지 크기 |
| `sort` | String | `createdAt,id` DESC | 정렬 기준 |

**Response Body**

```json
{
  "code": "SUCCESS",
  "data": {
    "content": [
      {
        "transactionId": 1,
        "type": "EARN",
        "typeLabel": "적립",
        "amount": 500,
        "paymentId": 300,
        "createdAt": "2026-08-19T10:05:00"
      }
    ],
    "page": 1,
    "size": 20,
    "totalElements": 3,
    "totalPages": 1
  }
}
```

> `type`은 `USE`(사용) / `EARN`(적립) / `USE_RESTORE`(사용복구) / `EARN_REVOKE`(적립회수) 중 하나입니다. `paymentId`는 결제와 무관한 거래인 경우 `null`입니다.

---

## 8. 환불 (Refund)

### 8-1. 환불 요청

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| URL | `/api/refunds` |
| 인증 | 필요 (본인 결제 검증) |
| 성공 응답 | `200 OK` |

**Request Body**

| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| `paymentId` | Long | O | 양수 |
| `cancelReason` | String | O | 공백 불가, 최대 500자 |

```json
{
  "paymentId": 300,
  "cancelReason": "단순 변심"
}
```

**동작 개요**: 결제 완료(`Payment = PAID`, `Order = CONFIRMED`)된 주문만 환불할 수 있습니다. 환불 확정과 함께 사용 포인트를 복구하고, PG 결제 금액(`pgAmount`)이 있으면 PortOne 결제를 취소한 뒤 재고를 전체 복구합니다. PG 취소가 실패하면 환불은 `FAILED`로 기록되고 예외가 발생합니다.

```text
Payment  PAID -> REFUND
Order    CONFIRMED -> CANCELLED
재고      전체 복구
포인트    사용분 복구
```

- 이미 환불된 결제(`Payment.status == REFUND`) 또는 환불 이력이 있는 결제는 `409 Conflict` (`PAYMENT_007`)로 거부됩니다.
- 결제 완료 상태가 아닌 결제는 `400 Bad Request` (`PAYMENT_004`)로 거부됩니다.

**Response Body**

```json
{
  "code": "SUCCESS",
  "data": {
    "refundId": 50,
    "status": "COMPLETED",
    "pgRefundAmount": 15000,
    "pointRefundAmount": 5000
  }
}
```

> `status`는 `COMPLETED`(환불 완료) / `FAILED`(PG 취소 실패) 중 하나입니다.

---

## 에러 코드

### 공통

| 코드 | HTTP Status | 메시지 |
|---|---|---|
| `COMMON_001` | 400 | 입력값이 올바르지 않습니다. |
| `COMMON_002` | 500 | 서버 내부 오류가 발생했습니다. |
| `COMMON_003` | 403 | 접근 권한이 없습니다. |

### 인증 / 회원

| 코드 | HTTP Status | 메시지 |
|---|---|---|
| `AUTH_001` | 401 | 인증이 필요합니다. |
| `MEMBER_001` | 404 | 회원을 찾을 수 없습니다. |
| `MEMBER_002` | 409 | 이미 존재하는 이메일입니다. |
| `MEMBER_003` | 401 | 이메일 또는 비밀번호가 올바르지 않습니다. |

### 포인트

| 코드 | HTTP Status | 메시지 |
|---|---|---|
| `POINT_001` | 409 | 포인트가 부족합니다. |
| `POINT_002` | 400 | 포인트 사용 금액이 올바르지 않습니다. |

### 상품

| 코드 | HTTP Status | 메시지 |
|---|---|---|
| `PRODUCT_001` | 404 | 상품을 찾을 수 없습니다. |
| `PRODUCT_002` | 409 | 재고가 부족합니다. |
| `PRODUCT_003` | 400 | 가격은 0 이상이어야 합니다. |

### 장바구니

| 코드       | HTTP Status | 메시지 |
|------------|---|---|
| `CART_001` | 404 | 장바구니 항목을 찾을 수 없습니다. |
| `CART_002` | 400 | 수량은 1 이상이어야 합니다. |

### 주문

| 코드 | HTTP Status | 메시지 |
|---|---|---|
| `ORDER_001` | 400 | 주문할 상품이 없습니다. |
| `ORDER_002` | 400 | 주문할 수 없는 상품이 포함되어 있습니다. |
| `ORDER_003` | 400 | 주문 대상 상품이 중복 선택되었습니다. |
| `ORDER_005` | 409 | 주문 상품의 재고가 부족합니다. |
| `ORDER_006` | 400 | 회원 ID는 필수입니다. |
| `ORDER_007` | 400 | 주문번호는 필수입니다. |
| `ORDER_008` | 404 | 주문을 찾을 수 없습니다. |
| `ORDER_009` | 500 | 주문의 결제 정보를 확인할 수 없습니다. |
| `ORDER_010` | 409 | 이미 취소된 주문입니다. |
| `ORDER_011` | 400 | 사용할 포인트 금액이 올바르지 않습니다. |
| `ORDER_012` | 409 | 결제 완료 주문은 환불 요청이 필요합니다. |

### 결제

| 코드          | HTTP Status | 메시지 |
|---------------|---|---|
| `PAYMENT_001` | 404 | 결제 정보를 찾을 수 없습니다. |
| `PAYMENT_002` | 400 | 결제 금액이 일치하지 않습니다. |
| `PAYMENT_003` | 400 | 유효하지 않은 결제 상태 변경입니다. |
| `PAYMENT_004` | 400 | PG사 결제가 완료되지 않았습니다. |
| `PAYMENT_005` | 409 | 이미 처리된 결제입니다. |
| `PAYMENT_006` | 400 | 결제 정보가 주문과 일치하지 않습니다. |
| `PAYMENT_007` | 409 | 이미 처리된 환불(취소)요청입니다. |
| `PAYMENT_008` | 409 | 해당 주문의 결제 정보가 이미 존재합니다. |
| `PAYMENT_009` | 502 | PG사 처리 중 오류가 발생했습니다. |

> `PAYMENT_008` 코드가 서로 다른 두 에러(결제 정보 중복 생성 / PG 처리 오류)에 중복 부여되어 있습니다. 소스(`ErrorCode.java`) 그대로 반영한 것으로, 응답의 HTTP Status와 메시지로 구분해야 합니다. 별도 이슈로 확인이 필요합니다.

### 환불

| 코드 | HTTP Status | 메시지 |
|---|---|---|
| `REFUND_001` | 404 | 환불 정보를 찾을 수 없습니다. |


