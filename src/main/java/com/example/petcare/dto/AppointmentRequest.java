package com.example.petcare.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AppointmentRequest {

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
    private Long serviceId;
    private Long doctorId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String notes;
}