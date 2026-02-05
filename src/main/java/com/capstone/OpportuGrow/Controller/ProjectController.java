package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.model.Project;
import com.capstone.OpportuGrow.model.ProjectStatus;
import com.capstone.OpportuGrow.model.ProjectType;
import com.capstone.OpportuGrow.model.User;
import com.capstone.OpportuGrow.Repository.ProjectRepository;
import com.capstone.OpportuGrow.Repository.UserRepository;
import com.capstone.OpportuGrow.Dto.ProjectRegisterDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Controller
@RequestMapping("/create-project")
public class ProjectController {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public ProjectController(UserRepository userRepository, ProjectRepository projectRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }
    // ================= Step 0: Choose Type =================
    @GetMapping("/choose-type")
    public String showChooseType() {
        return "choose-project-type"; // اسم الصفحة
    }
    @PostMapping("/choose-type")
    public String selectType(@RequestParam("type") ProjectType type, HttpSession session) {
        if (type == null) {
            return "redirect:/create-project/choose-type";
        }
        ProjectRegisterDto projectRegister =
                (ProjectRegisterDto) session.getAttribute("projectRegister");
        if (projectRegister == null) projectRegister = new ProjectRegisterDto();

        projectRegister.setType(type);
        session.setAttribute("projectRegister", projectRegister);

        return "redirect:/create-project/step1";
    }

    // ================= Step 1: Basic Info =================
    @GetMapping("/step1")
    public String showStep1(Model model, HttpSession session) {
        ProjectRegisterDto projectRegister = (ProjectRegisterDto) session.getAttribute("projectRegister");
        if (projectRegister == null) projectRegister = new ProjectRegisterDto();
        model.addAttribute("projectRegister", projectRegister);
        return "create-project-step1";
    }

    @PostMapping("/step1")
    public String saveStep1(@ModelAttribute ProjectRegisterDto projectRegisterForm, HttpSession session) {
        ProjectRegisterDto projectRegister = (ProjectRegisterDto) session.getAttribute("projectRegister");
        if (projectRegister == null) projectRegister = new ProjectRegisterDto();

        projectRegister.setTitle(projectRegisterForm.getTitle());
        projectRegister.setShortDescription(projectRegisterForm.getShortDescription());
        projectRegister.setLongDescription(projectRegisterForm.getLongDescription());
        projectRegister.setCategory(projectRegisterForm.getCategory());


        session.setAttribute("projectRegister", projectRegister);
        return "redirect:/create-project/step2";
    }

    // ================= Step 2: Funding Details =================
    @GetMapping("/step2")
    public String showStep2(Model model, HttpSession session) {
        ProjectRegisterDto projectRegister = (ProjectRegisterDto) session.getAttribute("projectRegister");
        if (projectRegister == null) return "redirect:/create-project/step1";
        model.addAttribute("projectRegister", projectRegister);
        return "create-project-step2";
    }

    @PostMapping("/step2")
    public String saveStep2(@ModelAttribute ProjectRegisterDto projectRegisterForm, HttpSession session) {
        ProjectRegisterDto projectRegister = (ProjectRegisterDto) session.getAttribute("projectRegister");
        if (projectRegister == null) projectRegister = new ProjectRegisterDto();

        projectRegister.setFundingGoal(projectRegisterForm.getFundingGoal());
        projectRegister.setFundingDuration(projectRegisterForm.getFundingDuration());
        BigDecimal goal = BigDecimal.valueOf(projectRegisterForm.getFundingGoal());

        BigDecimal platformFee = goal.multiply(new BigDecimal("0.05"));
        BigDecimal processingFee = goal.multiply(new BigDecimal("0.029"));

        BigDecimal receive =
                goal.subtract(platformFee).subtract(processingFee);

        projectRegister.setPlatformFee(platformFee);
        projectRegister.setProcessingFee(processingFee);
        projectRegister.setAmountYouWillReceive(receive);
        projectRegister.setAddress(projectRegisterForm.getAddress());
        projectRegister.setPhoneNumber(projectRegisterForm.getPhoneNumber());

        session.setAttribute("projectRegister", projectRegister);
        return "redirect:/create-project/step3";
    }

    // ================= Step 3: Media =================
    @GetMapping("/step3")
    public String showStep3(Model model, HttpSession session) {
        ProjectRegisterDto projectRegister = (ProjectRegisterDto) session.getAttribute("projectRegister");
        if (projectRegister == null) return "redirect:/create-project/step1";
        model.addAttribute("projectRegister", projectRegister);
        return "create-project-step3";
    }

    @PostMapping("/step3")
    public String saveStep3(@ModelAttribute ProjectRegisterDto projectRegisterForm,
                            HttpSession session) {

        ProjectRegisterDto projectRegister =
                (ProjectRegisterDto) session.getAttribute("projectRegister");

        if (projectRegister == null) {
            projectRegister = new ProjectRegisterDto();
        }

        if (projectRegisterForm.getImageFile() != null &&
                !projectRegisterForm.getImageFile().isEmpty()) {

            try {
                String uploadDir = "C:/uploads/opportugrow/";
                String fileName = System.currentTimeMillis() + "_" +
                        projectRegisterForm.getImageFile().getOriginalFilename();

                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                File destination = new File(uploadDir + fileName);
                projectRegisterForm.getImageFile().transferTo(destination);

                // نخزّن URL فقط بالـ DTO
                projectRegister.setImageUrl("/uploads/" + fileName);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        session.setAttribute("projectRegister", projectRegister);
        return "redirect:/create-project/step4";
    }





    // ================= Step 4: Review & Submit =================
    @GetMapping("/step4")
    public String showStep4(Model model, HttpSession session) {
        ProjectRegisterDto projectRegister = (ProjectRegisterDto) session.getAttribute("projectRegister");
        if (projectRegister == null) return "redirect:/create-project/step1";
        model.addAttribute("projectRegister", projectRegister);
        return "create-project-step4";
    }

    @PostMapping("/submit")
    public String submitProject(HttpSession session, Principal principal, RedirectAttributes redirectAttributes) {
        ProjectRegisterDto projectRegister = (ProjectRegisterDto) session.getAttribute("projectRegister");
        if (projectRegister == null) {
            redirectAttributes.addFlashAttribute("error", "Project data not found. Please start again.");
            return "redirect:/create-project/step1";
        }

        // تأكد إن المستخدم موجود
        Optional<User> optionalUser = userRepository.findByEmail(principal.getName());
        if (optionalUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/";
        }
        User user = optionalUser.get();

        // تأكد إن النوع موجود
        if (projectRegister.getType() == null) {
            redirectAttributes.addFlashAttribute("error", "Please select a project type first!");
            return "redirect:/create-project/choose-type";
        }

        // إنشاء المشروع
        Project project = new Project();
        project.setTitle(projectRegister.getTitle());
        project.setShortDescription(projectRegister.getShortDescription());
        project.setLongDescription(projectRegister.getLongDescription());
        project.setCategory(projectRegister.getCategory());
        project.setFundingGoal(projectRegister.getFundingGoal());
        project.setFundingDuration(projectRegister.getFundingDuration());
        project.setImageUrl(projectRegister.getImageUrl());
        project.setOwner(user);
        project.setStatus(ProjectStatus.PENDING);
        project.setType(projectRegister.getType());
        project.setPlatformFee(projectRegister.getPlatformFee());
        project.setProcessingFee(projectRegister.getProcessingFee());
        project.setAmountYouWillReceive(projectRegister.getAmountYouWillReceive());
        project.setPhoneNumber(projectRegister.getPhoneNumber());
        project.setAddress(projectRegister.getAddress());
        // تاريخ الإنشاء
        project.setCreatedAt(new Date());
        projectRepository.countByStatus(ProjectStatus.PENDING);

        // حفظ المشروع
        projectRepository.save(project);

        // تنظيف session
        session.removeAttribute("projectRegister");

        redirectAttributes.addFlashAttribute("message", "Your project is under review!");
        return "redirect:/";
    }



    // Shortcut to go directly to step1
    @GetMapping
    public String showCreateProjectForm() {
        return "redirect:/create-project/step1";
    }
}



