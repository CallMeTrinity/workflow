import at.favre.lib.crypto.bcrypt.BCrypt;
import org.example.config.SessionManager;
import org.example.exception.AutorisationException;
import org.example.model.User;
import org.example.model.enums.Role;
import org.example.repository.UserRepository;
import org.example.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    private User adminUser;
    private User memberUser;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
        adminUser = new User(1L, "Admin", "System", "admin@test.com", "hash", Role.ADMIN, null);
        memberUser = new User(2L, "Member", "Jane", "member@test.com", "hash", Role.MEMBER, null);
    }

    @AfterEach
    void tearDown() {
        SessionManager.clear();
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
    void getAllUsersEmptyListShouldReturnEmpty() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<User> result = userService.getAllUsers();

        assertTrue(result.isEmpty());
    }

    // --- createUser ---

    @Test
    void createUserAsAdminShouldHashPasswordAndSave() {
        SessionManager.setCurrentUser(adminUser);
        when(userRepository.findByMail("new@test.com")).thenReturn(null);
        when(userRepository.save(any())).thenReturn(10L);

        User result = userService.createUser("Doe", "John", "new@test.com",
                "secret123", Role.MEMBER, "johndoe");

        assertEquals(10L, result.getId());
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        String storedHash = captor.getValue().getPassword();
        assertNotEquals("secret123", storedHash);
        assertTrue(BCrypt.verifyer().verify("secret123".toCharArray(), storedHash).verified);
    }

    @Test
    void createUserAsMemberShouldThrowAutorisation() {
        SessionManager.setCurrentUser(memberUser);

        assertThrows(AutorisationException.class, () ->
                userService.createUser("Doe", "John", "new@test.com",
                        "secret123", Role.MEMBER, null));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUserWithoutSessionShouldThrowAutorisation() {
        SessionManager.clear();

        assertThrows(AutorisationException.class, () ->
                userService.createUser("Doe", "John", "new@test.com",
                        "secret123", Role.MEMBER, null));
    }

    @Test
    void createUserWithDuplicateEmailShouldThrow() {
        SessionManager.setCurrentUser(adminUser);
        when(userRepository.findByMail("member@test.com")).thenReturn(memberUser);

        assertThrows(IllegalArgumentException.class, () ->
                userService.createUser("Doe", "John", "member@test.com",
                        "secret123", Role.MEMBER, null));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUserWithBlankFieldsShouldThrow() {
        SessionManager.setCurrentUser(adminUser);

        assertThrows(IllegalArgumentException.class, () ->
                userService.createUser("", "John", "new@test.com",
                        "secret123", Role.MEMBER, null));
    }

    @Test
    void createUserWithEmptyPasswordShouldThrow() {
        SessionManager.setCurrentUser(adminUser);

        assertThrows(IllegalArgumentException.class, () ->
                userService.createUser("Doe", "John", "new@test.com",
                        "", Role.MEMBER, null));
    }

    // --- updateUser ---

    @Test
    void updateUserWithNewPasswordShouldRehash() {
        SessionManager.setCurrentUser(adminUser);
        User user = new User(2L, "Member", "Jane", "member@test.com", "oldhash", Role.MEMBER, null);
        when(userRepository.findByMail("member@test.com")).thenReturn(user);

        userService.updateUser(user, "newpass");

        assertTrue(BCrypt.verifyer().verify("newpass".toCharArray(), user.getPassword()).verified);
        verify(userRepository).update(user);
    }

    @Test
    void updateUserWithEmptyPasswordShouldKeepExistingHash() {
        SessionManager.setCurrentUser(adminUser);
        User user = new User(2L, "Member", "Jane", "member@test.com", "oldhash", Role.MEMBER, null);
        when(userRepository.findByMail("member@test.com")).thenReturn(user);

        userService.updateUser(user, "");

        assertEquals("oldhash", user.getPassword());
        verify(userRepository).update(user);
    }

    @Test
    void updateUserWithEmailOfAnotherUserShouldThrow() {
        SessionManager.setCurrentUser(adminUser);
        User user = new User(2L, "Member", "Jane", "admin@test.com", "hash", Role.MEMBER, null);
        when(userRepository.findByMail("admin@test.com")).thenReturn(adminUser);

        assertThrows(IllegalArgumentException.class, () ->
                userService.updateUser(user, null));
        verify(userRepository, never()).update(any());
    }

    @Test
    void updateUserAsMemberShouldThrowAutorisation() {
        SessionManager.setCurrentUser(memberUser);

        assertThrows(AutorisationException.class, () ->
                userService.updateUser(memberUser, "newpass"));
    }

    // --- deleteUser ---

    @Test
    void deleteUserAsAdminShouldCallRepository() {
        SessionManager.setCurrentUser(adminUser);

        userService.deleteUser(2L);

        verify(userRepository).delete(2L);
    }

    @Test
    void deleteOwnAccountShouldThrow() {
        SessionManager.setCurrentUser(adminUser);

        assertThrows(IllegalArgumentException.class, () ->
                userService.deleteUser(1L));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUserAsMemberShouldThrowAutorisation() {
        SessionManager.setCurrentUser(memberUser);

        assertThrows(AutorisationException.class, () ->
                userService.deleteUser(1L));
        verify(userRepository, never()).delete(any());
    }
}
