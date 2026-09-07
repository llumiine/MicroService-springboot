package com.formation.book.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookRequest {

    @NotBlank(message = "Le titre est obligatoire")
    private String title;

    @NotBlank(message = "L'auteur est obligatoire")
    private String author;

    @NotBlank(message = "L'ISBN est obligatoire")
    private String isbn;

    @NotNull(message = "Le nombre total d'exemplaires est obligatoire")
    @Min(value = 1, message = "totalCopies doit être >= 1")
    private Integer totalCopies;
    // Pas de availableCopies ici : il est calculé = totalCopies à la création
}