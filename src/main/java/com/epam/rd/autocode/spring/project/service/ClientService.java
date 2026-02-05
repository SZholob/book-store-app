package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClientService {

    Page<ClientDTO> getAllClients(String keyword,Boolean isBlocked, Pageable pageable);

    ClientDTO getClientByEmail(String email);

    ClientDTO addClient(ClientDTO client);

    void blockClient(String email);

    void unblockClient(String email);

    void updateMyProfile(String email, ClientDTO clientDTO);

    void deleteMyAccount(String email);
}
