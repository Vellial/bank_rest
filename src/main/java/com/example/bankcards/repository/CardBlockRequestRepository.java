package com.example.bankcards.repository;

import com.example.bankcards.entity.CardBlockRequest;
import com.example.bankcards.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CardBlockRequestRepository extends JpaRepository<CardBlockRequest, UUID> {
    List<CardBlockRequest> findByCardIdAndStatus(UUID cardId, RequestStatus status);
}
