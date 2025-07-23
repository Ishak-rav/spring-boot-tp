package com.example.controller;

import com.example.model.Priorite;
import com.example.service.PrioriteService;
import com.example.view.PrioriteBasicView;
import com.example.view.PrioriteDetailView;
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
@RequestMapping("/api/priorites")
@RequiredArgsConstructor
@Validated
@Tag(name = "Priorités", description = "API pour la gestion des priorités de tickets")
public class PrioriteController {

    private final PrioriteService prioriteService;

    /**
     * Récupère toutes les priorités
     */
    @GetMapping
    @JsonView(PrioriteBasicView.class)
    @Operation(summary = "Liste toutes les priorités", description = "Récupère la liste de toutes les priorités disponibles", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des priorités récupérée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<List<Priorite>> getAllPriorites() {
        try {
            log.info("Récupération de toutes les priorités");
            List<Priorite> priorites = prioriteService.getAllPriorites();
            return ResponseEntity.ok(priorites);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des priorités: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère une priorité par son ID
     */
    @GetMapping("/{id}")
    @JsonView(PrioriteDetailView.class)
    @Operation(summary = "Récupère une priorité par ID", description = "Récupère les détails d'une priorité spécifique", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Priorité trouvée"),
            @ApiResponse(responseCode = "404", description = "Priorité non trouvée"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<?> getPrioriteById(
            @Parameter(description = "ID de la priorité", required = true) @PathVariable @Min(1) Integer id) {
        try {
            Optional<Priorite> prioriteOpt = prioriteService.getPrioriteById(id);

            if (prioriteOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Priorité non trouvée");
                error.put("message", "Aucune priorité trouvée avec l'ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            return ResponseEntity.ok(prioriteOpt.get());
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la priorité {}: ", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur interne du serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Crée une nouvelle priorité (admin seulement)
     */
    @PostMapping
    @JsonView(PrioriteDetailView.class)
    @Operation(summary = "Crée une nouvelle priorité", description = "Crée une nouvelle priorité. Accessible uniquement aux administrateurs", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Priorité créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Droits administrateur requis"),
            @ApiResponse(responseCode = "409", description = "Une priorité avec ce nom existe déjà")
    })
    public ResponseEntity<?> createPriorite(
            @Parameter(description = "Nom de la priorité", required = true) @RequestParam @NotBlank(message = "Le nom est obligatoire") @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères") String nom) {
        try {
            log.info("Création d'une nouvelle priorité: {}", nom);

            Priorite priorite = prioriteService.createPriorite(nom);

            log.info("Priorité créée avec succès - ID: {}, Nom: {}", priorite.getId(), priorite.getNom());
            return ResponseEntity.status(HttpStatus.CREATED).body(priorite);

        } catch (RuntimeException e) {
            log.warn("Erreur lors de la création de la priorité '{}': {}", nom, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Création échouée");
            error.put("message", e.getMessage());

            HttpStatus status = e.getMessage().contains("existe déjà") ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status).body(error);
        } catch (Exception e) {
            log.error("Erreur lors de la création de la priorité: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur interne du serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Met à jour une priorité existante (admin seulement)
     */
    @PutMapping("/{id}")
    @JsonView(PrioriteDetailView.class)
    @Operation(summary = "Met à jour une priorité", description = "Met à jour une priorité existante. Accessible uniquement aux administrateurs", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Priorité mise à jour avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Droits administrateur requis"),
            @ApiResponse(responseCode = "404", description = "Priorité non trouvée"),
            @ApiResponse(responseCode = "409", description = "Une priorité avec ce nom existe déjà")
    })
    public ResponseEntity<?> updatePriorite(
            @Parameter(description = "ID de la priorité", required = true) @PathVariable @Min(1) Integer id,
            @Parameter(description = "Nouveau nom de la priorité", required = true) @RequestParam @NotBlank(message = "Le nom est obligatoire") @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères") String nom) {
        try {
            log.info("Mise à jour de la priorité {} avec le nom: {}", id, nom);

            Priorite priorite = prioriteService.updatePriorite(id, nom);

            log.info("Priorité {} mise à jour avec succès", id);
            return ResponseEntity.ok(priorite);

        } catch (RuntimeException e) {
            log.warn("Erreur lors de la mise à jour de la priorité {}: {}", id, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Mise à jour échouée");
            error.put("message", e.getMessage());

            HttpStatus status = e.getMessage().contains("non trouvée") ? HttpStatus.NOT_FOUND
                    : e.getMessage().contains("existe déjà") ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status).body(error);
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de la priorité {}: ", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur interne du serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Supprime une priorité (admin seulement)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime une priorité", description = "Supprime définitivement une priorité. Accessible uniquement aux administrateurs. Une priorité ne peut être supprimée si elle est utilisée par des tickets", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Priorité supprimée avec succès"),
            @ApiResponse(responseCode = "400", description = "Priorité utilisée par des tickets"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Droits administrateur requis"),
            @ApiResponse(responseCode = "404", description = "Priorité non trouvée")
    })
    public ResponseEntity<?> deletePriorite(
            @Parameter(description = "ID de la priorité", required = true) @PathVariable @Min(1) Integer id) {
        try {
            log.info("Suppression de la priorité {}", id);

            prioriteService.deletePriorite(id);

            log.info("Priorité {} supprimée avec succès", id);
            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            log.warn("Erreur lors de la suppression de la priorité {}: {}", id, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Suppression échouée");
            error.put("message", e.getMessage());

            HttpStatus status = e.getMessage().contains("non trouvée") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status).body(error);
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de la priorité {}: ", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur interne du serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Récupère les priorités avec leurs statistiques d'utilisation
     */
    @GetMapping("/stats")
    @Operation(summary = "Statistiques des priorités", description = "Récupère les priorités avec le nombre de tickets associés", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistiques récupérées"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<List<Object[]>> getPrioriteStats() {
        try {
            log.info("Récupération des statistiques des priorités");
            List<Object[]> stats = prioriteService.getPrioriteStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques des priorités: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Recherche des priorités par nom
     */
    @GetMapping("/search")
    @JsonView(PrioriteBasicView.class)
    @Operation(summary = "Recherche des priorités", description = "Recherche des priorités par nom (recherche partielle insensible à la casse)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Résultats de recherche récupérés"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<List<Priorite>> searchPriorites(
            @Parameter(description = "Terme de recherche", required = false) @RequestParam(required = false) String keyword) {
        try {
            log.info("Recherche de priorités avec le terme: {}", keyword);
            List<Priorite> priorites = prioriteService.searchPriorites(keyword);
            return ResponseEntity.ok(priorites);
        } catch (Exception e) {
            log.error("Erreur lors de la recherche de priorités: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les priorités ayant des tickets non résolus
     */
    @GetMapping("/with-unresolved-tickets")
    @JsonView(PrioriteBasicView.class)
    @Operation(summary = "Priorités avec tickets non résolus", description = "Récupère les priorités qui ont des tickets non résolus", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<List<Priorite>> getPrioritesWithUnresolvedTickets() {
        try {
            log.info("Récupération des priorités avec tickets non résolus");
            List<Priorite> priorites = prioriteService.getPrioritesWithUnresolvedTickets();
            return ResponseEntity.ok(priorites);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des priorités avec tickets non résolus: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}