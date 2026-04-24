package com.example.petcare.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class AppointmentResponse {

    private Long id;

    // Thông tin thú cưng
    private String petName;
    private String petType;
    private String petBreed;
    private Integer petAge;
    private Double petWeight;
    private String petGender;
    private String petColor;

    // Triệu chứng
    private String symptoms;
    private String medicalHistory;

    // Lịch hẹn
    private Long doctorId;
    private String doctorName;
    private Long serviceId;
    private String serviceName;
    private Double servicePrice;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String status;
    private String statusDisplayName;
    private String notes;
    private LocalDateTime createdAt;
}