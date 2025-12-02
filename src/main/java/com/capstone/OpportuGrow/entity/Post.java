package com.capstone.OpportuGrow.entity;

import com.capstone.OpportuGrow.enums.PostType;
import com.capstone.OpportuGrow.enums.PostStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostType postType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status = PostStatus.PENDING;

    // Financial details
    @NotNull
    @Column(name = "amount_requested", columnDefinition = "DECIMAL(15,2)")
    private Double amountRequested;

    @Column(name = "amount_fulfilled", columnDefinition = "DECIMAL(15,2) DEFAULT 0.00")
    private Double amountFulfilled = 0.0;

    @Column(name = "interest_rate", columnDefinition = "DECIMAL(5,2)")
    private Double interestRate; // For loans

    @Column(name = "repayment_term") // In months
    private Integer repaymentTerm;

    @Column(name = "expected_roi", columnDefinition = "DECIMAL(5,2)")
    private Double expectedROI; // For investments

    // Timelines
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();

    // ============ CONSTRUCTORS ============
    public Post() {}

    public Post(String title, String description, PostType postType,
                Double amountRequested, User user) {
        this.title = title;
        this.description = description;
        this.postType = postType;
        this.amountRequested = amountRequested;
        this.user = user;
    }

    // ============ LIFE CYCLE METHODS ============
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // Set expiry date to 30 days from creation
        if (expiryDate == null) {
            expiryDate = createdAt.plusDays(30);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ============ BUSINESS METHODS ============
    public Double getRemainingAmount() {
        return amountRequested - amountFulfilled;
    }

    public boolean isFullyFunded() {
        return amountFulfilled >= amountRequested;
    }

    public Double getFundingPercentage() {
        return (amountFulfilled / amountRequested) * 100;
    }

    // ============ GETTERS AND SETTERS ============
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public PostType getPostType() { return postType; }
    public void setPostType(PostType postType) { this.postType = postType; }

    public PostStatus getStatus() { return status; }
    public void setStatus(PostStatus status) { this.status = status; }

    public Double getAmountRequested() { return amountRequested; }
    public void setAmountRequested(Double amountRequested) { this.amountRequested = amountRequested; }

    public Double getAmountFulfilled() { return amountFulfilled; }
    public void setAmountFulfilled(Double amountFulfilled) { this.amountFulfilled = amountFulfilled; }

    public Double getInterestRate() { return interestRate; }
    public void setInterestRate(Double interestRate) { this.interestRate = interestRate; }

    public Integer getRepaymentTerm() { return repaymentTerm; }
    public void setRepaymentTerm(Integer repaymentTerm) { this.repaymentTerm = repaymentTerm; }

    public Double getExpectedROI() { return expectedROI; }
    public void setExpectedROI(Double expectedROI) { this.expectedROI = expectedROI; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }
}