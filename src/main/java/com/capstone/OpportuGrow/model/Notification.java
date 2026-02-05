package com.capstone.OpportuGrow.model;

import jakarta.persistence.*;


import java.time.LocalDateTime;
@Entity
@Table(name = "notifications") // الاسم حسب جدولك بالDB
public class Notification {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        @ManyToOne
        @JoinColumn(name = "user_id")
        private User user;
        private String message;
        private String link; // optional, مثلا رابط للـ project أو للـ chat
        private boolean isRead;
       private LocalDateTime createdAt;

        public Long getId() {
                return id;
        }

        public void setId(Long id) {
                this.id = id;
        }

        public User getUser() {
                return user;
        }

        public void setUser(User user) {
                this.user = user;
        }

        public String getMessage() {
                return message;
        }

        public void setMessage(String message) {
                this.message = message;
        }

        public String getLink() {
                return link;
        }

        public void setLink(String link) {
                this.link = link;
        }

        public boolean isRead() {
                return isRead;
        }

        public void setRead(boolean read) {
                isRead = read;
        }

        public LocalDateTime getCreatedAt() {
                return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
                this.createdAt = createdAt;
        }
}
