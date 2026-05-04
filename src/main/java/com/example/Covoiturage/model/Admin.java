package com.example.Covoiturage.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ADMIN")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

public class Admin extends User {

    public Admin() {}

    public Admin(String nom, String prenom, String email, String phone, String passwordHash) {
        super(nom, prenom, email, phone, passwordHash);
    }
}