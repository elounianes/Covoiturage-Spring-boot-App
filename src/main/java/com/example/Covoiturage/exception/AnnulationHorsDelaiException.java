package com.example.Covoiturage.exception;

public class AnnulationHorsDelaiException extends RuntimeException {
    public AnnulationHorsDelaiException() {
super("Annulation moins de 24h avant le départ — remboursement partiel appliqué.");
}
}
