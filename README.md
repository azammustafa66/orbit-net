# OrbitNet

A LinkedIn-style professional social network, built as a set of Spring Boot microservices.

Users register, publish posts, like them, and build a connection graph; notifications fan out asynchronously over Kafka. All traffic enters through an API gateway that validates JWTs — every other service sits behind it on a private network.

- **Repository** — https://github.com/azammustafa66/orbit-net
- **Live demo** — [TODO]
- **API documentation (Swagger / OpenAPI)** — [TODO]
- **Postman collection** — [TODO]

---

## Architecture

![OrbitNet architecture](docs/images/architecture.png)

<sub>Source: <code>docs/images/architecture.svg</code> — edit the SVG and re-export the PNG.</sub>

**Request path.** Client traffic reaches the **API Gateway** — the only component with a public IP. It validates the JWT once, resolves the caller's identity, and forwards it downstream as an `X-User-Id` header. Everything behind the gateway lives in a private subnet and is addressable only by private IP.

**Service discovery.** A **Eureka** registry provides discovery and client-side load balancing, so services address each other by name (`lb://POSTS-SERVICE`) rather than by IP. Configuration lives in each service's own `application.yaml`; there is no config server.

**Data ownership.** Every service owns its database and no service reads another's tables. Cross-service reads go through APIs or events.

**Asynchronous messaging.** user-service and posts-service emit domain events to **Kafka**; connection-service and notification-service consume them. A signup never blocks on graph writes, and a like never blocks on notification delivery.

**Observability.** Zipkin tracing and ELK logging are planned, not yet wired up.

### Services

| Service | Port | Store | Role |
| --- | --- | --- | --- |
| `api-gateway` | 8083 | — | Public entry point; JWT validation, routing |
| `discovery-service` | 8084 | — | Eureka registry |
| `user-service` | 8081 | PostgreSQL `user-service` | Accounts, signup, login, JWT issuance |
| `posts-service` | 8080 | PostgreSQL `posts-service` | Posts, feed pagination, likes |
| `connection-service` | 8082 | Neo4j | Connection graph, degree traversal |
| `notification-service` | 8085 | PostgreSQL `notification-service` | Consumes events, serves notifications |

---

## Authentication

user-service signs a JWT at login. The gateway verifies it and translates it into a plain header for the private network:

```
client ──JWT──▶ api-gateway ──X-User-Id──▶ service
```

Downstream services trust `X-User-Id` at face value, which is safe only because the gateway is the sole route into the subnet. To keep that true, `AuthFilter` **always strips** inbound `X-User-Id` and `X-User-Email` headers and re-adds them only from verified claims — a client that sends its own `X-User-Id` has it discarded, not forwarded.

Two consequences worth knowing:

- The gateway does **not** reject unauthenticated requests, because `/api/v1/users/auth/**` must stay anonymous. Each service decides what it requires, so a protected endpoint called without a token currently fails as a `400` (missing header) rather than a `401`.
- `jwt.secret` must be **identical** in api-gateway and user-service — one signs, the other verifies.

---

## Events

| Topic | Partitions | Producer | Consumer | Effect |
| --- | --- | --- | --- | --- |
| `user_created_topic` | 3 | user-service | connection-service | Creates the user's `Person` node in the graph |
| `post_created_topic` | 3 | posts-service | notification-service | Notifies each first-degree connection |
| `post_liked_topic` | 3 | posts-service | notification-service | Notifies the post's author |

**Payload contract.** Producers tag each event with a logical name (`userCreated`, `postCreated`, `postLiked`) via `spring.json.type.mapping`, and consumers map that name onto their own copy of the payload. The two sides share no package and no jar, so an event class can be renamed on one side without breaking the other.

**Delivery semantics.** Kafka is at-least-once, so consumers must tolerate redelivery. `user_created_topic` is handled with a Neo4j `MERGE` on `userId`, making repeated delivery a no-op rather than a duplicate node.

**Ordering with the transaction.** user-service publishes from an `AFTER_COMMIT` transaction listener, so an event is never emitted for a signup that later rolls back.

Both consumers wrap their deserializers in `ErrorHandlingDeserializer`, so an unreadable record goes to the error handler instead of becoming a poison pill that stalls the partition.

---

## Why a graph database for connections

![Connection degree query costs](docs/images/graph-db-query.png)

Degrees of connection ("you and X share a connection") are the query that decides the storage engine.

**Relational shape.** At 1M users averaging 1,000 connections each, a `connections` table holds **1B+ rows** (~12 GB). First-degree lookups are a single indexed scan, but each additional degree requires another self-join against a billion-row table:

```sql
select from connection where from_user is in (
  join select from connection where from_user is in (
    join (select from connection where from_user is user1)))
```

Third-degree traversal approaches **O(N³)** — impractical at this size, and the cost grows with the *depth* of the question rather than the size of the answer.

**Graph shape.** Neo4j stores the same information as a `from → to` adjacency mapping (~1 GB) and traverses it natively:

```
user1 -> user2
user2 -> user4
user1 -> user4
```

First- and second-degree lookups become pointer traversals from a starting node, proportional to the neighbourhood actually visited rather than to the size of the table.

**Decision:** connection-service uses Neo4j; the other services stay on PostgreSQL, where relational integrity and transactions matter more than traversal depth.

---

## Tech stack

Java 21 · Spring Boot 4.1 · Spring Cloud 2025.1.2 · Spring Cloud Gateway (MVC) · Netflix Eureka · Spring Web MVC · Spring Data JPA (Hibernate 7) · Spring Data Neo4j · Spring for Apache Kafka · PostgreSQL 16 · Neo4j 5 · Flyway · Bean Validation · JJWT · Lombok · ModelMapper · Testcontainers · Maven

**Planned:** Zipkin · ELK · Docker images · Kubernetes manifests · CI/CD

---

## Getting started

### Prerequisites

- JDK 21
- Docker
- Maven

### 1. Start the infrastructure

```bash
# PostgreSQL
docker run -d --name postgres_db \
  -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin \
  -p 5432:5432 postgres:16

# Kafka (KRaft, no ZooKeeper)
docker run -d --name kafka_broker -p 9092:9092 apache/kafka:3.9.0

# Neo4j
docker run -d --name neo4j_db \
  -e NEO4J_AUTH=neo4j/<your-password> \
  -p 7474:7474 -p 7687:7687 neo4j:5-community
```

### 2. Create the PostgreSQL databases

Each service owns one; Flyway creates the tables on first boot.

```bash
for db in user-service posts-service notification-service; do
  docker exec postgres_db psql -U admin -d postgres -c "create database \"$db\""
done
```

### 3. Set the environment

```bash
export JWT_SECRET='<base64-encoded HMAC secret>'   # same value for gateway and user-service
export NEO4J_PASSWORD='<your-password>'            # connection-service
```

`JWT_SECRET` is required by api-gateway — it has no fallback. Keep real secrets out of version control.

### 4. Run the services

Start `discovery-service` **first** so the others can register; the rest can start in any order.

```bash
cd discovery-service && mvn spring-boot:run   # 8084 — wait for this one
cd api-gateway        && mvn spring-boot:run  # 8083
cd user-service       && mvn spring-boot:run  # 8081
cd posts-service      && mvn spring-boot:run  # 8080
cd connection-service && mvn spring-boot:run  # 8082
cd notification-service && mvn spring-boot:run # 8085
```

Registration takes a few seconds to appear — Eureka's response cache refreshes on a ~30s interval, so a service can be up and serving before it shows in `/eureka/apps`.

> **Note:** the root `pom.xml` currently aggregates only `posts-service` and `user-service`, so a build from the repository root skips the other four. Build them from their own directories until the remaining modules are added to the reactor.

---

## API

All examples go through the gateway on `http://localhost:8083`. Every endpoint except signup and login needs `Authorization: Bearer <token>`.

### Auth — user-service

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/v1/users/auth/signup` | Register; emits `user_created_topic` |
| `POST` | `/api/v1/users/auth/login` | Exchange credentials for a JWT |

```bash
curl -X POST localhost:8083/api/v1/users/auth/signup \
  -H 'Content-Type: application/json' \
  -d '{"email":"ada@orbitnet.dev","password":"Passw0rd123!","fullName":"Ada Lovelace"}'
```

```json
{ "id": 1, "name": "Ada Lovelace", "email": "ada@orbitnet.dev", "accountCreated": true }
```

Passwords are BCrypt-hashed and bounded to 72 bytes (BCrypt's real ceiling — accented characters and emoji count as more than one). Login returns the same message and takes the same time whether the email is unknown or the password is wrong, so the endpoint does not leak which accounts exist.

### Posts — posts-service

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/v1/posts` | Create a post; fans out to first-degree connections |
| `GET` | `/api/v1/posts/{postId}` | Fetch a single post |
| `GET` | `/api/v1/posts/users/{userId}?page=1&size=10` | Page through a user's posts, newest first |
| `POST` | `/api/v1/posts/likes/{postId}` | Like a post |
| `DELETE` | `/api/v1/posts/likes/{postId}` | Remove a like |

```bash
curl -X POST localhost:8083/api/v1/posts \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"content":"first post"}'
```

Creating a post asks connection-service for the author's first-degree connections, then emits one `post_created` event per recipient.

Pages are **1-indexed**; `size` accepts 1–50.

```json
{
  "content": [ { "postId": 1, "userId": 7, "content": "…" } ],
  "page": 1, "size": 10, "totalElements": 1, "totalPages": 1, "hasNext": false
}
```

### Connections — connection-service

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/v1/connections/first-degree` | The caller's direct connections |

Identity comes from the `X-User-Id` header, so the endpoint always serves the caller's own graph rather than an arbitrary user's.

> Connection **requests** (send / accept / reject) are in progress and not yet callable.

### Notifications — notification-service

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/v1/notifications?page=1&size=10` | The caller's notifications, newest first |
| `GET` | `/api/v1/notifications/unread-count` | `{"unread": 3}` |
| `PATCH` | `/api/v1/notifications/{id}/read` | Mark one as read |

Marking a notification read is scoped to the owner — another user's notification returns `404`, not `403`, so the endpoint does not confirm that the id exists.

### Errors

Failures return a consistent envelope, with `fieldErrors` populated for validation failures:

```json
{
  "error": "Validation failed",
  "fieldErrors": { "content": "Post content must not be blank" },
  "statusCode": "400 BAD_REQUEST",
  "timestamp": "2026-08-15T14:48:11.442976"
}
```

| Status | Raised when |
| --- | --- |
| `400` | Validation failure, out-of-range paging params, duplicate like, unlike without a like, missing identity header |
| `401` | Bad credentials, or a protected action attempted without a resolved identity |
| `404` | Resource does not exist, or belongs to another user |
| `500` | Unexpected — logged server-side, generic message returned |

---

## Schema management

Flyway owns the schema in every PostgreSQL service; Hibernate runs with `ddl-auto: validate` and only checks that the entities match what the migrations built. Migrations live in `src/main/resources/db/migration`.

connection-service has no migrations — Neo4j is schema-less here, and nodes are created by the `user_created_topic` consumer.

---

## Project layout

```
orbit-net/
├── docs/images/            architecture and design diagrams
├── api-gateway/            public entry point, JWT validation   (8083)
├── discovery-service/      Eureka registry                      (8084)
├── user-service/           accounts, auth, JWT issuance         (8081)
├── posts-service/          posts, likes, feed pagination        (8080)
├── connection-service/     connection graph on Neo4j            (8082)
└── notification-service/   event consumer, notifications        (8085)
```

---

## Roadmap

- [x] Posts service — create, fetch, paginate, like
- [x] Consistent error handling and request validation
- [x] User service — registration, login, JWT issuance
- [x] API gateway — routing and JWT validation
- [x] Eureka service registry
- [x] Connections service on Neo4j
- [x] Kafka events and notification service
- [x] Flyway migrations
- [ ] Connection requests — send, accept, reject
- [ ] Uploader service with external object storage
- [ ] Zipkin tracing and ELK logging
- [ ] Dockerfiles, Kubernetes manifests, CI/CD pipeline

---

## Contributing

Contribution guidelines — [TODO]

## License

License — [TODO]

## Contact

Maintainer contact — [TODO]
