
package com.pbl.parkingsystem;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.pbl.parkingsystem.model.Vehicle;
import com.pbl.parkingsystem.network.ApiService;
import com.pbl.parkingsystem.network.RetrofitClient;
import com.pbl.parkingsystem.util.TokenManager;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VehicleActivity extends AppCompatActivity {

    private TextView btnAddVehicle;
    private TextView tvVehicleCount;
    private RecyclerView recyclerVehicles;
    private LinearLayout emptyState;

    private VehicleAdapter vehicleAdapter;
    private ApiService apiService;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vehicle);

        // Initialize views
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnAddVehicle = findViewById(R.id.btnAddVehicle);
        tvVehicleCount = findViewById(R.id.tvVehicleCount);
        recyclerVehicles = findViewById(R.id.recyclerVehicles);
        emptyState = findViewById(R.id.emptyState);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Initialize session and API
        tokenManager = new TokenManager(this);
        apiService = RetrofitClient.getApiService(this);

        // Setup RecyclerView
        recyclerVehicles.setLayoutManager(
                new LinearLayoutManager(this)
        );

        vehicleAdapter = new VehicleAdapter(
                this::confirmDeleteVehicle
        );

        recyclerVehicles.setAdapter(vehicleAdapter);

        // Add vehicle button
        btnAddVehicle.setOnClickListener(
                v -> showAddVehicleBottomSheet()
        );

        // Load user's vehicles
        loadVehicles();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (vehicleAdapter != null) {
            loadVehicles();
        }
    }

    private Long getLoggedInUserId() {
        return tokenManager.getUserId();
    }

    // Fetch vehicles from backend
    private void loadVehicles() {

        Long userId = getLoggedInUserId();

        if (userId == null) {
            Toast.makeText(
                    this,
                    "User session not found. Please log in again.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        apiService.getUserVehicles(userId).enqueue(
                new Callback<List<Vehicle>>() {

                    @Override
                    public void onResponse(
                            Call<List<Vehicle>> call,
                            Response<List<Vehicle>> response
                    ) {
                        if (response.isSuccessful()
                                && response.body() != null) {

                            List<Vehicle> vehicles = response.body();

                            // Refresh RecyclerView data
                            vehicleAdapter.setVehicles(vehicles);

                            // Update vehicle count
                            int count = vehicles.size();

                            tvVehicleCount.setText(
                                    count + (
                                            count == 1
                                                    ? " vehicle added"
                                                    : " vehicles added"
                                    )
                            );

                            // Update empty state
                            boolean isEmpty = vehicles.isEmpty();

                            emptyState.setVisibility(
                                    isEmpty ? View.VISIBLE : View.GONE
                            );

                            recyclerVehicles.setVisibility(
                                    isEmpty ? View.GONE : View.VISIBLE
                            );

                        } else {
                            Toast.makeText(
                                    VehicleActivity.this,
                                    "Unable to load vehicles. HTTP "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();

                            Log.e(
                                    "LOAD_VEHICLES",
                                    "HTTP error: " + response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<Vehicle>> call,
                            Throwable t
                    ) {
                        Log.e(
                                "LOAD_VEHICLES",
                                "Failed to load vehicles",
                                t
                        );

                        Toast.makeText(
                                VehicleActivity.this,
                                "Failed to load vehicles: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    // Show delete confirmation
    private void confirmDeleteVehicle(Vehicle vehicle) {

        String vehicleNumber = vehicle.getVehicleNumber();

        new AlertDialog.Builder(this)
                .setTitle("Delete Vehicle")
                .setMessage(
                        "Are you sure you want to delete "
                                + vehicleNumber + "?"
                )
                .setNegativeButton(
                        "Cancel",
                        (dialog, which) -> dialog.dismiss()
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> deleteVehicle(vehicle)
                )
                .show();
    }

    // Delete vehicle from backend and refresh the list
    private void deleteVehicle(Vehicle vehicle) {

        Long vehicleId = vehicle.getId();

        if (vehicleId == null) {
            Toast.makeText(
                    this,
                    "Vehicle ID is missing. Cannot delete this vehicle.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        apiService.deleteVehicle(vehicleId).enqueue(
                new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(
                            Call<ResponseBody> call,
                            Response<ResponseBody> response
                    ) {
                        if (response.isSuccessful()) {

                            Toast.makeText(
                                    VehicleActivity.this,
                                    "Vehicle deleted successfully",
                                    Toast.LENGTH_SHORT
                            ).show();

                            // Fetch updated list from the database
                            loadVehicles();

                        } else {

                            Log.e(
                                    "DELETE_VEHICLE",
                                    "Delete failed. HTTP "
                                            + response.code()
                            );

                            Toast.makeText(
                                    VehicleActivity.this,
                                    "Failed to delete vehicle. HTTP "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ResponseBody> call,
                            Throwable t
                    ) {
                        Log.e(
                                "DELETE_VEHICLE",
                                "Delete request failed",
                                t
                        );

                        Toast.makeText(
                                VehicleActivity.this,
                                "Delete request failed: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    // Add vehicle bottom sheet
    private void showAddVehicleBottomSheet() {

        BottomSheetDialog dialog = new BottomSheetDialog(this);

        View view = LayoutInflater.from(this).inflate(
                R.layout.bottom_sheet_add_vehicle,
                null
        );

        dialog.setContentView(view);

        EditText etVehicleNumber =
                view.findViewById(R.id.etVehicleNumber);

        TextView optionFourWheeler =
                view.findViewById(R.id.optionFourWheeler);

        TextView optionTwoWheeler =
                view.findViewById(R.id.optionTwoWheeler);

        TextView btnSubmitVehicle =
                view.findViewById(R.id.btnSubmitVehicle);

        // Default selection
        final String[] selectedType = {"FOUR_WHEELER"};

        // Select four-wheeler
        optionFourWheeler.setOnClickListener(v -> {

            selectedType[0] = "FOUR_WHEELER";

            optionFourWheeler.setBackgroundColor(
                    Color.parseColor("#171A21")
            );

            optionFourWheeler.setTextColor(Color.WHITE);

            optionTwoWheeler.setBackgroundColor(
                    Color.parseColor("#F1F3F6")
            );

            optionTwoWheeler.setTextColor(
                    Color.parseColor("#5E6470")
            );
        });

        // Select two-wheeler
        optionTwoWheeler.setOnClickListener(v -> {

            selectedType[0] = "TWO_WHEELER";

            optionTwoWheeler.setBackgroundColor(
                    Color.parseColor("#171A21")
            );

            optionTwoWheeler.setTextColor(Color.WHITE);

            optionFourWheeler.setBackgroundColor(
                    Color.parseColor("#F1F3F6")
            );

            optionFourWheeler.setTextColor(
                    Color.parseColor("#5E6470")
            );
        });

        // Submit vehicle
        btnSubmitVehicle.setOnClickListener(v -> {

            String vehicleNumber = etVehicleNumber
                    .getText()
                    .toString()
                    .trim()
                    .toUpperCase();

            if (vehicleNumber.isEmpty()) {

                etVehicleNumber.setError(
                        "Enter vehicle number"
                );

                etVehicleNumber.requestFocus();
                return;
            }

            Long userId = getLoggedInUserId();

            if (userId == null) {

                Toast.makeText(
                        VehicleActivity.this,
                        "User session not found. Please log in again.",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            btnSubmitVehicle.setEnabled(false);
            btnSubmitVehicle.setAlpha(0.6f);

            apiService.addVehicle(
                    userId,
                    vehicleNumber,
                    selectedType[0]
            ).enqueue(new Callback<Vehicle>() {

                @Override
                public void onResponse(
                        Call<Vehicle> call,
                        Response<Vehicle> response
                ) {
                    btnSubmitVehicle.setEnabled(true);
                    btnSubmitVehicle.setAlpha(1f);

                    if (response.isSuccessful()) {

                        Toast.makeText(
                                VehicleActivity.this,
                                "Vehicle added successfully!",
                                Toast.LENGTH_SHORT
                        ).show();

                        dialog.dismiss();

                        // Refresh vehicle list
                        loadVehicles();

                    } else {

                        Log.e(
                                "ADD_VEHICLE",
                                "Add failed. HTTP "
                                        + response.code()
                        );

                        Toast.makeText(
                                VehicleActivity.this,
                                "Failed to add vehicle. HTTP "
                                        + response.code(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }

                @Override
                public void onFailure(
                        Call<Vehicle> call,
                        Throwable t
                ) {
                    btnSubmitVehicle.setEnabled(true);
                    btnSubmitVehicle.setAlpha(1f);

                    Log.e(
                            "ADD_VEHICLE",
                            "Add vehicle request failed",
                            t
                    );

                    Toast.makeText(
                            VehicleActivity.this,
                            "Network error: " + t.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            });
        });

        dialog.show();
    }
}
