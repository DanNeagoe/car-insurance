package com.example.carins.web;

import com.example.carins.model.Car;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.service.CarNotFoundException;
import com.example.carins.service.PolicyValidationException;
import com.example.carins.web.dto.PolicyRequest;
import com.example.carins.web.dto.PolicyResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api")
public class PolicyController {

    private final CarRepository carRepo;
    private final InsurancePolicyRepository policyRepo;

    public PolicyController(CarRepository carRepo, InsurancePolicyRepository policyRepo) {
        this.carRepo = carRepo;
        this.policyRepo = policyRepo;
    }

    @PostMapping("/cars/{carId}/policies")
    public ResponseEntity<?> createPolicy(@PathVariable Long carId, @Valid @RequestBody PolicyRequest req) {
        Car car = carRepo.findById(carId)
                .orElseThrow(() -> new CarNotFoundException("Car " + carId + " not found"));

        if (req.getEndDate().isBefore(req.getStartDate())) {
            throw new PolicyValidationException("endDate must be >= startDate");
        }

        InsurancePolicy p = new InsurancePolicy();
        p.setCar(car);
        p.setProvider(req.getProvider());
        p.setStartDate(req.getStartDate());
        p.setEndDate(req.getEndDate());

        InsurancePolicy saved = policyRepo.save(p);

        var body = new PolicyResponse(
                saved.getId(),
                carId,
                saved.getProvider(),
                saved.getStartDate(),
                saved.getEndDate()
        );
        var location = URI.create("/api/cars/%d/policies/%d".formatted(carId, saved.getId()));
        return ResponseEntity.created(location).body(body);
    }
}