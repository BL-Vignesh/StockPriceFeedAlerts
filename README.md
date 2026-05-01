# Real-Time Portfolio Alerts System

A robust Spring Boot backend application providing real-time stock portfolio tracking, threshold alerts, bulk Excel uploads, and a secure JWT-based authentication system with RabbitMQ-driven email notifications.

---

## 👤 Profile
| Field | Details |
|---|---|
| **Project Name** | RealTime Portfolio Alerts |
| **Artifact ID** | `StockPriceFeed` |
| **Group ID** | `com.portfolio` |
| **Version** | `0.0.1-SNAPSHOT` |
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.3.0 |
| **Database** | PostgreSQL |
| **Authentication** | JWT |
| **Message Queues** | Apache Kafka + RabbitMQ |
| **Build Tool** | Maven |

---

## 📋 Description
Real-Time Portfolio Alerts is a complete backend REST API that allows authenticated investors to:
- Register and login securely with JWT authentication
- Upload portfolio via Excel using Apache POI or add stocks manually via UI form
- Validate stocks and view live portfolio valuation
- Manage portfolio — update quantity/price, delete stocks
- Set upper and lower % threshold alerts per stock
- Monitor portfolio in real-time via Kafka price cache & SSE (Server-Sent Events)
- Receive automated HTML email alerts when price crosses threshold via RabbitMQ

---

## 🏗️ Project Structure
```text
StockPriceFeedAlerts/
├── src/main/java/com/portfolio/stockpricefeed/
│   ├── config/          # Security, Kafka, RabbitMQ configs
│   ├── controller/      # REST API endpoints
│   ├── dto/             # Data Transfer Objects
│   ├── entities/        # JPA Entities (User, Portfolio, Alert, etc.)
│   ├── exception/       # Custom exceptions
│   ├── kafka/           # Kafka producers, consumers, and cache
│   ├── rabbitmq/        # RabbitMQ email alert processors
│   ├── repository/      # Spring Data JPA repositories
│   ├── service/         # Business logic interfaces
│   ├── serviceImpl/     # Service implementations
│   └── utils/           # Helper classes (JWT, Excel parsing)
├── src/main/resources/
│   └── application.yaml # App configuration
├── pom.xml              # Maven dependencies
└── compose.yaml         # Docker compose for Kafka & RabbitMQ
```

---

## 🛠 Tech Stack
- **Java 17** & **Spring Boot 3.3.0**
- **PostgreSQL** (Primary Data Store - Running Locally)
- **Apache Kafka** (Message Broker for Real-time Price Updates)
- **RabbitMQ** (Message Broker for Email Alert Processing)
- **Spring Security** & **jjwt** (Token Management)
- **Apache POI** (Excel Processing)
- **Spring Boot Mail** (Email Notifications)
- **Springdoc OpenAPI** (Swagger Documentation)
- **React** (Frontend UI Dashboard)

---

## 🚦 US Progress Tracker
| US | User Story | Status |
|---|---|---|
| **US1 & US2** | User Registration & Login (JWT) | ✅ Completed |
| **US3 & US4** | Home Page & Stock Market API | ✅ Completed |
| **US5** | Upload Portfolio (Excel) | ✅ Completed |
| **US6** | Create Portfolio (UI Form) | ✅ Completed |
| **US7** | Manage Portfolio (Update/Delete) | ✅ Completed |
| **US8** | Alert Threshold Setting | ✅ Completed |
| **US9** | Real-time Portfolio Monitor (Kafka + SSE) | ✅ Completed |
| **US10** | Send Alert Email (RabbitMQ) | ✅ Completed |

---

## ⚙️ Getting Started

### 1. Boot up the Infrastructure (Kafka & RabbitMQ)
This project uses Docker to run Kafka and RabbitMQ. Ensure Docker is running, then:
```bash
docker-compose up -d
```

### 2. Configure Local PostgreSQL
Ensure PostgreSQL is installed locally and running. Update your `application.yaml` with your local Postgres credentials (username, password, database name).

### 3. Start the Backend Server
Start the Spring application using the included Maven wrapper:
```bash
# On Windows
.\mvnw.cmd spring-boot:run

# On Mac/Linux
./mvnw spring-boot:run
```
*The server will start locally on port `8080`.*

### 4. Explore the API via Swagger
You can interactively test and view all available endpoints through the built-in Swagger UI:
**👉 [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**

*(Note: During endpoint testing for protected routes, ensure you register a user, login, retrieve the JWT Token, and secure your Swagger session by clicking the green "Authorize" padlock).*

---

## 📬 Postman / API Flow
1. `POST /api/v1/auth/register` - Register a new user
2. `POST /api/v1/auth/login` - Login to get JWT
3. `POST /api/v1/portfolio/upload` - Upload Excel portfolio
4. `POST /api/v1/portfolio` - Add single stock
5. `GET /api/v1/portfolio` - View portfolio
6. `POST /api/v1/alerts` - Set threshold alerts
7. `GET /api/v1/monitor` - Live portfolio tracking
8. `GET /api/v1/alerts/history` - View triggered alerts
