package com.capstone.OpportuGrow.model;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
public class Availability {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Consultant consultant;

    @Enumerated(EnumType.STRING)
    private DayOfWeek day; // Mon, Tue, etc.

    private LocalTime startTime;
    private LocalTime endTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Consultant getConsultant() { return consultant; }
    public void setConsultant(Consultant consultant) { this.consultant = consultant; }

    public DayOfWeek getDay() { return day; }
    public void setDay(DayOfWeek day) { this.day = day; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
}


