package org.example.service;

import org.example.config.SessionManager;
import org.example.exception.AutorisationException;
import org.example.exception.NotFoundException;
import org.example.model.UserStory;
import org.example.model.User;
import org.example.model.enums.Priority;
import org.example.model.enums.Role;
import org.example.repository.UserStoryRepository;

import java.util.List;

/**
 * Service de gestion des user stories.
 * Fournit les operations CRUD avec controle d'autorisation.
 */
public class UserStoryService {

    private final UserStoryRepository userStoryRepository;

    /** Constructeur par defaut. */
    public UserStoryService() {
        this.userStoryRepository = new UserStoryRepository();
    }

    /**
     * Constructeur avec injection du repository.
     * @param userStoryRepository le repository des user stories
     */
    public UserStoryService(UserStoryRepository userStoryRepository) {
        this.userStoryRepository = userStoryRepository;
    }

    /**
     * Cree une user story dans un projet.
     * @param title le titre
     * @param description la description
     * @param priority la priorite
     * @param projectId l'identifiant du projet
     * @return la user story creee
     */
    public UserStory createUserStory(String title, String description, Priority priority, Long projectId) {
        if (!isAdminOrLeader()) {
            throw new AutorisationException("Only admins or project leaders can create user stories");
        }
        UserStory userStory = new UserStory(null, title, description, priority, projectId);
        Long generatedId = userStoryRepository.save(userStory);
        userStory.setId(generatedId);
        return userStory;
    }

    /**
     * Retourne une user story par son identifiant.
     * @param id l'identifiant de la user story
     * @return la user story trouvee
     * @throws NotFoundException si la user story n'existe pas
     */
    public UserStory getUserStoryById(Long id) {
        UserStory userStory = userStoryRepository.findById(id);
        if (userStory == null) throw new NotFoundException("User story not found");
        return userStory;
    }

    /**
     * Retourne toutes les user stories d'un projet.
     * @param projectId l'identifiant du projet
     * @return la liste des user stories
     */
    public List<UserStory> getUserStoriesByProject(Long projectId) {
        return userStoryRepository.findByProject(projectId);
    }

    /**
     * Met a jour une user story existante.
     * @param userStory la user story avec les nouvelles valeurs
     */
    public void updateUserStory(UserStory userStory) {
        if (!isAdminOrLeader()) {
            throw new AutorisationException("Only admins or project leaders can update user stories");
        }
        userStoryRepository.update(userStory);
    }

    /**
     * Supprime une user story par son identifiant.
     * @param id l'identifiant de la user story
     */
    public void deleteUserStory(Long id) {
        if (!isAdminOrLeader()) {
            throw new AutorisationException("Only admins or project leaders can delete user stories");
        }
        userStoryRepository.delete(id);
    }

    private boolean isAdminOrLeader() {
        User currentUser = SessionManager.getCurrentUser();
        return currentUser != null
                && (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.PROJECT_LEADER);
    }
}
