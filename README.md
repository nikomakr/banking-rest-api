# 🏦 Banking REST API

> Production-grade Banking REST API built with 

**Java 17** and **Spring Boot 3**, implementing industry-standard practices.

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 🎯 Project Overview

A complete banking system demonstrating enterprise-grade API development with:
- ✅ Account management (create, freeze, close)
- ✅ Money transfers with idempotency
- ✅ Transaction history & audit trails
- ✅ Multi-currency support
- ✅ OAuth 2.0 authentication
- ✅ GDPR & PSD2 compliance
- ✅ Rate limiting & security

## 🏗️ Tech Stack

- **Backend:** Java 17, Spring Boot 3.x, Spring Security
- **Database:** PostgreSQL 15 with JPA/Hibernate
- **Security:** OAuth 2.0, JWT, BCrypt encryption
- **API Docs:** OpenAPI 3.0 (Swagger)
- **Monitoring:** Spring Actuator + Prometheus
- **Testing:** JUnit 5, Mockito, TestContainers

## 📚 Learning Path

This project follows a **curriculum** from Java basics to production deployment:

- **Week 1:** Java fundamentals + Spring Boot setup
- **Week 2:** Core banking features (accounts, transactions)
- **Week 3:** Security & compliance (OAuth, encryption, audit logs)
- **Week 4:** Production readiness (monitoring, rate limiting, docs)

[View Full Learning Plan](docs/LEARNING_PLAN.md)

## 🚀 Quick Start
```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/banking-rest-api.git
cd banking-rest-api

# Run with Docker (coming in Week 2)
docker-compose up -d

# Or run locally (requires Java 17+)
./mvnw spring-boot:run
```

**API will be available at:** `http://localhost:8080/api`

## 📊 Current Progress

- [x] Day 1: Java Basics - Variables, Types, Classes
- [ ] Day 2: Java - Methods, Constructors, OOP
- [ ] Day 3: Java - Collections & Streams
- [ ] Day 4: Spring Boot - Project Setup
- [ ] Day 5: Spring Boot - First REST Endpoint

## 🔐 Key Features (In Development)

- **Authentication:** OAuth 2.0 with JWT tokens
- **Authorization:** Role-based access control (RBAC)
- **Encryption:** TLS 1.3 in transit, AES-256 at rest
- **Idempotency:** Safe retry of financial operations
- **Rate Limiting:** 100 requests/minute per user
- **Audit Logging:** Complete compliance trail

## 📄 License

MIT License - see [LICENSE](LICENSE) file

---

**Built with ❤️ for learning enterprise fintech development**

*Preparing for Backend Engineer role at BNP Paribas*
