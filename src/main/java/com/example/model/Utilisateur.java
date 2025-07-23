package com.example.model;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.view.BasicView;
import com.example.view.UtilisateurBasicView;
import com.example.view.UtilisateurDetailView;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "utilisateur")
public class Utilisateur {

    public interface Add {}
    public interface Update {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView({BasicView.class, UtilisateurBasicView.class})
    private Integer id;

    @Column(nullable = false, unique = true)
    @NotBlank(groups = {Add.class, Update.class}, message = "Le pseudo est obligatoire")
    @Size(min = 3, max = 50, groups = {Add.class, Update.class}, message = "Le pseudo doit contenir entre 3 et 50 caractères")
    @JsonView({BasicView.class, UtilisateurBasicView.class})
    private String pseudo;

    @Column(nullable = false)
    @NotBlank(groups = {Add.class}, message = "Le mot de passe est obligatoire")
    @Size(min = 6, groups = {Add.class}, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;

    @Column(nullable = false)
    @JsonView({BasicView.class, UtilisateurBasicView.class})
    private Boolean admin = false;

    @OneToMany(mappedBy = "soumetteur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonView(UtilisateurDetailView.class)
    private List<Ticket> ticketsSoumis = new ArrayList<>();

    @OneToMany(mappedBy = "resolveur", fetch = FetchType.LAZY)
    @JsonView(UtilisateurDetailView.class)
    private List<Ticket> ticketsResolus = new ArrayList<>();

    public boolean isAdmin() {
        return admin != null && admin;
    }
}