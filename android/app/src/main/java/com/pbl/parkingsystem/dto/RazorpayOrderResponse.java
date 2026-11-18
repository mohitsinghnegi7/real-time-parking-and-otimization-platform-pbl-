
package com.pbl.parkingsystem.dto;

public class RazorpayOrderResponse {

    private Long paymentId;
    private String razorpayOrderId;
    private String razorpayKeyId;
    private Long amount;
    private String currency;

    public Long getPaymentId() {
        return paymentId;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public String getRazorpayKeyId() {
        return razorpayKeyId;
    }

    public Long getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }
}
