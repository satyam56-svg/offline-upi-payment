# 📡 Offline UPI Payment — Bluetooth Mesh Demo Backend

A Spring Boot demo that simulates how UPI payments could travel over a **Bluetooth store-and-forward mesh** when there is no internet, and settle automatically the moment any device in the mesh regains connectivity.

> **This is a learning / proof-of-concept project.** It models the full end-to-end cryptographic pipeline on a single JVM so it can be explored without real hardware or a real UPI network.

---

## 🚀 Features

- **Bluetooth Mesh Simulation** — virtual devices gossip packets hop-by-hop until a bridge node with internet is reached
- **Store-and-Forward** — offline devices hold encrypted packets and relay them to neighbours; TTL controls maximum hop count
- **Hybrid Encryption (RSA-OAEP + AES-256-GCM)** — payment instructions are encrypted end-to-end; intermediate nodes can route but never read the payload
- **Idempotent Settlement** — SHA-256 ciphertext hash used as dedup key; concurrent bridge uploads are safely handled
- **Replay & Staleness Protection** — packets are rejected if older than the configured freshness window or future-dated
- **Live Dashboard** — Thymeleaf UI at `/dashboard` showing mesh state, accounts, and recent transactions
- **H2 In-Memory Database** — zero-setup; data seeded automatically on every start
- **SLF4J / Logback Logging** — structured console logging across all services

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.5 |
| Web | Spring MVC (REST + Thymeleaf) |
| Persistence | Spring Data JPA + H2 (in-memory) |
| Encryption | RSA-OAEP SHA-256 + AES-256-GCM (JCA) |
| Build | Maven (Maven Wrapper included) |
| Containerisation | Docker (eclipse-temurin:17-jdk) |
| Logging | SLF4J + Logback |

---

## 🏗️ Architecture Overview

```text
  ┌──────────────────────────────────────────────────────────────────────┐
  │                      OFFLINE ZONE (no internet)                      │
  │                                                                      │
  │  phone-alice ──gossip──► phone-stranger1 ──gossip──► phone-stranger2 │
  │       │                                                    │         │
  │  (injects           Each hop:  copy packet,               │         │
  │   encrypted         decrement TTL                         │         │
  │   MeshPacket)                                             ▼         │
  │                                              phone-stranger3         │
  │                                                    │                 │
  └────────────────────────────────────────────────────│─────────────────┘
                                                       │ gossip
                                               ┌───────▼──────┐
                                               │ phone-bridge  │  ← has internet
                                               └───────┬───────┘
                                                       │ POST /api/mesh/flush
                                               ┌───────▼───────────────────┐
                                               │   Spring Boot Backend     │
                                               │  BridgeIngestionService   │
                                               │   ├─ dedup (SHA-256 hash) │
                                               │   ├─ decrypt (RSA + AES)  │
                                               │   ├─ staleness check      │
                                               │   └─ SettlementService    │
                                               │        ├─ debit sender    │
                                               │        └─ credit receiver │
                                               └───────────────────────────┘
```

---

## 🔐 Hybrid Encryption Wire Format

Each `MeshPacket.ciphertext` is a single Base64-encoded blob:

```text
[ 256 bytes — RSA-OAEP encrypted AES-256 session key ]
[ 12 bytes  — AES-GCM IV                             ]
[ N bytes   — AES-GCM ciphertext + 16-byte auth tag  ]
```

- The **session key** is fresh per packet — compromise of one packet reveals nothing about others.
- **AES-GCM authenticated encryption** means any single-bit tampering by an intermediate node causes decryption to throw on the server side.
- The server's **RSA-2048 key pair** is generated once on first startup and held in memory for the duration of the process.

---

## 📂 Project Structure

```text
offline-upi-payment/
├── src/main/
│   ├── java/com/satyam/offline_upi_payment/
│   │   ├── config/
│   │   │   └── AppConfig.java               # @EnableScheduling
│   │   ├── controller/
│   │   │   ├── ApiController.java           # All REST endpoints (/api/**)
│   │   │   └── DashboardController.java     # Serves Thymeleaf dashboard
│   │   ├── crypto/
│   │   │   ├── HybridCryptoService.java     # RSA-OAEP + AES-256-GCM encrypt/decrypt
│   │   │   └── ServerKeyHolder.java         # Generates & holds the server RSA key pair
│   │   ├── entity/
│   │   │   ├── Account.java                 # JPA entity — VPA + balance
│   │   │   └── Transaction.java             # JPA entity — settled/rejected payments
│   │   ├── model/
│   │   │   ├── MeshPacket.java              # Over-the-wire packet (encrypted)
│   │   │   └── PaymentInstruction.java      # Decrypted payment payload
│   │   ├── repository/
│   │   │   ├── AccountRepository.java
│   │   │   └── TransactionRepository.java
│   │   ├── BridgeIngestionService.java      # Validates & settles packets from bridge nodes
│   │   ├── DemoService.java                 # Creates demo MeshPackets; seeds demo accounts
│   │   ├── IdempotencyService.java          # In-memory dedup cache (ConcurrentHashMap)
│   │   ├── MeshSimulatorService.java        # Gossip protocol between VirtualDevices
│   │   ├── SettlementService.java           # Atomic debit/credit + Transaction record
│   │   ├── VirtualDevice.java               # Simulated phone (holds packets, has/no internet)
│   │   └── OfflineUpiPaymentApplication.java
│   └── resources/
│       ├── application.properties
│       └── templates/
│           └── dashboard.html               # Live mesh + account dashboard
├── data/
│   └── offline_upi_db.mv.db                # H2 file-mode DB (dev only)
├── Dockerfile
├── pom.xml
└── README.md
```

---

## 📡 API Endpoints

### Server Info

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/server-key` | Returns the server's RSA-2048 public key (Base64) and algorithm metadata |

---

### Demo — Create & Send a Payment

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/demo/send` | Build an encrypted `MeshPacket` and inject it into the mesh at a named device |

**Request body:**

```json
{
  "senderVpa":   "alice@demo",
  "receiverVpa": "bob@demo",
  "amount":      100.00,
  "pin":         "1234",
  "ttl":         5,
  "startDevice": "phone-alice"
}
```

`ttl` defaults to `5`; `startDevice` defaults to `"phone-alice"`.

---

### Mesh Simulation

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/mesh/state` | Snapshot of all virtual devices: which packets each holds, internet flag |
| POST | `/api/mesh/gossip` | Run one gossip round — propagate packets between all pairs of devices (TTL decremented per hop) |
| POST | `/api/mesh/flush` | Collect packets held by bridge nodes (internet=true) and settle them via `BridgeIngestionService` |
| POST | `/api/mesh/reset` | Clear all device queues and the idempotency cache |

---

### Direct Bridge Ingestion (advanced)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/bridge/ingest` | Submit a raw `MeshPacket` directly for settlement; used by `/mesh/flush` internally |

**Headers:** `X-Bridge-Node-Id` (string), `X-Hop-Count` (int)

---

### Data Queries

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/accounts` | List all accounts with current balances |
| GET | `/api/transactions` | Last 20 transactions (settled or rejected) |

---

### Dashboard UI

| URL | Description |
|-----|-------------|
| `http://localhost:8080/dashboard` | Live Thymeleaf dashboard — mesh state, accounts, transactions |
| `http://localhost:8080/h2-console` | H2 browser console (dev only) |

---

## ⚙️ Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+ *(or use the included `mvnw` wrapper — no installation needed)*

### 1. Clone the Repository

```bash
git clone https://github.com/satyam56-svg/offline-upi-payment.git
cd offline-upi-payment
```

### 2. Run the Application

```bash
./mvnw spring-boot:run
```

On Windows:

```cmd
mvnw.cmd spring-boot:run
```

The application starts at **http://localhost:8080**.

### 3. Explore

| What | Where |
|------|-------|
| Dashboard | http://localhost:8080/dashboard |
| H2 Console | http://localhost:8080/h2-console |
| Server public key | GET http://localhost:8080/api/server-key |
| Demo accounts | `alice@demo` · `bob@demo` · `carol@demo` · `dave@demo` |

### 4. Simulate a Payment End-to-End

```bash
# 1. Create an encrypted packet and inject it into the mesh at phone-alice
curl -X POST http://localhost:8080/api/demo/send \
  -H "Content-Type: application/json" \
  -d '{"senderVpa":"alice@demo","receiverVpa":"bob@demo","amount":50,"pin":"1234"}'

# 2. Run a gossip round to spread the packet to other devices
curl -X POST http://localhost:8080/api/mesh/gossip

# 3. Flush bridge nodes — packets reach the server and settle
curl -X POST http://localhost:8080/api/mesh/flush

# 4. Check updated balances
curl http://localhost:8080/api/accounts
```

---

## 🐳 Docker

```bash
# Build the image
docker build -t offline-upi-payment .

# Run the container
docker run -p 8080:8080 offline-upi-payment
```

---

## 🔑 How the Server RSA Key Works

`ServerKeyHolder` generates a fresh **RSA-2048 key pair** in memory at startup.

- The **public key** is exposed via `GET /api/server-key` and used by `DemoService` to encrypt each `PaymentInstruction` before putting it in a `MeshPacket`.
- The **private key** stays server-side and is used by `HybridCryptoService` to decrypt and settle packets.
- **Restarting the server generates a new key pair.** Any packets that were encrypted with the old public key cannot be decrypted after a restart. This is intentional for a demo — in production you would persist keys via a KMS.

> [!NOTE]
> In a production deployment, replace `ServerKeyHolder` with an integration that fetches keys from a proper **Key Management Service (KMS)** — e.g., AWS KMS, Google Cloud KMS, Azure Key Vault, or HashiCorp Vault — so that the private key material never appears in plaintext on the filesystem and key rotation does not break in-flight packets.

---

## 🔒 Security Design

| Concern | How it's handled |
|---------|-----------------|
| Payload confidentiality | AES-256-GCM — intermediate nodes see only ciphertext |
| Payload integrity | GCM auth tag — any tampering causes decryption failure |
| Session key confidentiality | RSA-2048 OAEP SHA-256 — only the server can unwrap the AES key |
| Replay attacks | SHA-256 ciphertext hash stored in `IdempotencyService` with TTL |
| Stale packets | `signedAt` timestamp checked against `upi.mesh.packet-max-age-seconds` |
| Future-dated packets | Rejected if `signedAt` is > 5 minutes ahead of server time |
| Duplicate bridge uploads | `ConcurrentHashMap.putIfAbsent` — exactly one bridge settles per packet |
| Outer-field tampering | `packetId`/`createdAt` are untrusted; idempotency uses the ciphertext hash |

---

## ⚙️ Configuration

```properties
# application.properties

# Server
server.port=8080

# H2 in-memory database (resets on every restart)
spring.datasource.url=jdbc:h2:mem:upimesh
spring.jpa.hibernate.ddl-auto=create-drop

# H2 web console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Idempotency: how long a packet hash is remembered
upi.mesh.idempotency-ttl-seconds=86400

# Packet freshness: packets older than this are rejected
upi.mesh.packet-max-age-seconds=86400
```

---

## 🚀 Future Enhancements

- React-based interactive frontend replacing the Thymeleaf dashboard
- QR-code / NFC payment initiation
- Real Bluetooth Low Energy (BLE) transport layer integration
- Persistent RSA key pair via AWS KMS / HashiCorp Vault
- Redis-backed distributed idempotency cache
- UPI PIN verification against a hashed credential store
- Role-Based Access Control (RBAC) for admin endpoints
- Unit & Integration test suite
- Cloud deployment (AWS / Render / Railway)

---

## 👨‍💻 Author

**Satyam Kumar**

- GitHub: https://github.com/satyam56-svg
- LinkedIn: https://www.linkedin.com/in/satyam-kumar-6a6888281/