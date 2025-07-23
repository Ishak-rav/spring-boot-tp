-- ===============================================
-- Seeders pour l'API de Gestion de Tickets
-- ===============================================
-- Ce fichier contient les données initiales pour tester l'application
-- Il est chargé automatiquement au démarrage de Spring Boot

-- ===============================================
-- 1. UTILISATEURS
-- ===============================================
-- Insertion des utilisateurs de test
-- Mot de passe pour tous: "password" (hash BCrypt)
INSERT INTO utilisateur (id, pseudo, password, admin) VALUES
    (1, 'admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', true),
    (2, 'user', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', false),
    (3, 'support', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', true),
    (4, 'alice', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', false),
    (5, 'bob', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', false),
    (6, 'charlie', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', false),
    (7, 'diana', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', false),
    (8, 'tech_lead', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', true);

-- ===============================================
-- 2. PRIORITÉS
-- ===============================================
INSERT INTO priorite (id, nom) VALUES
    (1, 'Faible'),
    (2, 'Normale'),
    (3, 'Élevée'),
    (4, 'Urgente'),
    (5, 'Critique');

-- ===============================================
-- 3. CATÉGORIES
-- ===============================================
INSERT INTO categorie (id, nom) VALUES
    (1, 'Bug'),
    (2, 'Fonctionnalité'),
    (3, 'Support Technique'),
    (4, 'Documentation'),
    (5, 'Sécurité'),
    (6, 'Performance'),
    (7, 'Interface Utilisateur'),
    (8, 'Base de Données'),
    (9, 'Réseau'),
    (10, 'Authentification'),
    (11, 'API'),
    (12, 'Mobile'),
    (13, 'Configuration'),
    (14, 'Test'),
    (15, 'Déploiement');

-- ===============================================
-- 4. TICKETS OUVERTS (NON RÉSOLUS)
-- ===============================================
INSERT INTO ticket (id, titre, description, resolu, date_creation, date_resolution, soumetteur_id, resolveur_id, priorite_id) VALUES
    (1, 'Problème de connexion à la base de données', 
        'L''application ne parvient pas à se connecter à la base de données depuis ce matin. Les utilisateurs reçoivent une erreur 500 lors de l''accès aux données.', 
        false, '2024-01-15 09:30:00', null, 4, null, 5),
        
    (2, 'Interface utilisateur non responsive sur mobile',
        'Le site web ne s''affiche pas correctement sur les appareils mobiles. Les boutons sont trop petits et le texte déborde des conteneurs.',
        false, '2024-01-15 14:20:00', null, 5, null, 3),
        
    (3, 'Demande d''ajout d''une fonctionnalité de recherche avancée',
        'Il serait utile d''avoir une fonctionnalité de recherche avancée avec des filtres par date, catégorie, priorité et statut des tickets.',
        false, '2024-01-16 11:45:00', null, 6, null, 2),
        
    (4, 'Lenteur dans le chargement des pages',
        'Les pages mettent plus de 5 secondes à se charger, particulièrement la liste des tickets. Cela impacte fortement l''expérience utilisateur.',
        false, '2024-01-16 16:30:00', null, 7, null, 4),
        
    (5, 'Erreur lors de la création d''un nouveau ticket',
        'Impossible de créer un nouveau ticket. Le formulaire affiche une erreur de validation même avec des données correctes. L''erreur persiste depuis hier.',
        false, '2024-01-17 08:15:00', null, 2, null, 4),
        
    (6, 'Mise à jour de la documentation API',
        'La documentation de l''API REST n''est pas à jour avec les dernières modifications. Plusieurs nouveaux endpoints manquent dans la documentation Swagger.',
        false, '2024-01-17 13:00:00', null, 4, null, 2),
        
    (7, 'Problème d''authentification avec JWT',
        'Les tokens JWT expirent de manière prématurée. Les utilisateurs sont déconnectés au bout de quelques minutes au lieu des 24 heures configurées.',
        false, '2024-01-18 10:20:00', null, 5, null, 3),
        
    (8, 'Demande d''export des données en CSV',
        'Ajout d''une fonctionnalité permettant d''exporter la liste des tickets au format CSV pour faciliter l''analyse externe et les rapports.',
        false, '2024-01-18 15:45:00', null, 6, null, 1),
        
    (9, 'Bug d''affichage des statistiques',
        'Les graphiques de statistiques n''affichent pas les bonnes données. Les pourcentages sont incorrects et ne correspondent pas aux données réelles.',
        false, '2024-01-19 09:10:00', null, 7, null, 3),
        
    (10, 'Configuration SSL pour l''environnement de production',
         'Mettre en place une configuration SSL sécurisée pour l''environnement de production avec certificats valides et redirection HTTPS.',
         false, '2024-01-19 14:30:00', null, 1, null, 4),
         
    (11, 'Amélioration du système de notifications',
         'Développer un système de notifications en temps réel pour informer les utilisateurs des changements de statut de leurs tickets.',
         false, '2024-01-20 10:15:00', null, 3, null, 2),
         
    (12, 'Problème de pagination sur la liste des tickets',
         'La pagination ne fonctionne pas correctement. Les boutons suivant/précédent ne répondent pas et l''affichage reste bloqué sur la première page.',
         false, '2024-01-20 16:45:00', null, 4, null, 3);

-- ===============================================
-- 5. TICKETS RÉSOLUS
-- ===============================================
INSERT INTO ticket (id, titre, description, resolu, date_creation, date_resolution, soumetteur_id, resolveur_id, priorite_id) VALUES
    (13, 'Correction du bug de validation des formulaires',
         'Le formulaire de création de ticket ne validait pas correctement les champs obligatoires, permettant la soumission de données incomplètes.',
         true, '2024-01-10 10:00:00', '2024-01-12 16:30:00', 4, 1, 3),
         
    (14, 'Amélioration des performances de l''API',
         'Optimisation des requêtes SQL et ajout d''index sur les tables principales pour améliorer les temps de réponse de l''API REST.',
         true, '2024-01-08 14:20:00', '2024-01-11 11:45:00', 5, 3, 4),
         
    (15, 'Mise en place du système de logs',
         'Implémentation d''un système de logging complet avec rotation des fichiers et niveaux de log configurables selon l''environnement.',
         true, '2024-01-05 09:15:00', '2024-01-14 17:00:00', 6, 8, 2),
         
    (16, 'Correction de la faille de sécurité XSS',
         'Correction d''une vulnérabilité XSS dans le module de commentaires qui permettait l''injection de scripts malveillants.',
         true, '2024-01-12 11:30:00', '2024-01-13 14:20:00', 2, 1, 5),
         
    (17, 'Migration vers la nouvelle version de Spring Boot',
         'Mise à jour de Spring Boot vers la version 3.2 avec tous les ajustements nécessaires et tests de régression complets.',
         true, '2024-01-01 08:00:00', '2024-01-09 18:30:00', 1, 8, 3),
         
    (18, 'Optimisation de la base de données',
         'Ajout d''index sur les colonnes fréquemment utilisées et optimisation des requêtes les plus coûteuses pour améliorer les performances globales.',
         true, '2024-01-03 13:45:00', '2024-01-07 16:15:00', 7, 3, 3),
         
    (19, 'Correction du problème d''encodage des caractères',
         'Les caractères spéciaux et accents n''étaient pas affichés correctement dans l''interface à cause d''un problème d''encodage UTF-8.',
         true, '2024-01-06 15:20:00', '2024-01-08 10:30:00', 4, 1, 2),
         
    (20, 'Mise en place des tests unitaires',
         'Développement d''une suite complète de tests unitaires pour tous les services et contrôleurs afin d''améliorer la qualité du code.',
         true, '2024-01-02 16:00:00', '2024-01-15 12:00:00', 8, 8, 2),
         
    (21, 'Configuration Docker pour le déploiement',
         'Création des fichiers Docker et docker-compose pour faciliter le déploiement de l''application en conteneurs.',
         true, '2024-01-04 12:30:00', '2024-01-10 14:45:00', 1, 3, 1),
         
    (22, 'Ajout de la pagination pour les listes',
         'Implémentation de la pagination côté serveur pour toutes les listes afin d''améliorer les performances d''affichage des grandes collections.',
         true, '2024-01-07 11:10:00', '2024-01-13 09:20:00', 5, 8, 2),
         
    (23, 'Correction du bug de tri des colonnes',
         'Le tri par colonnes dans les tableaux ne fonctionnait pas correctement pour les dates et les valeurs numériques.',
         true, '2024-01-09 14:30:00', '2024-01-11 16:45:00', 6, 1, 2),
         
    (24, 'Mise en place de la sauvegarde automatique',
         'Configuration d''un système de sauvegarde automatique quotidienne de la base de données avec rotation sur 30 jours.',
         true, '2024-01-11 08:20:00', '2024-01-16 10:15:00', 2, 3, 3);

-- ===============================================
-- 6. RELATIONS TICKET-CATÉGORIE
-- ===============================================
-- Association des tickets avec leurs catégories (relation many-to-many)

-- Tickets ouverts (1-12)
INSERT INTO ticket_categorie (ticket_id, categorie_id) VALUES
    -- Ticket 1: Problème de base de données
    (1, 1), (1, 8), (1, 5),
    
    -- Ticket 2: Interface mobile
    (2, 1), (2, 7), (2, 12),
    
    -- Ticket 3: Fonctionnalité de recherche
    (3, 2), (3, 7), (3, 11),
    
    -- Ticket 4: Performance
    (4, 6), (4, 11), (4, 8),
    
    -- Ticket 5: Erreur création ticket
    (5, 1), (5, 7), (5, 11),
    
    -- Ticket 6: Documentation API
    (6, 4), (6, 11),
    
    -- Ticket 7: Authentification JWT
    (7, 1), (7, 5), (7, 10),
    
    -- Ticket 8: Export CSV
    (8, 2), (8, 11),
    
    -- Ticket 9: Bug statistiques
    (9, 1), (9, 7),
    
    -- Ticket 10: Configuration SSL
    (10, 5), (10, 13), (10, 15),
    
    -- Ticket 11: Système de notifications
    (11, 2), (11, 7), (11, 11),
    
    -- Ticket 12: Pagination
    (12, 1), (12, 7), (12, 11);

-- Tickets résolus (13-24)
INSERT INTO ticket_categorie (ticket_id, categorie_id) VALUES
    -- Ticket 13: Bug validation
    (13, 1), (13, 7),
    
    -- Ticket 14: Performance API
    (14, 6), (14, 11), (14, 8),
    
    -- Ticket 15: Système de logs
    (15, 13), (15, 14),
    
    -- Ticket 16: Faille XSS
    (16, 1), (16, 5), (16, 7),
    
    -- Ticket 17: Migration Spring Boot
    (17, 13), (17, 15), (17, 14),
    
    -- Ticket 18: Optimisation BDD
    (18, 6), (18, 8),
    
    -- Ticket 19: Encodage caractères
    (19, 1), (19, 7), (19, 13),
    
    -- Ticket 20: Tests unitaires
    (20, 14), (20, 13),
    
    -- Ticket 21: Configuration Docker
    (21, 13), (21, 15),
    
    -- Ticket 22: Pagination listes
    (22, 2), (22, 6), (22, 7),
    
    -- Ticket 23: Bug tri colonnes
    (23, 1), (23, 7),
    
    -- Ticket 24: Sauvegarde automatique
    (24, 8), (24, 13), (24, 15);

-- ===============================================
-- INFORMATIONS UTILES POUR LES TESTS
-- ===============================================
/*
🔐 COMPTES UTILISATEUR CRÉÉS :
Tous les comptes utilisent le mot de passe "password"

👑 ADMINISTRATEURS :
- admin / password (Administrateur principal)
- support / password (Support technique)
- tech_lead / password (Chef technique)

👤 UTILISATEURS STANDARDS :
- user / password (Utilisateur de test)
- alice / password (Utilisatrice)
- bob / password (Utilisateur)
- charlie / password (Utilisateur)
- diana / password (Utilisatrice)

⚡ PRIORITÉS DISPONIBLES :
1. Faible      - Pour les demandes non urgentes
2. Normale     - Pour les tickets standards
3. Élevée      - Pour les problèmes importants
4. Urgente     - Pour les problèmes critiques
5. Critique    - Pour les pannes bloquantes

🏷️ CATÉGORIES DISPONIBLES :
1. Bug                    9. Réseau
2. Fonctionnalité        10. Authentification
3. Support Technique     11. API
4. Documentation         12. Mobile
5. Sécurité             13. Configuration
6. Performance          14. Test
7. Interface Utilisateur 15. Déploiement
8. Base de Données

🎫 TICKETS CRÉÉS :
- 12 tickets ouverts (IDs 1-12) avec différentes priorités
- 12 tickets résolus (IDs 13-24) pour tester l'historique
- Variété de catégories associées à chaque ticket
- Différents utilisateurs soumetteurs et résolveurs
- Dates réalistes pour tester les filtres temporels
- Relations many-to-many entre tickets et catégories

🚀 POUR TESTER L'API :
1. Démarrez l'application Spring Boot
POUR TESTER L'API :
1. Connectez-vous avec admin/password ou user/password
2. Utilisez Postman avec la collection fournie
3. Testez les différents endpoints avec ces données
4. Testez les fonctionnalités CRUD et les filtres

📊 DONNÉES STATISTIQUES :
- 8 utilisateurs (3 admins, 5 utilisateurs standard)
- 5 niveaux de priorité
- 15 catégories thématiques
- 24 tickets au total (50% ouverts, 50% résolus)
- Données réparties sur janvier 2024

La base de données est maintenant prête pour tous vos tests ! 🎉
*/

-- ===============================================
-- RESET DES SÉQUENCES AUTO-INCREMENT H2
-- ===============================================
-- Nécessaire pour éviter les conflits d'IDs lors de la création de nouvelles entités

-- Reset de la séquence des utilisateurs (dernier ID: 8)
ALTER TABLE utilisateur ALTER COLUMN id RESTART WITH 9;

-- Reset de la séquence des priorités (dernier ID: 5)
ALTER TABLE priorite ALTER COLUMN id RESTART WITH 6;

-- Reset de la séquence des catégories (dernier ID: 15)
ALTER TABLE categorie ALTER COLUMN id RESTART WITH 16;

-- Reset de la séquence des tickets (dernier ID: 24)
ALTER TABLE ticket ALTER COLUMN id RESTART WITH 25;