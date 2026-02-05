package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.conf.SecurityConfig;
import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private ClientService clientService;
    @MockBean
    private EmployeeService employeeService;


    @Test
    @WithAnonymousUser
    void getAllBooks_ShouldReturnListView() throws Exception {
        Page<BookDTO> page = new PageImpl<>(Collections.emptyList());
        when(bookService.getAllBooks(any(), any(), any(Pageable.class))).thenReturn(page);
        when(bookService.getAllGenres()).thenReturn(List.of("Fantasy", "Sci-Fi"));

        mockMvc.perform(get("/books")
                        .param("page", "0")
                        .param("size", "6")
                        .param("sortField", "price")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(view().name("books"))
                .andExpect(model().attributeExists("books", "genres", "currentPage", "totalPages"))
                .andExpect(model().attribute("sortField", "price"))
                .andExpect(model().attribute("reverseSortDir", "asc"));
    }

    @Test
    @WithAnonymousUser
    void getBookDetails_ShouldReturnDetailsView() throws Exception {
        String bookName = "Java Basics";
        BookDTO bookDTO = new BookDTO();
        bookDTO.setName(bookName);

        when(bookService.getBookByName(bookName)).thenReturn(bookDTO);

        mockMvc.perform(get("/books/{name}", bookName))
                .andExpect(status().isOk())
                .andExpect(view().name("book-details"))
                .andExpect(model().attribute("book", hasProperty("name", is(bookName))));
    }


    @Test
    @WithMockUser(username = "admin", roles = "EMPLOYEE")
    void showAddBookForm_Employee_ShouldReturnView() throws Exception {
        mockMvc.perform(get("/books/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("book-add"))
                .andExpect(model().attributeExists("book", "languages", "ageGroups"));
    }

    @Test
    @WithMockUser(username = "user", roles = "CUSTOMER")
    void showAddBookForm_Customer_ShouldBeForbidden() throws Exception {
        mockMvc.perform(get("/books/add"))
                .andExpect(status().isForbidden()); // 403
    }

    @Test
    @WithMockUser(username = "admin", roles = "EMPLOYEE")
    void addBook_ValidData_ShouldRedirect() throws Exception {
        mockMvc.perform(post("/books/add")
                        .with(csrf())
                        .param("name", "New Book")
                        .param("author", "Author")
                        .param("genre", "Fantasy")
                        .param("price", "100.00")
                        .param("quantity", "10")
                        .param("language", "ENGLISH")
                        .param("ageGroup", "ADULT"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService).addBook(any(BookDTO.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = "EMPLOYEE")
    void addBook_InvalidData_ShouldReturnForm() throws Exception {

        mockMvc.perform(post("/books/add")
                        .with(csrf())
                        .param("name", "")
                        .param("price", "100.00"))
                .andExpect(status().isOk())
                .andExpect(view().name("book-add"))
                .andExpect(model().attributeHasFieldErrors("book", "name"))

                .andExpect(model().attributeExists("languages", "ageGroups"));

        verify(bookService, never()).addBook(any());
    }

    @Test
    @WithMockUser(username = "admin", roles = "EMPLOYEE")
    void addBook_ServiceException_ShouldShowErrorMessage() throws Exception {

        doThrow(new RuntimeException("Book exists")).when(bookService).addBook(any());

        mockMvc.perform(post("/books/add")
                        .with(csrf())
                        .param("name", "Duplicate")
                        .param("author", "Author")
                        .param("genre", "Genre")
                        .param("price", "50")
                        .param("quantity", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("book-add"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attribute("errorMessage", "Book exists"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "EMPLOYEE")
    void showEditBookForm_ShouldReturnView() throws Exception {
        Long bookId = 1L;
        BookDTO book = new BookDTO();
        book.setId(bookId);

        when(bookService.getBookById(bookId)).thenReturn(book);

        mockMvc.perform(get("/books/edit/{id}", bookId))
                .andExpect(status().isOk())
                .andExpect(view().name("book-edit"))
                .andExpect(model().attribute("book", hasProperty("id", is(bookId))));
    }

    @Test
    @WithMockUser(username = "admin", roles = "EMPLOYEE")
    void updateBook_Success_ShouldRedirectToManage() throws Exception {
        mockMvc.perform(post("/books/update")
                        .with(csrf())
                        .param("id", "1")
                        .param("name", "Updated Name")
                        .param("author", "Updated Author")
                        .param("genre", "Updated Genre")
                        .param("price", "200")
                        .param("quantity", "20"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/manage"));

        verify(bookService).updateBook(eq(1L), any(BookDTO.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = "EMPLOYEE")
    void deleteBook_ShouldRedirectToManage() throws Exception {
        String bookName = "OldBook";

        mockMvc.perform(post("/books/delete/{name}", bookName)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/manage"));

        verify(bookService).deleteBookByName(bookName);
    }

    @Test
    @WithMockUser(username = "admin", roles = "EMPLOYEE")
    void manageBooks_ShouldReturnEmployeeView() throws Exception {
        Page<BookDTO> page = new PageImpl<>(Collections.emptyList());
        when(bookService.getAllBooks(any(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/books/manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee-books"))
                .andExpect(model().attributeExists("books", "currentPage"));
    }
}