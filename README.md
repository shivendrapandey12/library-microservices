# Library Management System (Microservices)

A Spring Boot microservices version of a library management system. A single application is split into independently deployable services that discover each other through Eureka, are reached through one API gateway, and each own their own database.

**Tech:** Java 21, Spring Boot 3.3, Spring Cloud (Gateway, Eureka, OpenFeign), Resilience4j, Spring Data JPA / Hibernate, MySQL, JWT, JUnit 5, Mockito, Docker Compose.

## Architecture

```
                        Client
                          |
                   api-gateway :8080        validates JWT, applies role rules
                          |
        +-----------------+------------------+
        |                 |                  |
 catalog-service   member-service    circulation-service
     :8081             :8082              :8083
   books, authors   members, login     issue / return
   (catalog_db)     (member_db)      (circulation_db)
        ^                 ^                  |
        |                 +------ Feign ------+
        +----------- Feign + circuit breaker -+

 discovery-server (Eureka) :8761  - every service registers here
```

| Service | Responsibility | Data |
|---|---|---|
| discovery-server | Eureka service registry | none |
| api-gateway | Single entry point, JWT validation, admin/user rules | none |
| catalog-service | Books, authors, copy availability | `catalog_db` |
| member-service | Registration, login, issues JWTs | `member_db` |
| circulation-service | Issue and return workflow, calls the other two | `circulation_db` |

## Key design decisions

- **Database per service.** Circulation stores only `memberId` and `bookId`. It never reads another service's tables; it asks over REST.
- **Concurrency on the last copy.** `Book` has a JPA `@Version` field. Two requests reserving the last copy at the same time cannot both succeed; the loser gets HTTP 409. This is the same optimistic-locking idea as the e-commerce inventory project, now across a service boundary.
- **No distributed transaction, so compensation.** Issuing a book reserves a copy in catalog, then saves the record locally. If the save fails, circulation calls `release` to give the copy back. Returning releases first, so a catalog outage leaves local data unchanged and the call can simply be retried.
- **Circuit breaker and fallback.** Feign calls are wrapped by Resilience4j. If catalog or member is down, circulation answers `503` quickly instead of hanging. Real `404`/`409` answers from the other service are passed through unchanged and do not open the circuit (see `Downstream.java`).
- **Authentication at the gateway.** Member-service issues JWTs; the gateway validates them once and forwards `X-User-Email` and `X-User-Role`. Internal services are not exposed to clients; only the gateway publishes a port.

## Run with Docker Compose

```bash
docker compose up --build
```

Then open http://localhost:8761 to see all services registered (allow about a minute for startup).

## Run locally without Docker

Each service falls back to an in-memory H2 database, so no MySQL is needed. Start in this order, each in its own terminal:

```bash
mvn -pl discovery-server spring-boot:run
mvn -pl catalog-service spring-boot:run
mvn -pl member-service spring-boot:run
mvn -pl circulation-service spring-boot:run
mvn -pl api-gateway spring-boot:run
```

## Run the tests

```bash
mvn test
```

## Try it

A demo admin and two demo books are created on first start (`admin@library.com` / `admin123`). Change or remove these for anything beyond local use.

```bash
# 1. Log in as admin and keep the token
TOKEN=$(curl -s -X POST localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@library.com","password":"admin123"}' | jq -r .token)

# 2. Register a normal member (public endpoint)
curl -s -X POST localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Asha","email":"asha@example.com","password":"secret1"}'

# 3. List books (any logged-in user)
curl -s localhost:8080/api/books -H "Authorization: Bearer $TOKEN"

# 4. Issue book 1 to member 2 (circulation calls member + catalog)
curl -s -X POST localhost:8080/api/circulation/issue \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"memberId":2,"bookId":1}'

# 5. Return it
curl -s -X POST localhost:8080/api/circulation/return/1 -H "Authorization: Bearer $TOKEN"
```

To see the circuit breaker work, stop `catalog-service` (`docker compose stop catalog-service`) and repeat step 4: you get `503` quickly instead of a hang. Start it again and issuing works once the circuit closes.

## API summary (via gateway on :8080)

| Method | Path | Access |
|---|---|---|
| POST | `/api/auth/register`, `/api/auth/login` | public |
| GET | `/api/books`, `/api/books/{id}`, `/api/authors` | logged-in user |
| POST | `/api/books`, `/api/authors` | ADMIN |
| GET | `/api/members`, `/api/members/{id}` | ADMIN |
| POST | `/api/circulation/issue`, `/api/circulation/return/{id}` | logged-in user |
| GET | `/api/circulation/member/{memberId}` | logged-in user |

## Known limits (good topics to discuss, and next steps)

- Compensation is best effort. A production system would add an outbox or a saga with retries.
- Services trust the gateway's headers and are not authenticated to each other; a real deployment would isolate them on a private network or add mTLS.
- The JWT secret is a shared symmetric key from configuration; production would use a secret store or asymmetric keys.
- Natural extensions: Kafka events for overdue notifications, distributed tracing, centralized configuration, Kubernetes manifests.
