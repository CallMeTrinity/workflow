package org.example.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.example.config.SessionManager;
import org.example.exception.InvalidLoginCredentialsException;
import org.example.exception.UserNotFoundException;
import org.example.model.User;
import org.example.repository.UserRepository;

/**
 * Service d'authentification.
 * Gere la connexion, la deconnexion et la session utilisateur via BCrypt.
 */
public class AuthService {

    private final UserRepository userRepository = new UserRepository();

    /**
     * Authentifie un utilisateur par email et mot de passe.
     * @param mail l'adresse email
     * @param password le mot de passe en clair
     * @return l'utilisateur authentifie
     * @throws UserNotFoundException si aucun utilisateur ne correspond a l'email
     * @throws InvalidLoginCredentialsException si le mot de passe est incorrect
     */
    public User login(String mail, String password) {
        User user = userRepository.findByMail(mail);

        if (user == null){
            throw new UserNotFoundException("No user associated to this mail");
        }

        String storedHash = user.getPassword();

        if (!BCrypt.verifyer().verify(
                password.toCharArray(),
                storedHash
        ).verified){
            throw new InvalidLoginCredentialsException("Invalid password");
        }

        SessionManager.setCurrentUser(user);
        return user;
    }

    /** Deconnecte l'utilisateur courant en vidant la session. */
    public void logout(){
        SessionManager.clear();
    }

    /**
     * Retourne l'utilisateur actuellement connecte.
     * @return l'utilisateur courant ou null si aucune session active
     */
    public User getCurrentUser(){
        return SessionManager.getCurrentUser();
    }
}
