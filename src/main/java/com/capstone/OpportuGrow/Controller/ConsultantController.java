package com.capstone.OpportuGrow.Controller;


import com.capstone.OpportuGrow.Repository.*;
import com.capstone.OpportuGrow.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Controller
public class ConsultantController {

    private final ConsultantRepository consultantRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final AvailabilityRepository availabilityRepository;
    private final ArticleRepository articleRepository;

    @Autowired
    public ConsultantController(ConsultantRepository consultantRepository,
                                         AppointmentRepository appointmentRepository,
                                         UserRepository userRepository,
                                AvailabilityRepository availabilityRepository, ArticleRepository articleRepository) {
        this.consultantRepository = consultantRepository;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.availabilityRepository=availabilityRepository;
        this.articleRepository=articleRepository;
    }

    // Dashboard page
    @GetMapping("/consultants/dashboard")
    public String dashboard(Model model, Principal principal) {
        // Get consultant by logged-in email
        String email = principal.getName();
        Consultant consultant = consultantRepository.findByEmail(email);

        // Load appointments
        List<Appointment> appointments = appointmentRepository.findByConsultantId(consultant.getId());
        consultant.setAppointments(appointments);

        List<Consultant> consultants = consultantRepository.findAll();

        // إذا بدك تضيف ملفات لكل consultant:
        for (Consultant c : consultants) {
            c.setFiles(c.getFiles()); // أو logic لجلب الملفات من DB
        }

        model.addAttribute("consultant", consultant);

        return "consultants-dashboard"; // Thymeleaf HTML file name
    }

    // Optional: view single appointment detail
    @GetMapping("/consultant/appointment/{id}")
    public String viewAppointment(@PathVariable Long id, Model model) {
        Appointment appointment = appointmentRepository.findById(id).orElse(null);
        model.addAttribute("appointment", appointment);
        return "appointment-detail"; // can create separate page
    }
    @GetMapping("/consultant/profile/edit")
    public String editProfile(Model model, Principal principal) {
        Consultant consultant =
                consultantRepository.findByEmail(principal.getName());


        model.addAttribute("consultant", consultant);
        return "consultant-edit-profile";
    }

    @PostMapping("/consultant/profile/update")
    public String updateProfile(@ModelAttribute Consultant consultant,
                                @RequestParam(value = "profileImageFile", required = false) MultipartFile profileImageFile,
                                RedirectAttributes redirectAttributes) {

        // 1️⃣ جلب الـ consultant الحالي من الداتابيز
        Consultant existingConsultant = consultantRepository.findById((long) consultant.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid consultant ID"));

        // 2️⃣ تحديث حقول الـ Specialty
        existingConsultant.setSpecialty(consultant.getSpecialty());

        // 3️⃣ معالجة رفع الصورة إلى المجلد الخارجي
        if (profileImageFile != null && !profileImageFile.isEmpty()) {
            try {
                // المسار الخارجي الذي اتفقنا عليه
                String uploadDir = "C:/og-uploads/";

                // تنظيف اسم الملف ومنع التكرار باستخدام UUID (اختياري ولكن أفضل)
                String fileName = System.currentTimeMillis() + "_" + StringUtils.cleanPath(profileImageFile.getOriginalFilename());

                // التأكد من وجود المجلد
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                // حفظ الملف في المسار الخارجي
                Path filePath = Paths.get(uploadDir + fileName);
                Files.copy(profileImageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // تخزين اسم الملف الجديد في الداتابيز
                existingConsultant.setProfileImage(fileName);

            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("error", "Failed to upload image: " + e.getMessage());
                return "redirect:/consultant/profile/edit";
            }
        }

        // 4️⃣ تحديث بيانات المستخدم (User Table)
        User user = existingConsultant.getUser();
        if (user != null) {
            user.setName(consultant.getName());
            user.setEmail(consultant.getEmail());
            userRepository.save(user);
        }

        // 5️⃣ حفظ التعديلات النهائية للـ consultant
        consultantRepository.save(existingConsultant);

        redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/consultants/dashboard";
    }
    // ConsultantController
    @GetMapping("/consultant/schedule")
    public String scheduleForm(Model model, Principal principal) {
        Consultant consultant = consultantRepository.findByEmail(principal.getName());
        List<Availability> availabilities = availabilityRepository.findByConsultant(consultant);

        model.addAttribute("consultant", consultant);
        model.addAttribute("availabilities", availabilities);
        return "consultant-schedule";
    }

    @PostMapping("/consultant/schedule")
    public String saveAvailability(@RequestParam DayOfWeek day,
                                   @RequestParam String startTime,
                                   @RequestParam String endTime,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {

        Consultant consultant = consultantRepository.findByEmail(principal.getName());

        Availability availability = new Availability();
        availability.setConsultant(consultant);
        availability.setDay(day);
        availability.setStartTime(LocalTime.parse(startTime));
        availability.setEndTime(LocalTime.parse(endTime));

        availabilityRepository.save(availability);
        redirectAttributes.addFlashAttribute("success", "Availability saved!");
        return "redirect:/consultant/schedule";
    }
    @PostMapping("/consultant/schedule/delete/{id}")
    public String deleteAvailability(@PathVariable Long id) {
        availabilityRepository.deleteById(id);
        return "redirect:/consultant/schedule";
    }

        @GetMapping("/consultant/logout")
        public String logoutPage() {
            // بعد logout رح يرجع للـ login page
            return "redirect:/login";

    }
    // 📄 Form page
    @GetMapping("/consultant/create")
    public String showCreateArticle(Model model) {
        model.addAttribute("article", new Article());
        model.addAttribute("types", ArticleType.values());
        return "consultant-article-create";
    }

    // 📤 Submit
    @PostMapping("/consultant/create")
    public String createArticle(@ModelAttribute Article article,
                                @RequestParam(required = false) MultipartFile file,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {

        Consultant consultant = consultantRepository
                .findByEmail(principal.getName());

        // 📂 Upload file (if PDF)
        if (file != null && !file.isEmpty()) {
            try {
                // غيري هيدا السطر بالـ Controller
                String uploadDir = "C:/og-uploads/";
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                file.transferTo(new File(uploadDir + fileName));

                article.setFileUrl("/uploads/articles/" + fileName);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "File upload failed");
                return "redirect:/consultant/articles/create";
            }
        }

        article.setConsultant(consultant);
        articleRepository.save(article);

        redirectAttributes.addFlashAttribute("success", "Article uploaded successfully!");
        return "redirect:/consultants/dashboard";
    }
}












