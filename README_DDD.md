# User Management API - Domain-Driven Design

A Spring Boot application for user management built using Domain-Driven Design (DDD) principles with RESTful API endpoints for CRUD operations.

## Architecture Overview

This project follows the Domain-Driven Design pattern with clear separation of concerns:

```
├── domain/
│   ├── entity/        # Core business entities
│   ├── repository/    # Repository interfaces (data access contracts)
│   └── service/       # Domain services (business logic)
├── application/
│   ├── dto/           # Data Transfer Objects (request/response)
│   └── usecase/       # Application use cases (orchestration)
└── presentation/
    ├── controller/    # REST API controllers
    └── exception/     # Exception handling and error responses
```

## Technology Stack

- **Java 21** - Programming language
- **Spring Boot 4.0.6** - Framework
- **Spring Data JPA** - ORM and data access
- **H2 Database** - In-memory database (can be replaced with MySQL/PostgreSQL)
- **Lombok** - Reduces boilerplate code
- **JUnit 5** - Testing framework

## Running the Application

### Prerequisites
- Java 21 or higher
- Maven 3.6+

### Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Access H2 Console (Development)

- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:userdb`
- Username: `sa`
- Password: (empty)

## API Endpoints

All endpoints use the base URL: `/api/v1/users`

### 1. Create User (POST)

**Endpoint:** `POST /api/v1/users`

**Request Body:**
```json
{
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "password": "securePassword123"
}
```

**Success Response (201 Created):**
```json
{
  "id": 1,
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "fullName": "John Doe",
  "active": true
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Validation failed",
  "status": 400,
  "timestamp": "2026-05-31T08:30:00",
  "errors": {
    "email": "Email should be valid"
  }
}
```

### 2. List All Users (GET)

**Endpoint:** `GET /api/v1/users`

**Success Response (200 OK):**
```json
[
  {
    "id": 1,
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "fullName": "John Doe",
    "active": true
  }
]
```

### 3. Get User by ID (GET)

**Endpoint:** `GET /api/v1/users/{id}`

**Success Response (200 OK):**
```json
{
  "id": 1,
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "fullName": "John Doe",
  "active": true
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "User not found with id: 99",
  "status": 400,
  "timestamp": "2026-05-31T08:30:00"
}
```

### 4. Update User (PUT)

**Endpoint:** `PUT /api/v1/users/{id}`

**Request Body:**
```json
{
  "firstName": "Jane",
  "lastName": "Smith"
}
```

**Success Response (200 OK):**
```json
{
  "id": 1,
  "email": "john.doe@example.com",
  "firstName": "Jane",
  "lastName": "Smith",
  "fullName": "Jane Smith",
  "active": true
}
```

### 5. Delete User (DELETE)

**Endpoint:** `DELETE /api/v1/users/{id}`

**Success Response (204 No Content):**
- No response body

## Testing with cURL

```bash
# Create a user
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "password": "password123"
  }'

# List all users
curl -X GET http://localhost:8080/api/v1/users

# Get user by ID
curl -X GET http://localhost:8080/api/v1/users/1

# Update user
curl -X PUT http://localhost:8080/api/v1/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith"
  }'

# Delete user
curl -X DELETE http://localhost:8080/api/v1/users/1
```

## DDD Pattern Explanation

### Domain Layer
- **User Entity**: Core business entity representing a user with domain logic
- **UserRepository**: Interface defining data access contracts
- **UserDomainService**: Encapsulates domain-specific business logic

### Application Layer
- **DTOs**: Isolate domain entities from external representations
- **Use Cases**: Orchestrate domain services for specific business operations
  - `CreateUserUseCase`: Handles user creation
  - `ListUsersUseCase`: Retrieves all users
  - `GetUserByIdUseCase`: Retrieves a specific user
  - `UpdateUserUseCase`: Updates user information
  - `DeleteUserUseCase`: Deletes a user

### Presentation Layer
- **UserController**: REST API endpoints
- **GlobalExceptionHandler**: Centralized exception handling
- **ErrorResponse**: Standardized error format

## Key Features

✅ Complete CRUD operations
✅ Input validation using Jakarta Validation
✅ Global exception handling
✅ RESTful API design
✅ Domain-Driven Design patterns
✅ H2 in-memory database
✅ JPA entity mapping
✅ Transactional operations
✅ Comprehensive test coverage
✅ Lombok for code reduction

## Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserControllerTest
```

## Future Enhancements

- [ ] User authentication and authorization
- [ ] Password encryption (bcrypt)
- [ ] User roles and permissions
- [ ] Pagination and sorting
- [ ] Email verification
- [ ] User profile updates
- [ ] API documentation (Swagger/OpenAPI)
- [ ] Docker containerization
- [ ] Database migration (Flyway)
- [ ] Caching layer (Redis)

## Project Structure

```
src/
├── main/
│   ├── java/com/example/
│   │   ├── UserManagementApplication.java
│   │   └── user/
│   │       ├── domain/
│   │       │   ├── entity/User.java
│   │       │   ├── repository/UserRepository.java
│   │       │   └── service/UserDomainService.java
│   │       ├── application/
│   │       │   ├── dto/
│   │       │   │   ├── CreateUserRequest.java
│   │       │   │   ├── UpdateUserRequest.java
│   │       │   │   └── UserResponse.java
│   │       │   └── usecase/
│   │       │       ├── CreateUserUseCase.java
│   │       │       ├── ListUsersUseCase.java
│   │       │       ├── GetUserByIdUseCase.java
│   │       │       ├── UpdateUserUseCase.java
│   │       │       └── DeleteUserUseCase.java
│   │       └── presentation/
│   │           ├── controller/UserController.java
│   │           └── exception/
│   │               ├── GlobalExceptionHandler.java
│   │               └── ErrorResponse.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/example/user/
        ├── presentation/controller/UserControllerTest.java
        └── application/usecase/
```

## Configuration

The application is configured via `application.properties`:

```properties
spring.application.name=User Management API
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
spring.jpa.hibernate.ddl-auto=update
spring.datasource.url=jdbc:h2:mem:userdb
server.port=8080
```

## License

This project is provided as a sample application for educational purposes.

## Contact & Support

For questions or support, please refer to the project documentation or contact the development team.
