package deringo.wisia;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import deringo.wisia.art.Art;
import deringo.wisia.art.ArtService;
import deringo.wisia.exporter.EinObjektExporter;
import deringo.wisia.fussnote.Fussnote;
import deringo.wisia.fussnote.FussnoteService;
import deringo.wisia.taxon.TaxonInformation;
import deringo.wisia.taxon.TaxonInformationChecker;
import deringo.wisia.taxon.TaxonInformationService;
import deringo.wisia.taxon.TaxonPageService;

public class WisiaExtraktor {
    public static final int maxKnotenId = 54999;//54999;
    
    /**
     * Liefert alle Fussnoten, die auf <a href="https://www.wisia.de/ShowFNDef">www.wisia.de/ShowFNDef</a> aufgelistet sind.
     */
    public static List<Fussnote> getFussnoten() {
        return FussnoteService.getFussnoten();
    }
    
    /**
     * Läd die Seiten zu den Knoten IDs 0 bis maxKnotenId herunter.
     */
    public static void downloadTaxonPages() {
        for (int i= 0; i< maxKnotenId; i++) {
            if (i%100 == 0) {
                System.out.println("Start download " + i + " to " + (i+99));
            }
            TaxonPageService.getPage(i);
        }
    }

    /**
     * Verarbeitet die Seiten zu den Knoten IDs 0 bis maxKnotenId
     */
    public static void extractTaxonInformation() {
        for (int i= 0; i<maxKnotenId; i++) {
            if (i%1000 == 0) {
                System.out.println("Start extract " + i + " to " + (i+999));
            }
            TaxonInformationService.getTaxonInformation(i);
        }
    }
    
    /**
     * Prüft die extrahierten TaxonInformationen
     */
    public static void check() {
        System.out.println("START check");
        TaxonInformationChecker.check();
        System.out.println("END check");
    }
    
    /**
     * Transformiert TaxonInformation zu Art zu den Knoten IDs 0 bis maxKnotenId
     */
    public static void transformTaxonInformation() {
        for (int i= 0; i<maxKnotenId; i++) {
            if (i%1000 == 0) {
                System.out.println("Start transform " + i + " to " + (i+999));
            }
            ArtService.getArt(i);
        }
    }
    
    
    /**
     * Liefert alle Informationen zu einer KnotenId, die auf <a href="https://www.wisia.de/GetTaxInfo">www.wisia.de/GetTaxInfo</a> angezeigt werden.
     */
    public static TaxonInformation getTaxonInformation(int knotenId) {
        return TaxonInformationService.getTaxonInformation(knotenId);
    }
    
    public static List<TaxonInformation> getAllTaxonInformation() {
        List<TaxonInformation> alleTaxonInformation = new ArrayList<>();
        for (int i= 0; i<maxKnotenId; i++) {
            TaxonInformation information = TaxonInformationService.getTaxonInformation(i);
            alleTaxonInformation.add(information);
        }
        return alleTaxonInformation;
    }
    
    public static List<TaxonInformation> getAllTaxonInformationWithoutEmpty() {
        List<TaxonInformation> alleTaxonInformation = getAllTaxonInformation();
        alleTaxonInformation.removeIf(information -> TaxonInformationService.isEmpty(information));
        return alleTaxonInformation;
    }
    
    public static List<Art> getAllArt() {
        List<Art> alleArten = new ArrayList<>();
        for (int i= 0; i<maxKnotenId; i++) {
            Art art = ArtService.getArt(i);
            if (art != null) {
                alleArten.add(art);
            }
        }
        return alleArten;
    }
    
    public static void exportEinObject() {
        long start = System.currentTimeMillis();
        List<Art> alleArten = WisiaExtraktor.getAllArt();
        long end = System.currentTimeMillis();
        long durationInSec = (end - start) / 1000;
        String message = String.format("Alle %d Arten geladen in %d Sekunden.", alleArten.size(), durationInSec);
        System.out.println(message);
        
        start = System.currentTimeMillis();
        EinObjektExporter.export(alleArten);
        end = System.currentTimeMillis();
        durationInSec = (end - start) / 1000;
        message = String.format("Alle %d Arten exportiert in %d Sekunden.", alleArten.size(), durationInSec);
        System.out.println(message);
    }
    
    public static List<Art> importEinObject() {
        long start = System.currentTimeMillis();
        List<Art> alleArten = EinObjektExporter.importArten();
        long end = System.currentTimeMillis();
        long durationInSec = (end - start) / 1000;
        String message = String.format("Alle %d Arten importiert in %d Sekunden.", alleArten.size(), durationInSec);
        System.out.println(message);
        return alleArten;
    }
    
    public static List<Art> importEinObject(String filename) {
        long start = System.currentTimeMillis();
        List<Art> alleArten = EinObjektExporter.importArten(filename);
        long end = System.currentTimeMillis();
        long durationInSec = (end - start) / 1000;
        String message = String.format("Alle %d Arten importiert in %d Sekunden.", alleArten.size(), durationInSec);
        System.out.println(message);
        return alleArten;
    }
    
    public static List<Art> importEinObjectFromResource() {
        InputStream is = WisiaExtraktor.class.getClassLoader().getResourceAsStream("alleArten.obj");
        List<Art> alleArten = EinObjektExporter.importArten(is);
        return alleArten;
    }
        
}
