
package com.pbl.parkingsystem.controller;

import com.pbl.parkingsystem.dto.BookingResponse;
import com.pbl.parkingsystem.entity.Booking;
import com.pbl.parkingsystem.service.BookingService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;

    // Create a new booking
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @RequestParam Long userId,
            @RequestParam Long vehicleId,
            @RequestParam Long parkingSlotId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {

        BookingResponse response = bookingService.createBooking(
                userId,
                vehicleId,
                parkingSlotId,
                startTime,
                endTime
        );

        return ResponseEntity.ok(response);
    }

    // Get all bookings of a particular user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getUserBookings(
            @PathVariable Long userId) {

        List<BookingResponse> bookings =
                bookingService.getUserBookings(userId);

        return ResponseEntity.ok(bookings);
    }

    // Cancel a booking
    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long bookingId,
            @RequestParam Long userId) {

        BookingResponse response =
                bookingService.cancelBooking(bookingId, userId);

        return ResponseEntity.ok(response);
    }
}
