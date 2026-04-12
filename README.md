# Real-Time Portfolio Alerts System

A robust Spring Boot backend application providing real-time stock portfolio tracking, threshold alerts, bulk Excel uploads, and a secure JWT-based authentication system.

## 🚀 Core Features

- **Authentication System (US1 & US2):** Secure user registration and login flows utilizing stateless JSON Web Tokens (JWT).
- **Market Data API (US3 & US4):** Live fetching of current stock prices from Yahoo Finance API for the top 50 NSE stocks.
- **Portfolio Management (US6 - US8):** Complete set of endpoints allowing users to manually create, edit, update, or remove stock holdings, as well as configure custom alert-threshold prices.
- **Bulk Upload Processing (US5):** Server-side raw Excel (`.xls` / `.xlsx`) parsing using **Apache POI**. Users can upload complete offline spreadsheets to instantiate their portfolios instantly.
- **Real-Time Data Streams (US9 & US10):** Event-driven architecture powered by **Apache Kafka** and **Server-Sent Events (SSE)**. Instantly pushes live price valuations, gain/loss metrics, and threshold-breach alerts directly to the frontend the moment they occur.
- **API Documentation:** Interactive and auto-updating Swagger documentation endpoint.

## 🛠 Tech Stack

- **Java 17** & **Spring Boot 3.3.0**
- **PostgreSQL 16** (Primary Data Store)
- **Apache Kafka v7.9.0** (KRaft Mode - Message Broker)
- **Spring Security** & **jjwt** (Token Management)
- **Apache POI** (Excel Processing)
- **Springdoc OpenAPI** (Swagger Documentation)

## 📦 Prerequisites

Ensure you have the following installed on your machine before running the application locally:
- [Java Development Kit (JDK) 17](https://jdk.java.net/17/)
- [Docker & Docker Compose](https://www.docker.com/) (Required for running Postgres and Kafka instantly)

## ⚙️ Getting Started

### 1. Boot up the Infrastructure
This project includes a fully configured `compose.yaml` to spin up your PostgreSQL database and Kafka broker.

Navigate to the root directory where `compose.yaml` is located and run:
```bash
docker-compose up -d
```

### 2. Start the Backend Server
Once the containers are healthy, you can boot the Spring application using the included Maven wrapper:
```bash
# On Windows
.\mvnw.cmd spring-boot:run

# On Mac/Linux
./mvnw spring-boot:run
```
*The server will start locally on port `8080`.*

### 3. Explore the API
You can interactively test and view all available endpoints through the built-in Swagger UI. Once the application is running, open your web browser and navigate to:
**👉 [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**

*(Note: During endpoint testing for protected routes, ensure you register a user, login, retrieve the JWT Token, and secure your Swagger session by clicking the green "Authorize" padlock at the top of the interface).*
