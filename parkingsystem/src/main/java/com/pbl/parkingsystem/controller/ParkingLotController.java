
package com.pbl.parkingsystem.controller;

import com.pbl.parkingsystem.dto.ParkingRateUpdateRequest;
import com.pbl.parkingsystem.entity.ParkingLot;
import com.pbl.parkingsystem.service.ParkingLotService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/parking-lots")
@RequiredArgsConstructor
public class ParkingLotController {

    private final ParkingLotService parkingLotService;

    @PostMapping
    public ResponseEntity<ParkingLot> createParkingLot(
            @RequestBody ParkingLot parkingLot) {

        return ResponseEntity.ok(
                parkingLotService.createParkingLot(parkingLot));
    }

    @GetMapping
    public ResponseEntity<List<ParkingLot>> getAllParkingLots() {

        return ResponseEntity.ok(
                parkingLotService.getAllParkingLots());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkingLot> getParkingLotById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                parkingLotService.getParkingLotById(id));
    }

    @PutMapping("/{id}/pricing")
    public ResponseEntity<ParkingLot> updateParkingRates(
            @PathVariable Long id,
            @RequestBody ParkingRateUpdateRequest request) {

        return ResponseEntity.ok(
                parkingLotService.updateParkingRates(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteParkingLot(
            @PathVariable Long id) {

        try {
            parkingLotService.deleteParkingLot(id);

            return ResponseEntity.ok(
                    "Parking lot deleted successfully");

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }
    }
}
