# Task Management API (Spring Boot + PostgreSQL)

A generative-AI exercise: use an AI coding tool to generate a REST API for tasks, review it, and document how it was done.

The code has not been compiled or tested. Before using it, build it, run the tests and check the dependency versions.

## Contents

1. [Scope and stack](#1-scope-and-stack)
2. [Endpoints](#2-endpoints)
3. [Phase 1: the prompt](#3-phase-1-the-prompt)
4. [Phase 2: structure](#4-phase-2-structure)
5. [Phase 3: what to verify](#5-phase-3-what-to-verify)
6. [Phase 4: code and tests](#6-phase-4-code-and-tests)
7. [How to run it](#7-how-to-run-it)
8. [Still missing for production](#8-still-missing-for-production)

---

## 1. Scope and stack

The exercise asks for task CRUD. Each task has `title`, `description`, `status` and `due_date`, and belongs to a user (a basic `User` model is assumed to exist).

Stack: Java 21, Spring Boot 3.x and Maven. Spring Data JPA with PostgreSQL and Flyway migrations. Spring Security with JWT and BCrypt. Bean Validation, springdoc-openapi, and JUnit 5, Mockito and Testcontainers for tests.

## 2. Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/login` | Returns a JWT |
| POST | `/api/tasks` | Creates a task (initial status `TODO`) |
| GET | `/api/tasks` | Paginated list. Filters: `status`, `dueDateFrom`, `dueDateTo` |
| GET | `/api/tasks/{id}` | Gets one of your tasks |
| PUT | `/api/tasks/{id}` | Updates one of your tasks |
| PATCH | `/api/tasks/{id}/status` | Changes only the status |
| DELETE | `/api/tasks/{id}` | Deletes one of your tasks |

Statuses: `TODO`, `IN_PROGRESS`, `DONE`. Sortable by `title`, `dueDate`, `status` and `createdAt`.

## 3. Phase 1: the prompt

A single-pass prompt meant for Claude Code or Cursor. If your tool has a plan mode, paste it there first and review the plan before generating code.

```text
You are a senior Java backend engineer. Generate a REST API for a task
management system.

## Stack
- Java 21, Spring Boot 3.x, Maven
- Spring Web, Spring Data JPA, Spring Validation, Spring Security
- PostgreSQL, schema managed with Flyway migrations (no ddl-auto=update)
- Lombok allowed
- springdoc-openapi for API docs
- JUnit 5, Mockito, Testcontainers (PostgreSQL) for tests

## Domain
- User (assume it exists, minimal): id (UUID), email (unique), passwordHash, createdAt
- Task: id (UUID), title (required, max 150), description (optional, max 2000),
  status (enum: TODO, IN_PROGRESS, DONE; default TODO),
  dueDate (optional, LocalDate), createdAt, updatedAt, owner (ManyToOne User)

## Functional requirements
- CRUD for tasks: POST /api/tasks, GET /api/tasks/{id}, GET /api/tasks,
  PUT /api/tasks/{id}, PATCH /api/tasks/{id}/status, DELETE /api/tasks/{id}
- GET /api/tasks supports pagination (page, size), sorting, and optional
  filters: status, dueDateFrom, dueDateTo
- Every task belongs to the authenticated user. A user can only see and modify
  their own tasks. Accessing another user's task returns 404 (not 403).

## Security
- JWT-based stateless authentication (login endpoint: POST /api/auth/login)
- Passwords hashed with BCrypt
- Secrets and DB credentials come from environment variables, never hardcoded
- The owner is ALWAYS taken from the security context, never from the request body

## Validation and error handling
- Use DTOs (request/response records); never expose JPA entities
- Bean Validation on request DTOs (@NotBlank, @Size, etc.)
- dueDate must not be in the past on creation (compare against UTC)
- Sorting only on an allow-list of fields; anything else is a 400
- @RestControllerAdvice with consistent error format (RFC 7807 ProblemDetail)
  covering: validation errors (400), not found (404), unauthorized (401),
  invalid enum/malformed JSON (400), unexpected errors (500, no stack trace leak)

## Architecture
- Layers: controller -> service -> repository, package by feature
- Keep it simple: one JPQL query for the optional filters (no Specification API),
  no extra config classes or abstraction layers beyond what the code needs
- Constructor injection, no field injection
- Transactions at the service layer, readOnly for queries
- Add DB indexes on tasks(owner_id, status) and tasks(owner_id, due_date)

## Tests
- Unit tests for the service layer (Mockito)
- Integration tests with MockMvc + Testcontainers covering: CRUD happy path,
  validation failures, access to another user's task, unauthenticated access

## Output
1. Project structure tree
2. Flyway migration SQL
3. All main classes (entity, repository, DTOs, service, controller,
   security config, exception handler)
4. Example tests
5. application.yml and a short README with run instructions

Do not invent features beyond the above. If something is ambiguous, state your
assumption in a comment at the top of the relevant file.
```

Why it's written this way: the stack and versions are explicit because otherwise the AI mixes `javax` with `jakarta` or reaches for `ddl-auto=update`. Security is a requirement rather than an extra, because a CRUD generated without it usually ships with no isolation between users. The negative constraints ("never expose entities", "never take the owner from the body") head off the typical mistakes. The "Keep it simple" line curbs the habit of inventing layers the problem doesn't need. And asking it to state its assumptions in comments makes visible what it decided on its own.

## 4. Phase 2: structure

```text
src/main/java/com/example/tasks/
├── TasksApplication.java
├── auth/
│   ├── AuthController.java
│   ├── AuthUser.java
│   ├── JwtService.java
│   ├── JwtAuthFilter.java
│   └── SecurityConfig.java
├── user/
│   ├── User.java
│   └── UserRepository.java
├── task/
│   ├── Task.java
│   ├── TaskStatus.java
│   ├── TaskRepository.java
│   ├── TaskService.java
│   ├── TaskController.java
│   └── dto/ (CreateTaskRequest, UpdateTaskRequest, UpdateStatusRequest, TaskResponse)
└── common/
    ├── NotFoundException.java
    ├── InvalidRequestException.java
    ├── UnauthorizedException.java
    └── GlobalExceptionHandler.java
src/main/resources/
├── application.yml
└── db/migration/V1__init.sql
src/test/java/com/example/tasks/
├── task/TaskServiceTest.java
└── TaskApiIT.java
```

## 5. Phase 3: what to verify

This code came out of a single AI pass and nobody has run it yet. This list covers what is worth checking, from cheapest to most expensive. Tick each item once you have confirmed it yourself.

### 5.1 Reading the code

- [ ] Controllers return DTOs, never JPA entities.
- [ ] The `owner` comes from the token, never from the body.
- [ ] `ddl-auto` is `validate` and Flyway owns the schema.
- [ ] No credentials or secrets in the code or in `application.yml`.
- [ ] Every dependency exists and its version is current. The AI sometimes invents artifacts or uses old versions.
- [ ] The assumptions the AI left in comments make sense.

### 5.2 When starting the application

- [ ] It builds and starts against a real PostgreSQL. Flyway and `ddl-auto: validate` raise no complaints.
- [ ] It refuses to start if `JWT_SECRET` is missing or too short.
- [ ] Swagger UI loads at `/swagger-ui.html`.

### 5.3 Where generated code tends to fail

These decisions are already applied in the code. Check that they behave as expected.

- [ ] A request without a token returns 401, not 403. With Spring Security 6, no configured entry point means 403.
- [ ] Hibernate generates the ID (`GenerationType.UUID`). Assigning it by hand makes `save()` run an extra `SELECT` before inserting.
- [ ] `?sort=` with an unknown field returns 400. Without the allow-list it would be a 500.
- [ ] ISO dates in query params (`2026-12-01`) parse correctly.
- [ ] A valid token for a deleted user returns 401, not 500.
- [ ] `dueDateFrom > dueDateTo` returns 400 instead of an empty list.
- [ ] The JPQL query with optional filters (`:status is null or ...`) works on PostgreSQL with every combination of null filters. This is the piece most likely to cause trouble, because PostgreSQL sometimes can't infer the type of a null parameter.
- [ ] A security error doesn't end up in the generic handler as a 500.
- [ ] The `dueDate` check uses UTC. Near midnight, someone in another time zone may see today's date rejected. Decide whether that works for you or you want a different rule.

### 5.4 Edge cases the code tries to cover

Authorization. Every task lookup goes through `findByIdAndOwnerId` or the `owner` filter in `search`. If the task belongs to someone else, the answer is 404 rather than 403, so its existence isn't confirmed. The DTOs have no `owner` field, so there is no mass assignment.

Authentication. The JWT is signed with a secret of at least 256 bits read from an environment variable. Login returns the same error for "unknown user" and "wrong password". Passwords use BCrypt and the token lasts 60 minutes.

Validation. Title is required and capped at 150 characters, description at 2000, and `status` is required on PUT. The database repeats the constraints with `NOT NULL` and a `CHECK` on `status`. The due date can't be in the past on creation, but it can on update, since editing an overdue task is legitimate. Malformed JSON, invalid enums and wrong types return 400 in `ProblemDetail` format. Unexpected errors return 500 without a stack trace.

The `TaskApiIT` tests cover: access to another user's task (read, update and delete), a list with only your own tasks, status filter, no token and tampered token, blank title, past date, invalid status, unknown sort field, inverted date range, and repeated delete.

### 5.5 Findings from the real run

When you run it, write down here what actually happened:

- What failed or behaved differently than expected.
- How you spotted it (compiler, test, manual check, review).
- How you fixed it.

That section is the one worth most in an interview, and only you can write it.

## 6. Phase 4: code and tests

Each block's path follows the structure in section 4.

### `pom.xml` (dependencies)

```xml
<dependencies>
  <!-- Versions managed by Spring Boot -->
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security</artifactId></dependency>
  <dependency><groupId>org.flywaydb</groupId><artifactId>flyway-core</artifactId></dependency>
  <dependency><groupId>org.flywaydb</groupId><artifactId>flyway-database-postgresql</artifactId></dependency>
  <dependency><groupId>org.postgresql</groupId><artifactId>postgresql</artifactId><scope>runtime</scope></dependency>
  <dependency><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId><optional>true</optional></dependency>

  <!-- Check for current versions -->
  <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-api</artifactId><version>0.12.6</version></dependency>
  <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-impl</artifactId><version>0.12.6</version><scope>runtime</scope></dependency>
  <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-jackson</artifactId><version>0.12.6</version><scope>runtime</scope></dependency>
  <dependency><groupId>org.springdoc</groupId><artifactId>springdoc-openapi-starter-webmvc-ui</artifactId><version>2.6.0</version></dependency>

  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
  <dependency><groupId>org.springframework.security</groupId><artifactId>spring-security-test</artifactId><scope>test</scope></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-testcontainers</artifactId><scope>test</scope></dependency>
  <dependency><groupId>org.testcontainers</groupId><artifactId>postgresql</artifactId><scope>test</scope></dependency>
  <dependency><groupId>org.testcontainers</groupId><artifactId>junit-jupiter</artifactId><scope>test</scope></dependency>
</dependencies>
```

### `db/migration/V1__init.sql`

```sql
CREATE TABLE users (
    id            UUID PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE tasks (
    id          UUID PRIMARY KEY,
    title       VARCHAR(150) NOT NULL,
    description VARCHAR(2000),
    status      VARCHAR(20)  NOT NULL DEFAULT 'TODO'
                CHECK (status IN ('TODO', 'IN_PROGRESS', 'DONE')),
    due_date    DATE,
    owner_id    UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_tasks_owner_status ON tasks(owner_id, status);
CREATE INDEX idx_tasks_owner_due    ON tasks(owner_id, due_date);
```

### `application.yml`

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  data:
    web:
      pageable:
        max-page-size: 100
app:
  jwt:
    secret: ${JWT_SECRET}          # Base64, at least 256 bits
    expiration-minutes: 60
```

### `user/User.java`, `user/UserRepository.java`

```java
@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @CreationTimestamp
    private Instant createdAt;
}

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
}
```

### `task/TaskStatus.java`, `task/Task.java`

```java
public enum TaskStatus { TODO, IN_PROGRESS, DONE }

@Entity
@Table(name = "tasks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Task {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status;

    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
```

### `task/TaskRepository.java`

```java
public interface TaskRepository extends JpaRepository<Task, UUID> {

    Optional<Task> findByIdAndOwnerId(UUID id, UUID ownerId);

    @Query("""
            select t from Task t
            where t.owner.id = :ownerId
              and (:status is null or t.status = :status)
              and (cast(:dueFrom as date) is null or t.dueDate >= :dueFrom)
              and (cast(:dueTo as date) is null or t.dueDate <= :dueTo)
            """)
    Page<Task> search(@Param("ownerId") UUID ownerId,
                      @Param("status") TaskStatus status,
                      @Param("dueFrom") LocalDate dueFrom,
                      @Param("dueTo") LocalDate dueTo,
                      Pageable pageable);
}
```

### `task/dto/*`

```java
public record CreateTaskRequest(
        @NotBlank @Size(max = 150) String title,
        @Size(max = 2000) String description,
        LocalDate dueDate) {}

public record UpdateTaskRequest(
        @NotBlank @Size(max = 150) String title,
        @Size(max = 2000) String description,
        @NotNull TaskStatus status,
        LocalDate dueDate) {}

public record UpdateStatusRequest(@NotNull TaskStatus status) {}

public record TaskResponse(
        UUID id, String title, String description,
        TaskStatus status, LocalDate dueDate,
        Instant createdAt, Instant updatedAt) {

    public static TaskResponse from(Task t) {
        return new TaskResponse(t.getId(), t.getTitle(), t.getDescription(),
                t.getStatus(), t.getDueDate(), t.getCreatedAt(), t.getUpdatedAt());
    }
}
```

### `common/*` (exceptions and error handler)

```java
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}

public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) { super(message); }
}

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) { super(message); }
}

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail notFound(NotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidRequestException.class)
    ProblemDetail invalid(InvalidRequestException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    ProblemDetail unauthorized(UnauthorizedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail forbidden(AccessDeniedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Access denied");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validation(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        pd.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField,
                        fe -> Objects.requireNonNullElse(fe.getDefaultMessage(), "invalid"),
                        (a, b) -> a)));
        return pd;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ProblemDetail badRequest(Exception ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Malformed request or invalid value");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
    }
}
```

### `task/TaskService.java`

```java
@Service
@RequiredArgsConstructor
public class TaskService {

    private static final Set<String> SORTABLE = Set.of("title", "dueDate", "status", "createdAt");

    private final TaskRepository tasks;
    private final UserRepository users;

    @Transactional
    public TaskResponse create(UUID userId, CreateTaskRequest req) {
        if (req.dueDate() != null && req.dueDate().isBefore(LocalDate.now(ZoneOffset.UTC))) {
            throw new InvalidRequestException("dueDate cannot be in the past");
        }
        User owner = users.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User no longer exists"));
        Task task = Task.builder()
                .title(req.title().trim())
                .description(req.description())
                .status(TaskStatus.TODO)
                .dueDate(req.dueDate())
                .owner(owner)
                .build();
        return TaskResponse.from(tasks.save(task));
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> list(UUID userId, TaskStatus status,
                                   LocalDate dueFrom, LocalDate dueTo, Pageable pageable) {
        if (dueFrom != null && dueTo != null && dueFrom.isAfter(dueTo)) {
            throw new InvalidRequestException("dueDateFrom must be <= dueDateTo");
        }
        pageable.getSort().forEach(order -> {
            if (!SORTABLE.contains(order.getProperty())) {
                throw new InvalidRequestException("Invalid sort field: " + order.getProperty());
            }
        });
        return tasks.search(userId, status, dueFrom, dueTo, pageable).map(TaskResponse::from);
    }

    @Transactional(readOnly = true)
    public TaskResponse get(UUID userId, UUID id) {
        return TaskResponse.from(getOwned(userId, id));
    }

    @Transactional
    public TaskResponse update(UUID userId, UUID id, UpdateTaskRequest req) {
        Task task = getOwned(userId, id);
        task.setTitle(req.title().trim());
        task.setDescription(req.description());
        task.setStatus(req.status());
        task.setDueDate(req.dueDate());
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse updateStatus(UUID userId, UUID id, TaskStatus status) {
        Task task = getOwned(userId, id);
        task.setStatus(status);
        return TaskResponse.from(task);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        tasks.delete(getOwned(userId, id));
    }

    // A task owned by someone else gets the same 404 as one that doesn't exist.
    private Task getOwned(UUID userId, UUID id) {
        return tasks.findByIdAndOwnerId(id, userId)
                .orElseThrow(() -> new NotFoundException("Task not found"));
    }
}
```

### `task/TaskController.java`

```java
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService service;

    @PostMapping
    public ResponseEntity<TaskResponse> create(@AuthenticationPrincipal AuthUser user,
                                               @Valid @RequestBody CreateTaskRequest req) {
        TaskResponse created = service.create(user.id(), req);
        return ResponseEntity.created(URI.create("/api/tasks/" + created.id())).body(created);
    }

    @GetMapping
    public Page<TaskResponse> list(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateTo,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.list(user.id(), status, dueDateFrom, dueDateTo, pageable);
    }

    @GetMapping("/{id}")
    public TaskResponse get(@AuthenticationPrincipal AuthUser user, @PathVariable UUID id) {
        return service.get(user.id(), id);
    }

    @PutMapping("/{id}")
    public TaskResponse update(@AuthenticationPrincipal AuthUser user, @PathVariable UUID id,
                               @Valid @RequestBody UpdateTaskRequest req) {
        return service.update(user.id(), id, req);
    }

    @PatchMapping("/{id}/status")
    public TaskResponse updateStatus(@AuthenticationPrincipal AuthUser user, @PathVariable UUID id,
                                     @Valid @RequestBody UpdateStatusRequest req) {
        return service.updateStatus(user.id(), id, req.status());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthUser user, @PathVariable UUID id) {
        service.delete(user.id(), id);
    }
}
```

### `auth/*` (JWT, filter, security config, login)

```java
public record AuthUser(UUID id) {}

@Service
public class JwtService {
    private final SecretKey key;
    private final long expirationMinutes;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        byte[] bytes = Decoders.BASE64.decode(secret);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 256 bits (Base64)");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expirationMinutes = expirationMinutes;
    }

    public String generate(UUID userId) {
        Instant now = Instant.now();
        return Jwts.builder().subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)))
                .signWith(key).compact();
    }

    public UUID extractUserId(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return UUID.fromString(claims.getSubject());
    }
}

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        String header = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            try {
                UUID userId = jwtService.extractUserId(header.substring(7));
                var auth = new UsernamePasswordAuthenticationToken(new AuthUser(userId), null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException | IllegalArgumentException e) {
                // Bad token: stay unauthenticated and let the entry point answer 401.
            }
        }
        chain.doFilter(req, res);
    }
}

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/api/auth/**", "/v3/api-docs/**",
                                "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}

public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
public record TokenResponse(String accessToken) {}

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        User user = users.findByEmail(req.email().trim().toLowerCase())
                .filter(u -> encoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        return new TokenResponse(jwt.generate(user.getId()));
    }
}
```

### `TaskServiceTest.java` (unit tests, Mockito)

```java
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock TaskRepository tasks;
    @Mock UserRepository users;
    @InjectMocks TaskService service;

    @Test
    void get_taskOfAnotherUser_throwsNotFound() {
        when(tasks.findByIdAndOwnerId(any(), any())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> service.get(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    void create_pastDueDate_throwsInvalidRequest() {
        LocalDate yesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1);
        var req = new CreateTaskRequest("t", null, yesterday);
        assertThrows(InvalidRequestException.class,
                () -> service.create(UUID.randomUUID(), req));
    }

    @Test
    void list_invertedDateRange_throwsInvalidRequest() {
        assertThrows(InvalidRequestException.class, () -> service.list(UUID.randomUUID(), null,
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 1, 1), Pageable.unpaged()));
    }
}
```

### `TaskApiIT.java` (integration tests, MockMvc + Testcontainers)

```java
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {
        "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "app.jwt.expiration-minutes=5"})
class TaskApiIT {

    @Container @ServiceConnection
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired JwtService jwt;
    @Autowired PasswordEncoder encoder;

    User alice, bob;

    @BeforeEach
    void setUp() {
        alice = newUser("alice-" + UUID.randomUUID() + "@test.com");
        bob = newUser("bob-" + UUID.randomUUID() + "@test.com");
    }

    @Test
    void createAndGetOwnTask() throws Exception {
        String id = createTask(alice, "Write report");
        mvc.perform(get("/api/tasks/" + id).header(AUTHORIZATION, bearer(alice)))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.title").value("Write report"))
           .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void otherUserGets404OnRead() throws Exception {
        String id = createTask(alice, "Private task");
        mvc.perform(get("/api/tasks/" + id).header(AUTHORIZATION, bearer(bob)))
           .andExpect(status().isNotFound());
    }

    @Test
    void otherUserCannotUpdateOrDelete() throws Exception {
        String id = createTask(alice, "Original");
        mvc.perform(put("/api/tasks/" + id).header(AUTHORIZATION, bearer(bob))
                .contentType(APPLICATION_JSON)
                .content("{\"title\":\"hacked\",\"status\":\"DONE\"}"))
           .andExpect(status().isNotFound());
        mvc.perform(delete("/api/tasks/" + id).header(AUTHORIZATION, bearer(bob)))
           .andExpect(status().isNotFound());
        mvc.perform(get("/api/tasks/" + id).header(AUTHORIZATION, bearer(alice)))
           .andExpect(jsonPath("$.title").value("Original"));
    }

    @Test
    void listReturnsOnlyOwnTasks() throws Exception {
        createTask(alice, "A1");
        createTask(alice, "A2");
        createTask(bob, "B1");
        mvc.perform(get("/api/tasks").header(AUTHORIZATION, bearer(alice)))
           .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void listFiltersByStatus() throws Exception {
        String id = createTask(alice, "Will be done");
        createTask(alice, "Stays todo");
        mvc.perform(patch("/api/tasks/" + id + "/status").header(AUTHORIZATION, bearer(alice))
                .contentType(APPLICATION_JSON).content("{\"status\":\"DONE\"}"))
           .andExpect(status().isOk());
        mvc.perform(get("/api/tasks?status=DONE").header(AUTHORIZATION, bearer(alice)))
           .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void withoutTokenReturns401() throws Exception {
        mvc.perform(get("/api/tasks")).andExpect(status().isUnauthorized());
    }

    @Test
    void tamperedTokenReturns401() throws Exception {
        mvc.perform(get("/api/tasks").header(AUTHORIZATION, bearer(alice) + "x"))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void blankTitleReturns400() throws Exception {
        mvc.perform(post("/api/tasks").header(AUTHORIZATION, bearer(alice))
                .contentType(APPLICATION_JSON).content("{\"title\":\"   \"}"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    void pastDueDateReturns400() throws Exception {
        mvc.perform(post("/api/tasks").header(AUTHORIZATION, bearer(alice))
                .contentType(APPLICATION_JSON)
                .content("{\"title\":\"x\",\"dueDate\":\"2000-01-01\"}"))
           .andExpect(status().isBadRequest());
    }

    @Test
    void invalidStatusReturns400() throws Exception {
        String id = createTask(alice, "x");
        mvc.perform(patch("/api/tasks/" + id + "/status").header(AUTHORIZATION, bearer(alice))
                .contentType(APPLICATION_JSON).content("{\"status\":\"NOPE\"}"))
           .andExpect(status().isBadRequest());
    }

    @Test
    void unknownSortFieldReturns400() throws Exception {
        mvc.perform(get("/api/tasks?sort=passwordHash").header(AUTHORIZATION, bearer(alice)))
           .andExpect(status().isBadRequest());
    }

    @Test
    void invertedDateRangeReturns400() throws Exception {
        mvc.perform(get("/api/tasks?dueDateFrom=2026-12-01&dueDateTo=2026-01-01")
                .header(AUTHORIZATION, bearer(alice)))
           .andExpect(status().isBadRequest());
    }

    @Test
    void deleteTwiceReturns404() throws Exception {
        String id = createTask(alice, "x");
        mvc.perform(delete("/api/tasks/" + id).header(AUTHORIZATION, bearer(alice)))
           .andExpect(status().isNoContent());
        mvc.perform(delete("/api/tasks/" + id).header(AUTHORIZATION, bearer(alice)))
           .andExpect(status().isNotFound());
    }

    private User newUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(encoder.encode("secret123"));
        return users.save(user);
    }

    private String bearer(User user) { return "Bearer " + jwt.generate(user.getId()); }

    private String createTask(User user, String title) throws Exception {
        String body = mvc.perform(post("/api/tasks").header(AUTHORIZATION, bearer(user))
                        .contentType(APPLICATION_JSON).content("{\"title\":\"" + title + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }
}
```

## 7. How to run it

You need Java 21, Maven (or `./mvnw`) and Docker.

1. Start PostgreSQL:

```bash
docker run --name tasks-db \
  -e POSTGRES_DB=tasks -e POSTGRES_USER=tasks -e POSTGRES_PASSWORD=tasks \
  -p 5432:5432 -d postgres:16-alpine
```

2. Set the environment variables:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/tasks
export DB_USER=tasks
export DB_PASSWORD=tasks
export JWT_SECRET=$(openssl rand -base64 32)
```

3. Start the application. Flyway creates the tables on startup:

```bash
./mvnw spring-boot:run
```

4. Create a demo user. The exercise assumes `User` already exists, so there is no registration endpoint. `pgcrypto` produces a BCrypt hash that `BCryptPasswordEncoder` accepts:

```bash
docker exec -i tasks-db psql -U tasks -d tasks <<'SQL'
CREATE EXTENSION IF NOT EXISTS pgcrypto;
INSERT INTO users (id, email, password_hash)
VALUES (gen_random_uuid(), 'demo@example.com', crypt('secret123', gen_salt('bf', 10)));
SQL
```

5. Try it:

```bash
TOKEN=$(curl -s -X POST localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"demo@example.com","password":"secret123"}' | jq -r .accessToken)

curl -X POST localhost:8080/api/tasks \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"title":"First task","dueDate":"2026-12-31"}'

curl "localhost:8080/api/tasks?status=TODO&sort=dueDate,asc" \
  -H "Authorization: Bearer $TOKEN"
```

Interactive docs are at `http://localhost:8080/swagger-ui.html`.

For the tests (integration tests need Docker running):

```bash
./mvnw verify
```

## 8. Still missing for production

- Attempt limits on `/api/auth/login`.
- Refresh tokens and session revocation.
- User registration and email verification.
- Turn off or protect Swagger UI outside development.
- CORS configured for the frontend.
- Actuator, metrics and structured logs.
- CI with tests, static analysis and dependency scanning.
- Dockerfile and deployment configuration.
