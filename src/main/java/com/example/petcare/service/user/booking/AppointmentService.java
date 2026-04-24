package com.example.petcare.service.user.booking;

import com.example.petcare.dto.AppointmentRequest;
import com.example.petcare.dto.AppointmentResponse;
import com.example.petcare.model.Appointment;
import com.example.petcare.model.AppointmentStatus;
import com.example.petcare.model.MedicalService;
import com.example.petcare.model.User;
import com.example.petcare.repository.user.AppointmentRepository;
import com.example.petcare.repository.user.MedicalServiceRepository;
import com.example.petcare.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final MedicalServiceRepository medicalServiceRepository;
    private final UserRepository userRepository;

    private static final List<LocalTime> DEFAULT_TIME_SLOTS = List.of(
            LocalTime.of(8, 0), LocalTime.of(8, 30),
            LocalTime.of(9, 0), LocalTime.of(9, 30),
            LocalTime.of(10, 0), LocalTime.of(10, 30),
            LocalTime.of(11, 0), LocalTime.of(11, 30),
            LocalTime.of(13, 0), LocalTime.of(13, 30),
            LocalTime.of(14, 0), LocalTime.of(14, 30),
            LocalTime.of(15, 0), LocalTime.of(15, 30),
            LocalTime.of(16, 0), LocalTime.of(16, 30),
            LocalTime.of(17, 0)
    );

    // Lấy danh sách khung giờ trống
    public List<LocalTime> getAvailableSlots(Long doctorId, LocalDate date) {
        // Lấy các khung giờ đã được đặt (không bao gồm cancelled)
        List<LocalTime> bookedTimes = appointmentRepository.findBookedTimesByDoctorAndDate(doctorId, date);
        if (bookedTimes == null) bookedTimes = List.of();

        System.out.println("📅 Doctor: " + doctorId + ", Date: " + date);
        System.out.println("📌 Booked times: " + bookedTimes);

        // Tất cả khung giờ
        List<LocalTime> allSlots = List.of(
                LocalTime.of(8, 0), LocalTime.of(8, 30),
                LocalTime.of(9, 0), LocalTime.of(9, 30),
                LocalTime.of(10, 0), LocalTime.of(10, 30),
                LocalTime.of(11, 0), LocalTime.of(11, 30),
                LocalTime.of(13, 0), LocalTime.of(13, 30),
                LocalTime.of(14, 0), LocalTime.of(14, 30),
                LocalTime.of(15, 0), LocalTime.of(15, 30),
                LocalTime.of(16, 0), LocalTime.of(16, 30),
                LocalTime.of(17, 0)
        );

        LocalTime now = LocalTime.now();
        boolean isToday = date.equals(LocalDate.now());

        List<LocalTime> availableSlots = new ArrayList<>();

        for (LocalTime slot : allSlots) {
            // Nếu slot đã được đặt (bởi lịch chưa hủy) -> bỏ qua
            if (bookedTimes.contains(slot)) {
                System.out.println("❌ Slot " + slot + " đã được đặt");
                continue;
            }
            // Nếu là ngày hôm nay và slot đã qua giờ -> bỏ qua
            if (isToday && slot.isBefore(now)) {
                System.out.println("⏰ Slot " + slot + " đã qua giờ");
                continue;
            }
            availableSlots.add(slot);
        }

        System.out.println("✅ Available slots: " + availableSlots);
        return availableSlots;
    }

    // Đặt lịch mới
    @Transactional
    public AppointmentResponse createAppointment(Long userId, AppointmentRequest request) {
        // Kiểm tra bác sĩ
        User doctor = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ"));

        if (doctor.getRole() != User.Role.doctor) {
            throw new RuntimeException("Người dùng không phải là bác sĩ");
        }

        // Kiểm tra dịch vụ
        MedicalService service = medicalServiceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dịch vụ"));

        // Kiểm tra ngày
        if (request.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Không thể đặt lịch trong quá khứ");
        }

        // Kiểm tra khung giờ trống
        boolean isBooked = appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                request.getDoctorId(),
                request.getAppointmentDate(),
                request.getAppointmentTime(),
                AppointmentStatus.cancelled
        );

        if (isBooked) {
            throw new RuntimeException("Khung giờ này đã có người đặt");
        }

        // Tạo appointment
        Appointment appointment = new Appointment();
        appointment.setUserId(userId);
        appointment.setPetName(request.getPetName());
        appointment.setPetType(request.getPetType());
        appointment.setPetBreed(request.getPetBreed());
        appointment.setPetAge(request.getPetAge());
        appointment.setPetWeight(request.getPetWeight());
        appointment.setPetGender(request.getPetGender());
        appointment.setPetColor(request.getPetColor());
        appointment.setSymptoms(request.getSymptoms());
        appointment.setMedicalHistory(request.getMedicalHistory());
        appointment.setDoctorId(request.getDoctorId());
        appointment.setServiceId(request.getServiceId());
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setNotes(request.getNotes());
        appointment.setStatus(AppointmentStatus.pending);

        Appointment saved = appointmentRepository.save(appointment);
        log.info("✅ Đặt lịch thành công! ID: {}, Pet: {}, Doctor: {}",
                saved.getId(), request.getPetName(), doctor.getFullName());

        return mapToResponse(saved, doctor, service);
    }

    // Lấy tất cả lịch hẹn của user
    public List<AppointmentResponse> getUserAppointments(Long userId) {
        return appointmentRepository.findByUserIdOrderByAppointmentDateDescAppointmentTimeDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Lấy lịch hẹn sắp tới (pending + confirmed, chưa quá hạn)
    public List<AppointmentResponse> getUpcomingAppointments(Long userId) {
        List<AppointmentStatus> statuses = List.of(AppointmentStatus.pending, AppointmentStatus.confirmed);
        List<Appointment> appointments = appointmentRepository.findUpcomingAppointments(userId, LocalDate.now(), statuses);

        // Lọc thêm lịch chưa quá giờ trong ngày hôm nay
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        return appointments.stream()
                .filter(a -> {
                    if (a.getAppointmentDate().isAfter(today)) return true;
                    if (a.getAppointmentDate().equals(today)) {
                        return a.getAppointmentTime().isAfter(now);
                    }
                    return false;
                })
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Lấy lịch sử khám (completed, cancelled, no_show)
    public List<AppointmentResponse> getAppointmentHistory(Long userId) {
        List<AppointmentStatus> statuses = List.of(
                AppointmentStatus.completed,
                AppointmentStatus.cancelled,
                AppointmentStatus.no_show
        );
        List<Appointment> appointments = appointmentRepository.findAppointmentHistory(userId, statuses);
        return appointments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Lấy chi tiết lịch hẹn
    public AppointmentResponse getAppointmentById(Long appointmentId, Long userId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn"));

        if (!appointment.getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xem lịch hẹn này");
        }

        return mapToResponse(appointment);
    }

    // Hủy lịch hẹn
    @Transactional
    public AppointmentResponse cancelAppointment(Long appointmentId, Long userId, String reason) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn"));

        if (!appointment.getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền hủy lịch hẹn này");
        }

        // Kiểm tra ngày đã qua chưa
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (appointment.getAppointmentDate().isBefore(today)) {
            throw new RuntimeException("Không thể hủy lịch hẹn đã qua");
        }

        if (appointment.getAppointmentDate().equals(today) &&
                appointment.getAppointmentTime().isBefore(now)) {
            throw new RuntimeException("Không thể hủy lịch hẹn đã qua");
        }

        if (appointment.getStatus() == AppointmentStatus.completed) {
            throw new RuntimeException("Lịch hẹn đã hoàn thành, không thể hủy");
        }

        appointment.setStatus(AppointmentStatus.cancelled);
        appointment = appointmentRepository.save(appointment);

        log.info("❌ Hủy lịch hẹn! ID: {}, User: {}, Lý do: {}", appointmentId, userId, reason);

        return mapToResponse(appointment);
    }

    // Cập nhật trạng thái lịch hẹn (cho bác sĩ/admin)
    @Transactional
    public AppointmentResponse updateAppointmentStatus(Long appointmentId, AppointmentStatus status, Long userId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Chỉ bác sĩ của lịch đó hoặc admin mới được sửa
        if (!appointment.getDoctorId().equals(userId) && user.getRole() != User.Role.admin) {
            throw new RuntimeException("Bạn không có quyền cập nhật lịch hẹn này");
        }

        appointment.setStatus(status);
        appointment = appointmentRepository.save(appointment);

        log.info("🔄 Cập nhật trạng thái lịch hẹn! ID: {}, Status: {}", appointmentId, status);

        return mapToResponse(appointment);
    }

    // Đếm số lịch hẹn của user
    public long countUserAppointments(Long userId) {
        return appointmentRepository.findByUserIdOrderByAppointmentDateDescAppointmentTimeDesc(userId).size();
    }

    // Đếm số lịch hẹn sắp tới
    public long countUpcomingAppointments(Long userId) {
        return getUpcomingAppointments(userId).size();
    }

    // Map Appointment -> AppointmentResponse
    private AppointmentResponse mapToResponse(Appointment appointment) {
        User doctor = userRepository.findById(appointment.getDoctorId()).orElse(null);
        MedicalService service = medicalServiceRepository.findById(appointment.getServiceId()).orElse(null);
        return mapToResponse(appointment, doctor, service);
    }

    private AppointmentResponse mapToResponse(Appointment appointment, User doctor, MedicalService service) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .petName(appointment.getPetName())
                .petType(appointment.getPetType())
                .petBreed(appointment.getPetBreed())
                .petAge(appointment.getPetAge())
                .petWeight(appointment.getPetWeight())
                .petGender(appointment.getPetGender())
                .petColor(appointment.getPetColor())
                .symptoms(appointment.getSymptoms())
                .medicalHistory(appointment.getMedicalHistory())
                .doctorId(appointment.getDoctorId())
                .doctorName(doctor != null ? doctor.getFullName() : null)
                .serviceId(appointment.getServiceId())
                .serviceName(service != null ? service.getName() : null)
                .servicePrice(service != null ? service.getPrice() : null)
                .appointmentDate(appointment.getAppointmentDate())
                .appointmentTime(appointment.getAppointmentTime())
                .status(appointment.getStatus().name())
                .statusDisplayName(appointment.getStatus().getDisplayName())
                .notes(appointment.getNotes())
                .createdAt(appointment.getCreatedAt())
                .build();
    }
}