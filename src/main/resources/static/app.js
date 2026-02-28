const projectForm = document.querySelector("#project-form");
const projectSelect = document.querySelector("#project-select");
const apiKeyForm = document.querySelector("#api-key-form");
const apiKeyInput = document.querySelector("#api-key-input");
const eventForm = document.querySelector("#event-form");
const filtersForm = document.querySelector("#filters-form");
const analyticsForm = document.querySelector("#analytics-form");
const eventsList = document.querySelector("#events-list");
const eventTypes = document.querySelector("#event-types");
const topUsers = document.querySelector("#top-users");
const dauValue = document.querySelector("#dau-value");
const topTypeValue = document.querySelector("#top-type-value");
const topUserValue = document.querySelector("#top-user-value");
const feedback = document.querySelector("#feedback");

document.addEventListener("DOMContentLoaded", async () => {
    seedAnalyticsRange();
    await loadProjects();
    await loadEvents();
    await loadAnalytics();
});

projectForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const formData = new FormData(projectForm);
    const payload = {
        name: value(formData, "name"),
        slug: value(formData, "slug"),
        description: "Created from embedded demo console"
    };
    try {
        const project = await apiFetch("/api/v1/projects", jsonOptions("POST", payload));
        projectForm.reset();
        await loadProjects(project.id);
        showFeedback("Project created.", "success");
    } catch (error) {
        showFeedback(error.message, "error");
    }
});

projectSelect.addEventListener("change", async () => {
    await loadEvents();
    await loadAnalytics();
});

apiKeyForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const projectId = currentProjectId();
    if (!projectId) {
        showFeedback("Create or select a project first.", "error");
        return;
    }
    const formData = new FormData(apiKeyForm);
    try {
        const response = await apiFetch(`/api/v1/projects/${projectId}/api-keys`, jsonOptions("POST", {
            name: value(formData, "name")
        }));
        apiKeyInput.value = response.secret;
        apiKeyForm.reset();
        showFeedback("API key generated. It is shown only once.", "success");
    } catch (error) {
        showFeedback(error.message, "error");
    }
});

eventForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const apiKey = apiKeyInput.value.trim();
    if (!apiKey) {
        showFeedback("X-API-Key is required for ingestion.", "error");
        return;
    }
    const formData = new FormData(eventForm);
    const payload = {
        eventId: value(formData, "eventId"),
        userId: value(formData, "userId"),
        type: value(formData, "type"),
        source: optionalValue(formData, "source"),
        sessionId: optionalValue(formData, "sessionId"),
        occurredAt: toIsoString(formData.get("occurredAt")),
        metadata: parseMetadata(formData.get("metadata"))
    };

    try {
        await apiFetch("/api/v1/events", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-API-Key": apiKey
            },
            body: JSON.stringify(payload)
        });
        eventForm.reset();
        showFeedback("Event ingested.", "success");
        await loadEvents();
        await loadAnalytics();
    } catch (error) {
        showFeedback(error.message, "error");
    }
});

filtersForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    await loadEvents();
});

analyticsForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    await loadAnalytics();
});

async function loadProjects(selectedId) {
    const projects = await apiFetch("/api/v1/projects");
    projectSelect.innerHTML = projects.map((project) => `
        <option value="${project.id}">${escapeHtml(project.name)} (${escapeHtml(project.slug)})</option>
    `).join("");
    if (selectedId) {
        projectSelect.value = selectedId;
    }
}

async function loadEvents() {
    const formData = new FormData(filtersForm);
    const params = new URLSearchParams({ size: "12" });
    addParam(params, "projectId", currentProjectId());
    addParam(params, "userId", formData.get("userId"));
    addParam(params, "type", formData.get("type"));
    addParam(params, "source", formData.get("source"));
    addParam(params, "sessionId", formData.get("sessionId"));
    addParam(params, "from", toIsoString(formData.get("from")));
    addParam(params, "to", toIsoString(formData.get("to")));

    const metadataKey = value(formData, "metadataKey");
    const metadataValue = value(formData, "metadataValue");
    if (metadataKey && metadataValue) {
        params.set(`metadata.${metadataKey}`, metadataValue);
    }

    try {
        const response = await apiFetch(`/api/v1/events?${params.toString()}`);
        renderEvents(response.content || []);
    } catch (error) {
        renderEvents([]);
        showFeedback(error.message, "error");
    }
}

async function loadAnalytics() {
    const projectId = currentProjectId();
    if (!projectId) {
        clearAnalytics();
        return;
    }
    const formData = new FormData(analyticsForm);
    const params = new URLSearchParams();
    addParam(params, "projectId", projectId);
    addParam(params, "from", toIsoString(formData.get("from")));
    addParam(params, "to", toIsoString(formData.get("to")));

    try {
        const [dauResponse, typesResponse, usersResponse] = await Promise.all([
            apiFetch(`/api/v1/analytics/dau?${params.toString()}`),
            apiFetch(`/api/v1/analytics/top-event-types?${params.toString()}`),
            apiFetch(`/api/v1/analytics/top-users?${params.toString()}`)
        ]);
        dauValue.textContent = dauResponse.points?.length ?? 0;
        const types = typesResponse.eventTypes || [];
        const users = usersResponse.users || [];
        topTypeValue.textContent = types[0]?.eventType || "-";
        topUserValue.textContent = users[0]?.userId || "-";
        renderCountList(eventTypes, types, "eventType");
        renderCountList(topUsers, users, "userId");
    } catch (error) {
        clearAnalytics();
        showFeedback(error.message, "error");
    }
}

function renderEvents(items) {
    if (!items.length) {
        eventsList.innerHTML = emptyItem("No events match current filters.");
        return;
    }
    eventsList.innerHTML = items.map((item) => `
        <article class="event-item">
            <header>
                <strong>${escapeHtml(item.type || item.eventType)}</strong>
                <span class="event-meta">${escapeHtml(item.userId)} · ${escapeHtml(item.eventId)}</span>
            </header>
            <span class="event-meta">${formatDate(item.occurredAt)} · source=${escapeHtml(item.source || "-")}</span>
            <pre>${escapeHtml(JSON.stringify(item.metadata || {}, null, 2))}</pre>
        </article>
    `).join("");
}

function renderCountList(target, items, labelKey) {
    if (!items.length) {
        target.innerHTML = emptyListItem("No data for selected range.");
        return;
    }
    target.innerHTML = items.map((item) => `
        <article class="list-item">
            <strong>${escapeHtml(item[labelKey])}</strong>
            <span>${item.count}</span>
        </article>
    `).join("");
}

function clearAnalytics() {
    dauValue.textContent = "-";
    topTypeValue.textContent = "-";
    topUserValue.textContent = "-";
    eventTypes.innerHTML = emptyListItem("No event type data.");
    topUsers.innerHTML = emptyListItem("No user data.");
}

function jsonOptions(method, payload) {
    return {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    };
}

function parseMetadata(value) {
    if (!value || !value.toString().trim()) {
        return null;
    }
    return JSON.parse(value.toString());
}

function addParam(params, key, value) {
    if (value) {
        params.set(key, value);
    }
}

function value(formData, key) {
    return formData.get(key)?.toString().trim() || "";
}

function optionalValue(formData, key) {
    return value(formData, key) || null;
}

function currentProjectId() {
    return projectSelect.value || null;
}

function toIsoString(value) {
    if (!value) {
        return null;
    }
    return new Date(value).toISOString();
}

function seedAnalyticsRange() {
    const to = new Date();
    const from = new Date(to.getTime() - 7 * 24 * 60 * 60 * 1000);
    analyticsForm.elements.from.value = toLocalInputValue(from);
    analyticsForm.elements.to.value = toLocalInputValue(to);
}

function toLocalInputValue(date) {
    const shifted = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
    return shifted.toISOString().slice(0, 16);
}

function formatDate(value) {
    if (!value) {
        return "-";
    }
    return new Intl.DateTimeFormat("ru-RU", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(new Date(value));
}

function showFeedback(message, type) {
    feedback.textContent = message;
    feedback.className = `feedback visible ${type}`;
}

function emptyItem(message) {
    return `<div class="event-item"><strong>${escapeHtml(message)}</strong></div>`;
}

function emptyListItem(message) {
    return `<div class="list-item"><span>${escapeHtml(message)}</span></div>`;
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#39;");
}

async function apiFetch(url, options = {}) {
    const response = await fetch(url, options);
    const isJson = response.headers.get("content-type")?.includes("application/json");
    const payload = isJson ? await response.json() : null;

    if (!response.ok) {
        throw new Error(payload?.message || `Request failed with status ${response.status}`);
    }
    return payload;
}
