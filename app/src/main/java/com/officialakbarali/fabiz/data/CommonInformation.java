package com.officialakbarali.fabiz.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class CommonInformation {
    private static int PHONE_NUMBER_LENGTH = 6;
    private static int DECIMAL_LENGTH = 3;
    private static String REAL_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static String REAL_DATE_FORMAT_OF_SEARCH = "yyyy-MM-dd";


    private static String CURRENCY = "BD";


    public static void setCurrency(String currency) {
        CURRENCY = currency;
    }

    public static String getCurrency() {
        return CURRENCY;
    }

    public static String convertDateToDisplayFormat(String dateString) throws ParseException {
        DateFormat sdf = new SimpleDateFormat(REAL_DATE_FORMAT);
        Date date = sdf.parse(dateString);
        return new SimpleDateFormat("dd,E MMM YYYY hh:mm a").format(date);
    }

    public static String convertDateToSearchFormat(String dateString) throws ParseException {
        DateFormat sdf = new SimpleDateFormat(REAL_DATE_FORMAT_OF_SEARCH);
        Date date = sdf.parse(dateString);
        return new SimpleDateFormat("dd,E MMM YYYY").format(date);
    }

    public static String GET_DATE_FORMAT_REAL() {
        return REAL_DATE_FORMAT;
    }

    public static String TruncateDecimal(String value) {
        BigDecimal truncateValue = new BigDecimal(value)
                .setScale(DECIMAL_LENGTH, RoundingMode.DOWN)
                .stripTrailingZeros();
        DecimalFormat df;
        if (DECIMAL_LENGTH == 2) {
            df = new DecimalFormat("#0.00");
        } else {
            df = new DecimalFormat("#0.000");
        }
        return df.format(truncateValue);
    }

    public static int GET_PHONE_NUMBER_LENGTH() {
        return PHONE_NUMBER_LENGTH;
    }

    public static int GET_DECIMAL_LENGTH() {
        return DECIMAL_LENGTH;
    }

    public static void SET_PHONE_NUMBER_LENGTH(int UP_PHONE_NUMBER_LENGTH) {
        PHONE_NUMBER_LENGTH = UP_PHONE_NUMBER_LENGTH;
    }

    public static void SET_DECIMAL_LENGTH(int UP_DECIMAL_LENGTH) {
        DECIMAL_LENGTH = UP_DECIMAL_LENGTH;
    }

    public static String getDayNameFromNumber(String dayInNumber) {
        String returnString = "";

        switch (Integer.parseInt(dayInNumber)) {
            case Calendar.MONDAY:
                returnString = "MONDAY";
                break;
            case Calendar.TUESDAY:
                returnString = "TUESDAY";
                break;
            case Calendar.WEDNESDAY:
                returnString = "WEDNESDAY";
                break;
            case Calendar.THURSDAY:
                returnString = "THURSDAY";
                break;
            case Calendar.FRIDAY:
                returnString = "FRIDAY";
                break;
            case Calendar.SATURDAY:
                returnString = "SATURDAY";
                break;
            case Calendar.SUNDAY:
                returnString = "SUNDAY";
                break;
        }

        return returnString;
    }

    public static int getNumberFromDayName(String dayPassed) {
        int returnDay = 1;

        switch (dayPassed) {
            case "MONDAY":
                returnDay = Calendar.MONDAY;
                break;
            case "TUESDAY":
                returnDay = Calendar.TUESDAY;
                break;
            case "WEDNESDAY":
                returnDay = Calendar.WEDNESDAY;
                break;
            case "THURSDAY":
                returnDay = Calendar.THURSDAY;
                break;
            case "FRIDAY":
                returnDay = Calendar.FRIDAY;
                break;
            case "SATURDAY":
                returnDay = Calendar.SATURDAY;
                break;
            case "SUNDAY":
                returnDay = Calendar.SUNDAY;
                break;
        }

        return returnDay;
    }

    public static String convertToCamelCase(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder converted = new StringBuilder();

        boolean convertNext = true;
        for (char ch : text.toCharArray()) {
            if (Character.isSpaceChar(ch)) {
                convertNext = true;
            } else if (convertNext) {
                ch = Character.toTitleCase(ch);
                convertNext = false;
            } else {
                ch = Character.toLowerCase(ch);
            }
            converted.append(ch);
        }
        return converted.toString();
    }
}


