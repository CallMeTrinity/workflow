PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS users (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    last_name         TEXT NOT NULL,
    first_name      TEXT NOT NULL,
    mail       TEXT NOT NULL UNIQUE,
    password    TEXT NOT NULL,
    role        TEXT NOT NULL CHECK(role IN ('ADMIN', 'PROJECT_LEADER', 'MEMBER')),
    username TEXT
);

CREATE TABLE IF NOT EXISTS project (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT NOT NULL,
    description TEXT,
    start_date  TEXT NOT NULL, -- format YYYY-MM-DD
    end_date    TEXT NOT NULL,
    project_leader_id INTEGER NOT NULL,
	created_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (project_leader_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS project_member (
    project_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    PRIMARY KEY (project_id, user_id),
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_story (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    title       TEXT NOT NULL,
    description TEXT,
    priority    TEXT NOT NULL DEFAULT 'MEDIUM' CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    project_id  INTEGER NOT NULL,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS task (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    title           TEXT NOT NULL,
    description     TEXT,
    status          TEXT NOT NULL DEFAULT 'TODO' CHECK (status IN ('TODO', 'IN_PROGRESS', 'DONE', 'BLOCKED')),
    priority        TEXT NOT NULL DEFAULT 'MEDIUM' CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    deadline        TEXT, -- format YYYY-MM-DD
    time_estimate    INTEGER, -- in hours
    assigned_user_id INTEGER,
    project_id       INTEGER NOT NULL,
    user_story_id   INTEGER,
    task_leader_id  INTEGER,
    FOREIGN KEY (assigned_user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (user_story_id) REFERENCES user_story(id) ON DELETE SET NULL,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (task_leader_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS room (
    id  INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    capacity INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS reservation (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT,
    date TEXT NOT NULL, -- format YYYY-MM-DD
    start_time TEXT NOT NULL, -- format HH:MM
    end_time TEXT NOT NULL, -- format HH:MM
    room_id INTEGER NOT NULL,
    project_id INTEGER,
    organizer_id INTEGER NOT NULL,
    FOREIGN KEY (room_id) REFERENCES room(id) ON DELETE CASCADE,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE SET NULL,
    FOREIGN KEY (organizer_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS participants_reservation (
    reservation_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    PRIMARY KEY (reservation_id, user_id),
    FOREIGN KEY (reservation_id) REFERENCES reservation(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS notification (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    message    TEXT NOT NULL,
    user_id    INTEGER NOT NULL,
    is_read    INTEGER NOT NULL DEFAULT 0,
    project_id INTEGER,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE SET NULL
);

-- Index sur les colonnes de jointure et de recherche frequentes
CREATE INDEX IF NOT EXISTS idx_task_project ON task(project_id);
CREATE INDEX IF NOT EXISTS idx_task_assigned_user ON task(assigned_user_id);
CREATE INDEX IF NOT EXISTS idx_task_user_story ON task(user_story_id);
CREATE INDEX IF NOT EXISTS idx_user_story_project ON user_story(project_id);
CREATE INDEX IF NOT EXISTS idx_reservation_room_date ON reservation(room_id, date);
CREATE INDEX IF NOT EXISTS idx_reservation_organizer ON reservation(organizer_id);
CREATE INDEX IF NOT EXISTS idx_notification_user ON notification(user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_participants_reservation_user ON participants_reservation(user_id);

INSERT OR IGNORE INTO users (last_name, first_name, mail, password, role, username) VALUES
(
 'Administrateur',
 'Admin',
 'admin@project.com',
 '$2a$12$n1CPDG.hPhT2Co40sU72c.9anLxYE1lJRF6BGGhzEhW1ftbBuCEzC', -- password: admin123
 'ADMIN',
 'x'
);


