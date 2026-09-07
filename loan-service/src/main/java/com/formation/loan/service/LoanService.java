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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookClient bookClient;

    public LoanService(LoanRepository loanRepository, BookClient bookClient) {
        this.loanRepository = loanRepository;
        this.bookClient = bookClient;
    }

    public List<LoanResponse> getAllLoans() {
        return loanRepository.findAll().stream()
                .map(LoanMapper::toResponse)
                .collect(Collectors.toList());
    }

    public LoanResponse getLoanById(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException(id));
        return LoanMapper.toResponse(loan);
    }

    @Transactional
    public LoanResponse createLoan(LoanRequest request) {
        // 1. loan-service appelle book-service : GET /api/books/{id}
        BookDto book = bookClient.getBookById(request.getBookId());

        // Vérification disponibilité
        if (book.getAvailableCopies() <= 0) {
            throw new NoAvailableCopiesException("Aucun exemplaire disponible pour ce livre");
        }

        // 2. loan-service appelle book-service : PATCH /api/books/{id}/decrement-stock
        // Si 409 transmis par Feign, il sera intercepté par le GlobalExceptionHandler (cas TOCTOU)
        bookClient.decrementStock(request.getBookId());

        // 3. Création de l'emprunt (Snapshot du titre)
        Loan loan = new Loan();
        loan.setMemberName(request.getMemberName());
        loan.setBookId(book.getId());
        loan.setBookTitle(book.getTitle());
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(14));
        loan.setStatus(LoanStatus.ACTIVE);

        return LoanMapper.toResponse(loanRepository.save(loan));
    }

    @Transactional
    public LoanResponse returnLoan(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException(id));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new LoanAlreadyReturnedException("cet emprunt est déjà terminé");
        }

        // Incrémentation du stock dans book-service
        bookClient.incrementStock(loan.getBookId());

        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnDate(LocalDate.now());

        return LoanMapper.toResponse(loanRepository.save(loan));
    }
}