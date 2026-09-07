package com.formation.book.controller;

import com.formation.book.dto.BookRequest;
import com.formation.book.dto.BookResponse;
import com.formation.book.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

	private final BookService bookService;

	@GetMapping
	public ResponseEntity<List<BookResponse>> getAllBooks() {
		return ResponseEntity.ok(bookService.getAllBooks());
	}

	@GetMapping("/{id}")
	public ResponseEntity<BookResponse> getBookById(@PathVariable Long id) {
		return ResponseEntity.ok(bookService.getBookById(id));
	}

	@PostMapping
	public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
		BookResponse response = bookService.createBook(request);
		return ResponseEntity.created(URI.create("/api/books/" + response.getId())).body(response);
	}

	@PutMapping("/{id}")
	public ResponseEntity<BookResponse> updateBook(
			@PathVariable Long id,
			@Valid @RequestBody BookRequest request) {
		return ResponseEntity.ok(bookService.updateBook(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
		bookService.deleteBook(id);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{id}/decrement-stock")
	public ResponseEntity<BookResponse> decrementStock(@PathVariable Long id) {
		return ResponseEntity.ok(bookService.decrementStock(id));
	}

	@PatchMapping("/{id}/increment-stock")
	public ResponseEntity<BookResponse> incrementStock(@PathVariable Long id) {
		return ResponseEntity.ok(bookService.incrementStock(id));
	}
}
