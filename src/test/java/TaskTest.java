import org.example.model.Task;
import org.example.model.enums.Status;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void shouldCreateTaskCorrectly() {

        Task task = new Task(
                1L,
                "Create database table",
                "Create the user table",
                Status.TODO,
                1L,
                null
        );

        assertEquals("Create database table", task.getTitle());
        assertEquals(Status.TODO, task.getStatus());
        assertEquals(1L, task.getProjectId());
        assertNull(task.getUserStoryId());
        System.out.println("TaskTest validé");
    }
}
