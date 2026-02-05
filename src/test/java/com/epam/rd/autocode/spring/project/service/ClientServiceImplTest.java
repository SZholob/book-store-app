package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.service.impl.ClientServiceImpl;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private ClientServiceImpl clientService;

    @Test
    void getAllClients_NoFilter_ShouldReturnAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Client client = new Client();
        client.setEmail("test@email.com");
        Page<Client> page = new PageImpl<>(Collections.singletonList(client));

        when(clientRepository.findAll(pageable)).thenReturn(page);

        Page<ClientDTO> result = clientService.getAllClients(null, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("test@email.com", result.getContent().get(0).getEmail());
        verify(clientRepository).findAll(pageable);
    }

    @Test
    void getAllClients_WithKeyword_ShouldFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        String keyword = "john";
        Client client = new Client();
        client.setName("John Doe");
        Page<Client> page = new PageImpl<>(Collections.singletonList(client));

        when(clientRepository.findByEmailContainingIgnoreCaseOrNameContainingIgnoreCase(keyword, keyword, pageable))
                .thenReturn(page);

        Page<ClientDTO> result = clientService.getAllClients(keyword, null, pageable);

        assertEquals(1, result.getTotalElements());
        verify(clientRepository).findByEmailContainingIgnoreCaseOrNameContainingIgnoreCase(keyword, keyword, pageable);
    }

    @Test
    void getAllClients_WithBlockedStatus_ShouldFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Client client = new Client();
        client.setBlocked(true);
        Page<Client> page = new PageImpl<>(Collections.singletonList(client));

        when(clientRepository.findByIsBlocked(true, pageable)).thenReturn(page);

        Page<ClientDTO> result = clientService.getAllClients(null, true, pageable);

        assertEquals(1, result.getTotalElements());
        verify(clientRepository).findByIsBlocked(true, pageable);
    }

    @Test
    void getClientByEmail_Success() {
        String email = "test@email.com";
        Client client = new Client();
        client.setEmail(email);
        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));

        ClientDTO result = clientService.getClientByEmail(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
    }

    @Test
    void getClientByEmail_NotFound() {
        String email = "unknown@email.com";
        when(clientRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> clientService.getClientByEmail(email));
    }

    @Test
    void addClient_Success() {
        ClientDTO dto = new ClientDTO();
        dto.setEmail("new@email.com");
        dto.setPassword("rawPassword");

        when(clientRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(clientRepository.save(any(Client.class))).thenAnswer(i -> i.getArgument(0));

        ClientDTO result = clientService.addClient(dto);

        assertNotNull(result);
        assertEquals("encodedPassword", result.getPassword()); // Перевіряємо, що пароль захешовано
        // Оскільки ми використовуємо Spy на ModelMapper, поле password в result буде захешоване,
        // бо save повертає об'єкт з encodedPassword, який мапиться назад в DTO.

        verify(clientRepository).save(argThat(client ->
                client.getRole() == Role.CUSTOMER &&
                        client.getPassword().equals("encodedPassword")
        ));
    }

    @Test
    void addClient_AlreadyExists() {
        ClientDTO dto = new ClientDTO();
        dto.setEmail("exist@email.com");

        when(clientRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(new Client()));

        assertThrows(AlreadyExistException.class, () -> clientService.addClient(dto));
        verify(clientRepository, never()).save(any());
    }

    @Test
    void blockClient_Success() {
        String email = "user@email.com";
        Client client = new Client();
        client.setBlocked(false);

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));

        clientService.blockClient(email);

        assertTrue(client.isBlocked());
        verify(clientRepository).save(client);
    }

    @Test
    void unblockClient_Success() {
        String email = "user@email.com";
        Client client = new Client();
        client.setBlocked(true);

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));

        clientService.unblockClient(email);

        assertFalse(client.isBlocked());
        verify(clientRepository).save(client);
    }

    @Test
    void updateMyProfile_Success_WithPassword() {
        String email = "user@email.com";
        ClientDTO dto = new ClientDTO();
        dto.setName("New Name");
        dto.setPassword("NewPass");

        Client client = new Client();
        client.setEmail(email);
        client.setName("Old Name");
        client.setPassword("OldHash");

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
        when(passwordEncoder.encode("NewPass")).thenReturn("NewHash");
        when(clientRepository.save(any(Client.class))).thenAnswer(i -> i.getArgument(0));

        clientService.updateMyProfile(email, dto);

        assertEquals("New Name", client.getName());
        assertEquals("NewHash", client.getPassword());
        verify(clientRepository).save(client);
    }

    @Test
    void updateMyProfile_Success_NoPassword() {
        String email = "user@email.com";
        ClientDTO dto = new ClientDTO();
        dto.setName("New Name");
        dto.setPassword("");

        Client client = new Client();
        client.setEmail(email);
        client.setName("Old Name");
        client.setPassword("OldHash");

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));

        clientService.updateMyProfile(email, dto);

        assertEquals("New Name", client.getName());
        assertEquals("OldHash", client.getPassword());
        verify(clientRepository).save(client);
    }

    @Test
    void deleteMyAccount_Success() {
        String email = "bye@email.com";
        Client client = new Client();
        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));

        clientService.deleteMyAccount(email);

        verify(clientRepository).delete(client);
    }
}