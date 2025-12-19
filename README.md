# 🎓 Bursary & Scholarship Matching Platform

## Overview

This application is a two-sided platform designed to connect learners with bursary providers. It helps learners discover bursaries they qualify for and manage applications, while enabling bursary providers to find eligible students, post bursaries and manage funding pipelines.


---
# 🧩 Problem Statement

### Traditional bursary systems suffer from:

-   Limited visibility into student performance before applications

-   Manual, paper-based verification of academic results

-   High fraud risk and misuse of funds

-   Reactive funding models that miss emerging talent

-   Low student motivation due to lack of feedback loops

---
# 💡 Solution Overview

This platform introduces a Performance Passport–driven bursary ecosystem, connecting learners, bursary providers, banks, NGOs, and governments in one intelligent system.

At its core:

-   Students build a verified academic profile

-   Providers proactively discover and monitor promising learners

-   Performance, motivation, and opportunity reinforce each other

---

# 🔑 Core Features

#### 1️⃣ Student Academic Performance Tracker
-   Students upload quarterly and annual report cards to build a lifelong academic profile.
#### 2️⃣ Bursary Provider Talent Search Engine
-   Bursary providers can discover learners using highly targeted criteria, even before applications open.
#### 3️⃣ “On Our Radar” Notification System
-   Creates a real-time feedback loop between effort and opportunity.

---

# 🧱 Technical Architecture
#### Backend
-   Spring Boot

-   JWT-based authentication

-   PostgreSQL (Neon)

-   RESTful API architecture

-   Dockerized for cloud deployment

-   Hosted on Render
#### Frontend
-   React + Vite build system
-   Tailwindcss
-   Deployed on Vercel

---
## 🏗️ System Design Architecture

### 🔍 High-Level Overview

The platform follows a **modern, cloud-ready, decoupled architecture** with a React frontend, Spring Boot backend, AI processing services, and a centralized PostgreSQL database.

```
┌────────────────────────────┐
│        Web / Mobile        │
│      (React Frontend)      │
│        Vercel Hosting      │
└─────────────▲──────────────┘
              │ HTTPS (REST API + JWT)
              ▼
┌─────────────────────────────────────────┐
│           Backend API Layer              │
│         Spring Boot (Dockerized)         │
│             Render Hosting               │
│                                         │
│  ┌───────────────────────────────────┐  │
│  │ Authentication & Authorization    │  │
│  │ - JWT Security                    │  │
│  │ - Role-based access               │  │
│  └───────────────────────────────────┘  │
│                                         │
│  ┌───────────────────────────────────┐  │
│  │ Learner & Provider Services        │  │
│  │ - Profiles                        │  │
│  │ - Applications                    │  │
│  │ - Notifications                   │  │
│  └───────────────────────────────────┘  │
│                                         │
│  ┌───────────────────────────────────┐  │
│  │ Academic Performance Engine        │  │
│  │ - Results ingestion               │  │
│  │ - Validation & scoring            │  │
│  │ - Performance passport            │  │
│  └───────────────────────────────────┘  │
│                                         │
│  ┌───────────────────────────────────┐  │
│  │ Search & Eligibility Engine        │  │
│  │ - Dynamic filtering               │  │
│  │ - Predictive scoring              │  │
│  └───────────────────────────────────┘  │
└─────────────▲───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│           Data & Storage Layer           │
│      PostgreSQL (Neon / Local DB)        │
│                                         │
│ - Users (Learners, Providers, Admins)   │
│ - Academic Results                      │
│ - Applications                          │
│ - Notifications                         │
│ - Verification Metadata                 │
└─────────────────────────────────────────┘

(Optional / Future)
┌─────────────────────────────────────────┐
│ -  AI / OCR Microservice           │
│ - Report card scanning                  │
│ - Subject & score extraction            │
│ - Fraud detection                       │
└─────────────────────────────────────────┘
```

---

### 🔐 Security Architecture

* Stateless **JWT authentication**
* Role-based authorization:

    * `LEARNER`
    * `PROVIDER`
* Secure CORS handling for:

    * Local development
    * Vercel production frontend
* Encrypted credentials and tokens

---

### 📦 Deployment Architecture

| Layer    | Technology               | Platform     |
| -------- | ------------------------ | ------------ |
| Frontend | React + Vite             | Vercel       |
| Backend  | Spring Boot + Docker     | Render       |
| Database | PostgreSQL               | Neon         |
| Auth     | JWT                      | Backend      |
| CI/CD    | GitHub → Vercel / Render | Cloud-native |

---

## 🧪 Running the Project Locally

### 🛠️ Prerequisites

Make sure you have the following installed:

* **Node.js** (v18+ recommended)
* **Java JDK 17**
* **Maven**
* **Docker** (optional, for containerized runs)
* **PostgreSQL** (local or Neon)

---

## ▶️ Backend (Spring Boot)

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/vtl-28/learner-bursary-platform.git
cd learner-bursary-platform
```

---

### 2️⃣ Configure Database

Option A: **Use Neon (Recommended)**

* Keep the existing `application.properties`

Option B: **Local PostgreSQL**

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/bursary_db
spring.datasource.username=postgres
spring.datasource.password=your_password
```

Ensure the database exists:

```sql
CREATE DATABASE bursary_db;
```

---

### 3️⃣ Run Backend Locally

```bash
./mvn spring-boot:run
```

Backend will start on:

```
http://localhost:8181
```

Health check:

```
http://localhost:8181/health
```

Swagger UI:

```
http://localhost:8181/swagger-ui.html
```

---

### 🐳 (Optional) Run Backend with Docker

```bash
docker build -t bursary-backend .
docker run -p 8181:8181 bursary-backend
```

---

## ▶️ Frontend (React)

### 1️⃣ Navigate to Frontend Directory

```bash
cd frontend
```

---

### 2️⃣ Install Dependencies

```bash
npm install
```

---

### 3️⃣ Configure API Base URL

`src/constants/Constants.js`

```js
export const API_BASE_URL = 'http://localhost:8181/api/v1';
```

---

### 4️⃣ Run Frontend

```bash
npm run dev
```

Frontend runs on:

```
http://localhost:5173
```

---

## 🧭 Environment Switching

| Environment | Frontend API URL                                       |
| ----------- | ------------------------------------------------------ |
| Local       | `http://localhost:8181/api/v1`                         |
| Production  | `https://learner-bursary-platform.onrender.com/api/v1` |

---
