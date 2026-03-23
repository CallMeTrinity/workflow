import org.example.config.SessionManager;
import org.example.exception.AutorisationException;
import org.example.exception.InvalidDateInputException;
import org.example.exception.NotFoundException;
import org.example.model.Project;
import org.example.model.User;
import org.example.model.enums.Role;
import org.example.repository.ProjectRepository;
import org.example.service.ProjectService;
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
public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    private User adminUser;
    private User leaderUser;
    private User memberUser;

    @BeforeEach
    void setUp() {
        adminUser  = new User(1L, "Admin", "System", "admin@test.com", "hash", Role.ADMIN);
        leaderUser = new User(2L, "Leader", "John", "leader@test.com", "hash", Role.PROJECT_LEADER);
        memberUser = new User(3L, "Member", "Jane", "member@test.com", "hash", Role.MEMBER);
    }

    // --- createProject ---

    @Test
    void adminShouldCreateProject() {
        SessionManager.setCurrentUser(adminUser);
        when(projectRepository.save(any(Project.class))).thenReturn(42L);

        Project result = projectService.createProject(
                "My Project", "Description", "2024-01-01", "2024-06-30"
        );

        assertNotNull(result);
        assertEquals(42L, result.getId());
        assertEquals("My Project", result.getName());
        assertEquals(1L, result.getProjectLeaderId());
    }

    @Test
    void leaderShouldCreateProject() {
        SessionManager.setCurrentUser(leaderUser);
        when(projectRepository.save(any(Project.class))).thenReturn(10L);

        Project result = projectService.createProject(
                "Leader Project", "Desc", "2024-02-01", "2024-08-01"
        );

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(2L, result.getProjectLeaderId());
    }

    @Test
    void memberShouldNotCreateProject() {
        SessionManager.setCurrentUser(memberUser);

        assertThrows(AutorisationException.class, () ->
                projectService.createProject("Project", "Desc", "2024-01-01", "2024-06-30")
        );
        verify(projectRepository, never()).save(any());
    }

    @Test
    void shouldRejectInvalidDateFormat() {
        SessionManager.setCurrentUser(adminUser);

        assertThrows(InvalidDateInputException.class, () ->
                projectService.createProject("Project", "Desc", "01-01-2024", "2024-06-30")
        );
    }

    // --- getProjectById ---

    @Test
    void shouldReturnProjectById() {
        Project project = new Project(1L, "Test", "Desc", "2024-01-01", "2024-06-30", 1L);
        when(projectRepository.findById(1L)).thenReturn(project);

        Project result = projectService.getProjectById(1L);

        assertEquals("Test", result.getName());
    }

    @Test
    void shouldThrowNotFoundIfProjectMissing() {
        when(projectRepository.findById(99L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> projectService.getProjectById(99L));
    }

    // --- getAllProjects ---

    @Test
    void shouldReturnAllProjects() {
        List<Project> projects = List.of(
                new Project(1L, "P1", "D1", "2024-01-01", "2024-06-30", 1L),
                new Project(2L, "P2", "D2", "2024-02-01", "2024-07-30", 2L)
        );
        when(projectRepository.findAll()).thenReturn(projects);

        List<Project> result = projectService.getAllProjects();

        assertEquals(2, result.size());
    }

    // --- updateProject ---

    @Test
    void adminShouldUpdateAnyProject() {
        SessionManager.setCurrentUser(adminUser);
        Project project = new Project(1L, "P", "D", "2024-01-01", "2024-06-30", 99L);

        projectService.updateProject(project);

        verify(projectRepository).update(project);
    }

    @Test
    void leaderShouldUpdateOwnProject() {
        SessionManager.setCurrentUser(leaderUser);
        Project project = new Project(1L, "P", "D", "2024-01-01", "2024-06-30", 2L);

        projectService.updateProject(project);

        verify(projectRepository).update(project);
    }

    @Test
    void leaderShouldNotUpdateOtherProject() {
        SessionManager.setCurrentUser(leaderUser);
        Project project = new Project(1L, "P", "D", "2024-01-01", "2024-06-30", 99L);

        assertThrows(AutorisationException.class, () -> projectService.updateProject(project));
        verify(projectRepository, never()).update(any());
    }

    // --- deleteProject ---

    @Test
    void adminShouldDeleteAnyProject() {
        SessionManager.setCurrentUser(adminUser);
        Project project = new Project(1L, "P", "D", "2024-01-01", "2024-06-30", 99L);
        when(projectRepository.findById(1L)).thenReturn(project);

        projectService.deleteProject(1L);

        verify(projectRepository).delete(1L);
    }

    @Test
    void memberShouldNotDeleteProject() {
        SessionManager.setCurrentUser(memberUser);
        Project project = new Project(1L, "P", "D", "2024-01-01", "2024-06-30", 99L);
        when(projectRepository.findById(1L)).thenReturn(project);

        assertThrows(AutorisationException.class, () -> projectService.deleteProject(1L));
        verify(projectRepository, never()).delete(any());
    }
}
