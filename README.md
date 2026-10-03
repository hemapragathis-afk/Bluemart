# BlueMart

A multi-seller e-commerce marketplace built with Java Servlets, JDBC, and Apache Tomcat for Anna University R2025 Semester 3 Capstone Project.

**Live demo:** https://bluemart.onrender.com

> Note: the live demo runs on Render's free tier with an embedded H2 database and no persistent disk. The database rebuilds itself automatically from `schema.sql` + `seed.sql` on every restart, so any data added during a session (new registrations, orders, reviews) resets after the service spins down from inactivity. This is a documented limitation of the free-tier deployment, not a bug.

## Demo Accounts

| Role | Email | Password |
|---|---|---|
| Admin | admin@bluemart.com | admin123 |
| Seller | seller1@bluemart.com | seller123 |
| Buyer | *(register a new one)* | — |

## Problem Statement

Sellers list products. Buyers browse, search, add to cart, and purchase. An admin manages users, orders, and listings. Checkout uses a mock payment confirmation (no real payment gateway). No real-time infrastructure (WebSockets, live tracking, mapping APIs).

## Tech Stack

| Component | Technology |
|---|---|
| Language | Java 17 |
| Servlet Container | Apache Tomcat 9.0.x |
| Build Tool | Maven |
| Database | H2 (embedded, file-based) |
| Connection Pooling | HikariCP |
| View Layer | Plain HTML + vanilla JavaScript (fetch API) |
| JSON | Gson |
| Password Hashing | jBCrypt |
| Deployment | Docker, Render.com |

## Architecture

Layered MVC over Servlets (Front Controller pattern):

```
Browser (HTML/CSS/vanilla JS + fetch)
        |
Servlet layer (Controllers — thin, no SQL, no business logic)
        |
Service layer (business rules, validation)
        |
DAO layer (all SQL, PreparedStatement only)
        |
HikariCP connection pool (via ServletContextListener)
        |
H2 Database
```

See [docs/diagrams.md](docs/diagrams.md) for the ER diagram, use case diagram, and sequence diagram (place-order flow).

## Features Implemented

- **F1** — Registration and login (Buyer/Seller roles; Admin seeded automatically)
- **F2** — Seller product listing create/edit/delete
- **F3** — Browse and search/filter products
- **F4** — Cart: add, update, remove, running total
- **F5** — Checkout via mock payment confirmation
- **F6** — Order history (buyer and seller views)
- **F7** — Admin panel: view all users/orders, moderate listings
- **F8** — Product reviews and star ratings (only on DELIVERED orders)
- **O2** — Order status workflow (PENDING → CONFIRMED → SHIPPED → DELIVERED)
- **O4** — AI chatbot (interface-based provider architecture, mock FAQ responses, rate-limited)

## Security

- All SQL via `PreparedStatement` — no string-concatenated queries
- Passwords hashed with bcrypt (jBCrypt), never logged or returned in API responses
- Session regenerated on login; 30-minute timeout
- Role-based access control enforced server-side (buyer/seller/admin boundaries checked in every servlet, not just hidden in the UI)
- Admin account cannot be self-registered — only ever created via server-side seeding

## Local Setup

**Prerequisites:** JDK 17, Maven, Apache Tomcat 9.0.x

1. Clone the repository:
```
   git clone https://github.com/hemapragathis-afk/Bluemart.git
   cd Bluemart
```
2. Build the WAR:
```
   mvn clean package
```
3. Copy `target/bluemart.war` into Tomcat's `webapps/` folder.
4. Start Tomcat. On first boot, the app automatically creates the schema, seed data, a demo seller, and an admin account.
5. Visit `http://localhost:8080/bluemart/login.html`

## Docker / Cloud Deployment

A multi-stage `Dockerfile` is included — the first stage builds the WAR from source with Maven, the second stage runs it on Tomcat. This is how the live demo on Render is deployed; any Docker-compatible host works the same way.

```
docker build -t bluemart .
docker run -p 8080:8080 bluemart
```

## Known Limitations

- No real payment gateway (mock confirmation only, per spec)
- Live demo's database is not persistent across restarts (free-tier hosting constraint)
- AI chatbot uses a mock provider with keyword-matched FAQ answers rather than a live LLM API, to keep the deployed demo free of API costs/keys — the `ChatProvider` interface is ready for a real provider to be swapped in
- No automated test suite yet (in progress)

## Project Structure

```
src/main/java/com/bluemart/bluemart/
├── controller/   Servlets (HTTP layer)
├── service/      Business logic
├── dao/          Data access (JDBC, PreparedStatement only)
├── model/        POJOs
├── exception/    Custom exceptions
├── listener/     DataSource init + startup seeding
└── util/         Shared helpers (Gson config)

src/main/webapp/      HTML pages + chat widget
src/main/resources/db/  schema.sql, seed.sql
docs/                 Diagrams
```