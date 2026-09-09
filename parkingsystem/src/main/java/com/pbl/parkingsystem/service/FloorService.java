package com.pbl.parkingsystem.service;

import com.pbl.parkingsystem.entity.Floor;
import com.pbl.parkingsystem.entity.ParkingLot;
import com.pbl.parkingsystem.repository.FloorRepository;
import com.pbl.parkingsystem.repository.ParkingLotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FloorService {
    private final FloorRepository floorRepository;
    private final ParkingLotRepository parkingLotRepository;

    public Floor createFloor(Integer floorNumber,Long parkingLotId) {
        ParkingLot parkingLot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() ->
                        new RuntimeException("Parking lot not found"));

        Floor floor = new Floor();

        floor.setFloorNumber(floorNumber);
        floor.setParkingLot(parkingLot);

        return floorRepository.save(floor);
    }

    public List<Floor> getAllFloors() {
        return floorRepository.findAll();
    }

    public List<Floor> getFloorsByParkingLot(Long parkingLotId) {
        if (!parkingLotRepository.existsById(parkingLotId)) {
            throw new RuntimeException("Parking lot not found");
        }
        return floorRepository.findByParkingLotId(parkingLotId);
    }

    public Floor getFloorById(Long id) {
        return floorRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Floor not found"));
    }
}
