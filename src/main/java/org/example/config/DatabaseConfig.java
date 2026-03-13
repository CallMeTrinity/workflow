package org.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class DatabaseConfig {
    private static Connection connection = null;
    private static final String DB_URL = "jdbc:sqlite:project_management.db";

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
    }
}
