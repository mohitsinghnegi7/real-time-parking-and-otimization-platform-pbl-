
package com.pbl.parkingsystem.service;

import com.pbl.parkingsystem.dto.BookingResponse;
import com.pbl.parkingsystem.entity.*;

import com.pbl.parkingsystem.repository.BookingRepository;
import com.pbl.parkingsystem.repository.ParkingSlotRepository;
import com.pbl.parkingsystem.repository.UserRepository;
import com.pbl.parkingsystem.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingSlotRepository parkingSlotRepository;

    // Create booking
    @Transactional
    public BookingResponse createBooking(
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

        // Verify vehicle ownership
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

        // Verify vehicle and slot type
        if (vehicle.getVehicleType() != slot.getVehicleType()) {
            throw new RuntimeException(
                    "Vehicle type does not match parking slot type");
        }

        // Validate booking time
        if (startTime == null || endTime == null) {
            throw new RuntimeException(
                    "Start time and end time are required");
        }

        if (!startTime.isBefore(endTime)) {
            throw new RuntimeException(
                    "Invalid booking time");
        }

        if (!startTime.isAfter(LocalDateTime.now())) {
            throw new RuntimeException(
                    "Booking start time must be in the future");
        }

        // Check overlapping bookings
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
                    "Parking slot is already booked for this time");
        }

        // Create pending booking
        Booking booking = new Booking();

        booking.setUser(user);
        booking.setVehicle(vehicle);
        booking.setParkingSlot(slot);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);

        // Booking will be confirmed after successful payment
        booking.setStatus(BookingStatus.PENDING);

        // Temporarily reserve the slot
        slot.setStatus(SlotStatus.RESERVED);

        parkingSlotRepository.save(slot);

        Booking savedBooking = bookingRepository.save(booking);

        return convertToResponse(savedBooking);
    }

    // Get all bookings of a user
    public List<BookingResponse> getUserBookings(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }

        return bookingRepository.findByUserId(userId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Cancel booking
    @Transactional
    public BookingResponse cancelBooking(
            Long bookingId,
            Long userId) {

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

        // Do not allow cancellation of a paid booking here.
        // A refund workflow is required for that.
        boolean paymentSuccessful = booking.getStatus()
                == BookingStatus.CONFIRMED;

        if (paymentSuccessful) {
            throw new RuntimeException(
                    "Paid booking cannot be cancelled without refund processing");
        }

        booking.setStatus(BookingStatus.CANCELLED);

        ParkingSlot slot = booking.getParkingSlot();
        slot.setStatus(SlotStatus.AVAILABLE);

        parkingSlotRepository.save(slot);

        Booking savedBooking = bookingRepository.save(booking);

        return convertToResponse(savedBooking);
    }

    // Convert entity to response DTO
    private BookingResponse convertToResponse(Booking booking) {

        ParkingSlot slot = booking.getParkingSlot();
        ParkingLot parkingLot = slot.getParkingLot();
        Vehicle vehicle = booking.getVehicle();

        return new BookingResponse(
                booking.getId(),
                booking.getUser().getId(),
                vehicle.getId(),
                vehicle.getVehicleNumber(),
                slot.getId(),
                slot.getSlotNumber(),
                parkingLot.getId(),
                parkingLot.getName(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getStatus()
        );
    }
}
