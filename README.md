# PolyPadel Backend (Spring Boot 3, Java 17)

Simple backend for the PolyPadel tournament management application using SQLite.

## Quick Start

```bash
cd polypadel-backend
mvn spring-boot:run
```

The app runs on http://localhost:8000/api/v1

## Docker Deployment

```bash
# Build and run with Docker
docker build -t polypadel-backend .
docker run -p 8000:8000 -v $(pwd)/data:/app/data polypadel-backend # For linux/macos
docker run -p 8000:8000 -v ${PWD}/data:/app/data polypadel-backend # For windows

# Or use docker-compose (includes frontend)
docker-compose up -d
```

## Requirements
- Java 17+ (or Docker)
- Maven 3.9+

## Configuration

Environment variables in `application.yml`:
- `app.jwt.secret` - JWT signing secret
- `app.jwt.expiration-hours` - Token validity (default: 24h)
- `app.lockout.max-attempts` - Failed login attempts before lockout (default: 5)
- `app.lockout.duration-minutes` - Lockout duration (default: 30min)

## Testing

```bash
mvn test                           # Run tests
mvn verify                         # Run tests with coverage check
open target/site/jacoco/index.html # View coverage report
```

**Code Coverage: 74%** (requirement: 70%)

## Default Test Accounts

Created automatically on first run:
- **Admin**: `admin@padel.com` / `Admin@2025!`
- **Player**: `joueur@padel.com` / `Joueur@2025!`

## API Endpoints

### Authentication
| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/auth/login` | POST | No | Login (returns JWT) |
| `/auth/change-password` | POST | Yes | Change password |
| `/auth/logout` | POST | Yes | Logout |

### Players (Admin only for POST/PUT/DELETE)
| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/players` | GET | Admin | List all players |
| `/players/{id}` | GET | Yes | Get player |
| `/players` | POST | Admin | Create player |
| `/players/{id}` | PUT | Admin | Update player |
| `/players/{id}` | DELETE | Admin | Delete player |

### Teams
| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/teams` | GET | Yes | List teams |
| `/teams` | POST | Admin | Create team |
| `/teams/{id}` | PUT | Admin | Update team |
| `/teams/{id}` | DELETE | Admin | Delete team |

### Pools
| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/pools` | GET | Yes | List pools |
| `/pools` | POST | Admin | Create pool (requires 6 teams) |
| `/pools/{id}` | DELETE | Admin | Delete pool |

### Events
| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/events` | GET | Yes | List events |
| `/events` | POST | Admin | Create event with matches |
| `/events/{id}` | DELETE | Admin | Delete event |

### Matches
| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/matches` | GET | Yes | List upcoming matches (30 days) |
| `/matches/{id}` | PUT | Admin | Update match (score, status) |
| `/matches/{id}` | DELETE | Admin | Delete match |

### Results & Rankings
| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/results/rankings` | GET | Yes | Company rankings |

### Profile
| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/profile/me` | GET | Yes | Get user profile |
| `/profile/me` | PUT | Yes | Update profile |

### Admin
| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/admin/accounts/create` | POST | Admin | Create account for player |
| `/admin/accounts/{id}/reset-password` | POST | Admin | Reset password |

## Project Structure

```
src/main/java/com/polypadel/
├── Application.java        # Main entry point
├── model/                  # JPA Entities (User, Player, Team, Pool, Event, Match)
├── repository/             # Spring Data JPA Repositories
├── service/                # Business logic
├── controller/             # REST Controllers
├── dto/                    # Request/Response DTOs
├── security/               # JWT Service and Filter
└── config/                 # Security, CORS, Exception handling
```

## Security Features

- **JWT Authentication** (24h token validity)
- **Brute Force Protection** (5 attempts, 30min lockout)
- **Password Hashing** (BCrypt)
- **Role-based Authorization** (JOUEUR, ADMINISTRATEUR)
- **XSS Protection** (HTML sanitization)
- **CORS Configuration**
