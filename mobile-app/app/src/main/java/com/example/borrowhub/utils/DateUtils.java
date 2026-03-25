package com.example.borrowhub.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {

    /**
     * Formats an ISO 8601 date string from the backend into a user-friendly Philippine time format.
     * Handle variations like milliseconds or 'Z' suffix.
     *
     * @param isoDate The date string from the backend (e.g., "2026-03-24T16:56:22.000000Z")
     * @return Formatted date string (e.g., "Mar 25, 2026, 12:56AM") or the original string if parsing fails.
     */
    public static String formatBackendDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) {
            return "";
        }

        String formattedDate = isoDate;
        try {
            // ISO 8601 parser with microseconds (common in Laravel)
            SimpleDateFormat isoParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US);
            isoParser.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date date = isoParser.parse(isoDate);
            if (date != null) {
                formattedDate = formatToPHTime(date);
            }
        } catch (Exception e) {
            // Fallback for short ISO variation (without microseconds)
            try {
                SimpleDateFormat isoParserShort = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                isoParserShort.setTimeZone(TimeZone.getTimeZone("UTC"));

                Date date = isoParserShort.parse(isoDate);
                if (date != null) {
                    formattedDate = formatToPHTime(date);
                }
            } catch (Exception ignored) {
                // If all fails, return the raw date or handle other formats if needed
            }
        }
        return formattedDate;
    }

    private static String formatToPHTime(Date date) {
        // Output format: Mar 25, 2026, 12:56AM
        SimpleDateFormat phFormatter = new SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.US);
        phFormatter.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));
        return phFormatter.format(date);
    }
}
