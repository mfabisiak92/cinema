package com.cinema.reservation.application;

import com.cinema.reservation.domain.ReservationId;
import com.cinema.reservation.domain.ReservationNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ConfirmReservationUseCase {

    private final ReservationRepository reservationRepository;

    public ConfirmReservationUseCase(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public void execute(Command command) {
        var reservation = reservationRepository.findById(command.reservationId())
                .orElseThrow(() -> new ReservationNotFoundException(command.reservationId()));
        reservation.confirm();
        reservationRepository.save(reservation);
    }

    public record Command(ReservationId reservationId) {}
}
