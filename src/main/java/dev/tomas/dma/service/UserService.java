package dev.tomas.dma.service;

import dev.tomas.dma.dto.common.UserDTO;

import java.util.List;

public interface UserService {
    UserDTO getById(Integer id);
    UserDTO getByEmail(String email);
    List<UserDTO> searchByEmail(String email);
}
