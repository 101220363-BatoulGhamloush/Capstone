package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Repository.AppointmentRepository;
import com.capstone.OpportuGrow.Repository.ProjectRepository;
import com.capstone.OpportuGrow.Repository.UserRepository;
import com.capstone.OpportuGrow.model.Appointment;
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
    private AppointmentRepository appointmentRepository;

    public DashboardController(AppointmentRepository appointmentRepository,ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.appointmentRepository=appointmentRepository;
    }

    @GetMapping("/dashboard")
    public String userDashboard(Model model, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        List<Project> allProjects = projectRepository.findAll();
        List<Project> myProjects = projectRepository.findByOwner(user);

        // 1. إحصائيات حسب نوع التمويل (Loan vs Fund vs Charity)
        model.addAttribute("loanCount", myProjects.stream().filter(p -> "Loan".equalsIgnoreCase(String.valueOf(p.getType()))).count());
        model.addAttribute("fundCount", myProjects.stream().filter(p -> "Fund".equalsIgnoreCase(String.valueOf(p.getType()))).count());
        model.addAttribute("charityCount", myProjects.stream().filter(p -> "Charity".equalsIgnoreCase(String.valueOf(p.getType()))).count());

        // 2. إحصائيات حالة المشاريع (Status)
        long completed = myProjects.stream().filter(p -> p.getRaisedAmount() >= p.getFundingGoal()).count();
        long active = myProjects.size() - completed;
        model.addAttribute("completedCount", completed);
        model.addAttribute("activeCount", active);

        // 3. إحصائيات المستخدمين والاجتماعات (Platform Level)
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalMeetings", appointmentRepository.count());
        model.addAttribute("myMeetings", appointmentRepository.findByUserOrderByDateAscTimeAsc(user).size());

        model.addAttribute("user", user);
        model.addAttribute("myProjects", myProjects);

        return "dashboard";
    }
}