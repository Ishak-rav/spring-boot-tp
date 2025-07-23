package com.example.integration;

import com.example.dto.AuthResponseDto;
import com.example.dto.LoginRequestDto;
import com.example.model.Utilisateur;
import com.example.dao.UtilisateurDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour AuthController
 * 
 * Ces tests vérifient le fonctionnement complet de l'API d'authentification
 * en testant les endpoints HTTP avec le contexte Spring complet.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests d'intégration - AuthController")
class AuthControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UtilisateurDao utilisateurDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String baseUrl;
    private Utilisateur testUser;
    private Utilisateur testAdmin;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/auth";

        utilisateurDao.deleteAll();

        testUser = new Utilisateur();
        testUser.setPseudo("testuser");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setAdmin(false);
        testUser = utilisateurDao.save(testUser);

        testAdmin = new Utilisateur();
        testAdmin.setPseudo("admin");
        testAdmin.setPassword(passwordEncoder.encode("admin123"));
        testAdmin.setAdmin(true);
        testAdmin = utilisateurDao.save(testAdmin);
    }

    @Test
    @DisplayName("POST /api/auth/login - Connexion réussie utilisateur normal")
    void testLogin_Success_RegularUser() {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setPseudo("testuser");
        loginRequest.setPassword("password123");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequestDto> request = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<AuthResponseDto> response = restTemplate.postForEntity(
                baseUrl + "/login",
                request,
                AuthResponseDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        AuthResponseDto authResponse = response.getBody();
        assertEquals("testuser", authResponse.getPseudo());
        assertEquals(false, authResponse.getAdmin());
        assertEquals("Bearer", authResponse.getType());
        assertNotNull(authResponse.getToken());
        assertEquals("Connexion réussie", authResponse.getMessage());

        String token = authResponse.getToken();
        assertTrue(token.contains("."));
        assertEquals(3, token.split("\\.").length); // Format JWT : header.payload.signature
    }

    @Test
    @DisplayName("POST /api/auth/login - Connexion réussie administrateur")
    void testLogin_Success_Admin() {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setPseudo("admin");
        loginRequest.setPassword("admin123");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequestDto> request = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<AuthResponseDto> response = restTemplate.postForEntity(
                baseUrl + "/login",
                request,
                AuthResponseDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        AuthResponseDto authResponse = response.getBody();
        assertEquals("admin", authResponse.getPseudo());
        assertEquals(true, authResponse.getAdmin());
        assertNotNull(authResponse.getToken());
    }

    @Test
    @DisplayName("POST /api/auth/login - Échec utilisateur non trouvé")
    void testLogin_Failure_UserNotFound() {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setPseudo("inexistant");
        loginRequest.setPassword("password123");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequestDto> request = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/login",
                request,
                Map.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, String> errorResponse = response.getBody();
        assertEquals("Authentification échouée", errorResponse.get("error"));
        assertEquals("Utilisateur non trouvé", errorResponse.get("message"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Échec mot de passe incorrect")
    void testLogin_Failure_WrongPassword() {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setPseudo("testuser");
        loginRequest.setPassword("wrongpassword");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequestDto> request = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/login",
                request,
                Map.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, String> errorResponse = response.getBody();
        assertEquals("Authentification échouée", errorResponse.get("error"));
        assertEquals("Mot de passe incorrect", errorResponse.get("message"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Échec données invalides")
    void testLogin_Failure_InvalidData() {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setPseudo("ab");
        loginRequest.setPassword("password123");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequestDto> request = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/login",
                request,
                Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("POST /api/auth/register - Enregistrement réussi utilisateur normal")
    void testRegister_Success_RegularUser() {
        String url = baseUrl + "/register?pseudo=newuser&password=newpassword123&admin=false";

        ResponseEntity<AuthResponseDto> response = restTemplate.postForEntity(
                url,
                null,
                AuthResponseDto.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        AuthResponseDto authResponse = response.getBody();
        assertEquals("newuser", authResponse.getPseudo());
        assertEquals(false, authResponse.getAdmin());
        assertNotNull(authResponse.getToken());
        assertEquals("Compte créé avec succès", authResponse.getMessage());

        assertTrue(utilisateurDao.existsByPseudo("newuser"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Enregistrement réussi administrateur")
    void testRegister_Success_Admin() {
        String url = baseUrl + "/register?pseudo=newadmin&password=adminpassword123&admin=true";

        ResponseEntity<AuthResponseDto> response = restTemplate.postForEntity(
                url,
                null,
                AuthResponseDto.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        AuthResponseDto authResponse = response.getBody();
        assertEquals("newadmin", authResponse.getPseudo());
        assertEquals(true, authResponse.getAdmin());
        assertNotNull(authResponse.getToken());
    }

    @Test
    @DisplayName("POST /api/auth/register - Échec pseudo déjà existant")
    void testRegister_Failure_PseudoExists() {
        String url = baseUrl + "/register?pseudo=testuser&password=newpassword123&admin=false";

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url,
                null,
                Map.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, String> errorResponse = response.getBody();
        assertEquals("Création de compte échouée", errorResponse.get("error"));
        assertTrue(errorResponse.get("message").contains("existe déjà"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Échec données invalides")
    void testRegister_Failure_InvalidData() {
        String url = baseUrl + "/register?pseudo=validuser&password=123&admin=false";

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url,
                null,
                Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("GET /api/auth/verify - Vérification token valide")
    void testVerifyToken_ValidToken() {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setPseudo("testuser");
        loginRequest.setPassword("password123");

        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequestDto> loginRequestEntity = new HttpEntity<>(loginRequest, loginHeaders);

        ResponseEntity<AuthResponseDto> loginResponse = restTemplate.postForEntity(
                baseUrl + "/login",
                loginRequestEntity,
                AuthResponseDto.class);

        String token = loginResponse.getBody().getToken();

        HttpHeaders verifyHeaders = new HttpHeaders();
        verifyHeaders.set("Authorization", "Bearer " + token);
        HttpEntity<Void> verifyRequest = new HttpEntity<>(verifyHeaders);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/verify",
                HttpMethod.GET,
                verifyRequest,
                Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> verifyResponse = response.getBody();
        assertEquals(true, verifyResponse.get("valid"));
        assertEquals("testuser", verifyResponse.get("pseudo"));
        assertEquals(testUser.getId(), verifyResponse.get("userId"));
        assertEquals(false, verifyResponse.get("admin"));
        assertEquals("Token valide", verifyResponse.get("message"));
    }

    @Test
    @DisplayName("GET /api/auth/verify - Token invalide")
    void testVerifyToken_InvalidToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer invalid.token.here");
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/verify",
                HttpMethod.GET,
                request,
                Map.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> errorResponse = response.getBody();
        assertEquals(false, errorResponse.get("valid"));
        assertEquals("Token invalide", errorResponse.get("error"));
    }

    @Test
    @DisplayName("GET /api/auth/verify - Header Authorization manquant")
    void testVerifyToken_MissingAuthHeader() {
        HttpEntity<Void> request = new HttpEntity<>(new HttpHeaders());

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/verify",
                HttpMethod.GET,
                request,
                Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("POST /api/auth/refresh - Renouvellement token réussi")
    void testRefreshToken_Success() {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setPseudo("testuser");
        loginRequest.setPassword("password123");

        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequestDto> loginRequestEntity = new HttpEntity<>(loginRequest, loginHeaders);

        ResponseEntity<AuthResponseDto> loginResponse = restTemplate.postForEntity(
                baseUrl + "/login",
                loginRequestEntity,
                AuthResponseDto.class);

        String originalToken = loginResponse.getBody().getToken();

        HttpHeaders refreshHeaders = new HttpHeaders();
        refreshHeaders.set("Authorization", "Bearer " + originalToken);
        HttpEntity<Void> refreshRequest = new HttpEntity<>(refreshHeaders);

        ResponseEntity<AuthResponseDto> response = restTemplate.postForEntity(
                baseUrl + "/refresh",
                refreshRequest,
                AuthResponseDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        AuthResponseDto refreshResponse = response.getBody();
        assertEquals("testuser", refreshResponse.getPseudo());
        assertEquals(false, refreshResponse.getAdmin());
        assertEquals("Token renouvelé", refreshResponse.getMessage());
        assertNotNull(refreshResponse.getToken());
        assertNotEquals(originalToken, refreshResponse.getToken()); // Nouveau token différent
    }

    @Test
    @DisplayName("Scénario complet - Enregistrement, connexion, vérification")
    void testCompleteAuthFlow() {
        String registerUrl = baseUrl + "/register?pseudo=flowuser&password=flowpassword123&admin=false";
        ResponseEntity<AuthResponseDto> registerResponse = restTemplate.postForEntity(
                registerUrl,
                null,
                AuthResponseDto.class);

        assertEquals(HttpStatus.CREATED, registerResponse.getStatusCode());
        String registerToken = registerResponse.getBody().getToken();
        assertNotNull(registerToken);

        HttpHeaders verifyHeaders = new HttpHeaders();
        verifyHeaders.set("Authorization", "Bearer " + registerToken);
        HttpEntity<Void> verifyRequest = new HttpEntity<>(verifyHeaders);

        ResponseEntity<Map> verifyResponse = restTemplate.exchange(
                baseUrl + "/verify",
                HttpMethod.GET,
                verifyRequest,
                Map.class);

        assertEquals(HttpStatus.OK, verifyResponse.getStatusCode());
        assertEquals(true, verifyResponse.getBody().get("valid"));

        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setPseudo("flowuser");
        loginRequest.setPassword("flowpassword123");

        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequestDto> loginRequestEntity = new HttpEntity<>(loginRequest, loginHeaders);

        ResponseEntity<AuthResponseDto> loginResponse = restTemplate.postForEntity(
                baseUrl + "/login",
                loginRequestEntity,
                AuthResponseDto.class);

        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        String loginToken = loginResponse.getBody().getToken();
        assertNotNull(loginToken);

        HttpHeaders refreshHeaders = new HttpHeaders();
        refreshHeaders.set("Authorization", "Bearer " + loginToken);
        HttpEntity<Void> refreshRequest = new HttpEntity<>(refreshHeaders);

        ResponseEntity<AuthResponseDto> refreshResponse = restTemplate.postForEntity(
                baseUrl + "/refresh",
                refreshRequest,
                AuthResponseDto.class);

        assertEquals(HttpStatus.OK, refreshResponse.getStatusCode());
        assertNotEquals(loginToken, refreshResponse.getBody().getToken());
    }
}