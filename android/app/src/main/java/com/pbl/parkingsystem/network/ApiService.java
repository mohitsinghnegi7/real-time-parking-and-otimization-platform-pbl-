
package com.pbl.parkingsystem.network;

import com.pbl.parkingsystem.dto.BookingResponse;
import com.pbl.parkingsystem.dto.LoginRequest;
import com.pbl.parkingsystem.dto.LoginResponse;
import com.pbl.parkingsystem.dto.UserRegistrationRequest;
import com.pbl.parkingsystem.dto.UserResponse;
import com.pbl.parkingsystem.dto.PaymentRequest;
import com.pbl.parkingsystem.dto.PaymentResponse;
import com.pbl.parkingsystem.dto.PaymentAnalyticsResponse;
import com.pbl.parkingsystem.dto.RazorpayOrderResponse;
import com.pbl.parkingsystem.dto.RazorpayVerifyRequest;

import com.pbl.parkingsystem.model.ParkingLot;
import com.pbl.parkingsystem.model.ParkingSlot;
import com.pbl.parkingsystem.model.Vehicle;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("api/users/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/users/register")
    Call<UserResponse> registerUser(
            @Body UserRegistrationRequest request
    );

    @GET("api/vehicles/user/{userId}")
    Call<List<Vehicle>> getUserVehicles(
            @Path("userId") Long userId
    );

    @POST("api/vehicles")
    Call<Vehicle> addVehicle(
            @Query("userId") Long userId,
            @Query("vehicleNumber") String vehicleNumber,
            @Query("vehicleType") String vehicleType
    );

    @DELETE("api/vehicles/{id}")
    Call<ResponseBody> deleteVehicle(
            @Path("id") Long vehicleId
    );

    @GET("api/parking-lots")
    Call<List<ParkingLot>> getAllParkingLots();

    @POST("api/parking-lots")
    Call<ParkingLot> createParkingLot(
            @Body ParkingLot parkingLot
    );

    @GET("api/parking-slots/parking-lot/{parkingLotId}")
    Call<List<ParkingSlot>> getSlotsByParkingLot(
            @Path("parkingLotId") Long parkingLotId
    );

    @PUT("api/parking-slots/{id}/status")
    Call<ParkingSlot> updateSlotStatus(
            @Path("id") Long id,
            @Query("status") String status
    );

    @DELETE("api/parking-slots/{id}")
    Call<ResponseBody> deleteParkingSlot(
            @Path("id") Long id
    );

    @DELETE("api/parking-lots/{id}")
    Call<ResponseBody> deleteParkingLot(
            @Path("id") Long parkingLotId
    );

    // Create booking
    @FormUrlEncoded
    @POST("api/bookings")
    Call<BookingResponse> createBooking(
            @Field("userId") Long userId,
            @Field("vehicleId") Long vehicleId,
            @Field("parkingSlotId") Long parkingSlotId,
            @Field("startTime") String startTime,
            @Field("endTime") String endTime
    );

    @GET("api/bookings/user/{userId}")
    Call<List<BookingResponse>> getUserBookings(
            @Path("userId") Long userId
    );

    @PUT("api/bookings/{bookingId}/cancel")
    Call<BookingResponse> cancelBooking(
            @Path("bookingId") Long bookingId,
            @Query("userId") Long userId
    );

    // Create Razorpay order
    @POST("api/payments/razorpay/create-order")
    Call<RazorpayOrderResponse> createRazorpayOrder(
            @Body PaymentRequest request
    );

    // Verify Razorpay payment
    @POST("api/payments/razorpay/verify")
    Call<PaymentResponse> verifyRazorpayPayment(
            @Body RazorpayVerifyRequest request
    );

    // Admin payment analytics
    @GET("api/admin/payments/summary")
    Call<PaymentAnalyticsResponse> getPaymentAnalytics();

    @POST("api/users/logout")
    Call<Map<String, String>> logout();
}
