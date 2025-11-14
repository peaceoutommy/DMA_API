package dev.tomas.dma.controller;

import dev.tomas.dma.dto.common.UserDTO;
import dev.tomas.dma.service.UserService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getById(@Positive @NotNull @PathVariable Integer id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getByEmail(@NotBlank @PathVariable String email) {
        return ResponseEntity.ok(userService.getByEmail(email));
    }

    @GetMapping("/search/{email}")
    public ResponseEntity<List<UserDTO>> searchByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.searchByEmail(email));
    }
}
