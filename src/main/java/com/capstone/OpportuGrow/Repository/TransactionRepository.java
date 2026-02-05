package com.capstone.OpportuGrow.Repository;


import com.capstone.OpportuGrow.model.Transaction;
import com.capstone.OpportuGrow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Find all transactions made by a specific user
    List<Transaction> findBySender(User sender);

    // Find all transactions for a specific project
    List<Transaction> findByProjectId(Long projectId);
}
