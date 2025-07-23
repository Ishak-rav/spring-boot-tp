package com.example.dao;

import com.example.model.Priorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrioriteDao extends JpaRepository<Priorite, Integer> {

    /**
     * Trouve une priorité par son nom
     * 
     * @param nom le nom de la priorité
     * @return la priorité si elle existe
     */
    Optional<Priorite> findByNom(String nom);

    /**
     * Trouve une priorité par son nom (insensible à la casse)
     * 
     * @param nom le nom de la priorité
     * @return la priorité si elle existe
     */
    Optional<Priorite> findByNomIgnoreCase(String nom);

    /**
     * Vérifie si une priorité avec ce nom existe déjà
     * 
     * @param nom le nom à vérifier
     * @return true si la priorité existe, false sinon
     */
    boolean existsByNom(String nom);

    /**
     * Vérifie si une priorité avec ce nom existe déjà (insensible à la casse)
     * 
     * @param nom le nom à vérifier
     * @return true si la priorité existe, false sinon
     */
    boolean existsByNomIgnoreCase(String nom);

    /**
     * Trouve les priorités qui ont des tickets associés
     * 
     * @return la liste des priorités ayant des tickets
     */
    @Query("SELECT DISTINCT p FROM Priorite p WHERE SIZE(p.tickets) > 0")
    List<Priorite> findPrioritiesWithTickets();

    /**
     * Trouve les priorités qui n'ont aucun ticket associé
     * 
     * @return la liste des priorités sans tickets
     */
    @Query("SELECT p FROM Priorite p WHERE SIZE(p.tickets) = 0")
    List<Priorite> findPrioritiesWithoutTickets();

    /**
     * Compte le nombre de tickets par priorité
     * 
     * @return la liste des priorités avec le nombre de tickets associés
     */
    @Query("SELECT p.nom, COUNT(t) FROM Priorite p LEFT JOIN p.tickets t GROUP BY p.id, p.nom ORDER BY COUNT(t) DESC")
    List<Object[]> getTicketCountByPriorite();

    /**
     * Trouve les priorités ayant des tickets non résolus
     * 
     * @return la liste des priorités avec des tickets non résolus
     */
    @Query("SELECT DISTINCT p FROM Priorite p JOIN p.tickets t WHERE t.resolu = false")
    List<Priorite> findPrioritiesWithUnresolvedTickets();

    /**
     * Compte le nombre de tickets non résolus par priorité
     * 
     * @return la liste des priorités avec le nombre de tickets non résolus
     */
    @Query("SELECT p.nom, COUNT(t) FROM Priorite p JOIN p.tickets t WHERE t.resolu = false GROUP BY p.id, p.nom ORDER BY COUNT(t) DESC")
    List<Object[]> getUnresolvedTicketCountByPriorite();

    /**
     * Trouve les priorités par nom contenant un mot-clé (recherche insensible à la
     * casse)
     * 
     * @param keyword le mot-clé à rechercher
     * @return la liste des priorités dont le nom contient le mot-clé
     */
    List<Priorite> findByNomContainingIgnoreCase(String keyword);

    /**
     * Trouve toutes les priorités triées par nom
     * 
     * @return la liste des priorités triées alphabétiquement
     */
    List<Priorite> findAllByOrderByNomAsc();

    /**
     * Trouve toutes les priorités triées par nombre de tickets (décroissant)
     * 
     * @return la liste des priorités triées par popularité
     */
    @Query("SELECT p FROM Priorite p LEFT JOIN p.tickets t GROUP BY p.id ORDER BY COUNT(t) DESC")
    List<Priorite> findAllOrderByTicketCountDesc();
}