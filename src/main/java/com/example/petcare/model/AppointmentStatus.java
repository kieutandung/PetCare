package com.example.petcare.model;

public enum AppointmentStatus {
    pending("Chờ xác nhận"),
    confirmed("Đã xác nhận"),
    in_progress("Đang khám"),
    completed("Đã hoàn thành"),
    cancelled("Đã hủy"),
    no_show("Không đến");

    private final String displayName;

    AppointmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}