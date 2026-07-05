package com.cinema.reservation.infrastructure.persistence;

import com.cinema.reservation.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface ReservationJpaRepository extends JpaRepository<ReservationJpaEntity, UUID> {
    List<ReservationJpaEntity> findByStatus(ReservationStatus status);
}
