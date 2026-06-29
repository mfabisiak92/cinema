package com.cinema.shared.domain;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredOn();
}
