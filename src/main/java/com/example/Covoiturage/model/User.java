package com.example.Covoiturage.model;

import com.example.Covoiturage.model.enums.UserRole;
import com.example.Covoiturage.model.enums.UserStatus;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIF;

    private int failedLoginAttempts = 0;

    protected User() {}

    protected User(String email, String phone, String passwordHash) {
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        if (this instanceof Passager) return UserRole.PASSAGER;
        if (this instanceof Chauffeur) return UserRole.CHAUFFEUR;
        if (this instanceof Admin) return UserRole.ADMIN;
        return null;
    }

    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.status = UserStatus.BLOQUE;
        }
    }

    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
    }

    public void suspendreCompte() {
        this.status = UserStatus.SUSPENDU;
    }

    public void bloquerUtilisateur() {
        this.status = UserStatus.BLOQUE;
    }

    public void deconnecter() {
        System.out.println("[AUTH] Déconnexion de " + email);
    }

    public void notifierEmail(String message) {
        System.out.println("[EMAIL → " + email + "] " + message);
    }

    public void notifierSMS(String message) {
        System.out.println("[SMS → " + phone + "] " + message);
    }

    public boolean isActif() {
        return this.status == UserStatus.ACTIF;
    }

    public String getId() { return id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int n) { this.failedLoginAttempts = n; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User u)) return false;
        return id != null && id.equals(u.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
               "{id='" + id + "', email='" + email + "', status=" + status + "}";
    }
}