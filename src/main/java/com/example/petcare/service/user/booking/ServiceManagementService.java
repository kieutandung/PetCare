package com.example.petcare.service.user.booking;

import com.example.petcare.model.MedicalService;
import com.example.petcare.repository.user.MedicalServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceManagementService {

    private final MedicalServiceRepository medicalServiceRepository;

    // Lấy tất cả dịch vụ đang hoạt động
    public List<MedicalService> getAllActiveServices() {
        return medicalServiceRepository.findByIsActiveTrue();
    }

    // Lấy tất cả dịch vụ
    public List<MedicalService> getAllServices() {
        return medicalServiceRepository.findAll();
    }

    // Lấy dịch vụ theo id
    public MedicalService getServiceById(Long id) {
        return medicalServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dịch vụ"));
    }

    // Thêm dịch vụ
    public MedicalService createService(MedicalService service) {
        service.setIsActive(true);
        return medicalServiceRepository.save(service);
    }

    // Cập nhật dịch vụ
    public MedicalService updateService(Long id, MedicalService serviceDetails) {
        MedicalService service = getServiceById(id);
        service.setName(serviceDetails.getName());
        service.setDescription(serviceDetails.getDescription());
        service.setDuration(serviceDetails.getDuration());
        service.setPrice(serviceDetails.getPrice());
        service.setIcon(serviceDetails.getIcon());
        service.setIsActive(serviceDetails.getIsActive());
        return medicalServiceRepository.save(service);
    }

    // Xóa dịch vụ
    public void deleteService(Long id) {
        medicalServiceRepository.deleteById(id);
    }
}