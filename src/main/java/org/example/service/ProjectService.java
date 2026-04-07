package org.example.service;

import org.example.config.SessionManager;
import org.example.exception.AutorisationException;
import org.example.exception.InvalidDateInputException;
import org.example.exception.NotFoundException;
import org.example.model.Project;
import org.example.model.User;
import org.example.model.enums.Role;
import org.example.repository.ProjectRepository;

import java.util.List;

/**
 * Service for managing projects.
 */
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService() {
        this.projectRepository = new ProjectRepository();
    }

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    /**
     * Creates a new project. Only admins and project leaders can create projects.
     */
    public Project createProject(String name, String description, String startDate, String endDate) {
        return createProject(name, description, startDate, endDate, SessionManager.getCurrentUser().getId());
    }

    public Project createProject(String name, String description, String startDate, String endDate, Long leaderId) {
        if (!isAdminOrProjectLeader()) {
            throw new AutorisationException("Only admins or project leaders can create projects");
        }
        if (!isValidDate(startDate) || !isValidDate(endDate)) {
            throw new InvalidDateInputException("Invalid date format. Expected format: YYYY-MM-DD");
        }

        Project project = new Project(null, name, description, startDate, endDate, leaderId);
        Long generatedId = projectRepository.save(project);
        project.setId(generatedId);
        // Le chef de projet est automatiquement membre du projet
        projectRepository.addMember(generatedId, leaderId);
        return project;
    }

    /**
     * Returns a project by its ID.
     * @throws NotFoundException if no project matches the given ID
     */
    public Project getProjectById(Long id) {
        Project project = projectRepository.findById(id);
        if (project == null) throw new NotFoundException("Project not found");
        return project;
    }

    /**
     * Returns all projects.
     */
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    /**
     * Updates a project. Only admins or the leader of this project can update.
     */
    public void updateProject(Project project) {
        if (!isAdminOrProjectLeader(project)) {
            throw new AutorisationException("Only admins or the project leader can update this project");
        }
        projectRepository.update(project);
        // S'assurer que le chef (potentiellement nouveau) est membre
        List<Long> memberIds = projectRepository.findMemberIds(project.getId());
        if (!memberIds.contains(project.getProjectLeaderId())) {
            projectRepository.addMember(project.getId(), project.getProjectLeaderId());
        }
    }

    /**
     * Deletes a project. Only admins or the leader of this project can delete.
     */
    public void deleteProject(Long id) {
        Project project = getProjectById(id);
        if (!isAdminOrProjectLeader(project)) {
            throw new AutorisationException("Only admins or the project leader can delete this project");
        }
        projectRepository.delete(id);
    }

    /**
     * Adds a member to a project. Only admins or the leader of this project can add members.
     */
    public void addMember(Long projectId, Long userId) {
        Project project = getProjectById(projectId);
        if (!isAdminOrProjectLeader(project)) {
            throw new AutorisationException("Only admins or the project leader can add members");
        }
        projectRepository.addMember(projectId, userId);
    }

    /**
     * Removes a member from a project. Only admins or the leader of this project can remove members.
     */
    public void removeMember(Long projectId, Long userId) {
        Project project = getProjectById(projectId);
        if (!isAdminOrProjectLeader(project)) {
            throw new AutorisationException("Only admins or the project leader can remove members");
        }
        projectRepository.removeMember(projectId, userId);
    }

    private boolean isValidDate(String dateStr) {
        return dateStr != null && dateStr.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    /**
     * Vérifie si le user courant est admin ou project leader (sans contexte de projet).
     * Utilisé pour createProject.
     */
    private boolean isAdminOrProjectLeader() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) return false;
        return currentUser.getRole() == Role.ADMIN
                || currentUser.getRole() == Role.PROJECT_LEADER;
    }

    /**
     * Vérifie si le user courant est admin ou leader de CE projet.
     * Utilisé pour update, delete, addMember, removeMember.
     */
    private boolean isAdminOrProjectLeader(Project project) {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) return false;
        if (currentUser.getRole() == Role.ADMIN) return true;
        return project.getProjectLeaderId().equals(currentUser.getId());
    }
}
