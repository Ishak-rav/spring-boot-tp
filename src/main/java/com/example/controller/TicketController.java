package com.example.controller;

import com.example.dto.CreateTicketDto;
import com.example.filter.JwtAuthenticationFilter;
import com.example.model.Ticket;
import com.example.service.TicketService;
import com.example.view.TicketBasicView;
import com.example.view.TicketDetailView;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Contrôleur pour la gestion des tickets
 */
@Slf4j
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Validated
@Tag(name = "Tickets", description = "API pour la gestion des tickets de support")
public class TicketController {

    private final TicketService ticketService;

    /**
     * Récupère tous les tickets (pour utilisateurs connectés)
     */
    @GetMapping
    @JsonView(TicketBasicView.class)
    @Operation(summary = "Liste tous les tickets", description = "Récupère la liste de tous les tickets. Accessible aux utilisateurs connectés (admin ou non-admin)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des tickets récupérée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<List<Ticket>> getAllTickets() {
        try {
            log.info("Récupération de tous les tickets");
            List<Ticket> tickets = ticketService.getAllTickets();
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des tickets: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère tous les tickets non résolus (accessible à tous, même non connectés)
     */
    @GetMapping("/unresolved")
    @JsonView(TicketBasicView.class)
    @Operation(summary = "Liste les tickets non résolus", description = "Récupère la liste des tickets non résolus. Accessible à tous, même aux utilisateurs non connectés")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des tickets non résolus récupérée avec succès")
    })
    public ResponseEntity<List<Ticket>> getUnresolvedTickets() {
        try {
            log.info("Récupération des tickets non résolus (accès public)");
            List<Ticket> tickets = ticketService.getUnresolvedTickets();
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des tickets non résolus: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Alias pour l'endpoint public des tickets non résolus
     */
    @GetMapping("/public")
    @JsonView(TicketBasicView.class)
    @Operation(summary = "Liste publique des tickets non résolus", description = "Alias pour /unresolved - Récupère la liste des tickets non résolus accessible publiquement")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des tickets non résolus récupérée avec succès")
    })
    public ResponseEntity<List<Ticket>> getPublicTickets() {
        return getUnresolvedTickets();
    }

    /**
     * Récupère un ticket par son ID
     */
    @GetMapping("/{id}")
    @JsonView(TicketDetailView.class)
    @Operation(summary = "Récupère un ticket par ID", description = "Récupère les détails d'un ticket spécifique", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket trouvé"),
            @ApiResponse(responseCode = "404", description = "Ticket non trouvé"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    public ResponseEntity<?> getTicketById(
            @Parameter(description = "ID du ticket", required = true) @PathVariable @Min(1) Integer id) {
        try {
            Optional<Ticket> ticketOpt = ticketService.getTicketById(id);

            if (ticketOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Ticket non trouvé");
                error.put("message", "Aucun ticket trouvé avec l'ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Integer userId = getCurrentUserId();
            boolean isAdmin = getCurrentUserIsAdmin();

            if (!ticketService.canAccessTicket(id, userId, isAdmin)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Accès refusé");
                error.put("message", "Vous n'avez pas les droits pour accéder à ce ticket");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            return ResponseEntity.ok(ticketOpt.get());
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du ticket {}: ", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur interne du serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Crée un nouveau ticket
     */
    @PostMapping
    @JsonView(TicketDetailView.class)
    @Operation(summary = "Crée un nouveau ticket", description = "Crée un nouveau ticket. Accessible aux utilisateurs connectés (admin ou non-admin)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ticket créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<?> createTicket(@Valid @RequestBody CreateTicketDto createTicketDto) {
        try {
            Integer userId = getCurrentUserId();
            log.info("Création d'un nouveau ticket par l'utilisateur {}", userId);

            Ticket ticket = ticketService.createTicket(createTicketDto, userId);

            log.info("Ticket créé avec succès - ID: {}", ticket.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(ticket);

        } catch (RuntimeException e) {
            log.warn("Erreur lors de la création du ticket: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Création échouée");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            log.error("Erreur lors de la création du ticket: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur interne du serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Met à jour un ticket existant
     */
    @PutMapping("/{id}")
    @JsonView(TicketDetailView.class)
    @Operation(summary = "Met à jour un ticket", description = "Met à jour un ticket existant. Seul le soumetteur ou un admin peut modifier un ticket", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket mis à jour avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Ticket non trouvé")
    })
    public ResponseEntity<?> updateTicket(
            @Parameter(description = "ID du ticket", required = true) @PathVariable @Min(1) Integer id,
            @Valid @RequestBody CreateTicketDto updateDto) {
        try {
            Integer userId = getCurrentUserId();
            log.info("Mise à jour du ticket {} par l'utilisateur {}", id, userId);

            Ticket ticket = ticketService.updateTicket(id, updateDto, userId);

            log.info("Ticket {} mis à jour avec succès", id);
            return ResponseEntity.ok(ticket);

        } catch (RuntimeException e) {
            log.warn("Erreur lors de la mise à jour du ticket {}: {}", id, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Mise à jour échouée");
            error.put("message", e.getMessage());

            HttpStatus status = e.getMessage().contains("non trouvé") ? HttpStatus.NOT_FOUND
                    : e.getMessage().contains("droit") ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status).body(error);
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du ticket {}: ", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur interne du serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Marque un ticket comme résolu (admin seulement)
     */
    @PutMapping("/{id}/resolve")
    @JsonView(TicketDetailView.class)
    @Operation(summary = "Marque un ticket comme résolu", description = "Marque un ticket comme résolu. Accessible uniquement aux administrateurs", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket marqué comme résolu"),
            @ApiResponse(responseCode = "400", description = "Ticket déjà résolu"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Droits administrateur requis"),
            @ApiResponse(responseCode = "404", description = "Ticket non trouvé")
    })
    public ResponseEntity<?> resolveTicket(
            @Parameter(description = "ID du ticket", required = true) @PathVariable @Min(1) Integer id) {
        try {
            Integer userId = getCurrentUserId();
            log.info("Résolution du ticket {} par l'admin {}", id, userId);

            Ticket ticket = ticketService.resolveTicket(id, userId);

            log.info("Ticket {} marqué comme résolu par l'admin {}", id, userId);
            return ResponseEntity.ok(ticket);

        } catch (RuntimeException e) {
            log.warn("Erreur lors de la résolution du ticket {}: {}", id, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Résolution échouée");
            error.put("message", e.getMessage());

            HttpStatus status = e.getMessage().contains("non trouvé") ? HttpStatus.NOT_FOUND
                    : e.getMessage().contains("administrateur") ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status).body(error);
        } catch (Exception e) {
            log.error("Erreur lors de la résolution du ticket {}: ", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur interne du serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Rouvre un ticket résolu (admin seulement)
     */
    @PutMapping("/{id}/reopen")
    @JsonView(TicketDetailView.class)
    @Operation(summary = "Rouvre un ticket résolu", description = "Rouvre un ticket précédemment résolu. Accessible uniquement aux administrateurs", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket rouvert avec succès"),
            @ApiResponse(responseCode = "400", description = "Ticket non résolu"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Droits administrateur requis"),
            @ApiResponse(responseCode = "404", description = "Ticket non trouvé")
    })
    public ResponseEntity<?> reopenTicket(
            @Parameter(description = "ID du ticket", required = true) @PathVariable @Min(1) Integer id) {
        try {
            Integer userId = getCurrentUserId();
            log.info("Réouverture du ticket {} par l'admin {}", id, userId);

            Ticket ticket = ticketService.reopenTicket(id, userId);

            log.info("Ticket {} rouvert par l'admin {}", id, userId);
            return ResponseEntity.ok(ticket);

        } catch (RuntimeException e) {
            log.warn("Erreur lors de la réouverture du ticket {}: {}", id, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Réouverture échouée");
            error.put("message", e.getMessage());

            HttpStatus status = e.getMessage().contains("non trouvé") ? HttpStatus.NOT_FOUND
                    : e.getMessage().contains("administrateur") ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status).body(error);
        } catch (Exception e) {
            log.error("Erreur lors de la réouverture du ticket {}: ", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur interne du serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Supprime un ticket (admin seulement)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime un ticket", description = "Supprime définitivement un ticket. Accessible uniquement aux administrateurs", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Ticket supprimé avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Droits administrateur requis"),
            @ApiResponse(responseCode = "404", description = "Ticket non trouvé")
    })
    public ResponseEntity<?> deleteTicket(
            @Parameter(description = "ID du ticket", required = true) @PathVariable @Min(1) Integer id) {
        try {
            Integer userId = getCurrentUserId();
            log.info("Suppression du ticket {} par l'admin {}", id, userId);

            ticketService.deleteTicket(id, userId);

            log.info("Ticket {} supprimé par l'admin {}", id, userId);
            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            log.warn("Erreur lors de la suppression du ticket {}: {}", id, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Suppression échouée");
            error.put("message", e.getMessage());

            HttpStatus status = e.getMessage().contains("non trouvé") ? HttpStatus.NOT_FOUND
                    : e.getMessage().contains("administrateur") ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status).body(error);
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du ticket {}: ", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur interne du serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Récupère les tickets d'un utilisateur spécifique
     */
    @GetMapping("/user/{userId}")
    @JsonView(TicketBasicView.class)
    @Operation(summary = "Récupère les tickets d'un utilisateur", description = "Récupère tous les tickets soumis par un utilisateur spécifique", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des tickets récupérée"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    public ResponseEntity<?> getTicketsByUser(
            @Parameter(description = "ID de l'utilisateur", required = true) @PathVariable @Min(1) Integer userId) {
        try {
            Integer currentUserId = getCurrentUserId();
            boolean isAdmin = getCurrentUserIsAdmin();

            if (!isAdmin && !currentUserId.equals(userId)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Accès refusé");
                error.put("message", "Vous ne pouvez voir que vos propres tickets");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            log.info("Récupération des tickets de l'utilisateur {} par {}", userId, currentUserId);
            List<Ticket> tickets = ticketService.getTicketsByUser(userId);

            return ResponseEntity.ok(tickets);
        } catch (RuntimeException e) {
            log.warn("Erreur lors de la récupération des tickets de l'utilisateur {}: {}", userId, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Récupération échouée");
            error.put("message", e.getMessage());

            HttpStatus status = e.getMessage().contains("non trouvé") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(error);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des tickets de l'utilisateur {}: ", userId, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur interne du serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Recherche des tickets par mot-clé
     */
    @GetMapping("/search")
    @JsonView(TicketBasicView.class)
    @Operation(summary = "Recherche des tickets", description = "Recherche des tickets par mot-clé dans le titre ou la description", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Résultats de recherche récupérés"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<List<Ticket>> searchTickets(
            @Parameter(description = "Mot-clé de recherche", required = false) @RequestParam(required = false) String keyword) {
        try {
            log.info("Recherche de tickets avec le mot-clé: {}", keyword);
            List<Ticket> tickets = ticketService.searchTickets(keyword);
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            log.error("Erreur lors de la recherche de tickets: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les statistiques des tickets
     */
    @GetMapping("/stats")
    @Operation(summary = "Statistiques des tickets", description = "Récupère les statistiques générales des tickets", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistiques récupérées"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<Map<String, Object>> getTicketStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", ticketService.getTotalTicketCount());
            stats.put("unresolved", ticketService.getUnresolvedTicketCount());
            stats.put("resolved", ticketService.getResolvedTicketCount());
            stats.put("byPriorite", ticketService.getTicketStatsByPriorite());

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Méthodes utilitaires pour récupérer les informations de l'utilisateur
     * connecté
     */
    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.getDetails() instanceof JwtAuthenticationFilter.JwtAuthenticationDetails) {
            JwtAuthenticationFilter.JwtAuthenticationDetails details = (JwtAuthenticationFilter.JwtAuthenticationDetails) authentication
                    .getDetails();
            return details.getUserId();
        }
        return null;
    }

    private boolean getCurrentUserIsAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.getDetails() instanceof JwtAuthenticationFilter.JwtAuthenticationDetails) {
            JwtAuthenticationFilter.JwtAuthenticationDetails details = (JwtAuthenticationFilter.JwtAuthenticationDetails) authentication
                    .getDetails();
            return Boolean.TRUE.equals(details.getIsAdmin());
        }
        return false;
    }
}