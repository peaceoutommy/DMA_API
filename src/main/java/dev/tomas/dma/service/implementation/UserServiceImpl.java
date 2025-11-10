package dev.tomas.dma.service.implementation;

import dev.tomas.dma.dto.common.UserDTO;
import dev.tomas.dma.entity.User;
import dev.tomas.dma.mapper.UserMapper;
import dev.tomas.dma.repository.UserRepo;
import dev.tomas.dma.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor

public class UserServiceImpl implements UserService {
    private final UserRepo userRepo;
    private final UserMapper userMapper;

    public UserDTO getById(Integer id) {
        User user = userRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return userMapper.toDTO(user);
    }

    public UserDTO getByEmail(String email) {
        User user = userRepo.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
        return userMapper.toDTO(user);
    }

    public List<UserDTO> searchByEmail(String email) {
        List<User> users = userRepo.findByEmailContainingIgnoreCase(email);
        List<UserDTO> dtos = new ArrayList<>();

        for (User user : users) {
            userMapper.toDTO(user);
            dtos.add(userMapper.toDTO(user));
        }
        return dtos;
    }
}
