
package com.pbl.parkingsystem.dto;

import com.google.gson.annotations.SerializedName;

public class PaymentAnalyticsResponse {

    @SerializedName("todayPayments")
    private long todayPayments;

    @SerializedName("todayRevenue")
    private double todayRevenue;

    @SerializedName("yesterdayPayments")
    private long yesterdayPayments;

    @SerializedName("yesterdayRevenue")
    private double yesterdayRevenue;

    @SerializedName("thisMonthPayments")
    private long thisMonthPayments;

    @SerializedName("thisMonthRevenue")
    private double thisMonthRevenue;

    @SerializedName("totalPayments")
    private long totalPayments;

    @SerializedName("totalRevenue")
    private double totalRevenue;

    public long getTodayPayments() {
        return todayPayments;
    }

    public double getTodayRevenue() {
        return todayRevenue;
    }

    public long getYesterdayPayments() {
        return yesterdayPayments;
    }

    public double getYesterdayRevenue() {
        return yesterdayRevenue;
    }

    public long getThisMonthPayments() {
        return thisMonthPayments;
    }

    public double getThisMonthRevenue() {
        return thisMonthRevenue;
    }

    public long getTotalPayments() {
        return totalPayments;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }
}
