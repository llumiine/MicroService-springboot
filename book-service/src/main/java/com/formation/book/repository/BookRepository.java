package com.formation.book.repository;

import com.formation.book.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
    // Bonus éventuel : findByAuthorContainingIgnoreCase, findByTitleContainingIgnoreCase
}