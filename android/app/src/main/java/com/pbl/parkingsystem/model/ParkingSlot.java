package com.pbl.parkingsystem.model;

public class ParkingSlot {

    private Long id;
    private String slotNumber;
    private String status;
    private String vehicleType;
    private ParkingLot parkingLot;

    public ParkingSlot() {
    }

    public Long getId() {
        return id;
    }

    public String getSlotNumber() {
        return slotNumber;
    }

    public String getStatus() {
        return status;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public ParkingLot getParkingLot() {
        return parkingLot;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSlotNumber(String slotNumber) {
        this.slotNumber = slotNumber;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public void setParkingLot(ParkingLot parkingLot) {
        this.parkingLot = parkingLot;
    }
}