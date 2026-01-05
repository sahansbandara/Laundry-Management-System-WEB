<div align="center">

# SmartFold — Laundry Management System

### Enterprise-Grade Laundry Operations Platform

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](CONTRIBUTING.md)

**A comprehensive, production-ready web application for managing end-to-end laundry operations with role-based access control, real-time order tracking, and integrated payment processing.**

[Features](#key-features) • [Quick Start](#quick-start) • [API Documentation](#api-reference) • [Architecture](#architecture) • [Demo Credentials](#demo-credentials)

</div>

---

## Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Quick Start](#quick-start)
- [Demo Credentials](#demo-credentials)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [API Reference](#api-reference)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

---

## Overview

**SmartFold** is a full-stack laundry management system designed to streamline operations for laundry businesses of any size. The platform provides distinct portals for customers, administrators, and staff members, each with tailored functionality to optimize workflow efficiency.

### Why SmartFold?

- **Complete Business Solution** — Handles orders, payments, inventory, staff tasks, and customer communications
- **Role-Based Access** — Separate interfaces for customers, admins, and staff with appropriate permissions
- **Modern Tech Stack** — Built with Spring Boot 3.x, JWT authentication, and responsive vanilla JavaScript
- **Production Ready** — Includes security, validation, error handling, and database migration support
- **Easy Deployment** — Single JAR deployment with embedded Tomcat server

---

## Key Features

### Customer Portal

<details>
<summary><b>Order Management</b></summary>

- **Service Selection**: Choose from multiple service types:
  - Wash & Fold
  - Dry Cleaning
  - Iron & Press
  - Express Service
- **Custom Pricing**: Dynamic pricing based on weight, service type, and active promotions
- **Order History**: View all past and current orders with detailed status
- **Invoice Generation**: Automatic invoice creation with itemized breakdown
</details>

<details>
<summary><b>Real-Time Order Tracking</b></summary>

- **Visual Timeline**: Interactive progress tracker showing:
  - Order Placed
  - Pickup Scheduled
  - In Progress (Washing/Cleaning)
  - Quality Check
  - Out for Delivery
  - Delivered
- **SMS/Email Notifications**: Status updates at each milestone (configurable)
- **Estimated Delivery**: Real-time ETA calculations
</details>

<details>
<summary><b>Subscription Plans</b></summary>

- **Recurring Services**: Weekly, Bi-Weekly, and Monthly plans
- **Automatic Scheduling**: Set-and-forget pickup/delivery automation
- **Discounted Rates**: Subscription members receive 10-20% off regular pricing
- **Flexible Management**: Pause, resume, or cancel subscriptions anytime
</details>

<details>
<summary><b>Reviews & Ratings</b></summary>

- **Public Testimonials**: Submit star ratings and written reviews
- **Service Feedback**: Rate specific aspects (speed, quality, customer service)
- **Photo Uploads**: Attach before/after images (optional)
</details>

<details>
<summary><b>In-App Messaging</b></summary>

- **Direct Support**: Chat with customer service representatives
- **Order-Specific Queries**: Link messages to specific orders
- **File Attachments**: Send images for stain/damage documentation
</details>

---

### Admin Console

<details>
<summary><b>Analytics Dashboard</b></summary>

- **Revenue Metrics**: Daily, weekly, monthly revenue charts (Chart.js)
- **Order Volume**: Track order trends and peak periods
- **Customer Growth**: New registrations and retention rates
- **Service Popularity**: Most requested services and pricing optimization
- **Financial Reports**: Paid vs. unpaid invoices, outstanding balances
</details>

<details>
<summary><b>Order Processing Workflow</b></summary>

- **Status Management**: Update orders through workflow stages:
  - `PENDING` → `CONFIRMED` → `PICKED_UP` → `IN_PROGRESS` → `READY` → `OUT_FOR_DELIVERY` → `DELIVERED`
- **Bulk Actions**: Process multiple orders simultaneously
- **Priority Flagging**: Mark urgent/express orders
- **Staff Assignment**: Assign orders to specific staff members
</details>

<details>
<summary><b>Inventory Management</b></summary>

- **Stock Tracking**: Monitor detergents, fabric softeners, hangers, packaging materials
- **Low-Stock Alerts**: Automatic notifications when supplies run low
- **Usage Analytics**: Track consumption patterns per service type
- **Supplier Management**: Maintain supplier contacts and reorder points
</details>

<details>
<summary><b>Promotional Campaigns</b></summary>

- **Promo Code Creation**: Generate discount codes with:
  - Fixed amount or percentage discounts
  - Expiration dates
  - Usage limits (per customer or total)
  - Minimum order requirements
- **Campaign Tracking**: Monitor redemption rates and ROI
</details>

<details>
<summary><b>User Management (CRM)</b></summary>

- **Customer Profiles**: View complete customer history and preferences
- **Role Assignment**: Manage admin, staff, driver, and customer roles
- **Account Status**: Enable/disable user accounts
- **Communication Logs**: Track all customer interactions
</details>

---

### Staff Portals

<details>
<summary><b>Task Management</b></summary>

- **Daily Task List**: View assigned pickups and deliveries
- **Route Optimization**: Addresses sorted by proximity (Google Maps integration ready)
- **Customer Details**: Access phone numbers and delivery instructions
- **Status Updates**: Mark tasks as:
  - `ASSIGNED` → `IN_TRANSIT` → `COMPLETED` → `FAILED` (with reason)
</details>

<details>
<summary><b>Delivery Confirmation</b></summary>

- **Photo Proof**: Capture delivery confirmation photos
- **Signature Collection**: Digital signature capture (optional)
- **Failed Delivery Handling**: Log reasons and reschedule attempts
</details>

---

### Design & User Experience

- **Modern UI**: Clean, glassmorphism-inspired design with smooth animations
- **Dark Mode**: System-wide theme toggle with localStorage persistence
- **Fully Responsive**: Optimized layouts for mobile (320px+), tablet, and desktop
- **Accessibility**: WCAG 2.1 AA compliant with keyboard navigation support
- **Performance**: Lazy loading, debounced searches, optimized API calls

---

## Quick Start

### Prerequisites

Before running the application, ensure you have:

- **Java Development Kit (JDK) 17** or higher ([Download](https://adoptium.net/))
- **Gradle** (wrapper included, no separate installation needed)
- **Git** for cloning the repository

### Installation Steps

1. **Clone the Repository**
   ```bash
   git clone https://github.com/sahansandaruwan/Laundry-Management-System-WEB.git
   cd Laundry-Management-System-WEB
   ```

2. **Run the Application**
   ```bash
   # Using Gradle Wrapper (default port 8080)
   ./gradlew bootRun
   
   # Or run on custom port (e.g., 8081)
   ./gradlew bootRun --args='--server.port=8081'
   
   # On Windows
   gradlew.bat bootRun
   ```

3. **Access the Application**
   
   Open your browser and navigate to:
   ```
   http://localhost:8080
   ```
   
   Or if using custom port 8081:
   ```
   http://localhost:8081
   ```
   
   The application will automatically redirect to the login page.

### Alternative: Build and Run JAR

```bash
# Build the JAR file
./gradlew clean build

# Run the JAR (default port 8080)
java -jar build/libs/laundry-management-system-0.0.1-SNAPSHOT.jar

# Run on custom port
java -jar build/libs/laundry-management-system-0.0.1-SNAPSHOT.jar --server.port=8081
```

---

## Demo Credentials

Use these pre-configured accounts to explore different user roles:

| Role | Email | Password | Access Level |
|------|-------|----------|--------------|
| **Admin** | `admin@smartfold.lk` | `admin123` | Full system access: Dashboard, Orders, Users, Inventory, Settings |
| **Customer** | `nimali@smartfold.lk` | `pass123` | Place orders, Track deliveries, Manage subscriptions |
| **Customer** | `ruwan@smartfold.lk` | `pass123` | Place orders, Track deliveries, Manage subscriptions |
| **Customer** | `kamal@smartfold.lk` | `pass123` | Place orders, Track deliveries, Manage subscriptions |
| **Customer** | `customer@test.lk` | `pass123` | Place orders, Track deliveries, Manage subscriptions |
| **Laundry Staff** | `laundry@smartfold.lk` | `staff123` | Manage orders, Update order status, Process laundry |
| **Delivery Staff** | `delivery@smartfold.lk` | `staff123` | View delivery tasks, Update delivery status, Route management |
| **Finance Staff** | `finance@smartfold.lk` | `staff123` | Manage payments, Generate invoices, Financial reports |
| **Customer Service** | `support@smartfold.lk` | `staff123` | Handle customer inquiries, Manage messages, Support chat |

**Important Notes:**
- The application uses an **in-memory H2 database** by default. All data resets when the server restarts.
- For production use, configure a persistent database (MySQL/PostgreSQL) in `application.properties`.
- Change default passwords before deploying to production.
- Default port is **8080**. To run on a different port (e.g., 8081), use: `./gradlew bootRun --args='--server.port=8081'`

---

## Architecture

SmartFold follows a **monolithic Spring Boot architecture** with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                     Client Browser                          │
│  (HTML5 + Vanilla JavaScript + CSS3)                        │
└────────────────────┬────────────────────────────────────────┘
                     │ HTTP/HTTPS
                     │ REST API (JSON)
┌────────────────────▼────────────────────────────────────────┐
│              Spring Boot Application                        │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Controllers (REST API Endpoints)                    │  │
│  │  - AuthController, OrderController, etc.             │  │
│  └────────────────────┬─────────────────────────────────┘  │
│                       │                                     │
│  ┌────────────────────▼─────────────────────────────────┐  │
│  │  Service Layer (Business Logic)                      │  │
│  │  - Order processing, Payment handling, etc.          │  │
│  └────────────────────┬─────────────────────────────────┘  │
│                       │                                     │
│  ┌────────────────────▼─────────────────────────────────┐  │
│  │  Repository Layer (Data Access)                      │  │
│  │  - Spring Data JPA Repositories                      │  │
│  └────────────────────┬─────────────────────────────────┘  │
│                       │                                     │
│  ┌────────────────────▼─────────────────────────────────┐  │
│  │  Security Layer                                       │  │
│  │  - JWT Authentication                                 │  │
│  │  - Role-based Authorization                           │  │
│  └───────────────────────────────────────────────────────┘  │
└────────────────────┬────────────────────────────────────────┘
                     │ JDBC
┌────────────────────▼────────────────────────────────────────┐
│              Database (H2 / MySQL / PostgreSQL)             │
└─────────────────────────────────────────────────────────────┘
```

### Key Architectural Decisions

- **Monolithic Design**: Single deployable unit for simplified operations
- **RESTful API**: Stateless API design with JWT token authentication
- **Frontend-Backend Separation**: Static files served by Spring Boot, API consumed via fetch
- **JPA/Hibernate**: Object-relational mapping for database abstraction
- **Spring Security**: Comprehensive security with CORS configuration

---

## Technology Stack

### Backend

| Technology | Version | Purpose |
|------------|---------|---------|
| **Spring Boot** | 3.5.6 | Application framework |
| **Spring Security** | 6.x | Authentication & authorization |
| **Spring Data JPA** | 3.x | Database abstraction layer |
| **Hibernate** | 6.x | ORM implementation |
| **JWT (JJWT)** | 0.12.6 | Token-based authentication |
| **Lombok** | Latest | Boilerplate code reduction |
| **SpringDoc OpenAPI** | 2.6.0 | API documentation (Swagger UI) |
| **Bean Validation** | 3.x | Request validation |

### Database

| Database | Environment | Notes |
|----------|-------------|-------|
| **H2** | Development | In-memory, auto-configured |
| **MySQL** | Production | Recommended for production |
| **PostgreSQL** | Production | Alternative production option |

### Frontend

| Technology | Purpose |
|------------|---------|
| **HTML5** | Semantic markup |
| **CSS3** | Styling with CSS variables, flexbox, grid |
| **Vanilla JavaScript (ES6+)** | Client-side logic, API consumption |
| **Chart.js** | Analytics visualizations |
| **Fetch API** | HTTP requests |

### Build & Deployment

- **Gradle 8.x**: Build automation and dependency management
- **Embedded Tomcat**: No external server required
- **JAR Packaging**: Single executable artifact

---

## API Reference

### Authentication Endpoints

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| `POST` | `/api/auth/register` | Register new customer | `{ name, email, password }` | `{ token, user, message }` |
| `POST` | `/api/auth/login` | Authenticate user | `{ email, password }` | `{ token, user, message }` |
| `GET` | `/api/auth/me` | Get current user info | - | `{ id, name, email, role }` |

**Headers Required**: `Authorization: Bearer <token>` (except register/login)

---

### Order Management Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `GET` | `/api/orders` | List all orders (filtered by role) | All authenticated users |
| `GET` | `/api/orders/{id}` | Get order details | Owner or Admin |
| `POST` | `/api/orders` | Create new order | Customer |
| `PUT` | `/api/orders/{id}/status` | Update order status | Admin, Staff |
| `DELETE` | `/api/orders/{id}` | Cancel order | Customer (if pending), Admin |

**Example Request (Create Order)**:
```json
POST /api/orders
{
  "serviceType": "WASH_AND_FOLD",
  "weight": 5.5,
  "pickupAddress": "123 Main St, Colombo",
  "pickupDate": "2026-01-10T10:00:00",
  "specialInstructions": "Handle delicate fabrics with care"
}
```

---

### Payment Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `GET` | `/api/payments` | List all payments | Admin |
| `GET` | `/api/payments/user/{userId}` | Get user payments | Owner or Admin |
| `POST` | `/api/payments` | Process payment | Customer |
| `GET` | `/api/payments/invoice/{orderId}` | Generate invoice | Owner or Admin |

---

### Admin Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `GET` | `/api/admin/dashboard` | Get dashboard metrics | Admin |
| `GET` | `/api/admin/users` | List all users | Admin |
| `PUT` | `/api/admin/users/{id}/role` | Update user role | Admin |
| `GET` | `/api/admin/inventory` | Get inventory status | Admin, Staff |
| `POST` | `/api/admin/inventory` | Add inventory item | Admin |
| `PUT` | `/api/admin/inventory/{id}` | Update inventory | Admin, Staff |
| `POST` | `/api/admin/promos` | Create promo code | Admin |
| `GET` | `/api/admin/promos` | List promo codes | Admin |

---

### Delivery/Task Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `GET` | `/api/delivery/tasks` | Get assigned tasks | Driver |
| `PUT` | `/api/delivery/tasks/{id}/status` | Update task status | Driver |
| `POST` | `/api/delivery/tasks/{id}/complete` | Mark task complete | Driver |

---

### Messaging Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `GET` | `/api/messages` | Get user messages | Authenticated |
| `POST` | `/api/messages` | Send message | Authenticated |
| `PUT` | `/api/messages/{id}/read` | Mark as read | Recipient |

---

### Service Catalog Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `GET` | `/api/catalog/services` | List available services | Public |
| `GET` | `/api/catalog/pricing` | Get pricing information | Public |

---

### API Documentation (Swagger UI)

Access interactive API documentation at:
```
http://localhost:8080/swagger-ui.html
```

---

## Project Structure

```
Laundry-Management-System-WEB/
├── src/
│   ├── main/
│   │   ├── java/com/laundry/lms/
│   │   │   ├── controller/          # REST API Controllers
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── OrderController.java
│   │   │   │   ├── AdminController.java
│   │   │   │   ├── PaymentController.java
│   │   │   │   ├── DeliveryController.java
│   │   │   │   ├── MessageController.java
│   │   │   │   └── ... (17 controllers total)
│   │   │   ├── model/               # JPA Entities
│   │   │   │   ├── User.java
│   │   │   │   ├── LaundryOrder.java
│   │   │   │   ├── OrderItem.java
│   │   │   │   ├── Payment.java
│   │   │   │   ├── DeliveryJob.java
│   │   │   │   ├── Invoice.java
│   │   │   │   ├── ServiceCatalog.java
│   │   │   │   └── ...
│   │   │   ├── repository/          # Spring Data JPA Repositories
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── LaundryOrderRepository.java
│   │   │   │   └── ...
│   │   │   ├── service/             # Business Logic Layer
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   ├── OrderUpdateRequest.java
│   │   │   │   └── ...
│   │   │   ├── security/            # Security Configuration
│   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── ...
│   │   │   ├── config/              # Application Configuration
│   │   │   └── LaundryManagementSystemApplication.java
│   │   └── resources/
│   │       ├── static/              # Frontend Assets
│   │       │   ├── css/
│   │       │   │   └── style.css    # Main stylesheet
│   │       │   ├── js/
│   │       │   │   ├── common.js    # Shared utilities, API client
│   │       │   │   ├── admin.js     # Admin dashboard logic
│   │       │   │   ├── user.js      # Customer dashboard logic
│   │       │   │   ├── driver.js    # Driver portal logic
│   │       │   │   ├── track-order.js
│   │       │   │   └── reviews.js
│   │       │   ├── index.html       # Landing page
│   │       │   ├── login.html       # Login page
│   │       │   ├── register.html    # Registration page
│   │       │   ├── dashboard-admin.html
│   │       │   ├── dashboard-user.html
│   │       │   ├── dashboard-driver.html
│   │       │   ├── place-order.html
│   │       │   ├── track-order.html
│   │       │   ├── pay.html
│   │       │   ├── reviews.html
│   │       │   ├── subscriptions.html
│   │       │   └── demo-checkout.html
│   │       └── application.properties  # Spring Boot Configuration
│   └── test/                        # Unit and Integration Tests
├── build.gradle                     # Gradle Build Configuration
├── settings.gradle
├── gradlew                          # Gradle Wrapper (Unix)
├── gradlew.bat                      # Gradle Wrapper (Windows)
├── pom.xml                          # Maven POM (alternative)
├── README.md
└── LICENSE
```

---

## Configuration

### Application Properties

Edit `src/main/resources/application.properties` to configure:

#### Server Configuration
```properties
# Server port (default: 8080)
server.port=8080

# Application name
spring.application.name=SmartFold Laundry System
```

#### Database Configuration

**H2 (Development)**:
```properties
spring.datasource.url=jdbc:h2:mem:laundrydb
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

**MySQL (Production)**:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/laundry_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
```

#### JWT Configuration
```properties
# JWT secret key (change in production!)
jwt.secret=your-256-bit-secret-key-change-this-in-production
jwt.expiration=86400000  # 24 hours in milliseconds
```

#### CORS Configuration
```properties
# Allowed origins (adjust for production)
cors.allowed-origins=http://localhost:3000,http://localhost:8080
```

---

## Troubleshooting

### Common Issues

<details>
<summary><b>Port 8080 Already in Use</b></summary>

**Error**: `Web server failed to start. Port 8080 was already in use.`

**Solution**:
```bash
# Option 1: Run on a different port
./gradlew bootRun --args='--server.port=8081'

# Option 2: Kill the process using port 8080
# On macOS/Linux:
lsof -ti:8080 | xargs kill -9

# On Windows:
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```
</details>

<details>
<summary><b>CORS Errors in Browser Console</b></summary>

**Error**: `Access to fetch at 'http://localhost:8080/api/...' has been blocked by CORS policy`

**Solution**: Ensure `@CrossOrigin("*")` is present on controllers, or configure global CORS in `SecurityConfig.java`:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```
</details>

<details>
<summary><b>401 Unauthorized on API Calls</b></summary>

**Error**: API returns `401 Unauthorized` even with valid credentials

**Checklist**:
1. Verify JWT token is stored in localStorage: `localStorage.getItem('token')`
2. Check Authorization header format: `Bearer <token>`
3. Ensure token hasn't expired (default: 24 hours)
4. Verify user exists in database with correct role
5. Check browser console for token validation errors
</details>

<details>
<summary><b>Database Connection Failed</b></summary>

**Error**: `Failed to configure a DataSource`

**Solution**:
- For H2: Ensure `spring.datasource.url=jdbc:h2:mem:laundrydb` in `application.properties`
- For MySQL: Verify MySQL server is running and credentials are correct
- Check database exists: `CREATE DATABASE laundry_db;`
</details>

<details>
<summary><b>Gradle Build Fails</b></summary>

**Error**: `Could not resolve dependencies` or `Compilation failed`

**Solution**:
```bash
# Clean build cache
./gradlew clean

# Rebuild with dependency refresh
./gradlew build --refresh-dependencies

# Check Java version
java -version  # Should be 17 or higher
```
</details>

---

## Contributing

We welcome contributions from the community! Here's how you can help:

### Development Workflow

1. **Fork the Repository**
   ```bash
   # Click "Fork" on GitHub, then clone your fork
   git clone https://github.com/YOUR_USERNAME/Laundry-Management-System-WEB.git
   cd Laundry-Management-System-WEB
   ```

2. **Create a Feature Branch**
   ```bash
   git checkout -b feature/amazing-new-feature
   ```

3. **Make Your Changes**
   - Follow existing code style and conventions
   - Add unit tests for new functionality
   - Update documentation as needed

4. **Test Your Changes**
   ```bash
   ./gradlew test
   ./gradlew bootRun  # Manual testing
   ```

5. **Commit and Push**
   ```bash
   git add .
   git commit -m "feat: add amazing new feature"
   git push origin feature/amazing-new-feature
   ```

6. **Open a Pull Request**
   - Go to the original repository on GitHub
   - Click "New Pull Request"
   - Provide a clear description of your changes

### Code Style Guidelines

- **Java**: Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- **JavaScript**: Use ES6+ features, avoid `var`, prefer `const` over `let`
- **Commit Messages**: Follow [Conventional Commits](https://www.conventionalcommits.org/)
  - `feat:` New features
  - `fix:` Bug fixes
  - `docs:` Documentation changes
  - `refactor:` Code refactoring
  - `test:` Test additions/modifications

---

## License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2026 SmartFold Laundry Management System

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```

---

## Support & Contact

- **Issues**: [GitHub Issues](https://github.com/sahansandaruwan/Laundry-Management-System-WEB/issues)
- **Discussions**: [GitHub Discussions](https://github.com/sahansandaruwan/Laundry-Management-System-WEB/discussions)
- **Email**: support@smartfold.lk

---

<div align="center">

**Built with ❤️ by the SmartFold Team**

⭐ Star this repository if you find it helpful!

</div>
