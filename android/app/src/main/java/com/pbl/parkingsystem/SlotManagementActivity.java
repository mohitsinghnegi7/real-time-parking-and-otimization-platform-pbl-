package com.pbl.parkingsystem;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pbl.parkingsystem.model.ParkingSlot;
import com.pbl.parkingsystem.network.ApiService;
import com.pbl.parkingsystem.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SlotManagementActivity extends AppCompatActivity {

    private LinearLayout layoutSlots;
    private ProgressBar progressSlots;
    private TextView tvEmptySlots;
    private TextView tvSelectedLot;

    private ApiService apiService;
    private Long parkingLotId;
    private String parkingLotName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slot_management);

        layoutSlots = findViewById(R.id.layoutSlots);
        progressSlots = findViewById(R.id.progressSlots);
        tvEmptySlots = findViewById(R.id.tvEmptySlots);
        tvSelectedLot = findViewById(R.id.tvSelectedLot);

        parkingLotId = getIntent().getLongExtra("parkingLotId", -1L);
        parkingLotName = getIntent().getStringExtra("parkingLotName");

        apiService = RetrofitClient.getApiService(this);

        if (parkingLotId == null || parkingLotId <= 0) {
            Toast.makeText(
                    this,
                    "Invalid parking lot",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        // Show lot name only; hide internal database ID.
        tvSelectedLot.setText(
                parkingLotName == null || parkingLotName.trim().isEmpty()
                        ? "Parking Lot"
                        : parkingLotName
        );

        tvEmptySlots.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (apiService != null
                && parkingLotId != null
                && parkingLotId > 0) {
            loadSlots();
        }
    }

    private void loadSlots() {

        progressSlots.setVisibility(View.VISIBLE);
        tvEmptySlots.setVisibility(View.GONE);

        apiService.getSlotsByParkingLot(parkingLotId)
                .enqueue(new Callback<List<ParkingSlot>>() {

                    @Override
                    public void onResponse(
                            retrofit2.Call<List<ParkingSlot>> call,
                            retrofit2.Response<List<ParkingSlot>> response) {

                        if (response.isSuccessful() && response.body() != null) {
                            displaySlots(response.body());
                        } else {
                            String error = "";

                            try {
                                if (response.errorBody() != null) {
                                    error = response.errorBody().string();
                                }
                            } catch (java.io.IOException e) {
                                error = e.getMessage();
                            }

                            android.util.Log.e(
                                    "SLOT_API_ERROR",
                                    "URL: " + call.request().url()
                                            + "\nHTTP: " + response.code()
                                            + "\nERROR BODY: " + error
                            );

                            Toast.makeText(
                                    SlotManagementActivity.this,
                                    "Slot API failed: " + response.code()
                                            + ". Check Logcat.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<ParkingSlot>> call,
                            Throwable t) {

                        progressSlots.setVisibility(View.GONE);
                        layoutSlots.removeAllViews();

                        tvEmptySlots.setVisibility(View.VISIBLE);
                        tvEmptySlots.setText(
                                "Connection failed. Please try again."
                        );

                        Toast.makeText(
                                SlotManagementActivity.this,
                                "Connection error: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void displaySlots(List<ParkingSlot> slots) {

        layoutSlots.removeAllViews();

        if (slots == null) {
            slots = new ArrayList<>();
        }

        List<ParkingSlot> twoWheelerSlots = new ArrayList<>();
        List<ParkingSlot> fourWheelerSlots = new ArrayList<>();

        for (ParkingSlot slot : slots) {

            if (slot == null) {
                continue;
            }

            String type = safe(slot.getVehicleType())
                    .toUpperCase(Locale.ROOT);

            if (type.contains("TWO_WHEELER")) {
                twoWheelerSlots.add(slot);

            } else if (type.contains("FOUR_WHEELER")) {
                fourWheelerSlots.add(slot);
            }
        }

        // Overall summary.
        addSectionTitle("Overall Summary");
        addSummaryCard("All Slots", slots);

        // Two-wheeler summary.
        addSectionTitle("Two-Wheeler Summary");
        addSummaryCard("Two-Wheeler Slots", twoWheelerSlots);

        // Four-wheeler summary.
        addSectionTitle("Four-Wheeler Summary");
        addSummaryCard("Four-Wheeler Slots", fourWheelerSlots);

        // Individual slots.
        addSectionTitle("Individual Slots");

        if (slots.isEmpty()) {
            tvEmptySlots.setVisibility(View.VISIBLE);
            tvEmptySlots.setText(
                    "No parking slots found for this lot."
            );
            return;
        }

        tvEmptySlots.setVisibility(View.GONE);

        for (ParkingSlot slot : slots) {
            if (slot != null) {
                layoutSlots.addView(createSlotCard(slot));
            }
        }
    }

    private void addSectionTitle(String title) {

        TextView heading = new TextView(this);

        heading.setText(title);
        heading.setTextSize(18);
        heading.setTextColor(Color.rgb(23, 26, 33));
        heading.setTypeface(null, Typeface.BOLD);
        heading.setPadding(0, dp(14), 0, dp(8));

        layoutSlots.addView(heading);
    }

    private void addSummaryCard(
            String title,
            List<ParkingSlot> slots) {

        int total = slots.size();
        int available = 0;
        int reserved = 0;
        int occupied = 0;

        for (ParkingSlot slot : slots) {

            String status = safe(slot.getStatus())
                    .toUpperCase(Locale.ROOT);

            if ("AVAILABLE".equals(status)) {
                available++;

            } else if ("RESERVED".equals(status)) {
                reserved++;

            } else if ("OCCUPIED".equals(status)) {
                occupied++;
            }
        }

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackgroundColor(Color.WHITE);
        card.setElevation(dp(2));

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(cardParams);

        TextView titleView = new TextView(this);

        titleView.setText(title + "  •  Total: " + total);
        titleView.setTextColor(Color.rgb(37, 99, 235));
        titleView.setTextSize(16);
        titleView.setTypeface(null, Typeface.BOLD);

        card.addView(titleView);

        addCountRow(card, "Available", available, 0xFF15803D);
        addCountRow(card, "Reserved", reserved, 0xFFD97706);
        addCountRow(card, "Occupied", occupied, 0xFFDC2626);

        layoutSlots.addView(card);
    }

    private void addCountRow(
            LinearLayout parent,
            String label,
            int count,
            int color) {

        TextView row = new TextView(this);

        row.setText(label + ": " + count);
        row.setTextSize(15);
        row.setTextColor(color);
        row.setPadding(0, dp(7), 0, dp(2));

        parent.addView(row);
    }

    private View createSlotCard(ParkingSlot slot) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(15), dp(13), dp(15), dp(13));
        card.setBackgroundColor(Color.WHITE);
        card.setElevation(dp(2));

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(params);

        TextView slotName = new TextView(this);

        slotName.setText(
                "Slot: " + safe(slot.getSlotNumber())
        );
        slotName.setTextSize(16);
        slotName.setTextColor(Color.rgb(23, 26, 33));
        slotName.setTypeface(null, Typeface.BOLD);

        TextView vehicleType = new TextView(this);

        vehicleType.setText(
                "Vehicle type: "
                        + formatValue(slot.getVehicleType())
        );
        vehicleType.setTextSize(14);
        vehicleType.setTextColor(Color.rgb(100, 116, 139));
        vehicleType.setPadding(0, dp(5), 0, dp(5));

        String status = safe(slot.getStatus())
                .toUpperCase(Locale.ROOT);

        TextView statusView = new TextView(this);

        statusView.setText(
                "Status: " + formatValue(status)
        );
        statusView.setTextSize(14);
        statusView.setTypeface(null, Typeface.BOLD);
        statusView.setPadding(0, dp(3), 0, 0);

        if ("AVAILABLE".equals(status)) {
            statusView.setTextColor(0xFF15803D);

        } else if ("RESERVED".equals(status)) {
            statusView.setTextColor(0xFFD97706);

        } else if ("OCCUPIED".equals(status)) {
            statusView.setTextColor(0xFFDC2626);

        } else {
            statusView.setTextColor(0xFF64748B);
        }

        card.addView(slotName);
        card.addView(vehicleType);
        card.addView(statusView);

        return card;
    }

    private String formatValue(String value) {

        if (value == null || value.trim().isEmpty()) {
            return "Not specified";
        }

        return value.replace("_", " ");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private int dp(int value) {
        return (int) (
                value * getResources()
                        .getDisplayMetrics().density + 0.5f
        );
    }
}
