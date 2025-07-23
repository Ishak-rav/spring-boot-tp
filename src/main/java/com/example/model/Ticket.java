package com.example.model;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.view.BasicView;
import com.example.view.TicketBasicView;
import com.example.view.TicketDetailView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ticket")
public class Ticket {

    public interface Add {}
    public interface Update {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView({BasicView.class, TicketBasicView.class})
    private Integer id;

    @Column(nullable = false)
    @NotBlank(groups = {Add.class, Update.class}, message = "Le titre est obligatoire")
    @Size(min = 3, max = 100, groups = {Add.class, Update.class}, message = "Le titre doit contenir entre 3 et 100 caractères")
    @JsonView({BasicView.class, TicketBasicView.class})
    private String titre;

    @Column(columnDefinition = "TEXT")
    @NotBlank(groups = {Add.class, Update.class}, message = "La description est obligatoire")
    @Size(min = 10, max = 1000, groups = {Add.class, Update.class}, message = "La description doit contenir entre 10 et 1000 caractères")
    @JsonView({BasicView.class, TicketBasicView.class})
    private String description;

    @Column(nullable = false)
    @JsonView({BasicView.class, TicketBasicView.class})
    private Boolean resolu = false;

    @Column(name = "date_creation", nullable = false)
    @JsonView({BasicView.class, TicketBasicView.class})
    private LocalDateTime dateCreation;

    @Column(name = "date_resolution")
    @JsonView({BasicView.class, TicketBasicView.class})
    private LocalDateTime dateResolution;

    @ManyToOne
    @JoinColumn(name = "soumetteur_id")
    @JsonView(TicketDetailView.class)
    private Utilisateur soumetteur;

    @ManyToOne
    @JoinColumn(name = "resolveur_id")
    @JsonView(TicketDetailView.class)
    private Utilisateur resolveur;

    @ManyToOne
    @JoinColumn(name = "priorite_id", nullable = false)
    @NotNull(groups = {Add.class, Update.class}, message = "La priorité est obligatoire")
    @JsonView({BasicView.class, TicketBasicView.class})
    private Priorite priorite;

    @ManyToMany
    @JoinTable(
        name = "ticket_categorie",
        joinColumns = @JoinColumn(name = "ticket_id"),
        inverseJoinColumns = @JoinColumn(name = "categorie_id")
    )
    @JsonView(TicketDetailView.class)
    private List<Categorie> categories = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
    }

    public boolean isResolu() {
        return resolu != null && resolu;
    }

    public void marquerCommeResolu(Utilisateur resolveur) {
        this.resolu = true;
        this.resolveur = resolveur;
        this.dateResolution = LocalDateTime.now();
    }
}