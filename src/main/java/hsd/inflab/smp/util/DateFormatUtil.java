package hsd.inflab.smp.util;

import java.time.LocalDate;

public class DateFormatUtil {

    private static final int STRING_STANDARD_LENGTH = 3; // NOPMD

    public static String formatAsGermanDate(LocalDate date) {
        if (date == null) {
            return "N/A";
        }
        return String.format("%02d.%02d.%d", date.getDayOfMonth(), date.getMonthValue(), date.getYear());
    }
}
