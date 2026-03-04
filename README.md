# WISIAExtractor
Extrahiert die Daten der WISIA Webseite

## Hintergrund
Ich benötige die Daten der Artenschutzdatenbank des <a href="http://www.bfn.de/">Bundesamt für Naturschutz</a> in Bonn.<br/>
Deren <a href="https://www.wisia.de/">Wissenschaftliches Informationssystem zum Internationalen Artenschutz</a> liefert diese Informationen über eine Webseite. Weder API noch Datenbankkopie sind verfügbar.<br/>
Daher musste ich die Seite jeder einzelnen Art aufrufen, die Informationen extrahieren und verarbeiten.<br/>

## Diese Projekt
Mit diesem Projekt speichere ich den Weg, wie ich meine Kopie der Artenschutzdatenbank aufgebaut habe. So kann jederzeit eine neue Version der Artenschutzdatenbank erstellt werden, beispielsweise nach einem Update der Original Datenbank.<br/>
Dieses Projekt ist weder universell, flexibel, konfigurierbar oder sonst was, sondern dient nur diesem einen Zweck.<br/>
Grade der Teil mit der Datenextraktion ist "Learn as you code" entstanden und sollte nicht unbedingt als Vorbild für andere Projekte dienen.<br/>

## Verwendung
In der Klasse WisiaExtraktor sind die verwendbaren Aufrufe zu finden.

## REST API (Read-only)
Der REST-Service liest Arten aus `alleArten.obj`.

Starten:

~~~bash
mvn spring-boot:run
~~~

Optional mit anderem Datenpfad:

~~~bash
mvn spring-boot:run -Dspring-boot.run.arguments=--wisia.data.file=/pfad/zu/alleArten.obj
~~~

### Endpunkte
- `GET /api/v1/arten/{knotenId}`
- `GET /api/v1/arten?name=&gruppe=&regelwerk=&anhang=&limit=&offset=`

Beispiele:

~~~bash
curl "http://localhost:8080/api/v1/arten/1234"
curl "http://localhost:8080/api/v1/arten?name=testudo&limit=20&offset=0"
~~~

### Swagger / OpenAPI
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

### Fehlerformat
Fehlerantworten sind einheitlich als JSON aufgebaut:

~~~json
{
  "timestamp": "2026-03-04T12:34:56.789Z",
  "status": 404,
  "error": "Not Found",
  "message": "Art mit knotenId 999999 nicht gefunden.",
  "path": "/api/v1/arten/999999"
}
~~~

## Ablauf


~~~mermaid
flowchart TD
    A[Seite herunterladen] -->|speichern| B
    B[Informationen extrahieren] -->|speichern| C
    C[Daten validieren] --> D
    D[Daten verarbeiten] --> E
    E[Daten exportieren] --> F
    F[serialisiertes Objekt]
~~~

## In anderen Projekten verwenden
Serialisiertes Java Objekt generieren lassen:

~~~Java
WisiaExtraktor.exportEinObject();
~~~

Die generierte Datei alleArten.obj nach src/main/resources kopieren.<br/>
Maven mit package laufen lassen und die so erzeugte WISIAExtractor-0.0.1-SNAPSHOT.jar Datei in das Ziel-Projekt einbinden.<BR/
Im Ziel-Projekt verwenden mit:

~~~Java
List<Art> alleArten = WisiaExtraktor.importEinObjectFromResource();
~~~
