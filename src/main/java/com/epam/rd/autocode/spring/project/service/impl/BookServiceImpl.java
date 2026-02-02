package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.service.BookService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;



    @Override
    public Page<BookDTO> getAllBooks(String keyword, String genre, Pageable pageable) {
        Page<Book> bookPage;

        if (genre != null && !genre.isEmpty()){
            bookPage = bookRepository.findByGenre(genre, pageable);
        }else if (keyword != null && !keyword.isEmpty()) {
            bookPage = bookRepository.findByNameContainingIgnoreCaseOrAuthorContainingIgnoreCase(keyword, keyword, pageable);
        } else {

            bookPage = bookRepository.findAll(pageable);
        }

        return bookPage.map(book -> modelMapper.map(book, BookDTO.class));
    }

    @Override
    public BookDTO getBookById(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Book not found with ID: " + bookId));
        return modelMapper.map(book, BookDTO.class);
    }

    @Override
    public BookDTO getBookByName(String name) {
        Book book = bookRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Book not found with name: " + name));
        return modelMapper.map(book, BookDTO.class);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('EMPLOYEE')")
    public BookDTO addBook(BookDTO bookDTO) {
        if (bookRepository.findByName(bookDTO.getName()).isPresent()){
            throw new AlreadyExistException("Book already exists with name: " + bookDTO.getName());
        }
        Book book = modelMapper.map(bookDTO, Book.class);
        Book saveBook = bookRepository.save(book);
        return modelMapper.map(saveBook, BookDTO.class);
    }

    @Override
    public List<String> getAllGenres() {
        return bookRepository.findAllGenres();
    }

    @Override
    @Transactional
    public BookDTO updateBookByName(String name, BookDTO bookDTO) {
        Book existingBook = bookRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Book not found with name: " + name));

        modelMapper.map(bookDTO, existingBook);

        Book updatedBook = bookRepository.save(existingBook);
        return modelMapper.map(updatedBook, BookDTO.class);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('EMPLOYEE')")
    public void deleteBookByName(String name) {
        Book book = bookRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Book not found with name: " + name));
        bookRepository.deleteByName(name);
    }


}
