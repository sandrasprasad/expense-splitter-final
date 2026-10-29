# Expense Splitter

A full-stack expense sharing application similar to Splitwise, built with **Spring Boot**, **Oracle Database** and **React**.

## Features
- **User management:** JWT authentication, login and signup.
- **Group management:** create groups, add and remove members.
- **Expense tracking:**
  - **EQUAL** and **EXACT** split types.
  - **BigDecimal** arithmetic for financial accuracy.
- **Settlements:**
  - **Min-Cash Flow algorithm** minimizes the number of payments needed to settle a group (see below).
  - Partial and full settlement support.
- **Audit trail:** history of all transactions per group.
- **Dark theme UI:** responsive React dashboard with real-time balances.

## How debt simplification works
Every member has a net balance: positive if they are owed money, negative if they owe money.
`MinCashFlowSolver` keeps creditors and debtors in two max-heaps, repeatedly pairs the largest debtor with the
largest creditor and settles `min(|debt|, credit)`. Each payment clears at least one member, so a group of `n`
members needs at most `n - 1` payments, computed in `O(n log n)`. Amounts under 0.01 are treated as settled.

## Tech stack
- **Backend:** Java 17, Spring Boot 3, Spring Security (JWT), Spring Data JPA / Hibernate.
- **Database:** Oracle Database.
- **Frontend:** React, Axios, Lucide Icons.
- **Testing:** JUnit 5, Mockito.

## API overview
| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/users/register`, `/api/users/login` | Sign up and obtain a JWT |
| POST | `/api/groups` | Create a group |
| GET | `/api/groups/my-groups` | Groups of the logged-in user |
| POST | `/api/groups/{id}/add-member` | Add a member |
| GET | `/api/groups/{id}/balances` | Net balances and suggested settlements |
| POST | `/api/expenses` | Add an expense (EQUAL or EXACT split) |
| POST | `/api/settlements` | Record a settlement |
| GET | `/api/audit/group/{id}` | Audit trail for a group |

## Setup

### Backend
1. `cd expense-splitter-backend`
2. Copy `.env.example` values into your environment (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`).
   Credentials and the JWT signing key are read from environment variables and are never committed.
3. `mvn spring-boot:run` starts the API on port `8082`.

### Frontend
1. `cd expense-splitter-frontend`
2. `npm install`
3. `npm start` opens the app at `http://localhost:3000`.

## Testing
- **Backend:** `cd expense-splitter-backend && mvn test` runs the JUnit/Mockito tests, including the Min-Cash Flow solver.
  `ExpenseSplitterApplicationTests` loads the full context and needs a reachable Oracle database.
- **Frontend:** `cd expense-splitter-frontend && npm test`.
