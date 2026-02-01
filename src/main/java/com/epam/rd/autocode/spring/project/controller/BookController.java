package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public String getAllBooks(Model model) {
        model.addAttribute("books", bookService.getAllBooks());
        System.out.println(model.getAttribute("books"));
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
