package com.capstone.OpportuGrow.Repository;

import com.capstone.OpportuGrow.model.Role;
import com.capstone.OpportuGrow.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    // البحث حسب الاسم أو الإيميل أو الدور
    @Query("""
        SELECT u FROM User u
        WHERE (:search IS NULL OR
              LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
              LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR
              LOWER(u.role) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:status IS NULL OR u.active = :status)
    """)
    Page<User> searchUsers(
            @Param("search") String search,
            @Param("status") Boolean status,
            Pageable pageable
    );
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);


    int countByRoleAndActive(Role role, boolean active);


    long countByActiveTrue();      // عدد المستخدمين النشطين
    long countByActiveFalse();     // عدد المستخدمين غير النشطين
    long countByRole(Role role);

}

