package com.pbl.parkingsystem.service;

import com.pbl.parkingsystem.entity.*;
import com.pbl.parkingsystem.repository.BookingRepository;
import com.pbl.parkingsystem.repository.ParkingSlotRepository;
import com.pbl.parkingsystem.repository.UserRepository;
import com.pbl.parkingsystem.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingSlotRepository parkingSlotRepository;

    public Booking createBooking(
            Long userId,
            Long vehicleId,
            Long parkingSlotId,
            LocalDateTime startTime,
            LocalDateTime endTime) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() ->
                        new RuntimeException("Vehicle not found"));

        if (!vehicle.getUser().getId().equals(userId)) {
            throw new RuntimeException(
                    "Vehicle does not belong to this user");
        }

        ParkingSlot slot = parkingSlotRepository.findById(parkingSlotId)
                .orElseThrow(() ->
                        new RuntimeException("Parking slot not found"));

        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new RuntimeException(
                    "Parking slot is not available");
        }

        if (vehicle.getVehicleType() != slot.getVehicleType()) {
            throw new RuntimeException(
                    "Vehicle type does not match parking slot type");
        }

        if (startTime == null || endTime == null) {
            throw new RuntimeException(
                    "Start time and end time are required");
        }

        if (!startTime.isBefore(endTime)) {
            throw new RuntimeException(
                    "Invalid booking time");
        }

        long overlappingBookings =
                bookingRepository.countOverlappingBookings(
                        parkingSlotId,
                        startTime,
                        endTime,
                        List.of(
                                BookingStatus.PENDING,
                                BookingStatus.CONFIRMED
                        )
                );

        if (overlappingBookings > 0) {
            throw new RuntimeException(
                    "Parking slot is already booked for this time"
            );
        }

        Booking booking = new Booking();

        booking.setUser(user);
        booking.setVehicle(vehicle);
        booking.setParkingSlot(slot);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setStatus(BookingStatus.CONFIRMED);

        slot.setStatus(SlotStatus.RESERVED);

        parkingSlotRepository.save(slot);

        return bookingRepository.save(booking);
    }


    public List<Booking> getUserBookings(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        return bookingRepository.findByUserId(userId);
    }

    public Booking cancelBooking(Long bookingId, Long userId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new RuntimeException("Booking not found"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new RuntimeException(
                    "Booking does not belong to this user");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException(
                    "Booking is already cancelled");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new RuntimeException(
                    "Completed booking cannot be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);

        ParkingSlot slot = booking.getParkingSlot();
        slot.setStatus(SlotStatus.AVAILABLE);

        parkingSlotRepository.save(slot);

        return bookingRepository.save(booking);
    }
}

