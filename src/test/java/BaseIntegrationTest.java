import org.example.config.DatabaseConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Classe de base pour les tests d'integration.
 * Cree une base SQLite en memoire avant chaque test
 * et la ferme apres chaque test pour garantir l'isolation.
 */
public abstract class BaseIntegrationTest {

    protected Connection connection;

    @BeforeEach
    void setUpDatabase() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        DatabaseConfig.setConnection(connection);

        // Charger et executer le schema
        InputStream is = getClass().getResourceAsStream("/db/schema.sql");
        String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        String[] instructions = sql.split(";");
        for (String instruction : instructions) {
            String trimmed = instruction.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute(trimmed);
                }
            }
        }

        // Appliquer les migrations comme dans DatabaseConfig
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("ALTER TABLE task ADD COLUMN task_leader_id INTEGER REFERENCES users(id) ON DELETE SET NULL");
        } catch (Exception ignored) { }

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("ALTER TABLE participants_reservation ADD COLUMN status TEXT DEFAULT 'pending'");
        } catch (Exception ignored) { }

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("ALTER TABLE notification ADD COLUMN project_id INTEGER REFERENCES project(id) ON DELETE SET NULL");
        } catch (Exception ignored) { }
    }

    @AfterEach
    void tearDownDatabase() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        DatabaseConfig.setConnection(null);
    }
}
