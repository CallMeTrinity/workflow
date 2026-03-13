import org.example.model.Project;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProjectTest {

    @Test
    void shouldCreateProjectCorrectly() {

        Project project = new Project(
                1L,
                "Website creation",
                "Create a website for client X"
        );

        assertEquals(1L, project.getId());
        assertEquals("Website creation", project.getName());
        assertEquals("Create a website for client X", project.getDescription());
        System.out.println("ProjectTest validé");
    }
}
