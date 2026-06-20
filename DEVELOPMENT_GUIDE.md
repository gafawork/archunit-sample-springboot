# Development Guide - User Management API

## 🎯 Architecture Quick Reference

### When to Add Features

#### Domain Layer
Add to domain layer when:
- Creating new entities (e.g., `Role`, `Permission`)
- Adding business validation rules
- Creating specialized repositories
- Implementing domain-specific calculations

**Example: Adding Role Entity**
```java
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String description;
}
```

#### Application Layer
Add to application layer when:
- Creating new use cases
- Adding DTOs for new features
- Implementing application-level orchestration

**Example: New UseCase**
```java
@Service
public class AssignRoleToUserUseCase {
    private final UserDomainService userDomainService;
    private final RoleRepository roleRepository;
    
    public void execute(Long userId, Long roleId) {
        User user = userDomainService.findUserById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        
        user.assignRole(role);
        userDomainService.saveUser(user);
    }
}
```

#### Presentation Layer
Add to presentation layer when:
- Creating new REST endpoints
- Adding new exception types
- Modifying API contracts (DTOs)

**Example: New Controller Endpoint**
```java
@PostMapping("/{id}/roles/{roleId}")
public ResponseEntity<Void> assignRoleToUser(
        @PathVariable Long id,
        @PathVariable Long roleId) {
    assignRoleToUserUseCase.execute(id, roleId);
    return ResponseEntity.ok().build();
}
```

---

## 📦 Adding New Entities

### Step 1: Create Domain Entity
```java
@Entity
@Table(name = "entity_name")
public class EntityName {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // Add fields
}
```

### Step 2: Create Repository
```java
@Repository
public interface EntityNameRepository extends JpaRepository<EntityName, Long> {
    // Custom query methods
}
```

### Step 3: Add Domain Service Methods
```java
@Service
public class EntityNameDomainService {
    private final EntityNameRepository repository;
    
    public EntityName create(String name) {
        return new EntityName(name);
    }
}
```

### Step 4: Create DTOs
```java
public class CreateEntityNameRequest {
    @NotBlank
    private String name;
}

public class EntityNameResponse {
    private Long id;
    private String name;
}
```

### Step 5: Create Use Cases
```java
@Service
public class CreateEntityNameUseCase {
    private final EntityNameDomainService service;
    
    @Transactional
    public EntityNameResponse execute(CreateEntityNameRequest request) {
        EntityName entity = service.create(request.getName());
        return toResponse(service.save(entity));
    }
}
```

### Step 6: Create Controller
```java
@RestController
@RequestMapping("/api/v1/entity-names")
public class EntityNameController {
    private final CreateEntityNameUseCase createUseCase;
    
    @PostMapping
    public ResponseEntity<EntityNameResponse> create(
            @Valid @RequestBody CreateEntityNameRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(createUseCase.execute(request));
    }
}
```

---

## 🔄 Adding Relationships

### One-to-Many: User → Posts

#### 1. Update User Entity
```java
@Entity
public class User {
    // ... existing fields
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Post> posts = new ArrayList<>();
    
    public void addPost(Post post) {
        posts.add(post);
        post.setUser(this);
    }
}
```

#### 2. Create Post Entity
```java
@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
```

#### 3. Create PostRepository
```java
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserId(Long userId);
}
```

---

## 🧪 Testing Guide

### Unit Test Example
```java
@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest {
    
    @Mock
    private UserDomainService userDomainService;
    
    @InjectMocks
    private CreateUserUseCase useCase;
    
    @Test
    void shouldCreateUserSuccessfully() {
        // Arrange
        CreateUserRequest request = CreateUserRequest.builder()
            .email("test@example.com")
            .firstName("John")
            .lastName("Doe")
            .password("password123")
            .build();
        
        User user = new User(request.getEmail(), 
            request.getFirstName(), 
            request.getLastName(), 
            request.getPassword());
        
        when(userDomainService.createUser(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(user);
        when(userDomainService.saveUser(any(User.class)))
            .thenReturn(user);
        
        // Act
        UserResponse response = useCase.execute(request);
        
        // Assert
        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
    }
}
```

### Integration Test Example
```java
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void shouldCreateAndRetrieveUser() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
            .email("test@example.com")
            .firstName("John")
            .lastName("Doe")
            .password("password123")
            .build();
        
        mockMvc.perform(post("/api/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("test@example.com"));
    }
}
```

---

## 🔐 Security Implementation

### Add Password Encryption

#### 1. Add Spring Security Dependency
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

#### 2. Update User Entity
```java
@Entity
public class User {
    // ... existing fields
    
    @Column(nullable = false)
    private String password;
    
    public void setPassword(String rawPassword) {
        this.password = new BCryptPasswordEncoder().encode(rawPassword);
    }
}
```

#### 3. Update Domain Service
```java
@Service
public class UserDomainService {
    public User createUser(String email, String firstName, 
                          String lastName, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        User user = new User(email, firstName, lastName, "");
        user.setPassword(password); // Encryption happens here
        return user;
    }
}
```

---

## 📊 Database Migration

### Using Flyway

#### 1. Add Dependency
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

#### 2. Create Migration File
Create file: `src/main/resources/db/migration/V1__Initial_Schema.sql`

```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 3. Update application.properties
```properties
spring.flyway.locations=classpath:db/migration
spring.jpa.hibernate.ddl-auto=validate
```

---

## 📝 API Documentation with Springdoc OpenAPI

### 1. Add Dependency
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.0.0</version>
</dependency>
```

### 2. Add Controller Annotations
```java
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User Management API")
public class UserController {
    
    @PostMapping
    @Operation(summary = "Create a new user")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        // ...
    }
}
```

### 3. Access Swagger UI
Visit: `http://localhost:8080/swagger-ui.html`

---

## 🚀 Production Considerations

### 1. Database Configuration
Switch from H2 to PostgreSQL:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/userdb
spring.datasource.username=postgres
spring.datasource.password=password
spring.datasource.driver-class-name=org.postgresql.Driver
```

### 2. Environment Variables
```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

### 3. Logging Configuration
Create `src/main/resources/logback.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.log</file>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

### 4. Docker Support
Create `Dockerfile`:
```dockerfile
FROM openjdk:21-jdk-slim
COPY target/archunit-sample-springboot-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

```bash
docker build -t user-api .
docker run -p 8080:8080 user-api
```

---

## 💡 Best Practices Checklist

- [ ] Follow DDD principles consistently
- [ ] Keep entities free of framework annotations when possible
- [ ] Use transactions appropriately
- [ ] Validate input at API boundaries
- [ ] Handle errors gracefully
- [ ] Use meaningful exception messages
- [ ] Document complex business logic
- [ ] Write tests for critical paths
- [ ] Use dependency injection
- [ ] Keep controllers thin (orchestration only)
- [ ] Keep services focused (single responsibility)
- [ ] Use appropriate HTTP status codes
- [ ] Implement proper logging
- [ ] Secure sensitive data (passwords, tokens)
- [ ] Version your API

---

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Domain-Driven Design](https://martinfowler.com/bliki/DomainDrivenDesign.html)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [REST API Design Guidelines](https://restfulapi.net/)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)

---

**Happy Coding! 🚀**
