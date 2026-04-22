import org.example.model.Task;
import org.example.model.enums.Priority;
import org.example.model.enums.Status;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {

    @Test
    void shouldCreateTaskCorrectly() {
        Task task = new Task(
                1L,
                "Create database table",
                "Create the task table in the database",
                Status.TODO,
                Priority.HIGH,
                "2024-07-01",
                5,
                1L,
                null,
                null
        );

        assertEquals("Create database table", task.getTitle());
        assertEquals(Status.TODO, task.getStatus());
        assertEquals(1L, task.getProjectId());
        assertNull(task.getUserStoryId());
        assertEquals("2024-07-01", task.getDeadline());
    }

    @Test
    void shouldCreateTaskWithNoArgConstructor() {
        Task task = new Task();
        assertNull(task.getId());
        assertNull(task.getTitle());
    }

    @Test
    void shouldCreateTaskWithTaskLeader() {
        Task task = new Task(1L, "Title", "Desc", Status.TODO, Priority.HIGH,
                "2024-07-01", 5, 1L, 2L, 3L, 4L);

        assertEquals(1L, task.getId());
        assertEquals("Title", task.getTitle());
        assertEquals("Desc", task.getDescription());
        assertEquals(Status.TODO, task.getStatus());
        assertEquals(Priority.HIGH, task.getPriority());
        assertEquals("2024-07-01", task.getDeadline());
        assertEquals(5, task.getTimeEstimate());
        assertEquals(1L, task.getProjectId());
        assertEquals(2L, task.getUserStoryId());
        assertEquals(3L, task.getAssignedUserId());
        assertEquals(4L, task.getTaskLeaderId());
    }

    @Test
    void shouldSetAndGetAllFields() {
        Task task = new Task();

        task.setId(10L);
        task.setTitle("Updated title");
        task.setDescription("Updated desc");
        task.setStatus(Status.IN_PROGRESS);
        task.setPriority(Priority.LOW);
        task.setDeadline("2025-01-01");
        task.setTimeEstimate(10);
        task.setProjectId(5L);
        task.setUserStoryId(6L);
        task.setAssignedUserId(7L);
        task.setTaskLeaderId(8L);

        assertEquals(10L, task.getId());
        assertEquals("Updated title", task.getTitle());
        assertEquals("Updated desc", task.getDescription());
        assertEquals(Status.IN_PROGRESS, task.getStatus());
        assertEquals(Priority.LOW, task.getPriority());
        assertEquals("2025-01-01", task.getDeadline());
        assertEquals(10, task.getTimeEstimate());
        assertEquals(5L, task.getProjectId());
        assertEquals(6L, task.getUserStoryId());
        assertEquals(7L, task.getAssignedUserId());
        assertEquals(8L, task.getTaskLeaderId());
    }
}
