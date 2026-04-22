import org.example.model.User;
import org.example.model.enums.Role;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryIntegrationTest extends BaseIntegrationTest {

    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository();
    }

    @Test
    void shouldSaveAndFindUserById() {
        User user = new User(null, "Dupont", "Marie", "marie@test.com",
                "hash123", Role.MEMBER, "mdupont");

        Long id = userRepository.save(user);

        assertNotNull(id);
        User found = userRepository.findById(id);
        assertNotNull(found);
        assertEquals("Dupont", found.getLastName());
        assertEquals("Marie", found.getFirstName());
        assertEquals("marie@test.com", found.getMail());
        assertEquals(Role.MEMBER, found.getRole());
        assertEquals("mdupont", found.getUsername());
    }

    @Test
    void shouldFindUserByMail() {
        User user = new User(null, "Martin", "Paul", "paul@test.com",
                "hash", Role.ADMIN, "pmartin");
        userRepository.save(user);

        User found = userRepository.findByMail("paul@test.com");

        assertNotNull(found);
        assertEquals("Martin", found.getLastName());
    }

    @Test
    void shouldReturnNullForUnknownMail() {
        User found = userRepository.findByMail("unknown@test.com");
        assertNull(found);
    }

    @Test
    void shouldReturnNullForUnknownId() {
        User found = userRepository.findById(999L);
        assertNull(found);
    }

    @Test
    void shouldFindAllUsers() {
        User u1 = new User(null, "A", "A", "a@test.com", "h", Role.MEMBER, null);
        User u2 = new User(null, "B", "B", "b@test.com", "h", Role.MEMBER, null);
        userRepository.save(u1);
        userRepository.save(u2);

        List<User> all = userRepository.findAll();

        // Le schema insere un admin par defaut + nos 2
        assertTrue(all.size() >= 2);
    }

    @Test
    void shouldUpdateUser() {
        User user = new User(null, "Old", "Name", "update@test.com",
                "hash", Role.MEMBER, "olduser");
        Long id = userRepository.save(user);

        User toUpdate = new User(id, "New", "Name", "update@test.com",
                "newhash", Role.PROJECT_LEADER, "newuser");
        userRepository.update(toUpdate);

        User found = userRepository.findById(id);
        assertEquals("New", found.getLastName());
        assertEquals(Role.PROJECT_LEADER, found.getRole());
        assertEquals("newuser", found.getUsername());
    }

    @Test
    void shouldDeleteUser() {
        User user = new User(null, "Delete", "Me", "delete@test.com",
                "hash", Role.MEMBER, null);
        Long id = userRepository.save(user);

        userRepository.delete(id);

        User found = userRepository.findById(id);
        assertNull(found);
    }

    @Test
    void shouldUpdateUsername() {
        User user = new User(null, "Test", "User", "username@test.com",
                "hash", Role.MEMBER, "oldname");
        Long id = userRepository.save(user);

        userRepository.updateUsername(id, "newname");

        User found = userRepository.findById(id);
        assertEquals("newname", found.getUsername());
    }
}
