# ProjectRoom

Application de bureau JavaFX pour la gestion de projets : tableaux Kanban, suivi des taches, et reservation de salles de reunion.

## Description

ProjectRoom est un outil de gestion de projets collaboratif qui permet aux equipes de planifier, organiser et suivre leurs taches via une interface Kanban intuitive avec glisser-deposer. L'application integre egalement un systeme de reservation de salles de reunion avec detection de conflits et un calendrier hebdomadaire.

## Stack technique

| Technologie | Version |
|-------------|---------|
| Java        | 21      |
| JavaFX      | 21      |
| Maven       | 3.8+    |
| SQLite      | Embarque (JDBC) |
| BCrypt      | Hachage des mots de passe |
| JUnit 5     | Tests unitaires |
| Mockito     | Mocks pour les tests |

## Prerequis

- **Java 21** ou superieur
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
- **Calendrier hebdomadaire** -- visualisation des reservations sur la semaine
- **Notifications** -- alertes pour les evenements importants
- **Controle d'acces par role** -- trois niveaux : `ADMIN`, `PROJECT_LEADER`, `MEMBER`
- **Profils utilisateurs** -- consultation et modification du profil

## Structure du projet

```
src/main/java/org/example/
    config/          -- DatabaseConfig, SessionManager
    exception/       -- exceptions metier
    model/           -- POJOs (User, Project, Task, UserStory, Room, Reservation, ...)
    repository/      -- couche d'acces aux donnees (JDBC)
    service/         -- logique metier (AuthService, ProjectService, TaskService, ...)
    ui/
        controller/  -- controleurs JavaFX
        util/        -- utilitaires UI
    Main.java        -- point d'entree de l'application

src/main/resources/
    fxml/            -- vues FXML
    db/schema.sql    -- schema de la base de donnees + donnees initiales
```

## Tests

Les tests utilisent **JUnit 5** et **Mockito**. Les dependances vers les repositories sont mockees : aucune connexion a la base de donnees n'est necessaire.

```bash
# Executer tous les tests
mvn test

# Executer un test specifique
mvn test -Dtest=NomDuTest
```

Les classes de test se trouvent dans `src/test/java/`.
