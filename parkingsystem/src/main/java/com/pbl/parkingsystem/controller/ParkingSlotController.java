package com.pbl.parkingsystem.controller;

import com.pbl.parkingsystem.entity.ParkingSlot;
import com.pbl.parkingsystem.entity.SlotStatus;
import com.pbl.parkingsystem.entity.VehicleType;
import com.pbl.parkingsystem.service.ParkingSlotService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking-slots")
@RequiredArgsConstructor
public class ParkingSlotController {

    private final ParkingSlotService parkingSlotService;

    // Create a slot directly under a parking lot.
    @PostMapping
    public ResponseEntity<ParkingSlot> createSlot(
            @RequestParam String slotNumber,
            @RequestParam Long parkingLotId,
            @RequestParam VehicleType vehicleType) {

        return ResponseEntity.ok(
                parkingSlotService.createSlot(
                        slotNumber,
                        parkingLotId,
                        vehicleType
                )
        );
    }

    // Get all parking slots.
    @GetMapping
    public ResponseEntity<List<ParkingSlot>> getAllSlots() {
        return ResponseEntity.ok(
                parkingSlotService.getAllSlots()
        );
    }

    // Get all available slots.
    @GetMapping("/available")
    public ResponseEntity<List<ParkingSlot>> getAvailableSlots() {
        return ResponseEntity.ok(
                parkingSlotService.getAvailableSlots()
        );
    }

    // Get a particular slot.
    @GetMapping("/{id}")
    public ResponseEntity<ParkingSlot> getSlotById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                parkingSlotService.getSlotById(id)
        );
    }

    // Update slot status.
    @PutMapping("/{id}/status")
    public ResponseEntity<ParkingSlot> updateSlotStatus(
            @PathVariable Long id,
            @RequestParam SlotStatus status) {

        return ResponseEntity.ok(
                parkingSlotService.updateSlotStatus(id, status)
        );
    }

    // Get all slots belonging to a parking lot.
    @GetMapping("/parking-lot/{parkingLotId}")
    public ResponseEntity<List<ParkingSlot>> getSlotsByParkingLot(
            @PathVariable Long parkingLotId) {

        return ResponseEntity.ok(
                parkingSlotService.getSlotsByParkingLot(parkingLotId)
        );
    }

    // Get available slots belonging to a parking lot.
    @GetMapping("/parking-lot/{parkingLotId}/available")
    public ResponseEntity<List<ParkingSlot>> getAvailableSlotsByParkingLot(
            @PathVariable Long parkingLotId) {

        return ResponseEntity.ok(
                parkingSlotService.getAvailableSlotsByParkingLot(
                        parkingLotId
                )
        );
    }

    // Delete a slot if allowed by the service.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSlot(
            @PathVariable Long id) {

        parkingSlotService.deleteSlot(id);

        return ResponseEntity.noContent().build();
    }
}
