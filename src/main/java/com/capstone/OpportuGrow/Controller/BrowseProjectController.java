package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Repository.CommentRepository;
import com.capstone.OpportuGrow.Repository.ProjectRepository;
import com.capstone.OpportuGrow.Repository.TransactionRepository;
import com.capstone.OpportuGrow.Repository.UserRepository;
import com.capstone.OpportuGrow.model.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Controller
@RequestMapping("/projects")
public class BrowseProjectController {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final CommentRepository commentRepository;

    public BrowseProjectController(CommentRepository commentRepository,TransactionRepository transactionRepository,ProjectRepository projectRepository,UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository=userRepository;
        this.transactionRepository=transactionRepository;
        this.commentRepository=commentRepository;
    }

    @GetMapping
    public String browseProjects(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ProjectType type,
            @RequestParam(defaultValue = "1") int page,
            Model model,
            Principal principal
    ) {
        // Fetch projects according to filters
        List<Project> projects;
        if ((keyword == null || keyword.isEmpty()) && type == null) {
            projects = projectRepository.findByStatus(ProjectStatus.APPROVED);
        } else if (keyword != null && !keyword.isEmpty() && type != null) {
            projects = projectRepository.searchApprovedByType(keyword, type);
        } else if (keyword != null && !keyword.isEmpty()) {
            projects = projectRepository.searchApprovedProjects(keyword);
        } else { // type != null
            projects = projectRepository.findByStatusAndType(ProjectStatus.APPROVED, type);
        }

        // إذا عندك userRepository
        Optional<User> currentUser = principal != null ? userRepository.findByEmail(principal.getName()) : null;
        for (Project project : projects) {
            project.calculateFundingPercent(); // يحسب funding %
            if (currentUser != null) {
                project.setLikedByCurrentUser(project.getLikedUsers().contains(currentUser));
            } else {
                project.setLikedByCurrentUser(false);
            }
        }


        // Pagination
        int pageSize = 6; // projects per page
        int totalProjects = projects.size();
        int totalPages = (int) Math.ceil((double) totalProjects / pageSize);
        page = Math.max(1, Math.min(page, totalPages)); // clamp page number

        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalProjects);

        List<Project> pageProjects = projects.subList(fromIndex, toIndex);
        Page<Project> projectPage = new PageImpl<>(pageProjects, PageRequest.of(page - 1, pageSize), totalProjects);

        // Add attributes
        model.addAttribute("projects", pageProjects);
        model.addAttribute("keyword", keyword);
        model.addAttribute("type", type);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "projects";
    }

    // Like project
    @PostMapping("/{id}/like")
    public String likeProject(@PathVariable Integer id, Principal principal) {
        if (principal == null) return "redirect:/login";

        Project project = projectRepository.findById(Long.valueOf(id)).orElseThrow();
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Toggle Logic: إذا موجود بشيله، إذا مش موجود بزيذه
        if (project.getLikedUsers().contains(user)) {
            project.getLikedUsers().remove(user);
        } else {
            project.getLikedUsers().add(user);
        }

        projectRepository.save(project);
        return "redirect:/projects";
    }

    @PostMapping("/{id}/comment")
    public String addComment(@PathVariable Integer id, @RequestParam String content, Principal principal) {
        if (principal == null) return "redirect:/login";

        Project project = projectRepository.findById(Long.valueOf(id)).orElseThrow();
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setProject(project);
        comment.setUser(user);
        comment.setCreatedAt(java.time.LocalDateTime.now());

        commentRepository.save(comment); // تأكد إنك عملت Autowired للـ commentRepository

        return "redirect:/projects/" + id; // بيرجعك لصفحة تفاصيل المشروع
    }

    @GetMapping("/{id}/agreement")
    public String showAgreementPage(
            @PathVariable Long id,
            @RequestParam("amount") Double amount, // Hon mnekhod l-mablagh mn l-modal
            Model model,
            HttpServletRequest request,
            Principal principal) {

        // 1. Njib l-Project mn l-DB
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project Id:" + id));

        // 2. Njib l-User li feteh halla2 (Lender/Investor)
        User currentUser = userRepository.findByEmail(principal.getName()).get();

        // 3. Nb3at kel l-data lal Agreement Page
        model.addAttribute("project", project);
        model.addAttribute("investor", currentUser);
        model.addAttribute("amount", amount); // L-mablagh li katabo bil-modal
        model.addAttribute("date", java.time.LocalDate.now());
        model.addAttribute("userIp", request.getRemoteAddr());

        return "agreement-page"; // Safhet l-3a2ed li 3melneha
    }
    @GetMapping("/{id}/payment")
    public String showPaymentPage(@PathVariable Long id, @RequestParam Double amount, Model model) {
        Project project = projectRepository.findById(id).get();
        model.addAttribute("project", project);
        model.addAttribute("amount", amount);
        return "payment-page";
    }

    @PostMapping("/{id}/process-payment")
    public String processPayment(@PathVariable Long id, @RequestParam Double amount, Principal principal) {
        Project project = projectRepository.findById(id).get();
        User user = userRepository.findByEmail(principal.getName()).get();

        // 1. Update project funding
        project.setRaisedAmount((long) (project.getRaisedAmount() + amount));
        // 2. التشيك: إذا صار المبلغ المجموع بيساوي أو أكبر من الهدف
        if (project.getRaisedAmount() >= project.getFundingGoal()) {
            project.setStatus(ProjectStatus.COMPLETED); // أو أي اسم status إنت معتمده
        }
        projectRepository.save(project);

        // 2. Create Transaction Record
        Transaction transaction = new Transaction();
        transaction.setSender(user);
        transaction.setProject(project);
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setType(project.getType().toString());
        transactionRepository.save(transaction);

        return "redirect:/projects?success=payment_completed";
    }
    @GetMapping("/{id}")
    public String showProjectDetails(@PathVariable int id, Model model, Principal principal) {
        Project project = projectRepository.findById((long) id).orElseThrow();

        // تأكد إنك حاسب النسبة كرمال الـ Progress Bar يبيّن
        project.calculateFundingPercent();

        // هيدي أهم جملة: لازم الاسم يكون "project" بالظبط
        model.addAttribute("project", project);

        return "project-details";
    }
}
