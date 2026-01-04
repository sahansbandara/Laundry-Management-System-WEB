# SmartFold — Laundry Management System

A comprehensive, enterprise-grade web application for managing laundry operations. Built with a robust Spring Boot backend and a modern, responsive vanilla JavaScript frontend.

![SmartFold Banner](https://via.placeholder.com/1200x400?text=SmartFold+Laundry+System)

## 🚀 Quick Start

### 1. Prerequisites
- **Java 17** or higher
- **Gradle** (wrapper included)

### 2. Run the Application
```bash
# Clone the repository
git clone https://github.com/sahansandaruwan/Laundry-Management-System-WEB.git

# Navigate to directory
cd Laundry-Management-System-WEB

# Run with Gradle
./gradlew bootRun
```

### 3. Access the Portal
Open your browser and navigate to:
**`http://localhost:8080`**

---

## 🔑 Login Credentials (Demo)

Use these credentials to explore the different user roles in the system.

| Role | Email | Password | Access Level |
|------|-------|----------|--------------|
| **Admin** | `admin@smartfold.lk` | `1234` | Full access to Dashboard, Orders, Users, Inventory, Settings |
| **Customer** | `nimali@smartfold.lk` | `1234` | Place orders, Track status, View history, Subscriptions |
| **Customer** | `ruwan@smartfold.lk` | `1234` | Place orders, Track status, View history, Subscriptions |
| **Customer** | `kamal@smartfold.lk` | `1234` | Place orders, Track status, View history, Subscriptions |

> **Note:** The application starts with an in-memory database (H2). All data resets when the application restarts.

---

## 🌟 Features

### Customer Portal
- **Order Management**: Place laundry orders with custom service selection (Wash & Fold, Dry Cleaning, etc.).
- **Real-time Tracking**: Visual timeline showing order progress from Pickup to Delivery.
- **Subscriptions**: Weekly, Bi-Weekly, and Monthly plans with automated scheduling and discounts.
- **Reviews**: Public star ratings and testimonials.
- **Support**: In-app messaging system for customer inquiries.

### Admin Console
- **Analytics Dashboard**: Visual charts (Chart.js) for revenue, order volume, and customer growth.
- **Order Processing**: Workflow management (Pending -> In Progress -> Ready -> Delivered).
- **Inventory Management**: Track detergents, hangers, and supplies with low-stock alerts.
- **Promo Codes**: Create and manage fixed or percentage-based discount codes.
- **User Management**: CRM features to view and manage customer profiles.

### Driver Portal
- **Task List**: Daily delivery and pickup usage.
- **Route Management**: View customer addresses and contact details.
- **Status Updates**: Mark tasks as "In Transit" or "Completed" in real-time.

### Design & UX
- **Modern UI**: Clean, glassmorphism-inspired aesthetic.
- **Dark Mode**: System-wide theme toggle.
- **Responsive**: Fully optimized for mobile, tablet, and desktop.

---

## 🛠 Project Structure

The project follows a standard Spring Boot structure with a clean separation of concerns.

```
src/main/resources/static/
├── css/
│   └── style.css               # centralized stylesheet with CSS variables
├── js/
│   ├── common.js               # API client, auth logic, theme manager
│   ├── admin.js                # Admin dashboard controller
│   ├── user.js                 # Customer dashboard controller
│   ├── driver.js               # Driver portal controller
│   ├── track-order.js          # Tracking timeline logic
│   └── reviews.js              # Reviews component
├── dashboard-admin.html        # Main admin interface
├── dashboard-user.html         # Main customer interface
├── dashboard-driver.html       # Driver task view
├── track-order.html            # Public tracking page
├── reviews.html                # Public reviews page
└── subscriptions.html          # Subscription landing page
```

---

## 🔧 Technology Stack

- **Backend**: Spring Boot 3.x, Spring Data JPA, Spring Security
- **Database**: H2 (In-Memory) for Dev / MySQL or PostgreSQL ready
- **Frontend**: HTML5, CSS3, Vanilla ES6 JavaScript (No framework complexity)
- **Visualization**: Chart.js for analytics
- **Build Tool**: Gradle

---

## 📡 API Endpoints

The backend exposes a RESTful API for all frontend interactions.

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/login` | Authenticate user and issue session |
| `GET` | `/api/orders` | Retrieve list of orders (filtered by role) |
| `POST` | `/api/orders` | Create a new laundry order |
| `PUT` | `/api/orders/{id}/status` | Update order workflow status |
| `GET` | `/api/inventory` | Get current stock levels |
| `POST` | `/api/promos` | Create new promotional code |

---

## 🤝 Contributing

We welcome contributions! Please follow these steps:
1. Fork the repository.
2. Create your feature branch: `git checkout -b feature/AmazingFeature`
3. Commit your changes: `git commit -m 'Add some AmazingFeature'`
4. Push to the branch: `git push origin feature/AmazingFeature`
5. Open a Pull Request.

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
