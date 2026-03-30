package com.example.Covoiturage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Covoiturage.model.PaymentTransaction;

public interface PaymentTransactionRepository
    extends JpaRepository<PaymentTransaction, String> {}