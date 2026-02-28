import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
  vus: 5,
  duration: "1m",
  thresholds: {
    http_req_failed: ["rate<0.05"],
    http_req_duration: ["p(95)<800"]
  }
};

const baseUrl = __ENV.BASE_URL || "http://localhost:8080";
const projectId = __ENV.PROJECT_ID || "1";
const from = __ENV.FROM || "2026-01-01T00:00:00Z";
const to = __ENV.TO || new Date().toISOString();

export default function () {
  for (const path of ["dau", "top-event-types", "top-users", "sessions"]) {
    const response = http.get(`${baseUrl}/api/v1/analytics/${path}?projectId=${projectId}&from=${from}&to=${to}`);
    check(response, { [`${path} ok`]: (res) => res.status === 200 });
  }
  sleep(1);
}
