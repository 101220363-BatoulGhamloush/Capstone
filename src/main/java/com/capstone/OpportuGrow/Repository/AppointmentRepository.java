package com.capstone.OpportuGrow.Repository;

import com.capstone.OpportuGrow.model.Appointment;
import com.capstone.OpportuGrow.model.Consultant;
import com.capstone.OpportuGrow.model.Project;
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



}
