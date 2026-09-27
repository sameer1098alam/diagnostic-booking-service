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
