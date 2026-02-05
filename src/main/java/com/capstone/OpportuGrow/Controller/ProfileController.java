package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Dto.UpdateUserRequest;
import com.capstone.OpportuGrow.Repository.TransactionRepository;
import com.capstone.OpportuGrow.Repository.UserRepository;
import com.capstone.OpportuGrow.model.Transaction;
import com.capstone.OpportuGrow.model.User;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
public class ProfileController {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public ProfileController(UserRepository userRepository, TransactionRepository transactionRepository,PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder=passwordEncoder;
    }

    @GetMapping("/profile")
    public String viewProfile(Model model, Principal principal) {
        // 1. Get current logged-in user
        User user = userRepository.findByEmail(principal.getName()).get();

        // 2. Get their transactions
        List<Transaction> transactions = transactionRepository.findBySender(user);

        // 3. Calculate stats
        double totalInvested = transactions.stream().mapToDouble(Transaction::getAmount).sum();

        model.addAttribute("user", user);
        model.addAttribute("transactions", transactions);
        model.addAttribute("totalInvested", totalInvested);

        return "profile"; // This will look for profile.html
    }
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam("name") String name,
                                @RequestParam(value = "phone", required = false) String phone,
                                @RequestParam(value = "address", required = false) String address,
                                @RequestParam(value = "newPassword", required = false) String newPassword,
                                @RequestParam(value = "profileImageFile", required = false) MultipartFile file,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        // 1. جلب المستخدم الحالي
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. تحديث البيانات الأساسية
        user.setName(name);
        user.setPhone(phone);
        user.setAddress(address);

        // 3. تحديث كلمة المرور (فقط إذا قام المستخدم بكتابة شيء جديد)
        if (newPassword != null && !newPassword.isEmpty()) {
            // ملاحظة: تأكدي من عمل Autowired للـ PasswordEncoder في الكلاس
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        // 4. معالجة رفع الصورة (نفس المنطق الناجح للمستشار)
        if (file != null && !file.isEmpty()) {
            String uploadDir = "C:/og-uploads/";
            String fileName = System.currentTimeMillis() + "_" + StringUtils.cleanPath(file.getOriginalFilename());

            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            Path path = Paths.get(uploadDir + fileName);
            try {
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // حفظ اسم الصورة في موديل المستخدم
            user.setProfileImage(fileName);
        }

        // 5. حفظ التعديلات
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/profile"; // تأكدي من أن هذا المسار هو نفسه صفحة التعديل

    }
}