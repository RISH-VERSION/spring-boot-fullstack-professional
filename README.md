# DeepThought HRMS - Worker Attendance & Overtime Settlement Engine

## Which HRMS I forked and why
Forked from [amigoscode/spring-boot-fullstack-professional](https://github.com/amigoscode/spring-boot-fullstack-professional) — clean Spring Boot + JPA + PostgreSQL structure, easy to extend without rewriting.

## AI Tools Used
- **Claude (Anthropic)** — schema design, service logic, Redis caching strategy, ticket fixes
- Used AI to generate boilerplate, then manually reviewed and understood every decision

## Setup Instructions

### Prerequisites
- Java 17+
- Maven
- Redis (running locally on port 6379)
- Supabase account

### Supabase Setup
1. Create project at supabase.com
2. Go to Connect → Transaction pooler (port 6543)
3. Copy connection string

### application.properties
spring.datasource.url=jdbc:postgresql://YOUR-POOLER-URL:6543/postgres?pgbouncer=true
spring.datasource.username=postgres.YOUR-PROJECT-ID
spring.datasource.password=YOUR-PASSWORD

### Run
```bash
mvn spring-boot:run
```
App starts on http://localhost:8080

## API Endpoints

### Workers
- POST /api/workers — create worker
- GET /api/workers — list all

### Sites
- POST /api/sites — create site
- GET /api/sites — list all

### Attendance
- POST /api/attendance/clock-in
- POST /api/attendance/clock-out
- GET /api/attendance/active — served from Redis
- GET /api/attendance/log?workerId=1&from=2026-01-01T00:00:00&to=2026-12-31T00:00:00

### Overtime
- GET /api/overtime/summary/{workerId}?month=2026-04
- POST /api/overtime/settle/{workerId}?month=2026-04

## Design Decisions
- **Redis TTL 16hrs** — auto-expires missed clock-outs, prevents stale cache
- **countQuery separate from main query** — fixes N+1 with pagination
- **@Transactional on settlement** — all-or-nothing, no partial state
- **Event publisher for SMS** — fires only after successful DB commit
- **HikariCP tuned for Supabase** — max-lifetime 270s, keepalive 60s to prevent idle drops
- **CORS externalized** — allowed origins in application.properties, not hardcoded