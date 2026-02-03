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
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceImplTest {

    @Mock private BookRepository bookRepository;
    @Mock private ModelMapper modelMapper;
    @InjectMocks private BookServiceImpl bookService;

    @Test
    void getAllBooks_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Book> books = List.of(new Book());
        Page<Book> bookPage = new PageImpl<>(books);

        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        when(modelMapper.map(any(), eq(BookDTO.class))).thenReturn(new BookDTO());

        Page<BookDTO> result = bookService.getAllBooks(null,null,  pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getBookByName_ShouldReturnBook() {
        String name = "Test Book";
        Book book = new Book();
        book.setName(name);

        when(bookRepository.findByName(name)).thenReturn(Optional.of(book));
        when(modelMapper.map(book, BookDTO.class)).thenReturn(new BookDTO());

        assertNotNull(bookService.getBookByName(name));
    }

    @Test
    void getBookById_ShouldThrowNotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> bookService.getBookById(1L));
    }

    @Test
    void addBook_ShouldThrowAlreadyExists() {
        BookDTO dto = new BookDTO();
        dto.setName("Existing Book");

        when(bookRepository.findByName(dto.getName())).thenReturn(Optional.of(new Book()));

        assertThrows(AlreadyExistException.class, () -> bookService.addBook(dto));
    }

    @Test
    void addBook_ShouldSaveBook() {
        BookDTO dto = new BookDTO();
        dto.setName("New Book");
        Book book = new Book();

        when(bookRepository.findByName(dto.getName())).thenReturn(Optional.empty());
        when(modelMapper.map(dto, Book.class)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(modelMapper.map(book, BookDTO.class)).thenReturn(dto);

        BookDTO result = bookService.addBook(dto);

        assertNotNull(result);
        verify(bookRepository).save(book);
    }

    @Test
    void updateBookByName_ShouldUpdate() {
        String name = "Old Name";
        BookDTO dto = new BookDTO();
        Book book = new Book();

        when(bookRepository.findByName(name)).thenReturn(Optional.of(book));
        when(bookRepository.save(book)).thenReturn(book);
        when(modelMapper.map(book, BookDTO.class)).thenReturn(dto);

        bookService.updateBook(, name, dto);

        verify(modelMapper).map(dto, book);
        verify(bookRepository).save(book);
    }

    @Test
    void deleteBookByName_ShouldDelete() {
        String name = "Delete Me";
        when(bookRepository.findByName(name)).thenReturn(Optional.of(new Book()));

        bookService.deleteBookByName(name);

        verify(bookRepository).deleteByName(name);
    }

    @Test
    void getBookByName_ShouldBook_WhenExists(){
        String bookName = "Java Basic";
        Book book = new Book();
        book.setName(bookName);
        book.setPrice(BigDecimal.valueOf(100));

        BookDTO bookDTO = new BookDTO();
        bookDTO.setName(bookName);
        bookDTO.setPrice(BigDecimal.valueOf(100));

        when(bookRepository.findByName(bookName)).thenReturn(Optional.of(book));
        when(modelMapper.map(book, BookDTO.class)).thenReturn(bookDTO);

        BookDTO result = bookService.getBookByName(bookName);

        assertNotNull(result);
        assertEquals(bookName, result.getName());
        verify(bookRepository, times(1)).findByName(bookName);
    }

    @Test
    void getBookByName_ShouldThrowException_WhenNotFound() {

        String bookName = "Unknown Book";
        when(bookRepository.findByName(bookName)).thenReturn(Optional.empty());


        assertThrows(NotFoundException.class, () -> bookService.getBookByName(bookName));
        verify(bookRepository, times(1)).findByName(bookName);
    }
}
