
package com.pbl.parkingsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAnalyticsResponse {

    private long todayPayments;
    private BigDecimal todayRevenue;

    private long yesterdayPayments;
    private BigDecimal yesterdayRevenue;

    private long thisMonthPayments;
    private BigDecimal thisMonthRevenue;

    private long totalPayments;
    private BigDecimal totalRevenue;
}
