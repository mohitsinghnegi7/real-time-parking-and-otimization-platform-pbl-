package com.pbl.parkingsystem.service;

import com.pbl.parkingsystem.entity.ParkingLot;
import com.pbl.parkingsystem.repository.ParkingLotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkingLotService {

    private final ParkingLotRepository parkingLotRepository;

    public ParkingLot createParkingLot(ParkingLot parkingLot){
        return parkingLotRepository.save(parkingLot);
    }

    public List<ParkingLot> getAllParkingLots(){
        return parkingLotRepository.findAll();
    }

    public ParkingLot getParkingLotById(Long id){
        return parkingLotRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Parking lot not found"));
    }


}
