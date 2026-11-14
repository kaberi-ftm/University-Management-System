# University Management System

Spring Boot (Maven) project with **MVC + Thymeleaf UI**, a **REST API**, **Spring Security** (authentication, BCrypt, RBAC) and **Docker Compose** (app + PostgreSQL).

## Run with Docker

```bash
docker compose up --build
```
INCASE OF UPDATE
```bash
docker run --rm -v "${PWD}:/app" -w /app maven:3.9-eclipse-temurin-17 mvn -B clean package -DskipTests

docker compose up --build
```



Open http://localhost:8080 and log in. Accounts created on first start (values come from `.env`):

|Role|Username|Password|
|-|-|-|
|Admin|`admin`|`admin123`|
|Teacher|`teacher`|`teacher123`|
|Student|`student`|`student123`|

Stop: `docker compose down` (add `-v` to wipe the database).

Run without Docker: Java 17 + Maven + a local PostgreSQL (`umsdb` / `ums` / `ums\_pass`), then `mvn spring-boot:run`.

## Access rules

|Panel|UI path|REST path|Admin|Teacher|Student|
|-|-|-|:-:|:-:|:-:|
|Admins|`/admins`|`/api/admins`|yes|no|no|
|Teachers|`/teachers`|`/api/teachers`|yes|yes|no|
|Students|`/students`|`/api/students`|yes|no|yes|

A wrong role gets the "Access Denied" page in the UI and `403` JSON from the API. Not logged in: redirect to `/login` (UI) or `401` JSON (API). The last remaining admin cannot be deleted.

## Security setup

* **Web UI:** form login at `/login`, session cookie, CSRF protection, logout via POST `/logout`.
* **REST API (`/api/\*\*`):** HTTP Basic, stateless.
* Passwords are stored as BCrypt hashes and never returned by the API.

## REST API

For each of `/api/admins`, `/api/teachers`, `/api/students`:

|Method|Path|Action|Success|
|-|-|-|-|
|GET|`/`|List|200|
|GET|`/{id}`|Get one|200|
|POST|`/`|Create|201 + `Location`|
|PUT|`/{id}`|Update|200|
|DELETE|`/{id}`|Delete|204|

Errors: `400` validation, `401` not authenticated, `403` forbidden, `404` not found, `409` duplicate username.

Body for POST/PUT (`password` optional on PUT):

```json
{ "username": "teacher2", "password": "secret123", "fullName": "Dr. Rahman",
  "email": "rahman@univ.edu", "department": "CSE" }
```

```bash
curl -u admin:admin123 http://localhost:8080/api/teachers
curl -u admin:admin123 -H "Content-Type: application/json" -X POST http://localhost:8080/api/students \\
  -d '{"username":"s2","password":"secret123","fullName":"Zohra","email":"z@univ.edu","department":"CSE"}'
curl -i -u teacher:teacher123 http://localhost:8080/api/students     # 403
```

## Structure (MVC)

```
.github/workflows/build.yml
src/main/java/com/example/ums/
  UmsApplication.java
  config/       DataInitializer              (seed users)
  controller/   HomeController, Base/Admin/Teacher/StudentViewController   (MVC pages)
    api/        Base/Admin/Teacher/StudentRestController                   (REST)
  dto/          UserRequest, UserResponse
  exception/    GlobalExceptionHandler, custom exceptions
  model/        AppUser, Role                (Model)
  repository/   UserRepository
  security/     SecurityConfig, DatabaseUserDetailsService
  service/      UserService
src/main/resources/
  application.properties
  static/css/style.css
  templates/    login, home, access-denied, fragments/layout, users/list, users/form   (View)
.env  .gitignore  .dockerignore  Dockerfile  compose.yaml  pom.xml  README.md
```

