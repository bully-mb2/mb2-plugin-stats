package com.templars_server.model;

import java.text.NumberFormat;
import java.util.Locale;

public class Display {

    public static final String MERGE = "^1! MERGE ! ^7";
    public static final String BALANCE = "^9Balance »^7 ";
    public static final String PREFIX = "^2Usage »^7 ";
    private static final NumberFormat NUMBER_FORMATTER = NumberFormat.getInstance(Locale.GERMAN);


    public static String renderBalance(int amount) {
        if (amount >= 0) {
            if (amount >= 1000000) {
                return "^2" + NUMBER_FORMATTER.format(amount / 1000) + "k^7";
            }
            return "^3" + NUMBER_FORMATTER.format(amount) + " CR^7";
        } else {
            return "^1" + NUMBER_FORMATTER.format(amount) + " CR^7";
        }
    }

}
