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
import java.util.ArrayList;
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
        List<Project> projects;

        // منحدد الحالات اللي بدنا نعرضها (بدنا المعروض والمكتمل)
        List<ProjectStatus> statuses = List.of(ProjectStatus.APPROVED, ProjectStatus.COMPLETED);


        // 1. جلب البيانات (الـ Logic تبعك صح بس زدت عليه الترتيب)
        if ((keyword == null || keyword.isEmpty()) && type == null) {
            // بدلاً من findByStatus، نستخدم findByStatusIn
            projects = projectRepository.findByStatusIn(statuses);
        } else if (keyword != null && !keyword.isEmpty() && type != null) {
            // يجب أن تدعم هذه الميثود البحث في الحالات المحددة أيضاً
            projects = projectRepository.findByStatusInAndTypeAndTitleContainingIgnoreCase(statuses, type, keyword);
        } else if (keyword != null && !keyword.isEmpty()) {
            projects = projectRepository.findByStatusInAndTitleContainingIgnoreCase(statuses, keyword);
        } else {
            projects = projectRepository.findByStatusInAndType(statuses, type);
        }

        // 2. معالجة البيانات (حساب النسبة والـ Like والحالة)
        User currentUser = (principal != null) ? userRepository.findByEmail(principal.getName()).orElse(null) : null;

        for (Project project : projects) {
            project.calculateFundingPercent();

            // فحص الـ Like بطريقة آمنة
            project.setLikedByCurrentUser(currentUser != null && project.getLikedUsers().contains(currentUser));

            // ميزة إضافية: فيكي هون تعملي Logic إذا المشروع صار Funded 100%
            // مثلاً: project.setIsCompleted(project.getRaisedAmount() >= project.getFundingGoal());
        }

        // 3. الترتيب: خلي المشاريع اللي بعدها بحاجة لتمويل تطلع بالأول
        projects.sort((p1, p2) -> {
            boolean p1Funded = p1.getRaisedAmount() >= p1.getFundingGoal();
            boolean p2Funded = p2.getRaisedAmount() >= p2.getFundingGoal();
            return Boolean.compare(p1Funded, p2Funded);
        });

        // 4. الـ Pagination (كودك ممتاز، تركته متل ما هو مع حماية من الـ Empty List)
        int pageSize = 6;
        int totalProjects = projects.size();
        int totalPages = (totalProjects == 0) ? 1 : (int) Math.ceil((double) totalProjects / pageSize);
        page = Math.max(1, Math.min(page, totalPages));

        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalProjects);

        List<Project> pageProjects = (totalProjects > 0) ? projects.subList(fromIndex, toIndex) : new ArrayList<>();

        // 5. إرسال البيانات
        model.addAttribute("projects", pageProjects);
        model.addAttribute("keyword", keyword);
        model.addAttribute("type", type);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("types", ProjectType.values()); // كرمال الـ Filter Dropdown بالـ HTML

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
