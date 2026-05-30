# FairShare

A backend REST API for splitting expenses among groups of people — with a graph-based debt simplification engine that minimizes the number of transactions needed to settle debts.

---

## What it does

FairShare lets users create groups, log shared expenses with flexible split modes, and calculates the simplest way for everyone to settle up. Instead of a web of payments between friends, the algorithm collapses it into the minimum number of transfers needed.

**Example:** 5 friends on a trip generate 12 IOUs between each other. FairShare reduces that to 4 payments total.

---

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3 |
| Database | PostgreSQL 16 |
| Migrations | Flyway |
| Cache | Redis |
| Messaging | Apache Kafka |
| Auth | JWT (jjwt 0.12) |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Build | Maven |
| Containers | Docker + Docker Compose |

---

## Architecture

```
Client
  └── REST API (Spring Boot)
        ├── Auth layer (JWT filter)
        ├── Domain layer (User, Group, Expense, Settlement)
        ├── Algorithm layer (DebtSimplifier — graph-based)
        ├── Cache layer (Redis — group balances)
        └── Event layer (Kafka — expense.created, settlement.created)
              └── Notification Service (Kafka consumer)
```

---

## API endpoints

### Auth
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/auth/register` | Register a new user | Public |
| POST | `/api/auth/login` | Login and receive JWT token | Public |

### Groups
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/groups` | Create a group | Required |
| GET | `/api/groups` | Get all groups for current user | Required |
| GET | `/api/groups/{id}` | Get a single group | Required |

### Expenses
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/groups/{id}/expenses` | Add an expense to a group | Required |
| GET | `/api/groups/{id}/expenses` | List all expenses in a group | Required |

### Settlements (Not yet implemented)
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/groups/{id}/balances` | Get current balances for a group | Required |
| GET | `/api/groups/{id}/simplify` | Get simplified debt transactions | Required |
| POST | `/api/groups/{id}/settle` | Record a payment between two users | Required |

---

## The debt simplification algorithm

The core feature of FairShare. Given a group where members owe each other arbitrary amounts, the algorithm:

1. Calculates each member's **net balance** (total paid minus total owed)
2. Builds a directed graph of debts
3. Uses a **greedy matching** of the largest creditor and largest debtor to produce the minimum number of transactions

---

## Running locally

### Prerequisites
- Java 21
- Docker Desktop

### Steps

**1. Clone the repo**
```bash
git clone https://github.com/brenluz/fairshare-backend
cd FairShare
```

**2. Start infrastructure**
```bash
docker compose up -d
```
This starts PostgreSQL, Redis, Kafka, and Zookeeper.

**3. Run the app**
```bash
./mvnw spring-boot:run
```

**4. Open Swagger UI**
```
http://localhost:8080/swagger-ui.html
```

Register a user, click **Authorize**, paste the JWT token, and explore the API.

---

## Project structure

```
src/main/java/com/brenluz/fairshare/
├── algorithm/         # Debt simplification engine
├── api/               # REST controllers + DTOs
│   └── dto/
│       ├── request/
│       └── response/
├── config/            # Security, Redis, Kafka, OpenAPI config
├── domain/            # JPA entities, repositories, services
│   ├── user/
│   ├── group/
│   ├── expense/
│   └── settlement/
├── events/            # Kafka event publishers
└── security/          # JWT filter, UserDetails implementation
```

---

## Planned features

- [ ] Receipt scanning with AI item recognition
- [ ] Per-item price editing with audit trail (created/edited by)
- [ ] Guest access via shareable group link (no account required)
- [ ] Angular frontend
- [ ] Push notifications via Kafka consumer service

---
