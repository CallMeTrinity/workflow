package org.example.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.example.config.SessionManager;
import org.example.exception.InvalidLoginCredentialsException;
import org.example.exception.UserNotFoundException;
import org.example.model.User;
import org.example.repository.UserRepository;

public class AuthService {

    private final UserRepository userRepository = new UserRepository();

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

    public void logout(){
        SessionManager.clear();
    }

    public User getCurrentUser(){
        return SessionManager.getCurrentUser();
    }
}
