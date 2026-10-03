# BlueMart — Final Project Report

**Project:** BlueMart — Multi-Seller E-Commerce Marketplace
**Course:** Anna University R2025, Semester 3
**Technology Stack:** Java Servlets, JDBC, Apache Tomcat 9
**Live Deployment:** https://bluemart.onrender.com
**Repository:** https://github.com/hemapragathis-afk/Bluemart

---

## 1. Overview

BlueMart is a multi-seller e-commerce web application where sellers list products, buyers browse and purchase through a cart-and-checkout flow, and an administrator oversees users, orders, and listings. The project was built end-to-end using layered Java Servlets, raw JDBC with HikariCP connection pooling, and an embedded H2 database, deployed as a Docker container.

## 2. Architecture

The application follows a layered MVC architecture with a Front Controller pattern:

```
Browser (HTML / vanilla JS, fetch API)
        |
Servlet layer — HTTP request handling only, no business logic
        |
Service layer — business rules, validation
        |
DAO layer — all SQL via PreparedStatement
        |
HikariCP connection pool (initialized by ServletContextListener at startup)
        |
H2 Database
```

Each feature (auth, products, cart, orders, reviews, admin, chat) follows the same vertical slice through all four layers, with a dedicated servlet, service, and DAO. Shared concerns — JDBC connection management, LocalDateTime JSON serialization, session-based authentication — are centralized in `DataSourceListener`, `GsonUtil`, and `AuthFilter` respectively, rather than duplicated per feature.

See [diagrams.md](diagrams.md) for the full ER diagram (D1), use case diagram (D2), and sequence diagram for the place-order flow (D3).

## 3. Feature Summary

| ID | Feature | Status |
|---|---|---|
| F1 | Registration & login (Buyer/Seller, Admin seeded) | ✅ Complete |
| F2 | Seller product create/edit/delete | ✅ Complete |
| F3 | Browse & search/filter products | ✅ Complete |
| F4 | Cart (add/update/remove, running total) | ✅ Complete |
| F5 | Checkout (mock payment confirmation) | ✅ Complete |
| F6 | Order history (buyer & seller views) | ✅ Complete |
| F7 | Admin panel (users, orders, moderation) | ✅ Complete |
| F8 | Product reviews & ratings (on delivered orders) | ✅ Complete |
| O2 | Order status workflow | ✅ Complete |
| O4 | AI chatbot | ✅ Complete |
| O1 | Wishlist | Not implemented |
| O3 | Seller sales dashboard (analytics) | Not implemented |

## 4. Key Technical Decisions

**Layered architecture over a monolithic servlet style.** Each servlet is deliberately thin — it reads the request, delegates to a service, and writes the response. All business rules (role checks, validation, stock calculations) live in the service layer, and all SQL lives in the DAO layer. This mirrors standard enterprise Java practice and made debugging far easier, since a bug could always be isolated to one layer.

**PreparedStatement everywhere, no exceptions.** Every single SQL query in the codebase — across all 8 DAOs — uses parameterized queries. This was a non-negotiable requirement from the start and was never compromised, even under time pressure.

**Shared `GsonUtil` for date serialization.** Gson's default reflection-based approach failed to serialize `java.time.LocalDateTime` fields under Java 17's module system (`InaccessibleObjectException`). Rather than patch this per-servlet, a single `GsonUtil` class with a custom `LocalDateTime` adapter was created once and reused everywhere `Gson` is needed.

**Mock payment and mock chatbot provider, both via explicit interfaces.** Both the checkout flow and the chatbot deliberately avoid real external dependencies (a payment gateway, a paid LLM API) per the spec's scope constraints. The chatbot specifically uses a `ChatProvider` interface with a `MockChatProvider` implementation, so a real provider (e.g. an LLM API) could be swapped in later without touching the servlet or service code that calls it — this was a deliberate Strategy-pattern choice, not a shortcut.

**Seeding a demo seller account before `seed.sql` runs.** `seed.sql` hardcodes `seller_id = 1` for its sample products. On a completely fresh database (as encountered during cloud deployment), no user with `id = 1` exists yet, so the product inserts would silently fail on the foreign key constraint. This was caught during the live deployment process and fixed by having `DataSourceListener` programmatically create a demo seller account with a real bcrypt-hashed password *before* `seed.sql` executes, guaranteeing the foreign key is satisfiable regardless of environment.

**Docker-based deployment over a bare VM.** The spec allows any platform meeting its requirements. A multi-stage Dockerfile (Maven build stage → Tomcat runtime stage) was chosen over manually provisioning and configuring a Linux VM, since it eliminates an entire category of environment-specific configuration drift between local development and the deployed environment — the same Dockerfile that runs locally is exactly what runs in production.

## 5. Security Measures Implemented

- All SQL via `PreparedStatement`; verified with a manual audit of every DAO, no string-concatenated queries anywhere in the codebase
- Passwords hashed with bcrypt (jBCrypt); never logged, never returned in any API response (a `passwordHash` leak in the admin users endpoint was caught and fixed during development by introducing a dedicated `UserSummary` DTO)
- Session ID regenerated on every login; 30-minute inactivity timeout
- Role-based access control enforced server-side in every relevant servlet (e.g. only `SELLER` role can create/edit/delete products; only `ADMIN` role can access `/api/v1/admin/*`) — never relying on hiding UI elements alone
- Admin accounts cannot be self-registered through the public registration endpoint; the only admin account in the system is programmatically seeded at startup
- Ownership checks on mutating operations (a seller can only update/delete their own products; can only update the status of orders containing their own products)

## 6. Known Limitations

- **No real payment gateway.** Checkout uses a mock confirmation step, per the project's explicit scope constraints.
- **Live deployment's database is not persistent across restarts.** The free-tier hosting environment used for the live demo does not provide a persistent disk; the embedded H2 database is rebuilt from `schema.sql` and `seed.sql` on every container restart. Data created during a live session (new registrations, orders, reviews) does not survive a restart. This is a hosting-tier constraint, not an application defect — the schema/seed/demo-account bootstrapping was specifically designed to make every fresh start land in a consistent, working state.
- **AI chatbot uses a mock provider.** `MockChatProvider` returns keyword-matched canned answers rather than calling a live LLM API, to avoid API costs and key management for the class deployment. The `ChatProvider` interface is structured so a real provider can be substituted with no changes to the servlet or service layer.
- **Test coverage is representative, not exhaustive.** A DAO-layer test (against an embedded H2 instance) and a service-layer test suite exist and pass, but do not cover every class. Given the project timeline, testing effort was prioritized toward the most business-critical paths (product persistence, stock decrement, order total calculation, status validation) rather than attempting full coverage.
- **Optional features O1 (wishlist) and O3 (seller sales dashboard) were not implemented**, in favor of completing all mandatory F1–F8 features, the mandatory O4 chatbot, and the O2 order status workflow needed to properly support F8 reviews.

## 7. Design Patterns Used

| Pattern | Where |
|---|---|
| DAO | Every data-access class (`ProductDAO`, `OrderDAO`, etc.) separates persistence from business logic |
| Front Controller | Each servlet acts as the single entry point for its resource, dispatching to services |
| Singleton | The HikariCP connection pool, initialized once in `DataSourceListener` |
| Strategy | `ChatProvider` interface, allowing `MockChatProvider` to be swapped for a real implementation |
| DTO | `AdminService.UserSummary` separates the API-facing user representation from the internal `User` entity, deliberately excluding `passwordHash` |

## 8. Conclusion

BlueMart implements the full mandatory feature set (F1–F8) plus the order status workflow (O2) and AI chatbot (O4), is deployed live on the public internet, follows the required layered architecture and security practices throughout, and includes a passing automated test suite. The project was built from a complete beginner's starting point — no prior experience with Java, Maven, Tomcat, or Git — across a single extended development effort, debugging real production issues (database initialization, serialization errors, foreign key constraints on fresh deployments) along the way.