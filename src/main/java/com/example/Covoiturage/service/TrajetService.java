package com.example.Covoiturage.service;
import java.time.LocalDateTime;
import com.example.Covoiturage.model.*;

import java.util.List;

public interface TrajetService {
    Trajet proposerTrajet(Chauffeur chauffeur,Vehicule vehicule, String origine, String destination, LocalDateTime heureDepart, int placesTotales, double prixParPlace);
    void cloreTrajet(String trajetId);
    List<Trajet> getTrajets(String origine, String destination);
    List<Trajet>getTrajetsDiponibles();
    void terminerTrajet(String trajetId);
    Trajet getTrajet(String trajetId);
}
