package com.example.dao;

import com.example.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurDao extends JpaRepository<Utilisateur, Integer> {

    /**
     * Trouve un utilisateur par son pseudo
     * 
     * @param pseudo le pseudo de l'utilisateur
     * @return l'utilisateur s'il existe
     */
    Optional<Utilisateur> findByPseudo(String pseudo);

    /**
     * Trouve tous les utilisateurs administrateurs
     * 
     * @return la liste des administrateurs
     */
    List<Utilisateur> findByAdminTrue();

    /**
     * Trouve tous les utilisateurs non-administrateurs
     * 
     * @return la liste des utilisateurs non-admin
     */
    List<Utilisateur> findByAdminFalse();

    /**
     * Vérifie si un pseudo existe déjà
     * 
     * @param pseudo le pseudo à vérifier
     * @return true si le pseudo existe, false sinon
     */
    boolean existsByPseudo(String pseudo);

    /**
     * Compte le nombre d'administrateurs
     * 
     * @return le nombre d'administrateurs
     */
    @Query("SELECT COUNT(u) FROM Utilisateur u WHERE u.admin = true")
    long countAdmins();

    /**
     * Trouve les utilisateurs qui ont soumis des tickets
     * 
     * @return la liste des utilisateurs ayant soumis des tickets
     */
    @Query("SELECT DISTINCT u FROM Utilisateur u WHERE SIZE(u.ticketsSoumis) > 0")
    List<Utilisateur> findUsersWithSubmittedTickets();

    /**
     * Trouve les utilisateurs qui ont résolu des tickets
     * 
     * @return la liste des utilisateurs ayant résolu des tickets
     */
    @Query("SELECT DISTINCT u FROM Utilisateur u WHERE SIZE(u.ticketsResolus) > 0")
    List<Utilisateur> findUsersWithResolvedTickets();
}