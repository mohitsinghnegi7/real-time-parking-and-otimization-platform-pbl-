
package com.pbl.parkingsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pbl.parkingsystem.model.Vehicle;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VehicleAdapter
        extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {

    public interface OnDeleteClickListener {
        void onDeleteClick(Vehicle vehicle);
    }

    private final List<Vehicle> vehicles = new ArrayList<>();
    private final OnDeleteClickListener deleteListener;

    public VehicleAdapter(OnDeleteClickListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setVehicles(List<Vehicle> newVehicles) {
        vehicles.clear();

        if (newVehicles != null) {
            vehicles.addAll(newVehicles);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vehicle, parent, false);

        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull VehicleViewHolder holder,
            int position
    ) {
        Vehicle vehicle = vehicles.get(position);

        String number = vehicle.getVehicleNumber();

        holder.tvVehicleNumber.setText(
                number == null ? "Unknown vehicle" : number
        );

        String type = vehicle.getVehicleType() == null
                ? ""
                : vehicle.getVehicleType().toString();

        String normalizedType = type.toUpperCase(Locale.ROOT);

        if ("TWO_WHEELER".equals(normalizedType)) {
            holder.tvVehicleType.setText("Two Wheeler");
            holder.tvVehicleIcon.setText("🏍️");
        } else if ("FOUR_WHEELER".equals(normalizedType)) {
            holder.tvVehicleType.setText("Four Wheeler");
            holder.tvVehicleIcon.setText("🚗");
        } else {
            holder.tvVehicleType.setText(
                    type.isEmpty() ? "Vehicle" : type
            );
            holder.tvVehicleIcon.setText("🚘");
        }

        holder.btnDeleteVehicle.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(vehicle);
            }
        });
    }

    @Override
    public int getItemCount() {
        return vehicles.size();
    }

    static class VehicleViewHolder extends RecyclerView.ViewHolder {

        TextView tvVehicleNumber;
        TextView tvVehicleType;
        TextView tvVehicleIcon;
        ImageButton btnDeleteVehicle;

        VehicleViewHolder(@NonNull View itemView) {
            super(itemView);

            tvVehicleNumber =
                    itemView.findViewById(R.id.tvVehicleNumber);

            tvVehicleType =
                    itemView.findViewById(R.id.tvVehicleType);

            tvVehicleIcon =
                    itemView.findViewById(R.id.tvVehicleIcon);

            btnDeleteVehicle =
                    itemView.findViewById(R.id.btnDeleteVehicle);
        }
    }
}
