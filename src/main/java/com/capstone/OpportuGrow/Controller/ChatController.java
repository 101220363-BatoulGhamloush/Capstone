package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Repository.*;
import com.capstone.OpportuGrow.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/chat")
public class ChatController {
    private final ChatMessageRepository chatRepository;
    private final UserRepository userRepository;


    public ChatController(ChatMessageRepository chatRepository, UserRepository userRepository) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/consultation")
    public String openChat(Model model, Principal principal) {
        // 1. جلب المستخدم الحالي
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. جلب قائمة الأدمنز (لتجنب الـ ClassCastException)
        List<User> admins = userRepository.findByRole(Role.ADMIN);

        if (admins.isEmpty()) {
            // إذا ما في أدمن، ممكن نبعت لـ ID 1 كـ fallback أو نطلع error واضح
            model.addAttribute("error", "No administrators available at the moment.");
            return "error";
        }

        // 3. نختار أول أدمن من القائمة (أو الأدمن رقم 10 إذا بدك تثبته مؤقتاً)
        User targetAdmin = admins.get(0);
        Long adminId = (long) targetAdmin.getId();

        // 4. جلب تاريخ المحادثة
        List<ChatMessage> messages = chatRepository.findChatHistory((long) currentUser.getId(), adminId);

        model.addAttribute("messages", messages);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("adminId", adminId);

        return "chat";
    }

    @PostMapping("/send")
    @ResponseBody
    public ResponseEntity<Void> sendMessage(@RequestParam String content, @RequestParam Long receiverId, Principal principal) {
        User sender = userRepository.findByEmail(principal.getName()).get();
        User receiver = userRepository.findById(Math.toIntExact(receiverId)).orElseThrow();

        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());

        chatRepository.save(message);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/admin/inbox")
    public String adminInbox(Model model, Principal principal) {
        User admin = userRepository.findByEmail(principal.getName()).get();

        // 1. جلب كل الرسائل الواردة
        List<ChatMessage> incomingMessages = chatRepository.findByReceiverIdOrderByTimestampDesc(Long.valueOf(admin.getId()));

        // 2. استخدام Stream لتصفية الرسائل (عرض آخر رسالة فقط لكل Sender)
        List<ChatMessage> filteredMessages = incomingMessages.stream()
                .collect(Collectors.toMap(
                        msg -> msg.getSender().getId(), // المفتاح هو ID المرسل
                        msg -> msg,                     // القيمة هي الرسالة نفسها
                        (existing, replacement) -> existing // إذا وجد تكرار، احتفظ بالأولى (لأنها الأحدث بسبب الـ OrderByDesc)
                ))
                .values()
                .stream()
                .sorted(Comparator.comparing(ChatMessage::getTimestamp).reversed()) // إعادة الترتيب حسب الوقت
                .toList();

        model.addAttribute("messages", filteredMessages);
        model.addAttribute("pageTitle", "Messages Inbox");
        model.addAttribute("contentTemplate", "admin-inbox");
        return "admin-layout";
    }
    @GetMapping("/admin/reply/{userId}")
    public String adminReply(@PathVariable Long userId, Model model, Principal principal) {
        User admin = userRepository.findByEmail(principal.getName()).get();
        User user = userRepository.findById(Math.toIntExact(userId)).orElseThrow();

        // منجيب التاريخ بين الأدمن وهيدا المستخدم
        List<ChatMessage> messages = chatRepository.findChatHistory((long) admin.getId(), (long) user.getId());

        model.addAttribute("messages", messages);
        model.addAttribute("currentUser", admin);
        model.addAttribute("adminId", user.getId());// هون الـ "adminId" بالـ JS صار هو الـ User اللي عم نرد عليه
        model.addAttribute("targetUserName", user.getName()); // نمرر الاسم للـ Header
        // منحدد المسار الجديد للملف
        model.addAttribute("contentTemplate", "admin-chat");
        return "admin-layout"; // منستخدم نفس صفحة الشات اللي صممناها قبل شوي
    }
}
