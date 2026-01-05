import { api, setCurrentUser, toastError, toastSuccess, toggleTheme } from "./common.js";

const form = document.getElementById("login-form");
const emailInput = document.getElementById("email");
const passwordInput = document.getElementById("password");
const errorMessage = document.getElementById("login-error");

const themeToggle = document.getElementById("theme-toggle");
themeToggle?.addEventListener("click", () => {
    toggleTheme();
});

const DEMO_USERS = [
    { email: "admin@smartfold.lk", password: "admin123", role: "ADMIN", name: "Admin", token: "demo-admin-token" },
    { email: "customer@test.lk", password: "pass123", role: "CUSTOMER", name: "Test Customer", token: "demo-customer-token" },
    { email: "nimali@smartfold.lk", password: "pass123", role: "CUSTOMER", name: "Nimali Jayasinghe", token: "demo-nimali-token" },
    { email: "ruwan@smartfold.lk", password: "pass123", role: "CUSTOMER", name: "Ruwan Perera", token: "demo-ruwan-token" },
    { email: "laundry@smartfold.lk", password: "staff123", role: "LAUNDRY_STAFF", name: "Saman Silva", token: "demo-laundry-token" },
    { email: "delivery@smartfold.lk", password: "staff123", role: "DELIVERY_STAFF", name: "Ishara Kumara", token: "demo-delivery-token" },
];

function validate() {
    let valid = true;
    errorMessage.style.display = "none";
    [emailInput, passwordInput].forEach((input) => {
        const helper = document.querySelector(`.helper-text[data-for="${input.id}"]`);
        if (!input.value) {
            input.classList.add("error");
            helper.textContent = "This field is required";
            helper.style.display = "block";
            valid = false;
        } else {
            input.classList.remove("error");
            helper.style.display = "none";
        }
    });
    if (!valid) toastError("Please fill in all required fields");
    return valid;
}

form?.addEventListener("submit", async (e) => {
    e.preventDefault();
    if (!validate()) return;

    const payload = {
        email: emailInput.value.trim(),
        password: passwordInput.value.trim(),
    };

    try {
        const user = await api.post("/api/auth/login", payload);
        handleSuccess(user);
    } catch (error) {
        if (isNetworkError(error)) {
            const fallback = findDemoUser(payload.email, payload.password);
            if (fallback) {
                handleSuccess(fallback);
                toastSuccess("Demo mode activated. Backend unreachable.");
                return;
            }
        }
        showError(error.message || "Login failed");
    }
});

function isNetworkError(error) {
    return (
        error instanceof TypeError ||
        error.message === "Failed to fetch" ||
        (typeof error.message === "string" && error.message.includes("NetworkError"))
    );
}
function findDemoUser(email, password) {
    return DEMO_USERS.find((u) => u.email === email && u.password === password);
}

function handleSuccess(response) {
    // Handle both nested API response { token, user, message } and flat demo user objects
    const token = response.token ?? response.accessToken ?? "session-token";
    const userData = response.user ?? response; // Use nested user if available, otherwise treat as flat

    const normalizedUser = {
        id: userData.id,
        name: userData.name,
        email: userData.email,
        role: (userData.role ?? "USER").toString().toUpperCase()
    };
    setCurrentUser(normalizedUser, token);

    toastSuccess(`Welcome back, ${normalizedUser.name ?? normalizedUser.email}!`);
    setTimeout(() => {
        // Redirection Logic
        switch (normalizedUser.role) {
            case "ADMIN":
            case "LAUNDRY_STAFF":
            case "FINANCE_STAFF":
            case "CUSTOMER_SERVICE":
                window.location.href = "./dashboard-admin.html";
                break;
            case "DELIVERY_STAFF":
            case "DRIVER":
            case "DELIVERY":
                window.location.href = "./dashboard-driver.html";
                break;
            default:
                // CUSTOMER and others
                window.location.href = "./dashboard-user.html";
        }
    }, 200);
}

function showError(msg) {
    errorMessage.textContent = msg;
    errorMessage.style.display = "block";
    toastError(msg);
}
