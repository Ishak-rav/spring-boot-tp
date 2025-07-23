package com.example.model;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.view.BasicView;
import com.example.view.CategorieBasicView;
import com.example.view.CategorieDetailView;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "categorie")
public class Categorie {

    public interface Add {}
    public interface Update {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView({BasicView.class, CategorieBasicView.class})
    private Integer id;

    @Column(nullable = false, unique = true)
    @NotBlank(groups = {Add.class, Update.class}, message = "Le nom de la catégorie est obligatoire")
    @Size(min = 2, max = 50, groups = {Add.class, Update.class}, message = "Le nom doit contenir entre 2 et 50 caractères")
    @JsonView({BasicView.class, CategorieBasicView.class})
    private String nom;

    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
    @JsonView(CategorieDetailView.class)
    private List<Ticket> tickets = new ArrayList<>();
}