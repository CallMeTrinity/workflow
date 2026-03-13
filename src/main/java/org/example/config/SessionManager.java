package org.example.config;

import org.example.model.User;

public class SessionManager {
    private static User currentUser;

    public static User getCurrentUser(){
        return currentUser;
    }

    public static void setCurrentUser(User user){
        currentUser = user;
    }

    public static void clear(){
        currentUser = null;
    }

}
