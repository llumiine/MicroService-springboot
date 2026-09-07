package com.formation.loan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LoanRequest {

    @NotBlank(message = "Le nom du membre est obligatoire")
    private String memberName;

    @NotNull(message = "L'ID du livre est obligatoire")
    private Long bookId;

    public LoanRequest() {}

    public LoanRequest(String memberName, Long bookId) {
        this.memberName = memberName;
        this.bookId = bookId;
    }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
}