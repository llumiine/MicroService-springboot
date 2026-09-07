package com.formation.book.exception;

public class NoCopyAvailableException extends RuntimeException {
    public NoCopyAvailableException(Long id) {
        super("Aucun exemplaire disponible pour le livre id : " + id);
    }
}