# Collaborative Task Tracking System

A RESTful backend API for collaborative task management. Users can create teams, manage tasks, assign work, and collaborate via comments and file attachments.

## Tech Stack

- **Java 17**
- **Spring Boot 4.1.1** (Spring MVC, Spring Security, Spring Data JPA)
- **MySQL** (production) / **H2** (tests)
- **JWT** authentication (jjwt 0.12.6), BCrypt password hashing
- **Maven** build (with Maven Wrapper)

## Prerequisites

- Java 17+
- MySQL running on `localhost:3306`
- Default DB credentials: user `root`, password `TestRoot` (configurable in `application.properties`)

## Getting Started

```bash
# 1. Run the application
mvnw.cmd spring-boot:run          # Windows
./mvnw spring-boot:run            # Linux/macOS

# 2. Build and run the JAR
mvnw.cmd package
java -jar target/ACollaborativeTask-TrackingSystem-0.0.1-SNAPSHOT.jar

# 3. Run tests
mvnw.cmd test
```

The API runs at **`http://localhost:8081`**.

### Configuration

Key settings in `src/main/resources/application.properties`:

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | `8081` | Server port |
| `spring.datasource.*` | `root`/`TestRoot` | MySQL connection settings |
| `app.upload.dir` | `C:/uploads` | File attachment storage location |
| `app.jwt.secret` | dev secret | JWT signing key (change in production) |
| `app.jwt.expiration-ms` | `86400000` | JWT validity (24h) |

The database `task_tracker` and tables (`users`, `teams`, `tasks`, `comments`, `attachments`, `token_blacklist`) are created automatically on first run.

## Authentication

Stateless JWT auth. All endpoints except `/api/auth/**` require a header:

```
Authorization: Bearer <token>
```

1. `POST /api/auth/register` — create an account
2. `POST /api/auth/login` — get a JWT token
3. `POST /api/auth/logout` — revoke the current token (server-side blacklist)

## API Endpoints

### Auth
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login, returns JWT |
| POST | `/api/auth/logout` | Revoke current token (JWT required) |

### Users
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/users/me` | View profile |
| PUT | `/api/users/me` | Update profile |

### Teams
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/teams` | Create a team (creator becomes OWNER) |
| GET | `/api/teams` | List teams the user belongs to |
| GET | `/api/teams/{id}` | Get team details |
| PUT | `/api/teams/{id}` | Update team (OWNER only) |
| DELETE | `/api/teams/{id}` | Delete team (OWNER only, no tasks allowed) |
| POST | `/api/teams/{id}/members` | Add member by email (OWNER only) |
| DELETE | `/api/teams/{id}/members/{userId}` | Remove member (OWNER only) |

### Tasks
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/tasks` | Create a task |
| GET | `/api/tasks` | List/filter tasks (see query params) |
| GET | `/api/tasks/assigned-to-me` | Tasks assigned to current user |
| GET | `/api/tasks/{id}` | Get a task |
| PUT | `/api/tasks/{id}` | Update a task (creator or assignee only) |
| DELETE | `/api/tasks/{id}` | Delete a task (creator or assignee only) |

**Task list query params:** `status` (`TO_DO`/`IN_PROGRESS`/`DONE`), `search` (keyword in title/description), `teamId`, `page`, `size` (default 20), `sort`.

**Task status:** `TO_DO`, `IN_PROGRESS`, `DONE` | **Priority:** `LOW`, `MEDIUM`, `HIGH`

### Comments
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/tasks/{taskId}/comments` | Add a comment |
| GET | `/api/tasks/{taskId}/comments` | List comments |

### Attachments
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/tasks/{taskId}/attachments` | Upload a file (multipart, max 10MB) |
| GET | `/api/tasks/{taskId}/attachments` | List attachments |
| GET | `/api/attachments/{attachmentId}/download` | Download a file |

## Quick Start (curl)

```bash
# Register
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"john@test.com","username":"john","password":"pass123","firstName":"John","lastName":"Doe"}'

# Login — copy the accessToken
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@test.com","password":"pass123"}'

# Create a team
curl -X POST http://localhost:8081/api/teams \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"name":"Frontend Team","description":"UI development"}'

# Create a task
curl -X POST http://localhost:8081/api/tasks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"title":"Fix login bug","description":"...","priority":"HIGH","dueDate":"2026-10-01"}'
```

## Authorization Rules

| Action | Allowed For |
|--------|-------------|
| View team / task details | Team members |
| Update/delete team, add/remove members | Team OWNER only |
| Update/delete task | Task creator or assignee |
| Comment / upload attachments | Task creator, assignee, or team members |

## Uploaded Attachments

Files are stored on disk in `C:/uploads/{taskId}/` with UUID-prefixed names; metadata is stored in the `attachments` table. **Max file size: 10MB.**

## Project Structure

```
src/main/java/com/cts/
├── config/         # Security & JWT configuration
├── controller/     # REST controllers
├── dto/            # Request/response records
├── entity/         # JPA entities
├── exception/      # Global exception handler
├── repository/     # Spring Data repositories
├── security/       # JWT util & auth filter
└── service/        # Business logic
```