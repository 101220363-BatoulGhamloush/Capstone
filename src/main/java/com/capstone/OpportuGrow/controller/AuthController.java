package com.capstone.OpportuGrow.controller;

import com.capstone.OpportuGrow.dto.ApiResponseDTO;
import com.capstone.OpportuGrow.dto.RegistrationRequestDTO;
import com.capstone.OpportuGrow.entity.User;
import com.capstone.OpportuGrow.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000") // Allow frontend to connect
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO> registerUser(@Valid @RequestBody RegistrationRequestDTO request) {
        try {
            User user = userService.registerUser(request);
            return ResponseEntity.ok(
                    new ApiResponseDTO(true, "User registered successfully! Please check your email for verification.")
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDTO(false, e.getMessage()));
        }
    }

    // Simple test endpoint to check if backend is working
    @GetMapping("/test")
    public ResponseEntity<ApiResponseDTO> test() {
        return ResponseEntity.ok(new ApiResponseDTO(true, "Backend is working!"));
    }
}