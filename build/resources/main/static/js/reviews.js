import { api, initTheme, toggleTheme } from "./common.js";

// Initialize theme
initTheme();

const themeToggle = document.getElementById("theme-toggle");
themeToggle?.addEventListener("click", toggleTheme);

// Demo reviews data
const DEMO_REVIEWS = [
    {
        id: 1,
        customerName: "Nimal Perera",
        rating: 5,
        comment: "Excellent service! My clothes came back perfectly pressed and smelling fresh. The pickup and delivery were right on time.",
        createdAt: new Date(Date.now() - 86400000).toISOString(),
        serviceName: "Wash & Iron",
    },
    {
        id: 2,
        customerName: "Amara Fernando",
        rating: 5,
        comment: "Best laundry service in Colombo. I've been using SmartFold for 6 months now and never had any issues. Highly recommend!",
        createdAt: new Date(Date.now() - 172800000).toISOString(),
        serviceName: "Weekly Subscription",
    },
    {
        id: 3,
        customerName: "Ruwan Silva",
        rating: 4,
        comment: "Great quality cleaning. The only reason for 4 stars is that delivery was 30 minutes late, but they apologized and gave me a discount.",
        createdAt: new Date(Date.now() - 345600000).toISOString(),
        serviceName: "Dry Cleaning",
    },
    {
        id: 4,
        customerName: "Dilani Wijesuriya",
        rating: 5,
        comment: "Love the express service! Had an urgent need for clean clothes before a business trip and they delivered in under 24 hours.",
        createdAt: new Date(Date.now() - 432000000).toISOString(),
        serviceName: "Express Laundry",
    },
    {
        id: 5,
        customerName: "Kasun Jayawardena",
        rating: 5,
        comment: "Very professional. The tracking feature is amazing - I could see exactly where my laundry was at every step.",
        createdAt: new Date(Date.now() - 518400000).toISOString(),
        serviceName: "Premium Care",
    },
    {
        id: 6,
        customerName: "Sachini Mendis",
        rating: 4,
        comment: "Good service overall. Prices are reasonable and quality is consistent. Will continue using them.",
        createdAt: new Date(Date.now() - 604800000).toISOString(),
        serviceName: "Wash & Fold",
    },
];

const elements = {
    avgRating: document.getElementById("avg-rating"),
    avgStars: document.getElementById("avg-stars"),
    reviewCount: document.getElementById("review-count"),
    reviewsList: document.getElementById("reviews-list"),
};

const formatDate = (dateStr) => {
    if (!dateStr) return "";
    const date = new Date(dateStr);
    return date.toLocaleDateString("en-LK", { month: "short", day: "numeric", year: "numeric" });
};

const renderStars = (rating, readonly = true) => {
    let html = "";
    for (let i = 1; i <= 5; i++) {
        const filled = i <= rating ? "filled" : "";
        html += `<span class="star ${filled}">★</span>`;
    }
    return `<div class="star-rating ${readonly ? "readonly" : ""}">${html}</div>`;
};

const calculateAverage = (reviews) => {
    if (reviews.length === 0) return 0;
    const sum = reviews.reduce((acc, r) => acc + r.rating, 0);
    return (sum / reviews.length).toFixed(1);
};

const renderReviews = (reviews) => {
    const avg = calculateAverage(reviews);
    elements.avgRating.textContent = avg;
    elements.reviewCount.textContent = `Based on ${reviews.length} reviews`;

    // Update average stars
    const fullStars = Math.floor(avg);
    let starsHtml = "";
    for (let i = 1; i <= 5; i++) {
        starsHtml += `<span class="star ${i <= fullStars ? "filled" : ""}">★</span>`;
    }
    elements.avgStars.innerHTML = starsHtml;

    // Render review cards
    elements.reviewsList.innerHTML = "";
    reviews.forEach((review) => {
        const card = document.createElement("div");
        card.className = "review-card";
        card.innerHTML = `
            <div class="review-header">
                <div>
                    <span class="review-author">${review.customerName}</span>
                    <span class="muted small-text" style="margin-left: 8px;">${review.serviceName}</span>
                </div>
                <span class="review-date">${formatDate(review.createdAt)}</span>
            </div>
            ${renderStars(review.rating)}
            <p class="review-text">${review.comment}</p>
        `;
        elements.reviewsList.appendChild(card);
    });
};

const fetchReviews = async () => {
    try {
        const data = await api.get("/api/reviews/public");
        if (Array.isArray(data) && data.length > 0) {
            renderReviews(data);
            return;
        }
    } catch {
        // Use demo data
    }
    renderReviews(DEMO_REVIEWS);
};

// Initialize
fetchReviews();

// Export for use in other pages
export { renderStars, DEMO_REVIEWS };
