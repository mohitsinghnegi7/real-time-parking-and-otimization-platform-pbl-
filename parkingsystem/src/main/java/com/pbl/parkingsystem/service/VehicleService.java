package com.pbl.parkingsystem.service;

import com.pbl.parkingsystem.entity.User;
import com.pbl.parkingsystem.entity.Vehicle;
import com.pbl.parkingsystem.entity.VehicleType;
import com.pbl.parkingsystem.repository.UserRepository;
import com.pbl.parkingsystem.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    public Vehicle addVehicle(Long userId, String vehicleNumber, VehicleType vehicleType){
        User user = userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("User not found"));

        if(vehicleRepository.findAll().stream()
                .anyMatch(vehicle ->
                        vehicle.getVehicleNumber()
                                .equalsIgnoreCase(vehicleNumber))){
            throw new RuntimeException("Vehicle already Registered");
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleNumber(vehicleNumber.toUpperCase());
        vehicle.setVehicleType(vehicleType);
        vehicle.setUser(user);

        return vehicleRepository.save(vehicle);
    }

    public List<Vehicle> getUserVehicles(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        return vehicleRepository.findByUserId(userId);
    }

    public Vehicle getVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Vehicle not found"));
    }

    public void deleteVehicle(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new RuntimeException("Vehicle not found");
        }
        vehicleRepository.deleteById(id);
    }
}
