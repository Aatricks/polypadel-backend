# Cahier de recette exécutable – PolyPadel Backend (Spring Boot / SQLite)

> Dernière mise à jour : 2025-12-10

## 0. Prérequis & variables
- API locale : `http://localhost:8080`
- Comptes seed : admin `admin@padel.com/Admin@2025!`, joueur `joueur@padel.com/Joueur@2025!`
- JWT requis hors `/auth/**`, rôle `ROLE_ADMINISTRATEUR` pour POST/PUT/DELETE et `/admin/**`
- DB : `padel_corpo.db`

```bash
BASE=http://localhost:8080
ADMIN_EMAIL=admin@padel.com
ADMIN_PASSWORD='Admin@2025!'
PLAYER_EMAIL=joueur@padel.com
PLAYER_PASSWORD='Joueur@2025!'
ADMIN_TOKEN=$(curl -s -X POST "$BASE/auth/login" -H 'Content-Type: application/json' -d "{\"email\":\"$ADMIN_EMAIL\",\"password\":$ADMIN_PASSWORD}" | jq -r '.token')
PLAYER_TOKEN=$(curl -s -X POST "$BASE/auth/login" -H 'Content-Type: application/json' -d "{\"email\":\"$PLAYER_EMAIL\",\"password\":$PLAYER_PASSWORD}" | jq -r '.token')
AUTHZ_ADMIN="Authorization: Bearer $ADMIN_TOKEN"
AUTHZ_PLAYER="Authorization: Bearer $PLAYER_TOKEN"
```

## 1. Jeux de données (exemples prêts à l’emploi)
- Licences invalides : `12345`, `L1234`, `L1234567`, `LX23456`
- Mots de passe invalides : `short1!`, `NoDigits!!!!!!!!`, `nouppercase123!`, `LowerOnly!!!!!!`
- Scores invalides : `6-4-1`, `6/4,6/3`, `6-4,6`, `7-8,6-3`
- Images : `photo_ok.jpg` (≤2MB), `photo_too_big.png` (3MB), `photo.txt`

## 2. Authentification & sécurité
| ID | Commandes (exécutables) | Attendu |
|----|-------------------------|---------|
| AUTH-01 | `curl -i -X POST "$BASE/auth/login" -H 'Content-Type: application/json' -d "{\"email\":\"$ADMIN_EMAIL\",\"password\":$ADMIN_PASSWORD}"` | 200, token bearer, user(id,email,role,must_change_password=false) |
| AUTH-02 | Boucle `for i in {1..$((max-1))}; do curl -o /dev/null -sw "%{http_code}" ...badpass ; done` | 401 avec tentatives restantes |
| AUTH-03 | `for i in {1..$max}; ...badpass`; dernière réponse | 403 + durée lockout |
| AUTH-04 | Rejouer login pendant lockout | 403 + minutes restantes |
| AUTH-05 | Login user `isActive=false` (créer ou modifier en DB) | 403 |
| AUTH-06 | `curl -i -X POST "$BASE/auth/change-password" -H "$AUTHZ_ADMIN" -H 'Content-Type: application/json' -d '{"currentPassword":"Admin@2025!","newPassword":"Admin@2026!","confirmPassword":"Admin@2026!"}'` puis login avec ancien mdp (401) puis login avec nouveau (200) | 1) 200 + must_change_password=false 2) 401 3) 200 |
| AUTH-07 | Même endpoint avec mdp invalides listés | 400 |
| AUTH-08 | `curl -i "$BASE/events"` sans Authorization | 401 |
| AUTH-09 | `curl -i -X POST "$BASE/players" -H "$AUTHZ_PLAYER" ...` | 403 |
| AUTH-10 | `curl -i "$BASE/events" -H 'Authorization: Bearer invalid'` | 401 |
| AUTH-11 | `curl -i -X POST "$BASE/auth/logout"` | 200 (stateless) |

## 3. Administration des comptes
| ID | Commandes | Attendu |
|----|-----------|---------|
| ADM-01 | `curl -i -X POST "$BASE/admin/accounts/create" -H "$AUTHZ_ADMIN" -H 'Content-Type: application/json' -d '{"player_id":<id>}'` | 201 + email `<license>@polypadel.local`, temp password 16 chars, must_change_password=true |
| ADM-02 | Même commande sur joueur déjà lié | 409 |
| ADM-03 | `curl -i -X POST "$BASE/admin/accounts/{userId}/reset-password" -H "$AUTHZ_ADMIN"` | 200 + nouveau temp password + must_change_password=true |
| ADM-04 | userId inconnu | 404 |

## 4. Joueurs
| ID | Commandes | Attendu |
|----|-----------|---------|
| PLR-01 | `curl -i "$BASE/players" -H "$AUTHZ_ADMIN"` | 200 + {players[], total} |
| PLR-02 | `curl -i -X POST "$BASE/players" -H "$AUTHZ_ADMIN" -H 'Content-Type: application/json' -d '{"firstName":"Ana","lastName":"Lopez","company":"Acme","license":"L123456","email":"ana@example.com"}'` | 201 + joueur persisté |
| PLR-03 | Rejouer avec licence/email déjà utilisés | 409 |
| PLR-04 | Payload licence invalide (liste ci-dessus) ou email invalide/champs vides | 400 |
| PLR-05 | `curl -i "$BASE/players/{id}" -H "$AUTHZ_ADMIN"` | 200 |
| PLR-06 | `curl -i -X PUT "$BASE/players/{id}" -H "$AUTHZ_ADMIN" -H 'Content-Type: application/json' -d '{"firstName":"Ana2","company":"Acme"}'` | 200 avec données à jour |
| PLR-07 | `curl -i -X DELETE "$BASE/players/{id}" -H "$AUTHZ_ADMIN"` sans équipe liée | 204 |
| PLR-08 | Même DELETE sur joueur assigné à une équipe | 409 |
| PLR-09 | DELETE avec JWT joueur | 403 |

## 5. Équipes
| ID | Commandes | Attendu |
|----|-----------|---------|
| TEAM-01 | `curl -i "$BASE/teams?poolId=<id>&company=Acme" -H "$AUTHZ_ADMIN"` | 200 + filtrage appliqué |
| TEAM-02 | `curl -i -X POST "$BASE/teams" -H "$AUTHZ_ADMIN" -H 'Content-Type: application/json' -d '{"player1Id":1,"player2Id":2,"company":"Acme","poolId":null}'` | 201 |
| TEAM-03 | player1Id = player2Id | 400 |
| TEAM-04 | Joueurs d’entreprises différentes | 400 |
| TEAM-05 | Joueur déjà dans une équipe | 409 |
| TEAM-06 | `curl -i -X PUT "$BASE/teams/{id}" ...` sans match associé | 200 |
| TEAM-07 | PUT sur équipe avec matchs existants | 409 |
| TEAM-08 | `curl -i -X DELETE "$BASE/teams/{id}" -H "$AUTHZ_ADMIN"` sans match | 204 |
| TEAM-09 | DELETE avec match | 409 |
| TEAM-10 | DELETE avec JWT joueur | 403 |

## 6. Poules
| ID | Commandes | Attendu |
|----|-----------|---------|
| POOL-01 | `curl -i "$BASE/pools" -H "$AUTHZ_ADMIN"` et `curl -i "$BASE/pools/{id}" -H "$AUTHZ_ADMIN"` | 200 |
| POOL-02 | `curl -i -X POST "$BASE/pools" -H "$AUTHZ_ADMIN" -H 'Content-Type: application/json' -d '{"name":"Pool A","teamIds":[1,2,3,4,5,6]}'` | 201 + teams liés |
| POOL-03 | Payload <6 ou >6 équipes | 400 |
| POOL-04 | Nom dupliqué | 409 |
| POOL-05 | PUT `/pools/{id}` sans matchs terminés | 200 |
| POOL-06 | PUT avec matchs terminés | 409 |
| POOL-07 | DELETE `/pools/{id}` sans matchs terminés | 204 + équipes détachées |
| POOL-08 | DELETE avec matchs terminés | 409 |
| POOL-09 | DELETE avec JWT joueur | 403 |

## 7. Événements (planning)
| ID | Commandes | Attendu |
|----|-----------|---------|
| EVT-01 | `curl -i "$BASE/events" -H "$AUTHZ_ADMIN"` | 200 + events avec matches |
| EVT-02 | `curl -i "$BASE/events?start_date=2025-01-01&end_date=2025-01-31" -H "$AUTHZ_ADMIN"` | 200 + intervalle |
| EVT-03 | `curl -i "$BASE/events?month=2025-02" -H "$AUTHZ_ADMIN"` | 200 + mois |
| EVT-04 | POST `/events` (date ≥ today, 1-3 matchs, courts 1-10, équipes distinctes, pas de court ou équipe dupliqués)` | 201 |
| EVT-05 | match team1Id=team2Id | 400 |
| EVT-06 | Deux matchs même court | 400 |
| EVT-07 | Même équipe sur 2 matchs | 400 |
| EVT-08 | PUT `/events/{id}` (date/heure) | 200 |
| EVT-09 | DELETE `/events/{id}` si tous les matchs = A_VENIR | 204 |
| EVT-10 | DELETE avec match terminé/annulé | 409 |
| EVT-11 | POST avec date passée | 400 (FutureOrPresent) |
| EVT-12 | DELETE avec JWT joueur | 403 |

## 8. Matchs
| ID | Commandes | Attendu |
|----|-----------|---------|
| MATCH-01 | `curl -i "$BASE/matches" -H "$AUTHZ_ADMIN"` | 200 + matches J→J+30 |
| MATCH-02 | `curl -i "$BASE/matches?teamId=<id>" -H "$AUTHZ_ADMIN"` | 200 + matches de l’équipe |
| MATCH-03 | `curl -i "$BASE/matches?myMatches=true" -H "$AUTHZ_PLAYER"` | 200 + mêmes matchs que sans param (backend ignore le filtre actuellement – écart connu) |
| MATCH-04 | POST `/matches` avec event existant, équipes distinctes | 201 |
| MATCH-05 | POST avec équipes identiques | 400 |
| MATCH-06 | PUT `/matches/{id}` status in {A_VENIR,TERMINE,ANNULE}, score regex `X-Y, X-Y[, X-Y]` | 200 + valeurs stockées |
| MATCH-07 | PUT score non conforme (liste invalides) | 400 |
| MATCH-08 | DELETE match status A_VENIR | 204 |
| MATCH-09 | DELETE status TERMINE/ANNULE | 409 |
| MATCH-10 | DELETE avec JWT joueur | 403 |

## 9. Résultats & classement
| ID | Commandes | Attendu |
|----|-----------|---------|
| RES-01 | `curl -i "$BASE/results/my-results" -H "$AUTHZ_PLAYER"` | 200 + matchs terminés de ses équipes + stats |
| RES-02 | Même endpoint joueur sans équipe | 200 + liste vide, stats 0 |
| RES-03 | JWT sans player | 404 |
| RES-04 | `curl -i "$BASE/results/rankings" -H "$AUTHZ_ADMIN"` | 200 + ranking points desc (tie-break wins, diff sets, alpha), +3/ victoire |
| RES-05 | Classement avec score null | sets comptés 0 (voir parseScore) |

## 10. Profil
| ID | Commandes | Attendu |
|----|-----------|---------|
| PROF-01 | `curl -i "$BASE/profile/me" -H "$AUTHZ_PLAYER"` | 200 + user + player si lié |
| PROF-02 | `curl -i -X PUT "$BASE/profile/me" -H "$AUTHZ_PLAYER" -H 'Content-Type: application/json' -d '{"firstName":"Joe","lastName":"Doe","email":"new@example.com"}'` | 200 ou 409 si email doublon |
| PROF-03 | `curl -i -X POST "$BASE/profile/me/change-password" ...` | Règles AUTH-06/07 |
| PROF-04 | `curl -i -X POST "$BASE/profile/me/photo" -H "$AUTHZ_PLAYER" -F photo=@photo_ok.jpg` | 200 + `photo_url` et fichier écrit |
| PROF-05 | Upload `photo_too_big.png` ou `photo.txt` | 400 |
| PROF-06 | `curl -i -X DELETE "$BASE/profile/me/photo" -H "$AUTHZ_PLAYER"` | 200 + suppression si existante |
| PROF-07 | Routes profil avec JWT sans player | 404 |

## 11. Règles métier transverses (rappels à vérifier)
- Sanitization : `PlayerService` retire tags HTML sur noms/entreprise.
- Match status enum {A_VENIR, TERMINE, ANNULE}; score regex appliquée à l’update uniquement.
- Pool update/delete bloqué si matchs terminés pour les équipes du pool.
- Event delete bloqué si un match non A_VENIR.
- Team update/delete bloqué si match associé (quel que soit statut).
- CORS : http://localhost:5173/5174.

## 12. Écarts vs spécifications
- Paramètre `myMatches` ignoré dans `MatchService.findUpcoming` (aucun filtrage joueur) : test MATCH-03 vérifie l’écart.
- Login success ne retourne pas attempts_remaining/locked_until (seuls messages d’erreur exposent ces infos).
- Email comptes créés par admin = `<license>@polypadel.local` (et non l’email joueur).
- Validation score ne vérifie pas cohérence sets (seul format regex).
