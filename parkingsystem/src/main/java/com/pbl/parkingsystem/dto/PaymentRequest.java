
package com.pbl.parkingsystem.dto;

import com.pbl.parkingsystem.entity.PaymentMethod;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {

    private Long bookingId;

    private PaymentMethod paymentMethod;
}
