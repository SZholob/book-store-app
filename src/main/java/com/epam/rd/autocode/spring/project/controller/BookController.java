package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;


@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /*@GetMapping
    public String getAllBooks(Model model) {
        model.addAttribute("books", bookService.getAllBooks());
        System.out.println(model.getAttribute("books"));
        return "books";
    }*/

    @GetMapping
    public String getAllBooks(Model model,
                              @RequestParam(defaultValue = "0") int page,      // Номер сторінки (0 - перша)
                              @RequestParam(defaultValue = "6") int size,      // Скільки книг на сторінці
                              @RequestParam(defaultValue = "id") String sortField, // Поле для сортування
                              @RequestParam(defaultValue = "asc") String sortDir,  // Напрямок (asc/desc)
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) String genre) {    // Пошуковий запит

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // 2. Отримуємо дані
        Page<BookDTO> bookPage = bookService.getAllBooks(keyword, genre, pageable);


        List<String> genres = bookService.getAllGenres();

        // 3. Закидаємо все в модель
        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("genres", genres);// Самі книги
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookPage.getTotalPages());
        model.addAttribute("totalItems", bookPage.getTotalElements());

        // Потрібно повернути параметри назад на сторінку, щоб перемикачі не "забували" пошук
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedGenre", genre);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        return "books";
    }

    @GetMapping("/{name}")
    public String getBookDetails(@PathVariable String name, Model model) {
        BookDTO book = bookService.getBookByName(name);
        model.addAttribute("book", book);
        return "book-details";
    }

    /*@GetMapping("/{name}")
    public ResponseEntity<BookDTO> getBookByName(@PathVariable String name){
        return ResponseEntity.ok(bookService.getBookByName(name));
    }*/

    @GetMapping("/add")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String showAddBookForm(Model model) {
        model.addAttribute("book", new BookDTO());
        return "book-add";
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String addBook(@ModelAttribute BookDTO bookDTO) {
        bookService.addBook(bookDTO);
        return "redirect:/books";
    }

    @PatchMapping("/{name}")
    public ResponseEntity<BookDTO> updateBook(@PathVariable String name, @RequestBody BookDTO bookDTO){
        return ResponseEntity.ok(bookService.updateBookByName(name, bookDTO));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteBook(@PathVariable String name){
        bookService.deleteBookByName(name);
        return ResponseEntity.noContent().build();
    }
}
