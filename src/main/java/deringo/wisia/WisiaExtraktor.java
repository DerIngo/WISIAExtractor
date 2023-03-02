package deringo.wisia;

import java.util.ArrayList;
import java.util.List;

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
     * Läd die Seiten zu den Knoten IDs 0 bis 54999 herunter.
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
     * Verarbeitet die Seiten zu den Knoten IDs 0 bis 54999
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
}
