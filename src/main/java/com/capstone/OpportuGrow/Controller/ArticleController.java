package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Repository.ArticleRepository;
import com.capstone.OpportuGrow.model.Article;
import com.capstone.OpportuGrow.model.ArticleType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ArticleController {
    private ArticleRepository articleRepository;


    public ArticleController(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @GetMapping("/articles")
    public String viewArticles(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            Model model) {

        List<Article> articles;

        if ((type != null && !type.isEmpty()) && (keyword != null && !keyword.isEmpty())) {
            articles = articleRepository.findByTypeAndTitleContainingIgnoreCaseAndPublishedTrueOrderByUploadedAtDesc(ArticleType.valueOf(type), keyword);
        } else if (type != null && !type.isEmpty()) {
            articles = articleRepository.findByTypeAndPublishedTrueOrderByUploadedAtDesc(ArticleType.valueOf(type));
        } else if (keyword != null && !keyword.isEmpty()) {
            articles = articleRepository.findByTitleContainingIgnoreCaseAndPublishedTrueOrderByUploadedAtDesc(keyword);
        } else {
            articles = articleRepository.findAllByPublishedTrueOrderByUploadedAtDesc();
        }

        model.addAttribute("articles", articles);
        model.addAttribute("selectedType", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("types", ArticleType.values());
        return "user-articles";
    }


}
