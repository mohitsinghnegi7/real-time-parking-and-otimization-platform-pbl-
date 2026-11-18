
package com.pbl.parkingsystem;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pbl.parkingsystem.model.ParkingLot;
import com.pbl.parkingsystem.network.ApiService;
import com.pbl.parkingsystem.network.RetrofitClient;

import java.math.BigDecimal;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParkingLotsActivity extends AppCompatActivity {

    private LinearLayout layoutParkingLots;
    private ProgressBar progressLots;
    private TextView tvEmptyLots;
    private Button btnAddParkingLot;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_lots);

        layoutParkingLots = findViewById(R.id.layoutParkingLots);
        progressLots = findViewById(R.id.progressLots);
        tvEmptyLots = findViewById(R.id.tvEmptyLots);
        btnAddParkingLot = findViewById(R.id.btnAddParkingLot);

        apiService = RetrofitClient.getApiService(this);

        btnAddParkingLot.setOnClickListener(v -> showAddLotDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadParkingLots();
    }

    private void loadParkingLots() {

        progressLots.setVisibility(View.VISIBLE);
        tvEmptyLots.setVisibility(View.GONE);

        apiService.getAllParkingLots().enqueue(new Callback<List<ParkingLot>>() {

            @Override
            public void onResponse(
                    Call<List<ParkingLot>> call,
                    Response<List<ParkingLot>> response) {

                progressLots.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    displayParkingLots(response.body());
                } else {
                    Toast.makeText(
                            ParkingLotsActivity.this,
                            "Unable to load parking lots. Code: " + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<List<ParkingLot>> call, Throwable t) {

                progressLots.setVisibility(View.GONE);

                Toast.makeText(
                        ParkingLotsActivity.this,
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void displayParkingLots(List<ParkingLot> lots) {

        layoutParkingLots.removeAllViews();

        if (lots == null || lots.isEmpty()) {
            tvEmptyLots.setVisibility(View.VISIBLE);
            return;
        }

        tvEmptyLots.setVisibility(View.GONE);

        for (ParkingLot lot : lots) {

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(18), dp(18), dp(18), dp(18));
            card.setBackgroundColor(0xFFFFFFFF);
            card.setElevation(dp(3));

            LinearLayout.LayoutParams cardParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            cardParams.setMargins(0, dp(6), 0, dp(14));
            card.setLayoutParams(cardParams);

            TextView name = new TextView(this);
            name.setText(lot.getName() == null ? "Parking Lot" : lot.getName());
            name.setTextSize(20);
            name.setTextColor(0xFF171A21);
            name.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView location = new TextView(this);
            location.setText("Location: " + safe(lot.getLocation()));
            location.setTextSize(14);
            location.setTextColor(0xFF64748B);
            location.setPadding(0, dp(8), 0, dp(12));

            TextView id = new TextView(this);
            id.setText("Lot ID: " + lot.getId());
            id.setTextSize(13);
            id.setTextColor(0xFF64748B);

            int twoWheelerCount = safeNumber(lot.getTwoWheelerSlots());
            int fourWheelerCount = safeNumber(lot.getFourWheelerSlots());
            int total = twoWheelerCount + fourWheelerCount;

            TextView twoWheeler = new TextView(this);
            twoWheeler.setText("Two-wheeler capacity: " + twoWheelerCount);
            twoWheeler.setTextSize(15);
            twoWheeler.setTextColor(0xFF334155);
            twoWheeler.setPadding(0, dp(10), 0, dp(4));

            TextView fourWheeler = new TextView(this);
            fourWheeler.setText("Four-wheeler capacity: " + fourWheelerCount);
            fourWheeler.setTextSize(15);
            fourWheeler.setTextColor(0xFF334155);

            TextView twoRate = new TextView(this);
            twoRate.setText(
                    "Two-wheeler rate: " + formatRate(lot.getTwoWheelerHourlyRate())
            );
            twoRate.setTextSize(14);
            twoRate.setTextColor(0xFF334155);
            twoRate.setPadding(0, dp(8), 0, dp(4));

            TextView fourRate = new TextView(this);
            fourRate.setText(
                    "Four-wheeler rate: " + formatRate(lot.getFourWheelerHourlyRate())
            );
            fourRate.setTextSize(14);
            fourRate.setTextColor(0xFF334155);

            TextView totalSlots = new TextView(this);
            totalSlots.setText("Total slots: " + total);
            totalSlots.setTextSize(15);
            totalSlots.setTextColor(0xFF2563EB);
            totalSlots.setTypeface(null, android.graphics.Typeface.BOLD);
            totalSlots.setPadding(0, dp(12), 0, dp(8));

            Button manageSlotsButton = new Button(this);
            manageSlotsButton.setText("Manage Slots");
            manageSlotsButton.setAllCaps(false);

            manageSlotsButton.setOnClickListener(v -> {

                if (lot.getId() == null) {
                    Toast.makeText(
                            this,
                            "Parking lot ID is missing",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                Intent intent = new Intent(
                        ParkingLotsActivity.this,
                        SlotManagementActivity.class
                );

                intent.putExtra("parkingLotId", lot.getId());
                intent.putExtra("parkingLotName", lot.getName());

                startActivity(intent);
            });

            Button deleteLotButton = new Button(this);
            deleteLotButton.setText("Delete Lot");
            deleteLotButton.setAllCaps(false);
            deleteLotButton.setTextColor(0xFFFFFFFF);

            deleteLotButton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFFDC2626)
            );

            deleteLotButton.setOnClickListener(v -> confirmDeleteLot(lot));

            card.addView(name);
            card.addView(location);
            card.addView(id);
            card.addView(twoWheeler);
            card.addView(fourWheeler);
            card.addView(twoRate);
            card.addView(fourRate);
            card.addView(totalSlots);
            card.addView(manageSlotsButton);
            card.addView(deleteLotButton);

            layoutParkingLots.addView(card);
        }
    }

    private void confirmDeleteLot(ParkingLot lot) {

        String lotName = lot.getName() == null
                ? "this parking lot" : lot.getName();

        new AlertDialog.Builder(this)
                .setTitle("Delete Parking Lot?")
                .setMessage(
                        "Are you sure you want to delete \"" + lotName +
                                "\"?\n\nThis action cannot be undone."
                )
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Delete", (dialog, which) -> deleteParkingLot(lot))
                .show();
    }

    private void deleteParkingLot(ParkingLot lot) {

        if (lot.getId() == null) {
            Toast.makeText(
                    this,
                    "Parking lot ID is missing",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        apiService.deleteParkingLot(lot.getId()).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(
                    Call<ResponseBody> call,
                    Response<ResponseBody> response) {

                if (response.isSuccessful()) {

                    Toast.makeText(
                            ParkingLotsActivity.this,
                            "Parking lot deleted successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    loadParkingLots();

                } else if (response.code() == 409) {

                    Toast.makeText(
                            ParkingLotsActivity.this,
                            "This lot has booking history and cannot be deleted.",
                            Toast.LENGTH_LONG
                    ).show();

                } else if (response.code() == 404) {

                    Toast.makeText(
                            ParkingLotsActivity.this,
                            "Parking lot was not found.",
                            Toast.LENGTH_LONG
                    ).show();

                    loadParkingLots();

                } else {

                    Toast.makeText(
                            ParkingLotsActivity.this,
                            "Delete failed. HTTP code: " + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

                Toast.makeText(
                        ParkingLotsActivity.this,
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void showAddLotDialog() {

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), dp(8), dp(20), 0);

        EditText etName = new EditText(this);
        etName.setHint("Parking lot name");
        etName.setSingleLine(true);

        EditText etLocation = new EditText(this);
        etLocation.setHint("Location");
        etLocation.setSingleLine(true);

        EditText etTwoWheeler = new EditText(this);
        etTwoWheeler.setHint("Two-wheeler capacity");
        etTwoWheeler.setInputType(InputType.TYPE_CLASS_NUMBER);

        EditText etFourWheeler = new EditText(this);
        etFourWheeler.setHint("Four-wheeler capacity");
        etFourWheeler.setInputType(InputType.TYPE_CLASS_NUMBER);

        EditText etTwoWheelerRate = new EditText(this);
        etTwoWheelerRate.setHint("Two-wheeler hourly rate (₹)");
        etTwoWheelerRate.setInputType(
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        EditText etFourWheelerRate = new EditText(this);
        etFourWheelerRate.setHint("Four-wheeler hourly rate (₹)");
        etFourWheelerRate.setInputType(
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        form.addView(etName);
        form.addView(etLocation);
        form.addView(etTwoWheeler);
        form.addView(etFourWheeler);
        form.addView(etTwoWheelerRate);
        form.addView(etFourWheelerRate);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add Parking Lot")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Create", null)
                .create();

        dialog.setOnShowListener(ignored ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

                    String name = etName.getText().toString().trim();
                    String location = etLocation.getText().toString().trim();

                    String twoText = etTwoWheeler.getText().toString().trim();
                    String fourText = etFourWheeler.getText().toString().trim();

                    String twoRateText = etTwoWheelerRate.getText().toString().trim();
                    String fourRateText = etFourWheelerRate.getText().toString().trim();

                    if (name.isEmpty() || location.isEmpty()) {
                        showMessage("Enter parking lot name and location");
                        return;
                    }

                    if (twoText.isEmpty() || fourText.isEmpty()) {
                        showMessage("Enter both parking capacities");
                        return;
                    }

                    if (twoRateText.isEmpty() || fourRateText.isEmpty()) {
                        showMessage("Enter both hourly parking rates");
                        return;
                    }

                    int two;
                    int four;
                    BigDecimal twoRate;
                    BigDecimal fourRate;

                    try {
                        two = Integer.parseInt(twoText);
                        four = Integer.parseInt(fourText);

                        twoRate = new BigDecimal(twoRateText);
                        fourRate = new BigDecimal(fourRateText);

                    } catch (NumberFormatException e) {
                        showMessage("Enter valid capacities and hourly rates");
                        return;
                    }

                    if (two < 0 || four < 0) {
                        showMessage("Capacity cannot be negative");
                        return;
                    }

                    if (two == 0 && four == 0) {
                        showMessage("At least one slot is required");
                        return;
                    }

                    if (twoRate.compareTo(BigDecimal.ZERO) <= 0 ||
                            fourRate.compareTo(BigDecimal.ZERO) <= 0) {
                        showMessage("Hourly rates must be greater than zero");
                        return;
                    }

                    ParkingLot lot = new ParkingLot(
                            name,
                            location,
                            two,
                            four,
                            twoRate,
                            fourRate
                    );

                    createParkingLot(lot, dialog);
                })
        );

        dialog.show();
    }

    private void createParkingLot(ParkingLot lot, AlertDialog dialog) {

        btnAddParkingLot.setEnabled(false);

        apiService.createParkingLot(lot).enqueue(new Callback<ParkingLot>() {

            @Override
            public void onResponse(
                    Call<ParkingLot> call,
                    Response<ParkingLot> response) {

                btnAddParkingLot.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {

                    dialog.dismiss();

                    Toast.makeText(
                            ParkingLotsActivity.this,
                            "Parking lot created successfully",
                            Toast.LENGTH_LONG
                    ).show();

                    loadParkingLots();

                } else {

                    String errorMessage = "Creation failed. HTTP code: " + response.code();

                    try {
                        if (response.errorBody() != null) {
                            String serverError = response.errorBody().string();

                            if (!serverError.isEmpty()) {
                                errorMessage += "\n" + serverError;
                            }
                        }
                    } catch (Exception ignored) {
                    }

                    new AlertDialog.Builder(ParkingLotsActivity.this)
                            .setTitle("Parking Lot Creation Failed")
                            .setMessage(errorMessage)
                            .setPositiveButton("OK", null)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<ParkingLot> call, Throwable t) {

                btnAddParkingLot.setEnabled(true);

                Toast.makeText(
                        ParkingLotsActivity.this,
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String safe(String value) {
        return value == null ? "Not specified" : value;
    }

    private int safeNumber(Integer value) {
        return value == null ? 0 : value;
    }

    private String formatRate(BigDecimal rate) {
        return rate == null ? "Not configured" : "₹" + rate + " / hour";
    }

    private int dp(int value) {
        return (int) (
                value * getResources().getDisplayMetrics().density + 0.5f
        );
    }
}
