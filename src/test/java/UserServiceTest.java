import org.example.model.User;
import org.example.model.enums.Role;
import org.example.repository.UserRepository;
import org.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    void updateUsernameShouldCallRepository() {
        userService.updateUsername(1L, "newname");

        verify(userRepository).updateUsername(1L, "newname");
    }

    @Test
    void getAllUsersShouldReturnList() {
        List<User> users = List.of(
                new User(1L, "Doe", "John", "john@test.com", "hash", Role.ADMIN, "johndoe"),
                new User(2L, "Smith", "Jane", "jane@test.com", "hash", Role.MEMBER, "janesmith")
        );
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
    }

    @Test
    void getUserByIdExistingShouldReturnUser() {
        User user = new User(1L, "Doe", "John", "john@test.com", "hash", Role.ADMIN, "johndoe");
        when(userRepository.findById(1L)).thenReturn(user);

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("Doe", result.getLastName());
    }

    @Test
    void getUserByIdNonExistingShouldReturnNull() {
        when(userRepository.findById(99L)).thenReturn(null);

        User result = userService.getUserById(99L);

        assertNull(result);
    }

    @Test
    void updateProfileShouldCallUpdateUsername() {
        userService.updateProfile(1L, "updatedname");

        verify(userRepository).updateUsername(1L, "updatedname");
    }

    @Test
    void getAllUsersEmptyListShouldReturnEmpty() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<User> result = userService.getAllUsers();

        assertTrue(result.isEmpty());
    }
}
