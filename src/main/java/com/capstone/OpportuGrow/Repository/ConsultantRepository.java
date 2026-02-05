package com.capstone.OpportuGrow.Repository;

import com.capstone.OpportuGrow.model.Consultant;
import com.capstone.OpportuGrow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultantRepository extends JpaRepository<Consultant, Long> {
    // لو بدك تضيف findByEmail أو أي استعلامات خاصة مستقبلاً
     Consultant findByEmail(String email);

    Consultant findByUser(User user);
    List<Consultant> findByUserId(Long userId);

}

