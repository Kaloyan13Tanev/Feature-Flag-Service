# Feature Flag Service

A REST API for managing feature flags, built with Spring Boot, Spring Data JPA, and H2.

## Prerequisites

- Java 25
- Maven

## Build and Run

```bash

mvn spring-boot:run
```

The application starts on `http://localhost:8081`.

## API Usage

### Create a flag

```bash
curl -L -X POST 'http://localhost:8081/flags' \
-H 'Content-Type: application/json' \
-d '{
    "name": "new-checkout-flow",
    "description": "Test flag",
    "enabled": true
}'
```

### List all flags

```bash
curl -L -X GET 'http://localhost:8081/flags'
```

### Get a single flag

```bash
curl -L -X GET 'http://localhost:8081/flags/1'
```

### Update a flag (partial update)

```bash
curl -L -X PATCH 'http://localhost:8081/flags/1' \
-H 'Content-Type: application/json' \
-d '{
    "enabled": false
}'
```

### Delete a flag

```bash
curl -L -X DELETE 'http://localhost:8081/flags/1'
```

### Evaluate a flag

```bash
curl -L -X GET 'http://localhost:8081/flags/new-checkout-flow/evaluate'
```

Returns: `{"name": "new-checkout-flow", "enabled": true}`

## Key Design Decisions and Trade-Offs

**Spring Boot** was chosen for its mature ecosystem, auto-configuration, and seamless integration with Spring Data JPA and embedded databases. It is the industry standard for building Java REST APIs.

**H2 with file-based storage** provides persistence across restarts without requiring external database setup. The file is stored at `./data/featureflags.mv.db`. The storage mechanism can be swapped by changing the datasource configuration in `application.properties` and, if needed, the JPA dialect, since the repository layer is abstracted behind Spring Data JPA's `JpaRepository` interface.

**Layered architecture (Controller → Service → Repository)** keeps responsibilities clearly separated. The controller handles HTTP concerns, the service contains business logic, and the repository manages data access. This is what the task means by "sensibly separated responsibilities."

**`FeatureFlagPatchDTO` with `Boolean` wrapper type** enables proper PATCH semantics. A primitive `boolean` defaults to `false` when not sent, making it impossible to distinguish between "client didn't send this field" and "client explicitly set it to false." The wrapper type `Boolean` remains `null` when not sent, so the service only updates fields that were explicitly included in the request. A known trade-off is that this approach cannot distinguish between "field not sent" and "field explicitly set to null," but this is acceptable for this use case since the only nullable field is `description`, and clearing it can be done by sending an empty string.

**`FlagEvaluationResponse` record** provides a dedicated response type for the evaluate endpoint, ensuring the response matches the required format (`{"name": "...", "enabled": true/false}`) rather than returning the full entity.

**`ResponseStatusException`** is used for error handling to keep error responses simple and avoid the overhead of a global exception handler for a small service.

**`JpaRepository` over `CrudRepository`** provides additional convenience methods such as `findAll()` returning a `List` instead of `Iterable`, and pagination support if needed later.

**Lombok** reduces boilerplate for getters, setters, and constructors in the entity class, keeping the code concise.

## Testing Strategy

Tests focus on two areas:

**Service layer (unit tests):** The service contains all business logic, so it is the most important layer to test. Tests cover all CRUD operations, duplicate name rejection, not-found scenarios, flag evaluation, and proper PATCH behavior (verifying that null fields in the DTO do not overwrite existing values). The repository is mocked to isolate the service logic.

**Controller layer (MockMvc tests):** These tests verify that the HTTP layer behaves correctly — that endpoints return the right status codes (201 Created, 204 No Content, 200 OK), that JSON serialization/deserialization works, and that the controller delegates to the service properly. `@WebMvcTest` is used to load only the web layer.

The repository layer is not tested directly because it consists entirely of Spring Data JPA auto-generated methods, which are already tested by the framework itself.

## What I Would Improve With More Time

- **Global exception handler (`@ControllerAdvice`)** for consistent, structured error responses across all endpoints instead of relying on `ResponseStatusException`.
- **Pagination** for the `GET /flags` endpoint to handle large numbers of flags efficiently.
- **Integration tests** using a real embedded H2 database to verify end-to-end behavior including actual persistence.
- **Swagger/OpenAPI documentation** to provide interactive API documentation.
- **Docker support** with a `Dockerfile` for containerized deployment.
- **Input validation** with `@Valid` on more endpoints and custom validation annotations where appropriate.
- **Reflection-based PATCH handling** to dynamically iterate over DTO fields and apply non-null values to the entity, eliminating manual if-null checks and making the update logic resilient to new fields being added.
- **Audience targeting with percentage rollouts** — associating flags with user groups and a rollout percentage (e.g. enable for 10% of users), allowing gradual feature rollouts and impact measurement before full release.
- **Dedicated DTOs and converters for all operations** (e.g. `FlagCreateDTO`, `FlagResponseDTO`) to fully decouple the entity from the API contract, preventing clients from setting internal fields like `id` or timestamps, and allowing the API shape to evolve independently of the data model.
