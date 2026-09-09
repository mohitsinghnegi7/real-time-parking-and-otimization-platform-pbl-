package com.pbl.parkingsystem.controller;


import com.pbl.parkingsystem.service.BookingService;
import com.pbl.parkingsystem.entity.Booking;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<Booking> createBooking(
            @RequestParam Long userId,
            @RequestParam Long vehicleId,
            @RequestParam Long parkingSlotId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {

        return ResponseEntity.ok(
                bookingService.createBooking(
                        userId,
                        vehicleId,
                        parkingSlotId,
                        startTime,
                        endTime
                )
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Booking>> getUserBookings(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                bookingService.getUserBookings(userId)
        );
    }

    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<Booking> cancelBooking(
            @PathVariable Long bookingId,
            @RequestParam Long userId) {

        return ResponseEntity.ok(
                bookingService.cancelBooking(
                        bookingId,
                        userId
                )
        );
    }
}
