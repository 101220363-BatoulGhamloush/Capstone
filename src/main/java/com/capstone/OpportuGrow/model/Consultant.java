package com.capstone.OpportuGrow.model;

import com.capstone.OpportuGrow.Dto.Slot;
import jakarta.persistence.*;

import java.io.File;
import java.util.List;

@Entity
public class Consultant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    private String name;
    @Column(nullable = false)
    private String password;

    @OneToMany(mappedBy = "consultant", cascade = CascadeType.ALL)
    private List<ConsultantFile> files;

    private String profileImage;

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public List<ConsultantFile> getFiles() {
        return files;
    }

    public void setFiles(List<ConsultantFile> files) {
        this.files = files;
    }

    private String email;
    private String specialty;
    @Column(nullable = false)
    private boolean active = true;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
// --- Files & Appointments relations (إذا موجودة)

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @OneToMany(mappedBy = "consultant")
    private List<Article> articles;

    @OneToMany(mappedBy = "consultant")
    private List<Appointment> appointments;

    // --- Transient property لعرض المواعيد المتاحة في Thymeleaf ---
    @Transient
    private List<Slot> availableSlots ;

    public List<Slot> getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(List<Slot> availableSlots) {
        this.availableSlots = availableSlots;
    }

    // --- Getters & Setters للباقي ---
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getSpecialty() { return specialty; }

    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }
}
