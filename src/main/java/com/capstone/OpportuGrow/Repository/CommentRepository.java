package com.capstone.OpportuGrow.Repository;

import com.capstone.OpportuGrow.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // فيك تزيد هاي إذا بدك تجيب كومنتات مشروع معين مرتبين حسب الوقت
    List<Comment> findByProjectIdOrderByCreatedAtDesc(Integer projectId);
}
