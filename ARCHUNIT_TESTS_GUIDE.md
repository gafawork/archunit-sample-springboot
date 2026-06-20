# ArchUnit Architecture Tests - Documentation

## Overview

ArchUnit is a free, simple and extensible library for checking the architecture of your Java code using any plain Java test. This document explains the 9 architecture test classes created for the User Management API.

## Setup Instructions

### 1. Maven Dependency

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit-junit5</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>
```

### 2. Run Tests

```bash
mvn test
```

## Architecture Test Classes

### 1. **DomainLayerArchitectureTest.java**
**Location:** `src/test/java/com/example/user/architecture/DomainLayerArchitectureTest.java`

**Purpose:** Validates Domain-Driven Design principles for the domain layer.

**Rules Tested:**
- ✓ Domain classes must reside in `..domain..` package
- ✓ Domain layer must NOT depend on Application layer
- ✓ Domain layer must NOT depend on Presentation layer  
- ✓ Domain layer must NOT depend on Spring Framework
- ✓ User entity must be in `..entity..` package
- ✓ All repositories must have `Repository` suffix
- ✓ All domain services must have `DomainService` suffix

**Example Violation Detection:**
```java
// This would violate the rules:
// Domain class depending on Spring annotations (except JPA)
@Autowired  // ❌ VIOLATION
UserService userService;
```

---

### 2. **ApplicationLayerArchitectureTest.java**
**Location:** `src/test/java/com/example/user/architecture/ApplicationLayerArchitectureTest.java`

**Purpose:** Ensures application layer (use cases) follows clean architecture patterns.

**Rules Tested:**
- ✓ Application layer classes must be in `..application..` package
- ✓ Use cases must be in `..usecase..` package
- ✓ Request DTOs must be in `..dto..` package
- ✓ Response DTOs must be in `..dto..` package
- ✓ Application layer must NOT depend on Presentation layer
- ✓ Use cases must depend on domain services or repositories
- ✓ Use cases must follow `[Operation]UseCase` naming convention

**Example Rule:**
```java
@Service
public class CreateUserUseCase {
    // ✓ Depends on domain service
    private final UserDomainService userDomainService;
    
    // ✓ NOT depending on controllers
}
```

---

### 3. **PresentationLayerArchitectureTest.java**
**Location:** `src/test/java/com/example/user/architecture/PresentationLayerArchitectureTest.java`

**Purpose:** Validates REST API layer structure and best practices.

**Rules Tested:**
- ✓ Presentation classes must be in `..presentation..` package
- ✓ All controllers must have `@RestController` annotation
- ✓ Exception handlers must be in `..exception..` package
- ✓ Controllers must NOT directly depend on domain services
- ✓ Controllers must depend on use cases
- ✓ Controllers must NOT be annotated with `@Service`
- ✓ Controllers must follow `[Entity]Controller` naming convention

**Example Rule:**
```java
@RestController  // ✓ REQUIRED
@RequestMapping("/api/v1/users")
public class UserController {
    // ✓ Only depends on use cases, NOT on domain services
    private final CreateUserUseCase createUserUseCase;
}
```

---

### 4. **DDDArchitectureTest.java**
**Location:** `src/test/java/com/example/user/architecture/DDDArchitectureTest.java`

**Purpose:** Enforces Domain-Driven Design patterns across all layers.

**Rules Tested:**
- ✓ Domain layer must NOT depend on Application or Presentation layers
- ✓ Application layer must NOT depend on Presentation layer
- ✓ Entity classes must be annotated with `@Entity`
- ✓ All classes must reside in `com.example.user..` package
- ✓ All three layers (domain, application, presentation) must exist
- ✓ Presentation layer must not create circular dependencies

**Layer Dependency Flow (CORRECT):**
```
Presentation Layer
        ↓
Application Layer (Use Cases)
        ↓
Domain Layer (Business Logic)

❌ NEVER:
Presentation ← Application ← Domain (backward flow is violation)
```

---

### 5. **CodingStyleArchitectureTest.java**
**Location:** `src/test/java/com/example/user/architecture/CodingStyleArchitectureTest.java`

**Purpose:** Enforces consistent coding style and conventions.

**Rules Tested:**
- ✓ Repositories must be annotated with `@Repository`
- ✓ Services and use cases must be annotated with `@Service`
- ✓ DTO classes must be public
- ✓ All class names must follow PascalCase convention
- ✓ Test classes must NOT be in main code packages
- ✓ Main application class must be in root package
- ✓ Response DTOs must not expose passwords

**Example Rule:**
```java
// ✓ CORRECT
@Repository
public interface UserRepository extends JpaRepository<User, Long> { }

// ❌ VIOLATION
public interface UserRepository extends JpaRepository<User, Long> { }
// Missing @Repository annotation
```

---

### 6. **RestControllerArchitectureTest.java**
**Location:** `src/test/java/com/example/user/architecture/RestControllerArchitectureTest.java`

**Purpose:** REST-specific architecture rules for controllers.

**Rules Tested:**
- ✓ REST controllers must have both `@RestController` and `@RequestMapping`
- ✓ Controllers should only depend on standard libraries and app code
- ✓ Controller names must match pattern: `[Entity]Controller`
- ✓ REST controllers must NOT have static methods
- ✓ Controller methods must be public or private

**Example Rule:**
```java
// ✓ CORRECT
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    @PostMapping
    public ResponseEntity<UserResponse> createUser(...) { }
}

// ❌ VIOLATION
@Component  // Should be @RestController
public class UserController {
    static void createUser(...) { }  // No static methods
}
```

---

### 7. **ValidationArchitectureTest.java**
**Location:** `src/test/java/com/example/user/architecture/ValidationArchitectureTest.java`

**Purpose:** Ensures input validation is properly implemented in DTOs.

**Rules Tested:**
- ✓ Request DTOs must have validation annotations
- ✓ CreateUserRequest must have email validation
- ✓ CreateUserRequest must have required fields (firstName, lastName, password)
- ✓ Response DTOs must NOT contain sensitive data
- ✓ All request DTOs must be in `..dto..` package
- ✓ All response DTOs must be in `..dto..` package

**Example Rule:**
```java
// ✓ CORRECT
public class CreateUserRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "First name is required")
    private String firstName;
}

// ❌ VIOLATION
public class UserResponse {
    private String password;  // Should NOT expose password
}
```

---

### 8. **EntityArchitectureTest.java**
**Location:** `src/test/java/com/example/user/architecture/EntityArchitectureTest.java`

**Purpose:** Validates entity design and domain logic separation.

**Rules Tested:**
- ✓ Entity classes must NOT depend on repositories or services
- ✓ User entity must have all required fields
- ✓ User entity must have business methods (getFullName, activate, deactivate)
- ✓ Entities must NOT directly access repositories
- ✓ Entity must be annotated with `@Entity`
- ✓ Only entity classes should use `@Entity` annotation

**Example Rule:**
```java
// ✓ CORRECT
@Entity
public class User {
    private String email;
    private String firstName;
    
    // Domain logic methods
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public void activate() {
        this.active = true;
    }
}

// ❌ VIOLATION
@Entity
public class User {
    // Should NOT have repository dependency
    @Autowired
    UserRepository repository;
}
```

---

### 9. **RepositoryArchitectureTest.java**
**Location:** `src/test/java/com/example/user/architecture/RepositoryArchitectureTest.java`

**Purpose:** Repository pattern and data access layer validation.

**Rules Tested:**
- ✓ Repositories must be interfaces
- ✓ Repositories must NOT implement business logic
- ✓ Repositories must NOT depend on services or use cases
- ✓ UserRepository must have standard CRUD methods
- ✓ Repositories must NOT depend on DTOs
- ✓ Repository names must match pattern: `[Entity]Repository`

**Example Rule:**
```java
// ✓ CORRECT
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}

// ❌ VIOLATION
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Should NOT depend on DTOs
    UserResponse getUserResponse(Long id);
}
```

---

## Running Tests

### Run all ArchUnit tests:
```bash
mvn test -Dtest=*ArchitectureTest
```

### Run specific architecture test:
```bash
mvn test -Dtest=DomainLayerArchitectureTest
```

### Run all tests including ArchUnit:
```bash
mvn clean test
```

## Expected Output

When all rules pass:
```
[INFO] Running com.example.user.architecture.DomainLayerArchitectureTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.5 s
[INFO] Running com.example.user.architecture.ApplicationLayerArchitectureTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.8 s
...
[INFO] BUILD SUCCESS
```

## Violation Examples

### Violation 1: Domain depends on Application
```java
// ❌ VIOLATION DETECTED
@Entity
public class User {
    @Autowired
    CreateUserUseCase createUserUseCase;  // Domain should NOT know about use cases
}
```

**Error Message:**
```
Domain layer should not depend on Application layer
  found unauthorized dependency from User to CreateUserUseCase
```

---

### Violation 2: Controller doesn't use @RestController
```java
// ❌ VIOLATION DETECTED
@Service
public class UserController {  // Should be @RestController
    public UserResponse createUser(...) { }
}
```

**Error Message:**
```
All controller classes should have @RestController annotation
  found 0 classes annotated with @RestController in UserController
```

---

### Violation 3: Request DTO without validation
```java
// ❌ VIOLATION DETECTED
public class CreateUserRequest {
    private String email;  // Missing @Email validation
    private String firstName;  // Missing @NotBlank validation
}
```

**Error Message:**
```
Request DTOs should have field-level validation annotations
  found 0 validation annotations in CreateUserRequest fields
```

---

## Adding New Rules

To add a new architecture rule:

```java
@ArchTest
static final ArchRule my_new_rule =
    classes()
        .that().haveSimpleName("MyClass")
        .should().resideInAPackage("com.example.my..")
        .as("My custom rule description");
```

## Common ArchUnit Methods

| Method | Purpose | Example |
|--------|---------|---------|
| `classes()` | Select classes | `classes().that().resideInAPackage("..domain..")` |
| `noClasses()` | Negative assertion | `noClasses().that().dependOnClassesThat()...` |
| `haveSimpleName()` | Match by class name | `.haveSimpleName("User")` |
| `resideInAPackage()` | Match by package | `.resideInAPackage("..domain..")` |
| `beAnnotatedWith()` | Require annotation | `.beAnnotatedWith(Repository.class)` |
| `dependOnClassesThat()` | Check dependencies | `.dependOnClassesThat().resideInAPackage("..service..")` |
| `should()/shouldNot()` | Assertion | `.should().resideInAPackage()` |
| `.as()` | Error message | `.as("Custom error message")` |

---

## Benefits of ArchUnit Testing

✅ **Prevents Architecture Decay**
- Catches violations early before code review
- Prevents "just this once" exceptions

✅ **Enforces Best Practices**
- Maintains clean architecture patterns
- Enforces DDD principles consistently

✅ **Documentation**
- Architecture rules become executable documentation
- New developers understand code structure

✅ **CI/CD Integration**
- Tests run in pipeline
- Failed builds on violations
- Prevents merging architecture-breaking code

✅ **Refactoring Confidence**
- Move code safely knowing architecture is checked
- Immediate feedback on structural changes

---

## Troubleshooting

### "Package does not exist" errors
**Solution:** Run `mvn dependency:resolve` to download ArchUnit

### "ArchTest annotation not found"
**Solution:** Ensure archunit-junit5 dependency is in pom.xml

### Tests not running
**Solution:** Ensure test files are in `src/test/java` directory

---

## References

- **ArchUnit Homepage:** https://www.archunit.org/
- **ArchUnit Documentation:** https://www.archunit.org/userguide/html/
- **ArchUnit GitHub:** https://github.com/TNG/ArchUnit
- **DDD Patterns:** https://martinfowler.com/bliki/DomainDrivenDesign.html
- **Clean Architecture:** https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html

---

## Summary

| Test Class | Rules | Focus Area |
|-----------|-------|-----------|
| DomainLayerArchitectureTest | 7 | Domain layer isolation |
| ApplicationLayerArchitectureTest | 7 | Use case patterns |
| PresentationLayerArchitectureTest | 7 | REST controller design |
| DDDArchitectureTest | 6 | DDD principles |
| CodingStyleArchitectureTest | 7 | Code conventions |
| RestControllerArchitectureTest | 5 | HTTP endpoints |
| ValidationArchitectureTest | 6 | Input validation |
| EntityArchitectureTest | 6 | Entity design |
| RepositoryArchitectureTest | 6 | Data access |
| **TOTAL** | **57** | **Complete Architecture** |

---

**All 9 test classes are ready to enforce your architecture!**
