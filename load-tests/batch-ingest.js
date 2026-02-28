import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
  vus: 10,
  duration: "1m",
  thresholds: {
    http_req_failed: ["rate<0.05"],
    http_req_duration: ["p(95)<500"]
  }
};

const apiKey = __ENV.API_KEY;
const baseUrl = __ENV.BASE_URL || "http://localhost:8080";

export default function () {
  const events = Array.from({ length: 50 }, (_, index) => ({
    eventId: `${__VU}-${__ITER}-${index}`,
    userId: `user-${index % 100}`,
    type: index % 5 === 0 ? "purchase" : "landing_view",
    occurredAt: new Date().toISOString(),
    source: "k6",
    sessionId: `session-${__VU}-${__ITER}`,
    metadata: { country: index % 2 === 0 ? "DE" : "US", device: "web" }
  }));

  const response = http.post(`${baseUrl}/api/v1/events/batch`, JSON.stringify(events), {
    headers: {
      "Content-Type": "application/json",
      "X-API-Key": apiKey
    }
  });

  check(response, {
    "batch accepted": (res) => res.status === 200,
    "no rejected items": (res) => (res.json("rejected") || 0) === 0
  });
  sleep(1);
}
