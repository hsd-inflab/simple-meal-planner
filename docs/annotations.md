# Annotations im Projekt

Diese Datei erklaert alle Annotationen, die im aktuellen Codebestand verwendet werden, kurz und projektbezogen.

## Spring Web und Spring Boot

- `@SpringBootApplication`
  Markiert die Hauptklasse einer Spring-Boot-Anwendung. Sie buendelt u. a. Konfiguration, Component-Scan und Auto-Configuration.
- `@RestController`
  Kennzeichnet eine Klasse als REST-Controller. Rueckgabewerte werden direkt als HTTP-Response serialisiert, typischerweise als JSON.
- `@RequestMapping`
  Definiert ein gemeinsames URL-Praefix oder allgemeine Routing-Regeln fuer einen Controller.
- `@GetMapping`
  Mappt HTTP-GET-Anfragen auf eine Methode.
- `@PostMapping`
  Mappt HTTP-POST-Anfragen auf eine Methode.
- `@PathVariable`
  Liest einen Wert aus der URL, z. B. `/mealplans/{date}`.
- `@RequestParam`
  Liest Query-Parameter aus der URL, z. B. `?start=2026-04-20`.
- `@RequestBody`
  Bindet den HTTP-Request-Body an ein Java-Objekt, hier typischerweise an DTOs.
- `@DateTimeFormat`
  Steuert das Parsen und Formatieren von Datumswerten. Im Projekt wird damit `LocalDate` im ISO-Format wie `2026-04-27` verarbeitet.
- `@Service`
  Markiert eine Klasse als Service-Komponente fuer Geschaeftslogik.
- `@Repository`
  Markiert eine Klasse oder ein Interface als Datenzugriffsschicht. Bei Spring Data dient das zusaetzlich der Exception-Uebersetzung.
- `@Component`
  Allgemeine Spring-Bean ohne speziellere Rolle. Wird fuer Hilfs- oder Infrastrukturklassen genutzt.
- `@Value`
  Liest einen Konfigurationswert aus Properties, Umgebungsvariablen oder anderen Spring-Property-Quellen.
- `@Autowired`
  Veranlasst Spring, eine Abhaengigkeit automatisch zu injizieren. Im Projekt kommt das in Tests vor.
- `@Transactional`
  Fuehrt Methoden oder Klassen innerhalb einer Datenbanktransaktion aus. Entweder werden alle Aenderungen erfolgreich gespeichert oder bei Fehlern zurueckgerollt.

## JPA und Persistenz

- `@Entity`
  Markiert eine Klasse als JPA-Entity, also als persistierbares Datenbankobjekt.
- `@Table`
  Legt den Namen der zugehoerigen Datenbanktabelle fest.
- `@Id`
  Kennzeichnet das Primaerschluessel-Feld einer Entity.
- `@GeneratedValue`
  Legt fest, dass der Primaerschluessel automatisch erzeugt wird.
- `@Column`
  Konfiguriert Details einer Spalte, z. B. Name, `nullable` oder `unique`.
- `@ManyToOne`
  Beschreibt eine Viele-zu-Eins-Beziehung zwischen Entities.
- `@OneToMany`
  Beschreibt eine Eins-zu-Viele-Beziehung zwischen Entities.
- `@JoinColumn`
  Bestimmt die Fremdschluesselspalte einer Relation.
- `@MappedSuperclass`
  Kennzeichnet eine Basisklasse, deren Felder von Entities geerbt werden, ohne selbst eine eigene Tabelle zu sein.
- `@EntityGraph`
  Definiert, welche Beziehungen bei einem Datenbankzugriff gezielt mitgeladen werden sollen, um z. B. Lazy-Loading-Probleme oder unnoetige Queries zu vermeiden.

## Test-Annotationen

- `@Test`
  Markiert eine JUnit-Testmethode.
- `@BeforeEach`
  Diese Methode wird vor jedem Testlauf ausgefuehrt, typischerweise zum Initialisieren von Mocks oder Testdaten.
- `@ParameterizedTest`
  Fuehrt dieselbe Testmethode mehrfach mit verschiedenen Eingabewerten aus.
- `@MethodSource`
  Liefert Daten fuer einen `@ParameterizedTest` aus einer Java-Methode.
- `@Disabled`
  Deaktiviert einen Test oder eine Testklasse temporaer.
- `@SpringBootTest`
  Startet fuer Tests den Spring-Kontext und eignet sich fuer Integrations- oder Anwendungstests.
- `@AnalyzeClasses`
  ArchUnit-Annotation, die festlegt, welche Klassen fuer Architekturtests analysiert werden.
- `@ArchTest`
  Markiert eine ArchUnit-Regel oder einen ArchUnit-Test.

## Java-Standardannotationen

- `@Override`
  Kennzeichnet, dass eine Methode eine Methode aus Oberklasse oder Interface ueberschreibt. Hilft dem Compiler, Fehler frueh zu erkennen.
- `@Deprecated`
  Markiert Code als veraltet. Neue Nutzung sollte vermieden und spaeter ersetzt werden.
- `@SuppressWarnings`
  Unterdrueckt Compiler-Warnungen fuer eine bestimmte Stelle. Sollte gezielt und sparsam eingesetzt werden.

## Hinweise zur Verwendung im Projekt

- Controller-Annotationen definieren nur das REST-Mapping. Fachliche Entscheidungen sollten im Service liegen.
- JPA-Annotationen beschreiben das relationale Datenmodell und dessen Beziehungen.
- Test-Annotationen strukturieren Unit-, Integrations- und Architekturtests.
