package com.capstone.OpportuGrow.Repository;

import com.capstone.OpportuGrow.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOwner(User user);
     List<Project> findByStatus(ProjectStatus status);
    List<Project> findByConsultant(Consultant consultant);
    List<Project> findByConsultantId(int consultantId);

    List<Project> findByConsultantIsNull();

    List<Project> findByType (ProjectType  type);
    long countByStatus(ProjectStatus status);
    // حسب الـ status و category
    // المشاريع حسب status و type
    List<Project> findByStatusAndType(ProjectStatus status, ProjectType type);

    // بحث بالمشاريع المعتمدة (APPROVED) حسب keyword
    @Query("""
        SELECT p FROM Project p
        WHERE p.status = 'APPROVED'
        AND (
            LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.longDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
    """)
    List<Project> searchApprovedProjects(@Param("keyword") String keyword);

    // بحث بالمشاريع المعتمدة حسب type + keyword
    @Query("""
        SELECT p FROM Project p
        WHERE p.status = 'APPROVED'
        AND p.type = :type
        AND (
            LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.longDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
    """)
    List<Project> searchApprovedByType(
            @Param("keyword") String keyword,
            @Param("type") ProjectType type
    );
}








