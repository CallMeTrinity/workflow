package org.example.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.example.config.SessionManager;
import org.example.exception.AutorisationException;
import org.example.model.User;
import org.example.model.enums.Role;
import org.example.repository.UserRepository;

import java.util.List;

/**
 * Service de gestion des utilisateurs.
 * Fournit les operations de consultation, creation, mise a jour et suppression
 * des utilisateurs avec controle d'autorisation et hachage BCrypt des mots de passe.
 */
public class UserService {

    /** Cout BCrypt utilise pour le hachage des mots de passe. */
    private static final int BCRYPT_COST = 12;

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
     * Cree un nouvel utilisateur. Reserve aux administrateurs.
     * Le mot de passe est hache avec BCrypt avant d'etre persiste.
     * @param lastName le nom de famille
     * @param firstName le prenom
     * @param mail l'adresse email (doit etre unique)
     * @param plainPassword le mot de passe en clair
     * @param role le role de l'utilisateur
     * @param username le nom d'utilisateur (optionnel)
     * @return l'utilisateur cree avec son identifiant genere
     * @throws AutorisationException si l'utilisateur courant n'est pas administrateur
     * @throws IllegalArgumentException si un champ obligatoire est vide ou si l'email existe deja
     */
    public User createUser(String lastName, String firstName, String mail,
                           String plainPassword, Role role, String username) {
        requireAdmin();
        validateRequiredFields(lastName, firstName, mail, role);
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (userRepository.findByMail(mail) != null) {
            throw new IllegalArgumentException("A user with this email already exists");
        }

        String hashedPassword = hashPassword(plainPassword);
        User user = new User(null, lastName, firstName, mail, hashedPassword, role, username);
        Long generatedId = userRepository.save(user);
        user.setId(generatedId);
        return user;
    }

    /**
     * Met a jour un utilisateur existant. Reserve aux administrateurs.
     * @param user l'utilisateur avec les nouvelles valeurs
     * @param newPlainPassword le nouveau mot de passe en clair, ou null/vide pour le conserver
     * @throws AutorisationException si l'utilisateur courant n'est pas administrateur
     * @throws IllegalArgumentException si un champ obligatoire est vide
     *         ou si l'email est deja utilise par un autre utilisateur
     */
    public void updateUser(User user, String newPlainPassword) {
        requireAdmin();
        validateRequiredFields(user.getLastName(), user.getFirstName(), user.getMail(), user.getRole());

        User existing = userRepository.findByMail(user.getMail());
        if (existing != null && !existing.getId().equals(user.getId())) {
            throw new IllegalArgumentException("A user with this email already exists");
        }
        if (newPlainPassword != null && !newPlainPassword.isEmpty()) {
            user.setPassword(hashPassword(newPlainPassword));
        }
        userRepository.update(user);
    }

    /**
     * Supprime un utilisateur. Reserve aux administrateurs.
     * Un administrateur ne peut pas supprimer son propre compte.
     * @param id l'identifiant de l'utilisateur a supprimer
     * @throws AutorisationException si l'utilisateur courant n'est pas administrateur
     * @throws IllegalArgumentException si l'administrateur tente de se supprimer lui-meme
     */
    public void deleteUser(Long id) {
        requireAdmin();
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser.getId().equals(id)) {
            throw new IllegalArgumentException("You cannot delete your own account");
        }
        userRepository.delete(id);
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

    private String hashPassword(String plainPassword) {
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, plainPassword.toCharArray());
    }

    private void validateRequiredFields(String lastName, String firstName, String mail, Role role) {
        if (lastName == null || lastName.isBlank()
                || firstName == null || firstName.isBlank()
                || mail == null || mail.isBlank()
                || role == null) {
            throw new IllegalArgumentException("Last name, first name, email and role are required");
        }
    }

    private void requireAdmin() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            throw new AutorisationException("Only admins can manage users");
        }
    }
}
