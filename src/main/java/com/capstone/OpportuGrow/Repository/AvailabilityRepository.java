package com.capstone.OpportuGrow.Repository;

import com.capstone.OpportuGrow.model.Availability;
import com.capstone.OpportuGrow.model.Consultant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    List<Availability> findByConsultant(Consultant consultant);
    List<Availability> findByConsultantAndDay(Consultant consultant, DayOfWeek day);
}

