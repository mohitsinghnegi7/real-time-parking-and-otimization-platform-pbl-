
package com.pbl.parkingsystem.service;

import com.pbl.parkingsystem.dto.PaymentRequest;
import com.pbl.parkingsystem.dto.PaymentResponse;
import com.pbl.parkingsystem.dto.RazorpayOrderResponse;
import com.pbl.parkingsystem.dto.RazorpayVerifyRequest;

import com.pbl.parkingsystem.entity.*;

import com.pbl.parkingsystem.repository.BookingRepository;
import com.pbl.parkingsystem.repository.PaymentRepository;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;

import lombok.RequiredArgsConstructor;

import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final RazorpayClient razorpayClient;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    // Create internal payment record
    @Transactional
    public PaymentResponse createPayment(
            Long userId,
            PaymentRequest request) {

        if (request.getBookingId() == null) {
            throw new RuntimeException("Booking ID is required");
        }

        if (request.getPaymentMethod() == null) {
            throw new RuntimeException("Payment method is required");
        }

        Booking booking = bookingRepository.findById(
                request.getBookingId()
        ).orElseThrow(() ->
                new RuntimeException("Booking not found"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new RuntimeException(
                    "Booking does not belong to this user");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED ||
                booking.getStatus() == BookingStatus.COMPLETED) {

            throw new RuntimeException(
                    "Payment cannot be created for this booking");
        }

        boolean alreadyPaid = paymentRepository
                .findByBookingId(booking.getId())
                .stream()
                .anyMatch(payment ->
                        payment.getStatus() == PaymentStatus.SUCCESS);

        if (alreadyPaid) {
            throw new RuntimeException(
                    "Payment is already completed for this booking");
        }

        ParkingSlot slot = booking.getParkingSlot();
        ParkingLot lot = slot.getParkingLot();

        long totalSeconds = Duration.between(
                booking.getStartTime(),
                booking.getEndTime()
        ).getSeconds();

        if (totalSeconds <= 0) {
            throw new RuntimeException("Invalid booking duration");
        }

        // Charge for every started hour
        long billableHours = (totalSeconds + 3599) / 3600;

        BigDecimal hourlyRate;

        if (slot.getVehicleType() == VehicleType.TWO_WHEELER) {
            hourlyRate = lot.getTwoWheelerHourlyRate();

        } else if (slot.getVehicleType() == VehicleType.FOUR_WHEELER) {
            hourlyRate = lot.getFourWheelerHourlyRate();

        } else {
            throw new RuntimeException("Invalid vehicle type");
        }

        if (hourlyRate == null ||
                hourlyRate.compareTo(BigDecimal.ZERO) <= 0) {

            throw new RuntimeException(
                    "Parking rate is not configured correctly");
        }

        BigDecimal amount = hourlyRate.multiply(
                BigDecimal.valueOf(billableHours)
        );

        Payment payment = new Payment();

        payment.setBooking(booking);
        payment.setAmount(amount);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(PaymentStatus.PENDING);

        Payment savedPayment = paymentRepository.save(payment);

        return convertToResponse(savedPayment);
    }

    // Create Razorpay order
    @Transactional
    public RazorpayOrderResponse createRazorpayOrder(
            Long userId,
            PaymentRequest request) {

        if (request.getPaymentMethod() != PaymentMethod.RAZORPAY) {
            throw new RuntimeException(
                    "Payment method must be RAZORPAY");
        }

        PaymentResponse paymentResponse =
                createPayment(userId, request);

        Payment payment = paymentRepository.findById(
                paymentResponse.getId()
        ).orElseThrow(() ->
                new RuntimeException("Payment not found"));

        try {

            long amountInPaise = payment.getAmount()
                    .multiply(new BigDecimal("100"))
                    .longValueExact();

            if (amountInPaise < 100) {
                throw new RuntimeException(
                        "Minimum Razorpay payment amount is ₹1");
            }

            JSONObject orderRequest = new JSONObject();

            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");

            orderRequest.put(
                    "receipt",
                    "parking_payment_" + payment.getId()
            );

            JSONObject notes = new JSONObject();

            notes.put("paymentId", payment.getId());
            notes.put(
                    "bookingId",
                    payment.getBooking().getId()
            );

            orderRequest.put("notes", notes);

            Order razorpayOrder =
                    razorpayClient.orders.create(orderRequest);

            String orderId = razorpayOrder.get("id");

            payment.setRazorpayOrderId(orderId);

            paymentRepository.save(payment);

            return new RazorpayOrderResponse(
                    payment.getId(),
                    orderId,
                    razorpayKeyId,
                    amountInPaise,
                    "INR"
            );

        } catch (RazorpayException e) {

            throw new RuntimeException(
                    "Unable to create Razorpay order: "
                            + e.getMessage()
            );
        }
    }

    // Verify Razorpay payment and confirm booking

    // Verify Razorpay payment and confirm booking
    @Transactional
    public PaymentResponse verifyRazorpayPayment(
            Long userId,
            RazorpayVerifyRequest request) {

        if (request.getPaymentId() == null ||
                request.getRazorpayOrderId() == null ||
                request.getRazorpayPaymentId() == null ||
                request.getRazorpaySignature() == null) {

            throw new RuntimeException(
                    "All Razorpay verification fields are required");
        }

        Payment payment = paymentRepository.findById(
                request.getPaymentId()
        ).orElseThrow(() ->
                new RuntimeException("Payment not found"));

        Booking booking = payment.getBooking();

        if (!booking.getUser().getId().equals(userId)) {
            throw new RuntimeException(
                    "Payment does not belong to this user");
        }

        if (payment.getPaymentMethod() != PaymentMethod.RAZORPAY) {
            throw new RuntimeException(
                    "This is not a Razorpay payment");
        }

        if (!request.getRazorpayOrderId().equals(
                payment.getRazorpayOrderId())) {

            throw new RuntimeException(
                    "Razorpay order ID does not match");
        }

        // Handle already successful payment
        if (payment.getStatus() == PaymentStatus.SUCCESS) {

            if (request.getRazorpayPaymentId().equals(
                    payment.getRazorpayPaymentId())) {

                return convertToResponse(payment);
            }

            throw new RuntimeException(
                    "Payment is already completed");
        }

        if (payment.getStatus() != PaymentStatus.PENDING ||
                booking.getStatus() != BookingStatus.PENDING) {

            throw new RuntimeException(
                    "Payment or booking is not in pending state");
        }

        try {

            // 1. Verify Razorpay signature
            JSONObject options = new JSONObject();

            options.put(
                    "razorpay_order_id",
                    request.getRazorpayOrderId()
            );

            options.put(
                    "razorpay_payment_id",
                    request.getRazorpayPaymentId()
            );

            options.put(
                    "razorpay_signature",
                    request.getRazorpaySignature()
            );

            boolean signatureVerified =
                    Utils.verifyPaymentSignature(
                            options,
                            razorpayKeySecret
                    );

            if (!signatureVerified) {
                throw new RuntimeException(
                        "Invalid Razorpay payment signature");
            }

            // 2. Fetch actual payment from Razorpay
            com.razorpay.Payment razorpayPayment =
                    razorpayClient.payments.fetch(
                            request.getRazorpayPaymentId()
                    );

            String actualStatus =
                    razorpayPayment.get("status");

            String actualOrderId =
                    razorpayPayment.get("order_id");

            String actualCurrency =
                    razorpayPayment.get("currency");

            String actualPaymentId =
                    razorpayPayment.get("id");

            Number actualAmount =
                    razorpayPayment.get("amount");

            Boolean captured =
                    razorpayPayment.get("captured");

            // 3. Verify payment details
            long expectedAmountPaise = payment.getAmount()
                    .multiply(new BigDecimal("100"))
                    .longValueExact();

            if (!request.getRazorpayPaymentId().equals(
                    actualPaymentId)) {

                throw new RuntimeException(
                        "Razorpay payment ID mismatch");
            }

            if (!request.getRazorpayOrderId().equals(
                    actualOrderId)) {

                throw new RuntimeException(
                        "Razorpay order ID mismatch");
            }

            if (!"INR".equals(actualCurrency)) {
                throw new RuntimeException(
                        "Invalid payment currency");
            }

            if (actualAmount == null ||
                    actualAmount.longValue() != expectedAmountPaise) {

                throw new RuntimeException(
                        "Payment amount mismatch");
            }

            if (!"captured".equalsIgnoreCase(actualStatus) ||
                    !Boolean.TRUE.equals(captured)) {

                throw new RuntimeException(
                        "Payment has not been captured by Razorpay");
            }

            // 4. Mark payment successful
            payment.setRazorpayPaymentId(
                    request.getRazorpayPaymentId()
            );

            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());

            // 5. Confirm booking
            booking.setStatus(BookingStatus.CONFIRMED);

            Payment savedPayment =
                    paymentRepository.save(payment);

            bookingRepository.save(booking);

            return convertToResponse(savedPayment);

        } catch (RazorpayException e) {

            throw new RuntimeException(
                    "Razorpay verification failed: "
                            + e.getMessage()
            );
        }
    }


    // Convert entity to response DTO
    private PaymentResponse convertToResponse(Payment payment) {

        return new PaymentResponse(
                payment.getId(),
                payment.getBooking().getId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getRazorpayOrderId(),
                payment.getRazorpayPaymentId(),
                payment.getTransactionReference(),
                payment.getCreatedAt(),
                payment.getPaidAt()
        );
    }
}
