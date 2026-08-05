# 🔐 Offline UPI Payment Backend

A secure Offline UPI Payment Backend built using Spring Boot.

This project simulates an Offline UPI Payment System with JWT Authentication, Wallet Management, Secure Money Transfer, Transaction History and Hybrid Encryption (AES + RSA).

---

## 🚀 Features

- User Registration
- User Login (JWT Authentication)
- Wallet Credit
- Wallet Debit
- Wallet Balance Check
- Secure Money Transfer
- Transaction History
- Hybrid Encryption (AES + RSA)
- Global Exception Handling
- Swagger API Documentation
- SLF4J Logging

---

## 🛠️ Tech Stack

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- MySQL
- JWT
- AES Encryption
- RSA Encryption
- Swagger OpenAPI
- Maven

## 🏗️ System Architecture

```text
                Client
                   │
                   ▼
          Spring Security (JWT)
                   │
                   ▼
             REST Controllers
        ┌──────────┼──────────┐
        ▼          ▼          ▼
   User Service  Wallet Service  Payment Service
        │          │          │
        └──────────┼──────────┘
                   ▼
           Spring Data JPA
                   │
                   ▼
                 MySQL
```

## 🔐 Hybrid Encryption Flow

```text
                    CLIENT

             PaymentRequest
                    │
                    ▼
             Convert to JSON
                    │
                    ▼
               AES Encrypt
                    │
        ┌───────────┴───────────┐
        ▼                       ▼
Encrypted Payment Data     AES Secret Key
                                    │
                                    ▼
                               RSA Encrypt
                                    │
                                    ▼
                          Encrypted AES Key

=================== Network ===================

        {
          encryptedData,
          encryptedKey
        }

=================== SERVER ====================

                  Encrypted AES Key
                          │
                          ▼
                     RSA Decrypt
                          │
                          ▼
                     AES Secret Key
                          │
                          ▼
                 AES Decrypt Data
                          │
                          ▼
                     Original JSON
                          │
                          ▼
                    PaymentRequest
                          │
                          ▼
                  PaymentService
                          │
                          ▼
                 Wallet Update + Save
                          │
                          ▼
                     Success Response
```

## 📂 Project Structure

```text
offline-upi-payment
│── src
│   ├── main
│   │   ├── java
│   │   │   └── com.satyam.offline_upi_payment
│   │   │       ├── config
│   │   │       ├── controller
│   │   │       ├── dto
│   │   │       ├── entity
│   │   │       ├── exception
│   │   │       ├── repository
│   │   │       ├── security
│   │   │       ├── service
│   │   │       │   └── impl
│   │   │       └── OfflineUpiPaymentApplication.java
│   │   │
│   │   └── resources
│   │       ├── application.properties
│   │       └── static
│   │
│   └── test
│
├── pom.xml
└── README.md
```

## 📡 API Endpoints

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/users/register` | Register a new user |
| POST | `/api/users/login` | Authenticate user and generate JWT |

---

### Wallet

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/wallet/balance/{upiId}` | Check wallet balance |
| POST | `/api/wallet/credit` | Credit wallet |
| POST | `/api/wallet/debit` | Debit wallet |

---

### Payments

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/payments/send` | Normal payment |
| POST | `/api/payments/secure-transfer` | Secure payment using Hybrid Encryption |
| GET | `/api/payments/history/{upiId}` | Get transaction history |

---

> **Complete API documentation is available in Swagger UI.**
> 
> ## ⚙️ Installation

### 1. Clone the Repository

```bash
git clone https://github.com/satyam56-svg/offline-upi-payment.git
```

### 2. Navigate to the Project

```bash
cd offline-upi-payment
```

### 3. Configure MySQL

Update the `application.properties` file with your MySQL credentials.

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/offline_upi
spring.datasource.username=root
spring.datasource.password=your_password
```

### 4. Build the Project

```bash
mvn clean install
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

The application will start at:

```text
http://localhost:8080
```

## 📖 API Documentation

The project uses **Swagger OpenAPI** for interactive API documentation.

After running the application, open:

```text
http://localhost:8080/swagger-ui/index.html
```

Swagger provides:

- Complete API Documentation
- Request & Response Models
- Interactive API Testing
- Endpoint Descriptions
```

## 🚀 Future Enhancements

- Develop a React-based frontend
- QR Code based UPI payments
- Docker containerization
- Cloud deployment (AWS / Render / Railway)
- API response optimization using DTOs
- Unit & Integration Testing
- Payment Notifications
- Role-Based Access Control (RBAC)

## 👨‍💻 Author

**Satyam Kumar**

- GitHub: https://github.com/satyam56-svg
- LinkedIn: https://www.linkedin.com/in/satyam-kumar-6a6888281/