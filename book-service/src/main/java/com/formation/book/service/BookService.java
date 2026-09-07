package com.formation.book.service;

import com.formation.book.dto.BookRequest;
import com.formation.book.dto.BookResponse;
import com.formation.book.exception.BookNotFoundException;
import com.formation.book.exception.NoCopyAvailableException;
import com.formation.book.model.Book;
import com.formation.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public BookResponse getBookById(Long id) {
        Book book = findBookOrThrow(id);
        return toResponse(book);
    }

    public BookResponse createBook(BookRequest request) {
        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .totalCopies(request.getTotalCopies())
                .availableCopies(request.getTotalCopies()) // initialisé = totalCopies
                .build();
        return toResponse(bookRepository.save(book));
    }

    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = findBookOrThrow(id);
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setTotalCopies(request.getTotalCopies());
        // On plafonne availableCopies pour ne jamais dépasser le nouveau totalCopies
        if (book.getAvailableCopies() > request.getTotalCopies()) {
            book.setAvailableCopies(request.getTotalCopies());
        }
        return toResponse(bookRepository.save(book));
    }

    public void deleteBook(Long id) {
        Book book = findBookOrThrow(id);
        bookRepository.delete(book);
    }

    /**
     * Appelé par loan-service à la création d'un emprunt.
     * Défense en profondeur : revérifie availableCopies même si loan-service
     * a déjà vérifié avant d'appeler (protection contre le TOCTOU).
     */
    public BookResponse decrementStock(Long id) {
        Book book = findBookOrThrow(id);
        if (book.getAvailableCopies() <= 0) {
            throw new NoCopyAvailableException(id);
        }
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        return toResponse(bookRepository.save(book));
    }

    /**
     * Appelé par loan-service au retour d'un emprunt.
     * Ne dépasse jamais totalCopies.
     */
    public BookResponse incrementStock(Long id) {
        Book book = findBookOrThrow(id);
        if (book.getAvailableCopies() < book.getTotalCopies()) {
            book.setAvailableCopies(book.getAvailableCopies() + 1);
        }
        return toResponse(bookRepository.save(book));
    }

    private Book findBookOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    private BookResponse toResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .totalCopies(book.getTotalCopies())
                .availableCopies(book.getAvailableCopies())
                .build();
    }
}