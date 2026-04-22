import org.example.model.Project;
import org.example.model.User;
import org.example.model.UserStory;
import org.example.model.enums.Priority;
import org.example.model.enums.Role;
import org.example.repository.ProjectRepository;
import org.example.repository.UserRepository;
import org.example.repository.UserStoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserStoryRepositoryIntegrationTest extends BaseIntegrationTest {

    private UserStoryRepository userStoryRepository;
    private Long projectId;

    @BeforeEach
    void setUp() {
        userStoryRepository = new UserStoryRepository();

        UserRepository userRepo = new UserRepository();
        Long userId = userRepo.save(new User(null, "Test", "User",
                "us-test@test.com", "hash", Role.MEMBER, null));

        ProjectRepository projectRepo = new ProjectRepository();
        projectId = projectRepo.save(new Project(null, "US Project", "Desc",
                "2024-01-01", "2024-12-31", userId));
    }

    @Test
    void shouldSaveAndFindById() {
        UserStory us = new UserStory(null, "Login feature",
                "As a user I can login", Priority.HIGH, projectId);

        Long id = userStoryRepository.save(us);

        assertNotNull(id);
        UserStory found = userStoryRepository.findById(id);
        assertNotNull(found);
        assertEquals("Login feature", found.getTitle());
        assertEquals("As a user I can login", found.getDescription());
        assertEquals(Priority.HIGH, found.getPriority());
        assertEquals(projectId, found.getProjectId());
    }

    @Test
    void shouldReturnNullForUnknownId() {
        assertNull(userStoryRepository.findById(999L));
    }

    @Test
    void shouldFindAll() {
        userStoryRepository.save(new UserStory(null, "US1", "D1",
                Priority.LOW, projectId));
        userStoryRepository.save(new UserStory(null, "US2", "D2",
                Priority.MEDIUM, projectId));

        List<UserStory> all = userStoryRepository.findAll();

        assertTrue(all.size() >= 2);
    }

    @Test
    void shouldFindByProject() {
        userStoryRepository.save(new UserStory(null, "ProjectUS", "Desc",
                Priority.CRITICAL, projectId));

        List<UserStory> stories = userStoryRepository.findByProject(projectId);

        assertFalse(stories.isEmpty());
        assertEquals("ProjectUS", stories.get(0).getTitle());
    }

    @Test
    void shouldFindByProjectReturnEmptyForUnknown() {
        List<UserStory> stories = userStoryRepository.findByProject(999L);
        assertTrue(stories.isEmpty());
    }

    @Test
    void shouldUpdateUserStory() {
        Long id = userStoryRepository.save(new UserStory(null, "Old", "Old Desc",
                Priority.LOW, projectId));

        userStoryRepository.update(new UserStory(id, "Updated", "New Desc",
                Priority.HIGH, projectId));

        UserStory found = userStoryRepository.findById(id);
        assertEquals("Updated", found.getTitle());
        assertEquals("New Desc", found.getDescription());
        assertEquals(Priority.HIGH, found.getPriority());
    }

    @Test
    void shouldDeleteUserStory() {
        Long id = userStoryRepository.save(new UserStory(null, "ToDelete", "Desc",
                Priority.MEDIUM, projectId));

        userStoryRepository.delete(id);

        assertNull(userStoryRepository.findById(id));
    }
}
