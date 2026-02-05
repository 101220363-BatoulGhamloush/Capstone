package com.capstone.OpportuGrow.Controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminLogout {
    @GetMapping("/logout")
    public String logoutPage() {
        // بعد logout رح يرجع للـ login page
        return "redirect:/login";
    }
}
