# SAFE Banking — Banking System

A full-stack mock banking application built as part of an IBM internship project. It simulates core retail banking operations including multi-currency accounts, peer-to-peer transfers with live FX conversion, card top-ups via Stripe, and an admin panel for oversight.

---

## Architecture

The project is a monorepo with two sub-projects that are packaged together into a single deployable JAR:

| Layer | Technology |
|---|---|
| **Frontend** | React 19, TypeScript, Vite, Tailwind CSS |
| **Backend** | Spring Boot 4 (Java 21), Spring Security, Spring Data JPA |
| **Database** | PostgreSQL 15 |
| **Payments** | Stripe (PaymentIntents + Webhooks) |
| **Auth** | JWT (JJWT 0.12) |
| **Build** | Maven + `frontend-maven-plugin` (pnpm) |

The Maven build compiles the React app via `pnpm build` and copies the output into `src/main/resources/static`, so the Spring Boot server serves both the REST API and the SPA from a single port.

---

## Features

### Authentication
- User registration and login (`POST /api/auth/register`, `POST /api/auth/login`)
- JWT-based stateless authentication with configurable expiry
- Role-based access control: `USER` and `ADMIN` roles

### Bank Accounts
- Create multiple named bank accounts, each with its own IBAN and currency
- View, rename, and look up accounts by IBAN
- Self-service suspend / activate own accounts (`PUT /api/users/accounts/{iban}/suspend|activate`)
- Admin block / unblock of any account (`PUT /api/admin/accounts/{iban}/block|unblock`)
- Account statuses: `ACTIVE`, `SUSPENDED`, `BLOCKED`

### Transfers
- Peer-to-peer transfers between any two IBANs (`POST /api/v1/transactions/transfer`)
- Automatic currency conversion using live exchange rates from [Frankfurter](https://api.frankfurter.app) with a configurable in-memory cache (default 5 min TTL)
- Transfer requires an active E-PIN to authorise

### Transaction History
- Paginated, filterable transaction history per user (`GET /api/v1/transactions`)
- Optional `from` / `to` date-range filters
- Admin view of all transactions system-wide (`GET /api/admin/transactions`)

### Top-Up via Stripe
- Create a Stripe PaymentIntent to top up an account balance (`POST /api/payments/create-intent`)
- Balance is credited automatically on `payment_intent.succeeded` webhook events
- Stripe signature verification on every webhook call; endpoint is unauthenticated by design

### E-PIN Security
- Users set a numeric E-PIN (`POST /api/users/e-pin`) stored as a bcrypt hash
- PIN can be changed (`PUT /api/users/e-pin`) or verified without a transfer (`POST /api/users/e-pin/verify`)
- Rate-limited by IP address to prevent brute-force attacks; configurable attempt window

### Admin Panel
- List all users with pagination (`GET /api/admin/users`)
- List all transactions system-wide (`GET /api/admin/transactions`)
- Block / unblock any bank account
- All admin endpoints require the `ADMIN` role enforced via `@PreAuthorize`

### User Profile
- View and update profile (first name, last name, date of birth)

---

## Project Structure

```
IBM_Internship_2026_Banking/
├── src/                          # React + TypeScript frontend source
│   └── main.tsx
├── index.html
├── vite.config.ts
├── tailwind.config.js
└── safebanking/                  # Spring Boot backend
    ├── pom.xml
    ├── docker-compose.yaml
    └── src/main/java/com/elsys/safebanking/
        ├── config/               # Stripe configuration & webhook verifier
        ├── controller/           # REST controllers (Auth, Account, User, Transaction, Payment, Admin)
        ├── dto/                  # Request / response DTOs
        ├── exception/            # Custom exceptions & global exception handler
        ├── model/                # JPA entities (User, BankAccount, BankingTransaction, ...)
        ├── repository/           # Spring Data JPA repositories
        ├── security/             # JWT filter & Spring Security config
        ├── service/              # Business logic (Transfer, Payment, Account, Auth, ...)
        └── validation/           # E-PIN policy & custom Bean Validation annotations
```

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker & Docker Compose (for the PostgreSQL database)
- A [Stripe](https://stripe.com) account (test mode is fine)
- Node.js 22 / pnpm 11 (only needed for standalone frontend development; the Maven build downloads them automatically)

### 1. Start the database

```bash
cd safebanking
docker compose up -d
```

This starts a PostgreSQL 15 instance on port `5433` with database `safebanking_db`.

### 2. Configure environment variables

Set the following before running the application:

| Variable | Description |
|---|---|
| `DB_USERNAME` | Database username (default in compose: `admin`) |
| `DB_PASSWORD` | Database password (default in compose: `secretpassword`) |
| `APP_JWT_SECRET` | HS256 secret, minimum 32 characters |
| `APP_JWT_EXPIRATION_SECONDS` | Token lifetime in seconds (default: `3600`) |
| `APP_ADMIN_EMAIL` | Email for the seeded admin account |
| `APP_ADMIN_PASSWORD` | Password for the seeded admin account |
| `STRIPE_SECRET_KEY` | Stripe secret key (`sk_test_...`) |
| `STRIPE_WEBHOOK_SECRET` | Stripe webhook signing secret (`whsec_...`) |

Optional FX tuning:

| Variable | Default | Description |
|---|---|---|
| `APP_FX_CACHE_TTL_SECONDS` | `300` | How long exchange rates are cached |
| `APP_FX_CONNECT_TIMEOUT_SECONDS` | `5` | TCP connect timeout to Frankfurter |
| `APP_FX_READ_TIMEOUT_SECONDS` | `5` | HTTP read timeout to Frankfurter |

### 3. Build and run

```bash
# From the safebanking/ directory — builds the React app then packages the JAR
mvn spring-boot:run
```

The application is served at **http://localhost:8080**.

### 4. Stripe webhooks (optional, for top-up)

Forward Stripe events to your local server using the Stripe CLI:

```bash
stripe listen --forward-to localhost:8080/api/payments/webhook
```

Use Stripe's test card `4242 4242 4242 4242` with any future expiry and any CVC.

---

## Running Tests

```bash
cd safebanking
mvn test
```

Tests use an in-memory H2 database configured via `src/test/resources/application-test.properties`. Test coverage includes controllers (MockMvc), services, repositories (DataJpaTest), DTOs, and E-PIN validation.

---

## API Reference (Summary)

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register a new user |
| `POST` | `/api/auth/login` | Public | Log in, receive JWT |
| `GET` | `/api/users/profile` | User | Get own profile |
| `PUT` | `/api/users/profile` | User | Update own profile |
| `GET` | `/api/users/e-pin/status` | User | E-PIN set status |
| `POST` | `/api/users/e-pin` | User | Set E-PIN |
| `PUT` | `/api/users/e-pin` | User | Change E-PIN |
| `POST` | `/api/users/e-pin/verify` | User | Verify E-PIN |
| `POST` | `/api/accounts` | User | Create bank account |
| `GET` | `/api/accounts` | User | List own accounts |
| `GET` | `/api/accounts/{iban}` | User | Get account by IBAN |
| `PUT` | `/api/accounts/{iban}` | User | Rename account |
| `GET` | `/api/accounts/lookup?iban=` | User | Look up recipient |
| `PUT` | `/api/users/accounts/{iban}/suspend` | User | Suspend own account |
| `PUT` | `/api/users/accounts/{iban}/activate` | User | Activate own account |
| `POST` | `/api/v1/transactions/transfer` | User | Transfer funds |
| `GET` | `/api/v1/transactions` | User | Transaction history |
| `POST` | `/api/payments/create-intent` | User | Create Stripe top-up |
| `POST` | `/api/payments/webhook` | Public* | Stripe webhook |
| `GET` | `/api/admin/users` | Admin | All users |
| `GET` | `/api/admin/transactions` | Admin | All transactions |
| `PUT` | `/api/admin/accounts/{iban}/block` | Admin | Block account |
| `PUT` | `/api/admin/accounts/{iban}/unblock` | Admin | Unblock account |

\* Secured by Stripe signature verification, not JWT.

---

## License

See [LICENSE](LICENSE).
