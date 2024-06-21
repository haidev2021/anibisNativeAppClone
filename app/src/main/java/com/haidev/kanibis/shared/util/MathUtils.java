package com.haidev.kanibis.shared.util;


public class MathUtils {

    public static double roundToHalf(double d) {
        return Math.round(d * 2) / 2.0;
    }

    public static double roundToSecondDecimalHalf(double d) {
        return Math.round(d * 20) / 20.0;
    }

    public static long minutesToMillis(int minutes) {
        return minutes * 60 * 1000;
    }

    public static long hoursToMillis(int hours) {
        return hours * 60 * 60 * 1000;
    }

    public static long daysToMillis(int days) {
        return days * 24 * 60 * 60 * 1000;
    }

    public static boolean hasDifference(Double d1, Double d2) {
        if (d1 == null && d2 == null)
            return false;

        return d1 == null || d2 == null || Math.abs(d1 - d2) > 0.01f;
    }

}
