package com.cinema.screening.infrastructure.web;

import com.cinema.screening.application.CreateScreeningUseCase;
import com.cinema.screening.application.ScreeningRepository;
import com.cinema.screening.domain.Hall;
import com.cinema.screening.domain.ScreeningId;
import com.cinema.screening.domain.ScreeningNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/screenings")
@Tag(name = "Screenings")
class ScreeningController {

    private final CreateScreeningUseCase createScreeningUseCase;
    private final ScreeningRepository screeningRepository;

    ScreeningController(CreateScreeningUseCase createScreeningUseCase, ScreeningRepository screeningRepository) {
        this.createScreeningUseCase = createScreeningUseCase;
        this.screeningRepository = screeningRepository;
    }

    @PostMapping
    @Operation(summary = "Schedule a new screening")
    ResponseEntity<ScreeningResponse> create(@Valid @RequestBody ScreeningRequest request) {
        var hall = new Hall(request.hall().name(), request.hall().rows(), request.hall().seatsPerRow());
        var command = new CreateScreeningUseCase.Command(request.movieTitle(), hall, request.startTime());
        ScreeningId id = createScreeningUseCase.execute(command);
        return ResponseEntity
                .created(URI.create("/screenings/" + id))
                .body(new ScreeningResponse(id.value()));
    }

    @GetMapping("/{id}/seats")
    @Operation(summary = "Get seat availability for a screening")
    ResponseEntity<SeatsResponse> seats(@PathVariable UUID id) {
        var screeningId = new ScreeningId(id);
        var screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new ScreeningNotFoundException(screeningId));
        return ResponseEntity.ok(SeatsResponse.from(screening));
    }
}
