package hsd.inflab.smp;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class getJson {
    public static void main(String[] args) throws IOException {
        // Jackson ObjectMapper zum Einlesen der JSON-Datei
        ObjectMapper objectMapper = new ObjectMapper();

        // Lesen der JSON-Datei und Umwandeln in eine Liste von Person-Objekten
        List<Person> personen = objectMapper.readValue(new File("personen.json"), objectMapper.getTypeFactory().constructCollectionType(List.class, Person.class));

        // Ausgabe der geladenen Personen
        for (Person person : personen) {
            System.out.println("Name: " + person.getName() + ", Alter: " + person.getAlter());
        }
    }
}
