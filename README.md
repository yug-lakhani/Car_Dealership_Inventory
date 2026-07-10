# Car Dealership Inventory System

A full-stack inventory management system for a car dealership, built as a
Test-Driven Development kata.

- **Backend:** Java 17, Spring Boot 3, Spring Security (JWT), Spring Data JPA,
  PostgreSQL, Flyway
- **Frontend:** React 18, Vite, React Router, Tailwind CSS, Axios
- **Testing:** JUnit 5 + Mockito (backend), Vitest + React Testing Library
  (frontend)

## Project Structure

```
Car_Dealership_Inventory/
├── Server/                      # Spring Boot REST API
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/dealership/inventory/
│       │   │   ├── config/      # Security, CORS, bean configuration
│       │   │   ├── controller/  # REST controllers (auth, vehicles, inventory)
│       │   │   ├── dto/
│       │   │   │   ├── request/   # Request payload DTOs
│       │   │   │   └── response/  # Response payload DTOs
│       │   │   ├── entity/      # JPA entities (User, Vehicle)
│       │   │   ├── repository/  # Spring Data JPA repositories
│       │   │   ├── service/     # Service interfaces
│       │   │   │   └── impl/    # Service implementations
│       │   │   ├── security/    # JWT filter/provider
│       │   │   └── exception/   # Custom exceptions + global handler
│       │   └── resources/
│       │       ├── application.yml
│       │       ├── application-dev.yml
│       │       └── db/migration/  # Flyway SQL migrations
│       └── test/
│           ├── java/com/dealership/inventory/
│           │   ├── controller/    # @WebMvcTest slice tests
│           │   ├── service/       # Unit tests (Mockito)
│           │   ├── repository/    # @DataJpaTest slice tests
│           │   └── integration/   # Full Spring Boot context tests
│           └── resources/application-test.yml
│
└── Client/                      # React SPA
    ├── package.json
    ├── vite.config.js
    ├── tailwind.config.js
    ├── index.html
    └── src/
        ├── api/            # Axios client + endpoint wrappers
        ├── components/
        │   ├── layout/     # Navbar, ProtectedRoute
        │   ├── vehicles/   # VehicleCard, VehicleList, VehicleForm, ...
        │   └── common/     # Reusable UI primitives
        ├── pages/          # Login, Register, Dashboard, Admin, NotFound
        ├── context/        # AuthContext (JWT session state)
        ├── hooks/          # useVehicles, etc.
        ├── utils/          # Constants, validators
        └── tests/          # Vitest setup + component tests
```

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+
- Node.js 18+
- PostgreSQL 14+ (running locally or in Docker)

### Backend (Server)

1. Create a PostgreSQL database (a local instance, or a managed one such as
   Aiven/Supabase/RDS):
   ```sql
   CREATE DATABASE dealership_inventory;
   ```
2. Copy `Server/src/main/resources/application-dev.yml.example` to
   `application-dev.yml` in that same folder, and fill in your real
   connection string, username, and password:
   ```
   cd Server/src/main/resources
   cp application-dev.yml.example application-dev.yml
   ```
   `application-dev.yml` is listed in `.gitignore` and will **never** be
   committed, so it's safe to put real credentials there. Alternatively,
   set `DB_USERNAME` / `DB_PASSWORD` as environment variables and leave the
   example file's placeholders as-is.
3. Set a JWT secret (env var, or add it under `app.jwt.secret` in your
   `application-dev.yml`):
   ```
   JWT_SECRET=replace-with-a-long-random-secret
   ```
4. Run the API:
   ```
   cd Server
   ./mvnw spring-boot:run
   ```
   The API will start on `http://localhost:8080`.
5. Run the backend test suite:
   ```
   ./mvnw test
   ```

### Frontend (Client)

1. Install dependencies:
   ```
   cd Client
   npm install
   ```
2. Copy `.env.example` to `.env` and adjust if needed:
   ```
   VITE_API_BASE_URL=http://localhost:8080/api
   ```
3. Start the dev server:
   ```
   npm run dev
   ```
   The app will be available at `http://localhost:5173`.
4. Run the frontend test suite:
   ```
   npm test
   ```

## API Overview

| Method | Endpoint                          | Description                          | Auth        |
|--------|------------------------------------|---------------------------------------|-------------|
| POST   | `/api/auth/register`               | Register a new user                   | Public      |
| POST   | `/api/auth/login`                  | Log in and receive a JWT               | Public      |
| GET    | `/api/vehicles`                    | List all available vehicles           | User        |
| GET    | `/api/vehicles/search`             | Search by make/model/category/price   | User        |
| POST   | `/api/vehicles`                    | Add a new vehicle                     | User        |
| PUT    | `/api/vehicles/{id}`               | Update a vehicle                      | User        |
| DELETE | `/api/vehicles/{id}`               | Delete a vehicle                      | Admin       |
| POST   | `/api/vehicles/{id}/purchase`      | Purchase a vehicle (decrement stock)  | User        |
| POST   | `/api/vehicles/{id}/restock`       | Restock a vehicle (increment stock)   | Admin       |

## Test Report

_To be filled in once the test suites are implemented, e.g. output of
`mvnw test` and `npm test` / coverage summaries._

## My AI Usage

_This section documents which AI tools were used throughout development, how
they were used, and a reflection on their impact on the workflow. To be
completed as development progresses._

- **Tools used:**
- **How they were used:**
- **Reflection:**

## Screenshots

_To be added once the UI is implemented._
