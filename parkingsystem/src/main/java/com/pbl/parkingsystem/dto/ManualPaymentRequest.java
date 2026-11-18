
package com.pbl.parkingsystem.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManualPaymentRequest {

    private Long paymentId;

    private String transactionReference;
}
