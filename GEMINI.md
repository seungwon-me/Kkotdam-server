# 0) Core Philosophy

- Reality-aware (Leaky Abstraction)
    - Use ORM, but own SQL. Know query plans, locks, isolation levels.
    - Simple APIs often hide locks/buffers/threads—understand what’s inside.
- Domain-first (DDD & Rich Domain)
    - Encapsulate state and behavior in the same object. Enforce invariants inside the domain.
    - Anemic is easy to start, expensive to operate.
- Honor bounded contexts (Context Mapping)
    - Models are organizational boundaries. Insert ACLs to stop complexity propagation.
- Observability-first
    - No logging/metrics/tracing = blind debugging. Wire it in by default.
- Keep it simple
    - Use proven patterns sparingly. Avoid over-engineering.

---

# 1) Project Structure

### Principles

- Organize by bounded context (or aggregate), not by layered architecture.
- Inside each context package, include everything: Controller, Service, Repository, Entity, DTO, Mapper, Events, etc.
- No direct cross-context references. Collaborate via Ports/Adapters, Published Language, ACL, or domain events.
- Hide internals with package-private; expose only public contracts (ports, controllers, shared DTOs).

---

### 1.1 Directory/Package Example

```
com.example
 ├─ user                    # Bounded Context: User (fully flat)
 │   ├─ UserController.java
 │   ├─ UserService.java
 │   ├─ UserRepository.java
 │   ├─ UserEntity.java
 │   ├─ UserMapper.java
 │   ├─ UserCommand.java
 │   ├─ UserResponse.java
 │   └─ UserEvents.java (optional)
 ├─ order                   # Bounded Context: Order
 │   ├─ OrderController.java
 │   ├─ OrderService.java
 │   ├─ OrderRepository.java
 │   ├─ OrderEntity.java
 │   └─ ...
 └─ billing                 # Bounded Context: Billing
     ├─ invoice             # Aggregate: Invoice (split only if needed)
     │  ├─ InvoiceController.java
     │  ├─ InvoiceService.java
     │  ├─ InvoiceRepository.java
     │  └─ InvoiceEntity.java
     └─ payment             # Aggregate: Payment
        ├─ PaymentController.java
        ├─ PaymentService.java
        ├─ PaymentRepository.java
        └─ PaymentEntity.java
```

- Small contexts: one package is enough. If it grows, split by aggregates as subpackages within that context only.

---

### 1.2 Dependency/Boundary Rules

- Inside a package: Controller → Service (@Transactional) → Repository → Entity.
- Entities enforce invariants internally (no setters; provide meaningful methods).
- Across packages:
    - Do not reference other contexts’ Entity/Repository/Service directly.
    - Use Port (public interface) + Adapter (package-private implementation).
    - Use ACL at the boundary when external models are complex.
    - For async collaboration, publish domain events; subscribers adapt on their side.

Port/Adapter example

```java
// order package — define a port that the 'user' context will implement
public interface UserPointPort {
    boolean hasEnoughPoint(String userId, int amount);
}

// user package — implement the port (do not expose the implementation outside)
class UserPointAdapter implements UserPointPort {
    private final UserRepository repo;
    UserPointAdapter(UserRepository repo) { this.repo = repo; }

    @Override
    public boolean hasEnoughPoint(String userId, int amount) {
        var user = repo.findById(userId).orElseThrow();
        return user.getPoint() >= amount;
    }
}
```

---

### 1.3 Visibility Strategy

- Default to package-private for classes/methods meant to stay internal to the context.
- Public only for external contracts (controllers, ports, shared DTOs).
- Do not expose Entities outside the package; map to read-only DTOs if needed.

Example

```java
// user/UserEntity.java
@Entity
class UserEntity { // package-private
    @Id @GeneratedValue Long id;
    String name;
    int point;

    void charge(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount > 0");
        point += amount;
    }
}
```

---

### 1.4 Spring Scanning/Setup

- If your application’s main class sits at com.example, Spring will component-scan all subpackages automatically.
- Keep JPA repositories inside their context packages; they’ll still be discovered.
- Transaction boundaries stay in Services only; never annotate Controllers with @Transactional.

---

### 1.5 Mini Sample (fully flat within one package)

```java
// com.example.user
@RestController
class UserController {
    private final UserService service;
    UserController(UserService service) { this.service = service; }

    @PostMapping("/v1/users/{id}/charge")
    ResponseEntity<Void> charge(@PathVariable Long id, @RequestBody ChargeRequest req) {
        service.chargePoint(id, req.amount());
        return ResponseEntity.ok().build();
    }
}

@Service
class UserService {
    private final UserRepository repo;
    UserService(UserRepository repo) { this.repo = repo; }

    @Transactional
    public void chargePoint(Long id, int amount) {
        var u = repo.findById(id).orElseThrow();
        u.charge(amount); // encapsulated domain rule
    }
}

interface UserRepository extends JpaRepository<UserEntity, Long> {}

@Entity
class UserEntity {
    @Id @GeneratedValue Long id;
    String name;
    int point;

    void charge(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount > 0");
        point += amount;
    }
}

record ChargeRequest(int amount) {}
```

---

# 2) Domain Model Rules (Rich Domain by default)

- Entities / Value Objects / Aggregates
    - Enforce invariants via methods; no public setters
    - Value Objects: immutable; equality by value
- Domain Services
    - Only when coordinating across aggregates
- Factories / Static constructors
    - Hide complex creation logic
- Domain Events
    - Express state changes meaningfully; helps with side effects and observability

Example

```java
@Getter
public class Order {
    private final OrderId id;
    private final CustomerId customerId;
    private Money total;
    private OrderStatus status;

    private Order(OrderId id, CustomerId customerId, Money total) {
        this.id = id;
        this.customerId = customerId;
        this.total = total;
        this.status = OrderStatus.CREATED;
        assertInvariants();
    }

    public static Order create(OrderId id, CustomerId customerId, Money total) {
        if (total.isNegative()) throw new DomainException("Total cannot be negative");
        return new Order(id, customerId, total);
    }

    public void pay(Money amount) {
        if (!amount.equals(total)) throw new DomainException("Payment amount mismatch");
        this.status = OrderStatus.PAID;
    }

    private void assertInvariants() {
        if (id == null || customerId == null) throw new DomainException("Missing identifiers");
    }
}
```

---

# 3) Application Layer Rules

- Transaction boundary lives only here (@Transactional)
- Separate I/O via DTO/Command/Query; do not expose domain objects
- Idempotency: safely handle retries with the same key

```java
@Service
@RequiredArgsConstructor
public class PayOrderUseCase {
    private final OrderRepository orderRepository;

    @Transactional
    public PayOrderResult handle(PayOrderCommand cmd) {
        Order order = orderRepository.getById(cmd.orderId());
        order.pay(cmd.amount());
        return PayOrderResult.from(order);
    }
}
```

---

# 4) Web/Interface Rules

- Keep controllers thin: validate → call use case → map response
- Global exception handling: unify error model with @ControllerAdvice
- Interceptors: propagate Correlation/Trace Id, inject auth context

Error response example

```json
{
  "code": "ORDER_NOT_FOUND",
  "message": "Order not found",
  "traceId": "c1a2b3..."
}
```

---

# 5) ORM/JPA Rules (for Leaky Abstraction)

- Default to LAZY; avoid EAGER
- equals/hashCode only when identifier is stable; never use @Data on entities
- Associations
    - Prefer unidirectional; only go bidirectional if truly needed
    - Cascade only within the aggregate boundary
- Query performance
    - Prevent N+1 via fetch join, @EntityGraph, batch-size
    - Complex reads: dedicated read repository + DTO projections
- Locking/Isolation
    - Prefer optimistic lock (@Version) + retry
    - Use pessimistic locks narrowly and briefly
- Transactions
    - Use readOnly = true for queries
    - Only annotate at service methods

Dev/local settings (not for prod)

```properties
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.default_batch_fetch_size=100
# In prod use logback + slow query alerts/sampling
```

Read repository example

```java
public interface OrderReadRepository {
    Optional<OrderSummaryDto> findSummaryById(OrderId id);
}
```

---

# 6) Transactions & Concurrency

- Boundary: application service
- Isolation: start with READ COMMITTED; adjust explicitly when needed
- Lock strategy:
    - Try optimistic + retry first
    - Pessimistic (PESSIMISTIC_WRITE) only when necessary and short-lived
- Anti-patterns: relying on System.out.println sync or method-level synchronized as a “business lock”

---

# 7) Context Mapping Guide

- Open Host Service
    - Upstream exposes standardized capabilities; downstream relies on the contract
- Conformist
    - Downstream adopts upstream’s model as-is; fast but brittle to upstream changes
- ACL (Anti-Corruption Layer)
    - Convert external models to internal; blocks complexity propagation
- Shared Kernel
    - Share a minimal core schema; acknowledge joint ownership cost
- Partnership
    - Co-develop when synchronized changes are inevitable
- Published Language
    - Formalize contracts (JSON/XML/OpenAPI/protocols) with versioning
- Separate Ways
    - No interaction? Split it. Don’t create dependencies by habit
- Big Ball of Mud
    - Blurred boundaries are the worst. Lint module/package deps to guard

ACL package sketch

```
interface.partnerX
 ├─ in  # PartnerX → us: webhook/controller
 ├─ out # us → PartnerX: client
 └─ acl # converters, adapters, mappers (external DTO ↔ internal model)
```

---

# 8) API Contract / Versioning / Compatibility

- Contract-first with OpenAPI
- Versioning: URI (/v1); breaking changes = new version
- Idempotency keys: guard against duplicate requests (Idempotency-Key header)
- Response consistency: include code/message/traceId

---

# 9) Logging / Observability / Debugging

- Logging
    - SLF4J; prefer structured JSON logs
    - Mask PII; never log passwords/tokens
- Tracing
    - Always propagate traceId (HTTP header → MDC)
- Metrics
    - Count and latency for key use cases
- Debugging
    - In dev only: SQL logging; p6spy/datasource-proxy optional
    - In prod: sampling + slow query alerts

---

# 10) Configuration / Environments

- Profiles: local/dev/stage/prod
- Secrets: env/secret manager; never commit
- DB migrations: Flyway mandatory, no manual SQL in prod
- Health: separate liveness/readiness

---

# 11) Testing Strategy

- Pyramid: Unit (domain) > Integration (repo/web-slice) > E2E
- Domain tests: pure Java, fast
- Repositories: Testcontainers with real DB
- Query tests: assert row counts/plans to guard N+1

---

# 12) Security / Validation

- Input validation: Bean Validation + Controller advice
- AuthZ: method-level security (@PreAuthorize), least privilege
- Auditing: auto inject createdBy/updatedBy

---

# 13) Code Style / Tooling
- Framework: Spring Boot 3.5.4
- Language: Java 24. Use records for response/read DTOs
- Lombok: @Getter, @RequiredArgsConstructor, @Builder (for VOs)
    - Avoid: @Data, careless @EqualsAndHashCode(callSuper = true)
- Naming: use domain terms; avoid cryptic abbreviations
- Enforce with Spotless/Checkstyle

Quick table

| Topic | Recommended | Avoid | Note |
|---|---|---|---|
| Entity loading | LAZY | EAGER | Fetch when needed |
| Transactions | Service | Controller | @Transactional boundary |
| Queries | DTO projection | Random entity graphs | Purpose-built repos |
| Logs | Structured JSON | System.out | traceId everywhere |

---

# 14) PR Checklist

- [ ] Are domain invariants enforced inside the objects?
- [ ] Is the transaction boundary only in application services?
- [ ] Are N+1 guards in place (fetch join/EntityGraph/batch)?
- [ ] Are external dependencies isolated behind an ACL?
- [ ] Is OpenAPI and the error model up to date?
- [ ] Are logs/metrics/traces present on critical flows?
- [ ] Are Flyway migrations included?
- [ ] Do tests expose cardinality/slow-query risks?

---

# 15) Example Snippets

Controller

```java
@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final PayOrderUseCase useCase;

    @PostMapping("/{id}/pay")
    public ResponseEntity<PayOrderResponse> pay(
            @PathVariable String id,
            @Valid @RequestBody PayOrderRequest req,
            @RequestHeader(value = "Idempotency-Key", required = false) String key) {

        PayOrderResult result = useCase.handle(new PayOrderCommand(id, req.amount()));
        return ResponseEntity.ok(PayOrderResponse.from(result));
    }
}
```

ACL adapter

```java
@Component
@RequiredArgsConstructor
class PartnerXPaymentAcl {
    private final PartnerXClient client;

    public PaymentResult pay(Payment payment) {
        PartnerXPayRequest req = PartnerXMapper.toRequest(payment);
        PartnerXPayResponse resp = client.pay(req);
        return PartnerXMapper.toDomain(resp);
    }
}
```

Optimistic lock

```java
@Entity
class Stock {
    @Id Long id;
    @Version Long version;
    int quantity;

    public void decrease(int n) {
        if (n < 0) throw new DomainException("Negative decrement not allowed");
        if (quantity < n) throw new DomainException("Insufficient stock");
        quantity -= n;
    }
}
```

---

# 16) Do-not List

| Topic | Don’t | Why | Alternative |
|---|---|---|---|
| Entity setters | setXxx | Breaks invariants | Meaningful methods |
| Leaking entities | Return Entity | Layer leakage | DTO/View model |
| Auto bidirectional | “Because easy” | Cycles/load traps | Unidirectional |
| @Transactional in controller | “Convenient” | Blurs boundaries | Service only |

---

# 17) Ops Tips

- Define SLOs; watch error rate and p95 latency
- Health checks should cover dependency chains; separate ready/liveness
- Runbooks: e.g., “Lock contention↑ → review isolation/index/plan”

---

# 18) Why these rules (bridging your notes)

- Leaky Abstraction: Abstractions (ORM, println, etc.) leak. Operability requires internal knowledge.
- Context Mapping: Boundaries stop complexity spread; ACL when in doubt.
- Rich Domain: Encapsulation keeps cohesion and reduces change cost. Anemic trades short-term ease for long-term pain.

---