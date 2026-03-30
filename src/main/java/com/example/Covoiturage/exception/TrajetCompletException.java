package com.example.Covoiturage.exception;

public class TrajetCompletException extends RuntimeException {
    public TrajetCompletException(String trajetId) {
        super("Le trajet " + trajetId + " est complet — aucune place disponible.");
    }
}