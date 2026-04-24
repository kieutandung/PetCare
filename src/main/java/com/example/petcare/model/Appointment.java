package com.example.petcare.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "appointments")
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "pet_name", nullable = false)
    private String petName;

    @Column(name = "pet_type")
    private String petType;

    @Column(name = "pet_breed")
    private String petBreed;

    @Column(name = "pet_age")
    private Integer petAge;

    @Column(name = "pet_weight")
    private Double petWeight;

    @Column(name = "pet_gender")
    private String petGender;

    @Column(name = "pet_color")
    private String petColor;

    @Column(name = "pet_image_url")
    private String petImageUrl;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "appointment_time", nullable = false)
    private LocalTime appointmentTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status = AppointmentStatus.pending;

    @Column(columnDefinition = "TEXT")
    private String symptoms;

    @Column(name = "medical_history", columnDefinition = "TEXT")
    private String medicalHistory;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}