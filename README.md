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

---

## 👩‍💻 Author

**Madhuri H S**

Java | Spring Boot | Microservices | AI



Markdown# FitPulse: Event-Driven AI Fitness Platform

A microservices-based fitness tracking application built to demonstrate distributed system architecture using Spring Cloud, RabbitMQ, and Google Gemini AI. 

FitPulse handles JWT-based authentication at the gateway level, securely persists user activity across relational and NoSQL databases, and utilizes an event-driven architecture to generate asynchronous AI fitness recommendations without blocking client requests.

---

## 🚀 Features

* **Centralized Security:** API Gateway handles all JWT validation and routes authenticated requests with downstream `X-User-ID` headers.
* **Event-Driven AI Processing:** RabbitMQ queues activity data so Google Gemini can generate personalized fitness recommendations asynchronously.
* **Microservices Architecture:** Fully decoupled services using Eureka for discovery and Spring Cloud Config for centralized property management.
* **Polyglot Persistence:** PostgreSQL handles structured user data and credentials, while MongoDB manages flexible, document-based activity logs and AI responses.
* **Secure Authentication:** BCrypt password hashing and stateless JWT sessions.
* **Modern Client:** React frontend integrated with Redux Toolkit for state management.

---

## 🏗️ Architecture

![FitPulse Architecture](fitpulse-architecture.svg)

### Infrastructure Components
* **Eureka Server** (`:8761`) — Service registry and discovery
* **Config Server** (`:8888`) — Centralized configuration management
* **API Gateway** (`:8080`) — Routing, rate limiting, and global JWT validation

---

## 🔄 Request Flow

### Authentication Flow
```text
Client (React) → API Gateway → User Service → PostgreSQL → JWT returned to client
The API Gateway validates the JWT on all protected routes and forwards the authenticated user's ID to downstream services.Asynchronous AI Processing FlowPlaintextClient → API Gateway → Activity Service → MongoDB (Saves Activity)
                           ↓
                      RabbitMQ (Publishes Event)
                           ↓
        AI Service ← (Consumes Event) → Google Gemini API
                           ↓
               MongoDB (Saves Recommendation)
Because AI processing is asynchronous, the Activity Service responds to the client immediately without waiting for Gemini to generate the recommendation.🛠️ Tech StackLayerTechnologyBackend FrameworkJava 23, Spring Boot 3.x, Spring WebFlux (WebClient)Spring CloudAPI Gateway, Eureka Naming Server, Config ServerDatabasesPostgreSQL (Spring Data JPA), MongoDB (Spring Data MongoDB)Message BrokerRabbitMQSecuritySpring Security, JWT (JJWT), BCryptAI IntegrationGoogle Gemini APIFrontendReact, Vite, Redux Toolkit, Material UI🔌 Main APIsAll protected APIs require: Authorization: Bearer <JWT> via the API Gateway.MethodEndpointDescriptionPOST/api/users/registerRegister new userPOST/api/users/loginAuthenticate and return JWTGET/api/users/{id}Retrieve user profilePOST/api/activitiesLog a new fitness activityGET/api/activitiesList all activities for userGET/api/activities/{id}Get specific activity detailsGET/api/recommendations/activity/{id}Fetch async AI recommendation⚙️ Getting StartedPrerequisitesJDK 23+ & MavenNode.jsPostgreSQL & MongoDB (running locally or via Docker)RabbitMQGoogle Gemini API Key1. Clone & ConfigureBashgit clone [https://github.com/MADHURI-HS/FitPulse.git](https://github.com/MADHURI-HS/FitPulse.git)
cd FitPulse
Ensure your environment variables are set for the Config Server to distribute:POSTGRES_URL, POSTGRES_USER, MONGO_URI, RABBITMQ_HOST, JWT_SECRET, and GEMINI_API_KEY.2. Bootstrapping the ClusterServices must be started in the following order to ensure proper registration:Eureka ServerConfig ServerUser Service / Activity Service / AI ServiceAPI Gateway3. Start FrontendBashcd fitness-app-frontend
npm install
npm run dev
Access the application at http://localhost:5173.📌 Future Improvements & ScalingTo push this architecture further toward enterprise production standards, I plan to implement:Resilience4j: Adding Circuit Breakers to the AI Service to handle rate limits or downtime from the Gemini API.Dead Letter Queues (DLQ): Configuring RabbitMQ retries and DLQs for failed activity processing events.Distributed Tracing: Integrating Micrometer and Zipkin to trace request spans across the microservices.Dockerization: Writing Dockerfiles for each service and a docker-compose.yml to spin up the entire cluster and backing services simultaneously.Author: Madhuri H S
