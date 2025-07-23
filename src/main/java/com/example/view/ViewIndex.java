package com.example.view;

public class ViewIndex {

    public interface BasicView {
    }

    public interface DetailView {
    }

    public interface PublicView {
    }

    public interface SummaryView {
    }

    public interface UtilisateurBasicView extends BasicView {
    }

    public interface UtilisateurDetailView extends UtilisateurBasicView {
    }

    public interface UtilisateurSummaryView extends SummaryView {
    }

    public interface UtilisateurPublicView extends PublicView {
    }

    public interface TicketBasicView extends BasicView {
    }

    public interface TicketDetailView extends TicketBasicView {
    }

    public interface TicketSummaryView extends SummaryView {
    }

    public interface TicketPublicView extends PublicView {
    }

    public interface PrioriteBasicView extends BasicView {
    }

    public interface PrioriteDetailView extends PrioriteBasicView {
    }

    public interface PrioriteSummaryView extends SummaryView {
    }

    public interface CategorieBasicView extends BasicView {
    }

    public interface CategorieDetailView extends CategorieBasicView {
    }

    public interface CategorieSummaryView extends SummaryView {
    }
}