package com.cinema.screening.infrastructure.persistence;

import com.cinema.screening.domain.Hall;
import com.cinema.screening.domain.Screening;
import com.cinema.screening.domain.ScreeningId;
import com.cinema.screening.domain.ScreeningNotFoundException;
import com.cinema.screening.application.ScreeningRepository;
import com.cinema.shared.infrastructure.PostgreSQLIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class ScreeningRepositoryAdapterTest extends PostgreSQLIntegrationTest {

    @Autowired
    ScreeningRepository screeningRepository;

    private final Hall hall = new Hall("A", 3, 5);
    private final LocalDateTime startTime = LocalDateTime.now().plusDays(1);

    @Test
    void shouldSaveAndReloadScreening() {
        var screening = new Screening(ScreeningId.newId(), "Inception", hall, startTime);

        screeningRepository.save(screening);
        var loaded = screeningRepository.findById(screening.id()).orElseThrow();

        assertThat(loaded.id()).isEqualTo(screening.id());
        assertThat(loaded.movieTitle()).isEqualTo("Inception");
        assertThat(loaded.hall().name()).isEqualTo("A");
        assertThat(loaded.hall().rows()).isEqualTo(3);
        assertThat(loaded.hall().seatsPerRow()).isEqualTo(5);
        assertThat(loaded.startTime()).isEqualTo(startTime);
    }

    @Test
    void shouldPersistFullSeatGrid() {
        var screening = new Screening(ScreeningId.newId(), "Dune", hall, startTime);

        screeningRepository.save(screening);
        var loaded = screeningRepository.findById(screening.id()).orElseThrow();

        assertThat(loaded.seats()).hasSize(15);
        assertThat(loaded.seats()).allMatch(s -> s.isAvailable());
    }

    @Test
    void shouldPersistSeatStatusAfterReservation() {
        var screening = new Screening(ScreeningId.newId(), "The Matrix", hall, startTime);
        screening.reserveSeat(1, 1);
        screening.reserveSeat(1, 2);

        screeningRepository.save(screening);
        var loaded = screeningRepository.findById(screening.id()).orElseThrow();

        long reserved = loaded.seats().stream().filter(s -> !s.isAvailable()).count();
        long available = loaded.seats().stream().filter(s -> s.isAvailable()).count();
        assertThat(reserved).isEqualTo(2);
        assertThat(available).isEqualTo(13);
    }

    @Test
    void shouldReturnEmptyForUnknownId() {
        var result = screeningRepository.findById(ScreeningId.newId());

        assertThat(result).isEmpty();
    }

    @Test
    void shouldUpdateExistingScreeningOnSave() {
        var screening = new Screening(ScreeningId.newId(), "Interstellar", hall, startTime);
        screeningRepository.save(screening);

        screening.reserveSeat(2, 3);
        screeningRepository.save(screening);

        var loaded = screeningRepository.findById(screening.id()).orElseThrow();
        long reserved = loaded.seats().stream().filter(s -> !s.isAvailable()).count();
        assertThat(reserved).isEqualTo(1);
    }
}
