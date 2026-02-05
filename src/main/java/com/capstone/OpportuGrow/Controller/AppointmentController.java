package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Dto.Slot;
import com.capstone.OpportuGrow.Repository.AvailabilityRepository;
import com.capstone.OpportuGrow.model.*;
import com.capstone.OpportuGrow.Repository.AppointmentRepository;
import com.capstone.OpportuGrow.Repository.ConsultantRepository;
import com.capstone.OpportuGrow.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/appointments")

public class AppointmentController {

    private final ConsultantRepository consultantRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final AvailabilityRepository availabilityRepository;

    public AppointmentController(AvailabilityRepository availabilityRepository,ConsultantRepository consultantRepository, AppointmentRepository appointmentRepository, UserRepository userRepository) {
        this.consultantRepository = consultantRepository;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.availabilityRepository=availabilityRepository;
    }

    /* =========================
           GET – صفحة حجز المواعيد
           ========================= */
    @GetMapping
    public String showAppointmentsPage(Model model) {

        List<Consultant> consultants = consultantRepository.findAll();

        for (Consultant consultant : consultants) {
            consultant.setAvailableSlots(
                    generateAvailableSlots(consultant)
            );
        }

        model.addAttribute("consultants", consultants);
        return "appointments"; // اسم ملف HTML
    }

    /* =========================
       POST – حجز موعد
       ========================= */
    private List<Slot> generateAvailableSlots(Consultant consultant) {

        List<Slot> slots = new ArrayList<>();

        // نولد المواعيد لمدة 5 أيام القادمة
        for (int day = 0; day < 5; day++) {

            LocalDate date = LocalDate.now().plusDays(day);

            // نجيب الـ Availability حسب اليوم
            List<Availability> availabilities = availabilityRepository.findByConsultantAndDay(
                    consultant, date.getDayOfWeek()
            );

            for (Availability availability : availabilities) {
                LocalTime time = availability.getStartTime();

                while (time.isBefore(availability.getEndTime())) {

                    LocalTime tempTime = time;

                    // نتأكد أن الموعد مش محجوز
                    boolean isTaken = appointmentRepository.findByConsultantId(consultant.getId())
                            .stream()
                            .anyMatch(a ->
                                    a.getDate().equals(date) &&
                                            a.getTime().equals(tempTime)
                            );

                    if (!isTaken) {
                        Slot newSlot = new Slot();
                        newSlot.setTime(tempTime); // تعيين الوقت
                        newSlot.setDate(date);     // تعيين التاريخ
                        newSlot.setBooked(false);  // تعيين حالة الحجز
                        slots.add(newSlot);
                    }

                    time = time.plusHours(1); // زيادة ساعة لكل slot
                }
            }
        }
        return slots;
    }


    @PostMapping("/book")
    public String bookAppointment(@RequestParam Long consultantId,
                                  @RequestParam String date,
                                  @RequestParam String time,
                                  Principal principal, // نمرر الـ Principal هون
                                  Model model) {

        // 1. نجلب المستشار
        Consultant consultant = consultantRepository.findById(consultantId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid consultant Id"));

        // 2. نجلب المستخدم المسجل حالياً (عن طريق الإيميل أو الـ username)
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found")); // تأكدي إن الميثود موجودة بالـ Repository

        // 3. تحويل الداتا
        LocalDate localDate = LocalDate.parse(date);
        LocalTime localTime = LocalTime.parse(time);

        // 4. إنشاء الموعد وربط الكل ببعض
        Appointment appointment = new Appointment();
        appointment.setConsultant(consultant);
        appointment.setUser(user); // <--- هون الربط اللي كان ناقص!
        appointment.setDate(localDate);
        appointment.setTime(localTime);

        // 5. حفظ
        appointmentRepository.save(appointment);

        return "redirect:/appointments?success=true";
    }

}
