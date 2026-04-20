import at.favre.lib.crypto.bcrypt.BCrypt;
import org.example.config.SessionManager;
import org.example.exception.InvalidLoginCredentialsException;
import org.example.exception.UserNotFoundException;
import org.example.model.User;
import org.example.model.enums.Role;
import org.example.repository.UserRepository;
import org.example.service.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    private AuthService authService;

    private User testUser;
    private String rawPassword;
    private String hashedPassword;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository);
        rawPassword = "admin123";
        hashedPassword = BCrypt.withDefaults().hashToString(12, rawPassword.toCharArray());
        testUser = new User(1L, "Doe", "John", "john@test.com", hashedPassword, Role.ADMIN, "johndoe");
    }

    @AfterEach
    void tearDown() {
        SessionManager.clear();
    }

    @Test
    void loginWithValidCredentialsShouldReturnUser() {
        when(userRepository.findByMail("john@test.com")).thenReturn(testUser);

        User result = authService.login("john@test.com", rawPassword);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("john@test.com", result.getMail());
    }

    @Test
    void loginWithValidCredentialsShouldSetSession() {
        when(userRepository.findByMail("john@test.com")).thenReturn(testUser);

        authService.login("john@test.com", rawPassword);

        assertEquals(testUser, SessionManager.getCurrentUser());
    }

    @Test
    void loginWithUnknownEmailShouldThrowUserNotFoundException() {
        when(userRepository.findByMail("unknown@test.com")).thenReturn(null);

        assertThrows(UserNotFoundException.class, () ->
                authService.login("unknown@test.com", "password")
        );
    }

    @Test
    void loginWithWrongPasswordShouldThrowInvalidCredentials() {
        when(userRepository.findByMail("john@test.com")).thenReturn(testUser);

        assertThrows(InvalidLoginCredentialsException.class, () ->
                authService.login("john@test.com", "wrongpassword")
        );
    }

    @Test
    void loginWithWrongPasswordShouldNotSetSession() {
        when(userRepository.findByMail("john@test.com")).thenReturn(testUser);

        try {
            authService.login("john@test.com", "wrongpassword");
        } catch (InvalidLoginCredentialsException ignored) {}

        assertNull(SessionManager.getCurrentUser());
    }

    @Test
    void logoutShouldClearSession() {
        SessionManager.setCurrentUser(testUser);

        authService.logout();

        assertNull(SessionManager.getCurrentUser());
    }

    @Test
    void getCurrentUserShouldReturnLoggedInUser() {
        SessionManager.setCurrentUser(testUser);

        User result = authService.getCurrentUser();

        assertEquals(testUser, result);
    }

    @Test
    void getCurrentUserShouldReturnNullWhenNoSession() {
        User result = authService.getCurrentUser();

        assertNull(result);
    }

    @Test
    void loginShouldCallFindByMail() {
        when(userRepository.findByMail("john@test.com")).thenReturn(testUser);

        authService.login("john@test.com", rawPassword);

        verify(userRepository).findByMail("john@test.com");
    }

    @Test
    void loginWithDifferentValidPasswordShouldWork() {
        String otherPassword = "securePass456";
        String otherHash = BCrypt.withDefaults().hashToString(12, otherPassword.toCharArray());
        User otherUser = new User(2L, "Smith", "Jane", "jane@test.com", otherHash, Role.MEMBER, "janesmith");
        when(userRepository.findByMail("jane@test.com")).thenReturn(otherUser);

        User result = authService.login("jane@test.com", otherPassword);

        assertEquals(2L, result.getId());
    }
}
