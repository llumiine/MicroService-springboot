package com.formation.loan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formation.loan.dto.LoanRequest;
import com.formation.loan.dto.LoanResponse;
import com.formation.loan.exception.LoanAlreadyReturnedException;
import com.formation.loan.exception.NoAvailableCopiesException;
import com.formation.loan.model.LoanStatus;
import com.formation.loan.service.LoanService;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @Test
    void createLoan_ShouldReturn409_whenNoAvailableCopies() throws Exception {
        LoanRequest request = new LoanRequest("Alice", 1L);

        when(loanService.createLoan(any(LoanRequest.class)))
                .thenThrow(new NoAvailableCopiesException("Aucun exemplaire disponible pour ce livre"));

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void returnLoan_ShouldReturn409_whenAlreadyReturned() throws Exception {
        when(loanService.returnLoan(eq(1L)))
                .thenThrow(new LoanAlreadyReturnedException("cet emprunt est déjà terminé"));

        mockMvc.perform(patch("/api/loans/1/return"))
                .andExpect(status().isConflict());
    }

    @Test
    void createLoan_ShouldReturn400_whenBookNotFound() throws Exception {
        LoanRequest request = new LoanRequest("Alice", 99L);

        when(loanService.createLoan(any(LoanRequest.class)))
                .thenThrow(feignNotFound());

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Cas de concurrence (TOCTOU) : book-service revérifie le stock côté serveur et
     * renvoie 409 alors que loan-service avait validé availableCopies > 0 à l'étape 1.
     * Le GlobalExceptionHandler doit traduire ce FeignException.Conflict en 409 client.
     */
    @Test
    void createLoan_ShouldReturn409_whenConcurrentStockDepletion() throws Exception {
        LoanRequest request = new LoanRequest("Alice", 1L);

        when(loanService.createLoan(any(LoanRequest.class)))
                .thenThrow(feignConflict());

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    private FeignException.NotFound feignNotFound() {
        Request request = feignRequest();
        Response response = Response.builder()
                .status(404)
                .reason("Not Found")
                .request(request)
                .headers(Collections.emptyMap())
                .build();
        return (FeignException.NotFound) FeignException.errorStatus("BookClient#getBookById(Long)", response);
    }

    private FeignException.Conflict feignConflict() {
        Request request = feignRequest();
        Response response = Response.builder()
                .status(409)
                .reason("Conflict")
                .request(request)
                .headers(Collections.emptyMap())
                .build();
        return (FeignException.Conflict) FeignException.errorStatus("BookClient#decrementStock(Long)", response);
    }

    private Request feignRequest() {
        return Request.create(
                Request.HttpMethod.GET,
                "/api/books/1",
                Collections.emptyMap(),
                null,
                StandardCharsets.UTF_8,
                new RequestTemplate());
    }
}
