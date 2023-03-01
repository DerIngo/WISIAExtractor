package deringo.wisia;

import java.util.List;

import deringo.wisia.fussnote.Fussnote;
import deringo.wisia.fussnote.FussnoteService;
import deringo.wisia.taxon.TaxonInformation;
import deringo.wisia.taxon.TaxonInformationService;
import deringo.wisia.taxon.TaxonPageService;

public class WisiaExtraktor {

    /**
     * Liefert alle Fussnoten, die auf <a href="https://www.wisia.de/ShowFNDef">www.wisia.de/ShowFNDef</a> aufgelistet sind.
     */
    public static List<Fussnote> getFussnoten() {
        return FussnoteService.getFussnoten();
    }
    
    /**
     * Liefert alle Informationen zu einer KnotenId, die auf <a href="https://www.wisia.de/GetTaxInfo">www.wisia.de/GetTaxInfo</a> angezeigt werden.
     */
    public static TaxonInformation getTaxonInformation(int knotenId) {
        return TaxonInformationService.getTaxonInformation(knotenId);
    }
    
    /**
     * Läd die Seiten zu den Knoten IDs 0 bis 54999 herunter.
     */
    public static void downloadTaxonPages() {
        for (int i= 0; i< 54999; i++) {
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
        for (int i= 0; i<54999; i++) {
            if (i%1000 == 0) {
                System.out.println("Start extract " + i + " to " + (i+999));
            }
            TaxonInformationService.getTaxonInformation(i);
        }
    }
}
