package com.example.carins.web;

import com.example.carins.model.Car;
import com.example.carins.model.Claim;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.ClaimRepository;
import com.example.carins.web.dto.ClaimRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api")
public class ClaimController {

    private final CarRepository carRepository;
    private final ClaimRepository claimRepository;

    public ClaimController(CarRepository carRepository, ClaimRepository claimRepository) {
        this.carRepository = carRepository;
        this.claimRepository = claimRepository;
    }

    @PostMapping("/cars/{carId}/claims")
    public ResponseEntity<?> registerClaim(@PathVariable Long carId, @Valid @RequestBody ClaimRequest request) {
        Car car = carRepository.findById(carId).orElse(null);
        if (car == null) {
            return ResponseEntity.notFound().build();
        }

        Claim claim = new Claim();
        claim.setCar(car);
        claim.setClaimDate(request.getClaimDate());
        claim.setDescription(request.getDescription());
        claim.setAmount(request.getAmount());

        Claim saved = claimRepository.save(claim);

        URI location = URI.create(String.format("/api/cars/%d/claims/%d", carId, saved.getId()));

        var body = new com.example.carins.web.dto.ClaimResponse(
                saved.getId(),
                saved.getCar().getId(),
                saved.getClaimDate(),
                saved.getDescription(),
                saved.getAmount()
        );

        return ResponseEntity.created(location).body(body);
    }
}