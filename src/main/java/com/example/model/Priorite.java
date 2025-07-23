package com.example.model;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.view.BasicView;
import com.example.view.PrioriteBasicView;
import com.example.view.PrioriteDetailView;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "priorite")
public class Priorite {

    public interface Add {}
    public interface Update {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView({BasicView.class, PrioriteBasicView.class})
    private Integer id;

    @Column(nullable = false, unique = true)
    @NotBlank(groups = {Add.class, Update.class}, message = "Le nom de la priorité est obligatoire")
    @Size(min = 2, max = 50, groups = {Add.class, Update.class}, message = "Le nom doit contenir entre 2 et 50 caractères")
    @JsonView({BasicView.class, PrioriteBasicView.class})
    private String nom;

    @OneToMany(mappedBy = "priorite", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonView(PrioriteDetailView.class)
    private List<Ticket> tickets = new ArrayList<>();
}