package com.example.Covoiturage.exception;

    public class CompteExistantException extends RuntimeException {
    public CompteExistantException() {
        super("Compte existant");
    }

}
