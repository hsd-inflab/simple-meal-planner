package hsd.inflab.smp.util;

import java.time.LocalDate;
import javafx.scene.control.TextField;

public class DateFormatUtil {

    private static final int STRING_STANDARD_LENGTH = 3; // NOPMD

    public static String formatAsGermanDate(LocalDate date) {
        if (date == null) {
            return "N/A";
        }
        return String.format("%02d.%02d.%d", date.getDayOfMonth(), date.getMonthValue(), date.getYear());
    }

    public static LocalDate parseGermanDate(String dateStr) {
        String[] parts = dateStr.split("\\.");
        if (parts.length != STRING_STANDARD_LENGTH) {
            throw new IllegalArgumentException("Invalid date format. Expected format: dd.MM.yyyy");
        }
        int day = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int year = Integer.parseInt(parts[2]);
        return LocalDate.of(year, month, day);
    }

    public static TextField parseGermanDate(TextField dateField) {
        String dateStr = dateField.getText();
        String[] parts = dateStr.split("\\.");
        if (parts.length != STRING_STANDARD_LENGTH) {
            throw new IllegalArgumentException("Invalid date format. Expected format: dd.MM.yyyy");
        }
        int day = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int year = Integer.parseInt(parts[2]);
        LocalDate date = LocalDate.of(year, month, day);
        dateField.setText(formatAsGermanDate(date));
        return dateField;
    }
}
