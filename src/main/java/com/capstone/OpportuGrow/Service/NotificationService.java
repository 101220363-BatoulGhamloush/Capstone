package com.capstone.OpportuGrow.Service;

import com.capstone.OpportuGrow.Repository.NotificationRepository;
import com.capstone.OpportuGrow.model.Notification;
import com.capstone.OpportuGrow.model.User;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // الميثود الأساسية
    public void sendNotification(User user, String message, String link) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setLink(link);
        notification.setRead(false); // تأكد إنها دايماً بتبلش "غير مقروءة"
        notification.setCreatedAt(java.time.LocalDateTime.now()); // مهم جداً للترتيب الزمني

        notificationRepository.save(notification);
    }

    // ميثود خاصة للرفض (كرمال تسهل على حالك)
    public void sendRejectionNotification(User user, String projectName) {
        String message = "Your project '" + projectName + "' was unfortunately rejected. Click here to chat with our consultant for feedback.";
        String chatLink = "/chat/consultation"; // الرابط اللي اتفقنا عليه
        sendNotification(user, message, chatLink);
    }
}