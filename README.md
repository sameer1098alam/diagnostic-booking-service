## Requirements

* Java 17+
* Maven 3.9+ (or use the included Maven Wrapper)
* PostgreSQL 15+
* Git
* IntelliJ IDEA (recommended)

## Clone the Repository

```bash
git clone https://github.com/sameer1098alam/diagnostic-booking-service.git
cd diagnostic-booking-service
```

## Run Auth Service

```bash
cd authService
mvn spring-boot:run
```

Runs on: `http://localhost:8084`

## Run Booking Service

Open a new terminal:

```bash
cd bookingService
mvn spring-boot:run
```

Runs on: `http://localhost:8082`

## Run Payment Service

Open another terminal:

```bash
cd paymentService
mvn spring-boot:run
```

Runs on: `http://localhost:8083`

## Build All Services

Run from each service directory:

```bash
mvn clean package
```

## Git Commands

```bash
git add .
git commit -m "Update diagnostic booking service"
git push
```



# Diagnostic Test Booking & Payment System

A microservices-based backend system for diagnostic test booking and simulated payments.

## Architecture

The application consists of three independent Spring Boot microservices:

- Auth Service - Port 8084
- Booking Service - Port 8082
- Payment Service - Port 8083

Each service has its own PostgreSQL database.

```text
                         ┌─────────────────────┐
                         │       CLIENT        │
                         │  Postman / Frontend │
                         └──────────┬──────────┘
                                    │
                 ┌──────────────────┼──────────────────┐
                 │                  │                  │
                 ▼                  ▼                  ▼
        ┌────────────────┐ ┌────────────────┐ ┌────────────────┐
        │  Auth Service  │ │Booking Service │ │Payment Service │
        │     :8084      │ │     :8082      │ │     :8083      │
        └───────┬────────┘ └───────┬────────┘ └───────┬────────┘
                │                  │                  │
                ▼                  ▼                  ▼
           ┌─────────┐       ┌───────────┐      ┌───────────┐
           │ auth_db │       │ booking_db│      │ payment_db│
           │PostgreSQL│      │PostgreSQL │      │PostgreSQL │
           └─────────┘       └───────────┘      └───────────┘
                                    ▲                  │
                                    │                  │
                                    └──── REST ────────┘
                                    
                                    
1. User
   ↓
2. Auth Service
   ↓
3. JWT Token
   ↓
4. Booking Service
   ↓
5. Booking = PENDING
   ↓
6. Payment Service
   ↓
7. Simulated Payment
   ↓
   ├── SUCCESS → Booking = CONFIRMED
   │
   └── FAILED  → Booking = FAILED
   
   
   
   
   
   Payment Provider
       │
       ▼
POST /payments/webhook
       │
       ▼
PaymentWebhookController
       │
       ▼
PaymentService
       │
       ▼
Check eventId
       │
       ├── Already processed → Ignore
       │
       └── New event
              │
              ▼
        Update Payment
              │
              ▼
       Booking Service
              │
              ▼
     Update Booking Status                                    



## Database Design

The application follows a **database-per-service architecture** using PostgreSQL. Each microservice owns its database and manages its own data. Services do not directly access another service's database.

### Database Overview

| Service         | Database     | Purpose                                         |
| --------------- | ------------ | ----------------------------------------------- |
| Auth Service    | `auth_db`    | User registration and authentication            |
| Booking Service | `booking_db` | Diagnostic centres, tests, pricing and bookings |
| Payment Service | `payment_db` | Payments and payment webhook tracking           |

---

### 1. Auth Service — `auth_db`

#### `users`

Stores user registration and authentication information.

| Column          | Type         | Constraints      | Description           |
| --------------- | ------------ | ---------------- | --------------------- |
| `id`            | BIGSERIAL    | PK               | Unique user ID        |
| `name`          | VARCHAR(255) | NOT NULL         | User name             |
| `email`         | VARCHAR(255) | NOT NULL, UNIQUE | User email            |
| `password_hash` | VARCHAR(255) | NOT NULL         | Hashed password       |
| `created_at`    | TIMESTAMP    | NOT NULL         | Account creation time |
| `updated_at`    | TIMESTAMP    | NOT NULL         | Last update time      |

A case-insensitive unique index on `lower(email)` prevents duplicate accounts with different email casing.

```sql
CREATE UNIQUE INDEX idx_users_email_lower
ON users (LOWER(email));
```

---

### 2. Booking Service — `booking_db`

The Booking Service manages diagnostic centres, available tests, centre-specific pricing and bookings.

#### `diagnostic_centres`

| Column     | Type         | Constraints | Description            |
| ---------- | ------------ | ----------- | ---------------------- |
| `id`       | BIGSERIAL    | PK          | Centre ID              |
| `name`     | VARCHAR(255) | NOT NULL    | Diagnostic centre name |
| `location` | VARCHAR(512) | NOT NULL    | Centre location        |

#### `diagnostic_tests`

| Column        | Type         | Constraints | Description      |
| ------------- | ------------ | ----------- | ---------------- |
| `id`          | BIGSERIAL    | PK          | Test ID          |
| `name`        | VARCHAR(255) | NOT NULL    | Test name        |
| `description` | TEXT         | NOT NULL    | Test description |

#### `centre_tests`

This junction table represents which tests are available at which diagnostic centres and stores the centre-specific price.

| Column      | Type          | Constraints | Description              |
| ----------- | ------------- | ----------- | ------------------------ |
| `centre_id` | BIGINT        | PK, FK      | Diagnostic centre        |
| `test_id`   | BIGINT        | PK, FK      | Diagnostic test          |
| `price`     | NUMERIC(10,2) | NOT NULL    | Test price at the centre |

**Composite Primary Key:** `(centre_id, test_id)`

Relationships:

```text
diagnostic_centres
        │
        │ 1
        ▼
   centre_tests
        ▲
        │ 1
        │
diagnostic_tests
```

#### `bookings`

Stores test appointments created by authenticated users.

| Column             | Type                     | Constraints | Description               |
| ------------------ | ------------------------ | ----------- | ------------------------- |
| `id`               | BIGSERIAL                | PK          | Booking ID                |
| `user_id`          | VARCHAR(255)             | NOT NULL    | User ID/email from JWT    |
| `centre_id`        | BIGINT                   | FK          | Diagnostic centre         |
| `test_id`          | BIGINT                   | FK          | Diagnostic test           |
| `appointment_time` | TIMESTAMP WITH TIME ZONE | NOT NULL    | Appointment date and time |
| `amount`           | NUMERIC(10,2)            | NOT NULL    | Booking amount            |
| `status`           | VARCHAR(32)              | NOT NULL    | Booking status            |
| `created_at`       | TIMESTAMP                | NOT NULL    | Booking creation time     |
| `updated_at`       | TIMESTAMP                | —           | Last update time          |

Possible booking statuses:

```text
PENDING
CONFIRMED
FAILED
CANCELLED
```

`user_id` is a logical reference to the Auth Service. No cross-database foreign key is used.

---

### 3. Payment Service — `payment_db`

The Payment Service manages simulated payments and webhook processing.

#### `payments`

| Column                | Type          | Constraints | Description                   |
| --------------------- | ------------- | ----------- | ----------------------------- |
| `id`                  | BIGSERIAL     | PK          | Payment ID                    |
| `booking_id`          | VARCHAR(255)  | NOT NULL    | Booking identifier            |
| `provider_payment_id` | VARCHAR(255)  | —           | Simulated payment provider ID |
| `event_id`            | VARCHAR(255)  | —           | Webhook event ID              |
| `amount`              | NUMERIC(10,2) | NOT NULL    | Payment amount                |
| `status`              | VARCHAR(32)   | NOT NULL    | Payment status                |
| `created_at`          | TIMESTAMP     | NOT NULL    | Payment creation time         |
| `updated_at`          | TIMESTAMP     | —           | Last update time              |

Possible payment statuses:

```text
PENDING
SUCCESS
FAILED
```

The `event_id` is used for **webhook idempotency**. If the same webhook event is received multiple times, the service detects the existing event and does not process it again.

---

### Database Relationship Overview

```text
┌─────────────────────┐
│      auth_db        │
│                     │
│       users         │
└─────────┬───────────┘
          │
          │ logical user_id
          ▼
┌─────────────────────────────┐
│         booking_db          │
│                             │
│  diagnostic_centres         │
│          │                  │
│          ▼                  │
│     centre_tests            │
│          ▲                  │
│          │                  │
│  diagnostic_tests           │
│                             │
│       bookings              │
└─────────────┬───────────────┘
              │
              │ logical booking_id
              ▼
┌─────────────────────────────┐
│         payment_db          │
│                             │
│         payments            │
└─────────────────────────────┘


## API Design

The application exposes RESTful APIs through three independent microservices.

| Service         | Base URL                | Responsibility                                  |
| --------------- | ----------------------- | ----------------------------------------------- |
| Auth Service    | `http://localhost:8084` | User registration, login and JWT authentication |
| Booking Service | `http://localhost:8082` | Diagnostic centres, tests, pricing and bookings |
| Payment Service | `http://localhost:8083` | Simulated payments and payment webhooks         |

Protected endpoints use JWT Bearer authentication:

```http
Authorization: Bearer <JWT_TOKEN>
```

---

### 1. Authentication APIs

#### Signup

```http
POST http://localhost:8084/api/auth/signup
Content-Type: application/json
```

**Request:**

```json
{
  "name": "Sameer Alam",
  "email": "sameer@gmail.com",
  "password": "Password@123"
}
```

#### Login

```http
POST http://localhost:8084/api/auth/login
Content-Type: application/json
```

**Request:**

```json
{
  "email": "sameer@gmail.com",
  "password": "Password@123"
}
```

**Response:**

```json
{
  "token": "<JWT_TOKEN>",
  "name": "Sameer Alam",
  "email": "sameer@gmail.com"
}
```

The JWT returned by the login API is used to access protected Booking and Payment APIs.

---

### 2. Diagnostic Centre APIs

Base URL:

```text
http://localhost:8082
```

| Method | Endpoint                        | Description                                         |
| ------ | ------------------------------- | --------------------------------------------------- |
| `POST` | `/api/centres`                  | Create a diagnostic centre                          |
| `GET`  | `/api/centres`                  | Get all diagnostic centres                          |
| `GET`  | `/api/centres/{id}`             | Get a diagnostic centre by ID                       |
| `POST` | `/api/centres/{centreId}/tests` | Add a test to a centre with centre-specific pricing |

#### Create Centre

```http
POST http://localhost:8082/api/centres
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

```json
{
  "name": "Apollo Imaging Centre",
  "location": "45 Residency Lane, Hyderabad"
}
```

#### Get All Centres

```http
GET http://localhost:8082/api/centres
Authorization: Bearer <JWT_TOKEN>
```

#### Get Centre by ID

```http
GET http://localhost:8082/api/centres/2
Authorization: Bearer <JWT_TOKEN>
```

#### Add Test to Centre

```http
POST http://localhost:8082/api/centres/2/tests
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

```json
{
  "testId": 2,
  "price": 4200
}
```

---

### 3. Diagnostic Test APIs

| Method | Endpoint          | Description                 |
| ------ | ----------------- | --------------------------- |
| `POST` | `/api/tests`      | Create a diagnostic test    |
| `GET`  | `/api/tests`      | Get all diagnostic tests    |
| `GET`  | `/api/tests/{id}` | Get a diagnostic test by ID |

#### Create Test

```http
POST http://localhost:8082/api/tests
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

```json
{
  "name": "MRI Scan",
  "description": "Magnetic resonance imaging for detailed internal views"
}
```

#### Get All Tests

```http
GET http://localhost:8082/api/tests
Authorization: Bearer <JWT_TOKEN>
```

#### Get Test by ID

```http
GET http://localhost:8082/api/tests/2
Authorization: Bearer <JWT_TOKEN>
```

---

### 4. Booking APIs

| Method | Endpoint                       | Description                      |
| ------ | ------------------------------ | -------------------------------- |
| `POST` | `/api/bookings`                | Create a diagnostic test booking |
| `GET`  | `/api/bookings/{id}`           | Get booking details              |
| `GET`  | `/api/users/{userId}/bookings` | Get bookings for a user          |

#### Create Booking

```http
POST http://localhost:8082/api/bookings
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

```json
{
  "centreId": 2,
  "testId": 2,
  "appointmentTime": "2026-10-05T10:30:00"
}
```

The user identity is obtained from the authenticated JWT rather than accepting a `userId` from the request body.

A newly created booking has the status:

```text
PENDING
```

Example response:

```json
{
  "id": 2,
  "userId": "sameer@gmail.com",
  "centreId": 2,
  "centreName": "Apollo Imaging Centre",
  "testId": 2,
  "testName": "MRI Scan",
  "bookingDateTime": "2026-10-05T10:30:00",
  "status": "PENDING",
  "amount": 4200.00
}
```

#### Get Booking

```http
GET http://localhost:8082/api/bookings/2
Authorization: Bearer <JWT_TOKEN>
```

#### Get User Bookings

```http
GET http://localhost:8082/api/users/sameer@gmail.com/bookings
Authorization: Bearer <JWT_TOKEN>
```

---

### 5. Payment APIs

Base URL:

```text
http://localhost:8083
```

| Method | Endpoint            | Description                |
| ------ | ------------------- | -------------------------- |
| `POST` | `/payments`         | Create a simulated payment |
| `GET`  | `/payments`         | Get all payments           |
| `GET`  | `/payments/{id}`    | Get payment by ID          |
| `POST` | `/payments/webhook` | Process payment webhook    |

#### Create Payment

```http
POST http://localhost:8083/payments
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

```json
{
  "bookingId": 2,
  "providerPaymentId": "pay_003",
  "amount": 4200
}
```

The payment service simulates a payment transaction.

Possible payment statuses:

```text
PENDING
SUCCESS
FAILED
```

#### Get All Payments

```http
GET http://localhost:8083/payments
Authorization: Bearer <JWT_TOKEN>
```

#### Get Payment by ID

```http
GET http://localhost:8083/payments/8
Authorization: Bearer <JWT_TOKEN>
```

---

### 6. Payment Webhook API

The webhook simulates a payment provider sending the result of a payment transaction.

```http
POST http://localhost:8083/payments/webhook
Content-Type: application/json
```

**Request:**

```json
{
  "paymentId": 8,
  "providerPaymentId": "pay_003",
  "eventId": "evt_002",
  "status": "SUCCESS"
}
```

Possible webhook statuses:

```text
SUCCESS
FAILED
```

### Webhook Idempotency

The `eventId` is used to prevent duplicate webhook processing.

If the same event is received again:

```json
{
  "paymentId": 8,
  "providerPaymentId": "pay_003",
  "eventId": "evt_002",
  "status": "SUCCESS"
}
```

the service detects that `evt_002` has already been processed and ignores the duplicate event.

This prevents duplicate payment processing and repeated booking updates.

---

### 7. Booking and Payment Flow

```text
User
 │
 │ Signup / Login
 ▼
Auth Service :8084
 │
 │ JWT
 ▼
Booking Service :8082
 │
 │ Create Booking
 ▼
Booking = PENDING
 │
 │ Create Payment
 ▼
Payment Service :8083
 │
 ├── SUCCESS ──► Booking = CONFIRMED
 │
 └── FAILED ───► Booking = FAILED
 │
 ▼
Payment Webhook
 │
 ▼
Idempotency Check
```

### Booking Status

```text
PENDING
CONFIRMED
FAILED
CANCELLED
```

### Payment Status

```text
PENDING
SUCCESS
FAILED
```

A successful payment results in:

```text
PENDING → CONFIRMED
```

A failed payment results in:

```text
PENDING → FAILED
```

---

### 8. HTTP Status Codes

| Status                      | Meaning                                         |
| --------------------------- | ----------------------------------------------- |
| `200 OK`                    | Request completed successfully                  |
| `201 Created`               | Resource created successfully                   |
| `400 Bad Request`           | Invalid request or validation failure           |
| `401 Unauthorized`          | Missing or invalid authentication               |
| `403 Forbidden`             | Access to the requested resource is not allowed |
| `404 Not Found`             | Resource not found                              |
| `409 Conflict`              | Duplicate or conflicting resource               |
| `500 Internal Server Error` | Unexpected server error                         |

---


