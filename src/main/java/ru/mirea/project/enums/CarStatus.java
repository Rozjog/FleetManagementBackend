package ru.mirea.project.enums;

public enum CarStatus {
    AVAILABLE,
    IN_USE,
    MAINTENANCE;

    public String getDisplayName() {
        return switch (this) {
            case AVAILABLE -> "Доступен";
            case IN_USE -> "В эксплуатации";
            case MAINTENANCE -> "На обслуживании";
        };
    }
}
