package com.pbl.parkingsystem.model;

public class Booking {

    private Long bookingId;
    private Long userId;
    private Long vehicleId;
    private String vehicleNumber;
    private Long parkingSlotId;
    private String slotNumber;
    private Long parkingLotId;
    private String parkingLotName;
    private String startTime;
    private String endTime;
    private String status;

    public Booking() {
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public Long getParkingSlotId() {
        return parkingSlotId;
    }

    public String getSlotNumber() {
        return slotNumber;
    }

    public Long getParkingLotId() {
        return parkingLotId;
    }

    public String getParkingLotName() {
        return parkingLotName;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }
}
