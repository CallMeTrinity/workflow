import org.example.model.Task;
import org.example.model.User;
import org.example.model.enums.Priority;
import org.example.model.enums.Role;
import org.example.model.enums.Status;
import org.example.repository.ProjectRepository;
import org.example.repository.TaskRepository;
import org.example.repository.UserRepository;
import org.example.model.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskRepositoryIntegrationTest extends BaseIntegrationTest {

    private TaskRepository taskRepository;
    private Long projectId;
    private Long userId;

    @BeforeEach
    void setUp() {
        taskRepository = new TaskRepository();

        // Creer un utilisateur et un projet pour les FK
        UserRepository userRepo = new UserRepository();
        userId = userRepo.save(new User(null, "Test", "User", "task-test@test.com",
                "hash", Role.MEMBER, null));

        ProjectRepository projectRepo = new ProjectRepository();
        projectId = projectRepo.save(new Project(null, "Test Project", "Desc",
                "2024-01-01", "2024-12-31", userId));
    }

    @Test
    void shouldSaveAndFindTaskById() {
        Task task = new Task(null, "My Task", "Description", Status.TODO,
                Priority.HIGH, "2024-06-01", 5, projectId, null, null);

        Long id = taskRepository.save(task);

        assertNotNull(id);
        Task found = taskRepository.findById(id);
        assertNotNull(found);
        assertEquals("My Task", found.getTitle());
        assertEquals("Description", found.getDescription());
        assertEquals(Status.TODO, found.getStatus());
        assertEquals(Priority.HIGH, found.getPriority());
        assertEquals("2024-06-01", found.getDeadline());
        assertEquals(5, found.getTimeEstimate());
        assertEquals(projectId, found.getProjectId());
    }

    @Test
    void shouldReturnNullForUnknownId() {
        assertNull(taskRepository.findById(999L));
    }

    @Test
    void shouldFindAllTasks() {
        taskRepository.save(new Task(null, "T1", "D1", Status.TODO,
                Priority.LOW, null, null, projectId, null, null));
        taskRepository.save(new Task(null, "T2", "D2", Status.DONE,
                Priority.MEDIUM, null, null, projectId, null, null));

        List<Task> all = taskRepository.findAll();

        assertTrue(all.size() >= 2);
    }

    @Test
    void shouldFindByProject() {
        taskRepository.save(new Task(null, "ProjectTask", "Desc", Status.TODO,
                Priority.LOW, null, null, projectId, null, null));

        List<Task> tasks = taskRepository.findByProject(projectId);

        assertFalse(tasks.isEmpty());
        assertEquals("ProjectTask", tasks.get(0).getTitle());
    }

    @Test
    void shouldFindByAssignedUser() {
        taskRepository.save(new Task(null, "Assigned", "Desc", Status.IN_PROGRESS,
                Priority.HIGH, null, 3, projectId, null, userId));

        List<Task> tasks = taskRepository.findByAssignedUser(userId);

        assertFalse(tasks.isEmpty());
        assertEquals("Assigned", tasks.get(0).getTitle());
    }

    @Test
    void shouldUpdateTask() {
        Long id = taskRepository.save(new Task(null, "Original", "Desc", Status.TODO,
                Priority.LOW, null, null, projectId, null, null));

        Task toUpdate = new Task(id, "Updated", "New Desc", Status.DONE,
                Priority.CRITICAL, "2025-01-01", 10, projectId, null, userId);
        taskRepository.update(toUpdate);

        Task found = taskRepository.findById(id);
        assertEquals("Updated", found.getTitle());
        assertEquals("New Desc", found.getDescription());
        assertEquals(Status.DONE, found.getStatus());
        assertEquals(Priority.CRITICAL, found.getPriority());
        assertEquals(userId, found.getAssignedUserId());
    }

    @Test
    void shouldDeleteTask() {
        Long id = taskRepository.save(new Task(null, "ToDelete", "Desc", Status.TODO,
                Priority.LOW, null, null, projectId, null, null));

        taskRepository.delete(id);

        assertNull(taskRepository.findById(id));
    }

    @Test
    void shouldHandleNullableFields() {
        Task task = new Task(null, "Nullable", "Desc", Status.TODO,
                Priority.MEDIUM, null, null, projectId, null, null, null);

        Long id = taskRepository.save(task);
        Task found = taskRepository.findById(id);

        assertNull(found.getDeadline());
        assertNull(found.getTimeEstimate());
        assertNull(found.getUserStoryId());
        assertNull(found.getAssignedUserId());
        assertNull(found.getTaskLeaderId());
    }
}
