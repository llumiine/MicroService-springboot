package com.formation.book.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formation.book.dto.BookRequest;
import com.formation.book.dto.BookResponse;
import com.formation.book.exception.BookNotFoundException;
import com.formation.book.exception.NoCopyAvailableException;
import com.formation.book.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @Test
    void decrementStock_shouldReturn409_whenNoCopyAvailable() throws Exception {
        when(bookService.decrementStock(eq(1L)))
                .thenThrow(new NoCopyAvailableException(1L));

        mockMvc.perform(patch("/api/books/1/decrement-stock"))
                .andExpect(status().isConflict());
    }

    @Test
    void decrementStock_shouldReturn200_whenCopyAvailable() throws Exception {
        BookResponse response = BookResponse.builder()
                .id(1L).title("Clean Code").author("R. Martin").isbn("123")
                .totalCopies(3).availableCopies(1).build();
        when(bookService.decrementStock(eq(1L))).thenReturn(response);

        mockMvc.perform(patch("/api/books/1/decrement-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCopies").value(1));
    }

    @Test
    void getBookById_shouldReturn404_whenNotFound() throws Exception {
        when(bookService.getBookById(eq(99L))).thenThrow(new BookNotFoundException(99L));

        mockMvc.perform(get("/api/books/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBook_shouldReturn201_whenValid() throws Exception {
        BookRequest request = new BookRequest("1984", "Orwell", "978-1", 5);
        BookResponse response = BookResponse.builder()
                .id(1L).title("1984").author("Orwell").isbn("978-1")
                .totalCopies(5).availableCopies(5).build();
        when(bookService.createBook(any())).thenReturn(response);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.availableCopies").value(5));
    }

    @Test
    void createBook_shouldReturn400_whenTitleMissing() throws Exception {
        BookRequest request = new BookRequest("", "Orwell", "978-1", 5);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}