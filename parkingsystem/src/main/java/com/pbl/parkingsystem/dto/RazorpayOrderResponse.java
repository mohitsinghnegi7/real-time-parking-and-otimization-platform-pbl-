package com.pbl.parkingsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RazorpayOrderResponse {

    private Long paymentId;

    private String razorpayOrderId;

    private String razorpayKeyId;

    private Long amount;

    private String currency;
}
