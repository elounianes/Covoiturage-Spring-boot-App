package com.example.Covoiturage.model;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User destinataire;

    private String type;    // "EMAIL" or "SMS"
    private String message;
    private boolean lu = false;
    private LocalDateTime dateEnvoi = LocalDateTime.now();

    public Notification() {}

    public Notification(User destinataire, String type, String message) {
        this.destinataire = destinataire;
        this.type = type;
        this.message = message;
        this.dateEnvoi = LocalDateTime.now();
    }

    public String getId() { return id; }
    public User getDestinataire() { return destinataire; }
    public void setDestinataire(User u) { this.destinataire = u; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isLu() { return lu; }
    public void setLu(boolean lu) { this.lu = lu; }
    public LocalDateTime getDateEnvoi() { return dateEnvoi; }

    @Override
    public String toString() {
        return "Notification{type='" + type + "', lu=" + lu +
               ", date=" + dateEnvoi + ", msg='" + message + "'}";
    }
}