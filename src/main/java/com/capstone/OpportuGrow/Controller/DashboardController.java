package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Repository.ProjectRepository;
import com.capstone.OpportuGrow.Repository.UserRepository;
import com.capstone.OpportuGrow.model.Project;
import com.capstone.OpportuGrow.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
public class DashboardController {
    private final ProjectRepository projectRepository;
    private UserRepository userRepository;

    public DashboardController(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String userDashboard(Model model, Principal principal) {

        Optional<User> optionalUser = userRepository.findByEmail(principal.getName());

        // عرف المتغير هون
        List<Project> myProjects;

        if (optionalUser.isPresent()) {
            User user = optionalUser.get(); // فك الـ Optional
            myProjects = projectRepository.findByOwner(user);
        } else {
            // حال ما لقينا المستخدم
            myProjects = Collections.emptyList();
        }

        // إحصائيات شخصية
        int myProjectsCount = myProjects.size();
        double myTotalRaised = myProjects.stream().mapToDouble(Project::getRaisedAmount).sum();

        // إحصائيات عامة للموقع
        long totalProjects = projectRepository.count();
        long totalUsers = userRepository.count();
        double totalRaised = projectRepository.findAll().stream().mapToDouble(Project::getRaisedAmount).sum();

        model.addAttribute("myProjects", myProjects);
        model.addAttribute("myProjectsCount", myProjectsCount);
        model.addAttribute("myTotalRaised", myTotalRaised);

        model.addAttribute("totalProjects", totalProjects);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalRaised", totalRaised);

        return "dashboard";
    }
}