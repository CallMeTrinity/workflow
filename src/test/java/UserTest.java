import org.example.model.User;
import org.example.model.enums.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    void shouldReturnFullName() {
        User user = new User(
                1L, "Busson", "Juline", "jbusson@polytech.com",
                "password123", Role.MEMBER, null
        );

        assertEquals("Juline Busson", user.getFullName());
    }

    @Test
    void shouldCreateUserWithNoArgConstructor() {
        User user = new User();
        assertNull(user.getId());
        assertNull(user.getLastName());
    }

    @Test
    void shouldGetAllFieldsFromConstructor() {
        User user = new User(1L, "Doe", "John", "john@test.com",
                "pass", Role.ADMIN, "johndoe");

        assertEquals(1L, user.getId());
        assertEquals("Doe", user.getLastName());
        assertEquals("John", user.getFirstName());
        assertEquals("john@test.com", user.getMail());
        assertEquals("pass", user.getPassword());
        assertEquals(Role.ADMIN, user.getRole());
        assertEquals("johndoe", user.getUsername());
    }

    @Test
    void shouldSetAndGetAllFields() {
        User user = new User();

        user.setId(2L);
        user.setLastName("Smith");
        user.setFirstName("Jane");
        user.setMail("jane@test.com");
        user.setPassword("secret");
        user.setRole(Role.PROJECT_LEADER);
        user.setUsername("janesmith");

        assertEquals(2L, user.getId());
        assertEquals("Smith", user.getLastName());
        assertEquals("Jane", user.getFirstName());
        assertEquals("jane@test.com", user.getMail());
        assertEquals("secret", user.getPassword());
        assertEquals(Role.PROJECT_LEADER, user.getRole());
        assertEquals("janesmith", user.getUsername());
    }
}
