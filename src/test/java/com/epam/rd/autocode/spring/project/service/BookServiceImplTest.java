package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.service.impl.BookServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private BookServiceImpl bookService;

    @Test
    void getAllBooks_NoFilter_ShouldReturnAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Book book = new Book();
        book.setId(1L);
        book.setName("Test Book");
        Page<Book> bookPage = new PageImpl<>(Collections.singletonList(book));

        when(bookRepository.findAll(pageable)).thenReturn(bookPage);

        Page<BookDTO> result = bookService.getAllBooks(null, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Book", result.getContent().get(0).getName());
        verify(bookRepository).findAll(pageable);
    }



    @Test
    void getAllBooks_WithGenre_ShouldFilterByGenre() {
        Pageable pageable = PageRequest.of(0, 10);
        String genre = "Fantasy";
        Book book = new Book();
        book.setGenre(genre);
        Page<Book> bookPage = new PageImpl<>(Collections.singletonList(book));

        when(bookRepository.findByGenre(genre, pageable)).thenReturn(bookPage);

        Page<BookDTO> result = bookService.getAllBooks(null, genre, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(genre, result.getContent().get(0).getGenre());
        verify(bookRepository).findByGenre(genre, pageable);
        verify(bookRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getAllBooks_WithKeyword_ShouldFilterByKeyword() {
        Pageable pageable = PageRequest.of(0, 10);
        String keyword = "Harry";
        Book book = new Book();
        book.setName("Harry Potter");
        Page<Book> bookPage = new PageImpl<>(Collections.singletonList(book));

        when(bookRepository.findByNameContainingIgnoreCaseOrAuthorContainingIgnoreCase(keyword, keyword, pageable))
                .thenReturn(bookPage);

        Page<BookDTO> result = bookService.getAllBooks(keyword, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Harry Potter", result.getContent().get(0).getName());
        verify(bookRepository).findByNameContainingIgnoreCaseOrAuthorContainingIgnoreCase(keyword, keyword, pageable);
    }

    @Test
    void getBookById_Success() {

        Book book = new Book();
        book.setId(1L);
        book.setName("Found Me");
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        BookDTO result = bookService.getBookById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Found Me", result.getName());
    }

    @Test
    void getBookById_NotFound_ShouldThrowException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());


        assertThrows(NotFoundException.class, () -> bookService.getBookById(99L));
    }

    @Test
    void getBookByName_Success() {
        String name = "Unique Book";
        Book book = new Book();
        book.setName(name);
        when(bookRepository.findByName(name)).thenReturn(Optional.of(book));


        BookDTO result = bookService.getBookByName(name);


        assertEquals(name, result.getName());
    }


    @Test
    void getBookByName_NotFound_ShouldThrowException() {
        String name = "Missing Book";
        when(bookRepository.findByName(name)).thenReturn(Optional.empty());


        assertThrows(NotFoundException.class, () -> bookService.getBookByName(name));
    }


    @Test
    void addBook_Success() {
        BookDTO dto = new BookDTO();
        dto.setName("New Book");
        dto.setPrice(BigDecimal.valueOf(20.0));

        when(bookRepository.findByName("New Book")).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book b = invocation.getArgument(0);
            b.setId(10L);
            return b;
        });

        BookDTO result = bookService.addBook(dto);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("New Book", result.getName());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void addBook_AlreadyExists_ShouldThrowException() {
        BookDTO dto = new BookDTO();
        dto.setName("Existing Book");
        when(bookRepository.findByName("Existing Book")).thenReturn(Optional.of(new Book()));

        assertThrows(AlreadyExistException.class, () -> bookService.addBook(dto));
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void updateBook_Success() {
        Long id = 1L;
        BookDTO dto = new BookDTO();
        dto.setId(id);
        dto.setName("Updated Name");

        Book existingBook = new Book();
        existingBook.setId(id);
        existingBook.setName("Old Name");

        when(bookRepository.findById(id)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookDTO result = bookService.updateBook(id, dto);

        assertEquals("Updated Name", result.getName());
        assertEquals(id, result.getId());
        verify(bookRepository).save(existingBook);
    }


    @Test
    void updateBook_NotFound_ShouldThrowException() {
        Long id = 1L;
        BookDTO dto = new BookDTO();
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookService.updateBook(id, dto));
        verify(bookRepository, never()).save(any());
    }


    @Test
    void deleteBookByName_Success() {
        String name = "Delete Me";
        Book book = new Book();
        book.setName(name);
        when(bookRepository.findByName(name)).thenReturn(Optional.of(book));

        bookService.deleteBookByName(name);

        verify(bookRepository).deleteByName(name);
    }

    @Test
    void deleteBookByName_NotFound_ShouldThrowException() {
        String name = "Unknown";
        when(bookRepository.findByName(name)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookService.deleteBookByName(name));
        verify(bookRepository, never()).deleteByName(anyString());
    }

    @Test
    void getAllGenres_ShouldReturnList() {
        List<String> genres = List.of("Fantasy", "Sci-Fi");
        when(bookRepository.findAllGenres()).thenReturn(genres);

        List<String> result = bookService.getAllGenres();

        assertEquals(2, result.size());
        assertTrue(result.contains("Fantasy"));
    }

}
