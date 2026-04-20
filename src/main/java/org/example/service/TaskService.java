package org.example.service;

import org.example.config.SessionManager;
import org.example.exception.AutorisationException;
import org.example.exception.NotFoundException;
import org.example.model.Project;
import org.example.model.Task;
import org.example.model.User;
import org.example.model.enums.Priority;
import org.example.model.enums.Role;
import org.example.model.enums.Status;
import org.example.repository.TaskRepository;
import org.example.repository.ProjectRepository;

import java.util.List;


/**
 * Service de gestion des taches.
 * Fournit les operations CRUD et l'assignation avec controle d'autorisation.
 */
public class TaskService {

    private final TaskRepository taskRepository;
    private final NotificationService notificationService;
    private final ProjectRepository projectRepository;

    /** Constructeur par defaut. */
    public TaskService() {
        this.taskRepository = new TaskRepository();
        this.notificationService = new NotificationService();
        this.projectRepository = new ProjectRepository();
    }

    /**
     * Constructeur avec injection du repository.
     * @param taskRepository le repository des taches
     */
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
        this.notificationService = new NotificationService();
        this.projectRepository = new ProjectRepository();
    }

    /**
     * Constructeur avec injection du repository et du service de notifications.
     * @param taskRepository le repository des taches
     * @param notificationService le service de notifications
     */
    public TaskService(TaskRepository taskRepository, NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.notificationService = notificationService;
        this.projectRepository = new ProjectRepository();
    }

    /**
     * Constructeur avec injection de toutes les dependances.
     * @param taskRepository le repository des taches
     * @param notificationService le service de notifications
     * @param projectRepository le repository des projets
     */
    public TaskService(TaskRepository taskRepository, NotificationService notificationService,
                       ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.notificationService = notificationService;
        this.projectRepository = projectRepository;
    }


    /**
     * Cree une tache sans responsable de tache.
     * @param title le titre de la tache
     * @param description la description de la tache
     * @param status le statut initial
     * @param priority la priorite
     * @param deadline la date limite
     * @param timeEstimate l'estimation de temps en heures
     * @param assignedUserId l'identifiant de l'utilisateur assigne
     * @param projectId l'identifiant du projet
     * @param userStoryId l'identifiant de la user story associee
     * @return la tache creee
     */
    public Task createTask(String title, String description, Status status, Priority priority,
                           String deadline, Integer timeEstimate, Long assignedUserId,
                           Long projectId, Long userStoryId) {
        return createTask(title, description, status, priority, deadline, timeEstimate,
                assignedUserId, projectId, userStoryId, null);
    }

    /**
     * Cree une tache avec tous les parametres.
     * @param title le titre de la tache
     * @param description la description de la tache
     * @param status le statut initial
     * @param priority la priorite
     * @param deadline la date limite
     * @param timeEstimate l'estimation de temps en heures
     * @param assignedUserId l'identifiant de l'utilisateur assigne
     * @param projectId l'identifiant du projet
     * @param userStoryId l'identifiant de la user story associee
     * @param taskLeaderId l'identifiant du responsable de tache
     * @return la tache creee
     */
    public Task createTask(String title, String description, Status status, Priority priority,
                           String deadline, Integer timeEstimate, Long assignedUserId,
                           Long projectId, Long userStoryId, Long taskLeaderId) {
        if (!isAdminOrLeader()) {
            throw new AutorisationException("Only admins or project leaders can create tasks");
        }
        Task task = new Task(null, title, description, status, priority, deadline,
                timeEstimate, projectId, userStoryId, assignedUserId, taskLeaderId);
        Long generatedId = taskRepository.save(task);
        task.setId(generatedId);
        if (assignedUserId != null) {
            notifyAssignment(assignedUserId, title, projectId);
        }
        return task;
    }

    /**
     * Returns a task by its ID.
     * @throws NotFoundException if no task matches the given ID
     */
    public Task getTaskById(Long id) {
        Task task = taskRepository.findById(id);
        if (task == null) throw new NotFoundException("Task not found");
        return task;
    }


    /**
     * Retourne toutes les taches d'un projet.
     * @param projectId l'identifiant du projet
     * @return la liste des taches du projet
     */
    public List<Task> getTasksByProject(Long projectId) {
        return taskRepository.findByProject(projectId);
    }

    /**
     * Retourne toutes les taches assignees a un utilisateur.
     * @param userId l'identifiant de l'utilisateur
     * @return la liste des taches assignees
     */
    public List<Task> getTasksByAssignedUser(Long userId) {
        return taskRepository.findByAssignedUser(userId);
    }


    /**
     * Assigne une tache a un utilisateur.
     * @param taskId l'identifiant de la tache
     * @param userId l'identifiant de l'utilisateur a assigner
     */
    public void assignTask(Long taskId, Long userId) {
        if (!isAdminOrLeader()) {
            throw new AutorisationException("Only admins or project leaders can assign tasks");
        }
        Task task = getTaskById(taskId);
        task.setAssignedUserId(userId);
        taskRepository.update(task);
        notifyAssignment(userId, task.getTitle(), task.getProjectId());
    }


    /**
     * Met a jour le statut d'une tache.
     * @param taskId l'identifiant de la tache
     * @param newStatus le nouveau statut
     */
    public void updateTaskStatus(Long taskId, Status newStatus) {
        User currentUser = SessionManager.getCurrentUser();
        Task task = getTaskById(taskId);

        boolean isAdminOrLeader = isAdminOrLeader();
        boolean isAssignee = task.getAssignedUserId() != null
                && task.getAssignedUserId().equals(currentUser.getId());

        if (!isAdminOrLeader && !isAssignee) {
            throw new AutorisationException("You are not allowed to update this task's status");
        }
        task.setStatus(newStatus);
        taskRepository.update(task);
    }


    /**
     * Met a jour une tache existante.
     * @param task la tache avec les nouvelles valeurs
     */
    public void updateTask(Task task) {
        if (!isAdminOrLeader()) {
            throw new AutorisationException("Only admins or project leaders can update tasks");
        }

        // Check if assignee changed → send notification
        Task oldTask = taskRepository.findById(task.getId());
        Long oldAssignee = oldTask != null ? oldTask.getAssignedUserId() : null;
        Long newAssignee = task.getAssignedUserId();

        taskRepository.update(task);

        if (newAssignee != null && !newAssignee.equals(oldAssignee)) {
            notifyAssignment(newAssignee, task.getTitle(), task.getProjectId());
        }
    }


    /**
     * Supprime une tache par son identifiant.
     * @param id l'identifiant de la tache a supprimer
     */
    public void deleteTask(Long id) {
        if (!isAdminOrLeader()) {
            throw new AutorisationException("Only admins or project leaders can delete tasks");
        }
        taskRepository.delete(id);
    }

    private void notifyAssignment(Long assigneeId, String taskTitle, Long projectId) {
        User currentUser = SessionManager.getCurrentUser();
        String assignedBy = currentUser != null ? currentUser.getFullName() : "Quelqu'un";

        String projectName = null;
        if (projectId != null) {
            Project project = projectRepository.findById(projectId);
            if (project != null) projectName = project.getName();
        }

        StringBuilder msg = new StringBuilder();
        msg.append(assignedBy).append(" vous a assigné la tâche \"").append(taskTitle).append("\"");
        if (projectName != null) {
            msg.append(" dans le projet ").append(projectName);
        }

        notificationService.createNotification(assigneeId, msg.toString(), projectId);
    }

    private boolean isAdminOrLeader() {
        User currentUser = SessionManager.getCurrentUser();
        return currentUser != null &&
                (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.PROJECT_LEADER);
    }
}
