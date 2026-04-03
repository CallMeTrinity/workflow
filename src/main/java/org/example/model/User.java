package org.example.model;

import org.example.model.enums.Role;

// Représente un utilisateur de l'application

public class User {

    private Long id;
    private String lastName;
    private String firstName;
    private String mail;
    private String password;
    private Role role;
    private String username;

    public User() {
    }

    // Constructeur principal

    public User(Long id, String lastName, String firstName, String mail, String password, Role role, String username) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.mail = mail;
        this.password = password;
        this.role = role;
        this.username = username;
    }

    // Getter et Setter

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Retourne le nom complet de l'utilisateur

    public String getFullName() {
        return firstName + " " + lastName;
    }
}


