import { api, initTheme, toggleTheme, getCurrentUser, clearCurrentUser, toastSuccess, toastError } from "./common.js";

// Initialize theme
initTheme();

const themeToggle = document.getElementById("theme-toggle");
themeToggle?.addEventListener("click", toggleTheme);

// Logout
const logoutBtn = document.getElementById("logout-btn");
logoutBtn?.addEventListener("click", () => {
    clearCurrentUser();
    window.location.href = "/login.html";
});

// Elements
const elements = {
    driverName: document.getElementById("driver-name"),
    taskDate: document.getElementById("task-date"),
    taskList: document.getElementById("task-list"),
    emptyState: document.getElementById("empty-state"),
    filterStatus: document.getElementById("filter-status"),
    refreshBtn: document.getElementById("refresh-btn"),
    kpiPending: document.getElementById("kpi-pending"),
    kpiTransit: document.getElementById("kpi-transit"),
    kpiCompleted: document.getElementById("kpi-completed"),
};

// Demo data
const DEMO_TASKS = [
    {
        id: 1,
        orderId: 1001,
        type: "PICKUP",
        status: "PENDING",
        customerName: "Nimal Perera",
        phone: "+94 77 123 4567",
        address: "42 Galle Road, Colombo 03",
        scheduledTime: new Date(Date.now() + 3600000).toISOString(),
        notes: "Gate code: 1234",
    },
    {
        id: 2,
        orderId: 1002,
        type: "DELIVERY",
        status: "PENDING",
        customerName: "Ruwan Silva",
        phone: "+94 77 987 6543",
        address: "15 Temple Road, Nugegoda",
        scheduledTime: new Date(Date.now() + 7200000).toISOString(),
        notes: "",
    },
    {
        id: 3,
        orderId: 998,
        type: "PICKUP",
        status: "IN_TRANSIT",
        customerName: "Amara Fernando",
        phone: "+94 71 555 1234",
        address: "78 Station Road, Dehiwala",
        scheduledTime: new Date(Date.now() - 1800000).toISOString(),
        notes: "Call before arrival",
    },
    {
        id: 4,
        orderId: 995,
        type: "DELIVERY",
        status: "COMPLETED",
        customerName: "Kasun Jayawardena",
        phone: "+94 76 333 2211",
        address: "23 Park Avenue, Mount Lavinia",
        scheduledTime: new Date(Date.now() - 7200000).toISOString(),
        completedAt: new Date(Date.now() - 5400000).toISOString(),
        notes: "",
    },
    {
        id: 5,
        orderId: 990,
        type: "PICKUP",
        status: "COMPLETED",
        customerName: "Dilani Wijesuriya",
        phone: "+94 77 888 9999",
        address: "56 High Level Road, Maharagama",
        scheduledTime: new Date(Date.now() - 10800000).toISOString(),
        completedAt: new Date(Date.now() - 9000000).toISOString(),
        notes: "",
    },
];

let tasks = [...DEMO_TASKS];

const formatTime = (dateStr) => {
    if (!dateStr) return "—";
    const date = new Date(dateStr);
    return date.toLocaleTimeString("en-LK", { hour: "2-digit", minute: "2-digit" });
};

const formatDate = (dateStr) => {
    if (!dateStr) return "—";
    const date = new Date(dateStr);
    return date.toLocaleDateString("en-LK", { weekday: "long", month: "long", day: "numeric" });
};

const updateKPIs = () => {
    const pending = tasks.filter((t) => t.status === "PENDING").length;
    const transit = tasks.filter((t) => t.status === "IN_TRANSIT").length;
    const completed = tasks.filter((t) => t.status === "COMPLETED").length;

    elements.kpiPending.textContent = pending;
    elements.kpiTransit.textContent = transit;
    elements.kpiCompleted.textContent = completed;
};

const renderTasks = (filter = "ALL") => {
    const filtered = filter === "ALL" ? tasks : tasks.filter((t) => t.status === filter);

    // Sort: PENDING first, then IN_TRANSIT, then COMPLETED
    const statusOrder = { PENDING: 0, IN_TRANSIT: 1, COMPLETED: 2 };
    filtered.sort((a, b) => statusOrder[a.status] - statusOrder[b.status]);

    elements.taskList.innerHTML = "";

    if (filtered.length === 0) {
        elements.emptyState.hidden = false;
        return;
    }

    elements.emptyState.hidden = true;

    filtered.forEach((task) => {
        const card = document.createElement("div");
        card.className = `driver-task-card ${task.status === "COMPLETED" ? "completed" : ""}`;
        card.dataset.taskId = task.id;

        const typeClass = task.type === "PICKUP" ? "pickup" : "delivery";

        card.innerHTML = `
            <div class="task-type ${typeClass}">${task.type}</div>
            <div class="task-info">
                <h3>Order #${task.orderId}</h3>
                <p>${task.customerName} • ${task.phone}</p>
                <div class="address">${task.address}</div>
                ${task.notes ? `<p class="muted small-text" style="margin-top: 8px;">Note: ${task.notes}</p>` : ""}
            </div>
            <div class="task-time">
                <div class="scheduled">${formatTime(task.scheduledTime)}</div>
                ${task.status === "COMPLETED" ? `<div class="eta">Done at ${formatTime(task.completedAt)}</div>` : `<div class="eta">Scheduled</div>`}
            </div>
            <div class="task-actions">
                ${task.status === "PENDING" ? `<button class="btn-primary btn-pill start-btn" data-id="${task.id}">Start</button>` : ""}
                ${task.status === "IN_TRANSIT" ? `<button class="btn-primary btn-pill complete-btn" data-id="${task.id}">Complete</button>` : ""}
                ${task.status === "COMPLETED" ? `<span class="badge status-COMPLETED">Done</span>` : ""}
            </div>
        `;

        elements.taskList.appendChild(card);
    });

    // Event listeners for buttons
    document.querySelectorAll(".start-btn").forEach((btn) => {
        btn.addEventListener("click", () => startTask(Number(btn.dataset.id)));
    });

    document.querySelectorAll(".complete-btn").forEach((btn) => {
        btn.addEventListener("click", () => completeTask(Number(btn.dataset.id)));
    });
};

const startTask = async (taskId) => {
    const task = tasks.find((t) => t.id === taskId);
    if (!task) return;

    try {
        // Try API first
        await api.put(`/api/delivery-tasks/${taskId}/start`);
    } catch {
        // Fallback: update locally
    }

    task.status = "IN_TRANSIT";
    toastSuccess(`Started ${task.type.toLowerCase()} for Order #${task.orderId}`);
    updateKPIs();
    renderTasks(elements.filterStatus.value);
};

const completeTask = async (taskId) => {
    const task = tasks.find((t) => t.id === taskId);
    if (!task) return;

    try {
        // Try API first
        await api.put(`/api/delivery-tasks/${taskId}/complete`);
    } catch {
        // Fallback: update locally
    }

    task.status = "COMPLETED";
    task.completedAt = new Date().toISOString();
    toastSuccess(`Completed ${task.type.toLowerCase()} for Order #${task.orderId}`);
    updateKPIs();
    renderTasks(elements.filterStatus.value);
};

const fetchTasks = async () => {
    try {
        const data = await api.get("/api/delivery-tasks/today");
        if (Array.isArray(data) && data.length > 0) {
            tasks = data;
        }
    } catch {
        // Use demo data
    }

    updateKPIs();
    renderTasks(elements.filterStatus.value);
};

// Initialize
const user = getCurrentUser();
if (user) {
    elements.driverName.textContent = user.name || user.email || "Driver";
}

elements.taskDate.textContent = formatDate(new Date().toISOString());

elements.filterStatus?.addEventListener("change", () => {
    renderTasks(elements.filterStatus.value);
});

elements.refreshBtn?.addEventListener("click", fetchTasks);

// Initial load
fetchTasks();
