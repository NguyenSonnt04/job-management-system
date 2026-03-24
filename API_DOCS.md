# API Documentation — CareerViet Job Management System

**Base URL:** `http://localhost:8083`

---

## Authentication — `/api/auth`

| Method | Endpoint | Yêu cầu auth |
|--------|----------|--------------|
| `GET` | `/api/auth/check-email?email=` | Không |
| `POST` | `/api/auth/register/user` | Không |
| `POST` | `/api/auth/register/employer/step1` | Không |
| `POST` | `/api/auth/register/employer/step2` | Session step 1 |
| `POST` | `/api/auth/login` | Không |
| `GET` | `/api/auth/me` | Không (optional) |
| `GET` | `/logout` | Có |

---

## User — `/api/user`

| Method | Endpoint | Yêu cầu auth |
|--------|----------|--------------|
| `GET` | `/api/user/me` | Không (optional) |

---

## Jobs — `/api/jobs`

| Method | Endpoint | Yêu cầu auth |
|--------|----------|--------------|
| `GET` | `/api/jobs/active` | Không |
| `GET` | `/api/jobs/search?keyword=` | Không |
| `GET` | `/api/jobs/form-options` | Không |
| `GET` | `/api/jobs/{id}` | Không |
| `POST` | `/api/jobs/{id}/view` | Không |
| `GET` | `/api/jobs/employer-info` | EMPLOYER |
| `GET` | `/api/jobs/my-jobs` | EMPLOYER |
| `POST` | `/api/jobs/create` | EMPLOYER |
| `PUT` | `/api/jobs/{id}` | EMPLOYER (owner) |
| `DELETE` | `/api/jobs/{id}` | EMPLOYER (owner) |

---

## Applications — `/api/applications`

| Method | Endpoint | Yêu cầu auth |
|--------|----------|--------------|
| `POST` | `/api/applications/apply` | Không (optional) |
| `GET` | `/api/applications/employer` | EMPLOYER |
| `PATCH` | `/api/applications/{id}/status` | EMPLOYER |

---

## CV Scoring AI — `/api/cv-scoring`

| Method | Endpoint | Yêu cầu auth |
|--------|----------|--------------|
| `GET` | `/api/cv-scoring/criteria` | Không |
| `POST` | `/api/cv-scoring/score` | Có |
| `GET` | `/api/cv-scoring/history` | Có |
| `GET` | `/api/cv-scoring/{id}` | Có |
| `POST` | `/api/cv-scoring/match-jobs` | Có |
| `DELETE` | `/api/cv-scoring/match-jobs/{sessionId}` | Có |

---

## Notifications — `/api/notifications`

| Method | Endpoint | Yêu cầu auth |
|--------|----------|--------------|
| `GET` | `/api/notifications` | Có |
| `PATCH` | `/api/notifications/read-all` | Có |
| `PATCH` | `/api/notifications/{id}/read` | Có |

---

## Admin — `/api/admin`

| Method | Endpoint | Yêu cầu auth |
|--------|----------|--------------|
| `GET` | `/api/admin/stats` | ADMIN |
