package com.pbl.parkingsystem.service;

import com.pbl.parkingsystem.entity.ParkingLot;
import com.pbl.parkingsystem.entity.ParkingSlot;
import com.pbl.parkingsystem.entity.SlotStatus;
import com.pbl.parkingsystem.repository.ParkingLotRepository;
import com.pbl.parkingsystem.repository.ParkingSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkingSlotService {
    private final ParkingSlotRepository parkingSlotRepository;
    private final ParkingLotRepository parkingLotRepository;

    public ParkingSlot createSlot(String slotNumber, Long parkingLotId){
        ParkingLot parkingLot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(()->new RuntimeException("Parking Lot not found"));

        ParkingSlot parkingSlot = new ParkingSlot();
        parkingSlot.setSlotNumber(slotNumber);
        parkingSlot.setStatus(SlotStatus.AVAILABLE);
        parkingSlot.setParkingLot(parkingLot);

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
        return parkingSlotRepository.findByParkingLotIdAndStatus(
                parkingLotId, SlotStatus.AVAILABLE );
    }
}
