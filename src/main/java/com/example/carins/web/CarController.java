package com.example.carins.web;

import com.example.carins.model.Car;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.ClaimRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.service.CarService;
import com.example.carins.web.dto.CarDto;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CarController {

    private final CarService service;
    private final CarRepository carRepository;
    private final InsurancePolicyRepository policyRepository;
    private final ClaimRepository claimRepository;

    public CarController(CarService service, CarRepository carRepository, InsurancePolicyRepository policyRepository, ClaimRepository claimRepository) {

        this.service = service;
        this.carRepository = carRepository;
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
    }

    @GetMapping("/cars/{carId}/history")
    public ResponseEntity<?> getCarHistory(@PathVariable Long carId) {
        var events = service.loadHistory(carId);
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/cars")
    public List<CarDto> getCars() {
        return service.listCars().stream().map(this::toDto).toList();
    }

    @GetMapping("/cars/{carId}/insurance-valid")
    public ResponseEntity<?> isInsuranceValid(
            @PathVariable Long carId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate MIN = LocalDate.of(1900, 1, 1);
        LocalDate MAX = LocalDate.of(2100, 12, 31);
        if (date.isBefore(MIN) || date.isAfter(MAX)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "error", "Bad Request",
                    "message", "Date out of supported range (1900-01-01..2100-12-31)"
            ));
        }

        boolean valid = service.isInsuranceValid(carId, date);
        return ResponseEntity.ok(Map.of(
                "carId", carId,
                "date", date.toString(),
                "valid", valid
        ));
    }

    private CarDto toDto(Car c) {
        var o = c.getOwner();
        return new CarDto(c.getId(), c.getVin(), c.getMake(), c.getModel(), c.getYearOfManufacture(),
                o != null ? o.getId() : null,
                o != null ? o.getName() : null,
                o != null ? o.getEmail() : null);
    }

    public record InsuranceValidityResponse(Long carId, String date, boolean valid) {}
}
