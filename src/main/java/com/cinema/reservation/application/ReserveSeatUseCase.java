package com.cinema.reservation.application;

import com.cinema.reservation.domain.CustomerId;
import com.cinema.reservation.domain.Reservation;
import com.cinema.reservation.domain.ReservationId;
import com.cinema.screening.application.ScreeningRepository;
import com.cinema.screening.domain.Seat;
import com.cinema.screening.domain.ScreeningId;
import com.cinema.screening.domain.ScreeningNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReserveSeatUseCase {

    private final ScreeningRepository screeningRepository;
    private final ReservationRepository reservationRepository;

    public ReserveSeatUseCase(ScreeningRepository screeningRepository, ReservationRepository reservationRepository) {
        this.screeningRepository = screeningRepository;
        this.reservationRepository = reservationRepository;
    }

    public ReservationId execute(Command command) {
        var screening = screeningRepository.findById(command.screeningId())
                .orElseThrow(() -> new ScreeningNotFoundException(command.screeningId()));

        List<Seat> requestedSeats = command.seats().stream()
                .map(pos -> screening.seats().stream()
                        .filter(s -> s.row() == pos.row() && s.number() == pos.number() && s.isAvailable())
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Seat [%d,%d] not found or unavailable in screening %s"
                                        .formatted(pos.row(), pos.number(), command.screeningId()))))
                .toList();

        requestedSeats.forEach(seat -> screening.reserveSeat(seat.row(), seat.number()));

        var id = ReservationId.newId();
        var reservation = new Reservation(
                id,
                command.screeningId(),
                command.customerId(),
                requestedSeats,
                LocalDateTime.now().plusMinutes(15)
        );
        screeningRepository.save(screening);
        reservationRepository.save(reservation);
        return id;
    }

    public record SeatPosition(int row, int number) {}

    public record Command(ScreeningId screeningId, CustomerId customerId, List<SeatPosition> seats) {}
}
