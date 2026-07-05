package com.cinema.screening.infrastructure.persistence;

import com.cinema.screening.domain.Hall;
import com.cinema.screening.domain.Screening;
import com.cinema.screening.domain.ScreeningId;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "screenings")
class ScreeningJpaEntity {

    @Id
    private UUID id;

    @Column(name = "movie_title", nullable = false)
    private String movieTitle;

    @Column(name = "hall_name", nullable = false)
    private String hallName;

    @Column(name = "hall_rows", nullable = false)
    private int hallRows;

    @Column(name = "hall_seats_per_row", nullable = false)
    private int hallSeatsPerRow;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "screening_seats", joinColumns = @JoinColumn(name = "screening_id"))
    private List<ScreeningSeatEmbeddable> seats = new ArrayList<>();

    protected ScreeningJpaEntity() {}

    static ScreeningJpaEntity from(Screening screening) {
        var entity = new ScreeningJpaEntity();
        entity.id = screening.id().value();
        entity.movieTitle = screening.movieTitle();
        entity.hallName = screening.hall().name();
        entity.hallRows = screening.hall().rows();
        entity.hallSeatsPerRow = screening.hall().seatsPerRow();
        entity.startTime = screening.startTime();
        entity.seats = screening.seats().stream()
                .map(ScreeningSeatEmbeddable::from)
                .toList();
        return entity;
    }

    Screening toDomain() {
        var hall = new Hall(hallName, hallRows, hallSeatsPerRow);
        var domainSeats = seats.stream().map(ScreeningSeatEmbeddable::toDomain).toList();
        return Screening.reconstitute(new ScreeningId(id), movieTitle, hall, startTime, domainSeats);
    }
}
