package com.cinema.reservation.infrastructure.web;

import com.cinema.reservation.application.ConfirmReservationUseCase;
import com.cinema.reservation.application.ReserveSeatUseCase;
import com.cinema.reservation.domain.CustomerId;
import com.cinema.reservation.domain.ReservationId;
import com.cinema.screening.domain.ScreeningId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reservations")
@Tag(name = "Reservations")
class ReservationController {

    private final ReserveSeatUseCase reserveSeatUseCase;
    private final ConfirmReservationUseCase confirmReservationUseCase;

    ReservationController(ReserveSeatUseCase reserveSeatUseCase, ConfirmReservationUseCase confirmReservationUseCase) {
        this.reserveSeatUseCase = reserveSeatUseCase;
        this.confirmReservationUseCase = confirmReservationUseCase;
    }

    @PostMapping
    @Operation(summary = "Reserve seats for a screening")
    ResponseEntity<ReservationResponse> reserve(@Valid @RequestBody ReservationRequest request) {
        List<ReserveSeatUseCase.SeatPosition> seats = request.seats().stream()
                .map(s -> new ReserveSeatUseCase.SeatPosition(s.row(), s.number()))
                .toList();
        var command = new ReserveSeatUseCase.Command(
                new ScreeningId(request.screeningId()),
                new CustomerId(request.customerId()),
                seats
        );
        ReservationId id = reserveSeatUseCase.execute(command);
        return ResponseEntity
                .created(URI.create("/reservations/" + id))
                .body(new ReservationResponse(id.value()));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm a pending reservation")
    ResponseEntity<Void> confirm(@PathVariable UUID id) {
        confirmReservationUseCase.execute(new ConfirmReservationUseCase.Command(new ReservationId(id)));
        return ResponseEntity.noContent().build();
    }
}
