CREATE TABLE IF NOT EXISTS rental_request (
                                              id              UUID PRIMARY KEY,
                                              external_id     VARCHAR(100) UNIQUE,
    car_id          UUID,
    user_id         UUID,
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    monthly_price   INTEGER NOT NULL CHECK (monthly_price > 0),
    status          VARCHAR(32) NOT NULL,
    priority        VARCHAR(32) NOT NULL,
    is_long_term    BOOLEAN NOT NULL DEFAULT TRUE,
    archived        BOOLEAN NOT NULL DEFAULT FALSE,
    archived_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ,
    updated_at      TIMESTAMPTZ,
    notes           TEXT,
    version         BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT chk_rental_period CHECK (end_date > start_date)
    );

CREATE INDEX IF NOT EXISTS idx_rental_request_status ON rental_request(status);
CREATE INDEX IF NOT EXISTS idx_rental_request_archived ON rental_request(archived);
CREATE INDEX IF NOT EXISTS idx_rental_request_user ON rental_request(user_id);
CREATE INDEX IF NOT EXISTS idx_rental_request_dates ON rental_request(start_date, end_date);
