package com.cinema.reservation.application;

import com.cinema.reservation.domain.Reservation;
import com.cinema.reservation.domain.ReservationId;
import com.cinema.reservation.domain.ReservationStatus;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    void save(Reservation reservation);
    Optional<Reservation> findById(ReservationId id);
    List<Reservation> findByStatus(ReservationStatus status);
}
