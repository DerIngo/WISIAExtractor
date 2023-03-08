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

## Ablauf


~~~mermaid
flowchart TD
    A[Seite herunterladen] -->|speichern| B
    B[Informationen extrahieren] -->|speichern| C
    C[Daten validieren] --> D
    D[Daten verarbeiten] --> E
    E[Daten exportieren] --> F
    G[serialisiertes Objekt]
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