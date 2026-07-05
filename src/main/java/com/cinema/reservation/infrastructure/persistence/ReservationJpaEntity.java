package com.cinema.reservation.infrastructure.persistence;

import com.cinema.reservation.domain.CustomerId;
import com.cinema.reservation.domain.Reservation;
import com.cinema.reservation.domain.ReservationId;
import com.cinema.reservation.domain.ReservationStatus;
import com.cinema.screening.domain.ScreeningId;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "reservations")
class ReservationJpaEntity {

    @Id
    private UUID id;

    @Column(name = "screening_id", nullable = false)
    private UUID screeningId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "reservation_seats", joinColumns = @JoinColumn(name = "reservation_id"))
    private List<ReservationSeatEmbeddable> seats = new ArrayList<>();

    protected ReservationJpaEntity() {}

    static ReservationJpaEntity from(Reservation reservation) {
        var entity = new ReservationJpaEntity();
        entity.id = reservation.id().value();
        entity.screeningId = reservation.screeningId().value();
        entity.customerId = reservation.customerId().value();
        entity.status = reservation.status();
        entity.expiresAt = reservation.expiresAt();
        entity.seats = reservation.seats().stream()
                .map(ReservationSeatEmbeddable::from)
                .toList();
        return entity;
    }

    Reservation toDomain() {
        var domainSeats = seats.stream().map(ReservationSeatEmbeddable::toDomain).toList();
        return Reservation.reconstitute(
                new ReservationId(id),
                new ScreeningId(screeningId),
                new CustomerId(customerId),
                domainSeats,
                expiresAt,
                status
        );
    }
}
