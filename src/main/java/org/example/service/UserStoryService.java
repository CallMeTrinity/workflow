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

public class UserStoryService {

    private final UserStoryRepository userStoryRepository;

    public UserStoryService() {
        this.userStoryRepository = new UserStoryRepository();
    }

    public UserStoryService(UserStoryRepository userStoryRepository) {
        this.userStoryRepository = userStoryRepository;
    }

    public UserStory createUserStory(String title, String description, Priority priority, Long projectId) {
        if (!isAdminOrLeader()) {
            throw new AutorisationException("Only admins or project leaders can create user stories");
        }
        UserStory userStory = new UserStory(null, title, description, priority, projectId);
        Long generatedId = userStoryRepository.save(userStory);
        userStory.setId(generatedId);
        return userStory;
    }

    public UserStory getUserStoryById(Long id) {
        UserStory userStory = userStoryRepository.findById(id);
        if (userStory == null) throw new NotFoundException("User story not found");
        return userStory;
    }

    public List<UserStory> getUserStoriesByProject(Long projectId) {
        return userStoryRepository.findByProject(projectId);
    }

    public void updateUserStory(UserStory userStory) {
        if (!isAdminOrLeader()) {
            throw new AutorisationException("Only admins or project leaders can update user stories");
        }
        userStoryRepository.update(userStory);
    }

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
