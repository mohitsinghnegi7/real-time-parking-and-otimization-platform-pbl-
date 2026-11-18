
package com.pbl.parkingsystem.controller;

import com.pbl.parkingsystem.dto.PaymentAnalyticsResponse;
import com.pbl.parkingsystem.service.PaymentAnalyticsService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentAnalyticsController {

    private final PaymentAnalyticsService paymentAnalyticsService;

    @GetMapping("/summary")
    public ResponseEntity<PaymentAnalyticsResponse> getPaymentSummary() {

        PaymentAnalyticsResponse response =
                paymentAnalyticsService.getPaymentSummary();

        return ResponseEntity.ok(response);
    }
}
