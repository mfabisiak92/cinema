package com.cinema.screening.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScreeningTest {

    private static final ScreeningId ID = new ScreeningId(UUID.randomUUID());
    private static final Hall HALL = new Hall("A", 3, 5);
    private static final LocalDateTime FUTURE = LocalDateTime.now().plusDays(1);

    @Test
    void shouldCreateScreeningWithValidData() {
        var screening = new Screening(ID, "Inception", HALL, FUTURE);

        assertThat(screening.id()).isEqualTo(ID);
        assertThat(screening.movieTitle()).isEqualTo("Inception");
        assertThat(screening.hall()).isEqualTo(HALL);
        assertThat(screening.startTime()).isEqualTo(FUTURE);
    }

    @Test
    void shouldNotAllowScreeningInThePast() {
        var past = LocalDateTime.now().minusMinutes(1);

        assertThatThrownBy(() -> new Screening(ID, "Inception", HALL, past))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("past");
    }

    @Test
    void shouldNotAllowBlankMovieTitle() {
        assertThatThrownBy(() -> new Screening(ID, "", HALL, FUTURE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Screening(ID, "  ", HALL, FUTURE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldInitializeSeatLayoutFromHall() {
        var screening = new Screening(ID, "Inception", HALL, FUTURE);

        assertThat(screening.seats()).containsExactlyInAnyOrderElementsOf(HALL.seats());
    }

    @Test
    void shouldHaveAllSeatsAvailableInitially() {
        var screening = new Screening(ID, "Inception", HALL, FUTURE);

        assertThat(screening.seats()).allMatch(Seat::isAvailable);
    }

    @Test
    void shouldEmitScreeningCreatedEventOnCreation() {
        var screening = new Screening(ID, "Inception", HALL, FUTURE);

        var events = screening.pullDomainEvents();

        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(ScreeningCreated.class);

        var event = (ScreeningCreated) events.getFirst();
        assertThat(event.screeningId()).isEqualTo(ID);
        assertThat(event.movieTitle()).isEqualTo("Inception");
    }

    @Test
    void shouldNotEmitEventsTwiceAfterPull() {
        var screening = new Screening(ID, "Inception", HALL, FUTURE);
        screening.pullDomainEvents();

        assertThat(screening.pullDomainEvents()).isEmpty();
    }
}
