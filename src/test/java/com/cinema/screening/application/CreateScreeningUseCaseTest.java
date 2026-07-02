package com.cinema.screening.application;

import com.cinema.screening.domain.Hall;
import com.cinema.screening.domain.Screening;
import com.cinema.screening.domain.ScreeningId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CreateScreeningUseCaseTest {

    @Mock
    ScreeningRepository screeningRepository;

    @InjectMocks
    CreateScreeningUseCase useCase;

    @Test
    void shouldSaveScreeningWithGeneratedId() {
        var command = new CreateScreeningUseCase.Command(
                "The Matrix",
                new Hall("A", 5, 10),
                LocalDateTime.now().plusDays(1)
        );

        ScreeningId id = useCase.execute(command);

        var captor = ArgumentCaptor.forClass(Screening.class);
        verify(screeningRepository).save(captor.capture());
        assertThat(captor.getValue().id()).isEqualTo(id);
        assertThat(captor.getValue().movieTitle()).isEqualTo("The Matrix");
    }

    @Test
    void shouldReturnIdOfSavedScreening() {
        var command = new CreateScreeningUseCase.Command(
                "Inception",
                new Hall("B", 3, 8),
                LocalDateTime.now().plusDays(2)
        );

        ScreeningId id = useCase.execute(command);

        assertThat(id).isNotNull();
        assertThat(id.value()).isNotNull();
    }
}
