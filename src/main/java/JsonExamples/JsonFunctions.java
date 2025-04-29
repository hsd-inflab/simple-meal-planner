package JsonExamples;

public class JsonFunctions {
    private String name;
    private int alter;

    // Standardkonstruktor (für Jackson erforderlich)
    public JsonFunctions() {
    }

    // Konstruktor mit Parametern
    public JsonFunctions(String name, int alter) {
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

