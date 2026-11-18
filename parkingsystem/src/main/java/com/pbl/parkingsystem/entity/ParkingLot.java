
package com.pbl.parkingsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity(name = "parking_lots")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ParkingLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String location;

    private Integer twoWheelerSlots = 0;

    private Integer fourWheelerSlots = 0;

    @Column(precision = 10, scale = 2)
    private BigDecimal twoWheelerHourlyRate;

    @Column(precision = 10, scale = 2)
    private BigDecimal fourWheelerHourlyRate;
}
