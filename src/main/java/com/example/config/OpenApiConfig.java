package com.example.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration OpenAPI/Swagger pour l'API de gestion de tickets
 * 
 * Cette classe configure la documentation automatique de l'API avec Swagger UI.
 * Elle définit les informations générales, l'authentification JWT et les tags
 * pour organiser la documentation.
 */
@Configuration
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer",
    description = "Authentification JWT - Obtenez votre token via l'endpoint /api/auth/login"
)
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${spring.application.name:ticket-management}")
    private String applicationName;

    /**
     * Configuration principale de l'API OpenAPI
     * 
     * @return Configuration OpenAPI complète
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(createApiInfo())
                .servers(createServers())
                .tags(createTags());
    }

    /**
     * Crée les informations générales de l'API
     * 
     * @return Informations de l'API
     */
    private Info createApiInfo() {
        return new Info()
                .title("Ticket Management API")
                .version("1.0.0")
                .description("""
                    # API de Gestion de Tickets de Support
                    
                    Cette API REST permet la gestion complète de tickets de support avec authentification sécurisée.
                    
                    ## 🎯 Fonctionnalités principales
                    
                    ### 👤 Gestion des utilisateurs
                    - Authentification JWT sécurisée
                    - Gestion des rôles (Utilisateur/Administrateur)
                    - Inscription et connexion
                    
                    ### 🎫 Gestion des tickets
                    - Création de tickets par tous les utilisateurs
                    - Modification par le créateur ou un administrateur
                    - Résolution par les administrateurs uniquement
                    - Consultation publique des tickets non résolus
                    
                    ### 🏷️ Organisation
                    - Système de priorités configurables
                    - Catégories multiples par ticket
                    - Recherche et filtrage avancés
                    
                    ## 🔐 Authentification
                    
                    1. **Obtenez un token** via `POST /api/auth/login`
                    2. **Utilisez le token** en ajoutant l'en-tête : `Authorization: Bearer <votre-token>`
                    3. **Renouvelez si nécessaire** via `POST /api/auth/refresh`
                    
                    ## 👥 Niveaux d'accès
                    
                    | Rôle | Permissions |
                    |------|-------------|
                    | **Public** | Consultation des tickets non résolus, inscription |
                    | **Utilisateur** | Création/modification de ses tickets, consultation de tous les tickets |
                    | **Admin** | Toutes les actions + résolution de tickets + gestion des priorités/catégories |
                    
                    ## 📊 Codes de réponse
                    
                    - `200` ✅ Succès
                    - `201` ✅ Créé avec succès
                    - `400` ❌ Données invalides
                    - `401` 🔒 Non authentifié
                    - `403` 🚫 Accès refusé
                    - `404` 🔍 Ressource non trouvée
                    - `409` ⚠️ Conflit (ressource existe déjà)
                    
                    ## 🎯 Exemples d'utilisation
                    
                    ### Connexion
                    ```bash
                    curl -X POST /api/auth/login \\
                      -H "Content-Type: application/json" \\
                      -d '{"pseudo":"admin","password":"admin123"}'
                    ```
                    
                    ### Créer un ticket
                    ```bash
                    curl -X POST /api/tickets \\
                      -H "Authorization: Bearer <token>" \\
                      -d '{"titre":"Bug urgent","description":"...","prioriteId":1}'
                    ```
                    
                    💡 **Astuce** : Utilisez le bouton "Authorize" ci-dessous pour configurer votre token une seule fois.
                    """)
                .contact(createContact())
                .license(createLicense());
    }

    /**
     * Crée les informations de contact
     * 
     * @return Contact de l'équipe de développement
     */
    private Contact createContact() {
        return new Contact()
                .name("Équipe Ticket Management")
                .email("support@ticketmanagement.com")
                .url("https://github.com/votre-repo/ticket-management");
    }

    /**
     * Crée les informations de licence
     * 
     * @return Licence du projet
     */
    private License createLicense() {
        return new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
    }

    /**
     * Configure les serveurs disponibles
     * 
     * @return Liste des serveurs
     */
    private List<Server> createServers() {
        return List.of(
                new Server()
                        .url("http://localhost:" + serverPort + contextPath)
                        .description("Serveur de développement local"),
                new Server()
                        .url("https://api.ticketmanagement.com")
                        .description("Serveur de production"),
                new Server()
                        .url("https://staging-api.ticketmanagement.com")
                        .description("Serveur de staging")
        );
    }

    /**
     * Crée les tags pour organiser la documentation
     * 
     * @return Liste des tags
     */
    private List<Tag> createTags() {
        return List.of(
                new Tag()
                        .name("Authentification")
                        .description("🔐 Gestion de l'authentification des utilisateurs (connexion, inscription, tokens)"),
                new Tag()
                        .name("Tickets")
                        .description("🎫 Gestion complète des tickets (création, modification, résolution, consultation)"),
                new Tag()
                        .name("Priorités")
                        .description("📋 Gestion des priorités de tickets (configuration par les administrateurs)"),
                new Tag()
                        .name("Catégories")
                        .description("🏷️ Gestion des catégories de tickets (organisation et classification)"),
                new Tag()
                        .name("Utilisateurs")
                        .description("👥 Gestion des utilisateurs et des comptes (administration)")
        );
    }
}