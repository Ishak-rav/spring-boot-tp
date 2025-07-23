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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketService {

    private final TicketDao ticketDao;
    private final UtilisateurDao utilisateurDao;
    private final PrioriteDao prioriteDao;
    private final CategorieDao categorieDao;

    /**
     * Crée un nouveau ticket
     * 
     * @param createTicketDto les données du ticket à créer
     * @param soumetteurId    l'ID de l'utilisateur qui soumet le ticket (peut être
     *                        null pour un utilisateur non connecté)
     * @return le ticket créé
     */
    public Ticket createTicket(CreateTicketDto createTicketDto, Integer soumetteurId) {
        Priorite priorite = prioriteDao.findById(createTicketDto.getPrioriteId())
                .orElseThrow(() -> new RuntimeException(
                        "Priorité non trouvée avec l'ID: " + createTicketDto.getPrioriteId()));

        Ticket ticket = new Ticket();
        ticket.setTitre(createTicketDto.getTitre());
        ticket.setDescription(createTicketDto.getDescription());
        ticket.setPriorite(priorite);
        ticket.setResolu(false);

        if (soumetteurId != null) {
            Utilisateur soumetteur = utilisateurDao.findById(soumetteurId)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + soumetteurId));
            ticket.setSoumetteur(soumetteur);
        }

        if (createTicketDto.getCategorieIds() != null && !createTicketDto.getCategorieIds().isEmpty()) {
            List<Categorie> categories = categorieDao.findAllById(createTicketDto.getCategorieIds());
            if (categories.size() != createTicketDto.getCategorieIds().size()) {
                throw new RuntimeException("Une ou plusieurs catégories n'ont pas été trouvées");
            }
            ticket.setCategories(categories);
        }

        return ticketDao.save(ticket);
    }

    /**
     * Marque un ticket comme résolu (admin seulement)
     * 
     * @param ticketId    l'ID du ticket à résoudre
     * @param resolveurId l'ID de l'utilisateur admin qui résout le ticket
     * @return le ticket mis à jour
     */
    public Ticket resolveTicket(Integer ticketId, Integer resolveurId) {
        Utilisateur resolveur = utilisateurDao.findById(resolveurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!resolveur.isAdmin()) {
            throw new RuntimeException("Seuls les administrateurs peuvent résoudre des tickets");
        }

        Ticket ticket = ticketDao.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket non trouvé avec l'ID: " + ticketId));

        if (ticket.isResolu()) {
            throw new RuntimeException("Le ticket est déjà résolu");
        }

        ticket.marquerCommeResolu(resolveur);

        return ticketDao.save(ticket);
    }

    /**
     * Rouvre un ticket résolu (admin seulement)
     * 
     * @param ticketId l'ID du ticket à rouvrir
     * @param adminId  l'ID de l'utilisateur admin
     * @return le ticket mis à jour
     */
    public Ticket reopenTicket(Integer ticketId, Integer adminId) {
        Utilisateur admin = utilisateurDao.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!admin.isAdmin()) {
            throw new RuntimeException("Seuls les administrateurs peuvent rouvrir des tickets");
        }

        Ticket ticket = ticketDao.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket non trouvé avec l'ID: " + ticketId));

        if (!ticket.isResolu()) {
            throw new RuntimeException("Le ticket n'est pas résolu");
        }

        ticket.setResolu(false);
        ticket.setResolveur(null);
        ticket.setDateResolution(null);

        return ticketDao.save(ticket);
    }

    /**
     * Récupère tous les tickets (pour utilisateurs connectés)
     * 
     * @return la liste de tous les tickets
     */
    public List<Ticket> getAllTickets() {
        return ticketDao.findAll();
    }

    /**
     * Récupère tous les tickets non résolus (accessible à tous, même non connectés)
     * 
     * @return la liste des tickets non résolus
     */
    public List<Ticket> getUnresolvedTickets() {
        return ticketDao.findByResoluFalse();
    }

    /**
     * Récupère tous les tickets résolus
     * 
     * @return la liste des tickets résolus
     */
    public List<Ticket> getResolvedTickets() {
        return ticketDao.findByResoluTrue();
    }

    /**
     * Récupère un ticket par son ID
     * 
     * @param ticketId l'ID du ticket
     * @return le ticket s'il existe
     */
    public Optional<Ticket> getTicketById(Integer ticketId) {
        return ticketDao.findById(ticketId);
    }

    /**
     * Récupère les tickets soumis par un utilisateur
     * 
     * @param utilisateurId l'ID de l'utilisateur
     * @return la liste des tickets soumis par l'utilisateur
     */
    public List<Ticket> getTicketsByUser(Integer utilisateurId) {
        Utilisateur utilisateur = utilisateurDao.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return ticketDao.findBySoumetteur(utilisateur);
    }

    /**
     * Récupère les tickets résolus par un utilisateur
     * 
     * @param resolveurId l'ID de l'utilisateur résolveur
     * @return la liste des tickets résolus par l'utilisateur
     */
    public List<Ticket> getTicketsResolvedByUser(Integer resolveurId) {
        Utilisateur resolveur = utilisateurDao.findById(resolveurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return ticketDao.findByResolveur(resolveur);
    }

    /**
     * Récupère les tickets par priorité
     * 
     * @param prioriteId l'ID de la priorité
     * @return la liste des tickets avec cette priorité
     */
    public List<Ticket> getTicketsByPriorite(Integer prioriteId) {
        Priorite priorite = prioriteDao.findById(prioriteId)
                .orElseThrow(() -> new RuntimeException("Priorité non trouvée"));
        return ticketDao.findByPriorite(priorite);
    }

    /**
     * Récupère les tickets par catégorie
     * 
     * @param categorieId l'ID de la catégorie
     * @return la liste des tickets contenant cette catégorie
     */
    public List<Ticket> getTicketsByCategorie(Integer categorieId) {
        Categorie categorie = categorieDao.findById(categorieId)
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée"));
        return ticketDao.findByCategoriesContaining(categorie);
    }

    /**
     * Recherche des tickets par mot-clé dans le titre ou la description
     * 
     * @param keyword le mot-clé à rechercher
     * @return la liste des tickets correspondants
     */
    public List<Ticket> searchTickets(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllTickets();
        }
        return ticketDao.searchByTitreOrDescription(keyword.trim());
    }

    /**
     * Récupère les tickets non résolus les plus anciens
     * 
     * @param limit le nombre maximum de tickets à retourner
     * @return la liste des tickets les plus anciens non résolus
     */
    public List<Ticket> getOldestUnresolvedTickets(int limit) {
        List<Ticket> tickets = ticketDao.findOldestUnresolvedTickets();
        return tickets.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * Récupère les tickets résolus récemment
     * 
     * @param days le nombre de jours à considérer
     * @return la liste des tickets résolus récemment
     */
    public List<Ticket> getRecentlyResolvedTickets(int days) {
        LocalDateTime dateLimit = LocalDateTime.now().minusDays(days);
        return ticketDao.findRecentlyResolvedTickets(dateLimit);
    }

    /**
     * Met à jour un ticket existant
     * 
     * @param ticketId  l'ID du ticket à mettre à jour
     * @param updateDto les nouvelles données
     * @param userId    l'ID de l'utilisateur effectuant la modification
     * @return le ticket mis à jour
     */
    public Ticket updateTicket(Integer ticketId, CreateTicketDto updateDto, Integer userId) {
        Ticket ticket = ticketDao.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket non trouvé"));

        Utilisateur utilisateur = utilisateurDao.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!utilisateur.isAdmin() &&
                (ticket.getSoumetteur() == null || !ticket.getSoumetteur().getId().equals(userId))) {
            throw new RuntimeException("Vous n'avez pas le droit de modifier ce ticket");
        }

        if (ticket.isResolu()) {
            throw new RuntimeException("Impossible de modifier un ticket résolu");
        }

        ticket.setTitre(updateDto.getTitre());
        ticket.setDescription(updateDto.getDescription());

        if (updateDto.getPrioriteId() != null) {
            Priorite priorite = prioriteDao.findById(updateDto.getPrioriteId())
                    .orElseThrow(() -> new RuntimeException("Priorité non trouvée"));
            ticket.setPriorite(priorite);
        }

        if (updateDto.getCategorieIds() != null) {
            List<Categorie> categories = categorieDao.findAllById(updateDto.getCategorieIds());
            if (categories.size() != updateDto.getCategorieIds().size()) {
                throw new RuntimeException("Une ou plusieurs catégories n'ont pas été trouvées");
            }
            ticket.setCategories(categories);
        }

        return ticketDao.save(ticket);
    }

    /**
     * Supprime un ticket (admin seulement)
     * 
     * @param ticketId l'ID du ticket à supprimer
     * @param adminId  l'ID de l'utilisateur admin
     */
    public void deleteTicket(Integer ticketId, Integer adminId) {
        Utilisateur admin = utilisateurDao.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!admin.isAdmin()) {
            throw new RuntimeException("Seuls les administrateurs peuvent supprimer des tickets");
        }

        Ticket ticket = ticketDao.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket non trouvé"));

        ticketDao.delete(ticket);
    }

    /**
     * Compte le nombre total de tickets
     * 
     * @return le nombre total de tickets
     */
    public long getTotalTicketCount() {
        return ticketDao.count();
    }

    /**
     * Compte le nombre de tickets non résolus
     * 
     * @return le nombre de tickets non résolus
     */
    public long getUnresolvedTicketCount() {
        return ticketDao.countByResoluFalse();
    }

    /**
     * Compte le nombre de tickets résolus
     * 
     * @return le nombre de tickets résolus
     */
    public long getResolvedTicketCount() {
        return ticketDao.countByResoluTrue();
    }

    /**
     * Obtient les statistiques des tickets par priorité
     * 
     * @return la liste des statistiques par priorité
     */
    public List<Object[]> getTicketStatsByPriorite() {
        return ticketDao.getTicketCountByPriorite();
    }

    /**
     * Vérifie si un utilisateur peut accéder à un ticket
     * 
     * @param ticketId l'ID du ticket
     * @param userId   l'ID de l'utilisateur (peut être null pour un utilisateur non
     *                 connecté)
     * @param isAdmin  si l'utilisateur est admin
     * @return true si l'accès est autorisé
     */
    public boolean canAccessTicket(Integer ticketId, Integer userId, boolean isAdmin) {
        Optional<Ticket> ticketOpt = ticketDao.findById(ticketId);
        if (ticketOpt.isEmpty()) {
            return false;
        }

        Ticket ticket = ticketOpt.get();

        if (isAdmin) {
            return true;
        }

        if (userId != null) {
            return true;
        }

        return !ticket.isResolu();
    }
}