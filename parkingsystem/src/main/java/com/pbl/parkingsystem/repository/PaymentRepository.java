
package com.pbl.parkingsystem.repository;

import com.pbl.parkingsystem.entity.Payment;
import com.pbl.parkingsystem.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByBookingId(Long bookingId);

    List<Payment> findByBookingUserId(Long userId);

    List<Payment> findByStatus(PaymentStatus status);

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);

    // Successful payment count within a date range
    @Query("""
        SELECT COUNT(p)
        FROM Payment p
        WHERE p.status = :status
        AND p.paidAt >= :start
        AND p.paidAt < :end
    """)
    long countSuccessfulPaymentsBetween(
            @Param("status") PaymentStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Successful revenue within a date range
    @Query("""
        SELECT SUM(p.amount)
        FROM Payment p
        WHERE p.status = :status
        AND p.paidAt >= :start
        AND p.paidAt < :end
    """)
    BigDecimal getSuccessfulRevenueBetween(
            @Param("status") PaymentStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // All-time successful payment count
    long countByStatus(PaymentStatus status);

    // All-time successful revenue
    @Query("""
        SELECT SUM(p.amount)
        FROM Payment p
        WHERE p.status = :status
    """)
    BigDecimal getTotalSuccessfulRevenue(
            @Param("status") PaymentStatus status
    );
}
