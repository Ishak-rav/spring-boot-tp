package com.example.dao;

import com.example.model.Ticket;
import com.example.model.Utilisateur;
import com.example.model.Priorite;
import com.example.model.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TicketDao extends JpaRepository<Ticket, Integer> {
    
    /**
     * Trouve tous les tickets non résolus
     * @return la liste des tickets non résolus
     */
    List<Ticket> findByResoluFalse();
    
    /**
     * Trouve tous les tickets résolus
     * @return la liste des tickets résolus
     */
    List<Ticket> findByResoluTrue();
    
    /**
     * Trouve tous les tickets par statut de résolution
     * @param resolu le statut de résolution
     * @return la liste des tickets avec le statut donné
     */
    List<Ticket> findByResolu(Boolean resolu);
    
    /**
     * Trouve tous les tickets soumis par un utilisateur
     * @param soumetteur l'utilisateur soumetteur
     * @return la liste des tickets soumis par l'utilisateur
     */
    List<Ticket> findBySoumetteur(Utilisateur soumetteur);
    
    /**
     * Trouve tous les tickets résolus par un utilisateur
     * @param resolveur l'utilisateur résolveur
     * @return la liste des tickets résolus par l'utilisateur
     */
    List<Ticket> findByResolveur(Utilisateur resolveur);
    
    /**
     * Trouve tous les tickets par priorité
     * @param priorite la priorité
     * @return la liste des tickets avec cette priorité
     */
    List<Ticket> findByPriorite(Priorite priorite);
    
    /**
     * Trouve tous les tickets contenant une catégorie spécifique
     * @param categorie la catégorie
     * @return la liste des tickets contenant cette catégorie
     */
    List<Ticket> findByCategoriesContaining(Categorie categorie);
    
    /**
     * Trouve tous les tickets non résolus d'une priorité donnée
     * @param priorite la priorité
     * @return la liste des tickets non résolus de cette priorité
     */
    List<Ticket> findByResoluFalseAndPriorite(Priorite priorite);
    
    /**
     * Trouve tous les tickets créés après une date donnée
     * @param date la date de création minimum
     * @return la liste des tickets créés après cette date
     */
    List<Ticket> findByDateCreationAfter(LocalDateTime date);
    
    /**
     * Trouve tous les tickets créés entre deux dates
     * @param dateDebut la date de début
     * @param dateFin la date de fin
     * @return la liste des tickets créés dans cette période
     */
    List<Ticket> findByDateCreationBetween(LocalDateTime dateDebut, LocalDateTime dateFin);
    
    /**
     * Compte le nombre de tickets non résolus
     * @return le nombre de tickets non résolus
     */
    long countByResoluFalse();
    
    /**
     * Compte le nombre de tickets résolus
     * @return le nombre de tickets résolus
     */
    long countByResoluTrue();
    
    /**
     * Trouve les tickets par titre contenant un mot-clé (recherche insensible à la casse)
     * @param keyword le mot-clé à rechercher
     * @return la liste des tickets dont le titre contient le mot-clé
     */
    List<Ticket> findByTitreContainingIgnoreCase(String keyword);
    
    /**
     * Trouve les tickets par description contenant un mot-clé (recherche insensible à la casse)
     * @param keyword le mot-clé à rechercher
     * @return la liste des tickets dont la description contient le mot-clé
     */
    List<Ticket> findByDescriptionContainingIgnoreCase(String keyword);
    
    /**
     * Recherche dans le titre ou la description
     * @param keyword le mot-clé à rechercher
     * @return la liste des tickets contenant le mot-clé dans le titre ou la description
     */
    @Query("SELECT t FROM Ticket t WHERE LOWER(t.titre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Ticket> searchByTitreOrDescription(@Param("keyword") String keyword);
    
    /**
     * Trouve les tickets non résolus les plus anciens
     * @return la liste des tickets non résolus triés par date de création croissante
     */
    @Query("SELECT t FROM Ticket t WHERE t.resolu = false ORDER BY t.dateCreation ASC")
    List<Ticket> findOldestUnresolvedTickets();
    
    /**
     * Trouve les tickets résolus récemment
     * @param nombreJours le nombre de jours à considérer
     * @return la liste des tickets résolus dans les derniers jours
     */
    @Query("SELECT t FROM Ticket t WHERE t.resolu = true AND t.dateResolution >= :dateLimit ORDER BY t.dateResolution DESC")
    List<Ticket> findRecentlyResolvedTickets(@Param("dateLimit") LocalDateTime dateLimit);
    
    /**
     * Statistiques par priorité
     * @return la liste des tickets groupés par priorité avec comptage
     */
    @Query("SELECT t.priorite.nom, COUNT(t) FROM Ticket t GROUP BY t.priorite.nom")
    List<Object[]> getTicketCountByPriorite();
    
    /**
     * Trouve les tickets assignés à un utilisateur (soumis ou à résoudre)
     * @param utilisateur l'utilisateur
     * @return la liste des tickets liés à cet utilisateur
     */
    @Query("SELECT t FROM Ticket t WHERE t.soumetteur = :utilisateur OR t.resolveur = :utilisateur")
    List<Ticket> findTicketsByUtilisateur(@Param("utilisateur") Utilisateur utilisateur);
}