package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO{
    private Long id;

    @NotBlank(message = "Book name is required")
    private String name;

    @NotBlank(message = "Genre is required")
    private String genre;

    private AgeGroup ageGroup;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private BigDecimal price;

    private LocalDate publicationDate;

    @NotBlank(message = "Author is required")
    private String author;

    @Min(value = 1, message = "Pages must be at least 1")
    private Integer pages;

    private String characteristics;

    @Size(max = 2000, message = "Description is too long (max 2000 characters)")
    private String description;
    private Language language;
    private String imageUrl;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;
}
