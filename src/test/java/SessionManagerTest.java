import org.example.config.SessionManager;
import org.example.model.User;
import org.example.model.enums.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SessionManagerTest {

    @AfterEach
    void tearDown() {
        SessionManager.clear();
    }

    @Test
    void setAndGetCurrentUser() {
        User user = new User(1L, "Doe", "John", "john@test.com", "hash", Role.ADMIN, "johndoe");

        SessionManager.setCurrentUser(user);

        assertEquals(user, SessionManager.getCurrentUser());
    }

    @Test
    void clearShouldSetCurrentUserToNull() {
        User user = new User(1L, "Doe", "John", "john@test.com", "hash", Role.ADMIN, "johndoe");
        SessionManager.setCurrentUser(user);

        SessionManager.clear();

        assertNull(SessionManager.getCurrentUser());
    }

    @Test
    void setMultipleUsersShouldReturnLast() {
        User user1 = new User(1L, "Doe", "John", "john@test.com", "hash", Role.ADMIN, "johndoe");
        User user2 = new User(2L, "Smith", "Jane", "jane@test.com", "hash", Role.MEMBER, "janesmith");

        SessionManager.setCurrentUser(user1);
        SessionManager.setCurrentUser(user2);

        assertEquals(user2, SessionManager.getCurrentUser());
    }

    @Test
    void getCurrentUserWithNoSessionShouldReturnNull() {
        assertNull(SessionManager.getCurrentUser());
    }
}
