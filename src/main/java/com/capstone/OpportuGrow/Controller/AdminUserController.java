package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Dto.UserTableDto;
import com.capstone.OpportuGrow.Repository.ConsultantRepository;
import com.capstone.OpportuGrow.Repository.UserRepository;
import com.capstone.OpportuGrow.model.Consultant;
import com.capstone.OpportuGrow.model.Role;
import com.capstone.OpportuGrow.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.capstone.OpportuGrow.Config.SecurityConfig;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ConsultantRepository consultantRepository;

    @GetMapping
    public String listUsers(@RequestParam(required=false) String search,
                            @RequestParam(required=false) String status,
                            Model model) {

        List<User> users = userRepository.findAll();

        // Filter by search
        if (search != null && !search.isEmpty()) {
            users = users.stream()
                    .filter(u -> u.getName().toLowerCase().contains(search.toLowerCase())
                            || u.getEmail().toLowerCase().contains(search.toLowerCase()))
                    .toList();
        }

        // Filter by status
        if ("active".equals(status)) {
            users = users.stream().filter(User::isActive).toList();
        } else if ("inactive".equals(status)) {
            users = users.stream().filter(u -> !u.isActive()).toList();
        }

        model.addAttribute("users", users);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("totalUsers", users.size());
        model.addAttribute("activeUsers", users.stream().filter(User::isActive).count());
        model.addAttribute("inactiveUsers", users.stream().filter(u -> !u.isActive()).count());
        long adminUsers = users.stream().filter(u -> u.getRole() == Role.ADMIN).count(); // ⚠ هنا
        model.addAttribute("adminUsers", adminUsers);
        model.addAttribute("pageTitle", "Users");
        model.addAttribute("activePage", "users");
        model.addAttribute("contentTemplate", "admin-users"); // Thymeleaf template
        return "admin-layout"; // layout الأساسي


    }



    @PostMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable int id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setActive(!user.isActive());
        userRepository.save(user);
        return "redirect:/admin/users";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable int id) {
        User user = userRepository.findById(id).orElseThrow();
        if(user.getRole() == Role.CONSULTANT) {
            Consultant consultant = consultantRepository.findByUser(user);
            if(consultant != null) {
                consultantRepository.delete(consultant);
            }
        }
        userRepository.deleteById(user.getId());
        return "redirect:/admin/users";
    }
    // صفحة إنشاء مستخدم
    @GetMapping("/create")
    public String createUserForm(Model model) {
        model.addAttribute("user", new User());
        return "admin-users-create"; // Thymeleaf template
    }

    // حفظ المستخدم الجديد
    @PostMapping("/create")
    public String saveUser(@ModelAttribute User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        user.setCreation(new Date());

        if(user.getRole() == Role.CONSULTANT) {
            Consultant consultant = new Consultant();
            consultant.setUser(user);
            consultant.setName(user.getName());
            consultant.setEmail(user.getEmail());
            consultant.setPassword(user.getPassword());
            consultant.getSpecialty();
            consultant.setActive(true);
            consultantRepository.save(consultant);
        }
        return "redirect:/admin/users"; // بعد الحفظ يرجع لصفحة الـ users
    }
    @GetMapping("/view/{id}")
    public String viewUser(@PathVariable int id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);
        return "admin-users-view";
    }
    @GetMapping("/edit/{id}")
    public String editUser(@PathVariable Integer id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);
        model.addAttribute("roles", Role.values()); // USER / ADMIN
        return "admin-users-edit";
    }
    @PostMapping("/edit/{id}")
    public String updateUser(
            @PathVariable Integer id,
            @ModelAttribute("user") User formUser,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String confirmPassword,
            RedirectAttributes redirectAttributes
    ) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(formUser.getName());
        user.setEmail(formUser.getEmail());
        user.setPhone(formUser.getPhone());
        user.setAddress(formUser.getAddress());
        user.setRole(formUser.getRole());
        user.setActive(formUser.isActive());
        // change password only if admin entered one
        if (newPassword != null && !newPassword.isBlank()) {

            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute(
                        "error", "Passwords do not match"
                );
                return "redirect:/admin/users/edit/" + id;
            }


            user.setPassword(passwordEncoder.encode(newPassword));
        }

        // ❌ لا تغيّري creation
        userRepository.save(user);

        return "redirect:/admin/users/view/" + id;
    }


}



