package org.example.service;

import org.example.model.User;
import org.example.repository.UserRepository;

import java.util.List;

/**
 * Service de gestion des utilisateurs.
 * Fournit les operations de consultation et de mise a jour des profils.
 */
public class UserService {

    private final UserRepository userRepository;

    /** Constructeur par defaut. */
    public UserService() {
        this.userRepository = new UserRepository();
    }

    /**
     * Constructeur avec injection du repository.
     * @param userRepository le repository des utilisateurs
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Met a jour le nom d'utilisateur.
     * @param userId l'identifiant de l'utilisateur
     * @param username le nouveau nom d'utilisateur
     */
    public void updateUsername(Long userId, String username) {
        userRepository.updateUsername(userId, username);
    }

    /**
     * Retourne tous les utilisateurs.
     * @return la liste de tous les utilisateurs
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Retourne un utilisateur par son identifiant.
     * @param id l'identifiant de l'utilisateur
     * @return l'utilisateur trouve ou null
     */
    public User getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Met a jour le profil d'un utilisateur.
     * @param id l'identifiant de l'utilisateur
     * @param username le nouveau nom d'utilisateur
     */
    public void updateProfile(Long id, String username) {
        userRepository.updateUsername(id, username);
    }
}
