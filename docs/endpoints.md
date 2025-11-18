# PolyPadel API Endpoints

> Version: 0.0.1-SNAPSHOT  
> Base URL (local Docker): `http://localhost:8080` (adjust if mapped differently in `docker-compose.yml`)

## Authentication & Session

| Method | Path | Auth Required | Description |
|--------|------|---------------|-------------|
| POST | /auth/login | No | Authenticate with email & password. Returns JSON body + sets HttpOnly `JWT` cookie. |
| POST | /auth/logout | Yes (valid JWT) | Revokes current token (via jti) and clears cookie. |

### Login Request
```json
{
  "email": "user@example.com",
  "password": "secret"
}
```
### Login Response
```json
{
  "token": "<jwt>",
  "role": "ADMIN|JOUEUR",
  "userId": "<uuid>",
  "expiresAt": "2025-11-19T12:34:56Z"
}
```
Use either:
- Cookie automatically sent (preferred in browsers)
- Or copy `token` and send in header: `Authorization: Bearer <jwt>`

## Profile
(Requires role JOUEUR or ADMIN)

| Method | Path | Description |
|--------|------|-------------|
| GET | /profile | Get current user's profile. |
| PUT | /profile | Update profile (name or other editable fields). |
| PUT | /profile/password | Change password. |

## Players (Admin)
All endpoints under `/admin/players` require role `ADMIN`.

| Method | Path | Description |
|--------|------|-------------|
| GET | /admin/players?query=... | Paginated player search/list. |
| POST | /admin/players | Create new player (email, name, etc.). |
| GET | /admin/players/{id} | Get player by UUID. |
| PUT | /admin/players/{id} | Update player fields. |
| DELETE | /admin/players/{id} | Remove player. |

## Teams (Admin)
All endpoints under `/admin/teams` require role `ADMIN`.

| Method | Path | Description |
|--------|------|-------------|
| GET | /admin/teams | Paginated list of teams. |
| POST | /admin/teams | Create a team. |
| GET | /admin/teams/{id} | Get team details. |
| PUT | /admin/teams/{id} | Update team. |
| DELETE | /admin/teams/{id} | Delete team. |

## Poules (Groups) (Admin)
All endpoints under `/admin/poules` require role `ADMIN`.

| Method | Path | Description |
|--------|------|-------------|
| GET | /admin/poules | Paginated list of poules. |
| POST | /admin/poules | Create poule. |
| GET | /admin/poules/{id} | Get poule. |
| PUT | /admin/poules/{id} | Update poule. |
| DELETE | /admin/poules/{id} | Delete (only if safe). |
| POST | /admin/poules/{pouleId}/assign/{teamId} | Assign team to poule. |
| POST | /admin/poules/{pouleId}/remove/{teamId} | Remove team from poule. |

## Events & Calendar
Public read access (GET) to `/events/**`. Admin-only for `/admin/events`.

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | /events/calendar?start=YYYY-MM-DD&end=YYYY-MM-DD | Public | List events in date range. |
| GET | /admin/events | ADMIN | Paginated list of events. |
| POST | /admin/events | ADMIN | Create event (date range). |
| GET | /admin/events/{id} | ADMIN | Get event. |
| PUT | /admin/events/{id} | ADMIN | Update event dates. |
| DELETE | /admin/events/{id} | ADMIN | Delete event (if no matches). |

## Matches
Public read access for `/matches/public/**` and general upcoming list for authenticated user.
Admin endpoints for creation & scoring.

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | /admin/matches | ADMIN | Create a match. |
| PUT | /admin/matches/{id}/score | ADMIN | Update final score of a match. |
| GET | /matches/upcoming | JOUEUR/ADMIN | Upcoming matches for current user. |
| GET | /matches/public/event/{eventId} | Public | Public list of matches for event. |

## Results

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | /results/user | JOUEUR/ADMIN | Finished matches for current user. |

## Rankings

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | /rankings/poule/{pouleId} | Public | Ranking table for a poule. |

## Admin Users
Endpoints for managing platform users (role ADMIN).

| Method | Path | Description |
|--------|------|-------------|
| POST | /admin/users | Create user with role. |
| POST | /admin/users/{id}/reset-password | Reset password and return new temporary credentials. |

## Security Rules Summary
- Public: `POST /auth/login`, `GET /events/**`, `GET /matches/public/**`, `GET /rankings/**`
- Admin only: Anything under `/admin/**`
- Authenticated (JOUEUR or ADMIN): `/profile/**`, `/results/user`, `/matches/upcoming`
- Logout requires valid JWT context.

## Common Status Codes
| Code | Meaning |
|------|---------|
| 200 | Success / OK |
| 201 | (Not currently used; creations return 200 with body) |
| 204 | No Content (logout, password change, delete) |
| 400 | Validation / business rule failure |
| 401 | Unauthorized (missing/invalid token) |
| 403 | Forbidden (role lacks permission) |
| 404 | Not found (missing resource) |
| 409 | Business conflict (e.g., delete event with matches) |
| 500 | Unexpected server error |

## Authentication Details
JWT is issued on login with a 24h expiry. It is returned both in the JSON response and set as an HttpOnly cookie `JWT` (if the backend is configured as shown). For Postman, you can:
1. Use the JSON response token in `Authorization` header.
2. Or capture the `Set-Cookie` header and store cookie manually (less common in Postman).

## Example Curl
```bash
curl -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@example.com","password":"secret"}'
```
Response includes token:
```json
{"token":"<jwt>","role":"ADMIN","userId":"<uuid>","expiresAt":"..."}
```
Then:
```bash
curl -H "Authorization: Bearer <jwt>" http://localhost:8080/admin/events
```

---
# Postman & Docker Testing Guide

## 1. Start the Backend with Docker
If using the provided `docker-compose.yml`:
```bash
docker compose up -d
```
Verify service health:
```bash
curl http://localhost:8080/actuator/health || curl -I http://localhost:8080/events/calendar?start=2025-01-01&end=2025-01-31
```
(If actuator endpoint not enabled, the calendar endpoint with no data should still return 200 or validation error.)

## 2. Create a Postman Environment
Add an environment with variables:
| Variable | Value |
|----------|-------|
| baseUrl | http://localhost:8080 |
| authToken | (will fill after login) |
| pouleId | (sample UUID for ranking) |
| eventId | (sample UUID for matches) |

## 3. Login Request
Create a POST request:
- URL: `{{baseUrl}}/auth/login`
- Body (raw JSON):
```json
{
  "email": "admin@example.com",
  "password": "secret"
}
```
Send. Copy `token` from response to environment variable `authToken`.

## 4. Authorized Requests
Set Authorization header automatically using Postman:
- In the request, Headers: `Authorization: Bearer {{authToken}}`
Or use a Pre-request Script:
```javascript
pm.request.headers.add({ key: 'Authorization', value: 'Bearer ' + pm.environment.get('authToken') });
```

## 5. Test Sequence
1. Login (store token)
2. Create Event (ADMIN):
   - POST `{{baseUrl}}/admin/events`
   - Body:
   ```json
   {"dateDebut":"2025-06-01","dateFin":"2025-06-07"}
   ```
3. List Events: GET `{{baseUrl}}/admin/events`
4. Create Poule: POST `{{baseUrl}}/admin/poules`
5. Create Team: POST `{{baseUrl}}/admin/teams`
6. Assign Team to Poule: POST `{{baseUrl}}/admin/poules/{{pouleId}}/assign/{{teamId}}`
7. Create Match: POST `{{baseUrl}}/admin/matches`
8. Update Match Score: PUT `{{baseUrl}}/admin/matches/{matchId}/score`
9. Public Matches: GET `{{baseUrl}}/matches/public/event/{{eventId}}` (No auth header needed)
10. Ranking: GET `{{baseUrl}}/rankings/poule/{{pouleId}}` (No auth)
11. User Upcoming: GET `{{baseUrl}}/matches/upcoming` (Authorized)
12. User Results: GET `{{baseUrl}}/results/user` (Authorized)
13. Logout: POST `{{baseUrl}}/auth/logout`

## 6. Collections & Automation Tips
- Save all requests in a Postman Collection.
- Add a folder "Admin Setup" for create operations.
- Use Postman Tests tab to extract IDs:
```javascript
let json = pm.response.json();
if(json.id){ pm.environment.set('eventId', json.id); }
```
- Chain requests: after creating a resource, next request uses saved variable.

## 7. Handling Cookies
If you prefer cookie auth:
- In Postman Settings enable "Automatically follow redirects" (on).
- After login, Postman stores `JWT` cookie; subsequent requests to same domain will include it automatically. (Ensure you are not using the "Disable cookie jar" option.)

## 8. Error Inspection
- 400 responses contain structured error JSON with `code` and `message` (Business exceptions).
- 401/403 contain standardized JSON from security handlers.
- For debugging ranking or match logic, inspect payloads returned; set pretty view in Postman.

## 9. Environment Reset
To simulate logout token revocation:
1. Call `/auth/logout` (removes cookie / marks jti revoked).
2. Attempt an ADMIN endpoint with old token -> expect 401.
3. Login again to obtain a fresh token.

## 10. Optional: Newman CLI
Automate collection runs:
```bash
newman run PolyPadel.postman_collection.json -e local.postman_environment.json
```
Integrate into CI later for smoke tests.

---
## FAQ
**Why do creations return 200 instead of 201?** Simplified uniform response handling; can be changed later for strict REST semantics.

**How do I switch from cookie to bearer auth?** Just capture `token` and provide `Authorization: Bearer <token>` header.

**Where is Swagger UI?** Once app runs: `http://localhost:8080/swagger-ui/index.html` (springdoc default).

**Time Zones?** Dates are treated as local date (no time) for events; JWT expiry uses UTC ISO timestamps.

---
_Last updated: 2025-11-18_
