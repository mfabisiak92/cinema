package com.cinema.screening.application;

import com.cinema.screening.domain.Screening;
import com.cinema.screening.domain.ScreeningId;

import java.util.Optional;

public interface ScreeningRepository {
    void save(Screening screening);
    Optional<Screening> findById(ScreeningId id);
}
