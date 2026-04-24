package com.example.petcare.controller.user;

import com.example.petcare.dto.AppointmentRequest;
import com.example.petcare.dto.AppointmentResponse;
import com.example.petcare.model.MedicalService;
import com.example.petcare.model.User;
import com.example.petcare.repository.user.UserRepository;
import com.example.petcare.service.user.booking.AppointmentService;
import com.example.petcare.service.user.booking.ServiceManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final ServiceManagementService medicalServiceService;
    private final UserRepository userRepository;

    // 1. Lấy danh sách dịch vụ
    @GetMapping("/services")
    public ResponseEntity<?> getServices() {
        List<MedicalService> services = medicalServiceService.getAllActiveServices();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("services", services);
        return ResponseEntity.ok(response);
    }

    // 2. Lấy danh sách bác sĩ
    @GetMapping("/doctors")
    public ResponseEntity<?> getDoctors() {
        List<User> doctors = userRepository.findByRoleAndStatus(User.Role.doctor, User.Status.active);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("doctors", doctors);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available-slots")
    public ResponseEntity<?> getAvailableSlots(
            @RequestParam Long doctorId,
            @RequestParam String date) {

        LocalDate appointmentDate = LocalDate.parse(date);
        List<LocalTime> availableSlots = appointmentService.getAvailableSlots(doctorId, appointmentDate);

        // Tạo danh sách tất cả slot
        List<LocalTime> allSlots = List.of(
                LocalTime.of(8,0), LocalTime.of(8,30), LocalTime.of(9,0), LocalTime.of(9,30),
                LocalTime.of(10,0), LocalTime.of(10,30), LocalTime.of(11,0), LocalTime.of(11,30),
                LocalTime.of(13,0), LocalTime.of(13,30), LocalTime.of(14,0), LocalTime.of(14,30),
                LocalTime.of(15,0), LocalTime.of(15,30), LocalTime.of(16,0), LocalTime.of(16,30),
                LocalTime.of(17,0)
        );

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("slots", availableSlots);      // Slot còn trống
        response.put("allSlots", allSlots);         // Tất cả slot
        return ResponseEntity.ok(response);
    }

    // 4. Đặt lịch mới
    @PostMapping("/book")
    public ResponseEntity<?> bookAppointment(
            Authentication authentication,
            @RequestBody AppointmentRequest request) {

        try {
            User user = (User) authentication.getPrincipal();
            log.info("📅 User {} đang đặt lịch", user.getEmail());

            AppointmentResponse appointment = appointmentService.createAppointment(user.getId(), request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đặt lịch thành công!");
            response.put("appointment", appointment);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Đặt lịch thất bại: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 5. Lấy danh sách lịch hẹn của tôi
    @GetMapping("/my-appointments")
    public ResponseEntity<?> getMyAppointments(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<AppointmentResponse> appointments = appointmentService.getUserAppointments(user.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("appointments", appointments);
        response.put("total", appointments.size());
        return ResponseEntity.ok(response);
    }

    // 6. Lấy lịch hẹn sắp tới
    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingAppointments(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<AppointmentResponse> appointments = appointmentService.getUpcomingAppointments(user.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("appointments", appointments);
        response.put("total", appointments.size());
        return ResponseEntity.ok(response);
    }

    // 7. Lấy chi tiết lịch hẹn
    @GetMapping("/{appointmentId}")
    public ResponseEntity<?> getAppointmentDetail(
            Authentication authentication,
            @PathVariable Long appointmentId) {

        try {
            User user = (User) authentication.getPrincipal();
            AppointmentResponse appointment = appointmentService.getAppointmentById(appointmentId, user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("appointment", appointment);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // 8. Hủy lịch hẹn
    @PutMapping("/cancel/{appointmentId}")
    public ResponseEntity<?> cancelAppointment(
            Authentication authentication,
            @PathVariable Long appointmentId,
            @RequestParam(required = false) String reason) {

        try {
            User user = (User) authentication.getPrincipal();
            log.info("❌ User {} đang hủy lịch {}", user.getEmail(), appointmentId);

            AppointmentResponse appointment = appointmentService.cancelAppointment(appointmentId, user.getId(), reason);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Hủy lịch thành công!");
            response.put("appointment", appointment);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Hủy lịch thất bại: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}