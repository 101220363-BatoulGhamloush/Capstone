package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Repository.*;
import com.capstone.OpportuGrow.model.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/consultants")
public class AdminConsultantController {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ArticleRepository articleRepository;
    private final AppointmentRepository appointmentRepository;
    private final ConsultantRepository consultantRepository;

    public AdminConsultantController(
            UserRepository userRepository,
            ProjectRepository projectRepository,
            ArticleRepository articleRepository,
            ConsultantRepository consultantRepository,
            AppointmentRepository appointmentRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.articleRepository = articleRepository;
        this.appointmentRepository = appointmentRepository;
        this.consultantRepository=consultantRepository;
    }

    // قائمة كل الاستشاريين
    @GetMapping
    public String listConsultants(Model model) {
        List<User> consultants = userRepository.findByRole(Role.CONSULTANT);
        Long totalConsultants = userRepository.countByRole(Role.CONSULTANT);
        int activeConsultants = userRepository.countByRoleAndActive(Role.CONSULTANT, true);
        int inactiveConsultants = userRepository.countByRoleAndActive(Role.CONSULTANT, false);

        model.addAttribute("consultants", consultants);
        model.addAttribute("totalConsultants", totalConsultants);
        model.addAttribute("activeConsultants", activeConsultants);
        model.addAttribute("inactiveConsultants", inactiveConsultants);
        model.addAttribute("activePage", "consultants");
        model.addAttribute("contentTemplate", "admin-consultants");
        return "admin-layout";
    }
    @PostMapping("/toggle-status/{id}")
    public String toggleConsultantStatus(@PathVariable int id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));

        user.setActive(!user.isActive()); // toggle
        userRepository.save(user);

        return "redirect:/admin/consultants";
    }


    // صفحة تفاصيل استشاري
    @GetMapping("/view/{id}")
    public String viewConsultant(@PathVariable int id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid consultant Id:" + id));

        // المشاريع، المقالات، والمواعيد المرتبطة بالاستشاري
        List<Project> assignedProjects = projectRepository.findByConsultantId(id);
        List<Article> consultantArticles = articleRepository.findByConsultantId(id);
        List<Appointment> appointments = appointmentRepository.findByConsultantId(id);

        model.addAttribute("user", user);
        model.addAttribute("assignedProjects", assignedProjects);
        model.addAttribute("consultantArticles", consultantArticles);
        model.addAttribute("appointments", appointments);

        return "admin-consultants-view";
    }


    // إنشاء استشاري جديد
    @GetMapping("/create")
    public String createConsultantForm(Model model) {
        User user = new User();
        user.setRole(Role.CONSULTANT); // افتراضياً يكون مستشار
        model.addAttribute("user", user);
        return "admin-consultants-create";
    }

    @PostMapping("/create")
    public String createConsultantSubmit(@ModelAttribute User user) {
        userRepository.save(user);
        return "redirect:/admin/consultants";
    }

    // تعديل استشاري
    @GetMapping("/edit/{id}")
    public String editConsultantForm(@PathVariable int id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid consultant Id:" + id));
        model.addAttribute("user", user);
        return "admin-consultants-edit";
    }

    @PostMapping("/edit/{id}")
    public String editConsultantSubmit(@PathVariable int id, @ModelAttribute User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid consultant Id:" + id));

        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        user.setPhone(userDetails.getPhone());
        user.setAddress(userDetails.getAddress());
        user.setActive(userDetails.isActive());

        userRepository.save(user);
        return "redirect:/admin/consultants/view/" + id;
    }

    // حذف استشاري
    @PostMapping("/delete/{userId}")
    @Transactional
    public String deleteUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {

        // 1. البحث عن كل Consultants المرتبطين بالـ User
        List<Consultant> consultants = consultantRepository.findByUserId(userId);

        if (!consultants.isEmpty()) {
            // مسح كل Consultants أول
            consultantRepository.deleteAll(consultants);
        }

        // 2. بعدين مسح الـ User
        userRepository.findById(Math.toIntExact(userId)).ifPresentOrElse(user -> {
            userRepository.delete(user);
            redirectAttributes.addFlashAttribute("success", "User and related consultants deleted successfully.");
        }, () -> {
            redirectAttributes.addFlashAttribute("error", "User not found.");
        });

        return "redirect:/admin/consultants";
    }









}

