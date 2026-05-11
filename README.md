# Coding Assessment Platform

<div align="center">

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen?style=flat-square&logo=springboot)
![React](https://img.shields.io/badge/React-19-61DAFB?style=flat-square&logo=react)
![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-4.x-38BDF8?style=flat-square&logo=tailwindcss)
![Clerk](https://img.shields.io/badge/Auth-Clerk-6C47FF?style=flat-square)
![Judge0](https://img.shields.io/badge/Judge0-CE-red?style=flat-square)

A full-stack LeetCode-style platform where users can browse problems, write code in a rich editor, execute it against test cases, and track their progress over time.

</div>

---

## Features

- **Authentication** — Secure signup, login, and protected routes via Clerk
- **Problem Library** — 84+ problems across Easy, Medium, and Hard difficulties with tags
- **Rich Code Editor** — Monaco Editor with syntax highlighting for Java, C++, and Python
- **Starter Code** — Pre-filled boilerplate for every problem in all three languages
- **Run & Submit** — Execute against custom input or full test suite via Judge0
- **Error Visibility** — Compile errors, runtime errors, and wrong-answer diffs shown clearly
- **Submission History** — Browse all past submissions with status, language, and timestamp
- **Performance Dashboard** — 30-day activity chart, acceptance rate, difficulty breakdown rings

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 19, Vite, Tailwind CSS 4, Recharts, Monaco Editor |
| Backend | Java 17, Spring Boot 3, Spring Data JPA, Hibernate |
| Database | MySQL 8 |
| Auth | Clerk |
| Code Execution | Judge0 Community Edition |

---

## Project Structure

```
Coding Assessment/
├── frontend/
│   └── src/
│       ├── components/      # Reusable UI (Badge, Loading, Navbar, …)
│       ├── hooks/           # Custom React hooks
│       ├── layouts/         # App shell and route layouts
│       ├── pages/           # Dashboard, Problems, ProblemDetails, Submissions
│       ├── services/        # API client (api.js)
│       └── utils/           # Status helpers, formatters
│
└── backend/
    └── src/main/java/com/assessment/platform/
        ├── config/          # DataSeeder, CORS, security
        ├── controller/      # REST endpoints
        ├── dto/             # Request / response records
        ├── entity/          # JPA entities (User, Problem, Submission, TestCase)
        ├── repository/      # Spring Data repositories
        ├── service/         # Business logic (Execution, Dashboard, …)
        └── util/            # Helpers
```

---

## Local Setup

### Prerequisites

- Java 17+
- Node.js 18+
- MySQL 8 running locally
- A [Clerk](https://clerk.dev) account (free)
- A [Judge0](https://judge0.com) API key or self-hosted CE instance

### 1 — Database

Create the database (Spring Boot will create tables automatically):

```sql
CREATE DATABASE code_assessment;
```

### 2 — Backend

```bash
cd backend
```

Set environment variables (or export them in your shell):

```bash
export DB_URL='jdbc:mysql://localhost:3306/code_assessment?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export DB_USERNAME=root
export DB_PASSWORD=your_mysql_password
export JUDGE0_BASE_URL='https://ce.judge0.com'
```

Start the server:

```bash
mvn spring-boot:run
```

Backend runs on **http://localhost:8080**. On first run, `DataSeeder` seeds all 84 problems and their test cases automatically.

### 3 — Frontend

```bash
cd frontend
cp .env.example .env
```

Set your Clerk publishable key in `frontend/.env`:

```
VITE_CLERK_PUBLISHABLE_KEY=pk_test_...
```

Install dependencies and start the dev server:

```bash
npm install
npm run dev
```

Frontend runs on **http://localhost:5173**.

---

## API Reference

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/problems` | List all problems |
| `GET` | `/problems/{id}` | Get a single problem |
| `POST` | `/problems` | Create a problem (admin) |
| `POST` | `/run` | Run code against custom input |
| `POST` | `/submit` | Submit code against full test suite |
| `GET` | `/submissions?clerkUserId=` | List submissions for a user |
| `GET` | `/submissions/{id}` | Get a single submission |
| `GET` | `/dashboard?clerkUserId=` | Get dashboard analytics |

---

## Build & Test

```bash
# Frontend production build
cd frontend && npm run build

# Backend tests
cd backend && mvn test
```
