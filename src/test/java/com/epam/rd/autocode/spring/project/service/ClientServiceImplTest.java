package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.service.impl.ClientServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClientServiceImplTest {

    @Mock private ClientRepository clientRepository;
    @Mock private ModelMapper modelMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private ClientServiceImpl clientService;

    @Test
    void getClientByEmail_ShouldReturnClient() {
        String email = "test@client.com";
        Client client = new Client();
        client.setEmail(email);

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
        when(modelMapper.map(client, ClientDTO.class)).thenReturn(new ClientDTO());

        assertNotNull(clientService.getClientByEmail(email));
    }

    @Test
    void addClient_ShouldThrowAlreadyExist() {
        ClientDTO dto = new ClientDTO();
        dto.setEmail("exist@client.com");

        when(clientRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(new Client()));

        assertThrows(AlreadyExistException.class, () -> clientService.addClient(dto));
    }

    @Test
    void addClient_ShouldSaveNewClient() {
        ClientDTO dto = new ClientDTO();
        dto.setEmail("new@client.com");
        dto.setPassword("pass");

        Client client = new Client();

        when(clientRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(modelMapper.map(dto, Client.class)).thenReturn(client);
        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");
        when(clientRepository.save(client)).thenReturn(client);
        when(modelMapper.map(client, ClientDTO.class)).thenReturn(dto);

        ClientDTO result = clientService.addClient(dto);

        assertNotNull(result);
        verify(passwordEncoder).encode("pass");
        verify(clientRepository).save(client);
    }

    @Test
    void updateClient_ShouldUpdatePassword_WhenProvided() {
        String email = "update@client.com";
        ClientDTO dto = new ClientDTO();
        dto.setPassword("newPass");
        Client client = new Client();

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");
        when(clientRepository.save(client)).thenReturn(client);
        when(modelMapper.map(client, ClientDTO.class)).thenReturn(dto);

        clientService.updateClientByEmail(email, dto);

        verify(passwordEncoder).encode("newPass");
        verify(clientRepository).save(client);
    }

    @Test
    void deleteClient_ShouldDelete() {
        String email = "del@client.com";
        Client client = new Client();
        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));

        clientService.deleteClientByEmail(email);

        verify(clientRepository).delete(client);
    }
}
