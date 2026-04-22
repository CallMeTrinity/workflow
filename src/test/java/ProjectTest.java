package org.example.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ProjectTest {

    @Test
    void shouldCreateProjectCorrectly() {
        Project project = new Project(
                1L, "Website creation", "Create a website for client X",
                "2024-01-01", "2024-06-30", 1L
        );

        assertEquals(1L, project.getId());
        assertEquals("Website creation", project.getName());
        assertEquals("Create a website for client X", project.getDescription());
    }

    @Test
    void shouldReturnProjectLeaderId() {
        Project project = new Project(
                1L, "Website creation", "Create a website for client X",
                "2024-01-01", "2024-06-30", 1L
        );

        assertEquals(1L, project.getProjectLeaderId());
    }

    @Test
    void shouldCreateProjectWithCreatedAt() {
        Project project = new Project(1L, "Name", "Desc",
                "2024-01-01", "2024-06-30", 1L, "2024-01-01T10:00:00");

        assertEquals("2024-01-01", project.getStartDate());
        assertEquals("2024-06-30", project.getEndDate());
        assertEquals("2024-01-01T10:00:00", project.getCreatedAt());
    }

    @Test
    void shouldCreateProjectWithNoArgConstructor() {
        Project project = new Project();
        assertNull(project.getId());
        assertNull(project.getName());
    }

    @Test
    void shouldSetAndGetAllFields() {
        Project project = new Project();

        project.setId(5L);
        project.setName("New Project");
        project.setDescription("A new project");
        project.setStartDate("2025-01-01");
        project.setEndDate("2025-12-31");
        project.setCreatedAt("2025-01-01T08:00:00");
        project.setProjectLeaderId(10L);

        assertEquals(5L, project.getId());
        assertEquals("New Project", project.getName());
        assertEquals("A new project", project.getDescription());
        assertEquals("2025-01-01", project.getStartDate());
        assertEquals("2025-12-31", project.getEndDate());
        assertEquals("2025-01-01T08:00:00", project.getCreatedAt());
        assertEquals(10L, project.getProjectLeaderId());
    }
}
