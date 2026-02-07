package com.capstone.OpportuGrow.Repository;

import com.capstone.OpportuGrow.model.Appointment;
import com.capstone.OpportuGrow.model.Consultant;
import com.capstone.OpportuGrow.model.Project;
import com.capstone.OpportuGrow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByConsultantId(int consultantId);
    boolean existsByConsultantIdAndDateAndTime(
            Long consultantId,
            LocalDate date,
            LocalTime time
    );
    // الميثود اللي طلبتيها لترتيب المواعيد للمستخدم
    List<Appointment> findByUserOrderByDateAscTimeAsc(User user);

    // ميثود إضافية ممكن تحتاجيها للمستشار (إذا بدك تعرضي مواعيده كمان)
    // List<Appointment> findByConsultantOrderByDateAscTimeAsc(Consultant consultant);



}
