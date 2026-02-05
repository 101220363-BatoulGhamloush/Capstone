package com.capstone.OpportuGrow.Controller;
import com.capstone.OpportuGrow.Repository.ChatMessageRepository;
import com.capstone.OpportuGrow.Repository.ProjectRepository;
import com.capstone.OpportuGrow.Repository.UserRepository;
import com.capstone.OpportuGrow.model.ChatMessage;
import com.capstone.OpportuGrow.model.Project;
import com.capstone.OpportuGrow.model.ProjectStatus;
import com.capstone.OpportuGrow.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.time.Month;
import java.time.LocalDate;

@Controller
@RequestMapping("/admin")

    public class AdminController {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;


    public AdminController(UserRepository userRepository, ProjectRepository projectRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;

    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        List<Project> projects = projectRepository.findAll();

        int totalProjects = projects.size();
        long totalRaised = projects.stream().mapToLong(Project::getRaisedAmount).sum();
        // تأكد إن ميثود isActive موجودة بكلاس الـ User
        long activeUsers = userRepository.findAll().stream().filter(User::isActive).count();

        long pendingProjects = projects.stream().filter(p -> p.getStatus() == ProjectStatus.PENDING).count();
        long approvedProjects = projects.stream().filter(p -> p.getStatus() == ProjectStatus.APPROVED).count();
        long rejectedProjects = projects.stream().filter(p -> p.getStatus() == ProjectStatus.REJECTED).count();

        // Monthly stats
        int[] projectsPerMonth = new int[12];
        long[] fundsPerMonth = new long[12];

        projects.forEach(p -> {
            if (p.getCreatedAt() != null) {
                // تحويل التاريخ لـ Month Index (0-11)
                int month = p.getCreatedAt().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .getMonthValue() - 1;
                projectsPerMonth[month]++;
                fundsPerMonth[month] += p.getRaisedAmount();
            }
        });

        model.addAttribute("totalProjects", totalProjects);
        model.addAttribute("totalRaised", totalRaised);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("pendingProjects", pendingProjects);
        model.addAttribute("approvedProjects", approvedProjects);
        model.addAttribute("rejectedProjects", rejectedProjects);

        // تحويل الـ Arrays لـ List كرمال Thymeleaf يقرأهم كـ JSON Arrays بالـ JS
        model.addAttribute("projectsPerMonth", projectsPerMonth);
        model.addAttribute("fundsPerMonth", fundsPerMonth);
        model.addAttribute("months", List.of("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"));

        // حساب الـ Success Rate مع تقريب الرقم (Formatting)
        double rawSuccessRate = totalProjects > 0 ? ((double) approvedProjects / totalProjects * 100) : 0;
        model.addAttribute("successRate", Math.round(rawSuccessRate * 10.0) / 10.0);

        model.addAttribute("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
        model.addAttribute("pageTitle", "Admin Dashboard");
        model.addAttribute("contentTemplate", "admin-dashboard");

        return "admin-layout"; // تأكد إنك عم ترجع الـ Layout الأساسي
    }; // بدون .html






}