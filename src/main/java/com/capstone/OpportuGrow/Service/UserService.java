package com.capstone.OpportuGrow.Service;

import com.capstone.OpportuGrow.Repository.ConsultantRepository;
import com.capstone.OpportuGrow.Repository.UserRepository;
import com.capstone.OpportuGrow.model.Consultant;
import com.capstone.OpportuGrow.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service

public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ConsultantRepository consultantRepository;


    public UserService(UserRepository userRepository,ConsultantRepository consultantRepository) {
        this.userRepository = userRepository;
        this.consultantRepository=consultantRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // single role
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole()); // إذا role مخزنة كـ "ADMIN" أو "MEMBER"

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(authority)
        );
    }
    public User saveUser(User user) {
        // تشفير الباسورد

        User savedUser = userRepository.save(user);

        // إذا كان consultant، أضف record بالـ consultant table
        if ("CONSULTANT".equalsIgnoreCase(String.valueOf(user.getRole()))) {
            Consultant consultant = new Consultant();
            consultant.setUser(savedUser);
            consultant.setSpecialty("General"); // مثال، ممكن تعدل حسب الحاجة
            consultantRepository.save(consultant);
        }

        return savedUser;
    }




}



