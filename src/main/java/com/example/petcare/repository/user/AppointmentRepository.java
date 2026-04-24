package com.example.petcare.repository.user;

import com.example.petcare.model.Appointment;
import com.example.petcare.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByUserIdOrderByAppointmentDateDescAppointmentTimeDesc(Long userId);

    List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate date);

    List<Appointment> findByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);

    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
            Long doctorId,
            LocalDate date,
            LocalTime time,
            AppointmentStatus status
    );

    @Query("SELECT a.appointmentTime FROM Appointment a WHERE a.doctorId = :doctorId AND a.appointmentDate = :date AND a.status != 'cancelled'")
    List<LocalTime> findBookedTimesByDoctorAndDate(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    @Query("SELECT a FROM Appointment a WHERE a.userId = :userId AND a.appointmentDate >= :today AND a.status IN :statuses ORDER BY a.appointmentDate ASC, a.appointmentTime ASC")
    List<Appointment> findUpcomingAppointments(
            @Param("userId") Long userId,
            @Param("today") LocalDate today,
            @Param("statuses") List<AppointmentStatus> statuses
    );

    @Query("SELECT a FROM Appointment a WHERE a.userId = :userId AND a.status IN :statuses ORDER BY a.appointmentDate DESC")
    List<Appointment> findAppointmentHistory(
            @Param("userId") Long userId,
            @Param("statuses") List<AppointmentStatus> statuses
    );
}