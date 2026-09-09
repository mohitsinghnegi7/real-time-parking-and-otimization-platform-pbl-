package com.pbl.parkingsystem.repository;

import com.pbl.parkingsystem.entity.Booking;
import com.pbl.parkingsystem.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);

    @Query("""
            SELECT COUNT(b)
            FROM Booking b
            WHERE b.parkingSlot.id = :slotId
            AND b.status IN :statuses
            AND b.startTime < :endTime
            AND b.endTime > :startTime
            """)
    long countOverlappingBookings(
            @Param("slotId") Long slotId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("statuses") List<BookingStatus> statuses
    );
}
