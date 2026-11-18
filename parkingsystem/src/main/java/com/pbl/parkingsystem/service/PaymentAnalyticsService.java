
package com.pbl.parkingsystem.service;

import com.pbl.parkingsystem.dto.PaymentAnalyticsResponse;
import com.pbl.parkingsystem.entity.PaymentStatus;
import com.pbl.parkingsystem.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentAnalyticsService {

    private final PaymentRepository paymentRepository;

    public PaymentAnalyticsResponse getPaymentSummary() {

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));

        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();

        LocalDateTime yesterdayStart = today.minusDays(1).atStartOfDay();

        LocalDate monthStartDate = today.withDayOfMonth(1);
        LocalDateTime monthStart = monthStartDate.atStartOfDay();

        PaymentStatus success = PaymentStatus.SUCCESS;

        long todayPayments = paymentRepository.countSuccessfulPaymentsBetween(
                success, todayStart, tomorrowStart
        );

        BigDecimal todayRevenue = paymentRepository.getSuccessfulRevenueBetween(
                success, todayStart, tomorrowStart
        );

        long yesterdayPayments = paymentRepository.countSuccessfulPaymentsBetween(
                success, yesterdayStart, todayStart
        );

        BigDecimal yesterdayRevenue = paymentRepository.getSuccessfulRevenueBetween(
                success, yesterdayStart, todayStart
        );

        long thisMonthPayments = paymentRepository.countSuccessfulPaymentsBetween(
                success, monthStart, tomorrowStart
        );

        BigDecimal thisMonthRevenue = paymentRepository.getSuccessfulRevenueBetween(
                success, monthStart, tomorrowStart
        );

        long totalPayments = paymentRepository.countByStatus(success);

        BigDecimal totalRevenue =
                paymentRepository.getTotalSuccessfulRevenue(success);

        return new PaymentAnalyticsResponse(
                todayPayments,
                safeAmount(todayRevenue),

                yesterdayPayments,
                safeAmount(yesterdayRevenue),

                thisMonthPayments,
                safeAmount(thisMonthRevenue),

                totalPayments,
                safeAmount(totalRevenue)
        );
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}
