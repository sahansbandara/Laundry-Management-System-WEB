# SmartFold — Laundry Management System

A comprehensive web application for managing laundry operations, built with Spring Boot and a modern vanilla JavaScript frontend.

## Technology Stack

| Layer      | Technology                          |
|------------|-------------------------------------|
| Backend    | Spring Boot 3, Java 17+             |
| Database   | H2 (in-memory, development)         |
| Frontend   | HTML5, CSS3, Vanilla JavaScript     |
| Charts     | Chart.js                            |

## Features

### Customer Portal
- Place laundry orders with service selection and scheduling
- Real-time order tracking with visual timeline
- Order history and status updates
- Subscription plans (Weekly, Bi-Weekly, Monthly)
- Apply promotional codes at checkout
- Contact support via in-app messaging
- Rate and review completed orders

### Admin Console
- Dashboard with KPIs and analytics charts
- Order management and status updates
- Service catalog configuration
- User management
- Inventory tracking with low-stock alerts
- Promotional code management (fixed + percentage discounts)
- Customer communication center

### Driver Portal
- Daily task list (pickups and deliveries)
- Mark tasks as in-progress or completed
- View customer details and addresses

### Design & UX
- Modern, responsive interface with glassmorphism aesthetics
- Dark mode support (system-wide toggle)
- Toast notifications and form validation
- Smooth animations and micro-interactions

## Getting Started

### Prerequisites
- Java 17 or higher (for backend)
- Gradle (wrapper included)

### Running the Application

**Full Stack (Recommended)**
```bash
./gradlew bootRun
```
Access the application at `http://localhost:8080`

**Frontend Preview (No Java Required)**
Open any of the following files directly in your browser:
- `src/main/resources/static/login.html` — Login page
- `src/main/resources/static/track-order.html` — Order tracking (try IDs: 1001, 1002, 1003)
- `src/main/resources/static/reviews.html` — Public reviews
- `src/main/resources/static/subscriptions.html` — Subscription plans
- `src/main/resources/static/dashboard-driver.html` — Driver portal

The application operates in demo mode with simulated data when the backend is unavailable.

### Database Console
When the backend is running, access the H2 console:
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:lms`
- Username: `sa`
- Password: *(leave blank)*

## Demo Credentials

| Role     | Email                  | Password |
|----------|------------------------|----------|
| Admin    | admin@smartfold.lk     | 1234     |
| Customer | nimal@smartfold.lk     | 1234     |
| Customer | ruwan@smartfold.lk     | 1234     |

## Project Structure

```
src/main/resources/static/
├── css/
│   └── style.css               # Main stylesheet with theme variables
├── js/
│   ├── common.js               # Shared utilities and API client
│   ├── admin.js                # Admin dashboard logic
│   ├── user.js                 # Customer portal logic
│   ├── driver.js               # Driver portal logic
│   ├── track-order.js          # Order tracking timeline
│   ├── reviews.js              # Public reviews page
│   ├── login.js                # Authentication handling
│   ├── register.js             # User registration
│   ├── place-order.js          # Order creation wizard
│   └── pay.js                  # Payment flow
├── dashboard-admin.html        # Admin console
├── dashboard-user.html         # Customer portal
├── dashboard-driver.html       # Driver portal
├── track-order.html            # Order tracking page
├── reviews.html                # Public reviews
├── subscriptions.html          # Subscription plans
├── login.html                  # Authentication page
├── register.html               # Registration page
├── place-order.html            # Order creation
└── pay.html                    # Payment method selection
```

## New Features (v2.0)

| Feature | Description |
|---------|-------------|
| Order Tracking | Visual timeline showing order progress through all stages |
| Subscriptions | Weekly, Bi-Weekly, Monthly plans with discounts |
| Reviews | Public customer reviews with star ratings |
| Driver Portal | Task management for pickup/delivery drivers |
| Promo Codes | Fixed and percentage-based discount codes |
| Inventory | Stock tracking with low-stock alerts |
| Dark Mode | System-wide theme toggle |
| Analytics | Chart.js visualizations for orders and revenue |

## Roadmap

The following features require backend implementation:
- Email/SMS Notifications (SendGrid, Twilio)
- Multi-Branch Support with MongoDB

## API Endpoints

| Method | Endpoint                     | Description              |
|--------|------------------------------|--------------------------|
| POST   | /api/auth/login              | User authentication      |
| POST   | /api/auth/register           | User registration        |
| GET    | /api/orders                  | List orders              |
| POST   | /api/orders                  | Create order             |
| PUT    | /api/orders/{id}/status      | Update order status      |
| GET    | /api/services                | List services            |
| POST   | /api/payments/checkout       | Initiate payment         |
| GET    | /api/reviews/public          | Get public reviews       |
| GET    | /api/delivery-tasks/today    | Get driver tasks         |

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/new-feature`)
3. Commit your changes (`git commit -m 'Add new feature'`)
4. Push to the branch (`git push origin feature/new-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License.
