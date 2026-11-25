package ru.mirea.project.car.enums;

public enum CarTransmission {
    AUTOMATIC,
    MANUAL,
    CVT;

    public String getDisplayName() {
        return switch (this) {
            case AUTOMATIC -> "Автоматическая";
            case MANUAL -> "Механическая";
            case CVT -> "Вариатор";
        };
    }
}
