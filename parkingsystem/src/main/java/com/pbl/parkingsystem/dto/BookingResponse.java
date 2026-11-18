package com.pbl.parkingsystem.dto;

import com.pbl.parkingsystem.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BookingResponse {

    private Long bookingId;
    private Long userId;
    private Long vehicleId;
    private String vehicleNumber;
    private Long parkingSlotId;
    private String slotNumber;
    private Long parkingLotId;
    private String parkingLotName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingStatus status;
}
