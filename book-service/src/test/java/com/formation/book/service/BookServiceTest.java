package com.formation.book.service;

import com.formation.book.dto.BookRequest;
import com.formation.book.exception.BookNotFoundException;
import com.formation.book.exception.NoCopyAvailableException;
import com.formation.book.model.Book;
import com.formation.book.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    void decrementStock_shouldDecrease_whenCopiesAvailable() {
        Book book = Book.builder().id(1L).title("Clean Code").author("Robert C. Martin")
                .isbn("123").totalCopies(3).availableCopies(2).build();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = bookService.decrementStock(1L);

        assertThat(response.getAvailableCopies()).isEqualTo(1);
    }

    @Test
    void decrementStock_shouldThrowConflict_whenNoCopyAvailable() {
        Book book = Book.builder().id(1L).title("Clean Code").author("Robert C. Martin")
                .isbn("123").totalCopies(3).availableCopies(0).build();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> bookService.decrementStock(1L))
                .isInstanceOf(NoCopyAvailableException.class);

        verify(bookRepository, never()).save(any());
    }

    @Test
    void decrementStock_shouldThrowNotFound_whenBookDoesNotExist() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.decrementStock(99L))
                .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    void incrementStock_shouldNotExceedTotalCopies() {
        Book book = Book.builder().id(1L).title("Clean Code").author("Robert C. Martin")
                .isbn("123").totalCopies(3).availableCopies(3).build();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = bookService.incrementStock(1L);

        assertThat(response.getAvailableCopies()).isEqualTo(3); // ne dépasse pas totalCopies
    }

    @Test
    void createBook_shouldInitializeAvailableCopiesToTotalCopies() {
        BookRequest request = new BookRequest("1984", "George Orwell", "978-1", 5);
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> {
            Book b = inv.getArgument(0);
            b.setId(10L);
            return b;
        });

        var response = bookService.createBook(request);

        assertThat(response.getAvailableCopies()).isEqualTo(5);
        assertThat(response.getTotalCopies()).isEqualTo(5);
    }
}