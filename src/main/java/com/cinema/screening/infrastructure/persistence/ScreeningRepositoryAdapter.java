package com.cinema.screening.infrastructure.persistence;

import com.cinema.screening.application.ScreeningRepository;
import com.cinema.screening.domain.Screening;
import com.cinema.screening.domain.ScreeningId;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ScreeningRepositoryAdapter implements ScreeningRepository {

    private final ScreeningJpaRepository jpaRepository;

    public ScreeningRepositoryAdapter(ScreeningJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Screening screening) {
        jpaRepository.save(ScreeningJpaEntity.from(screening));
    }

    @Override
    public Optional<Screening> findById(ScreeningId id) {
        return jpaRepository.findById(id.value()).map(ScreeningJpaEntity::toDomain);
    }
}
