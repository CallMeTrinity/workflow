package org.example.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.*;

public class DatabaseConfig {
    private static Connection connection = null;
    private static final String DB_URL = buildDbUrl();

    private static String buildDbUrl() {
        // Si on tourne depuis un JAR (installer), on utilise APPDATA
        // Si on tourne depuis Maven (dev), on utilise la racine du projet
        String codeSource = DatabaseConfig.class.getProtectionDomain()
                .getCodeSource().getLocation().toString();

        if (codeSource.endsWith(".jar")) {
            String appData = System.getenv("APPDATA");
            Path dir = appData != null
                    ? Path.of(appData, "ProjectRoom")
                    : Path.of(System.getProperty("user.home"), ".projectroom");
            new File(dir.toString()).mkdirs();
            return "jdbc:sqlite:" + dir.resolve("project_management.db");
        } else {
            // Développement : racine du projet (répertoire de travail courant)
            return "jdbc:sqlite:project_management.db";
        }
    }

    /**
     * Permet d'injecter une connexion externe (utile pour les tests d'integration
     * avec une base SQLite en memoire).
     */
    public static void setConnection(Connection conn) {
        connection = conn;
    }

    // Retourne la connexion au lieu de void
    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(DB_URL);
                System.out.println("Connection to SQLite has been established.");
                initSchema();
            } catch (SQLException | IOException e) {
                throw new RuntimeException("Failed to initialize database", e);
            }
        }
        return connection;
    }

    private static void initSchema() throws IOException, SQLException {
        InputStream is = DatabaseConfig.class.getResourceAsStream("/db/schema.sql");

        if (is == null) {
            throw new RuntimeException("schema.sql not found in resources");
        }

        String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        // Filtre les lignes vides et les commentaires
        String[] instructions = sql.split(";");
        for (String instruction : instructions) {
            String trimmed = instruction.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute(trimmed);
                }
            }
        }

        // Migration : ajouter task_leader_id si absent
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("ALTER TABLE task ADD COLUMN task_leader_id INTEGER REFERENCES users(id) ON DELETE SET NULL");
        } catch (SQLException ignored) {
            // Colonne déjà présente — migration déjà appliquée
        }

        // Migration : ajouter status à participants_reservation
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("ALTER TABLE participants_reservation ADD COLUMN status TEXT DEFAULT 'pending'");
        } catch (SQLException ignored) {
            // Colonne déjà présente — migration déjà appliquée
        }

        // Migration : ajouter project_id à notification
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("ALTER TABLE notification ADD COLUMN project_id INTEGER REFERENCES project(id) ON DELETE SET NULL");
        } catch (SQLException ignored) {
            // Colonne déjà présente — migration déjà appliquée
        }
    }
}
