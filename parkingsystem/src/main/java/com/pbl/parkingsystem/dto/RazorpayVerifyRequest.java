package com.pbl.parkingsystem.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RazorpayVerifyRequest {

    private Long paymentId;

    private String razorpayOrderId;

    private String razorpayPaymentId;

    private String razorpaySignature;
}
