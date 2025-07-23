package com.example.integration;

import com.example.dto.AuthResponseDto;
import com.example.dto.CreateTicketDto;
import com.example.dto.LoginRequestDto;
import com.example.model.Ticket;
import com.example.model.Utilisateur;
import com.example.model.Priorite;
import com.example.model.Categorie;
import com.example.dao.UtilisateurDao;
import com.example.dao.TicketDao;
import com.example.dao.PrioriteDao;
import com.example.dao.CategorieDao;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour TicketController
 * 
 * Ces tests vérifient le fonctionnement complet de l'API de gestion des tickets
 * en testant les endpoints HTTP avec le contexte Spring complet.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests d'intégration - TicketController")
class TicketControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UtilisateurDao utilisateurDao;

    @Autowired
    private TicketDao ticketDao;

    @Autowired
    private PrioriteDao prioriteDao;

    @Autowired
    private CategorieDao categorieDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String baseUrl;
    private String authUrl;
    private Utilisateur testUser;
    private Utilisateur testAdmin;
    private Priorite testPriorite;
    private Categorie testCategorie;
    private Ticket testTicket;
    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/tickets";
        authUrl = "http://localhost:" + port + "/api/auth";

        ticketDao.deleteAll();
        categorieDao.deleteAll();
        prioriteDao.deleteAll();
        utilisateurDao.deleteAll();

        testPriorite = new Priorite();
        testPriorite.setNom("Haute");
        testPriorite = prioriteDao.save(testPriorite);

        testCategorie = new Categorie();
        testCategorie.setNom("Bug");
        testCategorie = categorieDao.save(testCategorie);

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

        testTicket = new Ticket();
        testTicket.setTitre("Ticket de test");
        testTicket.setDescription("Description du ticket de test");
        testTicket.setResolu(false);
        testTicket.setPriorite(testPriorite);
        testTicket.setSoumetteur(testUser);
        testTicket.setDateCreation(LocalDateTime.now());
        testTicket.setCategories(Arrays.asList(testCategorie));
        testTicket = ticketDao.save(testTicket);

        userToken = getAuthToken("testuser", "password123");
        adminToken = getAuthToken("admin", "admin123");
    }

    private String getAuthToken(String pseudo, String password) {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setPseudo(pseudo);
        loginRequest.setPassword(password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequestDto> request = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<AuthResponseDto> response = restTemplate.postForEntity(
                authUrl + "/login",
                request,
                AuthResponseDto.class);

        return response.getBody().getToken();
    }

    private HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.set("Authorization", "Bearer " + token);
        }
        return headers;
    }

    @Test
    @DisplayName("GET /api/tickets/unresolved - Accès public aux tickets non résolus")
    void testGetUnresolvedTickets_PublicAccess() {
        HttpEntity<Void> request = new HttpEntity<>(new HttpHeaders());

        ResponseEntity<List> response = restTemplate.exchange(
                baseUrl + "/unresolved",
                HttpMethod.GET,
                request,
                List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    @DisplayName("GET /api/tickets/public - Alias pour l'accès public")
    void testGetPublicTickets() {
        HttpEntity<Void> request = new HttpEntity<>(new HttpHeaders());

        ResponseEntity<List> response = restTemplate.exchange(
                baseUrl + "/public",
                HttpMethod.GET,
                request,
                List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("GET /api/tickets - Accès aux tickets pour utilisateur connecté")
    void testGetAllTickets_AuthenticatedUser() {
        HttpHeaders headers = createAuthHeaders(userToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                request,
                List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("GET /api/tickets - Accès refusé sans authentification")
    void testGetAllTickets_Unauthenticated() {
        HttpEntity<Void> request = new HttpEntity<>(new HttpHeaders());

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                request,
                Map.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("POST /api/tickets - Création de ticket par utilisateur connecté")
    void testCreateTicket_AuthenticatedUser() {
        CreateTicketDto createTicketDto = new CreateTicketDto();
        createTicketDto.setTitre("Nouveau ticket de test");
        createTicketDto.setDescription("Description du nouveau ticket");
        createTicketDto.setPrioriteId(testPriorite.getId());
        createTicketDto.setCategorieIds(Arrays.asList(testCategorie.getId()));

        HttpHeaders headers = createAuthHeaders(userToken);
        HttpEntity<CreateTicketDto> request = new HttpEntity<>(createTicketDto, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl,
                request,
                Map.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> ticket = response.getBody();
        assertEquals("Nouveau ticket de test", ticket.get("titre"));
        assertEquals("Description du nouveau ticket", ticket.get("description"));
    }

    @Test
    @DisplayName("POST /api/tickets - Création échouée sans authentification")
    void testCreateTicket_Unauthenticated() {
        CreateTicketDto createTicketDto = new CreateTicketDto();
        createTicketDto.setTitre("Ticket sans auth");
        createTicketDto.setDescription("Description");
        createTicketDto.setPrioriteId(testPriorite.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateTicketDto> request = new HttpEntity<>(createTicketDto, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl,
                request,
                Map.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("POST /api/tickets - Création échouée avec données invalides")
    void testCreateTicket_InvalidData() {
        CreateTicketDto createTicketDto = new CreateTicketDto();
        createTicketDto.setTitre("AB"); // Trop court
        createTicketDto.setDescription("Description valide");
        createTicketDto.setPrioriteId(testPriorite.getId());

        HttpHeaders headers = createAuthHeaders(userToken);
        HttpEntity<CreateTicketDto> request = new HttpEntity<>(createTicketDto, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl,
                request,
                Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("PUT /api/tickets/{id}/resolve - Résolution par admin")
    void testResolveTicket_Admin() {
        HttpHeaders headers = createAuthHeaders(adminToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/" + testTicket.getId() + "/resolve",
                HttpMethod.PUT,
                request,
                Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> ticket = response.getBody();
        assertEquals(true, ticket.get("resolu"));
    }

    @Test
    @DisplayName("PUT /api/tickets/{id}/resolve - Refus pour utilisateur normal")
    void testResolveTicket_RegularUser() {
        HttpHeaders headers = createAuthHeaders(userToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/" + testTicket.getId() + "/resolve",
                HttpMethod.PUT,
                request,
                Map.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("PUT /api/tickets/{id}/reopen - Réouverture par admin")
    void testReopenTicket_Admin() {
        testTicket.setResolu(true);
        testTicket.setResolveur(testAdmin);
        testTicket.setDateResolution(LocalDateTime.now());
        ticketDao.save(testTicket);

        HttpHeaders headers = createAuthHeaders(adminToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/" + testTicket.getId() + "/reopen",
                HttpMethod.PUT,
                request,
                Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> ticket = response.getBody();
        assertEquals(false, ticket.get("resolu"));
    }

    @Test
    @DisplayName("DELETE /api/tickets/{id} - Suppression par admin")
    void testDeleteTicket_Admin() {
        HttpHeaders headers = createAuthHeaders(adminToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + testTicket.getId(),
                HttpMethod.DELETE,
                request,
                Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        assertFalse(ticketDao.existsById(testTicket.getId()));
    }

    @Test
    @DisplayName("DELETE /api/tickets/{id} - Refus pour utilisateur normal")
    void testDeleteTicket_RegularUser() {
        HttpHeaders headers = createAuthHeaders(userToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/" + testTicket.getId(),
                HttpMethod.DELETE,
                request,
                Map.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("GET /api/tickets/{id} - Accès aux détails d'un ticket")
    void testGetTicketById_AuthenticatedUser() {
        HttpHeaders headers = createAuthHeaders(userToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/" + testTicket.getId(),
                HttpMethod.GET,
                request,
                Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> ticket = response.getBody();
        assertEquals(testTicket.getTitre(), ticket.get("titre"));
        assertEquals(testTicket.getDescription(), ticket.get("description"));
    }

    @Test
    @DisplayName("GET /api/tickets/{id} - Ticket non trouvé")
    void testGetTicketById_NotFound() {
        HttpHeaders headers = createAuthHeaders(userToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/999999",
                HttpMethod.GET,
                request,
                Map.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, String> error = response.getBody();
        assertEquals("Ticket non trouvé", error.get("error"));
    }

    @Test
    @DisplayName("PUT /api/tickets/{id} - Mise à jour par le soumetteur")
    void testUpdateTicket_BySubmitter() {
        CreateTicketDto updateDto = new CreateTicketDto();
        updateDto.setTitre("Titre modifié");
        updateDto.setDescription("Description modifiée");
        updateDto.setPrioriteId(testPriorite.getId());

        HttpHeaders headers = createAuthHeaders(userToken);
        HttpEntity<CreateTicketDto> request = new HttpEntity<>(updateDto, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/" + testTicket.getId(),
                HttpMethod.PUT,
                request,
                Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> ticket = response.getBody();
        assertEquals("Titre modifié", ticket.get("titre"));
        assertEquals("Description modifiée", ticket.get("description"));
    }

    @Test
    @DisplayName("GET /api/tickets/user/{userId} - Tickets d'un utilisateur")
    void testGetTicketsByUser_OwnTickets() {
        HttpHeaders headers = createAuthHeaders(userToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
                baseUrl + "/user/" + testUser.getId(),
                HttpMethod.GET,
                request,
                List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("GET /api/tickets/user/{userId} - Refus d'accès aux tickets d'autrui")
    void testGetTicketsByUser_OtherUserTickets() {
        HttpHeaders headers = createAuthHeaders(userToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/user/" + testAdmin.getId(),
                HttpMethod.GET,
                request,
                Map.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, String> error = response.getBody();
        assertEquals("Accès refusé", error.get("error"));
    }

    @Test
    @DisplayName("GET /api/tickets/search - Recherche de tickets")
    void testSearchTickets() {
        HttpHeaders headers = createAuthHeaders(userToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
                baseUrl + "/search?keyword=test",
                HttpMethod.GET,
                request,
                List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("GET /api/tickets/stats - Statistiques des tickets")
    void testGetTicketStats() {
        HttpHeaders headers = createAuthHeaders(userToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/stats",
                HttpMethod.GET,
                request,
                Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> stats = response.getBody();
        assertTrue(stats.containsKey("total"));
        assertTrue(stats.containsKey("unresolved"));
        assertTrue(stats.containsKey("resolved"));
        assertTrue(stats.containsKey("byPriorite"));
    }

    @Test
    @DisplayName("Scénario complet - Création, modification, résolution d'un ticket")
    void testCompleteTicketFlow() {
        CreateTicketDto createDto = new CreateTicketDto();
        createDto.setTitre("Ticket flow test");
        createDto.setDescription("Test du flow complet");
        createDto.setPrioriteId(testPriorite.getId());
        createDto.setCategorieIds(Arrays.asList(testCategorie.getId()));

        HttpHeaders userHeaders = createAuthHeaders(userToken);
        HttpEntity<CreateTicketDto> createRequest = new HttpEntity<>(createDto, userHeaders);

        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
                baseUrl,
                createRequest,
                Map.class);

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        Integer ticketId = (Integer) createResponse.getBody().get("id");
        assertNotNull(ticketId);

        CreateTicketDto updateDto = new CreateTicketDto();
        updateDto.setTitre("Ticket flow test - modifié");
        updateDto.setDescription("Description mise à jour");
        updateDto.setPrioriteId(testPriorite.getId());

        HttpEntity<CreateTicketDto> updateRequest = new HttpEntity<>(updateDto, userHeaders);

        ResponseEntity<Map> updateResponse = restTemplate.exchange(
                baseUrl + "/" + ticketId,
                HttpMethod.PUT,
                updateRequest,
                Map.class);

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertEquals("Ticket flow test - modifié", updateResponse.getBody().get("titre"));

        HttpEntity<Void> resolveUserRequest = new HttpEntity<>(userHeaders);

        ResponseEntity<Map> resolveUserResponse = restTemplate.exchange(
                baseUrl + "/" + ticketId + "/resolve",
                HttpMethod.PUT,
                resolveUserRequest,
                Map.class);

        assertEquals(HttpStatus.FORBIDDEN, resolveUserResponse.getStatusCode());

        HttpHeaders adminHeaders = createAuthHeaders(adminToken);
        HttpEntity<Void> resolveAdminRequest = new HttpEntity<>(adminHeaders);

        ResponseEntity<Map> resolveAdminResponse = restTemplate.exchange(
                baseUrl + "/" + ticketId + "/resolve",
                HttpMethod.PUT,
                resolveAdminRequest,
                Map.class);

        assertEquals(HttpStatus.OK, resolveAdminResponse.getStatusCode());
        assertEquals(true, resolveAdminResponse.getBody().get("resolu"));

        HttpEntity<Void> publicRequest = new HttpEntity<>(new HttpHeaders());

        ResponseEntity<List> publicResponse = restTemplate.exchange(
                baseUrl + "/unresolved",
                HttpMethod.GET,
                publicRequest,
                List.class);

        assertEquals(HttpStatus.OK, publicResponse.getStatusCode());
    }
}