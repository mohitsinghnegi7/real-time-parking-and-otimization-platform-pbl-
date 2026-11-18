package com.pbl.parkingsystem.service;

import com.pbl.parkingsystem.entity.*;
import com.pbl.parkingsystem.repository.BookingRepository;
import com.pbl.parkingsystem.repository.ParkingLotRepository;
import com.pbl.parkingsystem.repository.ParkingSlotRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkingSlotService {

    private final ParkingSlotRepository parkingSlotRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public ParkingSlot createSlot(
            String slotNumber,
            Long parkingLotId,
            VehicleType vehicleType) {

        ParkingLot lot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() ->
                        new RuntimeException("Parking lot not found"));

        if (slotNumber == null || slotNumber.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Slot number is required");
        }

        if (vehicleType == null) {
            throw new IllegalArgumentException(
                    "Vehicle type is required");
        }

        if (parkingSlotRepository.existsByParkingLotIdAndSlotNumber(
                parkingLotId, slotNumber)) {
            throw new IllegalArgumentException(
                    "Slot number already exists in this parking lot");
        }

        long currentCount =
                parkingSlotRepository.countByParkingLotIdAndVehicleType(
                        parkingLotId, vehicleType);

        if (vehicleType == VehicleType.TWO_WHEELER
                && currentCount >= lot.getTwoWheelerSlots()) {
            throw new IllegalStateException(
                    "Two-wheeler parking capacity is full");
        }

        if (vehicleType == VehicleType.FOUR_WHEELER
                && currentCount >= lot.getFourWheelerSlots()) {
            throw new IllegalStateException(
                    "Four-wheeler parking capacity is full");
        }

        ParkingSlot slot = new ParkingSlot();
        slot.setSlotNumber(slotNumber.trim());
        slot.setStatus(SlotStatus.AVAILABLE);
        slot.setVehicleType(vehicleType);
        slot.setParkingLot(lot);

        return parkingSlotRepository.save(slot);
    }

    public List<ParkingSlot> getAllSlots() {
        return parkingSlotRepository.findAll();
    }

    public ParkingSlot getSlotById(Long id) {
        return parkingSlotRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Parking slot not found"));
    }

    @Transactional
    public ParkingSlot updateSlotStatus(Long id, SlotStatus status) {

        ParkingSlot slot = getSlotById(id);

        slot.setStatus(status);

        return parkingSlotRepository.save(slot);
    }

    public List<ParkingSlot> getAvailableSlots() {
        return parkingSlotRepository.findByStatus(
                SlotStatus.AVAILABLE);
    }

    public List<ParkingSlot> getAvailableSlotsByParkingLot(
            Long parkingLotId) {

        return parkingSlotRepository.findByParkingLotIdAndStatus(
                parkingLotId, SlotStatus.AVAILABLE);
    }

    public List<ParkingSlot> getSlotsByParkingLot(
            Long parkingLotId) {

        if (!parkingLotRepository.existsById(parkingLotId)) {
            throw new RuntimeException("Parking lot not found");
        }

        return parkingSlotRepository.findByParkingLotId(parkingLotId);
    }

    @Transactional
    public void deleteSlot(Long id) {

        ParkingSlot slot = getSlotById(id);

        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "Only AVAILABLE slots can be deleted");
        }

        if (bookingRepository.existsByParkingSlotId(id)) {
            throw new IllegalStateException(
                    "Cannot delete this slot because booking history exists");
        }

        parkingSlotRepository.delete(slot);
    }
}
