package com.capstone.OpportuGrow.Service;

import com.capstone.OpportuGrow.Repository.NotificationRepository;
import com.capstone.OpportuGrow.Repository.ProjectRepository;
import com.capstone.OpportuGrow.model.Notification;
import com.capstone.OpportuGrow.model.Project;
import com.capstone.OpportuGrow.model.ProjectStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;


    public ProjectService(ProjectRepository projectRepository, NotificationRepository notificationRepository,NotificationService notificationService) {
        this.projectRepository = projectRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService=notificationService;
    }

    public void approveProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        project.setStatus(ProjectStatus.APPROVED);
        projectRepository.save(project);

        // Send notification to user
        Notification notification = new Notification();
        notification.setUser(project.getOwner());
        notification.setMessage("Your project '" + project.getTitle() + "' has been approved!");
        notification.setLink("/projects/" + project.getId()); // رابط للصفحة المنشورة
        notificationRepository.save(notification);
    }

    public void rejectProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        project.setStatus(ProjectStatus.REJECTED);
        projectRepository.save(project);

        // Send notification to user
        Notification notification = new Notification();
        notification.setUser(project.getOwner());
        notification.setMessage("Your project '" + project.getTitle() + "' was rejected. Contact admin for details.");
        notification.setLink("/chat?userId=" + project.getOwner().getId()); // رابط للدردشة
        notificationRepository.save(notification);
    }
    public List<Project> getPendingProjects() {
        return projectRepository.findByStatus(ProjectStatus.PENDING);
    }

}

