package hsd.inflab.smp.entity;

import hsd.inflab.smp.enums.Category;
import hsd.inflab.smp.enums.Unit;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = "name")
@MappedSuperclass // Diese Klasse erzeugt keine eigene Tabelle in der DB -> keine Flyway-Tabelle
public class Ingredient {

    protected String name;
    protected Unit unit; // liter, grams, tablespoons etc.
    protected Double amount;
    protected Category category; // meat, vegetable, spice
}
