package com.example.repository;

import com.example.model.SlotDetails;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SlotDetailsRepository extends JpaRepository<SlotDetails, Long> {

    List<SlotDetails> findByOwnerEmail(String ownerEmail);
    // Add custom queries if needed

}
