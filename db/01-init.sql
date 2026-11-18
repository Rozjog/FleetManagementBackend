CREATE TABLE IF NOT EXISTS cars (
    car_id BIGSERIAL PRIMARY KEY,
    brand VARCHAR(30) NOT NULL,
    model VARCHAR(30) NOT NULL,
    transmission VARCHAR(30) NOT NULL,
    year INTEGER NOT NULL CHECK (year >= 1900),
    license_plate VARCHAR(30) UNIQUE NOT NULL,
    body_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL
    CHECK (status IN ('AVAILABLE', 'IN_USE', 'MAINTENANCE')),
    mileage INTEGER NOT NULL CHECK (mileage >= 0)
    );

CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(12) NOT NULL UNIQUE,
    email VARCHAR(255) UNIQUE,
    driver_license VARCHAR(10) UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'REGULAR',

    CONSTRAINT check_first_name
    CHECK (LENGTH(TRIM(first_name)) > 0),

    CONSTRAINT check_last_name
    CHECK (LENGTH(TRIM(last_name)) > 0),

    CONSTRAINT check_phone
    CHECK (phone ~ '^[+]7[0-9]{10}$'),

    CONSTRAINT check_driver_license
    CHECK (
              driver_license IS NULL
              OR driver_license ~ '^[0-9]{10}$'
          ),

    CONSTRAINT check_user_status
    CHECK (status IN ('REGULAR', 'VIP'))
    );

CREATE TABLE IF NOT EXISTS requests (
    request_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    car_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    monthly_price INTEGER NOT NULL CHECK (monthly_price > 0),
    status VARCHAR(30) NOT NULL
    CHECK (
              status IN (
              'NEW',
              'APPROVED',
              'ACTIVE',
              'COMPLETED',
              'CANCELLED'
                        )
    ),
    notes TEXT,

    CONSTRAINT check_rental_period
    CHECK (
              end_date > start_date
              AND (end_date - start_date) >= 30
    ),

    CONSTRAINT fk_requests_user
    FOREIGN KEY (user_id) REFERENCES users(user_id),

    CONSTRAINT fk_requests_car
    FOREIGN KEY (car_id) REFERENCES cars(car_id)
    );
