package com.pbl.parkingsystem.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ParkingRateUpdateRequest {

    private BigDecimal twoWheelerHourlyRate;

    private BigDecimal fourWheelerHourlyRate;
}
