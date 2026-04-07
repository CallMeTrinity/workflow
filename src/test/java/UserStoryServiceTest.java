import org.example.config.SessionManager;
import org.example.exception.AutorisationException;
import org.example.exception.NotFoundException;
import org.example.model.UserStory;
import org.example.model.User;
import org.example.model.enums.Priority;
import org.example.model.enums.Role;
import org.example.repository.UserStoryRepository;
import org.example.service.UserStoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserStoryServiceTest {

    @Mock
    private UserStoryRepository userStoryRepository;

    @InjectMocks
    private UserStoryService userStoryService;

    private User adminUser;
    private User leaderUser;
    private User memberUser;
    private UserStory sampleUserStory;

    @BeforeEach
    void setUp() {
        adminUser  = new User(1L, "Admin", "System", "admin@test.com", "hash", Role.ADMIN, null);
        leaderUser = new User(2L, "Leader", "John", "leader@test.com", "hash", Role.PROJECT_LEADER, null);
        memberUser = new User(3L, "Member", "Jane", "member@test.com", "hash", Role.MEMBER, null);

        sampleUserStory = new UserStory(1L, "Login feature", "As a user I want to login", Priority.HIGH, 1L);
    }

    // --- createUserStory ---

    @Test
    void adminShouldCreateUserStory() {
        SessionManager.setCurrentUser(adminUser);
        when(userStoryRepository.save(any(UserStory.class))).thenReturn(10L);

        UserStory result = userStoryService.createUserStory(
                "Login feature", "As a user I want to login", Priority.HIGH, 1L
        );

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Login feature", result.getTitle());
    }

    @Test
    void leaderShouldCreateUserStory() {
        SessionManager.setCurrentUser(leaderUser);
        when(userStoryRepository.save(any(UserStory.class))).thenReturn(11L);

        UserStory result = userStoryService.createUserStory(
                "Signup feature", "As a user I want to signup", Priority.MEDIUM, 1L
        );

        assertNotNull(result);
        assertEquals(11L, result.getId());
    }

    @Test
    void memberShouldNotCreateUserStory() {
        SessionManager.setCurrentUser(memberUser);

        assertThrows(AutorisationException.class, () ->
                userStoryService.createUserStory("Feature", "desc", Priority.LOW, 1L)
        );
        verify(userStoryRepository, never()).save(any());
    }

    // --- getUserStoryById ---

    @Test
    void shouldReturnUserStoryById() {
        when(userStoryRepository.findById(1L)).thenReturn(sampleUserStory);

        UserStory result = userStoryService.getUserStoryById(1L);

        assertEquals("Login feature", result.getTitle());
    }

    @Test
    void shouldThrowNotFoundIfUserStoryMissing() {
        when(userStoryRepository.findById(99L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> userStoryService.getUserStoryById(99L));
    }

    // --- getUserStoriesByProject ---

    @Test
    void shouldReturnUserStoriesByProject() {
        when(userStoryRepository.findByProject(1L)).thenReturn(List.of(sampleUserStory));

        List<UserStory> result = userStoryService.getUserStoriesByProject(1L);

        assertEquals(1, result.size());
        assertEquals("Login feature", result.getFirst().getTitle());
    }

    // --- updateUserStory ---

    @Test
    void adminShouldUpdateUserStory() {
        SessionManager.setCurrentUser(adminUser);

        userStoryService.updateUserStory(sampleUserStory);

        verify(userStoryRepository).update(sampleUserStory);
    }

    @Test
    void leaderShouldUpdateUserStory() {
        SessionManager.setCurrentUser(leaderUser);

        userStoryService.updateUserStory(sampleUserStory);

        verify(userStoryRepository).update(sampleUserStory);
    }

    @Test
    void memberShouldNotUpdateUserStory() {
        SessionManager.setCurrentUser(memberUser);

        assertThrows(AutorisationException.class, () -> userStoryService.updateUserStory(sampleUserStory));
        verify(userStoryRepository, never()).update(any());
    }

    // --- deleteUserStory ---

    @Test
    void adminShouldDeleteUserStory() {
        SessionManager.setCurrentUser(adminUser);

        userStoryService.deleteUserStory(1L);

        verify(userStoryRepository).delete(1L);
    }

    @Test
    void leaderShouldDeleteUserStory() {
        SessionManager.setCurrentUser(leaderUser);

        userStoryService.deleteUserStory(1L);

        verify(userStoryRepository).delete(1L);
    }

    @Test
    void memberShouldNotDeleteUserStory() {
        SessionManager.setCurrentUser(memberUser);

        assertThrows(AutorisationException.class, () -> userStoryService.deleteUserStory(1L));
        verify(userStoryRepository, never()).delete(any());
    }
}
