# Feuille de route ProjectRoom

## 5 seances x 5h — 2 developpeurs (Dev A = experimente, Dev B = debutant)

---

## Seance 1 — Fondations (5h)

**Objectif : projet qui tourne, base de donnees creee, login fonctionnel**

| Tache                                                             | Qui | Duree estime |
|-------------------------------------------------------------------|-----|--------------|
| ~~Creer le projet Maven, configurer le pom.xml, init Git/GitLab~~ | A   | 30min        |
| ~~Creer la structure des packages~~                               | A   | 20min        |
| ~~Ecrire le schema.sql (toutes les tables)~~                      | A   | 45min        |
| ~~DatabaseConfig.java + initialisation auto du schema.sql~~       | A   | 45min        |
| ~~Modeles User, Role (enum), priority (enum), status (enum)~~     | B   | 1h           |
| ~~UserRepository (save, findByEmail, findById)~~                  | A   | 45min        |
| ~~AuthService (login, hash bcrypt, SessionManager)~~              | A   | 45min        |
| ~~login.fxml + LoginController basique~~                          | B   | 1h           |
| ~~Modeles Projet, Tache, UserStory + enums~~                      | B   | 1h           |

**Fin de seance 1 :** on peut se connecter avec un user cree manuellement en base

---

## Seance 2 — Gestion de projets et taches (5h)

**Objectif : CRUD projets et taches, kanban basique**

| Tache                                                             | Qui | Duree |
|-------------------------------------------------------------------|-----|-------|
| ~~ProjetRepository (CRUD complet)~~                               | A   | 45min |
| ~~TacheRepository (CRUD + findByAssignee)~~                       | A   | 45min |
| ~~ProjetService + TacheService (logique metier, droits)~~         | A   | 1h    |
| ~~dashboard.fxml + DashboardController (liste projets)~~          | B   | 1h    |
| ~~kanban.fxml + KanbanController (3 colonnes, affichage taches)~~ | B   | 1h    |
| ~~Tests JUnit ProjetService + TacheService~~                      | A   | 30min |

**Fin de seance 2 :** on peut creer un projet, des taches, les deplacer dans le kanban

---

## Seance 3 — Salles et reservations (5h)

**Objectif : gestion des salles, creation de reservations**

| Tache                                                  | Qui | Duree |
|--------------------------------------------------------|-----|-------|
| ~~Amelioration de l'existant~~                         | B   | 3h    |
| ~~Modeles Salle, Reservation~~                         | A   | 45min |
| ~~SalleRepository + ReservationRepository~~            | A   | 1h    |
| ~~SalleService + ReservationService (verif conflits)~~ | A   | 1h    |
| ~~salle.fxml + SalleController (liste, CRUD admin)~~   | A   | 1h    |
| ~~reservation.fxml + ReservationController~~           | A   | 1h    |
| ~~Tests JUnit ReservationService (conflits)~~          | A   | 30min |
| ~~Branchement des vues entre elles (navigation)~~      | A   | 30min |

**Fin de seance 3 :** on peut gerer les salles et creer des reservations sans conflit

---

## Seance 4 — Planning et notifications (5h)

**Objectif : vue planning, recherche de creneaux, notifications**

| Tache                                                          | Qui | Duree |
|----------------------------------------------------------------|-----|-------|
| ~~Modele PlanningItem + PlanningService complet~~              | A   | 1h30  |
| ~~Algorithme findCreneauxLibres~~                              | A   | 1h    |
| ~~Tests JUnit PlanningService (algo creneaux)~~                | A   | 30min |
| ~~planning.fxml + PlanningController (vue calendrier)~~        | B   | 1h30  |
| ~~Modele Notification + NotificationRepository~~               | B   | 30min |
| ~~NotificationService + branchement dans les autres services~~ | B   | 30min |
| ~~notification.fxml + NotificationController~~                 | B   | 30min |
| ~~Amelioration UX et UI~~                                      | A   | 2h    |
| ~~Ameliorer reservation salle en fonction du nombre de gens~~  | A   | 2h    |

**Fin de seance 4 :** planning consultable, creneaux libres trouvables, notifications in-app

---

## Seance 5 — Roles, finitions, qualite (5h)

**Objectif : gestion des roles, polish, qualite du code**

| Tache                                                  | Qui | Duree |
|--------------------------------------------------------|-----|-------|
| ~~AdminController (gestion users, salles)~~            | B   | 1h    |
| ~~Verification des restrictions par role partout~~     | A   | 45min |
| ~~UserStoryRepository + UserStoryService~~             | A   | 45min |
| ~~Amelioration UI (CSS, coherence visuelle)~~          | B   | 1h30  |
| ~~Checkstyle — corriger les violations~~               | A+B | 30min |
| ~~Javadoc sur les services et repositories~~           | A+B | 30min |
| ~~README (install, lancer le projet, stack)~~          | B   | 30min |
| ~~Tests unitaires et d'integration (233 tests)~~       | A+B | 30min |

**Fin de seance 5 :** projet complet, propre, documente, pret pour la demo

---

## Bilan

- 5 seances realisees sur 5
- 21 controleurs, 7 services, 7 repositories
- 233 tests (unitaires + integration), couverture > 80%
- 0 violation Checkstyle
- Projet pret pour la beta 1
