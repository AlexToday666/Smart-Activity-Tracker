# Analytics

All analytics endpoints require `projectId`, `from` and `to` and operate on `[from, to)`.

- DAU / WAU / MAU: distinct users bucketed by UTC day, ISO-like week start or month start.
- Retention: groups users by their first event in the range and returns retained users by day offset.
- Cohorts: groups users by first event date.
- Funnels: checks whether users completed ordered event-type steps, for example `landing_view,signup,purchase`.
- Sessions: uses `sessionId` when present. If absent, events are grouped per user with a 30 minute inactivity window.
- Top users: returns `userId` and event count.
- Top event types: returns event type and count.

Current implementation intentionally computes retention, cohorts, funnels and sessions in Java over a bounded date range. This keeps the code readable for a pet project while the database indexes cover the read path. For very large production datasets, the natural next step is daily rollup tables or materialized views.
