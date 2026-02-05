package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Repository.ProjectRepository;
import com.capstone.OpportuGrow.Repository.UserRepository;
import com.capstone.OpportuGrow.model.Project;
import com.capstone.OpportuGrow.model.ProjectStatus;
import com.capstone.OpportuGrow.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class MyProjectsController {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public MyProjectsController(UserRepository userRepository,
                                ProjectRepository projectRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    @GetMapping("/my-projects")
    public String myProjects(Model model, Principal principal) {

        Optional<User> optionalUser =
                userRepository.findByEmail(principal.getName());

        if (optionalUser.isEmpty()) {
            return "redirect:/";
        }

        User currentUser = optionalUser.get();
        List<Project> projects =
                projectRepository.findByOwner(currentUser);

        model.addAttribute("projects", projects);
        return "my-projects";
    }
    @GetMapping("/projects/edit/{id}")
    public String editProject(@PathVariable Long id,
                              Model model,
                              Principal principal) {

        Project project = projectRepository.findById(id).orElseThrow();
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();

        // حماية
        if (!project.getOwner().equals(user)
                || project.getStatus() != ProjectStatus.PENDING) {
            return "redirect:/my-projects";
        }

        model.addAttribute("project", project);
        return "edit-project";
    }
    @PostMapping("/projects/edit/{id}")
    public String updateProject(@PathVariable Long id,
                                Project formProject,
                                Principal principal) {

        Project project = projectRepository.findById(id).orElseThrow();
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();

        if (!project.getOwner().equals(user)
                || project.getStatus() != ProjectStatus.PENDING) {
            return "redirect:/my-projects";
        }

        // 1️⃣ تحديث القيم من الفورم
        project.setTitle(formProject.getTitle());
        project.setShortDescription(formProject.getShortDescription());
        project.setFundingGoal(formProject.getFundingGoal());
        project.setFundingDuration(formProject.getFundingDuration());

        // 2️⃣ الحساب على القيمة الجديدة
        BigDecimal goal = BigDecimal.valueOf(formProject.getFundingGoal());

        BigDecimal platformFee = goal.multiply(new BigDecimal("0.05")); // 5%
        BigDecimal processingFee = goal.multiply(new BigDecimal("0.02")); // 2%

        BigDecimal amountReceive = goal
                .subtract(platformFee)
                .subtract(processingFee);

        project.setPlatformFee(platformFee);
        project.setProcessingFee(processingFee);
        project.setAmountYouWillReceive(amountReceive);

        // ❌ احذفي هالسطر نهائياً
        // project.setAmountFunded(formProject.getAmountFunded());

        projectRepository.save(project);
        return "redirect:/my-projects";
    }

    @PostMapping("/projects/delete/{id}")
    public String deleteProject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Project> projectOpt = projectRepository.findById(id);
        if (projectOpt.isPresent()) {
            projectRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "✅ Project '" + projectOpt.get().getTitle() + "' deleted successfully");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } else {
            redirectAttributes.addFlashAttribute("message", "⚠️ Project not found");
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/my-projects";
    }





}

