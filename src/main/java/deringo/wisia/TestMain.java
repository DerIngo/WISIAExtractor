package deringo.wisia;

import java.util.List;

import deringo.wisia.art.Art;
import deringo.wisia.exporter.EinObjektExporter;
import deringo.wisia.taxon.TaxonInformation;

public class TestMain {

    public static void main(String[] args) {
        List<Art> alleArten = EinObjektExporter.importArten();
        System.out.println(alleArten.size());
    }
    
    public static void einzelTest() {
        int leereSeite = 1; // Beispiel für leere Seite
        int testudoHermanni = 19442;
        int chersinaAngulata = 2510;
        int selenicereusTestudo = 30177; // Beispiel mit Fußnoten
        
        TaxonInformation taxonInformation;
        taxonInformation = WisiaExtraktor.getTaxonInformation(leereSeite);
        System.out.println(taxonInformation);
        
        taxonInformation = WisiaExtraktor.getTaxonInformation(testudoHermanni);
        System.out.println(taxonInformation);
        
        taxonInformation = WisiaExtraktor.getTaxonInformation(chersinaAngulata);
        System.out.println(taxonInformation);
        
        taxonInformation = WisiaExtraktor.getTaxonInformation(selenicereusTestudo);
        System.out.println(taxonInformation);
    }

}
