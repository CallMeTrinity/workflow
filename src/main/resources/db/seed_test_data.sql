-- ============================================================
-- SEED DATA — données de test
-- Mot de passe pour tous les utilisateurs : admin123
-- Lancer avec :  sqlite3 project_management.db < src/main/resources/db/seed_test_data.sql
-- ============================================================

PRAGMA foreign_keys = ON;

-- ============================================================
-- USERS  (l'admin id=1 existe déjà via schema.sql)
-- ============================================================

INSERT OR IGNORE INTO users (last_name, first_name, mail, password, role) VALUES
('Dupont',   'Marie',    'marie.dupont@project.com',   '$2a$12$n1CPDG.hPhT2Co40sU72c.9anLxYE1lJRF6BGGhzEhW1ftbBuCEzC', 'PROJECT_LEADER'),
('Martin',   'Lucas',    'lucas.martin@project.com',   '$2a$12$n1CPDG.hPhT2Co40sU72c.9anLxYE1lJRF6BGGhzEhW1ftbBuCEzC', 'PROJECT_LEADER'),
('Bernard',  'Sophie',   'sophie.bernard@project.com', '$2a$12$n1CPDG.hPhT2Co40sU72c.9anLxYE1lJRF6BGGhzEhW1ftbBuCEzC', 'MEMBER'),
('Petit',    'Thomas',   'thomas.petit@project.com',   '$2a$12$n1CPDG.hPhT2Co40sU72c.9anLxYE1lJRF6BGGhzEhW1ftbBuCEzC', 'MEMBER'),
('Moreau',   'Julie',    'julie.moreau@project.com',   '$2a$12$n1CPDG.hPhT2Co40sU72c.9anLxYE1lJRF6BGGhzEhW1ftbBuCEzC', 'MEMBER'),
('Leroy',    'Antoine',  'antoine.leroy@project.com',  '$2a$12$n1CPDG.hPhT2Co40sU72c.9anLxYE1lJRF6BGGhzEhW1ftbBuCEzC', 'MEMBER'),
('Roux',     'Camille',  'camille.roux@project.com',   '$2a$12$n1CPDG.hPhT2Co40sU72c.9anLxYE1lJRF6BGGhzEhW1ftbBuCEzC', 'MEMBER'),
('Garcia',   'Emma',     'emma.garcia@project.com',    '$2a$12$n1CPDG.hPhT2Co40sU72c.9anLxYE1lJRF6BGGhzEhW1ftbBuCEzC', 'PROJECT_LEADER'),
('Fournier', 'Hugo',     'hugo.fournier@project.com',  '$2a$12$n1CPDG.hPhT2Co40sU72c.9anLxYE1lJRF6BGGhzEhW1ftbBuCEzC', 'MEMBER'),
('Lambert',  'Léa',      'lea.lambert@project.com',    '$2a$12$n1CPDG.hPhT2Co40sU72c.9anLxYE1lJRF6BGGhzEhW1ftbBuCEzC', 'MEMBER');

-- ============================================================
-- PROJECTS  (sous-requêtes pour résoudre les user IDs)
-- ============================================================

INSERT INTO project (name, description, start_date, end_date, project_leader_id, created_at) VALUES
('Refonte Site Web',    'Refonte complète du site vitrine avec nouveau design responsive.',     '2026-03-01', '2026-06-30', (SELECT id FROM users WHERE mail='marie.dupont@project.com'),  '2026-03-01 09:00:00'),
('App Mobile V2',       'Développement de la v2 de l''application mobile iOS/Android.',        '2026-04-01', '2026-09-30', (SELECT id FROM users WHERE mail='lucas.martin@project.com'),  '2026-03-15 10:30:00'),
('Migration Cloud',     'Migration de l''infrastructure on-premise vers AWS.',                  '2026-02-15', '2026-05-31', (SELECT id FROM users WHERE mail='marie.dupont@project.com'),  '2026-02-10 14:00:00'),
('Dashboard Analytics', 'Tableau de bord temps réel pour le suivi des KPIs.',                   '2026-04-15', '2026-07-15', (SELECT id FROM users WHERE mail='emma.garcia@project.com'),   '2026-04-01 08:00:00'),
('API Partenaires',     'API REST pour l''intégration avec les partenaires externes.',          '2026-03-10', '2026-06-10', (SELECT id FROM users WHERE mail='lucas.martin@project.com'),  '2026-03-05 11:00:00');

-- ============================================================
-- USER STORIES  (sous-requêtes pour résoudre les project IDs)
-- ============================================================

-- Refonte Site Web
INSERT INTO user_story (title, description, priority, project_id) VALUES
('Page d''accueil',     'Nouvelle page d''accueil avec hero section et témoignages.',  'HIGH',     (SELECT id FROM project WHERE name='Refonte Site Web')),
('Page tarifs',         'Comparatif des offres avec toggle mensuel/annuel.',           'MEDIUM',   (SELECT id FROM project WHERE name='Refonte Site Web')),
('Formulaire contact',  'Formulaire de contact avec validation et captcha.',           'LOW',      (SELECT id FROM project WHERE name='Refonte Site Web')),
('Blog intégré',        'Section blog avec pagination et catégories.',                 'MEDIUM',   (SELECT id FROM project WHERE name='Refonte Site Web'));

-- App Mobile V2
INSERT INTO user_story (title, description, priority, project_id) VALUES
('Onboarding',          'Parcours d''onboarding en 3 étapes pour les nouveaux users.', 'HIGH',     (SELECT id FROM project WHERE name='App Mobile V2')),
('Notifications push',  'Système de notifications push personnalisables.',              'HIGH',     (SELECT id FROM project WHERE name='App Mobile V2')),
('Mode hors-ligne',     'Synchronisation et cache pour usage sans connexion.',          'CRITICAL', (SELECT id FROM project WHERE name='App Mobile V2')),
('Profil utilisateur',  'Page profil avec édition photo, bio et préférences.',          'MEDIUM',   (SELECT id FROM project WHERE name='App Mobile V2'));

-- Migration Cloud
INSERT INTO user_story (title, description, priority, project_id) VALUES
('Audit infra existante', 'Inventaire complet des serveurs et services à migrer.',      'CRITICAL', (SELECT id FROM project WHERE name='Migration Cloud')),
('Setup VPC',             'Configuration du réseau virtuel et des security groups.',     'HIGH',     (SELECT id FROM project WHERE name='Migration Cloud')),
('Migration BDD',         'Migration PostgreSQL vers RDS avec zero-downtime.',           'CRITICAL', (SELECT id FROM project WHERE name='Migration Cloud'));

-- Dashboard Analytics
INSERT INTO user_story (title, description, priority, project_id) VALUES
('Widget ventes',       'Graphique temps réel du chiffre d''affaires.',                 'HIGH',     (SELECT id FROM project WHERE name='Dashboard Analytics')),
('Widget utilisateurs', 'Métriques d''engagement et rétention.',                        'MEDIUM',   (SELECT id FROM project WHERE name='Dashboard Analytics')),
('Export PDF',          'Génération de rapports PDF planifiés.',                         'LOW',      (SELECT id FROM project WHERE name='Dashboard Analytics'));

-- API Partenaires
INSERT INTO user_story (title, description, priority, project_id) VALUES
('Authentification OAuth2', 'Flow OAuth2 avec refresh tokens pour les partenaires.',    'CRITICAL', (SELECT id FROM project WHERE name='API Partenaires')),
('Endpoints catalogue',     'CRUD produits avec pagination et filtres.',                 'HIGH',     (SELECT id FROM project WHERE name='API Partenaires')),
('Rate limiting',           'Limitation de débit par clé API avec quotas.',              'MEDIUM',   (SELECT id FROM project WHERE name='API Partenaires'));

-- ============================================================
-- TASKS  (sous-requêtes pour project_id, user_story_id, assigned_user_id)
-- ============================================================

-- Helpers :
--   P(name)  = (SELECT id FROM project WHERE name=...)
--   U(mail)  = (SELECT id FROM users WHERE mail=...)
--   US(title,proj) = (SELECT id FROM user_story WHERE title=... AND project_id=P(...))

-- ── Refonte Site Web / Page d'accueil ──

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Maquette Figma hero section',
 'Créer la maquette haute fidélité de la hero section.',
 'DONE', 'HIGH', '2026-03-15', 8,
 (SELECT id FROM users WHERE mail='sophie.bernard@project.com'),
 (SELECT id FROM project WHERE name='Refonte Site Web'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Page d''accueil' AND p.name='Refonte Site Web'));

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Intégration HTML/CSS hero',
 'Découper et intégrer la maquette en HTML/CSS responsive.',
 'DONE', 'HIGH', '2026-03-25', 12,
 (SELECT id FROM users WHERE mail='sophie.bernard@project.com'),
 (SELECT id FROM project WHERE name='Refonte Site Web'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Page d''accueil' AND p.name='Refonte Site Web'));

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Section témoignages',
 'Carrousel de témoignages clients avec API.',
 'IN_PROGRESS', 'MEDIUM', '2026-04-10', 6,
 (SELECT id FROM users WHERE mail='thomas.petit@project.com'),
 (SELECT id FROM project WHERE name='Refonte Site Web'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Page d''accueil' AND p.name='Refonte Site Web'));

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Animations scroll',
 'Ajouter les animations au scroll avec Intersection Observer.',
 'TODO', 'LOW', '2026-04-20', 4,
 NULL,
 (SELECT id FROM project WHERE name='Refonte Site Web'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Page d''accueil' AND p.name='Refonte Site Web'));

-- ── Refonte Site Web / Page tarifs ──

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Design page tarifs',
 'Maquette comparative des 3 offres.',
 'DONE', 'MEDIUM', '2026-03-20', 6,
 (SELECT id FROM users WHERE mail='sophie.bernard@project.com'),
 (SELECT id FROM project WHERE name='Refonte Site Web'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Page tarifs' AND p.name='Refonte Site Web'));

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Composant toggle mensuel/annuel',
 'Composant React pour switch entre prix mensuels et annuels.',
 'IN_PROGRESS', 'MEDIUM', '2026-04-05', 4,
 (SELECT id FROM users WHERE mail='thomas.petit@project.com'),
 (SELECT id FROM project WHERE name='Refonte Site Web'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Page tarifs' AND p.name='Refonte Site Web'));

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Intégration Stripe Checkout',
 'Lien vers Stripe pour le paiement.',
 'TODO', 'HIGH', '2026-04-30', 8,
 NULL,
 (SELECT id FROM project WHERE name='Refonte Site Web'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Page tarifs' AND p.name='Refonte Site Web'));

-- ── Refonte Site Web / Formulaire contact ──

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Formulaire avec validation',
 'Formulaire React Hook Form + Zod validation.',
 'TODO', 'LOW', '2026-05-10', 4,
 (SELECT id FROM users WHERE mail='julie.moreau@project.com'),
 (SELECT id FROM project WHERE name='Refonte Site Web'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Formulaire contact' AND p.name='Refonte Site Web'));

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Intégration reCAPTCHA',
 'Ajout de Google reCAPTCHA v3.',
 'TODO', 'LOW', '2026-05-15', 2,
 NULL,
 (SELECT id FROM project WHERE name='Refonte Site Web'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Formulaire contact' AND p.name='Refonte Site Web'));

-- ── Refonte Site Web / Blog intégré ──

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Setup CMS headless',
 'Configurer Strapi comme backend pour le blog.',
 'TODO', 'MEDIUM', '2026-05-01', 6,
 (SELECT id FROM users WHERE mail='antoine.leroy@project.com'),
 (SELECT id FROM project WHERE name='Refonte Site Web'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Blog intégré' AND p.name='Refonte Site Web'));

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Template article',
 'Page article avec MDX, table des matières, partage.',
 'TODO', 'MEDIUM', '2026-05-20', 8,
 NULL,
 (SELECT id FROM project WHERE name='Refonte Site Web'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Blog intégré' AND p.name='Refonte Site Web'));

-- ── App Mobile V2 / Onboarding ──

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Design écrans onboarding',
 'Maquettes des 3 écrans avec illustrations.',
 'DONE', 'HIGH', '2026-04-10', 6,
 (SELECT id FROM users WHERE mail='sophie.bernard@project.com'),
 (SELECT id FROM project WHERE name='App Mobile V2'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Onboarding' AND p.name='App Mobile V2'));

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Implémentation SwiftUI',
 'Développer le parcours en SwiftUI avec animations.',
 'IN_PROGRESS', 'HIGH', '2026-04-25', 10,
 (SELECT id FROM users WHERE mail='julie.moreau@project.com'),
 (SELECT id FROM project WHERE name='App Mobile V2'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Onboarding' AND p.name='App Mobile V2'));

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Implémentation Jetpack Compose',
 'Port Android avec Jetpack Compose.',
 'TODO', 'HIGH', '2026-05-05', 10,
 (SELECT id FROM users WHERE mail='antoine.leroy@project.com'),
 (SELECT id FROM project WHERE name='App Mobile V2'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Onboarding' AND p.name='App Mobile V2'));

-- ── App Mobile V2 / Notifications push ──

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Setup Firebase Cloud Messaging',
 'Configurer FCM pour iOS et Android.',
 'IN_PROGRESS', 'HIGH', '2026-04-20', 4,
 (SELECT id FROM users WHERE mail='antoine.leroy@project.com'),
 (SELECT id FROM project WHERE name='App Mobile V2'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Notifications push' AND p.name='App Mobile V2'));

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Backend envoi notifications',
 'Endpoint API + job queue pour envoi batch.',
 'TODO', 'HIGH', '2026-05-10', 8,
 (SELECT id FROM users WHERE mail='thomas.petit@project.com'),
 (SELECT id FROM project WHERE name='App Mobile V2'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Notifications push' AND p.name='App Mobile V2'));

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Préférences notifications',
 'Écran de préférences avec toggles par catégorie.',
 'TODO', 'MEDIUM', '2026-05-20', 6,
 NULL,
 (SELECT id FROM project WHERE name='App Mobile V2'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Notifications push' AND p.name='App Mobile V2'));

-- ── App Mobile V2 / Mode hors-ligne ──

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Architecture cache local',
 'Concevoir le système de cache avec SQLite embarqué.',
 'TODO', 'CRITICAL', '2026-05-15', 8,
 (SELECT id FROM users WHERE mail='julie.moreau@project.com'),
 (SELECT id FROM project WHERE name='App Mobile V2'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Mode hors-ligne' AND p.name='App Mobile V2'));

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Sync engine bidirectionnel',
 'Moteur de synchronisation avec gestion des conflits.',
 'TODO', 'CRITICAL', '2026-06-15', 20,
 NULL,
 (SELECT id FROM project WHERE name='App Mobile V2'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Mode hors-ligne' AND p.name='App Mobile V2'));

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Tests hors-ligne',
 'Suite de tests E2E en mode avion.',
 'TODO', 'HIGH', '2026-06-30', 6,
 NULL,
 (SELECT id FROM project WHERE name='App Mobile V2'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Mode hors-ligne' AND p.name='App Mobile V2'));

-- ── App Mobile V2 / Profil utilisateur ──

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('UI profil et édition',
 'Écran profil avec upload photo et champs éditables.',
 'TODO', 'MEDIUM', '2026-05-25', 6,
 (SELECT id FROM users WHERE mail='sophie.bernard@project.com'),
 (SELECT id FROM project WHERE name='App Mobile V2'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Profil utilisateur' AND p.name='App Mobile V2'));

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('API profil',
 'Endpoints REST pour CRUD profil utilisateur.',
 'TODO', 'MEDIUM', '2026-05-20', 4,
 (SELECT id FROM users WHERE mail='thomas.petit@project.com'),
 (SELECT id FROM project WHERE name='App Mobile V2'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Profil utilisateur' AND p.name='App Mobile V2'));

-- ── Migration Cloud / Audit infra existante ──

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Inventaire serveurs',
 'Lister tous les serveurs, services et dépendances.',
 'DONE', 'CRITICAL', '2026-03-01', 8,
 (SELECT id FROM users WHERE mail='antoine.leroy@project.com'),
 (SELECT id FROM project WHERE name='Migration Cloud'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Audit infra existante' AND p.name='Migration Cloud'));

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Cartographie réseau',
 'Schéma réseau complet avec flux de données.',
 'DONE', 'HIGH', '2026-03-10', 6,
 (SELECT id FROM users WHERE mail='antoine.leroy@project.com'),
 (SELECT id FROM project WHERE name='Migration Cloud'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Audit infra existante' AND p.name='Migration Cloud'));

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Rapport de risques',
 'Identifier les risques et plan de mitigation.',
 'IN_PROGRESS', 'HIGH', '2026-03-20', 4,
 (SELECT id FROM users WHERE mail='julie.moreau@project.com'),
 (SELECT id FROM project WHERE name='Migration Cloud'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Audit infra existante' AND p.name='Migration Cloud'));

-- ── Migration Cloud / Setup VPC ──

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Terraform VPC + subnets',
 'IaC pour VPC, subnets publics/privés, NAT gateway.',
 'IN_PROGRESS', 'HIGH', '2026-04-01', 10,
 (SELECT id FROM users WHERE mail='antoine.leroy@project.com'),
 (SELECT id FROM project WHERE name='Migration Cloud'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Setup VPC' AND p.name='Migration Cloud'));

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Security groups & NACLs',
 'Règles firewall pour chaque tier applicatif.',
 'TODO', 'HIGH', '2026-04-10', 6,
 NULL,
 (SELECT id FROM project WHERE name='Migration Cloud'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Setup VPC' AND p.name='Migration Cloud'));

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('VPN site-to-site',
 'Tunnel VPN entre le bureau et le VPC.',
 'TODO', 'MEDIUM', '2026-04-20', 8,
 NULL,
 (SELECT id FROM project WHERE name='Migration Cloud'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Setup VPC' AND p.name='Migration Cloud'));

-- ── Migration Cloud / Migration BDD ──

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Setup RDS Multi-AZ',
 'Provisionner RDS PostgreSQL en Multi-AZ.',
 'TODO', 'CRITICAL', '2026-04-15', 4,
 (SELECT id FROM users WHERE mail='antoine.leroy@project.com'),
 (SELECT id FROM project WHERE name='Migration Cloud'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Migration BDD' AND p.name='Migration Cloud'));

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Script migration DMS',
 'Configurer AWS DMS pour réplication continue.',
 'TODO', 'CRITICAL', '2026-04-25', 12,
 NULL,
 (SELECT id FROM project WHERE name='Migration Cloud'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Migration BDD' AND p.name='Migration Cloud'));

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Test de basculement',
 'Dry-run de basculement avec rollback plan.',
 'TODO', 'HIGH', '2026-05-10', 8,
 NULL,
 (SELECT id FROM project WHERE name='Migration Cloud'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Migration BDD' AND p.name='Migration Cloud'));

-- ── Dashboard Analytics / Widget ventes ──

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('API agrégation ventes',
 'Endpoint avec agrégation par jour/semaine/mois.',
 'IN_PROGRESS', 'HIGH', '2026-04-30', 8,
 (SELECT id FROM users WHERE mail='thomas.petit@project.com'),
 (SELECT id FROM project WHERE name='Dashboard Analytics'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Widget ventes' AND p.name='Dashboard Analytics'));

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Graphique D3.js temps réel',
 'Line chart avec WebSocket pour mise à jour live.',
 'TODO', 'HIGH', '2026-05-15', 10,
 (SELECT id FROM users WHERE mail='sophie.bernard@project.com'),
 (SELECT id FROM project WHERE name='Dashboard Analytics'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Widget ventes' AND p.name='Dashboard Analytics'));

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Filtres date et produit',
 'Sélecteurs pour filtrer les données affichées.',
 'TODO', 'MEDIUM', '2026-05-25', 4,
 NULL,
 (SELECT id FROM project WHERE name='Dashboard Analytics'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Widget ventes' AND p.name='Dashboard Analytics'));

-- ── Dashboard Analytics / Widget utilisateurs ──

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Métriques engagement',
 'Calcul DAU, WAU, MAU et taux de rétention.',
 'TODO', 'MEDIUM', '2026-05-20', 6,
 (SELECT id FROM users WHERE mail='thomas.petit@project.com'),
 (SELECT id FROM project WHERE name='Dashboard Analytics'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Widget utilisateurs' AND p.name='Dashboard Analytics'));

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Visualisation cohortes',
 'Tableau de cohortes avec heatmap.',
 'TODO', 'MEDIUM', '2026-06-01', 8,
 NULL,
 (SELECT id FROM project WHERE name='Dashboard Analytics'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Widget utilisateurs' AND p.name='Dashboard Analytics'));

-- ── Dashboard Analytics / Export PDF ──

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Génération PDF serveur',
 'Service de rendu PDF avec Puppeteer.',
 'TODO', 'LOW', '2026-06-15', 8,
 (SELECT id FROM users WHERE mail='julie.moreau@project.com'),
 (SELECT id FROM project WHERE name='Dashboard Analytics'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Export PDF' AND p.name='Dashboard Analytics'));

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Planification envoi email',
 'Cron job pour envoi hebdomadaire du rapport.',
 'TODO', 'LOW', '2026-06-30', 4,
 NULL,
 (SELECT id FROM project WHERE name='Dashboard Analytics'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Export PDF' AND p.name='Dashboard Analytics'));

-- ── API Partenaires / Authentification OAuth2 ──

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Flow authorization code',
 'Implémenter le flow OAuth2 authorization code.',
 'DONE', 'CRITICAL', '2026-03-25', 10,
 (SELECT id FROM users WHERE mail='thomas.petit@project.com'),
 (SELECT id FROM project WHERE name='API Partenaires'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Authentification OAuth2' AND p.name='API Partenaires'));

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Refresh token rotation',
 'Rotation automatique des refresh tokens.',
 'DONE', 'CRITICAL', '2026-04-01', 6,
 (SELECT id FROM users WHERE mail='thomas.petit@project.com'),
 (SELECT id FROM project WHERE name='API Partenaires'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Authentification OAuth2' AND p.name='API Partenaires'));

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Documentation OAuth',
 'Guide d''intégration pour les partenaires.',
 'IN_PROGRESS', 'MEDIUM', '2026-04-15', 4,
 (SELECT id FROM users WHERE mail='sophie.bernard@project.com'),
 (SELECT id FROM project WHERE name='API Partenaires'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Authentification OAuth2' AND p.name='API Partenaires'));

-- ── API Partenaires / Endpoints catalogue ──

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('CRUD produits',
 'Endpoints GET/POST/PUT/DELETE avec pagination.',
 'IN_PROGRESS', 'HIGH', '2026-04-20', 8,
 (SELECT id FROM users WHERE mail='julie.moreau@project.com'),
 (SELECT id FROM project WHERE name='API Partenaires'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Endpoints catalogue' AND p.name='API Partenaires'));

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Recherche et filtres',
 'Query params pour recherche full-text et filtres.',
 'TODO', 'HIGH', '2026-05-01', 6,
 (SELECT id FROM users WHERE mail='antoine.leroy@project.com'),
 (SELECT id FROM project WHERE name='API Partenaires'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Endpoints catalogue' AND p.name='API Partenaires'));

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Webhooks événements',
 'Notifications webhook sur création/modif produit.',
 'TODO', 'MEDIUM', '2026-05-15', 8,
 NULL,
 (SELECT id FROM project WHERE name='API Partenaires'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Endpoints catalogue' AND p.name='API Partenaires'));

-- ── API Partenaires / Rate limiting ──

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Middleware rate limiter',
 'Token bucket par clé API avec Redis.',
 'TODO', 'MEDIUM', '2026-05-10', 6,
 (SELECT id FROM users WHERE mail='antoine.leroy@project.com'),
 (SELECT id FROM project WHERE name='API Partenaires'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Rate limiting' AND p.name='API Partenaires'));

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Dashboard quotas partenaire',
 'Interface partenaire pour suivre sa consommation.',
 'TODO', 'LOW', '2026-05-25', 8,
 NULL,
 (SELECT id FROM project WHERE name='API Partenaires'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Rate limiting' AND p.name='API Partenaires'));

INSERT INTO task (title, description, status, priority, deadline, time_estimate, assigned_user_id, project_id, user_story_id) VALUES
('Alertes dépassement',
 'Email automatique à 80% et 100% du quota.',
 'TODO', 'LOW', '2026-06-01', 4,
 NULL,
 (SELECT id FROM project WHERE name='API Partenaires'),
 (SELECT us.id FROM user_story us JOIN project p ON us.project_id=p.id WHERE us.title='Rate limiting' AND p.name='API Partenaires'));

-- ============================================================
-- ROOMS
-- ============================================================

INSERT OR IGNORE INTO room (name, capacity) VALUES
('Salle Apollo',    10),
('Salle Gemini',    6),
('Salle Mercury',   4),
('Salle Artemis',   16),
('Salle Voyager',   8);

-- ============================================================
-- RESERVATIONS  (sous-requêtes pour room_id, project_id, organizer_id)
-- ============================================================

INSERT INTO reservation (title, description, date, start_time, end_time, room_id, project_id, organizer_id) VALUES
('Sprint planning Refonte',
 'Planification du sprint 4 — site web.',
 '2026-03-31', '09:00', '10:30',
 (SELECT id FROM room WHERE name='Salle Apollo'),
 (SELECT id FROM project WHERE name='Refonte Site Web'),
 (SELECT id FROM users WHERE mail='marie.dupont@project.com'));

INSERT INTO reservation (title, description, date, start_time, end_time, room_id, project_id, organizer_id) VALUES
('Daily App Mobile',
 'Stand-up quotidien équipe mobile.',
 '2026-03-31', '09:30', '10:00',
 (SELECT id FROM room WHERE name='Salle Mercury'),
 (SELECT id FROM project WHERE name='App Mobile V2'),
 (SELECT id FROM users WHERE mail='lucas.martin@project.com'));

INSERT INTO reservation (title, description, date, start_time, end_time, room_id, project_id, organizer_id) VALUES
('Revue architecture Cloud',
 'Revue de l''architecture cible AWS avec l''équipe infra.',
 '2026-03-31', '14:00', '16:00',
 (SELECT id FROM room WHERE name='Salle Artemis'),
 (SELECT id FROM project WHERE name='Migration Cloud'),
 (SELECT id FROM users WHERE mail='marie.dupont@project.com'));

INSERT INTO reservation (title, description, date, start_time, end_time, room_id, project_id, organizer_id) VALUES
('Démo sprint Refonte',
 'Démonstration des livrables du sprint 3.',
 '2026-04-07', '11:00', '12:00',
 (SELECT id FROM room WHERE name='Salle Apollo'),
 (SELECT id FROM project WHERE name='Refonte Site Web'),
 (SELECT id FROM users WHERE mail='marie.dupont@project.com'));

INSERT INTO reservation (title, description, date, start_time, end_time, room_id, project_id, organizer_id) VALUES
('Workshop OAuth2 partenaires',
 'Atelier technique sur l''intégration OAuth2.',
 '2026-04-07', '14:00', '16:00',
 (SELECT id FROM room WHERE name='Salle Gemini'),
 (SELECT id FROM project WHERE name='API Partenaires'),
 (SELECT id FROM users WHERE mail='lucas.martin@project.com'));

INSERT INTO reservation (title, description, date, start_time, end_time, room_id, project_id, organizer_id) VALUES
('Rétro App Mobile',
 'Rétrospective sprint 2 — équipe mobile.',
 '2026-04-08', '10:00', '11:00',
 (SELECT id FROM room WHERE name='Salle Gemini'),
 (SELECT id FROM project WHERE name='App Mobile V2'),
 (SELECT id FROM users WHERE mail='lucas.martin@project.com'));

INSERT INTO reservation (title, description, date, start_time, end_time, room_id, project_id, organizer_id) VALUES
('Formation D3.js',
 'Session de formation interne sur D3.js pour le dashboard.',
 '2026-04-08', '14:00', '17:00',
 (SELECT id FROM room WHERE name='Salle Artemis'),
 (SELECT id FROM project WHERE name='Dashboard Analytics'),
 (SELECT id FROM users WHERE mail='emma.garcia@project.com'));

INSERT INTO reservation (title, description, date, start_time, end_time, room_id, project_id, organizer_id) VALUES
('Point migration BDD',
 'Suivi avancement migration PostgreSQL vers RDS.',
 '2026-04-09', '09:00', '10:00',
 (SELECT id FROM room WHERE name='Salle Mercury'),
 (SELECT id FROM project WHERE name='Migration Cloud'),
 (SELECT id FROM users WHERE mail='antoine.leroy@project.com'));

INSERT INTO reservation (title, description, date, start_time, end_time, room_id, project_id, organizer_id) VALUES
('Sprint planning Dashboard',
 'Planification sprint 1 du projet Dashboard Analytics.',
 '2026-04-09', '10:00', '11:30',
 (SELECT id FROM room WHERE name='Salle Voyager'),
 (SELECT id FROM project WHERE name='Dashboard Analytics'),
 (SELECT id FROM users WHERE mail='emma.garcia@project.com'));

INSERT INTO reservation (title, description, date, start_time, end_time, room_id, project_id, organizer_id) VALUES
('1:1 Marie / Sophie',
 'Point individuel hebdomadaire.',
 '2026-04-09', '15:00', '15:30',
 (SELECT id FROM room WHERE name='Salle Mercury'),
 NULL,
 (SELECT id FROM users WHERE mail='marie.dupont@project.com'));

INSERT INTO reservation (title, description, date, start_time, end_time, room_id, project_id, organizer_id) VALUES
('Réunion toute l''équipe',
 'All-hands mensuel — bilan et objectifs.',
 '2026-04-10', '09:00', '10:30',
 (SELECT id FROM room WHERE name='Salle Artemis'),
 NULL,
 (SELECT id FROM users WHERE mail='admin@project.com'));

INSERT INTO reservation (title, description, date, start_time, end_time, room_id, project_id, organizer_id) VALUES
('Code review API',
 'Revue de code collective sur les endpoints catalogue.',
 '2026-04-10', '14:00', '15:00',
 (SELECT id FROM room WHERE name='Salle Gemini'),
 (SELECT id FROM project WHERE name='API Partenaires'),
 (SELECT id FROM users WHERE mail='julie.moreau@project.com'));
