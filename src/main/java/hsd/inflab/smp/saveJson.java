package hsd.inflab.smp;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class saveJson {
    public static void main(String[] args) throws IOException {
        // Erstellen einer Liste von Personen
        List<Person> personen = Arrays.asList(
                new Person("Max", 25),
                new Person("Anna", 30),
                new Person("John", 28)
        );

        // Erstellen des Jackson ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();

        // Speichern der Personen als JSON-Datei
        objectMapper.writeValue(new File("personen.json"), personen);

        System.out.println("Die Personen wurden erfolgreich in der JSON-Datei gespeichert.");
    }
}

