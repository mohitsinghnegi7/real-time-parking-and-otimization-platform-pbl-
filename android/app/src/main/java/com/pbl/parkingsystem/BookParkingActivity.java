
package com.pbl.parkingsystem;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pbl.parkingsystem.dto.BookingResponse;
import com.pbl.parkingsystem.dto.PaymentRequest;
import com.pbl.parkingsystem.dto.PaymentResponse;
import com.pbl.parkingsystem.dto.RazorpayOrderResponse;
import com.pbl.parkingsystem.dto.RazorpayVerifyRequest;

import com.pbl.parkingsystem.model.ParkingLot;
import com.pbl.parkingsystem.model.ParkingSlot;
import com.pbl.parkingsystem.model.Vehicle;

import com.pbl.parkingsystem.network.ApiService;
import com.pbl.parkingsystem.network.RetrofitClient;
import com.pbl.parkingsystem.util.TokenManager;

import com.razorpay.Checkout;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookParkingActivity extends AppCompatActivity
        implements PaymentResultWithDataListener {

    private Spinner spinnerLot, spinnerVehicle, spinnerSlot;
    private Button btnStartTime, btnEndTime, btnConfirmBooking;
    private TextView tvBookingMessage;
    private ProgressBar progressBar;

    private ApiService apiService;
    private TokenManager tokenManager;

    private final List<ParkingLot> lots = new ArrayList<>();
    private final List<Vehicle> vehicles = new ArrayList<>();
    private final List<ParkingSlot> slots = new ArrayList<>();

    private Calendar startCalendar;
    private Calendar endCalendar;

    private boolean startSelected = false;
    private boolean endSelected = false;
    private boolean loadingSlots = false;

    // Razorpay details
    private Long currentPaymentId;
    private String currentRazorpayOrderId;
    private String currentRazorpayKeyId;
    private Long currentBookingId;

    private final SimpleDateFormat apiDateFormat =
            new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss",
                    Locale.US
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_parking);

        spinnerLot = findViewById(R.id.spinnerLot);
        spinnerVehicle = findViewById(R.id.spinnerVehicle);
        spinnerSlot = findViewById(R.id.spinnerSlot);

        btnStartTime = findViewById(R.id.btnStartTime);
        btnEndTime = findViewById(R.id.btnEndTime);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);

        tvBookingMessage = findViewById(R.id.tvBookingMessage);
        progressBar = findViewById(R.id.progressBar);

        tokenManager = new TokenManager(this);
        apiService = RetrofitClient.getApiService(this);

        // Initialize Razorpay Checkout
        Checkout.preload(getApplicationContext());

        startCalendar = Calendar.getInstance();
        startCalendar.add(Calendar.MINUTE, 30);
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);

        endCalendar = (Calendar) startCalendar.clone();
        endCalendar.add(Calendar.HOUR_OF_DAY, 1);

        btnStartTime.setOnClickListener(v -> pickDateTime(true));
        btnEndTime.setOnClickListener(v -> pickDateTime(false));

        btnConfirmBooking.setOnClickListener(
                v -> confirmBooking()
        );

        spinnerLot.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            android.view.View view,
                            int position,
                            long id) {

                        loadAvailableSlots();
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {
                    }
                });

        spinnerVehicle.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            android.view.View view,
                            int position,
                            long id) {

                        loadAvailableSlots();
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {
                    }
                });

        updateTimeButtons();

        loadLots();
        loadVehicles();
    }

    // Load parking lots
    private void loadLots() {

        progressBar.setVisibility(android.view.View.VISIBLE);

        apiService.getAllParkingLots().enqueue(
                new Callback<List<ParkingLot>>() {

                    @Override
                    public void onResponse(
                            Call<List<ParkingLot>> call,
                            Response<List<ParkingLot>> response) {

                        if (isFinishing()) return;

                        if (response.isSuccessful()
                                && response.body() != null) {

                            lots.clear();
                            lots.addAll(response.body());

                            List<String> names = new ArrayList<>();

                            for (ParkingLot lot : lots) {
                                names.add(
                                        lot.getName()
                                                + " - "
                                                + lot.getLocation()
                                );
                            }

                            spinnerLot.setAdapter(
                                    new ArrayAdapter<>(
                                            BookParkingActivity.this,
                                            android.R.layout.simple_spinner_dropdown_item,
                                            names
                                    )
                            );

                            if (lots.isEmpty()) {
                                tvBookingMessage.setText(
                                        "No parking lots available. Please try again later."
                                );
                            }

                        } else {
                            showMessage("Unable to load parking lots.");
                        }

                        progressBar.setVisibility(
                                android.view.View.GONE
                        );
                    }

                    @Override
                    public void onFailure(
                            Call<List<ParkingLot>> call,
                            Throwable t) {

                        if (isFinishing()) return;

                        progressBar.setVisibility(
                                android.view.View.GONE
                        );

                        showMessage(
                                "Network error while loading parking lots."
                        );
                    }
                });
    }

    // Load user vehicles
    private void loadVehicles() {

        Long userId = tokenManager.getUserId();

        if (userId == null) {
            showMessage("Please login again.");
            return;
        }

        apiService.getUserVehicles(userId).enqueue(
                new Callback<List<Vehicle>>() {

                    @Override
                    public void onResponse(
                            Call<List<Vehicle>> call,
                            Response<List<Vehicle>> response) {

                        if (isFinishing()) return;

                        if (response.isSuccessful()
                                && response.body() != null) {

                            vehicles.clear();
                            vehicles.addAll(response.body());

                            List<String> labels = new ArrayList<>();

                            for (Vehicle vehicle : vehicles) {
                                labels.add(
                                        vehicle.getVehicleNumber()
                                                + " ("
                                                + vehicle.getVehicleType()
                                                + ")"
                                );
                            }

                            spinnerVehicle.setAdapter(
                                    new ArrayAdapter<>(
                                            BookParkingActivity.this,
                                            android.R.layout.simple_spinner_dropdown_item,
                                            labels
                                    )
                            );

                            if (vehicles.isEmpty()) {
                                showMessage(
                                        "Add a vehicle first from My Vehicles."
                                );
                            }

                        } else {
                            showMessage("Unable to load your vehicles.");
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<Vehicle>> call,
                            Throwable t) {

                        if (isFinishing()) return;

                        showMessage(
                                "Network error while loading vehicles."
                        );
                    }
                });
    }

    // Load available slots
    private void loadAvailableSlots() {

        if (lots.isEmpty() || vehicles.isEmpty()) return;

        int lotPosition = spinnerLot.getSelectedItemPosition();
        int vehiclePosition = spinnerVehicle.getSelectedItemPosition();

        if (lotPosition < 0 || lotPosition >= lots.size()
                || vehiclePosition < 0
                || vehiclePosition >= vehicles.size()) {

            return;
        }

        ParkingLot selectedLot = lots.get(lotPosition);
        Vehicle selectedVehicle = vehicles.get(vehiclePosition);

        loadingSlots = true;

        slots.clear();

        spinnerSlot.setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        new ArrayList<>(
                                List.of("Loading available slots...")
                        )
                )
        );

        apiService.getSlotsByParkingLot(selectedLot.getId())
                .enqueue(new Callback<List<ParkingSlot>>() {

                    @Override
                    public void onResponse(
                            Call<List<ParkingSlot>> call,
                            Response<List<ParkingSlot>> response) {

                        if (isFinishing()) return;

                        loadingSlots = false;

                        if (response.isSuccessful()
                                && response.body() != null) {

                            slots.clear();

                            for (ParkingSlot slot : response.body()) {

                                if ("AVAILABLE".equalsIgnoreCase(
                                        slot.getStatus())
                                        && selectedVehicle.getVehicleType() != null
                                        && selectedVehicle.getVehicleType()
                                        .equalsIgnoreCase(
                                                slot.getVehicleType())) {

                                    slots.add(slot);
                                }
                            }

                            List<String> labels = new ArrayList<>();

                            for (ParkingSlot slot : slots) {
                                labels.add(
                                        slot.getSlotNumber()
                                                + " ("
                                                + slot.getVehicleType()
                                                + ")"
                                );
                            }

                            if (labels.isEmpty()) {
                                labels.add(
                                        "No available slots for this vehicle"
                                );
                            }

                            spinnerSlot.setAdapter(
                                    new ArrayAdapter<>(
                                            BookParkingActivity.this,
                                            android.R.layout.simple_spinner_dropdown_item,
                                            labels
                                    )
                            );

                            if (slots.isEmpty()) {
                                showMessage(
                                        "No available slots for this vehicle type."
                                );
                            } else {
                                tvBookingMessage.setText(
                                        "Select a slot and booking time."
                                );
                            }

                        } else {

                            slots.clear();

                            showMessage(
                                    "Unable to load parking slots."
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<ParkingSlot>> call,
                            Throwable t) {

                        if (isFinishing()) return;

                        loadingSlots = false;
                        slots.clear();

                        showMessage(
                                "Network error while loading slots."
                        );
                    }
                });
    }

    // Date and time picker
    private void pickDateTime(boolean isStart) {

        Calendar initial = isStart
                ? (Calendar) startCalendar.clone()
                : (Calendar) endCalendar.clone();

        DatePickerDialog dateDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {

                    initial.set(Calendar.YEAR, year);
                    initial.set(Calendar.MONTH, month);
                    initial.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    new TimePickerDialog(
                            this,
                            (timeView, hourOfDay, minute) -> {

                                initial.set(
                                        Calendar.HOUR_OF_DAY,
                                        hourOfDay
                                );

                                initial.set(
                                        Calendar.MINUTE,
                                        minute
                                );

                                initial.set(Calendar.SECOND, 0);
                                initial.set(Calendar.MILLISECOND, 0);

                                if (isStart) {

                                    startCalendar =
                                            (Calendar) initial.clone();

                                    startSelected = true;

                                    if (!endCalendar.after(startCalendar)) {

                                        endCalendar =
                                                (Calendar) startCalendar.clone();

                                        endCalendar.add(
                                                Calendar.HOUR_OF_DAY,
                                                1
                                        );

                                        endSelected = false;
                                    }

                                } else {

                                    if (!initial.after(startCalendar)) {

                                        showMessage(
                                                "End time must be after start time."
                                        );

                                        return;
                                    }

                                    endCalendar =
                                            (Calendar) initial.clone();

                                    endSelected = true;
                                }

                                updateTimeButtons();
                            },
                            initial.get(Calendar.HOUR_OF_DAY),
                            initial.get(Calendar.MINUTE),
                            true
                    ).show();
                },
                initial.get(Calendar.YEAR),
                initial.get(Calendar.MONTH),
                initial.get(Calendar.DAY_OF_MONTH)
        );

        dateDialog.getDatePicker().setMinDate(
                System.currentTimeMillis() - 1000
        );

        dateDialog.show();
    }

    // Update time buttons
    private void updateTimeButtons() {

        SimpleDateFormat displayFormat =
                new SimpleDateFormat(
                        "dd MMM yyyy, hh:mm a",
                        Locale.getDefault()
                );

        btnStartTime.setText(
                "Start: "
                        + displayFormat.format(startCalendar.getTime())
        );

        btnEndTime.setText(
                "End: "
                        + displayFormat.format(endCalendar.getTime())
        );
    }

    // Create booking
    private void confirmBooking() {

        Long userId = tokenManager.getUserId();

        if (userId == null) {
            showMessage("Please login again.");
            return;
        }

        if (lots.isEmpty()) {
            showMessage("No parking lots available.");
            return;
        }

        if (vehicles.isEmpty()) {
            showMessage("Please add a vehicle first.");
            return;
        }

        if (loadingSlots || slots.isEmpty()) {
            showMessage("No available slot selected.");
            return;
        }

        if (!startSelected || !endSelected) {
            showMessage("Please select start and end time.");
            return;
        }

        if (!endCalendar.after(startCalendar)) {
            showMessage("End time must be after start time.");
            return;
        }

        int vehiclePosition =
                spinnerVehicle.getSelectedItemPosition();

        int slotPosition =
                spinnerSlot.getSelectedItemPosition();

        if (vehiclePosition < 0
                || vehiclePosition >= vehicles.size()
                || slotPosition < 0
                || slotPosition >= slots.size()) {

            showMessage(
                    "Please select a valid vehicle and slot."
            );

            return;
        }

        Vehicle vehicle = vehicles.get(vehiclePosition);
        ParkingSlot slot = slots.get(slotPosition);

        btnConfirmBooking.setEnabled(false);

        progressBar.setVisibility(
                android.view.View.VISIBLE
        );

        apiService.createBooking(
                userId,
                vehicle.getId(),
                slot.getId(),
                apiDateFormat.format(startCalendar.getTime()),
                apiDateFormat.format(endCalendar.getTime())
        ).enqueue(new Callback<BookingResponse>() {

            @Override
            public void onResponse(
                    Call<BookingResponse> call,
                    Response<BookingResponse> response) {

                if (isFinishing()) return;

                progressBar.setVisibility(
                        android.view.View.GONE
                );

                if (response.isSuccessful()
                        && response.body() != null) {

                    BookingResponse booking = response.body();

                    currentBookingId = booking.getBookingId();

                    Toast.makeText(
                            BookParkingActivity.this,
                            "Booking created. Proceeding to payment.",
                            Toast.LENGTH_LONG
                    ).show();

                    createRazorpayOrder(currentBookingId);

                } else {

                    btnConfirmBooking.setEnabled(true);

                    String message =
                            "Booking failed. Please refresh slots and try again.";

                    try {
                        if (response.errorBody() != null) {

                            String error =
                                    response.errorBody().string();

                            if (!error.isEmpty()) {
                                message = error;
                            }
                        }

                    } catch (Exception ignored) {
                    }

                    showMessage(message);
                    loadAvailableSlots();
                }
            }

            @Override
            public void onFailure(
                    Call<BookingResponse> call,
                    Throwable t) {

                if (isFinishing()) return;

                btnConfirmBooking.setEnabled(true);

                progressBar.setVisibility(
                        android.view.View.GONE
                );

                showMessage(
                        "Network error while creating booking."
                );
            }
        });
    }

    // Create Razorpay order
    private void createRazorpayOrder(Long bookingId) {

        if (bookingId == null) {

            btnConfirmBooking.setEnabled(true);

            showMessage("Invalid booking ID.");
            return;
        }

        PaymentRequest request =
                new PaymentRequest(
                        bookingId,
                        "RAZORPAY"
                );

        progressBar.setVisibility(
                android.view.View.VISIBLE
        );

        apiService.createRazorpayOrder(request)
                .enqueue(new Callback<RazorpayOrderResponse>() {

                    @Override
                    public void onResponse(
                            Call<RazorpayOrderResponse> call,
                            Response<RazorpayOrderResponse> response) {

                        if (isFinishing()) return;

                        progressBar.setVisibility(
                                android.view.View.GONE
                        );

                        if (response.isSuccessful()
                                && response.body() != null) {

                            RazorpayOrderResponse order =
                                    response.body();

                            currentPaymentId = order.getPaymentId();
                            currentRazorpayOrderId =
                                    order.getRazorpayOrderId();

                            currentRazorpayKeyId =
                                    order.getRazorpayKeyId();

                            if (currentPaymentId == null
                                    || currentRazorpayOrderId == null
                                    || currentRazorpayKeyId == null
                                    || order.getAmount() == null) {

                                btnConfirmBooking.setEnabled(true);

                                showMessage(
                                        "Invalid Razorpay order response."
                                );

                                return;
                            }

                            openRazorpayCheckout(order);

                        } else {

                            btnConfirmBooking.setEnabled(true);

                            showMessage(
                                    "Unable to create Razorpay order. "
                                            + "Your booking is still pending."
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<RazorpayOrderResponse> call,
                            Throwable t) {

                        if (isFinishing()) return;

                        progressBar.setVisibility(
                                android.view.View.GONE
                        );

                        btnConfirmBooking.setEnabled(true);

                        showMessage(
                                "Network error while creating payment order. "
                                        + "Your booking is still pending."
                        );
                    }
                });
    }

    // Open Razorpay Checkout
    private void openRazorpayCheckout(
            RazorpayOrderResponse order) {

        try {

            Checkout checkout = new Checkout();

            checkout.setKeyID(
                    order.getRazorpayKeyId()
            );

            JSONObject options = new JSONObject();

            options.put(
                    "name",
                    "Parking Management"
            );

            options.put(
                    "description",
                    "Parking Booking Payment"
            );

            options.put(
                    "order_id",
                    order.getRazorpayOrderId()
            );

            options.put(
                    "currency",
                    order.getCurrency()
            );

            options.put(
                    "amount",
                    order.getAmount()
            );

            JSONObject theme = new JSONObject();
            theme.put("color", "#1565C0");

            options.put("theme", theme);

            checkout.open(this, options);

        } catch (Exception e) {

            btnConfirmBooking.setEnabled(true);

            showMessage(
                    "Unable to open Razorpay Checkout: "
                            + e.getMessage()
            );
        }
    }

    // Razorpay payment success callback
    @Override
    public void onPaymentSuccess(
            String razorpayPaymentId,
            PaymentData paymentData) {

        if (currentPaymentId == null
                || currentRazorpayOrderId == null) {

            showMessage(
                    "Payment details unavailable. Please contact support."
            );

            return;
        }

        String orderId = currentRazorpayOrderId;
        String signature = paymentData.getSignature();

        if (signature == null || signature.trim().isEmpty()) {

            showMessage(
                    "Payment signature unavailable. Please contact support."
            );

            return;
        }

        RazorpayVerifyRequest request =
                new RazorpayVerifyRequest(
                        currentPaymentId,
                        orderId,
                        razorpayPaymentId,
                        signature
                );

        progressBar.setVisibility(
                android.view.View.VISIBLE
        );

        apiService.verifyRazorpayPayment(request)
                .enqueue(new Callback<PaymentResponse>() {

                    @Override
                    public void onResponse(
                            Call<PaymentResponse> call,
                            Response<PaymentResponse> response) {

                        if (isFinishing()) return;

                        progressBar.setVisibility(android.view.View.GONE);

                        Log.e("PAYMENT_VERIFY",
                                "HTTP Code: " + response.code());

                        if (response.isSuccessful() && response.body() != null) {

                            Log.e("PAYMENT_VERIFY",
                                    "Payment Status: " + response.body().getStatus());

                            if ("SUCCESS".equalsIgnoreCase(
                                    response.body().getStatus())) {

                                Toast.makeText(
                                        BookParkingActivity.this,
                                        "Payment successful! Booking confirmed.",
                                        Toast.LENGTH_LONG
                                ).show();

                                setResult(RESULT_OK);
                                finish();

                            } else {
                                showMessage(
                                        "Verification returned status: "
                                                + response.body().getStatus()
                                );
                            }

                        } else {

                            try {
                                if (response.errorBody() != null) {
                                    Log.e("PAYMENT_VERIFY",
                                            "Error: " + response.errorBody().string());
                                }
                            } catch (Exception e) {
                                Log.e("PAYMENT_VERIFY",
                                        "Error reading response", e);
                            }

                            showMessage(
                                    "Payment verification failed. HTTP: "
                                            + response.code()
                            );
                        }
                    }
                    @Override
                    public void onFailure(
                            Call<PaymentResponse> call,
                            Throwable t) {

                        if (isFinishing()) return;

                        progressBar.setVisibility(
                                android.view.View.GONE
                        );

                        showMessage(
                                "Payment verification could not be completed. "
                                        + "Please check your booking before retrying."
                        );
                    }
                });
    }

    // Razorpay payment failure callback
    @Override
    public void onPaymentError(
            int code,
            String description,
            PaymentData paymentData) {

        btnConfirmBooking.setEnabled(true);

        showMessage(
                "Payment not completed: " + description
        );
    }

    // Show message
    private void showMessage(String message) {

        tvBookingMessage.setText(message);

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }
}
