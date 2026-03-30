package com.example.Covoiturage.exception;

public class PaiementEchouéException extends RuntimeException {
    public PaiementEchouéException(String reason) {
        super("Échec du paiement : " + reason);
    }
}
