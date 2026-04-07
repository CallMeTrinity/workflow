package org.example.service;

import org.example.config.SessionManager;
import org.example.exception.AutorisationException;
import org.example.exception.NotFoundException;
import org.example.model.Task;
import org.example.model.User;
import org.example.model.enums.Priority;
import org.example.model.enums.Role;
import org.example.model.enums.Status;
import org.example.repository.TaskRepository;

import java.util.List;


public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService() {
        this.taskRepository = new TaskRepository();
    }

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }


    public Task createTask(String title, String description, Status status, Priority priority,
                           String deadline, Integer timeEstimate, Long assignedUserId,
                           Long projectId, Long userStoryId) {
        return createTask(title, description, status, priority, deadline, timeEstimate,
                assignedUserId, projectId, userStoryId, null);
    }

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


    public List<Task> getTasksByProject(Long projectId) {
        return taskRepository.findByProject(projectId);
    }

    public List<Task> getTasksByAssignedUser(Long userId) {
        return taskRepository.findByAssignedUser(userId);
    }


    public void assignTask(Long taskId, Long userId) {
        if (!isAdminOrLeader()) {
            throw new AutorisationException("Only admins or project leaders can assign tasks");
        }
        Task task = getTaskById(taskId);
        task.setAssignedUserId(userId);
        taskRepository.update(task);
    }


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


    public void updateTask(Task task) {
        if (!isAdminOrLeader()) {
            throw new AutorisationException("Only admins or project leaders can update tasks");
        }
        taskRepository.update(task);
    }


    public void deleteTask(Long id) {
        if (!isAdminOrLeader()) {
            throw new AutorisationException("Only admins or project leaders can delete tasks");
        }
        taskRepository.delete(id);
    }

    private boolean isAdminOrLeader() {
        User currentUser = SessionManager.getCurrentUser();
        return currentUser != null &&
                (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.PROJECT_LEADER);
    }
}
