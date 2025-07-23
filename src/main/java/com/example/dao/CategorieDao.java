package com.example.dao;

import com.example.model.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategorieDao extends JpaRepository<Categorie, Integer> {
    
    /**
     * Trouve une catégorie par son nom
     * @param nom le nom de la catégorie
     * @return la catégorie si elle existe
     */
    Optional<Categorie> findByNom(String nom);
    
    /**
     * Trouve une catégorie par son nom (insensible à la casse)
     * @param nom le nom de la catégorie
     * @return la catégorie si elle existe
     */
    Optional<Categorie> findByNomIgnoreCase(String nom);
    
    /**
     * Vérifie si une catégorie avec ce nom existe déjà
     * @param nom le nom à vérifier
     * @return true si la catégorie existe, false sinon
     */
    boolean existsByNom(String nom);
    
    /**
     * Vérifie si une catégorie avec ce nom existe déjà (insensible à la casse)
     * @param nom le nom à vérifier
     * @return true si la catégorie existe, false sinon
     */
    boolean existsByNomIgnoreCase(String nom);
    
    /**
     * Trouve les catégories qui ont des tickets associés
     * @return la liste des catégories ayant des tickets
     */
    @Query("SELECT DISTINCT c FROM Categorie c WHERE SIZE(c.tickets) > 0")
    List<Categorie> findCategoriesWithTickets();
    
    /**
     * Trouve les catégories qui n'ont aucun ticket associé
     * @return la liste des catégories sans tickets
     */
    @Query("SELECT c FROM Categorie c WHERE SIZE(c.tickets) = 0")
    List<Categorie> findCategoriesWithoutTickets();
    
    /**
     * Compte le nombre de tickets par catégorie
     * @return la liste des catégories avec le nombre de tickets associés
     */
    @Query("SELECT c.nom, COUNT(t) FROM Categorie c LEFT JOIN c.tickets t GROUP BY c.id, c.nom ORDER BY COUNT(t) DESC")
    List<Object[]> getTicketCountByCategorie();
    
    /**
     * Trouve les catégories ayant des tickets non résolus
     * @return la liste des catégories avec des tickets non résolus
     */
    @Query("SELECT DISTINCT c FROM Categorie c JOIN c.tickets t WHERE t.resolu = false")
    List<Categorie> findCategoriesWithUnresolvedTickets();
    
    /**
     * Compte le nombre de tickets non résolus par catégorie
     * @return la liste des catégories avec le nombre de tickets non résolus
     */
    @Query("SELECT c.nom, COUNT(t) FROM Categorie c JOIN c.tickets t WHERE t.resolu = false GROUP BY c.id, c.nom ORDER BY COUNT(t) DESC")
    List<Object[]> getUnresolvedTicketCountByCategorie();
    
    /**
     * Trouve les catégories par nom contenant un mot-clé (recherche insensible à la casse)
     * @param keyword le mot-clé à rechercher
     * @return la liste des catégories dont le nom contient le mot-clé
     */
    List<Categorie> findByNomContainingIgnoreCase(String keyword);
    
    /**
     * Trouve toutes les catégories triées par nom
     * @return la liste des catégories triées alphabétiquement
     */
    List<Categorie> findAllByOrderByNomAsc();
    
    /**
     * Trouve toutes les catégories triées par nombre de tickets (décroissant)
     * @return la liste des catégories triées par popularité
     */
    @Query("SELECT c FROM Categorie c LEFT JOIN c.tickets t GROUP BY c.id ORDER BY COUNT(t) DESC")
    List<Categorie> findAllOrderByTicketCountDesc();
    
    /**
     * Trouve les catégories les plus utilisées (avec au moins un certain nombre de tickets)
     * @param minTickets le nombre minimum de tickets
     * @return la liste des catégories populaires
     */
    @Query("SELECT c FROM Categorie c WHERE SIZE(c.tickets) >= :minTickets ORDER BY SIZE(c.tickets) DESC")
    List<Categorie> findPopularCategories(@Param("minTickets") int minTickets);
    
    /**
     * Trouve les catégories utilisées par un utilisateur spécifique
     * @param utilisateurId l'ID de l'utilisateur
     * @return la liste des catégories utilisées par cet utilisateur
     */
    @Query("SELECT DISTINCT c FROM Categorie c JOIN c.tickets t WHERE t.soumetteur.id = :utilisateurId")
    List<Categorie> findCategoriesUsedByUser(@Param("utilisateurId") Integer utilisateurId);
}