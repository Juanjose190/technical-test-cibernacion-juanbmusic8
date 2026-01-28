# BankPro Core API

![Java 21](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Spring Boot 3.5.7](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen?style=flat-square&logo=springboot)
![PostgreSQL 15](https://img.shields.io/badge/PostgreSQL-15-blue?style=flat-square&logo=postgresql)

** Case Study**

**BankPro** is a robust backend system designed for **credit application management**. It serves as the core engine for processing financial requests, implementing automated business rules, and ensuring data integrity.

### **Key Features**

*   **Full CRUD Operations:** Manage credit applications seamlessly.
*   **Automated Credit Evaluation:** Rule-based approval system based on `maxAutoEvalAmount`.
*   **Product Differentiation:** Specialized handling for `PERSONAL` and `BUSINESS` credit types.
*   **Lifecycle Management:** State transitions through `PENDING`, `APPROVED`, and `REJECTED`.
*   **Centralized Exception Handling:** Standardized API responses for all error scenarios.
*   **Container-Ready:** Fully dockerized for consistent deployment.

---

### ** Tech Stack**

*   **Language:** Java 21 (LTS)
*   **Framework:** Spring Boot 3.5.7
*   **Data Access:** Spring Data JPA
*   **Database:** PostgreSQL
*   **Infrastructure:** Docker / Docker Compose
*   **Testing:** JUnit 5 + Mockito

---

### ** Quick Start with Docker**

1.  **Clone the repository:**
    ```bash
    https://github.com/Juanjose190/technical-test-cibernacion-juanbmusic8.git
    ```
2.  **Navigate to the folder:**
    ```bash
    cd credits-core
    ```
3.  **Launch services:**
    ```bash
    docker-compose up --build
    ```

**API Access:** The server will be available at: [http://localhost:8080](http://localhost:8080)

---

### ** API Endpoints**

| Method | URL | Description |
| :--- | :--- | :--- |
| **POST** | `/api/credits` | Submit a new credit application |
| **GET** | `/api/credits` | Retrieve all applications |
| **GET** | `/api/credits/{id}` | Find application by ID |
| **PUT** | `/api/credits/{id}` | Update existing application |
| **DELETE** | `/api/credits/{id}` | Remove an application |

---

### ** Local Development (Manual)**

To run without Docker, ensure you have a local PostgreSQL instance and execute:
```bash
./mvnw spring-boot:run
```

---

### ** Architectural Decisions**

*   **Immutable Data Structures:** Utilizing Java Records for `CreditRequestDTO` and `CreditResponseDTO`.
*   **Transaction Management:** `@Transactional` services to ensure ACID compliance.
*   **Separation of Concerns:** Business logic is centralized in the service layer, keeping controllers thin.
*   **Security & CORS:** Restricted access to authorized origins (e.g., Angular on port 4200).
*   **Global Error Handling:** `@ControllerAdvice` for consistent error payloads.
*   **Auditing:** Automated timestamping with `AuditingEntityListener`.

---

### ** Testing Strategy**

The project follows a test-driven approach to ensure reliability:
*   **Unit Tests:** Full coverage of `CreditService`.
*   **Scenarios:** Validates auto-approval logic, rejection thresholds, and boundary conditions.
*   **Error Handling:** Verifies correct throwing of `EntityNotFoundException`.

**Run tests:**
```bash
./mvnw test
```
**Coverage:** 100% of core business logic is covered using Mockito and JUnit 5.

---

### ** Configuration**

Key settings in `application.yml`:
```yaml
credit:
  auto-eval:
    max-amount: 50000.00 # Threshold for automatic approval
server:
  port: 8080
```
*   **Database:** Default config uses postgres user/pass on `bcredits` schema.
*   **HikariCP:** Optimized pool settings (Max: 10, Min: 5).
