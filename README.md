# Ezy English Backend

Spring Boot backend for the Ezy English e-learning platform with JWT authentication, role-based access control, and MongoDB.

## Tech Stack

- **Java 17** + **Spring Boot 3.2**
- **Spring Security** with JWT (jjwt 0.12.x)
- **Spring Data MongoDB**
- **Lombok** for boilerplate reduction
- **Jakarta Validation** for request DTOs

## Project Structure

```
src/main/java/com/ezyenglish/
├── EzyEnglishApplication.java          # Main entry point
├── config/
│   ├── DatabaseSeeder.java             # Auto-seeds roles on first startup
│   └── MongoConfig.java                # Enables MongoDB auditing
├── controller/
│   ├── AuthController.java             # POST /api/auth/signup, /api/auth/signin
│   ├── UserController.java             # CRUD /api/users/**
│   ├── StudentController.java          # /api/students/**
│   ├── TeacherController.java          # /api/teachers/**
│   ├── ParentController.java           # /api/parents/**
│   └── TestController.java             # /api/test/** (role verification)
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── SignupRequest.java
│   │   └── UserUpdateRequest.java
│   └── response/
│       ├── JwtResponse.java
│       ├── MessageResponse.java
│       └── UserResponse.java
├── exception/
│   └── GlobalExceptionHandler.java     # Structured error responses
├── model/
│   ├── ERole.java                      # ROLE_STUDENT, ROLE_TEACHER, ROLE_ADMIN
│   ├── Role.java
│   ├── User.java
│   ├── StudentProfile.java
│   ├── TeacherProfile.java
│   └── AdminProfile.java
├── repository/
│   ├── UserRepository.java
│   ├── RoleRepository.java
│   ├── StudentProfileRepository.java
│   ├── TeacherProfileRepository.java
│   └── AdminProfileRepository.java
├── security/
│   ├── WebSecurityConfig.java          # Security filter chain, CORS, CSRF
│   ├── jwt/
│   │   ├── AuthEntryPointJwt.java      # 401 handler
│   │   ├── AuthTokenFilter.java        # JWT extraction filter
│   │   └── JwtUtils.java              # Token generate/parse/validate
│   └── service/
│       ├── UserDetailsImpl.java        # Spring Security UserDetails
│       └── UserDetailsServiceImpl.java # Loads users from MongoDB
└── service/
    ├── UserService.java                # User CRUD + profile enrichment
    ├── StudentService.java             # Course enrollment
    ├── TeacherService.java             # Course assignment
    └── ParentService.java              # Child linking
```

## Prerequisites

- **Java 17+** installed
- **MongoDB** running on `localhost:27017`

## Getting Started

1. **Start MongoDB** (if not already running):
   ```bash
   mongod
   ```

2. **Build and run**:
   ```bash
   ./mvnw spring-boot:run
   ```
   Or on Windows:
   ```bash
   mvnw.cmd spring-boot:run
   ```

3. Roles (`ROLE_STUDENT`, `ROLE_TEACHER`, `ROLE_ADMIN`) are **auto-seeded** on first startup.

## API Endpoints

### Authentication (Public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/signup` | Register (specify role: student/teacher/parent) |
| POST | `/api/auth/signin` | Login → returns JWT + roles |

### Users (Authenticated)

| Method | Endpoint | Security |
|--------|----------|----------|
| GET | `/api/users/profile` | Any authenticated user |
| GET | `/api/users/{id}` | Teacher or Parent |
| GET | `/api/users` | Teacher or Parent |
| PUT | `/api/users/{id}` | Own profile or Teacher/Parent |
| DELETE | `/api/users/{id}` | Teacher (admin) |

### Students

| Method | Endpoint | Security |
|--------|----------|----------|
| GET | `/api/students` | Teacher or Parent |
| POST | `/api/students/enroll` | Student |
| POST | `/api/students/unenroll` | Student |
| GET | `/api/students/profile` | Student |

### Teachers

| Method | Endpoint | Security |
|--------|----------|----------|
| POST | `/api/teachers/{id}/courses` | Teacher |
| DELETE | `/api/teachers/{id}/courses/{courseId}` | Teacher |
| GET | `/api/teachers/profile` | Teacher |
| GET | `/api/teachers/{id}/students?courseId=X` | Teacher |

### Parents

| Method | Endpoint | Security |
|--------|----------|----------|
| GET | `/api/parents/{id}/children` | Parent |
| POST | `/api/parents/{id}/children` | Parent |
| DELETE | `/api/parents/{id}/children/{childId}` | Parent |
| GET | `/api/parents/profile` | Parent |

## Example API Usage

### Register a Student
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_student",
    "email": "john@ezyenglish.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "roles": ["student"]
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_student",
    "password": "password123"
  }'
```

### Access Protected Endpoint
```bash
curl -X GET http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

## Configuration

Key properties in `application.properties`:

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | 8080 | Server port |
| `spring.data.mongodb.uri` | `mongodb://localhost:27017/ezyenglish` | MongoDB connection |
| `app.jwt.secret` | (configured) | JWT signing secret (Base64) |
| `app.jwt.expiration-ms` | 86400000 (24h) | JWT token expiry |
| `app.cors.allowed-origins` | `http://localhost:3000,http://localhost:5173` | CORS origins |