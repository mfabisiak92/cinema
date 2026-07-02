package com.cinema.screening.application;

import com.cinema.screening.domain.Hall;
import com.cinema.screening.domain.Screening;
import com.cinema.screening.domain.ScreeningId;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CreateScreeningUseCase {

    private final ScreeningRepository screeningRepository;

    public CreateScreeningUseCase(ScreeningRepository screeningRepository) {
        this.screeningRepository = screeningRepository;
    }

    public ScreeningId execute(Command command) {
        var id = ScreeningId.newId();
        var screening = new Screening(id, command.movieTitle(), command.hall(), command.startTime());
        screeningRepository.save(screening);
        return id;
    }

    public record Command(String movieTitle, Hall hall, LocalDateTime startTime) {}
}
