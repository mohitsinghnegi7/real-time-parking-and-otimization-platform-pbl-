
package com.pbl.parkingsystem.service;

import com.pbl.parkingsystem.entity.*;
import com.pbl.parkingsystem.repository.BookingRepository;
import com.pbl.parkingsystem.repository.ParkingLotRepository;
import com.pbl.parkingsystem.repository.ParkingSlotRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ParkingLotService {

    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public ParkingLot createParkingLot(ParkingLot parkingLot) {

        if (parkingLot.getName() == null
                || parkingLot.getName().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Parking lot name is required");
        }

        if (parkingLot.getLocation() == null
                || parkingLot.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Parking lot location is required");
        }

        int twoWheelerCount =
                parkingLot.getTwoWheelerSlots() == null
                        ? 0 : parkingLot.getTwoWheelerSlots();

        int fourWheelerCount =
                parkingLot.getFourWheelerSlots() == null
                        ? 0 : parkingLot.getFourWheelerSlots();

        if (twoWheelerCount < 0 || fourWheelerCount < 0) {
            throw new IllegalArgumentException(
                    "Slot capacity cannot be negative");
        }

        if (twoWheelerCount == 0 && fourWheelerCount == 0) {
            throw new IllegalArgumentException(
                    "At least one slot is required");
        }

        // Validate hourly parking rates
        if (parkingLot.getTwoWheelerHourlyRate() == null ||
                parkingLot.getFourWheelerHourlyRate() == null) {
            throw new IllegalArgumentException(
                    "Hourly parking rates are required");
        }

        if (parkingLot.getTwoWheelerHourlyRate()
                .compareTo(BigDecimal.ZERO) < 0 ||
                parkingLot.getFourWheelerHourlyRate()
                        .compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Parking rates cannot be negative");
        }

        parkingLot.setTwoWheelerSlots(twoWheelerCount);
        parkingLot.setFourWheelerSlots(fourWheelerCount);

        // Save the lot first to generate its ID.
        ParkingLot savedLot = parkingLotRepository.save(parkingLot);

        List<ParkingSlot> slots = new ArrayList<>();

        // Create all two-wheeler slots.
        for (int i = 1; i <= twoWheelerCount; i++) {

            ParkingSlot slot = new ParkingSlot();

            slot.setSlotNumber(String.format("TW-%03d", i));
            slot.setStatus(SlotStatus.AVAILABLE);
            slot.setVehicleType(VehicleType.TWO_WHEELER);
            slot.setParkingLot(savedLot);

            slots.add(slot);
        }

        // Create all four-wheeler slots.
        for (int i = 1; i <= fourWheelerCount; i++) {

            ParkingSlot slot = new ParkingSlot();

            slot.setSlotNumber(String.format("FW-%03d", i));
            slot.setStatus(SlotStatus.AVAILABLE);
            slot.setVehicleType(VehicleType.FOUR_WHEELER);
            slot.setParkingLot(savedLot);

            slots.add(slot);
        }

        // Save every generated slot.
        parkingSlotRepository.saveAll(slots);

        return savedLot;
    }

    public List<ParkingLot> getAllParkingLots() {
        return parkingLotRepository.findAll();
    }

    public ParkingLot getParkingLotById(Long id) {
        return parkingLotRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Parking lot not found"));
    }


    @Transactional
    public ParkingLot updateParkingRates(
            Long id,
            com.pbl.parkingsystem.dto.ParkingRateUpdateRequest request) {

        ParkingLot lot = parkingLotRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Parking lot not found"));

        if (request.getTwoWheelerHourlyRate() == null ||
                request.getFourWheelerHourlyRate() == null) {
            throw new IllegalArgumentException(
                    "Both hourly parking rates are required");
        }

        if (request.getTwoWheelerHourlyRate()
                .compareTo(BigDecimal.ZERO) < 0 ||
                request.getFourWheelerHourlyRate()
                        .compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Parking rates cannot be negative");
        }

        lot.setTwoWheelerHourlyRate(
                request.getTwoWheelerHourlyRate());

        lot.setFourWheelerHourlyRate(
                request.getFourWheelerHourlyRate());

        return parkingLotRepository.save(lot);
    }

    @Transactional
    public void deleteParkingLot(Long id) {

        ParkingLot lot = parkingLotRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Parking lot not found"));

        List<ParkingSlot> slots =
                parkingSlotRepository.findByParkingLotId(id);

        // Preserve booking history.
        for (ParkingSlot slot : slots) {
            if (bookingRepository.existsByParkingSlotId(slot.getId())) {
                throw new IllegalStateException(
                        "This parking lot has booking history and cannot be deleted.");
            }
        }

        parkingSlotRepository.deleteAll(slots);
        parkingLotRepository.delete(lot);
    }
}
