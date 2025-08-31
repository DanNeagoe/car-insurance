package com.example.carins.service;

import com.example.carins.model.Car;
import com.example.carins.model.Claim;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.ClaimRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.web.dto.HistoryEvent;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final InsurancePolicyRepository policyRepository;
    private final ClaimRepository claimRepository;

    public CarService(CarRepository carRepository, InsurancePolicyRepository policyRepository, ClaimRepository claimRepository) {
        this.carRepository = carRepository;
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
    }

    public List<Car> listCars() {
        return carRepository.findAll();
    }

    public void assertCarExists(Long carId) {
        if (!carRepository.existsById(carId)) {
            throw new CarNotFoundException("Car " + carId + " not found");
        }
    }

    public boolean isInsuranceValid(Long carId, LocalDate date) {
        if (carId == null || date == null) return false;

        if (!carRepository.existsById(carId)) {
            throw new CarNotFoundException("Car " + carId + " not found");
        }

        return policyRepository.existsActiveOnDate(carId, date);
    }

    public List<HistoryEvent> loadHistory(Long carId) {
        assertCarExists(carId);
        List<HistoryEvent> events = new ArrayList<>();
        for (InsurancePolicy p : policyRepository.findByCarId(carId)) {
            events.add(new HistoryEvent(
                    "POLICY",
                    p.getStartDate(),
                    "Policy with " + (p.getProvider() != null ? p.getProvider() : "Unknown"),
                    Map.of("startDate", p.getStartDate(), "endDate", p.getEndDate())
            ));
        }
        for (Claim c : claimRepository.findByCar_IdOrderByClaimDateAsc(carId)) {
            events.add(new HistoryEvent(
                    "CLAIM",
                    c.getClaimDate(),
                    c.getDescription(),
                    Map.of("amount", c.getAmount())
            ));
        }
        events.sort(Comparator.comparing(HistoryEvent::getDate));
        return events;
    }

    @Transactional
    public Car registerCar(Car car) {
        if (carRepository.findByVin(car.getVin()).isPresent()) {
            throw new IllegalArgumentException("VIN already exists: " + car.getVin());
        }
        return carRepository.save(car);
    }
}
