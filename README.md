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

## 🏗️ System Architecture & Client-Side Non-Repudiation

```text
               BROWSER CLIENT (React)                      SERVER (Spring Boot)
       ┌─────────────────────────────────────┐      ┌─────────────────────────────┐
       │ 1. Keygen: Web Crypto API           │      │                             │
       │    - 2048-bit RSASSA-PKCS1-v1_5 SHA-256│      │                             │
       │ 2. Save Private Key (non-extractable│      │                             │
       │    CryptoKey) -> IndexedDB          │      │                             │
       │ 3. Send SPKI Public Key ------------┼─────►│ Stores Public Key in DB     │
       │                                     │      │ (No Private Key stored)     │
       │ 4. Pay: Fetch Bank Public Key ◄─────┼──────│ GET /api/bank/public-key  │
       │ 5. Encrypt Payload: AES-256-GCM      │      │                             │
       │ 6. Wrap AES Key: RSA-OAEP SHA-256   │      │                             │
       │ 7. Sign Payload: Web Crypto API     │      │                             │
       │ 8. Send Pre-Signed PaymentPacket ───┼─────►│ Verify & Mesh Forward       │
       └─────────────────────────────────────┘      └─────────────────────────────┘
```

> [!WARNING]
> **Important Database Schema Requirement**:
> User private keys are generated and stored exclusively within browser **IndexedDB** as non-extractable `CryptoKey` objects. The `privateKey` column on the `User` entity has been dropped.
> Deployments require a **fresh database instance** (or clearing existing user table rows) as legacy users registered under the server-custodial key model will not have a client-side private key in browser storage and must re-register.

---

## 🔐 Hybrid Encryption & Non-Repudiation Flow

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


## 🔑 Bank RSA Key Persistence

The bank server uses a 2048-bit RSA key pair to decrypt the AES keys that
clients embed in each payment packet.

### How it works

On startup, `BankKeyManager` checks for PEM files at the paths configured in
`application.properties`:

```properties
bank.key.public-path=./keys/bank_public.pem
bank.key.private-path=./keys/bank_private.pem
```

| Scenario | Behaviour |
|---|---|
| **Both files exist** | Keys are loaded from disk — no new pair is generated |
| **Either file is missing** | A fresh RSA key pair is generated and **both** files are written to disk |

The `keys/` directory is listed in `.gitignore` so the files are never
committed to source control.

### ⚠️ Important: do not delete the keys directory carelessly

Every payment packet carries an AES session key that was **encrypted with the
bank's public key at the time the packet was created**. If you delete
`keys/bank_private.pem` (or the whole `keys/` directory), the server will
generate a brand-new key pair on the next restart and will be **permanently
unable to decrypt any packet that was created before that restart**.

Practically, this means:
- Any in-flight or queued offline payment packets will fail with a decryption
  error.
- Only packets created *after* the restart (using the new public key) will
  succeed.

### 🏭 Production note

> **These PEM files are a development-only convenience.**  
> In a production or staging deployment you should replace `BankKeyManager`
> with an integration that fetches and stores keys from a proper
> **Key Management Service (KMS)** or **Hardware Security Module (HSM)** — for
> example AWS KMS, Google Cloud KMS, Azure Key Vault, or HashiCorp Vault. A
> real KMS provides:
> - Encrypted key storage with access-control policies
> - Automatic key rotation without service downtime
> - Audit logging of every key usage
> - No plaintext private key material on the filesystem

---

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