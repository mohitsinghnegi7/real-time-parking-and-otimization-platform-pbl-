package com.pbl.parkingsystem.repository;

import com.pbl.parkingsystem.entity.ParkingSlot;
import com.pbl.parkingsystem.entity.SlotStatus;
import com.pbl.parkingsystem.entity.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {

    List<ParkingSlot> findByStatus(SlotStatus status);
    List<ParkingSlot> findByFloorIdAndStatus(
            Long floorId,
            SlotStatus status
    );
    long countByFloorParkingLotIdAndVehicleType(
            Long parkingLotId,
            VehicleType vehicleType
    );

    List<ParkingSlot> findByFloorParkingLotIdAndStatus(
            Long parkingLotId,
            SlotStatus status
    );
}
