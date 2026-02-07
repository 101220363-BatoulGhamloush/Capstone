package com.capstone.OpportuGrow.Controller;
import com.capstone.OpportuGrow.Repository.ChatMessageRepository;
import com.capstone.OpportuGrow.Repository.ProjectRepository;
import com.capstone.OpportuGrow.Repository.TransactionRepository;
import com.capstone.OpportuGrow.Repository.UserRepository;
import com.capstone.OpportuGrow.model.*;
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
    private final TransactionRepository transactionRepository;

    public AdminController(TransactionRepository transactionRepository,UserRepository userRepository, ProjectRepository projectRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.transactionRepository=transactionRepository;

    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        // جلب كل البيانات الأساسية
        List<Project> allProjects = projectRepository.findAll();
        List<User> allUsers = userRepository.findAll();
        List<Transaction> allTransactions = transactionRepository.findAll();

        // --- [1] إحصائيات سريعة (Top Summary) ---
        long totalProjects = allProjects.size();
        double totalRevenue = allTransactions.stream().mapToDouble(Transaction::getAmount).sum();
        long activeUsers = allUsers.size();
        long pendingRequests = allProjects.stream().filter(p -> p.getStatus() == ProjectStatus.PENDING).count();

        // --- [2] توزيع المشاريع حسب النوع (Pie Chart) ---
        long charityCount = allProjects.stream().filter(p -> p.getType() == ProjectType.CHARITY).count();
        long fundCount = allProjects.stream().filter(p -> p.getType() == ProjectType.FUND).count();
        long loanCount = allProjects.stream().filter(p -> p.getType() == ProjectType.LOAN).count();

        // --- [3] توزيع الحالات (Doughnut Chart) ---
        long approved = allProjects.stream().filter(p -> p.getStatus() == ProjectStatus.APPROVED).count();
        long completed = allProjects.stream().filter(p -> p.getStatus() == ProjectStatus.COMPLETED).count();
        long rejected = allProjects.stream().filter(p -> p.getStatus() == ProjectStatus.REJECTED).count();

        // --- [4] تحليل الأشهر (Line & Bar Charts) ---
        int[] projectsPerMonth = new int[12];
        double[] fundsPerMonth = new double[12];
        int[] usersPerMonth = new int[12];

        allProjects.forEach(p -> {
            if (p.getCreatedAt() != null) {
                int m = p.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).getMonthValue() - 1;
                projectsPerMonth[m]++;
            }
        });

        // حساب الأموال شهرياً - نسخة LocalDateTime
        allTransactions.forEach(t -> {
            if (t.getTimestamp() != null) {
                // نأخذ رقم الشهر مباشرة وننقص منه 1 ليتناسب مع المصفوفة (0-11)
                int m = t.getTimestamp().getMonthValue() - 1;
                fundsPerMonth[m] += t.getAmount();
            }
        });

        allUsers.forEach(u -> {
            if (u.getCreation() != null) {
                int m = u.getCreation().toInstant().atZone(ZoneId.systemDefault()).getMonthValue() - 1;
                usersPerMonth[m]++;
            }
        });

        // --- [5] حساب الـ Success Rate (Radial/Gauge Chart) ---
        double successRate = totalProjects > 0 ? ((double) completed / totalProjects) * 100 : 0;

        // --- [6] إرسال البيانات للموديل ---
        model.addAttribute("totalProjects", totalProjects);
        model.addAttribute("totalRevenue", String.format("%.2f", totalRevenue));
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("pendingRequests", pendingRequests);

        // Arrays للرسومات البيانية
        model.addAttribute("projectsByMonth", projectsPerMonth);
        model.addAttribute("fundsByMonth", fundsPerMonth);
        model.addAttribute("usersByMonth", usersPerMonth);

        // بيانات الـ Pie/Doughnut
        model.addAttribute("typeData", List.of(charityCount, fundCount, loanCount));
        model.addAttribute("statusData", List.of(pendingRequests, approved, completed, rejected));
        model.addAttribute("successRate", Math.round(successRate));
        model.addAttribute("contentTemplate", "admin-dashboard");

        return "admin-layout";
    }






}