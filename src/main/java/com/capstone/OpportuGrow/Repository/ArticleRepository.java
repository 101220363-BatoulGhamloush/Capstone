package com.capstone.OpportuGrow.Repository;

import com.capstone.OpportuGrow.model.Article;
import com.capstone.OpportuGrow.model.ArticleType;
import com.capstone.OpportuGrow.model.Consultant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ArticleRepository extends JpaRepository<Article,Long> {
    List<Article> findByConsultantId(long consultantId);
    List<Article> findByConsultant(Consultant consultant);
    List<Article> findAllByPublishedTrueOrderByUploadedAtDesc();

    List<Article> findByTypeAndPublishedTrueOrderByUploadedAtDesc(ArticleType type);

    List<Article> findByTitleContainingIgnoreCaseAndPublishedTrueOrderByUploadedAtDesc(String keyword);

    List<Article> findByTypeAndTitleContainingIgnoreCaseAndPublishedTrueOrderByUploadedAtDesc(ArticleType type, String keyword);



}
