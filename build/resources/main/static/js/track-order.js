import { api, initTheme, toggleTheme, toastError } from "./common.js";

// Initialize theme
initTheme();

const themeToggle = document.getElementById("theme-toggle");
themeToggle?.addEventListener("click", toggleTheme);

// Tracking stages in order
const TRACKING_STAGES = [
    { key: "RECEIVED", label: "Order Received", icon: "📦" },
    { key: "PICKED_UP", label: "Picked Up", icon: "🚚" },
    { key: "WASHING", label: "Washing", icon: "🧺" },
    { key: "DRYING", label: "Drying", icon: "💨" },
    { key: "IRONING", label: "Ironing", icon: "👔" },
    { key: "QUALITY_CHECK", label: "Quality Check", icon: "✅" },
    { key: "READY", label: "Ready for Delivery", icon: "📋" },
    { key: "DELIVERED", label: "Delivered", icon: "🎉" },
];

// Map order status to tracking stage index
const STATUS_TO_STAGE = {
    PENDING: 0,
    PICKED_UP: 1,
    IN_PROGRESS: 3, // Assume drying stage for in-progress
    READY: 6,
    DELIVERED: 7,
    CANCELLED: -1,
};

const elements = {
    form: document.getElementById("track-form"),
    orderIdInput: document.getElementById("order-id"),
    resultSection: document.getElementById("tracking-result"),
    errorSection: document.getElementById("tracking-error"),
    resultOrderId: document.getElementById("result-order-id"),
    resultService: document.getElementById("result-service"),
    resultStatus: document.getElementById("result-status"),
    resultCustomer: document.getElementById("result-customer"),
    resultPickup: document.getElementById("result-pickup"),
    resultDelivery: document.getElementById("result-delivery"),
    resultAmount: document.getElementById("result-amount"),
    timeline: document.getElementById("tracking-timeline"),
    tryAgainBtn: document.getElementById("try-again-btn"),
};

const formatDate = (dateStr) => {
    if (!dateStr) return "—";
    const date = new Date(dateStr);
    return date.toLocaleDateString("en-LK", {
        weekday: "short",
        month: "short",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    });
};

const formatLKR = (amount) => {
    if (amount == null) return "—";
    return `LKR ${Number(amount).toLocaleString("en-LK")}`;
};

const renderTimeline = (currentStage, trackingHistory = []) => {
    elements.timeline.innerHTML = "";

    TRACKING_STAGES.forEach((stage, index) => {
        const step = document.createElement("div");
        step.className = "timeline-step";

        if (index < currentStage) {
            step.classList.add("completed");
        } else if (index === currentStage) {
            step.classList.add("current");
        }

        const historyEntry = trackingHistory.find((h) => h.stage === stage.key);
        const timestamp = historyEntry?.timestamp ? formatDate(historyEntry.timestamp) : "";
        const notes = historyEntry?.notes || "";

        step.innerHTML = `
            <div class="step-marker">
                <span class="step-icon">${stage.icon}</span>
                <span class="step-line"></span>
            </div>
            <div class="step-content">
                <span class="step-label">${stage.label}</span>
                ${timestamp ? `<span class="step-time">${timestamp}</span>` : ""}
                ${notes ? `<span class="step-notes">${notes}</span>` : ""}
            </div>
        `;

        elements.timeline.appendChild(step);
    });
};

const showResult = (order) => {
    elements.errorSection.hidden = true;
    elements.resultSection.hidden = false;

    elements.resultOrderId.textContent = order.id || order.orderId || "—";
    elements.resultService.textContent = order.serviceName || order.service?.name || "Laundry Service";
    elements.resultStatus.textContent = order.status || "PENDING";
    elements.resultStatus.className = `badge status-${order.status || "PENDING"}`;
    elements.resultCustomer.textContent = order.customerName || order.user?.name || "—";
    elements.resultPickup.textContent = formatDate(order.pickupDate);
    elements.resultDelivery.textContent = formatDate(order.deliveryDate);
    elements.resultAmount.textContent = formatLKR(order.price || order.total);

    const stageIndex = STATUS_TO_STAGE[order.status] ?? 0;
    renderTimeline(stageIndex, order.trackingHistory || []);
};

const showError = () => {
    elements.resultSection.hidden = true;
    elements.errorSection.hidden = false;
};

const reset = () => {
    elements.resultSection.hidden = true;
    elements.errorSection.hidden = true;
    elements.orderIdInput.value = "";
    elements.orderIdInput.focus();
};

// Demo data for testing without backend
const DEMO_ORDERS = {
    1001: {
        id: 1001,
        serviceName: "Wash & Iron",
        status: "IN_PROGRESS",
        customerName: "Nimal Perera",
        pickupDate: new Date(Date.now() - 86400000).toISOString(),
        deliveryDate: new Date(Date.now() + 86400000).toISOString(),
        price: 1250,
        trackingHistory: [
            { stage: "RECEIVED", timestamp: new Date(Date.now() - 86400000).toISOString() },
            { stage: "PICKED_UP", timestamp: new Date(Date.now() - 82800000).toISOString() },
            { stage: "WASHING", timestamp: new Date(Date.now() - 72000000).toISOString() },
            { stage: "DRYING", timestamp: new Date(Date.now() - 36000000).toISOString(), notes: "Air drying delicate items" },
        ],
    },
    1002: {
        id: 1002,
        serviceName: "Dry Cleaning",
        status: "READY",
        customerName: "Ruwan Silva",
        pickupDate: new Date(Date.now() - 172800000).toISOString(),
        deliveryDate: new Date(Date.now()).toISOString(),
        price: 2400,
        trackingHistory: [
            { stage: "RECEIVED", timestamp: new Date(Date.now() - 172800000).toISOString() },
            { stage: "PICKED_UP", timestamp: new Date(Date.now() - 169200000).toISOString() },
            { stage: "WASHING", timestamp: new Date(Date.now() - 158400000).toISOString() },
            { stage: "DRYING", timestamp: new Date(Date.now() - 129600000).toISOString() },
            { stage: "IRONING", timestamp: new Date(Date.now() - 100800000).toISOString() },
            { stage: "QUALITY_CHECK", timestamp: new Date(Date.now() - 72000000).toISOString() },
            { stage: "READY", timestamp: new Date(Date.now() - 36000000).toISOString(), notes: "Ready for pickup or delivery" },
        ],
    },
    1003: {
        id: 1003,
        serviceName: "Express Laundry",
        status: "DELIVERED",
        customerName: "Amara Fernando",
        pickupDate: new Date(Date.now() - 259200000).toISOString(),
        deliveryDate: new Date(Date.now() - 172800000).toISOString(),
        price: 1875,
        trackingHistory: [
            { stage: "RECEIVED", timestamp: new Date(Date.now() - 259200000).toISOString() },
            { stage: "PICKED_UP", timestamp: new Date(Date.now() - 255600000).toISOString() },
            { stage: "WASHING", timestamp: new Date(Date.now() - 248400000).toISOString() },
            { stage: "DRYING", timestamp: new Date(Date.now() - 234000000).toISOString() },
            { stage: "IRONING", timestamp: new Date(Date.now() - 219600000).toISOString() },
            { stage: "QUALITY_CHECK", timestamp: new Date(Date.now() - 205200000).toISOString() },
            { stage: "READY", timestamp: new Date(Date.now() - 190800000).toISOString() },
            { stage: "DELIVERED", timestamp: new Date(Date.now() - 172800000).toISOString(), notes: "Signed by customer" },
        ],
    },
};

const fetchOrder = async (orderId) => {
    try {
        const res = await fetch(`/api/orders/${orderId}`);
        if (res.ok) {
            return await res.json();
        }
    } catch {
        // Backend unavailable, use demo data
    }

    // Fallback to demo data
    const demoOrder = DEMO_ORDERS[orderId];
    if (demoOrder) {
        return demoOrder;
    }
    return null;
};

elements.form?.addEventListener("submit", async (e) => {
    e.preventDefault();
    const orderId = elements.orderIdInput.value.trim();

    if (!orderId) {
        toastError("Please enter an order ID");
        return;
    }

    const order = await fetchOrder(orderId);
    if (order) {
        showResult(order);
    } else {
        showError();
    }
});

elements.tryAgainBtn?.addEventListener("click", reset);

// Check URL params for order ID
const params = new URLSearchParams(window.location.search);
const urlOrderId = params.get("orderId") || params.get("id");
if (urlOrderId) {
    elements.orderIdInput.value = urlOrderId;
    fetchOrder(urlOrderId).then((order) => {
        if (order) showResult(order);
    });
}
