package com.capstone.OpportuGrow.entity;

import com.capstone.OpportuGrow.enums.TransactionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "amount", columnDefinition = "DECIMAL(15,2)")
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(name = "transaction_fee", columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")
    private Double transactionFee = 0.0;

    @Column(name = "notes", length = 500)
    private String notes;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lender_id", nullable = false)
    private User lender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrower_id", nullable = false)
    private User borrower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // ============ CONSTRUCTORS ============
    public Transaction() {}

    public Transaction(Double amount, User lender, User borrower, Post post) {
        this.amount = amount;
        this.lender = lender;
        this.borrower = borrower;
        this.post = post;
        this.transactionDate = LocalDateTime.now();
    }

    // ============ LIFE CYCLE METHODS ============
    @PrePersist
    protected void onCreate() {
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
    }

    // ============ BUSINESS METHODS ============
    public void markAsCompleted() {
        this.status = TransactionStatus.COMPLETED;
        this.completedDate = LocalDateTime.now();
    }

    // ============ GETTERS AND SETTERS ============
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    public LocalDateTime getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDateTime completedDate) { this.completedDate = completedDate; }

    public Double getTransactionFee() { return transactionFee; }
    public void setTransactionFee(Double transactionFee) { this.transactionFee = transactionFee; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public User getLender() { return lender; }
    public void setLender(User lender) { this.lender = lender; }

    public User getBorrower() { return borrower; }
    public void setBorrower(User borrower) { this.borrower = borrower; }

    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }
}