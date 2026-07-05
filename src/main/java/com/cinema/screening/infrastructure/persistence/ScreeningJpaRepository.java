package com.cinema.screening.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface ScreeningJpaRepository extends JpaRepository<ScreeningJpaEntity, UUID> {}
