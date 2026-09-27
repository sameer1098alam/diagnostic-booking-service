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
Client
  |
  +---- Auth Service :8084
  |          |
  |        auth_db
  |
  +---- Booking Service :8082
  |          |
  |       booking_db
  |
  +---- Payment Service :8083
             |
          payment_db



Database Design
auth_db

users

id
name
email
password_hash
created_at
updated_at
booking_db

diagnostic_centres

id
name
location

diagnostic_tests

id
name
description

centre_tests

centre_id
test_id
price

bookings

id
user_id
centre_id
test_id
appointment_time
status
amount
payment_db

payments

id
booking_id
provider_payment_id
event_id
amount
status
created_at
updated_at

API Endpoints
Authentication

POST /api/auth/signup

POST /api/auth/login

Diagnostic Centres

POST /api/centres

GET /api/centres

GET /api/centres/{id}

Diagnostic Tests

POST /api/tests

GET /api/tests

GET /api/tests/{id}

Centre Tests

POST /api/centres/{centreId}/tests

Bookings

POST /api/bookings

GET /api/bookings/{id}

GET /api/users/{userId}/bookings

Payments

POST /payments

GET /payments

GET /payments/{id}

POST /payments/webhook
