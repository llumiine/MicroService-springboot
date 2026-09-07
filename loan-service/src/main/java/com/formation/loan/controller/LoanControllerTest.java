package com.formation.loan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formation.loan.dto.LoanRequest;
import com.formation.loan.dto.LoanResponse;
import com.formation.loan.model.LoanStatus;
import com.formation.loan.service.LoanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanController.class)
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LoanService loanService;

    @Test
    void getAllLoans_ShouldReturnList() throws Exception {
        LoanResponse response = new LoanResponse();
        response.setId(1L);
        response.setMemberName("Alice");

        when(loanService.getAllLoans()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].memberName").value("Alice"));
    }

    @Test
    void createLoan_ShouldReturnCreated() throws Exception {
        LoanRequest request = new LoanRequest("Alice", 1L);
        LoanResponse response = new LoanResponse();
        response.setId(1L);
        response.setMemberName("Alice");
        response.setBookTitle("Clean Code");
        response.setStatus(LoanStatus.ACTIVE);

        when(loanService.createLoan(any(LoanRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.memberName").value("Alice"));
    }
}