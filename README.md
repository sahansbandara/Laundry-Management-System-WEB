# SmartFold — Laundry Management System

**Stack:** Spring Boot + H2 + Vanilla HTML/CSS/JS

> **Note:** This project is designed to run with a Java backend. If you are viewing this in an environment without Java, you can still explore the **Frontend Experience** by opening the HTML files directly.

## Frontend Features (New)
- **Modern Design:** Premium aesthetic with glassmorphism, smooth animations, and responsive layout.
- **Dark Mode:** Fully supported dark theme across all pages.
- **Analytics:** Admin dashboard includes visual charts (Chart.js) for orders and revenue.
- **Improved UX:** Better error handling, toast notifications, and form validation.

## How to Run
### Full Stack (Requires Java 17+)
1. `./gradlew bootRun`
2. Open `http://localhost:8080` (redirects to login)

### Frontend Only (Preview)
1. Open `frontend/login.html` in your browser.
2. Use **Demo Credentials** below to simulate login (if backend is down, it will use fallback demo mode).

## Database (When Backend Running)
- Go to `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:lms`  |  User: `sa`  |  Password: *(blank)*

## Demo Credentials
- **Admin:** `admin@smartfold.lk` / `1234`
- **Customers:**
  - `nimal@smartfold.lk` / `1234`
  - `ruwan@smartfold.lk`  / `1234`

## Project Structure
- `src/main/resources/static/frontend/`: Contains all HTML, CSS, and JS files.
- `style.css`: Main stylesheet handling themes and components.
- `common.js`: core utilities and API handling.
