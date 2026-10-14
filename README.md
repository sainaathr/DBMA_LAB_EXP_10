# Diagnostic Center Management System

> **DBMS Lab PAT 4** — Java Spring Boot + JDBC + MySQL  
> A complete, production-ready diagnostic center management SaaS built for academic evaluation.

---

## ✨ Features

| Module | Capabilities |
|---|---|
| **Authentication** | BCrypt passwords, session auth, CSRF protection, logout |
| **Dashboard** | Live stats, Chart.js booking trends & revenue charts from MySQL |
| **Patients** | Full CRUD, search, pagination, detail view |
| **Doctors** | Full CRUD, specialization filter, center assignment |
| **Centers** | Full CRUD, doctor/booking counts, card layout |
| **Medical Tests** | Full CRUD, price & duration validation |
| **Bookings** | Multi-test selection, status management, server-side totals |
| **Billing** | Bill generation, payment recording, printable receipt |
| **Reports** | Date-filtered Chart.js charts + revenue breakdown table |

---

## 🛠 Technology Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.2, Spring MVC |
| **Database Access** | **JDBC (JdbcTemplate) — mandatory academic requirement** |
| Database | MySQL 8.0 |
| Security | Spring Security, BCrypt, CSRF, session |
| Frontend | Thymeleaf, Bootstrap 5, Bootstrap Icons, Chart.js |
| Build | Maven (Maven Wrapper included) |
| Container | Docker, Docker Compose |
| CI | GitHub Actions |

---

## 🚀 Quick Start (Windows)

### Prerequisites
- JDK 17+ (`java -version`)
- MySQL 8.0 (`mysql --version`)
- Git

### 1. Set up MySQL

```sql
-- Run in MySQL shell as root
CREATE DATABASE IF NOT EXISTS diagnostic_center CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS 'dcadmin'@'localhost' IDENTIFIED BY 'dcpassword';
GRANT ALL PRIVILEGES ON diagnostic_center.* TO 'dcadmin'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Import the schema and seed data

```powershell
mysql -u dcadmin -pdcpassword diagnostic_center < database\schema.sql
mysql -u dcadmin -pdcpassword diagnostic_center < database\seed.sql
```

### 3. Set environment variables

```powershell
$env:DB_URL      = "jdbc:mysql://localhost:3306/diagnostic_center?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
$env:DB_USERNAME = "dcadmin"
$env:DB_PASSWORD = "dcpassword"
```

### 4. Build and run

```powershell
.\mvnw spring-boot:run
```

### 5. Open in browser

```
http://localhost:8080
```

### Demo login credentials
| Field | Value |
|---|---|
| Username | `admin` |
| Password | `Admin@1234` |

### Stop the application
Press `Ctrl+C` in the terminal.

---

## 🐳 Docker Compose (Alternative)

```powershell
docker compose up --build
```

This starts MySQL and the application together. Database is initialised automatically.

---

## 🏗 Project Structure

```
src/
├── main/
│   ├── java/com/diagnosticcenter/app/
│   │   ├── DiagnosticCenterApplication.java   # Entry point
│   │   ├── config/SecurityConfig.java         # Spring Security
│   │   ├── controller/                        # MVC controllers
│   │   ├── dao/                               # JDBC repositories
│   │   ├── model/                             # Domain models
│   │   ├── service/DiagnosticService.java     # Business logic
│   │   └── exception/GlobalExceptionHandler.java
│   └── resources/
│       ├── application.properties             # Config (env-var based)
│       ├── templates/                         # Thymeleaf HTML
│       └── static/
│           ├── css/style.css                  # Design system
│           └── js/app.js
database/
├── schema.sql    # Table definitions + FK constraints
└── seed.sql      # Demo data + BCrypt admin account
Dockerfile
compose.yaml
```

---

## ☁️ Cloud Deployment (Railway)

### Step 1 — Create a Railway account
Visit [railway.app](https://railway.app) and sign in with GitHub.

### Step 2 — Create a MySQL service
1. New Project → Add a service → **MySQL**
2. Copy the `DATABASE_URL` from the Variables tab.

### Step 3 — Deploy the app
1. New Project → Deploy from GitHub repo → select this repo.
2. Railway will detect Dockerfile automatically.
3. Add environment variables in Settings → Variables:

```
DB_URL       = jdbc:mysql://<HOST>:<PORT>/railway?useSSL=true&serverTimezone=UTC
DB_USERNAME  = <MySQL username from Railway>
DB_PASSWORD  = <MySQL password from Railway>
PORT         = ${{PORT}}   (Railway sets this automatically)
```

### Step 4 — Import database schema
Use the Railway MySQL shell or a MySQL client connected to the hosted instance, then run `schema.sql` and `seed.sql`.

### Step 5 — Verify
Visit the generated `.up.railway.app` URL and log in with `admin` / `Admin@1234`.

> **Note:** Railway's free Hobby plan includes 500 hours/month. The MySQL service persists data between deployments.

---

## 🔒 Security Notes

- Passwords stored as BCrypt hashes (cost 10).
- All SQL uses parameterised queries (JdbcTemplate).
- CSRF tokens required on all form submissions.
- Raw stack traces never shown to users.
- Secrets controlled via environment variables only.

---

## 📚 Academic Documentation

See [`PAT4_DEMO_GUIDE.md`](PAT4_DEMO_GUIDE.md) for viva questions, architecture explanation, and the demonstration guide.

---

## 📄 License

MIT — free for academic and personal use.
