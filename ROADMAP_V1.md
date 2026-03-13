# Feuille de route ProjectRoom

## 5 séances x 5h — 2 développeurs (Dev A = expérimenté, Dev B = débutant)

---

## Séance 1 — Fondations (5h)

**Objectif : projet qui tourne, base de données créée, login fonctionnel**

| Tâche                                                             | Qui | Durée |
|-------------------------------------------------------------------|-----|-------|
| ~~Créer le projet Maven, configurer le pom.xml, init Git/GitLab~~ | A   | 30min |
| ~~Créer la structure des packages~~                               | A   | 20min |
| ~~Écrire le schema.sql (toutes les tables)~~                      | A   | 45min |
| ~~DatabaseConfig.java + initialisation auto du schema.sql~~       | A   | 45min |
| ~~Modèles User, Role (enum), priority (enum), status (enum)~~     | B   | 1h    |
| UserRepository (save, findByEmail, findById)                      | A   | 45min |
| AuthService (login, hash bcrypt, SessionManager)                  | A   | 45min |
| login.fxml + LoginController basique                              | B   | 1h    |

**Fin de séance 1 :** on peut se connecter avec un user créé manuellement en base

---

## Séance 2 — Gestion de projets et tâches (5h)

**Objectif : CRUD projets et tâches, kanban basique**

| Tâche                                                         | Qui | Durée |
|---------------------------------------------------------------|-----|-------|
| Modèles Projet, Tache, UserStory + enums                      | B   | 1h    |
| ProjetRepository (CRUD complet)                               | A   | 45min |
| TacheRepository (CRUD + findByAssignee)                       | A   | 45min |
| ProjetService + TacheService (logique métier, droits)         | A   | 1h    |
| dashboard.fxml + DashboardController (liste projets)          | B   | 1h    |
| kanban.fxml + KanbanController (3 colonnes, affichage tâches) | B   | 1h    |
| Tests JUnit ProjetService + TacheService                      | A   | 30min |

**Fin de séance 2 :** on peut créer un projet, des tâches, les déplacer dans le kanban

---

## Séance 3 — Salles et réservations (5h)

**Objectif : gestion des salles, création de réservations**

| Tâche                                              | Qui | Durée |
|----------------------------------------------------|-----|-------|
| Modèles Salle, Reservation, Creneau                | B   | 45min |
| SalleRepository + ReservationRepository            | A   | 1h    |
| SalleService + ReservationService (vérif conflits) | A   | 1h    |
| salle.fxml + SalleController (liste, CRUD admin)   | B   | 1h    |
| reservation.fxml + ReservationController           | B   | 1h    |
| Tests JUnit ReservationService (conflits)          | A   | 30min |
| Branchement des vues entre elles (navigation)      | A   | 30min |

**Fin de séance 3 :** on peut gérer les salles et créer des réservations sans conflit

---

## Séance 4 — Planning et notifications (5h)

**Objectif : vue planning, recherche de créneaux, notifications**

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
