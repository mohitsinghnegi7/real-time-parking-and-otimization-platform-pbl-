package com.pbl.parkingsystem.repository;

import com.pbl.parkingsystem.entity.Floor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FloorRepository extends JpaRepository<Floor, Long> {
    List<Floor> findByParkingLotId(Long parkingLotId);
}
