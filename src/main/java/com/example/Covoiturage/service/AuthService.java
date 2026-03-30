package com.example.Covoiturage.service;
import com.example.Covoiturage.model.*;
import com.example.Covoiturage.model.enums.*;
public interface AuthService {
    User creerCompte(String email,String phone, String mdp,UserRole role);
    User authentifier(String email, String mdp);
    void deconnecter(String userId);
    void suspendreCompte(String userId);
    void bloquerCompte(String userId);

    
}
