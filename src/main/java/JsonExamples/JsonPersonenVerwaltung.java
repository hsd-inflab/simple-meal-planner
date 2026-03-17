package JsonExamples;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class JsonPersonenVerwaltung {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Methode zum Einlesen der Personen aus einer JSON-Datei
    public List<JsonFunctions> readPersonsFromJson(String dateipfad) throws IOException {
        return objectMapper.readValue(
                new File(dateipfad),
                objectMapper.getTypeFactory().constructCollectionType(List.class, JsonFunctions.class));
    }

    // Methode zum Speichern einer Liste von Personen in eine JSON-Datei
    public void savePersonsToJson(List<JsonFunctions> personen, String dateipfad) throws IOException {
        objectMapper.writeValue(new File(dateipfad), personen);
    }

    // Beispielmethode zum Erzeugen einer Beispiel-Liste von Personen (optional)
    public List<JsonFunctions> createBeispielPersonen() {
        return Arrays.asList(
                new JsonFunctions("Max", 25), new JsonFunctions("Anna", 30), new JsonFunctions("John", 28));
    }
}
