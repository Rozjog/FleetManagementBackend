package ru.mirea.project.enums;

public enum CarBodyType {
    HATCHBACK,
    SEDAN,
    COUPE,
    LIFTBACK,
    CROSSOVER,
    VAN,
    CONVERTIBLE,
    PICKUP;

    public String getDisplayName() {
        return switch (this) {
            case HATCHBACK -> "Хэтчбек";
            case SEDAN -> "Седан";
            case COUPE -> "Купе";
            case LIFTBACK -> "Лифтбек";
            case CROSSOVER -> "Кроссовер";
            case VAN -> "Фургон";
            case CONVERTIBLE -> "Кабриолет";
            case PICKUP -> "Пикап";
        };
    }
}

