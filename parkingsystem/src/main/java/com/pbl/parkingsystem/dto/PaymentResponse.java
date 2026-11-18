
package com.pbl.parkingsystem.dto;

import com.pbl.parkingsystem.entity.PaymentMethod;
import com.pbl.parkingsystem.entity.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PaymentResponse {

    private Long id;

    private Long bookingId;

    private BigDecimal amount;

    private PaymentMethod paymentMethod;

    private PaymentStatus status;

    private String razorpayOrderId;

    private String razorpayPaymentId;

    private String transactionReference;

    private LocalDateTime createdAt;

    private LocalDateTime paidAt;
}
