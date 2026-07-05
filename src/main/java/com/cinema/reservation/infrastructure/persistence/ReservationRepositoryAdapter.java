package com.cinema.reservation.infrastructure.persistence;

import com.cinema.reservation.application.ReservationRepository;
import com.cinema.reservation.domain.Reservation;
import com.cinema.reservation.domain.ReservationId;
import com.cinema.reservation.domain.ReservationStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ReservationRepositoryAdapter implements ReservationRepository {

    private final ReservationJpaRepository jpaRepository;

    public ReservationRepositoryAdapter(ReservationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Reservation reservation) {
        jpaRepository.save(ReservationJpaEntity.from(reservation));
    }

    @Override
    public Optional<Reservation> findById(ReservationId id) {
        return jpaRepository.findById(id.value()).map(ReservationJpaEntity::toDomain);
    }

    @Override
    public List<Reservation> findByStatus(ReservationStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(ReservationJpaEntity::toDomain)
                .toList();
    }
}
