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

    @PostMapping
    public ResponseEntity<ParkingSlot> createSlot(@RequestParam String slotNumber,
                                                  @RequestParam Long floorId,
                                                  @RequestParam VehicleType vehicleType) {
        return ResponseEntity.ok(parkingSlotService.createSlot(slotNumber,floorId,vehicleType));
    }

    @GetMapping
    public ResponseEntity<List<ParkingSlot>> getAllSlots() {
        return ResponseEntity.ok(parkingSlotService.getAllSlots());
    }

    @GetMapping("/available")
    public ResponseEntity<List<ParkingSlot>> getAvailableSlots() {
        return ResponseEntity.ok(parkingSlotService.getAvailableSlots());
    }
    @GetMapping("/floor/{floorId}/available")
    public ResponseEntity<List<ParkingSlot>> getAvailableSlotsByFloor(
            @PathVariable Long floorId) {

        return ResponseEntity.ok(
                parkingSlotService.getAvailableSlotsByFloor(floorId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkingSlot> getSlotById(@PathVariable Long id) {
        return ResponseEntity.ok(parkingSlotService.getSlotById(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ParkingSlot> updateSlotStatus(@PathVariable Long id,
                                                        @RequestParam SlotStatus status) {
        return ResponseEntity.ok(parkingSlotService.updateSlotStatus(id, status));
    }

    @GetMapping("/parking-lot/{parkingLotId}/available")
    public ResponseEntity<List<ParkingSlot>> getAvailableSlotsByParkingLot(
            @PathVariable Long parkingLotId) {
        return ResponseEntity.ok(parkingSlotService.getAvailableSlotsByParkingLot(parkingLotId));
    }


}
