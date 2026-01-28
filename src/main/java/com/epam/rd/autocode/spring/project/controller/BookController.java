package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    @GetMapping
    public ResponseEntity<List<BookDTO>> getAllBooks(){
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @GetMapping("/{name}")
    public ResponseEntity<BookDTO> getBookByName(@PathVariable String name){
        return ResponseEntity.ok(bookService.getBookByName(name));
    }

    @PostMapping
    public ResponseEntity<BookDTO> addBook(@RequestBody BookDTO bookDTO){
        return new ResponseEntity<>(bookService.addBook(bookDTO), HttpStatus.CREATED);
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
