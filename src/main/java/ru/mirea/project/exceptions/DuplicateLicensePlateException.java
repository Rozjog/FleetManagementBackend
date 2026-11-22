package ru.mirea.project.exceptions;

public class DuplicateLicensePlateException
        extends RuntimeException {

    public DuplicateLicensePlateException(String message) {
        super(message);
    }
}