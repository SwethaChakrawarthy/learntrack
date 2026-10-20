# LearnTrack

A course-enrollment platform built as Spring Boot microservices with a React frontend.

## Services
| Service | Port | Status | Purpose |
|---|---|---|---|
| auth-service | 8081 | Done | Registration, login, JWT issuing |
| course-service | 8082 | Done | Course catalog and enrollments (PostgreSQL + Redis cache-aside) |
| notification-service | | Planned | Consumes Kafka enrollment events |
| gateway | | Planned | Spring Cloud Gateway entry point |
| frontend | | Planned | React + TypeScript |

## Run locally
1. `cp .env.example .env` and fill in the values.
2. `docker compose up -d` to start PostgreSQL and Redis.
3. In each service folder, export the same values and run `mvn spring-boot:run`
   (or run the `*Application` class from IntelliJ with the env variables set in the run configuration).
4. Run tests with `mvn test` in each service folder.

## Auth API (auth-service, 8081)
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | /api/auth/register | Public | Create a student account |
| POST | /api/auth/login | Public | Returns a JWT |
| GET | /api/auth/me | Bearer token | Current user's profile |

## Course API (course-service, 8082)
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | /api/courses | Public | Course catalog (cached in Redis) |
| GET | /api/courses/{id} | Public | One course (cached in Redis) |
| POST | /api/courses | INSTRUCTOR | Create a course |
| PUT | /api/courses/{id} | INSTRUCTOR (owner) | Update a course |
| POST | /api/courses/{id}/enrollments | STUDENT | Enroll in a course |
| GET | /api/enrollments/me | Bearer token | My enrollments |
| PATCH | /api/enrollments/{id}/progress | Bearer token (owner) | Update progress 0–100 |

## Design notes
- **Database per service:** auth-service and course-service each own their own PostgreSQL database.
- **Cache-aside with Redis:** reads check Redis first, fall back to PostgreSQL on a miss, then cache the result for 10 minutes. Writes evict the affected keys after the transaction commits. If Redis is down, reads fall back to the database.
- **Duplicate enrollments** are blocked by an application check and by a database unique constraint, which catches two simultaneous requests.

## Making an instructor account
Registration always creates STUDENT accounts. To promote one for testing:
```bash
docker exec -it learntrack-postgres psql -U learntrack -d learntrack_auth \
  -c "UPDATE users SET role='INSTRUCTOR' WHERE email='teacher@example.com';"
```
Then log in again, because the role is stored inside the token.
