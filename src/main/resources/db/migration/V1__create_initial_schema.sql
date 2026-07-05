CREATE TABLE screenings (
    id              UUID         NOT NULL PRIMARY KEY,
    movie_title     VARCHAR(255) NOT NULL,
    hall_name       VARCHAR(100) NOT NULL,
    hall_rows       INT          NOT NULL,
    hall_seats_per_row INT       NOT NULL,
    start_time      TIMESTAMP    NOT NULL
);

CREATE TABLE screening_seats (
    screening_id UUID        NOT NULL REFERENCES screenings (id),
    row_number   INT         NOT NULL,
    seat_number  INT         NOT NULL,
    status       VARCHAR(20) NOT NULL,
    PRIMARY KEY (screening_id, row_number, seat_number)
);

CREATE TABLE reservations (
    id           UUID        NOT NULL PRIMARY KEY,
    screening_id UUID        NOT NULL REFERENCES screenings (id),
    customer_id  UUID        NOT NULL,
    status       VARCHAR(20) NOT NULL,
    expires_at   TIMESTAMP   NOT NULL
);

CREATE TABLE reservation_seats (
    reservation_id UUID NOT NULL REFERENCES reservations (id),
    row_number     INT  NOT NULL,
    seat_number    INT  NOT NULL,
    PRIMARY KEY (reservation_id, row_number, seat_number)
);
