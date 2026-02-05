package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.model.Project;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.capstone.OpportuGrow.Repository.ProjectRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.capstone.OpportuGrow.model.ProjectStatus;

import java.util.List;

@Controller
public class HomeController {
    private final ProjectRepository projectRepository;

    public HomeController(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<Project> projects =
                projectRepository.findByStatus(ProjectStatus.APPROVED);
        model.addAttribute("name", "coding complex");
        model.addAttribute("projects", projects);
        return "index";
    }
}