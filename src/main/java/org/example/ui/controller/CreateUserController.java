package org.example.ui.controller;

import at.favre.lib.crypto.bcrypt.BCrypt;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.User;
import org.example.model.enums.Role;
import org.example.repository.UserRepository;

/**
 * Controleur du formulaire de creation / modification d'un utilisateur.
 */
public class CreateUserController {

    @FXML private TextField lastNameField;
    @FXML private TextField firstNameField;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<Role> roleBox;
    @FXML private Label errorLabel;
    @FXML private Label dialogTitle;
    @FXML private Button submitButton;

    private final UserRepository userRepository = new UserRepository();
    private User userToEdit;

    @FXML
    public void initialize() {
        roleBox.getItems().addAll(Role.values());
    }

    /**
     * Pre-remplit le formulaire pour la modification d'un utilisateur existant.
     */
    public void setUser(User user) {
        this.userToEdit = user;
        dialogTitle.setText("Modifier l'utilisateur");
        submitButton.setText("Enregistrer");
        lastNameField.setText(user.getLastName());
        firstNameField.setText(user.getFirstName());
        emailField.setText(user.getMail());
        usernameField.setText(user.getUsername());
        roleBox.setValue(user.getRole());
        passwordField.setPromptText("Nouveau mot de passe (laisser vide pour ne pas changer)");
    }

    @FXML
    private void handleSubmit() {
        String lastName = lastNameField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        Role role = roleBox.getValue();

        if (lastName.isEmpty() || firstName.isEmpty() || email.isEmpty() || role == null) {
            errorLabel.setText("Nom, prénom, email et rôle sont obligatoires");
            return;
        }

        try {
            if (userToEdit != null) {
                userToEdit.setLastName(lastName);
                userToEdit.setFirstName(firstName);
                userToEdit.setMail(email);
                userToEdit.setUsername(username);
                userToEdit.setRole(role);
                if (!password.isEmpty()) {
                    userToEdit.setPassword(BCrypt.withDefaults()
                            .hashToString(12, password.toCharArray()));
                }
                userRepository.update(userToEdit);
            } else {
                if (password.isEmpty()) {
                    errorLabel.setText("Le mot de passe est obligatoire");
                    return;
                }
                String hashedPassword = BCrypt.withDefaults()
                        .hashToString(12, password.toCharArray());
                User user = new User(null, lastName, firstName, email,
                        hashedPassword, role, username);
                userRepository.save(user);
            }
            ((Stage) lastNameField.getScene().getWindow()).close();
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }
}
