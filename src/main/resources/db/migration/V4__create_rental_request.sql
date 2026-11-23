
CREATE TABLE rental_request (
    id UUID PRIMARY KEY,
    external_id VARCHAR(100) UNIQUE,
    car_id BIGINT NOT NULL REFERENCES cars(car_id),
    user_id BIGINT NOT NULL REFERENCES users(user_id),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,

    monthly_price INTEGER NOT NULL
        CHECK (monthly_price > 0),

    status VARCHAR(32) NOT NULL
        CHECK (status IN (
            'NEW', 'APPROVED', 'ACTIVE',
            'COMPLETED', 'CANCELLED'
        )),

    priority VARCHAR(32) NOT NULL
        CHECK (priority IN ('LOW', 'NORMAL', 'HIGH')),

    is_long_term BOOLEAN NOT NULL DEFAULT TRUE,
    archived BOOLEAN NOT NULL DEFAULT FALSE,

    archived_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,

    notes TEXT,

    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT chk_rental_period
        CHECK (end_date - start_date >= 30)
);

CREATE INDEX idx_rental_request_status
    ON rental_request(status);

CREATE INDEX idx_rental_request_archived
    ON rental_request(archived);

CREATE INDEX idx_rental_request_user
    ON rental_request(user_id);

CREATE INDEX idx_rental_request_dates
    ON rental_request(start_date, end_date);
