package com.pbl.parkingsystem.repository;

import com.pbl.parkingsystem.entity.ParkingSlot;
import com.pbl.parkingsystem.entity.SlotStatus;
import com.pbl.parkingsystem.entity.VehicleType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkingSlotRepository
        extends JpaRepository<ParkingSlot, Long> {

    List<ParkingSlot> findByStatus(SlotStatus status);

    List<ParkingSlot> findByParkingLotIdAndStatus(
            Long parkingLotId,
            SlotStatus status
    );

    List<ParkingSlot> findByParkingLotId(
            Long parkingLotId
    );

    long countByParkingLotIdAndVehicleType(
            Long parkingLotId,
            VehicleType vehicleType
    );

    long countByParkingLotIdAndStatus(
            Long parkingLotId,
            SlotStatus status
    );

    long countByParkingLotIdAndVehicleTypeAndStatus(
            Long parkingLotId,
            VehicleType vehicleType,
            SlotStatus status
    );

    boolean existsByParkingLotIdAndSlotNumber(
            Long parkingLotId,
            String slotNumber
    );
}