package com.example.petcare.controller.admin;

import com.example.petcare.model.MedicalService;
import com.example.petcare.service.user.booking.ServiceManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class MedicalServiceController {

    private final ServiceManagementService serviceManagementService;

    // Lấy danh sách dịch vụ
    @GetMapping
    public ResponseEntity<?> getAllServices() {
        List<MedicalService> services = serviceManagementService.getAllActiveServices();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("services", services);
        return ResponseEntity.ok(response);
    }

    // Thêm dịch vụ
    @PostMapping("/admin")
    public ResponseEntity<?> addService(@RequestBody MedicalService service) {
        try {
            MedicalService saved = serviceManagementService.createService(service);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Thêm dịch vụ thành công");
            response.put("service", saved);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}