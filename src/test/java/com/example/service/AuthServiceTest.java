package com.example.service;

import com.example.dao.UtilisateurDao;
import com.example.dto.AuthResponseDto;
import com.example.dto.LoginRequestDto;
import com.example.model.Utilisateur;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour AuthService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - AuthService")
class AuthServiceTest {

    @Mock
    private UtilisateurDao utilisateurDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private Utilisateur testUser;
    private Utilisateur testAdmin;
    private LoginRequestDto loginRequest;
    private final String testSecret = "mySecretKey123456789012345678901234567890123456789012345678901234567890";

    @BeforeEach
    void setUp() {
        // Configuration des propriétés JWT
        ReflectionTestUtils.setField(authService, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(authService, "jwtExpirationInSeconds", 86400);

        // Création d'un utilisateur de test
        testUser = new Utilisateur();
        testUser.setId(1);
        testUser.setPseudo("testuser");
        testUser.setPassword("hashedPassword");
        testUser.setAdmin(false);

        // Création d'un admin de test
        testAdmin = new Utilisateur();
        testAdmin.setId(2);
        testAdmin.setPseudo("admin");
        testAdmin.setPassword("hashedAdminPassword");
        testAdmin.setAdmin(true);

        // Requête de connexion de test
        loginRequest = new LoginRequestDto();
        loginRequest.setPseudo("testuser");
        loginRequest.setPassword("plainPassword");
    }

    @Test
    @DisplayName("Login réussi - Utilisateur normal")
    void testLogin_Success_RegularUser() {
        // Given
        when(utilisateurDao.findByPseudo("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("plainPassword", "hashedPassword")).thenReturn(true);

        // When
        AuthResponseDto response = authService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals("testuser", response.getPseudo());
        assertEquals(false, response.getAdmin());
        assertEquals("Bearer", response.getType());
        assertNotNull(response.getToken());
        assertEquals("Connexion réussie", response.getMessage());

        verify(utilisateurDao).findByPseudo("testuser");
        verify(passwordEncoder).matches("plainPassword", "hashedPassword");
    }

    @Test
    @DisplayName("Login réussi - Administrateur")
    void testLogin_Success_Admin() {
        // Given
        loginRequest.setPseudo("admin");
        when(utilisateurDao.findByPseudo("admin")).thenReturn(Optional.of(testAdmin));
        when(passwordEncoder.matches("plainPassword", "hashedAdminPassword")).thenReturn(true);

        // When
        AuthResponseDto response = authService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals("admin", response.getPseudo());
        assertEquals(true, response.getAdmin());
        assertNotNull(response.getToken());
    }

    @Test
    @DisplayName("Login échoué - Utilisateur non trouvé")
    void testLogin_Failure_UserNotFound() {
        // Given
        when(utilisateurDao.findByPseudo("testuser")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Utilisateur non trouvé", exception.getMessage());
        verify(utilisateurDao).findByPseudo("testuser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Login échoué - Mot de passe incorrect")
    void testLogin_Failure_WrongPassword() {
        // Given
        when(utilisateurDao.findByPseudo("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("plainPassword", "hashedPassword")).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Mot de passe incorrect", exception.getMessage());
        verify(passwordEncoder).matches("plainPassword", "hashedPassword");
    }

    @Test
    @DisplayName("Génération de token JWT")
    void testGenerateToken() {
        // When
        String token = authService.generateToken(testUser);

        // Then
        assertNotNull(token);
        assertTrue(token.length() > 0);

        // Vérification du contenu du token
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(testSecret.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("testuser", claims.getSubject());
        assertEquals(1, claims.get("userId", Integer.class));
        assertEquals(false, claims.get("admin", Boolean.class));
    }

    @Test
    @DisplayName("Validation de token JWT valide")
    void testValidateToken_ValidToken() {
        // Given
        String token = authService.generateToken(testUser);

        // When
        Claims claims = authService.validateToken(token);

        // Then
        assertNotNull(claims);
        assertEquals("testuser", claims.getSubject());
        assertEquals(1, claims.get("userId", Integer.class));
        assertEquals(false, claims.get("admin", Boolean.class));
    }

    @Test
    @DisplayName("Validation de token JWT invalide")
    void testValidateToken_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authService.validateToken(invalidToken);
        });
    }

    @Test
    @DisplayName("Extraction du nom d'utilisateur du token")
    void testGetUsernameFromToken() {
        // Given
        String token = authService.generateToken(testUser);

        // When
        String username = authService.getUsernameFromToken(token);

        // Then
        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("Extraction de l'ID utilisateur du token")
    void testGetUserIdFromToken() {
        // Given
        String token = authService.generateToken(testUser);

        // When
        Integer userId = authService.getUserIdFromToken(token);

        // Then
        assertEquals(1, userId);
    }

    @Test
    @DisplayName("Vérification du statut admin du token")
    void testIsAdminFromToken() {
        // Given
        String userToken = authService.generateToken(testUser);
        String adminToken = authService.generateToken(testAdmin);

        // When & Then
        assertEquals(false, authService.isAdminFromToken(userToken));
        assertEquals(true, authService.isAdminFromToken(adminToken));
    }

    @Test
    @DisplayName("Hachage du mot de passe")
    void testHashPassword() {
        // Given
        String plainPassword = "password123";
        String hashedPassword = "hashedPassword123";
        when(passwordEncoder.encode(plainPassword)).thenReturn(hashedPassword);

        // When
        String result = authService.hashPassword(plainPassword);

        // Then
        assertEquals(hashedPassword, result);
        verify(passwordEncoder).encode(plainPassword);
    }

    @Test
    @DisplayName("Vérification du mot de passe")
    void testVerifyPassword() {
        // Given
        String plainPassword = "password123";
        String hashedPassword = "hashedPassword123";
        when(passwordEncoder.matches(plainPassword, hashedPassword)).thenReturn(true);

        // When
        boolean result = authService.verifyPassword(plainPassword, hashedPassword);

        // Then
        assertTrue(result);
        verify(passwordEncoder).matches(plainPassword, hashedPassword);
    }

    @Test
    @DisplayName("Création d'utilisateur réussie")
    void testCreateUser_Success() {
        // Given
        String pseudo = "newuser";
        String password = "password123";
        String hashedPassword = "hashedPassword123";

        when(utilisateurDao.existsByPseudo(pseudo)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(hashedPassword);
        when(utilisateurDao.save(any(Utilisateur.class))).thenAnswer(invocation -> {
            Utilisateur user = invocation.getArgument(0);
            user.setId(3);
            return user;
        });

        // When
        Utilisateur result = authService.createUser(pseudo, password, false);

        // Then
        assertNotNull(result);
        assertEquals(pseudo, result.getPseudo());
        assertEquals(hashedPassword, result.getPassword());
        assertEquals(false, result.isAdmin());

        verify(utilisateurDao).existsByPseudo(pseudo);
        verify(passwordEncoder).encode(password);
        verify(utilisateurDao).save(any(Utilisateur.class));
    }

    @Test
    @DisplayName("Création d'utilisateur échouée - Pseudo existe déjà")
    void testCreateUser_Failure_PseudoExists() {
        // Given
        String pseudo = "existinguser";
        when(utilisateurDao.existsByPseudo(pseudo)).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.createUser(pseudo, "password", false);
        });

        assertEquals("Un utilisateur avec ce pseudo existe déjà", exception.getMessage());
        verify(utilisateurDao).existsByPseudo(pseudo);
        verify(utilisateurDao, never()).save(any());
    }

    @Test
    @DisplayName("Récupération d'utilisateur depuis token")
    void testGetUserFromToken() {
        // Given
        String token = authService.generateToken(testUser);
        when(utilisateurDao.findByPseudo("testuser")).thenReturn(Optional.of(testUser));

        // When
        Utilisateur result = authService.getUserFromToken(token);

        // Then
        assertEquals(testUser, result);
        verify(utilisateurDao).findByPseudo("testuser");
    }

    @Test
    @DisplayName("Récupération d'utilisateur depuis token - Utilisateur non trouvé")
    void testGetUserFromToken_UserNotFound() {
        // Given
        String token = authService.generateToken(testUser);
        when(utilisateurDao.findByPseudo("testuser")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authService.getUserFromToken(token);
        });
    }

    @Test
    @DisplayName("Vérification des droits d'accès - Admin peut tout")
    void testCanAccessResource_AdminCanAccessAll() {
        // Given
        String adminToken = authService.generateToken(testAdmin);

        // When & Then
        assertTrue(authService.canAccessResource(adminToken, 999));
        assertTrue(authService.canAccessResource(adminToken, 1));
    }

    @Test
    @DisplayName("Vérification des droits d'accès - Utilisateur peut accéder à ses ressources")
    void testCanAccessResource_UserCanAccessOwnResource() {
        // Given
        String userToken = authService.generateToken(testUser);

        // When & Then
        assertTrue(authService.canAccessResource(userToken, 1)); // Ses propres ressources
        assertFalse(authService.canAccessResource(userToken, 999)); // Ressources d'autrui
    }

    @Test
    @DisplayName("Mise à jour mot de passe réussie")
    void testUpdatePassword_Success() {
        // Given
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        String hashedOldPassword = "hashedOldPassword";
        String hashedNewPassword = "hashedNewPassword";

        testUser.setPassword(hashedOldPassword);

        when(utilisateurDao.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(oldPassword, hashedOldPassword)).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(hashedNewPassword);
        when(utilisateurDao.save(testUser)).thenReturn(testUser);

        // When
        authService.updatePassword(1, oldPassword, newPassword);

        // Then
        verify(utilisateurDao).findById(1);
        verify(passwordEncoder).matches(oldPassword, hashedOldPassword);
        verify(passwordEncoder).encode(newPassword);
        verify(utilisateurDao).save(testUser);
    }

    @Test
    @DisplayName("Mise à jour mot de passe échouée - Ancien mot de passe incorrect")
    void testUpdatePassword_Failure_WrongOldPassword() {
        // Given
        String oldPassword = "wrongOldPassword";
        String newPassword = "newPassword";
        String hashedOldPassword = "hashedOldPassword";

        testUser.setPassword(hashedOldPassword);

        when(utilisateurDao.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(oldPassword, hashedOldPassword)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.updatePassword(1, oldPassword, newPassword);
        });

        assertEquals("Ancien mot de passe incorrect", exception.getMessage());
        verify(utilisateurDao, never()).save(any());
    }

    @Test
    @DisplayName("Renouvellement de token réussi")
    void testRefreshToken_Success() {
        // Given
        String originalToken = authService.generateToken(testUser);
        when(utilisateurDao.findByPseudo("testuser")).thenReturn(Optional.of(testUser));

        // When
        AuthResponseDto response = authService.refreshToken(originalToken);

        // Then
        assertNotNull(response);
        assertEquals("testuser", response.getPseudo());
        assertEquals(false, response.getAdmin());
        assertEquals("Token renouvelé", response.getMessage());
        assertNotNull(response.getToken());
        assertNotEquals(originalToken, response.getToken()); // Nouveau token différent
    }

    @Test
    @DisplayName("Test de token expiré")
    void testIsTokenExpired_ExpiredToken() {
        // Given - Créer un token avec une expiration très courte
        ReflectionTestUtils.setField(authService, "jwtExpirationInSeconds", -1);
        String expiredToken = authService.generateToken(testUser);

        // Remettre la valeur normale
        ReflectionTestUtils.setField(authService, "jwtExpirationInSeconds", 86400);

        // When & Then
        assertTrue(authService.isTokenExpired(expiredToken));
    }

    @Test
    @DisplayName("Test de token valide (non expiré)")
    void testIsTokenExpired_ValidToken() {
        // Given
        String validToken = authService.generateToken(testUser);

        // When & Then
        assertFalse(authService.isTokenExpired(validToken));
    }
}