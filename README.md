# Priora — Intelligent Task Prioritisation System

> **A self-prioritizing task board powered by real-time mathematical scoring.**  
> Stop guessing what's urgent. Let the Gravity Engine decide.

---

## What Is Priora?

Traditional project management tools like Jira rely on manually assigned priority labels — *Low, Medium, High, Critical*. These labels decay over time, create priority inflation, and never reflect real delivery risk.

**Priora replaces static priority labels with a live mathematical score** called the **Gravity Score**. Every task is automatically evaluated in real-time against six urgency factors — deadlines, downstream dependencies, developer workload, stagnation, environment, and blocker status — and sorted into one of three visual zones on a dynamic canvas.

Engineers always see what to work on next. No guessing. No outdated priorities.

---

## How It Works

### The Gravity Score Formula

Every task receives a **Gravity Score** computed dynamically on each API call:

$$\text{Total Score} = \Big(\text{Blast Radius} + \text{Deadline Urgency} + \text{Stagnation} + \text{Blocker Penalty}\Big) \times \text{Workload Multiplier} \times \text{Environment Multiplier}$$

#### Score Components

| Factor | Formula | Max Points |
|---|---|---|
| **Blast Radius** | $15 \times \text{direct dependents} + 5 \times \log_2(1 + \text{indirect dependents})$ | Unbounded |
| **Deadline Urgency** (Approaching) | $\frac{100}{1 + e^{0.1 \times (T_{remaining} - 24)}}$ — sigmoid curve | 100 pts |
| **Deadline Urgency** (Overdue) | $100 + \min(30, \text{hours overdue})$ | 130 pts |
| **Stagnation** | $\min(15, \frac{\text{days inactive}}{7} \times 5)$ | 15 pts |
| **Blocker Penalty** | Flat +25 pts if status is `BLOCKED` | 25 pts |
| **Workload Multiplier** | $1.0 + 0.05 \times \max(0, \text{active tasks} - 3)$ | Multiplier |
| **Environment Multiplier** | PRODUCTION = 1.5×, STAGING = 1.2×, DEVELOPMENT = 1.0× | Multiplier |

### The 3-Zone Visual Canvas

Based on the computed score, tasks are automatically placed into one of three zones:

| Zone | Condition | Meaning |
|---|---|---|
| 🔴 **Focus** | Score ≥ 80 **or** status is `BLOCKED` | Work on this immediately |
| 🟡 **Drifting** | Score 40–79.9 | Monitor and progress steadily |
| 🟢 **Orbit** | Score < 40 **or** status is `IN_REVIEW` / `DONE` | Backlog, complete, or low urgency |

Tasks move between zones automatically as deadlines approach, dependencies grow, or workloads shift — **with no manual re-labelling needed.**

---

## Features

### Core Engine
- ✅ **Real-time Gravity Score calculation** — Recomputed on every API response with live data
- ✅ **Two-phase deadline urgency** — Smooth sigmoid approach curve + capped overdue escalation
- ✅ **Logarithmic blast radius** — Exponential cascade impact scoring for dependency chains
- ✅ **Stagnation aging** — Forgotten tickets automatically rise in priority over time
- ✅ **Customizable formula** — Team Leads can adjust factor weights and inject custom risk dimensions (e.g., Security Risk Weight)

### Task Lifecycle
- ✅ **Full status workflow** — `TODO` → `IN_PROGRESS` → `IN_REVIEW` → `DONE` / `BLOCKED`
- ✅ **Task dependencies** — Define parent/child task chains with optional sequential execution ordering
- ✅ **Approval workflow** — Configurable policy requiring lead sign-off on new task submissions
- ✅ **Reassignment & deadline extension** — Managers can shift assignees and extend deadlines from the canvas
- ✅ **Developer pings** — Team leads can send urgent notification pings on stalled high-gravity tasks

### Authentication & Access Control
- ✅ **Role-based access control (RBAC)** — Four distinct roles with granular permission enforcement
- ✅ **Self-service registration** — Users create their own accounts from the `/register` page
- ✅ **OWASP-compliant validation** — Production-grade double-validation on usernames, passwords, and employee IDs
- ✅ **Spring Security HTTP Basic** — Session-backed authentication with BCrypt password encoding

### Role Capabilities

| Action | Project Manager | Team Lead | Developer | Tester |
|---|---|---|---|---|
| Create tasks | ✅ | ✅ | ❌ | ❌ |
| Approve / reject tasks | ✅ | ✅ | ❌ | ❌ |
| Ping assignee | ✅ | ✅ | ❌ | ❌ |
| Reassign / extend deadline | ✅ | ✅ | ❌ | ❌ |
| Update task status | ✅ | ✅ | ✅ | ✅ |
| Customize formula settings | ✅ | ✅ | ❌ | ❌ |
| Provision new users | ✅ | ❌ | ❌ | ❌ |
| View canvas & tasks | ✅ | ✅ | ✅ | ✅ |

---

## Technology Stack

| Layer | Technology |
|---|---|
| **Backend** | Spring Boot 3.2.3 (Java 17) |
| **Security** | Spring Security 6 — HTTP Basic auth, BCrypt, RBAC |
| **ORM / JPA** | Spring Data JPA + Hibernate (`ddl-auto=update`) |
| **Database** | PostgreSQL (HikariCP connection pool) |
| **Frontend** | Angular 16 — Standalone Components + Signals architecture |
| **Styling** | Tailwind CSS |
| **Build** | Maven (Backend) + Angular CLI / npm (Frontend) |

---

## Project Structure

```
gravity-canvas/
├── backend/
│   ├── src/main/java/com/gravitycanvas/
│   │   ├── config/              # SecurityConfig, CORS (no DataInitializer — zero pre-seeding)
│   │   ├── controller/          # REST API: /api/tasks, /api/users, /api/settings, /api/auth
│   │   ├── dto/                 # Request/Response data transfer objects
│   │   ├── entity/              # JPA entities: Task, UserAccount, GravitySettings
│   │   ├── repository/          # Spring Data repositories
│   │   └── service/
│   │       ├── GravityEngineService.java       # Core score formula & zone classification
│   │       ├── GravityCalculatorService.java   # API response DTO builder with all score metrics
│   │       ├── TaskService.java                # Task lifecycle & approval workflow
│   │       ├── UserService.java                # User provisioning with OWASP validation
│   │       ├── SettingsService.java            # Formula customization management
│   │       └── UserValidationService.java      # OWASP production-grade input validation
│   └── src/main/resources/
│       └── application.properties             # PostgreSQL, HikariCP, Hibernate config
│
└── frontend/src/app/
    ├── components/
    │   ├── canvas/                            # 3-Zone task board with live score sorting
    │   ├── task-card/                         # Individual task card with zone & score display
    │   ├── create-task-modal/                 # Task creation form with dependency selector
    │   ├── reassign-modal/                    # Reassignment workflow
    │   ├── edit-deadline-modal/               # Deadline extension form
    │   ├── header/                            # Navigation & user context
    │   └── login-modal/                       # Inline login overlay
    ├── pages/
    │   ├── login/                             # /login — Sign in page
    │   ├── register/                          # /register — Self-service sign up page
    │   ├── approvals/                         # /approvals — Manager approval queue
    │   ├── settings/                          # /settings — Formula weight customization
    │   ├── user-management/                   # /users — Project Manager user provisioning
    │   └── profile/                           # /profile — User details & change password
    └── services/
        ├── auth.service.ts                    # Login, role context, credentials
        ├── task.service.ts                    # Task API + Angular Signals state
        ├── user.service.ts                    # User & registration API
        └── settings.service.ts               # Formula settings API
```

---

## Quickstart

### Prerequisites
- Java 17+
- Maven 3.8+
- Node.js 18+ and npm
- PostgreSQL running locally

### 1. Database
Create the PostgreSQL database:
```sql
CREATE DATABASE priora;
```

### 2. Backend
```bash
cd backend
mvn clean spring-boot:run
```
> Backend starts on **`http://localhost:8081`**  
> Hibernate automatically creates all tables (`users`, `tasks`, `gravity_settings`, `task_dependencies`) on first run — no SQL scripts needed.

### 3. Frontend
```bash
cd frontend
npm install
npm start
```
> Frontend starts on **`http://localhost:4200`**

---

## Creating Your First Account

Because there is **no pre-seeding or default users**, create accounts directly from the UI:

1. Open **`http://localhost:4200/register`**
2. Fill in the form — password must meet OWASP requirements (8+ chars, uppercase, lowercase, number, special character e.g. `Password123!`)
3. Submit — you'll be redirected to the login page automatically

> **Start with a Project Manager account** so you can access all features including user provisioning, settings, and task approvals.

---

## Key API Reference

| Method | Endpoint | Description | Roles |
|---|---|---|---|
| `POST` | `/api/users/register` | Public self-service account registration | Public |
| `GET` | `/api/auth/me` | Get current authenticated user profile | All |
| `GET` | `/api/tasks` | Get all tasks with live gravity scores | All |
| `POST` | `/api/tasks` | Create a new task | Manager, Team Lead |
| `PUT` | `/api/tasks/{id}/status` | Update task status | All |
| `POST` | `/api/tasks/{id}/ping` | Send urgent ping to assignee | Manager, Team Lead |
| `PUT` | `/api/tasks/{id}/approve` | Approve pending task | Manager, Team Lead |
| `PUT` | `/api/tasks/{id}/reject` | Reject with reason | Manager, Team Lead |
| `PUT` | `/api/tasks/{id}/reassign` | Reassign to different developer | Manager, Team Lead |
| `PUT` | `/api/tasks/{id}/deadline` | Extend task deadline | Manager, Team Lead |
| `GET` | `/api/settings` | Retrieve formula configuration | All |
| `PUT` | `/api/settings` | Update formula weights & custom factor | Manager, Team Lead |
| `GET` | `/api/users` | List all user accounts | All |
| `POST` | `/api/users` | Provision account (admin only) | Project Manager |
| `PUT` | `/api/users/change-password` | Change own password | All |

---

## OWASP Password Rules

Passwords must meet all of the following:
- Minimum **8 characters**, maximum **128 characters**
- At least **1 uppercase** letter (A–Z)
- At least **1 lowercase** letter (a–z)
- At least **1 number** (0–9)
- At least **1 special character** (e.g. `!`, `@`, `#`, `$`, `%`, `^`, `&`, `*`)

**Example valid password:** `Password123!`