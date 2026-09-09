package com.pbl.parkingsystem.controller;

import com.pbl.parkingsystem.entity.Vehicle;
import com.pbl.parkingsystem.entity.VehicleType;
import com.pbl.parkingsystem.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehilces")
@RequiredArgsConstructor
public class VehicleConroller {
    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<Vehicle> addVehicle(@RequestParam Long userId,
                                              @RequestParam String vehicelNumber,
                                              @RequestParam VehicleType vehicleType){
        return ResponseEntity.ok(vehicleService.addVehicle(userId, vehicelNumber, vehicleType));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Vehicle>> getUserVehicles(@PathVariable Long userId) {
        return ResponseEntity.ok(
                vehicleService.getUserVehicles(userId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable Long id) {
        return ResponseEntity.ok(
                vehicleService.getVehicleById(id)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok("Vehicle deleted successfully");
    }
}
