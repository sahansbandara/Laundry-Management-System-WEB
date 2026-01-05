export const API_BASE = window.API_BASE || "";
const toastContainerId = "toast-container";
const AUTH_STORAGE_KEY = "smartfold_auth";

/* ---------- Toasts ---------- */
function ensureToastContainer() {
    let c = document.getElementById(toastContainerId);
    if (!c) {
        c = document.createElement("div");
        c.id = toastContainerId;
        c.className = "toast-container";
        document.body.appendChild(c);
    }
    return c;
}
function showToast(message, type = "success") {
    const c = ensureToastContainer();
    const t = document.createElement("div");
    t.className = `toast ${type}`;
    t.innerText = message;
    c.appendChild(t);
    setTimeout(() => t.remove(), 3500);
}
export const toastSuccess = (m) => showToast(m, "success");
export const toastError = (m) => showToast(m, "error");

/* ---------- Auth Storage ---------- */
export function saveAuth(auth = {}) {
    try {
        localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(auth));
    } catch (e) {
        console.error("Failed to persist auth", e);
    }
}
export function getAuth() {
    const raw = localStorage.getItem(AUTH_STORAGE_KEY);
    if (!raw) return null;
    try {
        return JSON.parse(raw);
    } catch {
        return null;
    }
}
export function clearAuth() {
    localStorage.removeItem(AUTH_STORAGE_KEY);
    localStorage.removeItem("user");
}
export function requireAuthOrRedirect() {
    const auth = getAuth();
    if (!auth || !auth.token) {
        window.location.href = "./login.html";
        return null;
    }
    return auth;
}

/* ---------- Fetch helpers ---------- */
async function parseResponse(response) {
    const text = await response.text();
    const data = text ? JSON.parse(text) : null;
    if (!response.ok) {
        const msg = data?.error || data?.message || response.statusText || "Something went wrong";
        throw new Error(msg);
    }
    return data;
}

function buildHeaders(base = {}) {
    const auth = getAuth();
    const headers = { ...base };
    if (auth?.token) headers.Authorization = `Bearer ${auth.token}`;
    return headers;
}

export const api = {
    async get(path) {
        const res = await fetch(`${API_BASE}${path}`, {
            method: "GET",
            headers: buildHeaders(),
            credentials: "include",
        });
        return parseResponse(res);
    },
    async post(path, body) {
        const res = await fetch(`${API_BASE}${path}`, {
            method: "POST",
            headers: buildHeaders({ "Content-Type": "application/json" }),
            body: JSON.stringify(body ?? {}),
            credentials: "include",
        });
        return parseResponse(res);
    },
    async patch(path, body) {
        const isJsonBody = body !== undefined && !(body instanceof FormData);
        const res = await fetch(`${API_BASE}${path}`, {
            method: "PATCH",
            headers: buildHeaders(isJsonBody ? { "Content-Type": "application/json" } : {}),
            body: isJsonBody ? JSON.stringify(body) : body,
            credentials: "include",
        });
        return parseResponse(res);
    },
    async del(path) {
        const res = await fetch(`${API_BASE}${path}`, {
            method: "DELETE",
            headers: buildHeaders(),
            credentials: "include",
        });
        return parseResponse(res);
    },
};

/* ---------- Auth utils ---------- */
export function getCurrentUser() {
    const auth = getAuth();
    return auth?.user ?? null;
}
export function setCurrentUser(user, token) {
    saveAuth({ token: token ?? user?.token ?? null, user });
}
export function clearCurrentUser() {
    clearAuth();
}
export function requireAuth(allowedRoles) {
    const auth = requireAuthOrRedirect();
    const user = auth?.user;
    if (!user) return null;

    // Convert single role to array for consistent handling
    const roles = Array.isArray(allowedRoles) ? allowedRoles : (allowedRoles ? [allowedRoles] : []);

    // If specific roles are required, check if user has one of them
    if (roles.length > 0 && !roles.includes(user.role)) {
        // Redirect based on user's actual role
        window.location.href = user.role === "ADMIN" ? "./dashboard-admin.html" : "./dashboard-user.html";
        return null;
    }
    return user;
}

/* ---------- Helpers ---------- */
export async function loadServiceOptions(selectEl) {
    if (!selectEl) return;
    try {
        const services = await api.get("/api/catalog/services");
        selectEl.innerHTML =
            `<option value="">Select service</option>` +
            services.map((s) => `<option value="${s}">${s}</option>`).join("");
    } catch (e) {
        toastError(e.message);
    }
}
export async function loadUnitOptions(selectEl) {
    if (!selectEl) return;
    try {
        const units = await api.get("/api/catalog/units");
        selectEl.innerHTML =
            `<option value="">Select unit</option>` +
            units.map((u) => `<option value="${u}">${u}</option>`).join("");
    } catch (e) {
        toastError(e.message);
    }
}
export function renderStatusBadge(v) {
    return `<span class="badge status-${v}">${v.replace(/_/g, " ")}</span>`;
}
export function confirmAction(message) {
    return window.confirm(message);
}

/* ---------- Theme ---------- */
export function initTheme() {
    const saved = localStorage.getItem("theme");
    const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
    if (saved === "dark" || (!saved && prefersDark)) {
        document.documentElement.setAttribute("data-theme", "dark");
    } else {
        document.documentElement.removeAttribute("data-theme");
    }
}
export function toggleTheme() {
    const current = document.documentElement.getAttribute("data-theme");
    const next = current === "dark" ? "light" : "dark";
    if (next === "dark") {
        document.documentElement.setAttribute("data-theme", "dark");
    } else {
        document.documentElement.removeAttribute("data-theme");
    }
    localStorage.setItem("theme", next);
}

// Auto-init theme
/* ---------- Scroll Reveal ---------- */
export function initScrollReveal() {
    // 1. Auto-tag common elements if they don't have the class yet
    const autoTargets = document.querySelectorAll("section, .card, .card-soft, .table-wrapper, .kpi-row");
    autoTargets.forEach((el, index) => {
        if (!el.classList.contains("reveal-on-scroll")) {
            el.classList.add("reveal-on-scroll");
            // Add slight stagger for items already in viewport or in sequence
            if (index < 5) el.style.transitionDelay = `${index * 0.1}s`;
        }
    });

    // 2. Set up Observer
    const observer = new IntersectionObserver((entries) => {
        entries.forEach((entry) => {
            if (entry.isIntersecting) {
                entry.target.classList.add("reveal--visible");
                observer.unobserve(entry.target); // Animate only once
            }
        });
    }, {
        threshold: 0.15, // Trigger when 15% visible
        rootMargin: "0px 0px -50px 0px" // Slightly offset from bottom
    });

    // 3. Observe all targets
    document.querySelectorAll(".reveal-on-scroll").forEach((el) => {
        observer.observe(el);
    });
}

// Auto-init theme & animations
initTheme();
// Use requestAnimationFrame to ensure DOM is ready for animations
requestAnimationFrame(() => {
    initScrollReveal();
});
