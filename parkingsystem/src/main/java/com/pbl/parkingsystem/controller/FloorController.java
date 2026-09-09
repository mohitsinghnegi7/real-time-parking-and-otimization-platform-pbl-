package com.pbl.parkingsystem.controller;

import com.pbl.parkingsystem.entity.Floor;
import com.pbl.parkingsystem.service.FloorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/floors")
@RequiredArgsConstructor
public class FloorController {
    private final FloorService floorService;

    @PostMapping
    public ResponseEntity<Floor> createFloor(
            @RequestParam Integer floorNumber,
            @RequestParam Long parkingLotId) {

        return ResponseEntity.ok(
                floorService.createFloor(
                        floorNumber,
                        parkingLotId
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<Floor>> getAllFloors() {

        return ResponseEntity.ok(
                floorService.getAllFloors()
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}")
    public ResponseEntity<List<Floor>> getFloorsByParkingLot(
            @PathVariable Long parkingLotId) {

        return ResponseEntity.ok(
                floorService.getFloorsByParkingLot(parkingLotId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Floor> getFloorById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                floorService.getFloorById(id)
        );
    }
}
