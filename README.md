# ProjectRoom

Application de bureau JavaFX pour la gestion de projets : tableaux Kanban, suivi des taches, et reservation de salles de reunion.

## Description

ProjectRoom est un outil de gestion de projets collaboratif qui permet aux equipes de planifier, organiser et suivre leurs taches via une interface Kanban intuitive avec glisser-deposer. L'application integre egalement un systeme de reservation de salles de reunion avec detection de conflits et un calendrier hebdomadaire.

## Stack technique

| Technologie | Version |
|-------------|---------|
| Java        | 25      |
| JavaFX      | 25      |
| Maven       | 3.8+    |
| SQLite      | Embarque (JDBC) |
| BCrypt      | Hachage des mots de passe |
| JUnit 5     | Tests unitaires |
| Mockito     | Tests unitaires (mocks) |
| JaCoCo      | Couverture de code |

## Prerequis

- **Java 25** ou superieur
- **Maven 3.8** ou superieur

## Installation et lancement

```bash
git clone <url-du-depot>
cd projet
mvn javafx:run
```

## Commandes utiles

```bash
# Lancer l'application
mvn javafx:run

# Compiler et verifier (tests + couverture + checkstyle)
mvn clean verify

# Compiler le projet
mvn clean package

# Executer tous les tests
mvn test

# Executer un test specifique
mvn test -Dtest=TaskServiceTest

# Verifier le style de code (Google Style)
mvn checkstyle:check
```

## Identifiants par defaut

| Email               | Mot de passe |
|---------------------|--------------|
| `admin@project.com` | `admin123`   |

## Architecture

L'application suit une architecture en couches :

```
FXML + Controllers (ui/controller/)
        |
   Services (service/)          -- logique metier + autorisation
        |
   Repositories (repository/)   -- requetes JDBC
        |
   Models (model/)              -- POJOs
        |
   SQLite                       -- base de donnees embarquee
```

La session utilisateur est geree par `SessionManager` qui conserve l'utilisateur authentifie de maniere statique. Toutes les methodes des services verifient les permissions en fonction du role.

## Fonctionnalites

- **Authentification** -- connexion / deconnexion securisee avec BCrypt
- **Gestion de projets** -- creation, modification, suppression de projets
- **Tableau Kanban** -- colonnes TODO / IN_PROGRESS / DONE avec glisser-deposer
- **Affectation de taches** -- assignation de taches aux membres du projet
- **User Stories** -- regroupement des taches par user story
- **Salles de reunion** -- gestion des salles disponibles
- **Reservations** -- reservation de creneaux avec detection de conflits
- **Recherche de creneaux** -- algorithme de recherche de creneaux libres multi-participants
- **Calendrier hebdomadaire** -- visualisation des reservations sur la semaine
- **Notifications** -- alertes pour les assignations de taches et invitations
- **Administration** -- panneau d'administration pour la gestion des utilisateurs
- **Controle d'acces par role** -- trois niveaux : `ADMIN`, `PROJECT_LEADER`, `MEMBER`
- **Profils utilisateurs** -- consultation et modification du profil

## Structure du projet

```
src/main/java/org/example/
    config/          -- DatabaseConfig, SessionManager
    exception/       -- exceptions metier
    model/           -- POJOs (User, Project, Task, UserStory, Room, Reservation, Notification)
    repository/      -- couche d'acces aux donnees (JDBC)
    service/         -- logique metier (AuthService, ProjectService, TaskService, ...)
    ui/
        controller/  -- controleurs JavaFX (21 controleurs)
        util/        -- utilitaires UI
    Main.java        -- point d'entree de l'application

src/main/resources/
    css/             -- feuilles de style
    fxml/            -- vues FXML
    db/schema.sql    -- schema de la base de donnees + donnees initiales
```

## Tests

Le projet contient **233 tests** repartis en deux categories :

### Tests unitaires
Les services sont testes avec **Mockito** : les repositories sont mockes pour isoler la logique metier. Les modeles sont egalement testes (constructeurs, getters/setters).

### Tests d'integration
Les repositories sont testes avec une **base SQLite en memoire** (`jdbc:sqlite::memory:`) pour verifier les requetes SQL, le mapping des resultats et les contraintes de cles etrangeres. Chaque test demarre avec une base vierge pour garantir l'isolation.

```bash
# Executer tous les tests
mvn test

# Executer un test specifique
mvn test -Dtest=NomDuTest

# Verifier la couverture (rapport dans target/site/jacoco/)
mvn clean verify
```

La couverture de code cible est de **80%** sur les services et modeles (les controleurs UI et repositories sont exclus du seuil JaCoCo).

## Versioning

Le projet suit le [Semantic Versioning](https://semver.org/lang/fr/). Voir le fichier [CHANGELOG.md](CHANGELOG.md) pour l'historique des versions.

**Version actuelle : 1.0.0-beta.1**
