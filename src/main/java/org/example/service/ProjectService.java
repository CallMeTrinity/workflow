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
        if (!isAdminOrLeader()) {
            throw new AutorisationException("Only admins or project leaders can create projects");
        }
        if (!isValidDate(startDate) || !isValidDate(endDate)) {
            throw new InvalidDateInputException("Invalid date format. Expected format: YYYY-MM-DD");
        }

        User currentUser = SessionManager.getCurrentUser();
        Project project = new Project(null, name, description, startDate, endDate, currentUser.getId());
        Long generatedId = projectRepository.save(project);
        project.setId(generatedId);
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
     * Updates a project. Only admins or the project leader can update.
     */
    public void updateProject(Project project) {
        User currentUser = SessionManager.getCurrentUser();
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isLeader = project.getProjectLeaderId().equals(currentUser.getId());

        if (!isAdmin && !isLeader) {
            throw new AutorisationException("Only admins or the project leader can update this project");
        }
        projectRepository.update(project);
    }

    /**
     * Deletes a project. Only admins or the project leader can delete.
     */
    public void deleteProject(Long id) {
        User currentUser = SessionManager.getCurrentUser();
        Project project = getProjectById(id);
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isLeader = project.getProjectLeaderId().equals(currentUser.getId());

        if (!isAdmin && !isLeader) {
            throw new AutorisationException("Only admins or the project leader can delete this project");
        }
        projectRepository.delete(id);
    }

    /**
     * Adds a member to a project.
     */
    public void addMember(Long projectId, Long userId) {
        if (!isAdminOrLeader()) {
            throw new AutorisationException("Only admins or project leaders can add members");
        }
        projectRepository.addMember(projectId, userId);
    }

    /**
     * Removes a member from a project.
     */
    public void removeMember(Long projectId, Long userId) {
        if (!isAdminOrLeader()) {
            throw new AutorisationException("Only admins or project leaders can remove members");
        }
        projectRepository.removeMember(projectId, userId);
    }

    private boolean isValidDate(String dateStr) {
        return dateStr != null && dateStr.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    private boolean isAdminOrLeader() {
        User currentUser = SessionManager.getCurrentUser();
        return currentUser.getRole() == Role.ADMIN
                || currentUser.getRole() == Role.PROJECT_LEADER;
    }
}
