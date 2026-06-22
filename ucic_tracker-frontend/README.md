# UCIC Exception Tracker

A full-stack fintech compliance tool that automates **UCIC (Unique Customer Identification Code) duplicate detection and vendor resolution workflows** — built based on a real compliance problem managed in production at an NBFC.

---

## Problem Statement

In financial institutions, the same customer can exist multiple times in the system with mismatched PANs, Customer IDs, or KYC documents (Aadhaar, Voter ID, DL, Passport). These duplicates cause failures in credit decisioning, RBI compliance reporting, and loan processing.

The traditional approach — running nested SQL loops and tracking exceptions via Excel sheets and email chains — is slow, error-prone, and has no audit trail.

**UCIC Exception Tracker solves this by:**
- Automatically detecting duplicate customer clusters using a Union-Find algorithm
- Providing a dashboard to track exception status end-to-end
- Giving vendors a secure, tokenized portal to submit resolutions directly
- Maintaining a complete audit trail for every action taken

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.2 |
| Database | MySQL 8.0, PL/SQL-style stored logic |
| Frontend | React 18, Vite, React Router |
| API | REST APIs with JSON |
| Auth | Token-based vendor access (UUID tokens) |

---

## Architecture

```
React Frontend (Vite)
       ↓
Spring Boot REST APIs
       ↓
MySQL Database (5 tables)
```

**Core algorithm:** Union-Find (Disjoint Set) implemented in Java — groups customers by shared PAN, Customer ID, Voter ID, or Aadhaar. Processes 50,000 records and identifies ~3,400 duplicate groups.

---

## Database Schema

| Table | Purpose |
|---|---|
| `customers` | 50,000 synthetic customer records with realistic duplicate patterns |
| `exception_groups` | Detected duplicate clusters with status tracking |
| `exception_group_members` | Maps customers to their exception group |
| `audit_log` | Every action logged with actor and timestamp |
| `analysis_runs` | History of each Union-Find analysis execution |

---

## Key Features

**1. Union-Find Duplicate Detection**
- Detects 5 duplicate patterns: same PAN, same Customer ID, same Voter ID, same Aadhaar, multiple KYC issues
- Processes 50,000 records in ~15 seconds locally
- Identifies ~3,400 duplicate groups per run

**2. Exception Lifecycle Management**
- Full status workflow: `PENDING → SENT_TO_VENDOR → RESOLVED / REJECTED`
- Every status change logged in the audit trail

**3. Tokenized Vendor Portal**
- Each exception group gets a unique, expiring token (7 days)
- Vendors open a secure link — no login required
- Vendors submit root cause and resolution note directly
- System auto-updates status on submission

**4. Dashboard**
- Live counts: total customers, total groups, pending, sent, resolved, rejected
- Last analysis run stats (groups found, duration)

---

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/analysis/run` | Trigger Union-Find analysis |
| GET | `/api/dashboard` | Get summary statistics |
| GET | `/api/exceptions` | List all exception groups |
| GET | `/api/exceptions?status=PENDING` | Filter by status |
| GET | `/api/exceptions/{id}` | Get group with members |
| PUT | `/api/exceptions/{id}/send-vendor` | Generate vendor token |
| PUT | `/api/exceptions/{id}/resolve` | Manually resolve |
| PUT | `/api/exceptions/{id}/reject` | Reject group |
| GET | `/api/exceptions/{id}/audit` | Get audit trail |
| GET | `/api/vendor/review?token=xxx` | Vendor views group |
| POST | `/api/vendor/resolve?token=xxx` | Vendor submits resolution |

---

## Setup & Running Locally

### Prerequisites
- Java 17+
- Maven 3.9+
- MySQL 8.0
- Node.js 20+

### Backend Setup

```bash
# 1. Create database
mysql -u root -p
CREATE DATABASE ucic_tracker;
exit

# 2. Run the schema
mysql -u root -p ucic_tracker < schema.sql

# 3. Update application.properties
spring.datasource.password=your_password_here

# 4. Start the backend
mvn spring-boot:run
# Backend runs on http://localhost:8080
```

### Seed Data

```bash
# On first run, the DataSeeder automatically populates 50,000 records
# Watch the logs for: "Seeding complete! Total: 50000"
```

### Run Analysis

```bash
# Trigger Union-Find via Postman or curl
curl -X POST http://localhost:8080/api/analysis/run
```

### Frontend Setup

```bash
cd ucic_tracker-frontend
npm install
npm run dev
# Frontend runs on http://localhost:5173
```

---

## Project Background

This project is inspired by a real UCIC exception resolution workflow I owned at TVS Credit Services — where duplicate customer records across 20M+ records were causing failures in RBI compliance reporting and credit decisioning.

The Union-Find algorithm used here is the same algorithmic approach I applied in production to reduce customer grouping time from 30 minutes to under 10 seconds.

The vendor portal replaces a manual email-based process where Excel sheets were sent back and forth — adding structure, traceability, and accountability to the resolution workflow.

---

## Folder Structure

```
ucic-tracker/               ← Spring Boot backend
├── src/main/java/com/fintrack/ucic_tracker/
│   ├── controller/         ← REST API controllers
│   ├── service/            ← Business logic + Union-Find
│   ├── entity/             ← JPA entities
│   ├── repository/         ← Spring Data repositories
│   ├── dto/                ← Request/response objects
│   ├── enums/              ← Status and root cause enums
│   └── util/               ← DataSeeder
├── src/main/resources/
│   └── application.properties
└── schema.sql

ucic_tracker-frontend/      ← React frontend
├── src/
│   ├── api/                ← Axios client + service functions
│   ├── pages/              ← Dashboard, List, Detail, Vendor
│   └── index.css
└── index.html
```

---

## Author

**Kishore R** — Software Developer at TVS Credit Services
- Backend: Java, Spring Boot, PL/SQL
