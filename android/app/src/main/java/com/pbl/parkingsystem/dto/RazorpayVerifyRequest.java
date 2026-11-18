
package com.pbl.parkingsystem.dto;

public class RazorpayVerifyRequest {

    private Long paymentId;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

    public RazorpayVerifyRequest(
            Long paymentId,
            String razorpayOrderId,
            String razorpayPaymentId,
            String razorpaySignature) {

        this.paymentId = paymentId;
        this.razorpayOrderId = razorpayOrderId;
        this.razorpayPaymentId = razorpayPaymentId;
        this.razorpaySignature = razorpaySignature;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public String getRazorpaySignature() {
        return razorpaySignature;
    }
}
