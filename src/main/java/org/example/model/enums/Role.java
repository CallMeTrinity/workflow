package org.example.model.enums;

// Role possible d'un utilisateur dans l'application

public enum Role {
    MEMBER, // membre d'un projet, avec des droits limités pour consulter et participer aux projets
    PROJECT_LEADER, // chef de projet, avec des droits supplémentaires pour gérer les projets
    ADMIN // administrateur de l'application, avec tous les droits
}

