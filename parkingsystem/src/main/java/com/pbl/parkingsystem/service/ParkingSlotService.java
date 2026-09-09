package com.pbl.parkingsystem.service;

import com.pbl.parkingsystem.entity.*;
import com.pbl.parkingsystem.repository.FloorRepository;
import com.pbl.parkingsystem.entity.Floor;
import com.pbl.parkingsystem.repository.ParkingSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkingSlotService {
    private final ParkingSlotRepository parkingSlotRepository;
    private final FloorRepository floorRepository;

    public ParkingSlot createSlot(String slotNumber, Long floorId, VehicleType vehicleType){
        Floor floor = floorRepository.findById(floorId)
                .orElseThrow(()->new RuntimeException("Floor not found"));

        ParkingLot parkingLot = floor.getParkingLot();

        long currentCount =
                parkingSlotRepository.countByFloorParkingLotIdAndVehicleType(
                        parkingLot.getId(),
                        vehicleType
                );

        if (vehicleType == VehicleType.TWO_WHEELER
                && currentCount >= parkingLot.getTwoWheelerSlots()) {

            throw new RuntimeException(
                    "Two-wheeler parking capacity is full");
        }

        if (vehicleType == VehicleType.FOUR_WHEELER
                && currentCount >= parkingLot.getFourWheelerSlots()) {

            throw new RuntimeException(
                    "Four-wheeler parking capacity is full");
        }


        ParkingSlot parkingSlot = new ParkingSlot();
        parkingSlot.setSlotNumber(slotNumber);
        parkingSlot.setStatus(SlotStatus.AVAILABLE);
        parkingSlot.setVehicleType(vehicleType);
        parkingSlot.setFloor(floor);

        return parkingSlotRepository.save(parkingSlot);
    }

    public List<ParkingSlot> getAllSlots() {
        return parkingSlotRepository.findAll();
    }

    public ParkingSlot getSlotById(Long id) {
        return parkingSlotRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Parking slot not found"));
    }

    public ParkingSlot updateSlotStatus(Long id, SlotStatus status){
        ParkingSlot slot = parkingSlotRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Parking Slot not found"));
        slot.setStatus(status);
        return parkingSlotRepository.save(slot);
    }

    public List<ParkingSlot> getAvailableSlots() {
        return parkingSlotRepository.findByStatus(SlotStatus.AVAILABLE);
    }

    public List<ParkingSlot> getAvailableSlotsByParkingLot(Long parkingLotId) {
        return parkingSlotRepository.findByFloorParkingLotIdAndStatus(
                parkingLotId,
                SlotStatus.AVAILABLE
        );

    }

    public List<ParkingSlot> getAvailableSlotsByFloor(
            Long floorId) {

        return parkingSlotRepository.findByFloorIdAndStatus(
                floorId,
                SlotStatus.AVAILABLE
        );
    }
}
