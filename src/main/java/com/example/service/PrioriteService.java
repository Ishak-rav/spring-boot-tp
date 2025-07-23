package com.example.service;

import com.example.dao.PrioriteDao;
import com.example.dao.TicketDao;
import com.example.model.Priorite;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PrioriteService {

    private final PrioriteDao prioriteDao;
    private final TicketDao ticketDao;

    /**
     * Récupère toutes les priorités triées par nom
     * 
     * @return la liste de toutes les priorités
     */
    public List<Priorite> getAllPriorites() {
        return prioriteDao.findAllByOrderByNomAsc();
    }

    /**
     * Récupère une priorité par son ID
     * 
     * @param id l'ID de la priorité
     * @return la priorité si elle existe
     */
    public Optional<Priorite> getPrioriteById(Integer id) {
        return prioriteDao.findById(id);
    }

    /**
     * Récupère une priorité par son nom
     * 
     * @param nom le nom de la priorité
     * @return la priorité si elle existe
     */
    public Optional<Priorite> getPrioriteByNom(String nom) {
        return prioriteDao.findByNom(nom);
    }

    /**
     * Crée une nouvelle priorité
     * 
     * @param nom le nom de la priorité
     * @return la priorité créée
     */
    public Priorite createPriorite(String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            throw new RuntimeException("Le nom de la priorité est obligatoire");
        }

        String nomTrimmed = nom.trim();

        if (prioriteDao.existsByNomIgnoreCase(nomTrimmed)) {
            throw new RuntimeException("Une priorité avec ce nom existe déjà");
        }

        Priorite priorite = new Priorite();
        priorite.setNom(nomTrimmed);

        return prioriteDao.save(priorite);
    }

    /**
     * Met à jour une priorité existante
     * 
     * @param id  l'ID de la priorité à mettre à jour
     * @param nom le nouveau nom
     * @return la priorité mise à jour
     */
    public Priorite updatePriorite(Integer id, String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            throw new RuntimeException("Le nom de la priorité est obligatoire");
        }

        String nomTrimmed = nom.trim();

        Priorite priorite = prioriteDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Priorité non trouvée avec l'ID: " + id));

        Optional<Priorite> existingPriorite = prioriteDao.findByNomIgnoreCase(nomTrimmed);
        if (existingPriorite.isPresent() && !existingPriorite.get().getId().equals(id)) {
            throw new RuntimeException("Une priorité avec ce nom existe déjà");
        }

        priorite.setNom(nomTrimmed);

        return prioriteDao.save(priorite);
    }

    /**
     * Supprime une priorité
     * 
     * @param id l'ID de la priorité à supprimer
     */
    public void deletePriorite(Integer id) {
        Priorite priorite = prioriteDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Priorité non trouvée avec l'ID: " + id));

        long ticketCount = ticketDao.findByPriorite(priorite).size();
        if (ticketCount > 0) {
            throw new RuntimeException("Impossible de supprimer cette priorité car elle est utilisée par " +
                    ticketCount + " ticket(s)");
        }

        prioriteDao.delete(priorite);
    }

    /**
     * Recherche des priorités par nom (recherche partielle insensible à la casse)
     * 
     * @param keyword le terme de recherche
     * @return la liste des priorités correspondantes
     */
    public List<Priorite> searchPriorites(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllPriorites();
        }
        return prioriteDao.findByNomContainingIgnoreCase(keyword.trim());
    }

    /**
     * Récupère les priorités ayant des tickets associés
     * 
     * @return la liste des priorités utilisées
     */
    public List<Priorite> getPrioritesWithTickets() {
        return prioriteDao.findPrioritiesWithTickets();
    }

    /**
     * Récupère les priorités sans tickets associés
     * 
     * @return la liste des priorités non utilisées
     */
    public List<Priorite> getPrioritesWithoutTickets() {
        return prioriteDao.findPrioritiesWithoutTickets();
    }

    /**
     * Récupère les priorités ayant des tickets non résolus
     * 
     * @return la liste des priorités avec tickets non résolus
     */
    public List<Priorite> getPrioritesWithUnresolvedTickets() {
        return prioriteDao.findPrioritiesWithUnresolvedTickets();
    }

    /**
     * Récupère les statistiques d'utilisation des priorités
     * 
     * @return la liste des priorités avec leur nombre de tickets
     */
    public List<Object[]> getPrioriteStats() {
        return prioriteDao.getTicketCountByPriorite();
    }

    /**
     * Récupère les statistiques des tickets non résolus par priorité
     * 
     * @return la liste des priorités avec leur nombre de tickets non résolus
     */
    public List<Object[]> getUnresolvedTicketStatsByPriorite() {
        return prioriteDao.getUnresolvedTicketCountByPriorite();
    }

    /**
     * Récupère les priorités triées par popularité (nombre de tickets)
     * 
     * @return la liste des priorités triées par nombre de tickets décroissant
     */
    public List<Priorite> getPrioritiesByPopularity() {
        return prioriteDao.findAllOrderByTicketCountDesc();
    }

    /**
     * Vérifie si une priorité existe par son nom
     * 
     * @param nom le nom à vérifier
     * @return true si la priorité existe, false sinon
     */
    public boolean existsByNom(String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            return false;
        }
        return prioriteDao.existsByNomIgnoreCase(nom.trim());
    }

    /**
     * Compte le nombre total de priorités
     * 
     * @return le nombre de priorités
     */
    public long getTotalPrioriteCount() {
        return prioriteDao.count();
    }

    /**
     * Compte le nombre de priorités utilisées (ayant au moins un ticket)
     * 
     * @return le nombre de priorités utilisées
     */
    public long getUsedPrioriteCount() {
        return prioriteDao.findPrioritiesWithTickets().size();
    }

    /**
     * Compte le nombre de priorités non utilisées
     * 
     * @return le nombre de priorités non utilisées
     */
    public long getUnusedPrioriteCount() {
        return prioriteDao.findPrioritiesWithoutTickets().size();
    }

    /**
     * Vérifie si une priorité peut être supprimée
     * 
     * @param id l'ID de la priorité
     * @return true si la priorité peut être supprimée, false sinon
     */
    public boolean canDelete(Integer id) {
        Optional<Priorite> prioriteOpt = prioriteDao.findById(id);
        if (prioriteOpt.isEmpty()) {
            return false;
        }

        Priorite priorite = prioriteOpt.get();
        return ticketDao.findByPriorite(priorite).isEmpty();
    }

    /**
     * Récupère le nombre de tickets pour une priorité donnée
     * 
     * @param id l'ID de la priorité
     * @return le nombre de tickets
     */
    public long getTicketCountForPriorite(Integer id) {
        Optional<Priorite> prioriteOpt = prioriteDao.findById(id);
        if (prioriteOpt.isEmpty()) {
            return 0;
        }

        Priorite priorite = prioriteOpt.get();
        return ticketDao.findByPriorite(priorite).size();
    }

    /**
     * Initialise les priorités par défaut si aucune n'existe
     */
    public void initializeDefaultPriorites() {
        if (prioriteDao.count() == 0) {
            String[] defaultPriorites = { "Faible", "Normale", "Haute", "Critique", "Urgente" };

            for (String nom : defaultPriorites) {
                try {
                    createPriorite(nom);
                } catch (Exception e) {
                    System.err.println(
                            "Erreur lors de la création de la priorité par défaut '" + nom + "': " + e.getMessage());
                }
            }
        }
    }
}