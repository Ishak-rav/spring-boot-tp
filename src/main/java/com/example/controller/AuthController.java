package com.example.controller;

import com.example.dto.AuthResponseDto;
import com.example.dto.LoginRequestDto;
import com.example.model.Utilisateur;
import com.example.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "Authentification", description = "API pour la gestion de l'authentification des utilisateurs")
public class AuthController {

    private final AuthService authService;

    /**
     * Connexion d'un utilisateur
     * @param loginRequest les données de connexion (pseudo et mot de passe)
     * @return le token JWT et les informations de l'utilisateur
     */
    @PostMapping("/login")
    @Operation(
        summary = "Connexion utilisateur",
        description = "Authentifie un utilisateur avec son pseudo et mot de passe et retourne un token JWT"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Connexion réussie",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Données de connexion invalides",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Identifiants incorrects",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        try {
            log.info("Tentative de connexion pour l'utilisateur: {}", loginRequest.getPseudo());
            
            AuthResponseDto response = authService.login(loginRequest);
            
            log.info("Connexion réussie pour l'utilisateur: {}", loginRequest.getPseudo());
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.warn("Échec de connexion pour l'utilisateur {}: {}", loginRequest.getPseudo(), e.getMessage());
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentification échouée");
            error.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            log.error("Erreur lors de la connexion: ", e);
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur interne du serveur");
            error.put("message", "Une erreur inattendue s'est produite");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Enregistrement d'un nouvel utilisateur
     * @param pseudo le pseudo de l'utilisateur
     * @param password le mot de passe
     * @param admin si l'utilisateur doit être administrateur (optionnel, défaut: false)
     * @return les informations de l'utilisateur créé avec son token
     */
    @PostMapping("/register")
    @Operation(
        summary = "Enregistrement d'un nouvel utilisateur",
        description = "Crée un nouveau compte utilisateur et retourne un token JWT"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Utilisateur créé avec succès",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Données d'enregistrement invalides",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Un utilisateur avec ce pseudo existe déjà",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<?> register(
            @Parameter(description = "Pseudo de l'utilisateur", required = true)
            @RequestParam @NotBlank(message = "Le pseudo est obligatoire") 
            @Size(min = 3, max = 50, message = "Le pseudo doit contenir entre 3 et 50 caractères") String pseudo,
            
            @Parameter(description = "Mot de passe de l'utilisateur", required = true)
            @RequestParam @NotBlank(message = "Le mot de passe est obligatoire") 
            @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères") String password,
            
            @Parameter(description = "Si l'utilisateur doit être administrateur", required = false)
            @RequestParam(defaultValue = "false") Boolean admin) {
        
        try {
            log.info("Tentative d'enregistrement pour l'utilisateur: {}", pseudo);
            
            Utilisateur utilisateur = authService.createUser(pseudo, password, admin);
            String token = authService.generateToken(utilisateur);
            
            AuthResponseDto response = new AuthResponseDto(
                token, 
                utilisateur.getPseudo(), 
                utilisateur.isAdmin(),
                "Compte créé avec succès"
            );
            
            log.info("Utilisateur créé avec succès: {} (Admin: {})", pseudo, admin);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (RuntimeException e) {
            log.warn("Échec de création de compte pour {}: {}", pseudo, e.getMessage());
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Création de compte échouée");
            error.put("message", e.getMessage());
            
            HttpStatus status = e.getMessage().contains("existe déjà") ? 
                HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
            
            return ResponseEntity.status(status).body(error);
        } catch (Exception e) {
            log.error("Erreur lors de la création de compte: ", e);
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur interne du serveur");
            error.put("message", "Une erreur inattendue s'est produite");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Renouvellement d'un token JWT
     * @param authorizationHeader le header Authorization contenant le token actuel
     * @return un nouveau token JWT
     */
    @PostMapping("/refresh")
    @Operation(
        summary = "Renouvellement de token",
        description = "Génère un nouveau token JWT à partir d'un token existant valide"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token renouvelé avec succès",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token invalide ou expiré",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<?> refreshToken(
            @Parameter(description = "Header Authorization avec le token Bearer", required = true)
            @RequestHeader("Authorization") String authorizationHeader) {
        
        try {
            if (!authorizationHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Format de token invalide");
            }
            
            String token = authorizationHeader.substring(7);
            AuthResponseDto response = authService.refreshToken(token);
            
            log.info("Token renouvelé pour l'utilisateur: {}", response.getPseudo());
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.warn("Échec de renouvellement de token: {}", e.getMessage());
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Renouvellement de token échoué");
            error.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            log.error("Erreur lors du renouvellement de token: ", e);
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur interne du serveur");
            error.put("message", "Une erreur inattendue s'est produite");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Vérification de la validité d'un token
     * @param authorizationHeader le header Authorization contenant le token
     * @return les informations de l'utilisateur si le token est valide
     */
    @GetMapping("/verify")
    @Operation(
        summary = "Vérification de token",
        description = "Vérifie la validité d'un token JWT et retourne les informations de l'utilisateur"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token valide",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token invalide ou expiré",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<?> verifyToken(
            @Parameter(description = "Header Authorization avec le token Bearer", required = true)
            @RequestHeader("Authorization") String authorizationHeader) {
        
        try {
            if (!authorizationHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Format de token invalide");
            }
            
            String token = authorizationHeader.substring(7);
            
            if (authService.isTokenExpired(token)) {
                throw new RuntimeException("Token expiré");
            }
            
            String pseudo = authService.getUsernameFromToken(token);
            Integer userId = authService.getUserIdFromToken(token);
            Boolean isAdmin = authService.isAdminFromToken(token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("pseudo", pseudo);
            response.put("userId", userId);
            response.put("admin", isAdmin);
            response.put("message", "Token valide");
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.warn("Token invalide: {}", e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("valid", false);
            error.put("error", "Token invalide");
            error.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            log.error("Erreur lors de la vérification du token: ", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("valid", false);
            error.put("error", "Erreur interne du serveur");
            error.put("message", "Une erreur inattendue s'est produite");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}