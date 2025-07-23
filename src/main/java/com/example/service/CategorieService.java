package com.example.service;

import com.example.dao.CategorieDao;
import com.example.dao.TicketDao;
import com.example.model.Categorie;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategorieService {

    private final CategorieDao categorieDao;
    private final TicketDao ticketDao;

    /**
     * Récupère toutes les catégories triées par nom
     * 
     * @return la liste de toutes les catégories
     */
    public List<Categorie> getAllCategories() {
        return categorieDao.findAllByOrderByNomAsc();
    }

    /**
     * Récupère une catégorie par son ID
     * 
     * @param id l'ID de la catégorie
     * @return la catégorie si elle existe
     */
    public Optional<Categorie> getCategorieById(Integer id) {
        return categorieDao.findById(id);
    }

    /**
     * Récupère une catégorie par son nom
     * 
     * @param nom le nom de la catégorie
     * @return la catégorie si elle existe
     */
    public Optional<Categorie> getCategorieByNom(String nom) {
        return categorieDao.findByNom(nom);
    }

    /**
     * Crée une nouvelle catégorie
     * 
     * @param nom le nom de la catégorie
     * @return la catégorie créée
     */
    public Categorie createCategorie(String nom) {
        // Validation du nom
        if (nom == null || nom.trim().isEmpty()) {
            throw new RuntimeException("Le nom de la catégorie est obligatoire");
        }

        String nomTrimmed = nom.trim();

        // Vérification de l'unicité
        if (categorieDao.existsByNomIgnoreCase(nomTrimmed)) {
            throw new RuntimeException("Une catégorie avec ce nom existe déjà");
        }

        // Création de la catégorie
        Categorie categorie = new Categorie();
        categorie.setNom(nomTrimmed);

        return categorieDao.save(categorie);
    }

    /**
     * Met à jour une catégorie existante
     * 
     * @param id  l'ID de la catégorie à mettre à jour
     * @param nom le nouveau nom
     * @return la catégorie mise à jour
     */
    public Categorie updateCategorie(Integer id, String nom) {
        // Validation du nom
        if (nom == null || nom.trim().isEmpty()) {
            throw new RuntimeException("Le nom de la catégorie est obligatoire");
        }

        String nomTrimmed = nom.trim();

        Categorie categorie = categorieDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec l'ID: " + id));

        Optional<Categorie> existingCategorie = categorieDao.findByNomIgnoreCase(nomTrimmed);
        if (existingCategorie.isPresent() && !existingCategorie.get().getId().equals(id)) {
            throw new RuntimeException("Une catégorie avec ce nom existe déjà");
        }

        categorie.setNom(nomTrimmed);

        return categorieDao.save(categorie);
    }

    /**
     * Supprime une catégorie
     * 
     * @param id l'ID de la catégorie à supprimer
     */
    public void deleteCategorie(Integer id) {
        Categorie categorie = categorieDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec l'ID: " + id));

        long ticketCount = ticketDao.findByCategoriesContaining(categorie).size();
        if (ticketCount > 0) {
            throw new RuntimeException("Impossible de supprimer cette catégorie car elle est utilisée par " +
                    ticketCount + " ticket(s)");
        }

        categorieDao.delete(categorie);
    }

    /**
     * Recherche des catégories par nom (recherche partielle insensible à la casse)
     * 
     * @param keyword le terme de recherche
     * @return la liste des catégories correspondantes
     */
    public List<Categorie> searchCategories(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllCategories();
        }
        return categorieDao.findByNomContainingIgnoreCase(keyword.trim());
    }

    /**
     * Récupère les catégories ayant des tickets associés
     * 
     * @return la liste des catégories utilisées
     */
    public List<Categorie> getCategoriesWithTickets() {
        return categorieDao.findCategoriesWithTickets();
    }

    /**
     * Récupère les catégories sans tickets associés
     * 
     * @return la liste des catégories non utilisées
     */
    public List<Categorie> getCategoriesWithoutTickets() {
        return categorieDao.findCategoriesWithoutTickets();
    }

    /**
     * Récupère les catégories ayant des tickets non résolus
     * 
     * @return la liste des catégories avec tickets non résolus
     */
    public List<Categorie> getCategoriesWithUnresolvedTickets() {
        return categorieDao.findCategoriesWithUnresolvedTickets();
    }

    /**
     * Récupère les statistiques d'utilisation des catégories
     * 
     * @return la liste des catégories avec leur nombre de tickets
     */
    public List<Object[]> getCategorieStats() {
        return categorieDao.getTicketCountByCategorie();
    }

    /**
     * Récupère les statistiques des tickets non résolus par catégorie
     * 
     * @return la liste des catégories avec leur nombre de tickets non résolus
     */
    public List<Object[]> getUnresolvedTicketStatsByCategorie() {
        return categorieDao.getUnresolvedTicketCountByCategorie();
    }

    /**
     * Récupère les catégories triées par popularité (nombre de tickets)
     * 
     * @return la liste des catégories triées par nombre de tickets décroissant
     */
    public List<Categorie> getCategoriesByPopularity() {
        return categorieDao.findAllOrderByTicketCountDesc();
    }

    /**
     * Récupère les catégories populaires (avec un minimum de tickets)
     * 
     * @param minTickets le nombre minimum de tickets
     * @return la liste des catégories populaires
     */
    public List<Categorie> getPopularCategories(int minTickets) {
        return categorieDao.findPopularCategories(minTickets);
    }

    /**
     * Récupère les catégories utilisées par un utilisateur
     * 
     * @param utilisateurId l'ID de l'utilisateur
     * @return la liste des catégories utilisées par cet utilisateur
     */
    public List<Categorie> getCategoriesUsedByUser(Integer utilisateurId) {
        return categorieDao.findCategoriesUsedByUser(utilisateurId);
    }

    /**
     * Vérifie si une catégorie existe par son nom
     * 
     * @param nom le nom à vérifier
     * @return true si la catégorie existe, false sinon
     */
    public boolean existsByNom(String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            return false;
        }
        return categorieDao.existsByNomIgnoreCase(nom.trim());
    }

    /**
     * Compte le nombre total de catégories
     * 
     * @return le nombre de catégories
     */
    public long getTotalCategorieCount() {
        return categorieDao.count();
    }

    /**
     * Compte le nombre de catégories utilisées (ayant au moins un ticket)
     * 
     * @return le nombre de catégories utilisées
     */
    public long getUsedCategorieCount() {
        return categorieDao.findCategoriesWithTickets().size();
    }

    /**
     * Compte le nombre de catégories non utilisées
     * 
     * @return le nombre de catégories non utilisées
     */
    public long getUnusedCategorieCount() {
        return categorieDao.findCategoriesWithoutTickets().size();
    }

    /**
     * Vérifie si une catégorie peut être supprimée
     * 
     * @param id l'ID de la catégorie
     * @return true si la catégorie peut être supprimée, false sinon
     */
    public boolean canDelete(Integer id) {
        Optional<Categorie> categorieOpt = categorieDao.findById(id);
        if (categorieOpt.isEmpty()) {
            return false;
        }

        Categorie categorie = categorieOpt.get();
        return ticketDao.findByCategoriesContaining(categorie).isEmpty();
    }

    /**
     * Récupère le nombre de tickets pour une catégorie donnée
     * 
     * @param id l'ID de la catégorie
     * @return le nombre de tickets
     */
    public long getTicketCountForCategorie(Integer id) {
        Optional<Categorie> categorieOpt = categorieDao.findById(id);
        if (categorieOpt.isEmpty()) {
            return 0;
        }

        Categorie categorie = categorieOpt.get();
        return ticketDao.findByCategoriesContaining(categorie).size();
    }

    /**
     * Initialise les catégories par défaut si aucune n'existe
     */
    public void initializeDefaultCategories() {
        if (categorieDao.count() == 0) {
            String[] defaultCategories = {
                    "Bug", "Amélioration", "Nouvelle fonctionnalité",
                    "Documentation", "Support", "Question",
                    "Performance", "Sécurité", "Infrastructure"
            };

            for (String nom : defaultCategories) {
                try {
                    createCategorie(nom);
                } catch (Exception e) {
                    System.err.println(
                            "Erreur lors de la création de la catégorie par défaut '" + nom + "': " + e.getMessage());
                }
            }
        }
    }

    /**
     * Valide les IDs de catégories pour un ticket
     * 
     * @param categorieIds la liste des IDs de catégories
     * @return true si tous les IDs sont valides
     */
    public boolean validateCategorieIds(List<Integer> categorieIds) {
        if (categorieIds == null || categorieIds.isEmpty()) {
            return true;
        }

        List<Categorie> categories = categorieDao.findAllById(categorieIds);
        return categories.size() == categorieIds.size();
    }

    /**
     * Récupère les catégories par leurs IDs
     * 
     * @param categorieIds la liste des IDs
     * @return la liste des catégories correspondantes
     */
    public List<Categorie> getCategoriesByIds(List<Integer> categorieIds) {
        if (categorieIds == null || categorieIds.isEmpty()) {
            return List.of();
        }
        return categorieDao.findAllById(categorieIds);
    }
}