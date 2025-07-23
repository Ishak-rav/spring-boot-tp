package com.example.controller;

import com.example.model.Categorie;
import com.example.service.CategorieService;
import com.example.view.CategorieBasicView;
import com.example.view.CategorieDetailView;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Validated
@Tag(name = "Catégories", description = "API pour la gestion des catégories de tickets")
public class CategorieController {

    private final CategorieService categorieService;

    /**
     * Récupère toutes les catégories
     */
    @GetMapping
    @JsonView(CategorieBasicView.class)
    @Operation(summary = "Liste toutes les catégories", description = "Récupère la liste de toutes les catégories disponibles", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des catégories récupérée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<List<Categorie>> getAllCategories() {
        try {
            log.info("Récupération de toutes les catégories");
            List<Categorie> categories = categorieService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des catégories: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère une catégorie par son ID
     */
    @GetMapping("/{id}")
    @JsonView(CategorieDetailView.class)
    @Operation(summary = "Récupère une catégorie par ID", description = "Récupère les détails d'une catégorie spécifique", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Catégorie trouvée"),
            @ApiResponse(responseCode = "404", description = "Catégorie non trouvée"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<?> getCategorieById(
            @Parameter(description = "ID de la catégorie", required = true) @PathVariable @Min(1) Integer id) {
        try {
            Optional<Categorie> categorieOpt = categorieService.getCategorieById(id);

            if (categorieOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Catégorie non trouvée");
                error.put("message", "Aucune catégorie trouvée avec l'ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            return ResponseEntity.ok(categorieOpt.get());
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la catégorie {}: ", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur interne du serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Crée une nouvelle catégorie (admin seulement)
     */
    @PostMapping
    @JsonView(CategorieDetailView.class)
    @Operation(summary = "Crée une nouvelle catégorie", description = "Crée une nouvelle catégorie. Accessible uniquement aux administrateurs", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Catégorie créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Droits administrateur requis"),
            @ApiResponse(responseCode = "409", description = "Une catégorie avec ce nom existe déjà")
    })
    public ResponseEntity<?> createCategorie(
            @Parameter(description = "Nom de la catégorie", required = true) @RequestParam @NotBlank(message = "Le nom est obligatoire") @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères") String nom) {
        try {
            log.info("Création d'une nouvelle catégorie: {}", nom);

            Categorie categorie = categorieService.createCategorie(nom);

            log.info("Catégorie créée avec succès - ID: {}, Nom: {}", categorie.getId(), categorie.getNom());
            return ResponseEntity.status(HttpStatus.CREATED).body(categorie);

        } catch (RuntimeException e) {
            log.warn("Erreur lors de la création de la catégorie '{}': {}", nom, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Création échouée");
            error.put("message", e.getMessage());

            HttpStatus status = e.getMessage().contains("existe déjà") ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status).body(error);
        } catch (Exception e) {
            log.error("Erreur lors de la création de la catégorie: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur interne du serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Met à jour une catégorie existante (admin seulement)
     */
    @PutMapping("/{id}")
    @JsonView(CategorieDetailView.class)
    @Operation(summary = "Met à jour une catégorie", description = "Met à jour une catégorie existante. Accessible uniquement aux administrateurs", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Catégorie mise à jour avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Droits administrateur requis"),
            @ApiResponse(responseCode = "404", description = "Catégorie non trouvée"),
            @ApiResponse(responseCode = "409", description = "Une catégorie avec ce nom existe déjà")
    })
    public ResponseEntity<?> updateCategorie(
            @Parameter(description = "ID de la catégorie", required = true) @PathVariable @Min(1) Integer id,
            @Parameter(description = "Nouveau nom de la catégorie", required = true) @RequestParam @NotBlank(message = "Le nom est obligatoire") @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères") String nom) {
        try {
            log.info("Mise à jour de la catégorie {} avec le nom: {}", id, nom);

            Categorie categorie = categorieService.updateCategorie(id, nom);

            log.info("Catégorie {} mise à jour avec succès", id);
            return ResponseEntity.ok(categorie);

        } catch (RuntimeException e) {
            log.warn("Erreur lors de la mise à jour de la catégorie {}: {}", id, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Mise à jour échouée");
            error.put("message", e.getMessage());

            HttpStatus status = e.getMessage().contains("non trouvée") ? HttpStatus.NOT_FOUND
                    : e.getMessage().contains("existe déjà") ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status).body(error);
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de la catégorie {}: ", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur interne du serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Supprime une catégorie (admin seulement)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime une catégorie", description = "Supprime définitivement une catégorie. Accessible uniquement aux administrateurs. Une catégorie ne peut être supprimée si elle est utilisée par des tickets", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Catégorie supprimée avec succès"),
            @ApiResponse(responseCode = "400", description = "Catégorie utilisée par des tickets"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Droits administrateur requis"),
            @ApiResponse(responseCode = "404", description = "Catégorie non trouvée")
    })
    public ResponseEntity<?> deleteCategorie(
            @Parameter(description = "ID de la catégorie", required = true) @PathVariable @Min(1) Integer id) {
        try {
            log.info("Suppression de la catégorie {}", id);

            categorieService.deleteCategorie(id);

            log.info("Catégorie {} supprimée avec succès", id);
            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            log.warn("Erreur lors de la suppression de la catégorie {}: {}", id, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Suppression échouée");
            error.put("message", e.getMessage());

            HttpStatus status = e.getMessage().contains("non trouvée") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status).body(error);
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de la catégorie {}: ", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur interne du serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Récupère les catégories avec leurs statistiques d'utilisation
     */
    @GetMapping("/stats")
    @Operation(summary = "Statistiques des catégories", description = "Récupère les catégories avec le nombre de tickets associés", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistiques récupérées"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<List<Object[]>> getCategorieStats() {
        try {
            log.info("Récupération des statistiques des catégories");
            List<Object[]> stats = categorieService.getCategorieStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques des catégories: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Recherche des catégories par nom
     */
    @GetMapping("/search")
    @JsonView(CategorieBasicView.class)
    @Operation(summary = "Recherche des catégories", description = "Recherche des catégories par nom (recherche partielle insensible à la casse)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Résultats de recherche récupérés"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<List<Categorie>> searchCategories(
            @Parameter(description = "Terme de recherche", required = false) @RequestParam(required = false) String keyword) {
        try {
            log.info("Recherche de catégories avec le terme: {}", keyword);
            List<Categorie> categories = categorieService.searchCategories(keyword);
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            log.error("Erreur lors de la recherche de catégories: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les catégories ayant des tickets non résolus
     */
    @GetMapping("/with-unresolved-tickets")
    @JsonView(CategorieBasicView.class)
    @Operation(summary = "Catégories avec tickets non résolus", description = "Récupère les catégories qui ont des tickets non résolus", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<List<Categorie>> getCategoriesWithUnresolvedTickets() {
        try {
            log.info("Récupération des catégories avec tickets non résolus");
            List<Categorie> categories = categorieService.getCategoriesWithUnresolvedTickets();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des catégories avec tickets non résolus: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les catégories populaires
     */
    @GetMapping("/popular")
    @JsonView(CategorieBasicView.class)
    @Operation(summary = "Catégories populaires", description = "Récupère les catégories les plus utilisées (avec au moins un certain nombre de tickets)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<List<Categorie>> getPopularCategories(
            @Parameter(description = "Nombre minimum de tickets", required = false) @RequestParam(defaultValue = "1") @Min(1) int minTickets) {
        try {
            log.info("Récupération des catégories populaires (min {} tickets)", minTickets);
            List<Categorie> categories = categorieService.getPopularCategories(minTickets);
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des catégories populaires: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les catégories triées par popularité
     */
    @GetMapping("/by-popularity")
    @JsonView(CategorieBasicView.class)
    @Operation(summary = "Catégories triées par popularité", description = "Récupère toutes les catégories triées par nombre de tickets décroissant", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<List<Categorie>> getCategoriesByPopularity() {
        try {
            log.info("Récupération des catégories triées par popularité");
            List<Categorie> categories = categorieService.getCategoriesByPopularity();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des catégories par popularité: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les catégories utilisées par un utilisateur
     */
    @GetMapping("/user/{userId}")
    @JsonView(CategorieBasicView.class)
    @Operation(summary = "Catégories utilisées par un utilisateur", description = "Récupère les catégories utilisées dans les tickets soumis par un utilisateur spécifique", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    public ResponseEntity<List<Categorie>> getCategoriesUsedByUser(
            @Parameter(description = "ID de l'utilisateur", required = true) @PathVariable @Min(1) Integer userId) {
        try {
            log.info("Récupération des catégories utilisées par l'utilisateur {}", userId);
            List<Categorie> categories = categorieService.getCategoriesUsedByUser(userId);
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des catégories de l'utilisateur {}: ", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}