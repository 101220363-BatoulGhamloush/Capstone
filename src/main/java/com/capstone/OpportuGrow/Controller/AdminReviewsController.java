package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Repository.ProjectRepository;
import com.capstone.OpportuGrow.Service.NotificationService;
import com.capstone.OpportuGrow.Service.ProjectService;
import com.capstone.OpportuGrow.model.Project;
import com.capstone.OpportuGrow.model.ProjectStatus;
import com.capstone.OpportuGrow.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminReviewsController {

    private final ProjectRepository projectRepository;

    private final ProjectService projectService;
    private final NotificationService notificationService;


    public AdminReviewsController(NotificationService notificationService,ProjectRepository projectRepository, ProjectService projectService) {
        this.projectRepository = projectRepository;
        this.projectService = projectService;
        this.notificationService=notificationService;
    }

    @PostMapping("/approve/{id}")
    public String approveProject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        projectService.approveProject(id);
        redirectAttributes.addFlashAttribute("success", "Project approved successfully!");
        return "redirect:/admin/reviews";
    }

    @PostMapping("/reject/{id}")
    public String rejectProject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // 1. جلب المشروع ومعلومات صاحبه "قبل الحذف"
        Project project = projectRepository.findById(id).orElse(null);

        if (project != null) {
            User owner = project.getOwner();
            String projectName = project.getTitle();

            // 2. إرسال النوتيفيكاشن أولاً (لأننا بحاجة لـ User object)
            notificationService.sendRejectionNotification(owner, projectName);

            // 3. الآن يمكنك حذف المشروع أو تغييره عبر الـ Service
            projectService.rejectProject(id);

            redirectAttributes.addFlashAttribute("success", "Project rejected and owner notified via Chat link!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Project not found!");
        }

        return "redirect:/admin/reviews";
    }
    @GetMapping("/reviews")
    public String reviewProjects(Model model) {
        List<Project> pendingProjects = projectRepository.findByStatus(ProjectStatus.PENDING);

        // Stats
        long pendingCount = pendingProjects.size();
        long todayCount = pendingProjects.stream()
                .filter(p -> {
                    Date created = p.getCreatedAt();
                    LocalDate createdDate = created.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    return createdDate.isEqual(LocalDate.now());
                })
                .count();

        long approvedCount = projectRepository.findByStatus(ProjectStatus.APPROVED).size();
        long rejectedCount = projectRepository.findByStatus(ProjectStatus.REJECTED).size();

        model.addAttribute("pendingProjects", pendingProjects);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("todayCount", todayCount);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("rejectedCount", rejectedCount);
        model.addAttribute("activePage", "reviews");
        model.addAttribute("contentTemplate", "admin-reviews");

        return "admin-layout"; // اسم الـ HTML
    }


}