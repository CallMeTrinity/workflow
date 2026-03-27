# Feuille de route ProjectRoom

## 5 séances x 5h — 2 développeurs (Dev A = expérimenté, Dev B = débutant)

---

## Séance 1 — Fondations (5h)

**Objectif : projet qui tourne, base de données créée, login fonctionnel**

| Tâche                                                             | Qui | Durée estimé |
|-------------------------------------------------------------------|-----|--------------|
| ~~Créer le projet Maven, configurer le pom.xml, init Git/GitLab~~ | A   | 30min        |
| ~~Créer la structure des packages~~                               | A   | 20min        |
| ~~Écrire le schema.sql (toutes les tables)~~                      | A   | 45min        |
| ~~DatabaseConfig.java + initialisation auto du schema.sql~~       | A   | 45min        |
| ~~Modèles User, Role (enum), priority (enum), status (enum)~~     | B   | 1h           |
| ~~UserRepository (save, findByEmail, findById)~~                  | A   | 45min        |
| ~~AuthService (login, hash bcrypt, SessionManager)~~              | A   | 45min        |
| ~~login.fxml + LoginController basique~~                          | B   | 1h           |
| ~~Modèles Projet, Tache, UserStory + enums~~                      | B   | 1h           |

**Fin de séance 1 :** on peut se connecter avec un user créé manuellement en base

---

## Séance 2 — Gestion de projets et tâches (5h)

**Objectif : CRUD projets et tâches, kanban basique**

| Tâche                                                             | Qui | Durée |
|-------------------------------------------------------------------|-----|-------|
| ~~ProjetRepository (CRUD complet)~~                               | A   | 45min |
| ~~TacheRepository (CRUD + findByAssignee)~~                       | A   | 45min |
| ~~ProjetService + TacheService (logique métier, droits)~~         | A   | 1h    |
| ~~dashboard.fxml + DashboardController (liste projets)~~          | B   | 1h    |
| ~~kanban.fxml + KanbanController (3 colonnes, affichage tâches)~~ | B   | 1h    |
| ~~Tests JUnit ProjetService + TacheService~~                      | A   | 30min |

**Fin de séance 2 :** on peut créer un projet, des tâches, les déplacer dans le kanban

---

## Séance 3 — Salles et réservations (5h)

**Objectif : gestion des salles, création de réservations**

| Tâche                                                  | Qui | Durée |
|--------------------------------------------------------|-----|-------|
| Amélioration de l'existant                             | B   | 3h    |
| ~~Modèles Salle, Reservation~~                         | A   | 45min |
| ~~SalleRepository + ReservationRepository~~            | A   | 1h    |
| ~~SalleService + ReservationService (vérif conflits)~~ | A   | 1h    |
| ~~salle.fxml + SalleController (liste, CRUD admin)~~   | A   | 1h    |
| ~~reservation.fxml + ReservationController~~           | A   | 1h    |
| ~~Tests JUnit ReservationService (conflits)~~          | A   | 30min |
| ~~Branchement des vues entre elles (navigation)~~      | A   | 30min |

**Fin de séance 3 :** on peut gérer les salles et créer des réservations sans conflit

---

## Séance 4 — Planning et notifications (5h)

**Objectif : vue planning, recherche de créneaux, notifications**

Ce qui existe déjà (utilisable tel quel)

- Table reservation — avec date, start_time, end_time, room_id, organizer_id
- Table participants_reservation — la jointure user ↔ réservation, c'est la clé pour savoir qui est occupé quand
- Table room — avec capacité

  ---
Ce qui manque

Couche Model

Un POJO Reservation (id, title, description, date, startTime, endTime, roomId, projectId, organizerId).

Couche Repository

Un ReservationRepository avec ces requêtes :
- findByParticipantAndDate(userId, date) — toutes les réservations d'un user sur une date donnée (via jointure participants_reservation)
- findByRoomAndDate(roomId, date) — créneaux occupés pour une salle
- save(reservation) + addParticipant(reservationId, userId)

Couche Service

Un ReservationService avec deux responsabilités principales :

1. Trouver les créneaux disponibles — c'est l'algorithme central :
   findAvailableSlots(List<Long> userIds, String date, int durationMinutes, Long roomId)
   Logique :
1. Pour chaque user de la liste, récupère ses créneaux occupés ce jour-là
2. Fusionne tous les intervalles occupés (union)
3. Inverse → créneaux libres
4. Croise avec les créneaux libres de la salle
5. Filtre par durée minimale souhaitée

2. Créer une réservation avec vérification de conflits au moment de la confirmation.

Couche UI

- Un écran "Planifier une réunion" : sélection de date, durée souhaitée, participants (multi-select sur les membres du projet), salle
- Un affichage des créneaux proposés (ex: liste "10h-11h", "14h-15h"...)
- Confirmation → création de la réservation + ajout des participants

  ---
À quoi t'attendre en termes de complexité

┌───────────────────────────┬────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│          Partie           │                                               Complexité                                               │
├───────────────────────────┼────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│ Model + Repository        │ Faible — mécanique, similaire à TaskRepository                                                         │
├───────────────────────────┼────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│ Algorithme de slots       │ Moyenne — manipulation d'intervalles de temps (comparaisons de strings HH:MM ou conversion en minutes) │
├───────────────────────────┼────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│ UI sélection participants │ Moyenne — ListView avec multi-sélection                                                                │
├───────────────────────────┼────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│ UI affichage des créneaux │ Faible si liste simple, plus élevée si tu veux un vrai calendrier visuel                               │
└───────────────────────────┴────────────────────────────────────────────────────────────────────────────────────────────────────────┘


| Tâche                                                      | Qui | Durée |
|------------------------------------------------------------|-----|-------|
| Modèle PlanningItem + PlanningService complet              | A   | 1h30  |
| Algorithme findCreneauxLibres                              | A   | 1h    |
| Tests JUnit PlanningService (algo créneaux)                | A   | 30min |
| planning.fxml + PlanningController (vue calendrier)        | B   | 1h30  |
| Modèle Notification + NotificationRepository               | B   | 30min |
| NotificationService + branchement dans les autres services | A   | 30min |
| notification.fxml + NotificationController                 | B   | 30min |

**Fin de séance 4 :** planning consultable, créneaux libres trouvables, notifications in-app

---

## Séance 5 — Rôles, finitions, qualité (5h)

**Objectif : gestion des rôles, polish, qualité du code**

| Tâche                                          | Qui | Durée |
|------------------------------------------------|-----|-------|
| AdminController (gestion users, salles)        | B   | 1h    |
| Vérification des restrictions par rôle partout | A   | 45min |
| UserStoryRepository + UserStoryService         | A   | 45min |
| Amélioration UI (CSS, cohérence visuelle)      | B   | 1h30  |
| Checkstyle — corriger les violations           | A+B | 30min |
| Javadoc sur les services et repositories       | A+B | 30min |
| README (install, lancer le projet, stack)      | B   | 30min |
| Tests de bout en bout manuels                  | A+B | 30min |

**Fin de séance 5 :** projet complet, propre, documenté, prêt pour la démo

---

## Ce que Dev B doit apprendre avant la séance 1

- Bases JavaFX : `Label`, `Button`, `TextField`, `@FXML`, `initialize()`
- Lire le schema.sql pour comprendre la structure des tables
- Installer IntelliJ + plugin JavaFX si pas déjà fait

---

## Règles d'équipe

- **Chaque fin de séance** : commit + push sur GitLab, même si c'est incomplet
- **Branches Git** : une branche par fonctionnalité, merge sur `main` quand ça marche
- **Jamais de logique métier dans les controllers** — si Dev B est tenté, Dev A relit
- **Les tests** passent toujours avant de merger

---

Par quoi vous voulez commencer concrètement ? Je peux vous générer le `schema.sql`, le `DatabaseConfig.java`, ou les
premiers modèles.
