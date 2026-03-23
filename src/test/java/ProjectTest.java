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
}
