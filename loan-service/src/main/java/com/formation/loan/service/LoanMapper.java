package com.formation.loan.service;

import com.formation.loan.dto.LoanResponse;
import com.formation.loan.model.Loan;

public class LoanMapper {

    public static LoanResponse toResponse(Loan loan) {
        LoanResponse dto = new LoanResponse();
        dto.setId(loan.getId());
        dto.setMemberName(loan.getMemberName());
        dto.setBookId(loan.getBookId());
        dto.setBookTitle(loan.getBookTitle());
        dto.setLoanDate(loan.getLoanDate());
        dto.setDueDate(loan.getDueDate());
        dto.setReturnDate(loan.getReturnDate());
        dto.setStatus(loan.getStatus());
        return dto;
    }
}