package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.model.Project;
import com.capstone.OpportuGrow.model.ProjectStatus;
import com.capstone.OpportuGrow.Repository.ProjectRepository;
import com.capstone.OpportuGrow.Repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public HomeController(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<Project> allApproved = projectRepository.findByStatus(ProjectStatus.APPROVED);

        // حساب المبالغ المجموعة (لا حاجة لفحص null إذا كان النوع long)
        double totalRaisedSum = allApproved.stream()
                .mapToDouble(Project::getRaisedAmount)
                .sum();

        // حساب نسبة النجاح (فقط إذا كان الهدف أكبر من صفر لتجنب القسمة على صفر)
        long successfulOnes = allApproved.stream()
                .filter(p -> p.getFundingGoal() > 0 && ((double)p.getRaisedAmount() / p.getFundingGoal()) >= 0.5)
                .count();

        double successRateValue = allApproved.isEmpty() ? 0 : (double) successfulOnes / allApproved.size() * 100;

        // إرسال البيانات للموديل
        model.addAttribute("totalProjects", allApproved.size());
        model.addAttribute("totalFunded", String.format("$%.0fK", totalRaisedSum / 1000));
        model.addAttribute("happyInvestors", userRepository.count());
        model.addAttribute("successRate", String.format("%.0f%%", successRateValue));

        // جلب أول 3 مشاريع للـ Featured
        model.addAttribute("featuredProjects", allApproved.stream().limit(3).collect(Collectors.toList()));

        return "index";
    }

    // Helper Class بسيطة لنقل بيانات التصنيفات للـ HTML
    public static class CategoryDTO {
        private String displayName;
        private String icon;
        private String name;

        public CategoryDTO(String displayName, String icon, String name) {
            this.displayName = displayName;
            this.icon = icon;
            this.name = name;
        }
        public String getDisplayName() { return displayName; }
        public String getIcon() { return icon; }
        public String getName() { return name; }
    }
}