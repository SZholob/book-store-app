package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;


    @Override
    @PreAuthorize("hasRole('EMPLOYEE')")
    public Page<ClientDTO> getAllClients(String keyword, Boolean isBlocked, Pageable pageable) {
        Page<Client> clientPage;

        if (keyword != null && !keyword.isEmpty()) {
            clientPage = clientRepository.findByEmailContainingIgnoreCaseOrNameContainingIgnoreCase(keyword, keyword, pageable);
        } else if (isBlocked != null){
            clientPage = clientRepository.findByIsBlocked(isBlocked, pageable);
        } else {
            clientPage = clientRepository.findAll(pageable);
        }

        return clientPage.map(client -> modelMapper.map(client, ClientDTO.class));
    }

    @Override
    public ClientDTO getClientByEmail(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(()-> new NotFoundException("Client not found: " + email));
        return modelMapper.map(client, ClientDTO.class);
    }

    @Override
    @Transactional
    public ClientDTO addClient(ClientDTO clientDTO) {
        if (clientRepository.findByEmail(clientDTO.getEmail()).isPresent()){
            throw new AlreadyExistException("Client already exists: " + clientDTO.getEmail());
        }
        Client client = modelMapper.map(clientDTO, Client.class);
        client.setRole(Role.CUSTOMER);
        client.setPassword(passwordEncoder.encode(client.getPassword()));
        return modelMapper.map(clientRepository.save(client), ClientDTO.class);

    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('EMPLOYEE')")
    public void blockClient(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found: " + email));

        client.setBlocked(true);
        clientRepository.save(client);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('EMPLOYEE')")
    public void unblockClient(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found: " + email));
        client.setBlocked(false);
        clientRepository.save(client);
    }

    @Override
    @Transactional
    public void updateMyProfile(String email, ClientDTO clientDTO) {
        Client existingClient = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));

        existingClient.setName(clientDTO.getName());

        if (clientDTO.getPassword() != null && !clientDTO.getPassword().isBlank()) {
            existingClient.setPassword(passwordEncoder.encode(clientDTO.getPassword()));
        }

        Client savedClient = clientRepository.save(existingClient);
        modelMapper.map(savedClient, ClientDTO.class);
    }

    @Override
    public void deleteMyAccount(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));

        clientRepository.delete(client);
    }

    @Override
    @Transactional
    public ClientDTO updateClientByEmail(String email, ClientDTO clientDTO) {
        Client existingClient = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found: " + email));

        modelMapper.map(clientDTO, existingClient);

        if (clientDTO.getPassword() != null && !clientDTO.getPassword().isEmpty()) {
            existingClient.setPassword(passwordEncoder.encode(clientDTO.getPassword()));
        }

        return modelMapper.map(clientRepository.save(existingClient), ClientDTO.class);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('EMPLOYEE')")
    public void deleteClientByEmail(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found: " + email));
        clientRepository.delete(client);
    }
}
