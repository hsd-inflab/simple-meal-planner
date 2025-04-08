package hsd.inflab.smp;

public class Person {
    private String name;
    private int alter;

    // Standardkonstruktor (für Jackson erforderlich)
    public Person() {
    }

    // Konstruktor mit Parametern
    public Person(String name, int alter) {
        this.name = name;
        this.alter = alter;
    }

    // Getter und Setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAlter() {
        return alter;
    }

    public void setAlter(int alter) {
        this.alter = alter;
    }
}

