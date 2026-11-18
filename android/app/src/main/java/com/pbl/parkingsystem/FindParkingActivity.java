
package com.pbl.parkingsystem;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pbl.parkingsystem.model.ParkingLot;
import com.pbl.parkingsystem.model.ParkingSlot;
import com.pbl.parkingsystem.network.ApiService;
import com.pbl.parkingsystem.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindParkingActivity extends AppCompatActivity {

    private Spinner spinnerLot, spinnerVehicleType;
    private TextView tvAvailability, tvEmpty;
    private LinearLayout slotsContainer;
    private ProgressBar progressBar;
    private Button btnRefresh;

    private ApiService apiService;
    private final List<ParkingLot> parkingLots = new ArrayList<>();
    private final List<ParkingSlot> parkingSlots = new ArrayList<>();

    private boolean loadingLots = false;
    private boolean loadingSlots = false;
    private Long selectedLotId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_parking);

        spinnerLot = findViewById(R.id.spinnerLot);
        spinnerVehicleType = findViewById(R.id.spinnerVehicleType);
        tvAvailability = findViewById(R.id.tvAvailability);
        tvEmpty = findViewById(R.id.tvEmpty);
        slotsContainer = findViewById(R.id.slotsContainer);
        progressBar = findViewById(R.id.progressBar);
        btnRefresh = findViewById(R.id.btnRefresh);

        apiService = RetrofitClient.getApiService(this);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        String[] vehicleTypes = {
                "All Vehicle Types",
                "TWO_WHEELER",
                "FOUR_WHEELER"
        };

        ArrayAdapter<String> vehicleAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                vehicleTypes
        );
        vehicleAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        spinnerVehicleType.setAdapter(vehicleAdapter);

        spinnerLot.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        if (position >= 0 && position < parkingLots.size()) {
                            Long lotId = parkingLots.get(position).getId();

                            if (lotId != null &&
                                    !lotId.equals(selectedLotId)) {
                                selectedLotId = lotId;
                                loadSlots(lotId);
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {
                        selectedLotId = null;
                        parkingSlots.clear();
                        renderSlots();
                    }
                }
        );

        spinnerVehicleType.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {
                        renderSlots();
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {
                        renderSlots();
                    }
                }
        );

        btnRefresh.setOnClickListener(v -> {
            if (selectedLotId != null) {
                loadSlots(selectedLotId);
            } else {
                loadParkingLots();
            }
        });

        loadParkingLots();
    }

    private void loadParkingLots() {
        if (loadingLots) return;

        loadingLots = true;
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        apiService.getAllParkingLots().enqueue(
                new Callback<List<ParkingLot>>() {
                    @Override
                    public void onResponse(
                            Call<List<ParkingLot>> call,
                            Response<List<ParkingLot>> response) {

                        loadingLots = false;
                        progressBar.setVisibility(View.GONE);

                        if (isFinishing() || isDestroyed()) return;

                        if (!response.isSuccessful()
                                || response.body() == null) {
                            showEmpty("Unable to load parking lots.");
                            return;
                        }

                        parkingLots.clear();
                        parkingLots.addAll(response.body());

                        List<String> names = new ArrayList<>();
                        for (ParkingLot lot : parkingLots) {
                            names.add(lot.getName() == null
                                    ? "Parking Lot"
                                    : lot.getName());
                        }

                        if (names.isEmpty()) {
                            selectedLotId = null;
                            parkingSlots.clear();
                            renderSlots();
                            showEmpty("No parking lots available.");
                            return;
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                FindParkingActivity.this,
                                android.R.layout.simple_spinner_item,
                                names
                        );

                        adapter.setDropDownViewResource(
                                android.R.layout.simple_spinner_dropdown_item
                        );

                        spinnerLot.setAdapter(adapter);
                        spinnerLot.setSelection(0);
                    }

                    @Override
                    public void onFailure(
                            Call<List<ParkingLot>> call,
                            Throwable t) {

                        loadingLots = false;
                        progressBar.setVisibility(View.GONE);

                        if (isFinishing() || isDestroyed()) return;

                        showEmpty("Network error while loading parking lots.");
                    }
                }
        );
    }

    private void loadSlots(Long lotId) {
        if (loadingSlots) return;

        loadingSlots = true;
        progressBar.setVisibility(View.VISIBLE);
        btnRefresh.setEnabled(false);
        tvEmpty.setVisibility(View.GONE);

        apiService.getSlotsByParkingLot(lotId).enqueue(
                new Callback<List<ParkingSlot>>() {
                    @Override
                    public void onResponse(
                            Call<List<ParkingSlot>> call,
                            Response<List<ParkingSlot>> response) {

                        loadingSlots = false;
                        progressBar.setVisibility(View.GONE);
                        btnRefresh.setEnabled(true);

                        if (isFinishing() || isDestroyed()) return;

                        if (!response.isSuccessful()
                                || response.body() == null) {
                            showEmpty("Unable to load slots. Try refreshing.");
                            return;
                        }

                        // Ignore a stale response if another lot was selected.
                        if (!lotId.equals(selectedLotId)) return;

                        parkingSlots.clear();
                        parkingSlots.addAll(response.body());
                        renderSlots();
                    }

                    @Override
                    public void onFailure(
                            Call<List<ParkingSlot>> call,
                            Throwable t) {

                        loadingSlots = false;
                        progressBar.setVisibility(View.GONE);
                        btnRefresh.setEnabled(true);

                        if (isFinishing() || isDestroyed()) return;

                        showEmpty("Network error. Please refresh.");
                    }
                }
        );
    }

    private void renderSlots() {
        slotsContainer.removeAllViews();

        String selectedType = String.valueOf(
                spinnerVehicleType.getSelectedItem()
        );

        int total = 0;
        int available = 0;
        int reserved = 0;
        int occupied = 0;

        List<ParkingSlot> filteredSlots = new ArrayList<>();

        for (ParkingSlot slot : parkingSlots) {
            String type = slot.getVehicleType();

            if (!"All Vehicle Types".equals(selectedType)
                    && (type == null || !type.equalsIgnoreCase(selectedType))) {
                continue;
            }

            filteredSlots.add(slot);
            total++;

            String status = slot.getStatus();

            if ("AVAILABLE".equalsIgnoreCase(status)) {
                available++;
            } else if ("RESERVED".equalsIgnoreCase(status)) {
                reserved++;
            } else if ("OCCUPIED".equalsIgnoreCase(status)) {
                occupied++;
            }
        }

        tvAvailability.setText(
                "Total: " + total
                        + "    Available: " + available
                        + "\nReserved: " + reserved
                        + "    Occupied: " + occupied
        );

        if (filteredSlots.isEmpty()) {
            showEmpty("No slots found for this selection.");
            return;
        }

        tvEmpty.setVisibility(View.GONE);

        for (ParkingSlot slot : filteredSlots) {
            addSlotRow(slot);
        }
    }

    private void addSlotRow(ParkingSlot slot) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(14), dp(12), dp(14), dp(12));
        row.setBackgroundColor(0xFFFFFFFF);
        row.setElevation(dp(1));

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        params.setMargins(0, 0, 0, dp(10));
        row.setLayoutParams(params);

        TextView name = new TextView(this);
        name.setText("Slot: " + safe(slot.getSlotNumber()));
        name.setTextSize(17);
        name.setTextColor(0xFF171A21);
        name.setTypeface(null, android.graphics.Typeface.BOLD);
        row.addView(name);

        TextView type = new TextView(this);
        type.setText("Vehicle type: " + safe(slot.getVehicleType()));
        type.setTextSize(14);
        type.setTextColor(0xFF555B68);
        type.setPadding(0, dp(4), 0, dp(4));
        row.addView(type);

        String status = safe(slot.getStatus());

        TextView statusText = new TextView(this);
        statusText.setText("Status: " + status);
        statusText.setTextSize(14);
        statusText.setTypeface(null, android.graphics.Typeface.BOLD);

        int color;
        if ("AVAILABLE".equalsIgnoreCase(status)) {
            color = 0xFF16794B;
        } else if ("RESERVED".equalsIgnoreCase(status)) {
            color = 0xFF9A6700;
        } else {
            color = 0xFFB42318;
        }

        statusText.setTextColor(color);
        row.addView(statusText);

        slotsContainer.addView(row);
    }

    private void showEmpty(String message) {
        slotsContainer.removeAllViews();
        tvEmpty.setText(message);
        tvEmpty.setVisibility(View.VISIBLE);
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty()
                ? "N/A"
                : value;
    }

    private int dp(int value) {
        return (int) (
                value * getResources()
                        .getDisplayMetrics().density + 0.5f
        );
    }
}
