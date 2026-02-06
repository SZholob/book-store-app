package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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

    @GetMapping
    public String getAllBooks(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "6") int size,
                              @RequestParam(defaultValue = "name") String sortField,
                              @RequestParam(defaultValue = "asc") String sortDir,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) String genre) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, size, sort);


        Page<BookDTO> bookPage = bookService.getAllBooks(keyword, genre, pageable);


        List<String> genres = bookService.getAllGenres();


        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("genres", genres);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookPage.getTotalPages());
        model.addAttribute("totalItems", bookPage.getTotalElements());


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


    @GetMapping("/add")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String showAddBookForm(Model model) {
        model.addAttribute("book", new BookDTO());
        model.addAttribute("languages", Language.values());
        model.addAttribute("ageGroups", AgeGroup.values());
        return "book-add";
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String addBook(@Valid @ModelAttribute("book") BookDTO bookDTO,
                          BindingResult bindingResult,
                          Model model) {
        if (bindingResult.hasErrors()){
            model.addAttribute("languages", Language.values());
            model.addAttribute("ageGroups", AgeGroup.values());
            return "book-add";
        }
        try {
            bookService.addBook(bookDTO);
            return "redirect:/books";
        }catch (Exception e){
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("languages", Language.values());
            model.addAttribute("ageGroups", AgeGroup.values());
            return "book-add";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String showEditBookForm(@PathVariable Long id, Model model) {
        BookDTO book = bookService.getBookById(id);

        model.addAttribute("book", book);
        model.addAttribute("languages", Language.values());
        model.addAttribute("ageGroups", AgeGroup.values());

        return "book-edit";
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String updateBook(@Valid @ModelAttribute("book") BookDTO bookDTO,
                                              BindingResult bindingResult,
                                              Model model){
        if (bindingResult.hasErrors()) {
            model.addAttribute("languages", Language.values());
            model.addAttribute("ageGroups", AgeGroup.values());
            return "book-edit";
        }
        try {
            bookService.updateBook(bookDTO.getId(), bookDTO);
            return "redirect:/books/manage";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("languages", Language.values());
            model.addAttribute("ageGroups", AgeGroup.values());
            return "book-edit";
        }
    }

    @PostMapping("/delete/{name}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String deleteBook(@PathVariable String name){
        bookService.deleteBookByName(name);
        return "redirect:/books/manage";
    }

    @GetMapping("/manage")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String manageBooks(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) String keyword) {

        Pageable pageable = PageRequest.of(page, size);

        Page<BookDTO> bookPage = bookService.getAllBooks(keyword, null, pageable);

        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookPage.getTotalPages());
        model.addAttribute("totalItems", bookPage.getTotalElements());
        model.addAttribute("keyword", keyword);

        return "employee-books";
    }
}
