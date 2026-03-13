import org.example.model.UserStory;
import org.example.model.enums.Priority;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserStoryTest {

    @Test
    void shouldCreateUserStoryCorrectly() {

        UserStory userStory = new UserStory(
                1L,
                "User registration",
                "As a user I can create an account",
                Priority.HIGH,
                1L
        );

        assertEquals("User registration", userStory.getTitle());
        assertEquals(Priority.HIGH, userStory.getPriority());
        assertEquals(1L, userStory.getProjectId());
        System.out.println("UserStoryTest validé");
    }
}
