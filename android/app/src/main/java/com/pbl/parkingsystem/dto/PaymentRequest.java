
package com.pbl.parkingsystem.dto;

public class PaymentRequest {

    private Long bookingId;
    private String paymentMethod;

    public PaymentRequest(Long bookingId, String paymentMethod) {
        this.bookingId = bookingId;
        this.paymentMethod = paymentMethod;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }
}
