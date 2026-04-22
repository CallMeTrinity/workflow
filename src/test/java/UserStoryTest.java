import org.example.model.UserStory;
import org.example.model.enums.Priority;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserStoryTest {

    @Test
    void shouldCreateUserStoryCorrectly() {
        UserStory userStory = new UserStory(
                1L, "User registration",
                "As a user I can create an account",
                Priority.HIGH, 1L
        );

        assertEquals("User registration", userStory.getTitle());
        assertEquals(Priority.HIGH, userStory.getPriority());
        assertEquals(1L, userStory.getProjectId());
    }

    @Test
    void shouldCreateUserStoryWithNoArgConstructor() {
        UserStory userStory = new UserStory();
        assertNull(userStory.getId());
        assertNull(userStory.getTitle());
    }

    @Test
    void shouldGetAllFieldsFromConstructor() {
        UserStory us = new UserStory(2L, "Login", "Login feature", Priority.LOW, 3L);

        assertEquals(2L, us.getId());
        assertEquals("Login", us.getTitle());
        assertEquals("Login feature", us.getDescription());
        assertEquals(Priority.LOW, us.getPriority());
        assertEquals(3L, us.getProjectId());
    }

    @Test
    void shouldSetAndGetAllFields() {
        UserStory us = new UserStory();

        us.setId(5L);
        us.setTitle("New story");
        us.setDescription("New desc");
        us.setPriority(Priority.MEDIUM);
        us.setProjectId(10L);

        assertEquals(5L, us.getId());
        assertEquals("New story", us.getTitle());
        assertEquals("New desc", us.getDescription());
        assertEquals(Priority.MEDIUM, us.getPriority());
        assertEquals(10L, us.getProjectId());
    }
}
