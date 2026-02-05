package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Dto.RegisterDto;
import com.capstone.OpportuGrow.Repository.UserRepository;
import com.capstone.OpportuGrow.model.Role;
import com.capstone.OpportuGrow.model.User;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Controller
@RequestMapping("/register")

public class RegisterController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String register(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        model.addAttribute("success", false);
        return "register";
    }

    @PostMapping
    public String register(
            @Valid @ModelAttribute("registerDto") RegisterDto registerDto,
            BindingResult bindingResult,
            Model model) {

        if (!registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
            bindingResult.addError(
                    new FieldError("registerDto", "confirmPassword", "Passwords do not match")
            );
        }

        if (userRepository.findByEmail(registerDto.getEmail()).isPresent()) {
            bindingResult.addError(
                    new FieldError("registerDto", "email", "Email already exists")
            );
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        User user = new User();
        user.setName(registerDto.getName());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setPhone(registerDto.getPhone());
        user.setAddress(registerDto.getAddress());
        user.setRole(Role.MEMBER);
        user.setCreation(new Date());
        user.setActive(true);



        userRepository.save(user);

        model.addAttribute("success", true);

        return "register";
    }
}

