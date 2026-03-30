package com.example.Covoiturage.exception;

public class UtilisateurInactifException extends RuntimeException {
    public UtilisateurInactifException(String email) {
        super("Le compte " + email + " est suspendu ou bloqué.");
    }
}