package models;

import models.Category;
import models.Unit;

import java.awt.*;
import java.time.LocalDate;

public abstract class Ingredient {

    protected final String name;
    protected Unit unit;          //liter, grams, tablespoons etc.
    protected Double amount;
    protected Category category;      //meat, vegetable, spice

    //default constructor for Jackson, needed for deserialization: PantryItem Default Constructor
    public Ingredient() {
        this.name = null;
        this.unit = null;
        this.amount = null;
        this.category = null;
    }

    //only use for recipe ingredient
    public Ingredient (String name) {
        this.name = name;
    }
    public Ingredient(String name, Unit unit, Double amount, Category category) {
        this.name = name;
        this.unit = unit;
        this.amount = amount;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public Unit getUnit() {
        return unit;
    }

    protected void setUnit(Unit unit) {
        this.unit = unit;
    }

    public Double getAmount() {
        return amount;
    }

    protected void setAmount(Double amount) {
        this.amount = amount;
    }

    public Category getCategory() {
        return category;
    }

    public String formatAsGermanDate(LocalDate date) {
        if (date == null) return "N/A";
        return String.format("%02d.%02d.%d", date.getDayOfMonth(), date.getMonthValue(), date.getYear());
    }

    public LocalDate parseGermanDate(String dateStr) {
        String[] parts = dateStr.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid date format. Expected format: dd.MM.yyyy");
        }
        int day = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int year = Integer.parseInt(parts[2]);
        return LocalDate.of(year, month, day);
    }

    public TextField parseGermanDate(TextField dateField) {
        String dateStr = dateField.getText();
        String[] parts = dateStr.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid date format. Expected format: dd.MM.yyyy");
        }
        int day = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int year = Integer.parseInt(parts[2]);
        LocalDate date = LocalDate.of(year, month, day);
        dateField.setText(formatAsGermanDate(date));
        return dateField;
    }

    @Override
    public String toString() {
        return name;
    }
}
