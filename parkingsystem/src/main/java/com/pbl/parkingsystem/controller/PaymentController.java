
package com.pbl.parkingsystem.controller;

import com.pbl.parkingsystem.dto.PaymentRequest;
import com.pbl.parkingsystem.dto.PaymentResponse;
import com.pbl.parkingsystem.dto.RazorpayOrderResponse;
import com.pbl.parkingsystem.dto.RazorpayVerifyRequest;

import com.pbl.parkingsystem.entity.User;

import com.pbl.parkingsystem.repository.UserRepository;
import com.pbl.parkingsystem.service.PaymentService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    @PostMapping("/create")
    public ResponseEntity<PaymentResponse> createPayment(
            @AuthenticationPrincipal String email,
            @RequestBody PaymentRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        PaymentResponse response =
                paymentService.createPayment(
                        user.getId(),
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/razorpay/create-order")
    public ResponseEntity<RazorpayOrderResponse> createRazorpayOrder(
            @AuthenticationPrincipal String email,
            @RequestBody PaymentRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        RazorpayOrderResponse response =
                paymentService.createRazorpayOrder(
                        user.getId(),
                        request
                );

        return ResponseEntity.ok(response);
    }

    // Verify Razorpay payment
    @PostMapping("/razorpay/verify")
    public ResponseEntity<PaymentResponse> verifyRazorpayPayment(
            @AuthenticationPrincipal String email,
            @RequestBody RazorpayVerifyRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        PaymentResponse response =
                paymentService.verifyRazorpayPayment(
                        user.getId(),
                        request
                );

        return ResponseEntity.ok(response);
    }
}
