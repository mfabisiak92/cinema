# Cinema Seat Booking

A backend application for booking seats at cinema screenings. Built to demonstrate **DDD**, **SOLID principles**, and **TDD** using Java 21, Spring Boot 3.4, and PostgreSQL.

## Architecture

The project is structured around two bounded contexts — **Screening** and **Reservation** — each with a clean layered architecture:

```
com.cinema
├── screening
│   ├── domain          # Aggregate: Screening, Value Objects: Hall, Seat, SeatStatus
│   ├── application     # Use cases: CreateScreeningUseCase, ScreeningRepository (port)
│   └── infrastructure
│       ├── web         # ScreeningController, request/response DTOs
│       └── persistence # ScreeningJpaEntity, ScreeningRepositoryAdapter
├── reservation
│   ├── domain          # Aggregate: Reservation, Value Objects: ReservationStatus, CustomerId
│   ├── application     # Use cases: ReserveSeatUseCase, ConfirmReservationUseCase,
│   │                   #            ExpireReservationsUseCase, ReservationRepository (port)
│   └── infrastructure
│       ├── web         # ReservationController, request/response DTOs
│       ├── persistence # ReservationJpaEntity, ReservationRepositoryAdapter
│       └── scheduling  # ReservationExpiryScheduler (@Scheduled every 60 s)
└── shared
    ├── domain          # AggregateRoot (domain event accumulation), DomainEvent
    └── infrastructure
        └── web         # GlobalExceptionHandler (RFC 7807 ProblemDetail)
```

### Key design decisions

| Principle | How it's applied |
|---|---|
| **DDD aggregates** | `Screening` owns seats; `Reservation` owns status lifecycle. Cross-aggregate coordination happens in use cases, never inside the domain. |
| **Repository port** | `ScreeningRepository` and `ReservationRepository` are interfaces in `application`. Infrastructure adapters implement them. |
| **Reconstitute factory** | `Screening.reconstitute()` / `Reservation.reconstitute()` hydrate aggregates from the DB without re-running creation invariants. |
| **Domain events** | `AggregateRoot.registerEvent()` accumulates events; `pullDomainEvents()` drains them atomically (snapshot before clear). |
| **RFC 7807 errors** | `GlobalExceptionHandler` maps domain exceptions to `ProblemDetail` responses. |

## Tech stack

- Java 21, Spring Boot 3.4.1, Gradle 8.13 (Kotlin DSL)
- Spring Data JPA + Flyway (PostgreSQL)
- Testcontainers 1.21.1 (singleton container pattern)
- springdoc-openapi 2.7.0 (Swagger UI at `/swagger-ui.html`)

## Running locally

```bash
# Start PostgreSQL
docker run -d -p 5432:5432 -e POSTGRES_DB=cinema -e POSTGRES_USER=cinema -e POSTGRES_PASSWORD=cinema postgres:16-alpine

# Run the application
./gradlew bootRun
```

## Testing

```bash
# Unit tests
./gradlew test

# Integration tests (Testcontainers — requires Docker)
./gradlew integrationTest
```

## API examples

### Schedule a screening

```bash
curl -s -X POST http://localhost:8080/screenings \
  -H 'Content-Type: application/json' \
  -d '{
    "movieTitle": "Oppenheimer",
    "hall": { "name": "A", "rows": 10, "seatsPerRow": 20 },
    "startTime": "2025-12-31T20:00:00"
  }'
```

```json
{ "id": "b1e2c3d4-..." }
```

### Check seat availability

```bash
curl -s http://localhost:8080/screenings/b1e2c3d4-.../seats
```

```json
{
  "seats": [
    { "row": 1, "number": 1, "available": true },
    { "row": 1, "number": 2, "available": true }
  ]
}
```

### Reserve seats

```bash
curl -s -X POST http://localhost:8080/reservations \
  -H 'Content-Type: application/json' \
  -d '{
    "screeningId": "b1e2c3d4-...",
    "customerId": "a0b1c2d3-...",
    "seats": [
      { "row": 1, "number": 1 },
      { "row": 1, "number": 2 }
    ]
  }'
```

```json
{ "id": "f4e5d6c7-..." }
```

Reservation expires automatically after **15 minutes** if not confirmed.

### Confirm a reservation

```bash
curl -s -X POST http://localhost:8080/reservations/f4e5d6c7-.../confirm
```

Returns `204 No Content`.

### Error responses (RFC 7807)

```json
{
  "status": 422,
  "detail": "Seat [1,1] not found or unavailable in screening b1e2c3d4-..."
}
```

```json
{
  "status": 409,
  "detail": "Cannot confirm an expired reservation"
}
```

## Reservation lifecycle

```
PENDING ──(confirm)──► CONFIRMED
   │
   └──(expire after 15 min)──► EXPIRED
```

Expiry is handled by `ReservationExpiryScheduler`, which runs every 60 seconds, finds all `PENDING` reservations past their `expiresAt` timestamp, marks them `EXPIRED`, and releases the seats back to `AVAILABLE` on the corresponding `Screening`.
