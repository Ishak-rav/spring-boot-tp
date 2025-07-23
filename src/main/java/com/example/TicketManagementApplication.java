package com.example;

import com.example.service.AuthService;
import com.example.service.PrioriteService;
import com.example.service.CategorieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;

/**
 * Application principale pour le système de gestion de tickets
 * 
 * Cette application Spring Boot fournit une API REST pour la gestion de tickets
 * de support
 * avec authentification JWT et contrôle d'accès basé sur les rôles.
 * 
 * Fonctionnalités principales :
 * - Authentification utilisateur avec JWT
 * - Gestion des tickets (création, modification, résolution)
 * - Gestion des priorités et catégories
 * - Contrôle d'accès admin/utilisateur
 * - API documentée avec Swagger
 * 
 * @author Ticket Management Team
 * @version 1.0.0
 * @since 2024
 */
@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
@OpenAPIDefinition(info = @Info(title = "Ticket Management API", version = "1.0.0", description = """
        API de gestion de tickets de support permettant :

        **Fonctionnalités principales :**
        - Authentification et autorisation des utilisateurs
        - Création et gestion de tickets par tous les utilisateurs connectés
        - Résolution de tickets par les administrateurs
        - Consultation publique des tickets non résolus
        - Gestion des priorités et catégories

        **Rôles et permissions :**
        - **Utilisateur non connecté** : Peut voir les tickets non résolus et créer un compte
        - **Utilisateur connecté** : Peut créer, modifier ses tickets et voir tous les tickets
        - **Administrateur** : Peut tout faire + résoudre/rouvrir les tickets + gérer priorités/catégories

        **Authentification :**
        Utilisez l'endpoint `/api/auth/login` pour obtenir un token JWT, puis ajoutez-le dans l'en-tête :
        `Authorization: Bearer <votre-token>`
        """, contact = @Contact(name = "Équipe de développement", email = "support@ticketmanagement.com"), license = @License(name = "MIT License", url = "https://opensource.org/licenses/MIT")))
@SecuritySchemes({
        @SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT", description = "Token JWT pour l'authentification. Obtenez-le via /api/auth/login")
})
public class TicketManagementApplication {

    /**
     * Point d'entrée principal de l'application
     * 
     * @param args arguments de la ligne de commande
     */
    public static void main(String[] args) {
        try {
            log.info("=".repeat(60));
            log.info("🚀 Démarrage de l'application Ticket Management");
            log.info("=".repeat(60));

            SpringApplication.run(TicketManagementApplication.class, args);

            log.info("=".repeat(60));
            log.info("✅ Application Ticket Management démarrée avec succès !");
            log.info("📖 Documentation API disponible : http://localhost:8080/swagger-ui.html");
            log.info("🔧 Console H2 (dev) : http://localhost:8080/h2-console");
            log.info("=".repeat(60));

        } catch (Exception e) {
            log.error("❌ Erreur lors du démarrage de l'application", e);
            System.exit(1);
        }
    }

    /**
     * Initialisation des données de base au démarrage de l'application
     * 
     * Cette méthode s'exécute automatiquement après le démarrage et initialise :
     * - Les priorités par défaut (Faible, Normale, Haute, Critique, Urgente)
     * - Les catégories par défaut (Bug, Amélioration, etc.)
     * - Un utilisateur administrateur par défaut
     * 
     * @param prioriteService  service de gestion des priorités
     * @param categorieService service de gestion des catégories
     * @param authService      service d'authentification
     * @return CommandLineRunner pour l'initialisation
     */
    @Bean
    public CommandLineRunner initializeData(
            PrioriteService prioriteService,
            CategorieService categorieService,
            AuthService authService) {

        return args -> {
            try {
                log.info("🔧 Initialisation des données de base...");

                log.info("📋 Initialisation des priorités par défaut...");
                prioriteService.initializeDefaultPriorites();
                long prioriteCount = prioriteService.getTotalPrioriteCount();
                log.info("✅ {} priorité(s) disponible(s)", prioriteCount);

                log.info("🏷️  Initialisation des catégories par défaut...");
                categorieService.initializeDefaultCategories();
                long categorieCount = categorieService.getTotalCategorieCount();
                log.info("✅ {} catégorie(s) disponible(s)", categorieCount);

                log.info("👤 Création de l'utilisateur administrateur par défaut...");
                try {
                    authService.createUser("admin", "admin123", true);
                    log.info("✅ Utilisateur admin créé (pseudo: admin, mot de passe: admin123)");
                    log.warn("⚠️  ATTENTION: Changez le mot de passe de l'admin en production !");
                } catch (RuntimeException e) {
                    if (e.getMessage().contains("existe déjà")) {
                        log.info("ℹ️  Utilisateur admin existe déjà");
                    } else {
                        log.error("❌ Erreur lors de la création de l'admin: {}", e.getMessage());
                    }
                }

                log.info("👤 Création d'un utilisateur de test...");
                try {
                    authService.createUser("user", "user123", false);
                    log.info("✅ Utilisateur de test créé (pseudo: user, mot de passe: user123)");
                } catch (RuntimeException e) {
                    if (e.getMessage().contains("existe déjà")) {
                        log.info("ℹ️  Utilisateur de test existe déjà");
                    } else {
                        log.error("❌ Erreur lors de la création de l'utilisateur de test: {}", e.getMessage());
                    }
                }

                log.info("🎉 Initialisation des données terminée avec succès !");

                displayUsefulInformation();

            } catch (Exception e) {
                log.error("❌ Erreur lors de l'initialisation des données", e);
            }
        };
    }

    /**
     * Affiche des informations utiles pour les développeurs
     */
    private void displayUsefulInformation() {
        log.info("");
        log.info("📚 Informations utiles :");
        log.info("━".repeat(50));
        log.info("🔗 API Documentation : http://localhost:8080/swagger-ui.html");
        log.info("🗄️  Base de données H2 : http://localhost:8080/h2-console");
        log.info("   └─ JDBC URL: jdbc:h2:mem:ticketdb");
        log.info("   └─ Username: sa");
        log.info("   └─ Password: (vide)");
        log.info("");
        log.info("👥 Comptes de test disponibles :");
        log.info("   🔧 Admin    : pseudo=admin, password=admin123");
        log.info("   👤 User     : pseudo=user,  password=user123");
        log.info("");
        log.info("🌐 Endpoints principaux :");
        log.info("   🔐 Login          : POST /api/auth/login");
        log.info("   📝 Créer compte   : POST /api/auth/register");
        log.info("   🎫 Tickets publics: GET  /api/tickets/unresolved");
        log.info("   🎫 Tous tickets  : GET  /api/tickets (auth requis)");
        log.info("   ➕ Créer ticket  : POST /api/tickets (auth requis)");
        log.info("   ✅ Résoudre      : PUT  /api/tickets/{id}/resolve (admin)");
        log.info("━".repeat(50));
        log.info("");
    }
}