package com.example.petcare.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class MedicalServiceResponse {
    private Long id;
    private String name;
    private String description;
    private Integer duration;
    private Double price;
    private String icon;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}