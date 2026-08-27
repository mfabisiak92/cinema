package com.cinema.reservation.infrastructure.scheduling;

import com.cinema.reservation.application.ExpireReservationsUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservationExpiryScheduler {

    private final ExpireReservationsUseCase expireReservationsUseCase;

    public ReservationExpiryScheduler(ExpireReservationsUseCase expireReservationsUseCase) {
        this.expireReservationsUseCase = expireReservationsUseCase;
    }

    @Scheduled(fixedDelay = 60_000)
    public void expireReservations() {
        expireReservationsUseCase.execute();
    }
}
