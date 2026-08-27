package com.cinema.reservation.application;

import com.cinema.reservation.domain.ReservationStatus;
import com.cinema.screening.application.ScreeningRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ExpireReservationsUseCase {

    private final ReservationRepository reservationRepository;
    private final ScreeningRepository screeningRepository;

    public ExpireReservationsUseCase(ReservationRepository reservationRepository, ScreeningRepository screeningRepository) {
        this.reservationRepository = reservationRepository;
        this.screeningRepository = screeningRepository;
    }

    public void execute() {
        var now = LocalDateTime.now();
        var pending = reservationRepository.findByStatus(ReservationStatus.PENDING);

        for (var reservation : pending) {
            if (!reservation.isExpired(now)) {
                continue;
            }
            reservation.expire();
            var screening = screeningRepository.findById(reservation.screeningId()).orElseThrow();
            reservation.seats().forEach(seat -> screening.releaseSeat(seat.row(), seat.number()));
            reservationRepository.save(reservation);
            screeningRepository.save(screening);
        }
    }
}
