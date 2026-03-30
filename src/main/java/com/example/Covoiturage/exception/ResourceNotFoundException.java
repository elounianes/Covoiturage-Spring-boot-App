package com.example.Covoiturage.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, String id) {
        super(resource + " introuvable avec l'id : " + id);
    }
}