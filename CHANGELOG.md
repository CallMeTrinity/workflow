# Changelog

Toutes les modifications notables du projet sont documentees dans ce fichier.
Le format est base sur [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/)
et ce projet suit le [Semantic Versioning](https://semver.org/lang/fr/).

## [1.1.0-beta.1] - 2026-07-01

### Ajoute
- Theme sombre avec bascule dans la barre superieure, persiste entre les lancements
- Cartes de statistiques sur le dashboard (projets, taches en cours, reunions a venir, notifications non lues)
- Colonne "Avancement" avec barre de progression par projet dans le tableau des projets
- Recherche plein texte des taches dans le Kanban (titre + description)
- Compteur de taches par colonne dans le Kanban

### Modifie
- **Toutes les boites de dialogue (creation/edition, confirmations, erreurs) s'affichent
  desormais en superposition dans la fenetre principale** au lieu d'ouvrir une fenetre
  separee. Le comportement est identique sur toutes les plateformes et corrige le bug
  macOS ou les modaux ouvraient une nouvelle fenetre en mode plein ecran.
- Fermeture des modaux via ECHAP, clic sur le fond assombri ou bouton de fermeture
- Refonte de la feuille de style : palette centralisee en design tokens
  (looked-up colors), styles harmonises pour tableaux, listes, formulaires,
  menus contextuels, calendrier et barres de defilement
- Animations de fondu/zoom a l'ouverture et la fermeture des modaux
- Cartes de taches du Kanban stylees via classes CSS (compatibles theme sombre)

## [1.0.0-beta.1] - 2026-04-22

### Ajoute
- Authentification securisee avec hachage BCrypt
- Gestion de projets (CRUD complet avec roles)
- Tableau Kanban 3 colonnes avec glisser-deposer (TODO, IN_PROGRESS, DONE)
- Affectation de taches aux membres du projet
- User Stories liees aux projets
- Gestion des salles de reunion (CRUD admin)
- Reservation de creneaux avec detection de conflits horaires
- Algorithme de recherche de creneaux libres (multi-participants + auto-room)
- Calendrier hebdomadaire de visualisation des reservations
- Systeme de notifications in-app (assignation de taches, invitations)
- Panneau d'administration (gestion des utilisateurs)
- Profils utilisateurs (consultation et modification)
- Controle d'acces par role (ADMIN, PROJECT_LEADER, MEMBER)
- 233 tests (unitaires + integration) avec couverture > 80%
- Tests d'integration avec SQLite en memoire pour tous les repositories
- Style CSS unifie (theme clair)
- Javadoc sur les services et repositories

### Technique
- Java 25, JavaFX 25
- SQLite embarque avec schema auto-initialise
- Architecture en couches : Controller > Service > Repository > Model
- Mockito 5.21.0, JaCoCo 0.8.14
- Checkstyle Google Style (0 violation)

## [0.4.0] - 2026-04-21

### Ajoute
- Systeme de notifications pour les assignations de taches
- Champ project_id dans les notifications
- Vue planning avec calendrier hebdomadaire
- Algorithme findAvailableSlots et findAvailableSlotsWithAutoRoom
- Selection automatique de salle en fonction du nombre de participants

## [0.3.0] - 2026-04-14

### Ajoute
- Modeles Salle et Reservation
- Repositories et services pour salles et reservations
- Detection de conflits horaires
- Vues FXML pour salles et reservations
- Navigation entre les vues

## [0.2.0] - 2026-04-07

### Ajoute
- CRUD complet pour les projets
- CRUD complet pour les taches
- Tableau Kanban avec 3 colonnes
- Services ProjectService et TaskService avec verification des droits
- Tests unitaires pour les services

## [0.1.0] - 2026-03-31

### Ajoute
- Structure Maven initiale et configuration du pom.xml
- Schema SQL complet (users, project, task, user_story, room, reservation)
- DatabaseConfig avec initialisation automatique du schema
- Modeles User, Project, Task, UserStory avec enums (Role, Status, Priority)
- UserRepository, AuthService avec hachage BCrypt
- SessionManager pour la gestion de session statique
- Ecran de connexion (login.fxml + LoginController)
