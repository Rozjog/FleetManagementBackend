package ru.mirea.project.car.exceptions;

public class DuplicateLicensePlateException
        extends RuntimeException {

    public DuplicateLicensePlateException(String message) {
        super(message);
    }
}