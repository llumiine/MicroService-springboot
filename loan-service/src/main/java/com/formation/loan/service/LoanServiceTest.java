package com.formation.loan.service;

import com.formation.loan.client.BookClient;
import com.formation.loan.dto.BookDto;
import com.formation.loan.dto.LoanRequest;
import com.formation.loan.dto.LoanResponse;
import com.formation.loan.exception.LoanAlreadyReturnedException;
import com.formation.loan.exception.LoanNotFoundException;
import com.formation.loan.exception.NoAvailableCopiesException;
import com.formation.loan.model.Loan;
import com.formation.loan.model.LoanStatus;
import com.formation.loan.repository.LoanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookClient bookClient;

    @InjectMocks
    private LoanService loanService;

    @Test
    void createLoan_Success() {
        LoanRequest request = new LoanRequest("Alice", 1L);
        BookDto book = new BookDto();
        book.setId(1L);
        book.setTitle("Clean Code");
        book.setAvailableCopies(2);

        when(bookClient.getBookById(1L)).thenReturn(book);
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
            Loan saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        LoanResponse response = loanService.createLoan(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Alice", response.getMemberName());
        assertEquals("Clean Code", response.getBookTitle());
        assertEquals(LoanStatus.ACTIVE, response.getStatus());

        verify(bookClient).decrementStock(1L);
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void createLoan_NoAvailableCopies_ThrowsException() {
        LoanRequest request = new LoanRequest("Alice", 1L);
        BookDto book = new BookDto();
        book.setId(1L);
        book.setAvailableCopies(0);

        when(bookClient.getBookById(1L)).thenReturn(book);

        assertThrows(NoAvailableCopiesException.class, () -> loanService.createLoan(request));
        verify(bookClient, never()).decrementStock(anyLong());
        verify(loanRepository, never()).save(any());
    }

    @Test
    void returnLoan_Success() {
        Loan loan = new Loan("Alice", 1L, "Clean Code", LocalDate.now().minusDays(3), LocalDate.now().plusDays(11), LoanStatus.ACTIVE);
        loan.setId(10L);

        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(i -> i.getArgument(0));

        LoanResponse response = loanService.returnLoan(10L);

        assertEquals(LoanStatus.RETURNED, response.getStatus());
        assertNotNull(response.getReturnDate());
        verify(bookClient).incrementStock(1L);
    }

    @Test
    void returnLoan_AlreadyReturned_ThrowsException() {
        Loan loan = new Loan("Alice", 1L, "Clean Code", LocalDate.now().minusDays(5), LocalDate.now().plusDays(9), LoanStatus.RETURNED);
        loan.setId(10L);

        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan));

        assertThrows(LoanAlreadyReturnedException.class, () -> loanService.returnLoan(10L));
        verify(bookClient, never()).incrementStock(anyLong());
    }
}