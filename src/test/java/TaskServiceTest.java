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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private User adminUser;
    private User leaderUser;
    private User memberUser;
    private Task sampleTask;

    @BeforeEach
    void setUp() {
        adminUser  = new User(1L, "Admin", "System", "admin@test.com", "hash", Role.ADMIN, null);
        leaderUser = new User(2L, "Leader", "John", "leader@test.com", "hash", Role.PROJECT_LEADER, null);
        memberUser = new User(3L, "Member", "Jane", "member@test.com", "hash", Role.MEMBER, null);

        sampleTask = new Task(1L, "Fix bug", "Fix the login bug",
                Status.TODO, Priority.HIGH, "2024-06-01", 2, null, 1L, null);
    }

    // --- createTask ---

    @Test
    void adminShouldCreateTask() {
        SessionManager.setCurrentUser(adminUser);
        when(taskRepository.save(any(Task.class))).thenReturn(10L);

        Task result = taskService.createTask(
                "Fix bug", "desc", Status.TODO, Priority.HIGH,
                "2024-06-01", 2, null, 1L, null
        );

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Fix bug", result.getTitle());
    }

    @Test
    void leaderShouldCreateTask() {
        SessionManager.setCurrentUser(leaderUser);
        when(taskRepository.save(any(Task.class))).thenReturn(11L);

        Task result = taskService.createTask(
                "New feature", "desc", Status.TODO, Priority.MEDIUM,
                "2024-07-01", 5, null, 1L, null
        );

        assertNotNull(result);
        assertEquals(11L, result.getId());
    }

    @Test
    void memberShouldNotCreateTask() {
        SessionManager.setCurrentUser(memberUser);

        assertThrows(AutorisationException.class, () ->
                taskService.createTask("Task", "desc", Status.TODO, Priority.LOW,
                        null, null, null, 1L, null)
        );
        verify(taskRepository, never()).save(any());
    }

    // --- getTaskById ---

    @Test
    void shouldReturnTaskById() {
        when(taskRepository.findById(1L)).thenReturn(sampleTask);

        Task result = taskService.getTaskById(1L);

        assertEquals("Fix bug", result.getTitle());
    }

    @Test
    void shouldThrowNotFoundIfTaskMissing() {
        when(taskRepository.findById(99L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> taskService.getTaskById(99L));
    }

    // --- getTasksByProject ---

    @Test
    void shouldReturnTasksByProject() {
        when(taskRepository.findByProject(1L)).thenReturn(List.of(sampleTask));

        List<Task> result = taskService.getTasksByProject(1L);

        assertEquals(1, result.size());
        assertEquals("Fix bug", result.getFirst().getTitle());
    }

    // --- getTasksByAssignedUser ---

    @Test
    void shouldReturnTasksByAssignedUser() {
        Task assignedTask = new Task(2L, "Review PR", "desc",
                Status.IN_PROGRESS, Priority.MEDIUM, null, 1, 3L, 3L, 3L);
        when(taskRepository.findByAssignedUser(3L)).thenReturn(List.of(assignedTask));

        List<Task> result = taskService.getTasksByAssignedUser(3L);

        assertEquals(1, result.size());
        assertEquals(3L, result.getFirst().getAssignedUserId());
    }

    // --- assignTask ---

    @Test
    void adminShouldAssignTask() {
        SessionManager.setCurrentUser(adminUser);
        when(taskRepository.findById(1L)).thenReturn(sampleTask);

        taskService.assignTask(1L, 3L);

        assertEquals(3L, sampleTask.getAssignedUserId());
        verify(taskRepository).update(sampleTask);
    }

    @Test
    void memberShouldNotAssignTask() {
        SessionManager.setCurrentUser(memberUser);

        assertThrows(AutorisationException.class, () -> taskService.assignTask(1L, 3L));
        verify(taskRepository, never()).update(any());
    }

    @Test
    void shouldThrowNotFoundWhenAssigningMissingTask() {
        SessionManager.setCurrentUser(adminUser);
        when(taskRepository.findById(99L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> taskService.assignTask(99L, 3L));
    }

    // --- updateTaskStatus ---

    @Test
    void adminShouldUpdateAnyTaskStatus() {
        SessionManager.setCurrentUser(adminUser);
        when(taskRepository.findById(1L)).thenReturn(sampleTask);

        taskService.updateTaskStatus(1L, Status.DONE);

        assertEquals(Status.DONE, sampleTask.getStatus());
        verify(taskRepository).update(sampleTask);
    }

    @Test
    void memberShouldUpdateStatusOfOwnTask() {
        sampleTask.setAssignedUserId(3L);
        SessionManager.setCurrentUser(memberUser);
        when(taskRepository.findById(1L)).thenReturn(sampleTask);

        taskService.updateTaskStatus(1L, Status.IN_PROGRESS);

        assertEquals(Status.IN_PROGRESS, sampleTask.getStatus());
        verify(taskRepository).update(sampleTask);
    }

    @Test
    void memberShouldNotUpdateStatusOfOtherTask() {
        sampleTask.setAssignedUserId(99L); // assignée à quelqu'un d'autre
        SessionManager.setCurrentUser(memberUser);
        when(taskRepository.findById(1L)).thenReturn(sampleTask);

        assertThrows(AutorisationException.class, () ->
                taskService.updateTaskStatus(1L, Status.DONE)
        );
        verify(taskRepository, never()).update(any());
    }

    // --- updateTask ---

    @Test
    void adminShouldUpdateTask() {
        SessionManager.setCurrentUser(adminUser);

        taskService.updateTask(sampleTask);

        verify(taskRepository).update(sampleTask);
    }

    @Test
    void memberShouldNotUpdateTask() {
        SessionManager.setCurrentUser(memberUser);

        assertThrows(AutorisationException.class, () -> taskService.updateTask(sampleTask));
        verify(taskRepository, never()).update(any());
    }

    // --- deleteTask ---

    @Test
    void adminShouldDeleteTask() {
        SessionManager.setCurrentUser(adminUser);

        taskService.deleteTask(1L);

        verify(taskRepository).delete(1L);
    }

    @Test
    void leaderShouldDeleteTask() {
        SessionManager.setCurrentUser(leaderUser);

        taskService.deleteTask(1L);

        verify(taskRepository).delete(1L);
    }

    @Test
    void memberShouldNotDeleteTask() {
        SessionManager.setCurrentUser(memberUser);

        assertThrows(AutorisationException.class, () -> taskService.deleteTask(1L));
        verify(taskRepository, never()).delete(any());
    }
}
