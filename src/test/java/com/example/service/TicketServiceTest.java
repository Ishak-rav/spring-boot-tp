package com.example.service;

import com.example.dao.TicketDao;
import com.example.dao.UtilisateurDao;
import com.example.dao.PrioriteDao;
import com.example.dao.CategorieDao;
import com.example.dto.CreateTicketDto;
import com.example.model.Ticket;
import com.example.model.Utilisateur;
import com.example.model.Priorite;
import com.example.model.Categorie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour TicketService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - TicketService")
class TicketServiceTest {

    @Mock
    private TicketDao ticketDao;

    @Mock
    private UtilisateurDao utilisateurDao;

    @Mock
    private PrioriteDao prioriteDao;

    @Mock
    private CategorieDao categorieDao;

    @InjectMocks
    private TicketService ticketService;

    private Utilisateur testUser;
    private Utilisateur testAdmin;
    private Priorite testPriorite;
    private Categorie testCategorie;
    private Ticket testTicket;
    private CreateTicketDto createTicketDto;

    @BeforeEach
    void setUp() {
        // Création d'un utilisateur de test
        testUser = new Utilisateur();
        testUser.setId(1);
        testUser.setPseudo("testuser");
        testUser.setAdmin(false);

        // Création d'un admin de test
        testAdmin = new Utilisateur();
        testAdmin.setId(2);
        testAdmin.setPseudo("admin");
        testAdmin.setAdmin(true);

        // Création d'une priorité de test
        testPriorite = new Priorite();
        testPriorite.setId(1);
        testPriorite.setNom("Haute");

        // Création d'une catégorie de test
        testCategorie = new Categorie();
        testCategorie.setId(1);
        testCategorie.setNom("Bug");

        // Création d'un ticket de test
        testTicket = new Ticket();
        testTicket.setId(1);
        testTicket.setTitre("Ticket de test");
        testTicket.setDescription("Description du ticket de test");
        testTicket.setResolu(false);
        testTicket.setPriorite(testPriorite);
        testTicket.setSoumetteur(testUser);
        testTicket.setDateCreation(LocalDateTime.now());

        // Création d'un DTO de création de ticket
        createTicketDto = new CreateTicketDto();
        createTicketDto.setTitre("Nouveau ticket");
        createTicketDto.setDescription("Description du nouveau ticket");
        createTicketDto.setPrioriteId(1);
        createTicketDto.setCategorieIds(Arrays.asList(1));
    }

    @Test
    @DisplayName("Création de ticket réussie - Utilisateur connecté")
    void testCreateTicket_Success_ConnectedUser() {
        // Given
        when(prioriteDao.findById(1)).thenReturn(Optional.of(testPriorite));
        when(utilisateurDao.findById(1)).thenReturn(Optional.of(testUser));
        when(categorieDao.findAllById(Arrays.asList(1))).thenReturn(Arrays.asList(testCategorie));
        when(ticketDao.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket ticket = invocation.getArgument(0);
            ticket.setId(1);
            return ticket;
        });

        // When
        Ticket result = ticketService.createTicket(createTicketDto, 1);

        // Then
        assertNotNull(result);
        assertEquals("Nouveau ticket", result.getTitre());
        assertEquals("Description du nouveau ticket", result.getDescription());
        assertEquals(testPriorite, result.getPriorite());
        assertEquals(testUser, result.getSoumetteur());
        assertEquals(false, result.isResolu());
        assertTrue(result.getCategories().contains(testCategorie));

        verify(prioriteDao).findById(1);
        verify(utilisateurDao).findById(1);
        verify(categorieDao).findAllById(Arrays.asList(1));
        verify(ticketDao).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Création de ticket réussie - Utilisateur non connecté")
    void testCreateTicket_Success_AnonymousUser() {
        // Given
        when(prioriteDao.findById(1)).thenReturn(Optional.of(testPriorite));
        when(categorieDao.findAllById(Arrays.asList(1))).thenReturn(Arrays.asList(testCategorie));
        when(ticketDao.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket ticket = invocation.getArgument(0);
            ticket.setId(1);
            return ticket;
        });

        // When
        Ticket result = ticketService.createTicket(createTicketDto, null);

        // Then
        assertNotNull(result);
        assertEquals("Nouveau ticket", result.getTitre());
        assertNull(result.getSoumetteur());
        assertEquals(false, result.isResolu());

        verify(prioriteDao).findById(1);
        verify(utilisateurDao, never()).findById(anyInt());
        verify(ticketDao).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Création de ticket échouée - Priorité non trouvée")
    void testCreateTicket_Failure_PrioriteNotFound() {
        // Given
        when(prioriteDao.findById(1)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.createTicket(createTicketDto, 1);
        });

        assertEquals("Priorité non trouvée avec l'ID: 1", exception.getMessage());
        verify(ticketDao, never()).save(any());
    }

    @Test
    @DisplayName("Création de ticket échouée - Catégorie non trouvée")
    void testCreateTicket_Failure_CategorieNotFound() {
        // Given
        when(prioriteDao.findById(1)).thenReturn(Optional.of(testPriorite));
        when(utilisateurDao.findById(1)).thenReturn(Optional.of(testUser));
        when(categorieDao.findAllById(Arrays.asList(1))).thenReturn(Arrays.asList()); // Aucune catégorie trouvée

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.createTicket(createTicketDto, 1);
        });

        assertEquals("Une ou plusieurs catégories n'ont pas été trouvées", exception.getMessage());
        verify(ticketDao, never()).save(any());
    }

    @Test
    @DisplayName("Résolution de ticket réussie")
    void testResolveTicket_Success() {
        // Given
        when(utilisateurDao.findById(2)).thenReturn(Optional.of(testAdmin));
        when(ticketDao.findById(1)).thenReturn(Optional.of(testTicket));
        when(ticketDao.save(any(Ticket.class))).thenReturn(testTicket);

        // When
        Ticket result = ticketService.resolveTicket(1, 2);

        // Then
        assertNotNull(result);
        verify(utilisateurDao).findById(2);
        verify(ticketDao).findById(1);
        verify(ticketDao).save(testTicket);
    }

    @Test
    @DisplayName("Résolution de ticket échouée - Utilisateur non admin")
    void testResolveTicket_Failure_NotAdmin() {
        // Given
        when(utilisateurDao.findById(1)).thenReturn(Optional.of(testUser)); // Utilisateur normal

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.resolveTicket(1, 1);
        });

        assertEquals("Seuls les administrateurs peuvent résoudre des tickets", exception.getMessage());
        verify(ticketDao, never()).findById(anyInt());
        verify(ticketDao, never()).save(any());
    }

    @Test
    @DisplayName("Résolution de ticket échouée - Ticket déjà résolu")
    void testResolveTicket_Failure_AlreadyResolved() {
        // Given
        testTicket.setResolu(true);
        when(utilisateurDao.findById(2)).thenReturn(Optional.of(testAdmin));
        when(ticketDao.findById(1)).thenReturn(Optional.of(testTicket));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.resolveTicket(1, 2);
        });

        assertEquals("Le ticket est déjà résolu", exception.getMessage());
        verify(ticketDao, never()).save(any());
    }

    @Test
    @DisplayName("Réouverture de ticket réussie")
    void testReopenTicket_Success() {
        // Given
        testTicket.setResolu(true);
        testTicket.setResolveur(testAdmin);
        testTicket.setDateResolution(LocalDateTime.now());
        
        when(utilisateurDao.findById(2)).thenReturn(Optional.of(testAdmin));
        when(ticketDao.findById(1)).thenReturn(Optional.of(testTicket));
        when(ticketDao.save(any(Ticket.class))).thenReturn(testTicket);

        // When
        Ticket result = ticketService.reopenTicket(1, 2);

        // Then
        assertNotNull(result);
        verify(ticketDao).save(testTicket);
    }

    @Test
    @DisplayName("Réouverture de ticket échouée - Ticket non résolu")
    void testReopenTicket_Failure_NotResolved() {
        // Given
        testTicket.setResolu(false); // Ticket non résolu
        when(utilisateurDao.findById(2)).thenReturn(Optional.of(testAdmin));
        when(ticketDao.findById(1)).thenReturn(Optional.of(testTicket));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.reopenTicket(1, 2);
        });

        assertEquals("Le ticket n'est pas résolu", exception.getMessage());
        verify(ticketDao, never()).save(any());
    }

    @Test
    @DisplayName("Récupération de tous les tickets")
    void testGetAllTickets() {
        // Given
        List<Ticket> expectedTickets = Arrays.asList(testTicket);
        when(ticketDao.findAll()).thenReturn(expectedTickets);

        // When
        List<Ticket> result = ticketService.getAllTickets();

        // Then
        assertEquals(expectedTickets, result);
        verify(ticketDao).findAll();
    }

    @Test
    @DisplayName("Récupération des tickets non résolus")
    void testGetUnresolvedTickets() {
        // Given
        List<Ticket> expectedTickets = Arrays.asList(testTicket);
        when(ticketDao.findByResoluFalse()).thenReturn(expectedTickets);

        // When
        List<Ticket> result = ticketService.getUnresolvedTickets();

        // Then
        assertEquals(expectedTickets, result);
        verify(ticketDao).findByResoluFalse();
    }

    @Test
    @DisplayName("Récupération de ticket par ID")
    void testGetTicketById() {
        // Given
        when(ticketDao.findById(1)).thenReturn(Optional.of(testTicket));

        // When
        Optional<Ticket> result = ticketService.getTicketById(1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testTicket, result.get());
        verify(ticketDao).findById(1);
    }

    @Test
    @DisplayName("Mise à jour de ticket réussie - Par le soumetteur")
    void testUpdateTicket_Success_BySubmitter() {
        // Given
        CreateTicketDto updateDto = new CreateTicketDto();
        updateDto.setTitre("Titre modifié");
        updateDto.setDescription("Description modifiée");
        updateDto.setPrioriteId(1);

        when(ticketDao.findById(1)).thenReturn(Optional.of(testTicket));
        when(utilisateurDao.findById(1)).thenReturn(Optional.of(testUser));
        when(prioriteDao.findById(1)).thenReturn(Optional.of(testPriorite));
        when(ticketDao.save(any(Ticket.class))).thenReturn(testTicket);

        // When
        Ticket result = ticketService.updateTicket(1, updateDto, 1);

        // Then
        assertNotNull(result);
        verify(ticketDao).findById(1);
        verify(utilisateurDao).findById(1);
        verify(ticketDao).save(testTicket);
    }

    @Test
    @DisplayName("Mise à jour de ticket échouée - Droits insuffisants")
    void testUpdateTicket_Failure_InsufficientRights() {
        // Given
        Utilisateur otherUser = new Utilisateur();
        otherUser.setId(3);
        otherUser.setAdmin(false);

        CreateTicketDto updateDto = new CreateTicketDto();
        updateDto.setTitre("Titre modifié");

        when(ticketDao.findById(1)).thenReturn(Optional.of(testTicket));
        when(utilisateurDao.findById(3)).thenReturn(Optional.of(otherUser));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.updateTicket(1, updateDto, 3);
        });

        assertEquals("Vous n'avez pas le droit de modifier ce ticket", exception.getMessage());
        verify(ticketDao, never()).save(any());
    }

    @Test
    @DisplayName("Suppression de ticket réussie")
    void testDeleteTicket_Success() {
        // Given
        when(utilisateurDao.findById(2)).thenReturn(Optional.of(testAdmin));
        when(ticketDao.findById(1)).thenReturn(Optional.of(testTicket));

        // When
        ticketService.deleteTicket(1, 2);

        // Then
        verify(utilisateurDao).findById(2);
        verify(ticketDao).findById(1);
        verify(ticketDao).delete(testTicket);
    }

    @Test
    @DisplayName("Suppression de ticket échouée - Non admin")
    void testDeleteTicket_Failure_NotAdmin() {
        // Given
        when(utilisateurDao.findById(1)).thenReturn(Optional.of(testUser));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.deleteTicket(1, 1);
        });

        assertEquals("Seuls les administrateurs peuvent supprimer des tickets", exception.getMessage());
        verify(ticketDao, never()).delete(any());
    }

    @Test
    @DisplayName("Recherche de tickets par mot-clé")
    void testSearchTickets() {
        // Given
        String keyword = "test";
        List<Ticket> expectedTickets = Arrays.asList(testTicket);
        when(ticketDao.searchByTitreOrDescription(keyword)).thenReturn(expectedTickets);

        // When
        List<Ticket> result = ticketService.searchTickets(keyword);

        // Then
        assertEquals(expectedTickets, result);
        verify(ticketDao).searchByTitreOrDescription(keyword);
    }

    @Test
    @DisplayName("Recherche de tickets - Mot-clé vide")
    void testSearchTickets_EmptyKeyword() {
        // Given
        List<Ticket> expectedTickets = Arrays.asList(testTicket);
        when(ticketDao.findAll()).thenReturn(expectedTickets);

        // When
        List<Ticket> result = ticketService.searchTickets("");

        // Then
        assertEquals(expectedTickets, result);
        verify(ticketDao).findAll();
        verify(ticketDao, never()).searchByTitreOrDescription(anyString());
    }

    @Test
    @DisplayName("Récupération tickets par utilisateur")
    void testGetTicketsByUser() {
        // Given
        List<Ticket> expectedTickets = Arrays.asList(testTicket);
        when(utilisateurDao.findById(1)).thenReturn(Optional.of(testUser));
        when(ticketDao.findBySoumetteur(testUser)).thenReturn(expectedTickets);

        // When
        List<Ticket> result = ticketService.getTicketsByUser(1);

        // Then
        assertEquals(expectedTickets, result);
        verify(utilisateurDao).findById(1);
        verify(ticketDao).findBySoumetteur(testUser);
    }

    @Test
    @DisplayName("Vérification d'accès ticket - Admin peut tout voir")
    void testCanAccessTicket_AdminCanSeeAll() {
        // Given
        when(ticketDao.findById(1)).thenReturn(Optional.of(testTicket));

        // When
        boolean result = ticketService.canAccessTicket(1, 2, true);

        // Then
        assertTrue(result);
        verify(ticketDao).findById(1);
    }

    @Test
    @DisplayName("Vérification d'accès ticket - Utilisateur connecté peut voir tous les tickets")
    void testCanAccessTicket_ConnectedUserCanSeeAll() {
        // Given
        when(ticketDao.findById(1)).thenReturn(Optional.of(testTicket));

        // When
        boolean result = ticketService.canAccessTicket(1, 1, false);

        // Then
        assertTrue(result);
        verify(ticketDao).findById(1);
    }

    @Test
    @DisplayName("Vérification d'accès ticket - Utilisateur non connecté ne peut voir que les tickets non résolus")
    void testCanAccessTicket_AnonymousUserOnlyUnresolved() {
        // Given
        testTicket.setResolu(false); // Ticket non résolu
        when(ticketDao.findById(1)).thenReturn(Optional.of(testTicket));

        // When
        boolean result = ticketService.canAccessTicket(1, null, false);

        // Then
        assertTrue(result);

        // Test avec ticket résolu
        testTicket.setResolu(true);
        boolean resultResolved = ticketService.canAccessTicket(1, null, false);
        assertFalse(resultResolved);
    }

    @Test
    @DisplayName("Comptage des tickets non résolus")
    void testGetUnresolvedTicketCount() {
        // Given
        when(ticketDao.countByResoluFalse()).thenReturn(5L);

        // When
        long result = ticketService.getUnresolvedTicketCount();

        // Then
        assertEquals(5L, result);
        verify(ticketDao).countByResoluFalse();
    }

    @Test
    @DisplayName("Comptage des tickets résolus")
    void testGetResolvedTicketCount() {
        // Given
        when(ticketDao.countByResoluTrue()).thenReturn(3L);

        // When
        long result = ticketService.getResolvedTicketCount();

        // Then
        assertEquals(3L, result);
        verify(ticketDao).countByResoluTrue();
    }

    @Test
    @DisplayName("Statistiques tickets par priorité")
    void testGetTicketStatsByPriorite() {
        // Given
        List<Object[]> expectedStats = Arrays.asList(
            new Object[]{"Haute", 5L},
            new Object[]{"Normale", 3L}
        );
        when(ticketDao.getTicketCountByPriorite()).thenReturn(expectedStats);

        // When
        List<Object[]> result = ticketService.getTicketStatsByPriorite();

        // Then
        assertEquals(expectedStats, result);
        verify(ticketDao).getTicketCountByPriorite();
    }
}