package com.example.carins;

import com.example.carins.model.Car;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.*;
import com.example.carins.service.CarService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MvcResult;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureMockMvc
@SpringBootTest
class CarInsuranceApplicationTests {

    @Autowired
    CarService service;
    @Autowired
    MockMvc mvc;
    @Autowired
    ClaimRepository claimRepository;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    CarRepository carRepository;
    @Autowired
    InsurancePolicyRepository policyRepository;
    @Autowired
    PolicyExpiryLogRepository expiryLogRepo;
    @Autowired
    private OwnerRepository ownerRepo;

    @Test
    void insuranceValidityBasic() {
        assertTrue(service.isInsuranceValid(1L, LocalDate.parse("2024-06-01")));
        assertTrue(service.isInsuranceValid(1L, LocalDate.parse("2025-06-01")));
        assertFalse(service.isInsuranceValid(2L, LocalDate.parse("2025-02-01")));
    }

    @Test
    void cannotInsertDuplicateVin() {
        var owner = ownerRepo.findById(1L).orElseThrow();

        Car car1 = new Car("TESTVIN123", "BMW", "X5", 2020, owner);
        carRepository.saveAndFlush(car1);

        Car car2 = new Car("TESTVIN123", "Audi", "A4", 2021, owner);

        assertThrows(DataIntegrityViolationException.class,
                () -> carRepository.saveAndFlush(car2));
    }

    @Test
    void savingPolicyWithoutEndDateFails() {
        Car car = carRepository.findById(1L).orElseThrow();
        InsurancePolicy p = new InsurancePolicy();
        p.setCar(car);
        p.setProvider("Test");
        p.setStartDate(LocalDate.parse("2025-01-01"));
        p.setEndDate(null);

        assertThrows(ConstraintViolationException.class, () -> {
            policyRepository.saveAndFlush(p);
        });
    }

    @Test
    void insuranceValid_404_whenCarMissing() throws Exception {
        mvc.perform(get("/api/cars/{carId}/insurance-valid", 9999)
                        .param("date", "2025-06-01"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("not found")));
    }

    @Test
    void insuranceValid_400_whenDateFormatBad() throws Exception {
        mvc.perform(get("/api/cars/{carId}/insurance-valid", 1)
                        .param("date", "2025/06/01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("yyyy-MM-dd")));
    }

    @Test
    void insuranceValid_400_whenDateOutOfRange() throws Exception {
        mvc.perform(get("/api/cars/{carId}/insurance-valid", 1)
                        .param("date", "1500-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("supported range")));
    }

    @Test
    void insuranceValid_200_nominal() throws Exception {
        mvc.perform(get("/api/cars/{carId}/insurance-valid", 1)
                        .param("date", "2025-06-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.carId").value(1))
                .andExpect(jsonPath("$.date").value("2025-06-01"))
                .andExpect(jsonPath("$.valid").isBoolean());
    }

    @Test
    void claimsAreLoadedFromImport() {
        var claims = claimRepository.findByCar_IdOrderByClaimDateAsc(1L);
        assertFalse(claims.isEmpty());
        assertEquals("Broken windshield", claims.get(0).getDescription());
    }

    @Test
    @Transactional
    void registerClaim_201_created_and_hasLocation() throws Exception {
        String body = """
        {"claimDate":"2025-08-01","description":"Unit test accident","amount":123.45}
        """;

        mvc.perform(post("/api/cars/{carId}/claims", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/cars/1/claims/")))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.carId").value(1))
                .andExpect(jsonPath("$.claimDate").value("2025-08-01"))
                .andExpect(jsonPath("$.description").value("Unit test accident"))
                .andExpect(jsonPath("$.amount").value(123.45));
    }

    @Test
    void registerClaim_404_whenCarMissing() throws Exception {
        String body = """
        {"claimDate":"2025-08-01","description":"Missing car","amount":10}
        """;

        mvc.perform(post("/api/cars/{carId}/claims", 99999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void registerClaim_400_whenBodyInvalid() throws Exception {
        String body = """
        {"claimDate":"2025-08-01","description":"no","amount":null}
        """;

        mvc.perform(post("/api/cars/{carId}/claims", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void history_200_returnsChronologicalEvents() throws Exception {
        MvcResult res = mvc.perform(get("/api/cars/{carId}/history", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].type", anyOf(is("POLICY"), is("CLAIM"))))
                .andReturn();

        String json = res.getResponse().getContentAsString();
        record Event(String type, String date) {}
        Event[] events = objectMapper.readValue(json, Event[].class);

        java.time.LocalDate prev = null;
        for (Event e : events) {
            java.time.LocalDate d = java.time.LocalDate.parse(e.date());
            if (prev != null) {
                org.junit.jupiter.api.Assertions.assertFalse(d.isBefore(prev),
                        "History not sorted: " + d + " before " + prev);
            }
            prev = d;
        }
    }

    @Test
    void history_404_whenCarMissing() throws Exception {
        mvc.perform(get("/api/cars/{carId}/history", 99999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    void expiredPolicyIsLoggedOnce() {
        var logs = expiryLogRepo.findAll();
        assertTrue(
                logs.stream().anyMatch(l -> l.getPolicyId().equals(99L)),
                "Expected expired policy with id=99 to be logged"
        );
    }
}
