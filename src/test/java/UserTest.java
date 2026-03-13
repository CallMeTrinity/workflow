import org.example.model.User;
import org.example.model.enums.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    void shouldReturnFullName() {

        User user = new User(
                1L,
                "Busson",
                "Juline",
                "jbusson@polytech.com",
                "password123",
                Role.MEMBER
        );

        String fullName = user.getFullName();

        assertEquals("Juline Busson", fullName);
        System.out.println("Full name : " + fullName);
    }
}
