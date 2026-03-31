package com.example.Covoiturage.model;


import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends User {

    public Admin() {}

    public Admin(String email, String phone, String passwordHash) {
        super(email, phone, passwordHash);
    }
/* 
    @Override
    public void suspendreCompte() {
        throw new UnsupportedOperationException(
            "Use AdminService.suspendreCompte(userId) instead");
    }

    @Override
    public void bloquerUtilisateur() {
        throw new UnsupportedOperationException(
            "Use AdminService.bloquerUtilisateur(userId) instead");
    }*/
}