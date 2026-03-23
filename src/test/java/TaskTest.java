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
}
