
package com.pbl.parkingsystem.dto;

import java.math.BigDecimal;

public class PaymentResponse {

    private Long id;
    private Long bookingId;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String transactionReference;
    private String createdAt;
    private String paidAt;

    public Long getId() {
        return id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getPaidAt() {
        return paidAt;
    }
}
