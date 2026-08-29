# FitPulse-Event-Driven-Fitness-AI-Telemetry-Platform


A **microservices-based fitness tracking application** built with Spring Boot, Spring Cloud, React, MongoDB, PostgreSQL, RabbitMQ, and Google Gemini AI.

The application provides JWT-based authentication, fitness activity tracking, and AI-powered fitness recommendations using an event-driven architecture.

---

## 🚀 Features

* JWT-based user authentication and authorization
* User registration and login with BCrypt password hashing
* Fitness activity creation and tracking
* AI-generated fitness recommendations
* Asynchronous activity processing using RabbitMQ
* API Gateway with centralized JWT validation
* Service discovery using Eureka
* Centralized configuration using Spring Cloud Config
* PostgreSQL for user data
* MongoDB for activities and recommendations
* React frontend with Redux Toolkit

---

## 🏗️ Architecture

![FitPulse Architecture](fitpulse-architecture.svg)

### Infrastructure

* **Eureka Server** — service discovery (`:8761`)
* **Config Server** — centralized configuration (`:8888`)
* **API Gateway** — routing and JWT validation (`:8080`)

---

## 🔄 Request Flow

### Authentication

```text
React
  ↓
API Gateway
  ↓
User Service
  ↓
PostgreSQL
  ↓
JWT returned to client
```

The API Gateway validates the JWT on protected requests and forwards the authenticated user's ID to downstream services through the `X-User-ID` header.

### Activity + AI Processing

```text
React
  ↓
API Gateway
  ↓
Activity Service
  ↓
MongoDB
  ↓
RabbitMQ
  ↓
AI Service
  ↓
Google Gemini
  ↓
Recommendation → MongoDB
```

AI processing is asynchronous, so creating an activity does not have to wait for Gemini to generate the recommendation.

---

## 🛠️ Tech Stack

### Backend

* Java 23
* Spring Boot
* Spring Cloud
* Spring Cloud Gateway
* Eureka
* Spring Cloud Config
* Spring Data JPA
* Spring Data MongoDB
* WebClient
* RabbitMQ
* JWT / JJWT
* BCrypt

### Frontend

* React
* Vite
* Redux Toolkit
* React Router
* Axios
* Material UI

### Databases & AI

* PostgreSQL
* MongoDB
* RabbitMQ
* Google Gemini API

---

## 📁 Project Structure

```text
FitPulse/
├── eureka/
├── configserver/
├── gateway/
├── userservice/
├── activityservice/
├── aiservice/
├── fitness-app-frontend/
└── README.md
```

---

## 🔌 Main APIs

| Method | Endpoint                             | Description           |
| ------ | ------------------------------------ | --------------------- |
| POST   | `/api/users/register`                | Register user         |
| POST   | `/api/users/login`                   | Login                 |
| GET    | `/api/users/{id}`                    | Get user              |
| POST   | `/api/activities`                    | Create activity       |
| GET    | `/api/activities`                    | Get user activities   |
| GET    | `/api/activities/{id}`               | Get activity          |
| GET    | `/api/recommendations/activity/{id}` | Get AI recommendation |

All protected APIs are accessed through the API Gateway using:

```http
Authorization: Bearer <JWT>
```

---

## ⚙️ Getting Started

### Prerequisites

* JDK 23
* Maven
* Node.js
* PostgreSQL
* MongoDB
* RabbitMQ
* Google Gemini API Key

### 1. Clone

```bash
git clone <repository-url>
cd FitPulse
```

### 2. Configure

Configure PostgreSQL, MongoDB, RabbitMQ, JWT secret, and Gemini API credentials in the application configuration/environment variables.

### 3. Start Services

Start in this order:

```text
Eureka
↓
Config Server
↓
User Service
Activity Service
AI Service
↓
API Gateway
↓
React Frontend
```

### 4. Start Frontend

```bash
cd fitness-app-frontend
npm install
npm run dev
```

Frontend:

```text
http://localhost:5173
```

Gateway:

```text
http://localhost:8080
```

---

## 🔐 Security

* Passwords are hashed using BCrypt.
* JWTs are signed using HMAC-SHA256.
* API Gateway validates JWTs before forwarding protected requests.
* User identity is propagated using `X-User-ID`.
* Sensitive credentials should be supplied through environment variables in production.

---

## 📌 Future Improvements

* Refresh token support
* Role-based authorization
* Activity ownership validation
* RabbitMQ retry/dead-letter queues
* Resilience4j circuit breakers
* Centralized logging and monitoring
* Docker/Kubernetes deployment
* Automated integration tests

