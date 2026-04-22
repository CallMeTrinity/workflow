import org.example.model.Project;
import org.example.model.User;
import org.example.model.enums.Role;
import org.example.repository.ProjectRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectRepositoryIntegrationTest extends BaseIntegrationTest {

    private ProjectRepository projectRepository;
    private Long leaderId;
    private Long memberId;

    @BeforeEach
    void setUp() {
        projectRepository = new ProjectRepository();

        UserRepository userRepo = new UserRepository();
        leaderId = userRepo.save(new User(null, "Leader", "Test",
                "leader@test.com", "hash", Role.PROJECT_LEADER, null));
        memberId = userRepo.save(new User(null, "Member", "Test",
                "member@test.com", "hash", Role.MEMBER, null));
    }

    @Test
    void shouldSaveAndFindById() {
        Project project = new Project(null, "Mon Projet", "Description",
                "2024-01-01", "2024-12-31", leaderId);

        Long id = projectRepository.save(project);

        assertNotNull(id);
        Project found = projectRepository.findById(id);
        assertNotNull(found);
        assertEquals("Mon Projet", found.getName());
        assertEquals("Description", found.getDescription());
        assertEquals("2024-01-01", found.getStartDate());
        assertEquals("2024-12-31", found.getEndDate());
        assertEquals(leaderId, found.getProjectLeaderId());
        assertNotNull(found.getCreatedAt());
    }

    @Test
    void shouldReturnNullForUnknownId() {
        assertNull(projectRepository.findById(999L));
    }

    @Test
    void shouldFindAll() {
        projectRepository.save(new Project(null, "P1", "D1",
                "2024-01-01", "2024-06-30", leaderId));
        projectRepository.save(new Project(null, "P2", "D2",
                "2024-07-01", "2024-12-31", leaderId));

        List<Project> all = projectRepository.findAll();

        assertTrue(all.size() >= 2);
    }

    @Test
    void shouldFindByLeader() {
        projectRepository.save(new Project(null, "LeaderProject", "Desc",
                "2024-01-01", "2024-12-31", leaderId));

        List<Project> projects = projectRepository.findByLeader(leaderId);

        assertFalse(projects.isEmpty());
        assertEquals("LeaderProject", projects.get(0).getName());
    }

    @Test
    void shouldFindByLeaderReturnEmptyForUnknown() {
        List<Project> projects = projectRepository.findByLeader(999L);
        assertTrue(projects.isEmpty());
    }

    @Test
    void shouldUpdateProject() {
        Long id = projectRepository.save(new Project(null, "Old", "Old Desc",
                "2024-01-01", "2024-06-30", leaderId));

        projectRepository.update(new Project(id, "New", "New Desc",
                "2025-01-01", "2025-12-31", leaderId));

        Project found = projectRepository.findById(id);
        assertEquals("New", found.getName());
        assertEquals("New Desc", found.getDescription());
        assertEquals("2025-01-01", found.getStartDate());
    }

    @Test
    void shouldDeleteProject() {
        Long id = projectRepository.save(new Project(null, "ToDelete", "Desc",
                "2024-01-01", "2024-12-31", leaderId));

        projectRepository.delete(id);

        assertNull(projectRepository.findById(id));
    }

    @Test
    void shouldAddAndFindMembers() {
        Long projectId = projectRepository.save(new Project(null, "TeamProject", "Desc",
                "2024-01-01", "2024-12-31", leaderId));

        projectRepository.addMember(projectId, memberId);

        List<User> members = projectRepository.findMembers(projectId);
        assertEquals(1, members.size());
        assertEquals("Member", members.get(0).getLastName());
    }

    @Test
    void shouldFindMemberIds() {
        Long projectId = projectRepository.save(new Project(null, "P", "D",
                "2024-01-01", "2024-12-31", leaderId));

        projectRepository.addMember(projectId, leaderId);
        projectRepository.addMember(projectId, memberId);

        List<Long> ids = projectRepository.findMemberIds(projectId);
        assertEquals(2, ids.size());
        assertTrue(ids.contains(leaderId));
        assertTrue(ids.contains(memberId));
    }

    @Test
    void shouldRemoveMember() {
        Long projectId = projectRepository.save(new Project(null, "P", "D",
                "2024-01-01", "2024-12-31", leaderId));

        projectRepository.addMember(projectId, memberId);
        projectRepository.removeMember(projectId, memberId);

        List<User> members = projectRepository.findMembers(projectId);
        assertTrue(members.isEmpty());
    }

    @Test
    void shouldCascadeDeleteMembersWhenProjectDeleted() {
        Long projectId = projectRepository.save(new Project(null, "Cascade", "D",
                "2024-01-01", "2024-12-31", leaderId));
        projectRepository.addMember(projectId, memberId);

        projectRepository.delete(projectId);

        List<Long> ids = projectRepository.findMemberIds(projectId);
        assertTrue(ids.isEmpty());
    }
}
