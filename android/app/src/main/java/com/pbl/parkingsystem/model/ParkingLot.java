
package com.pbl.parkingsystem.model;

import java.math.BigDecimal;

public class ParkingLot {

    private Long id;
    private String name;
    private String location;
    private Integer twoWheelerSlots;
    private Integer fourWheelerSlots;

    private BigDecimal twoWheelerHourlyRate;
    private BigDecimal fourWheelerHourlyRate;

    public ParkingLot() {
    }

    public ParkingLot(
            String name,
            String location,
            Integer twoWheelerSlots,
            Integer fourWheelerSlots,
            BigDecimal twoWheelerHourlyRate,
            BigDecimal fourWheelerHourlyRate) {

        this.name = name;
        this.location = location;
        this.twoWheelerSlots = twoWheelerSlots;
        this.fourWheelerSlots = fourWheelerSlots;
        this.twoWheelerHourlyRate = twoWheelerHourlyRate;
        this.fourWheelerHourlyRate = fourWheelerHourlyRate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getTwoWheelerSlots() {
        return twoWheelerSlots;
    }

    public void setTwoWheelerSlots(Integer twoWheelerSlots) {
        this.twoWheelerSlots = twoWheelerSlots;
    }

    public Integer getFourWheelerSlots() {
        return fourWheelerSlots;
    }

    public void setFourWheelerSlots(Integer fourWheelerSlots) {
        this.fourWheelerSlots = fourWheelerSlots;
    }

    public BigDecimal getTwoWheelerHourlyRate() {
        return twoWheelerHourlyRate;
    }

    public void setTwoWheelerHourlyRate(BigDecimal twoWheelerHourlyRate) {
        this.twoWheelerHourlyRate = twoWheelerHourlyRate;
    }

    public BigDecimal getFourWheelerHourlyRate() {
        return fourWheelerHourlyRate;
    }

    public void setFourWheelerHourlyRate(BigDecimal fourWheelerHourlyRate) {
        this.fourWheelerHourlyRate = fourWheelerHourlyRate;
    }
}
