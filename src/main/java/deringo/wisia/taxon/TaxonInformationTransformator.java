package deringo.wisia.taxon;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;

import deringo.wisia.art.Anhang;
import deringo.wisia.art.Art;
import deringo.wisia.art.Fussnote;
import deringo.wisia.art.Regelwerk;
import deringo.wisia.art.Unterschutzstellung;
import deringo.wisia.taxon.TaxonInformation.DetaillierteSchutzdaten;
import deringo.wisia.taxon.TaxonInformation.LandesprName;
import deringo.wisia.taxon.TaxonInformation.Schutz;

public class TaxonInformationTransformator {

    public static void main(String[] args) {
        int leereSeite = 1; // Beispiel für leere Seite
        int testudoHermanni = 19442;
        int chersinaAngulata = 2510;
        int selenicereusTestudo = 30177; // Beispiel mit Fußnoten
        

        Art art;
        art = TaxonInformationTransformator.transform(leereSeite);
        System.out.println(art);
        art = TaxonInformationTransformator.transform(testudoHermanni);
        System.out.println(art);
        art = TaxonInformationTransformator.transform(chersinaAngulata);
        System.out.println(art);
        art = TaxonInformationTransformator.transform(selenicereusTestudo);
        System.out.println(art);
    }
    
    public static Art transform(int knotenId) {
        TaxonInformation taxonInformation = TaxonInformationService.getTaxonInformation(knotenId);
        return transform(taxonInformation);
    }
    
    public static Art transform(TaxonInformation taxonInformation) {
        if (TaxonInformationService.isEmpty(taxonInformation)) {
            return null;
        }
        Art art = new Art();
        art.setKnoten_id(taxonInformation.knotenId);
        art.setWissenschaftlicherName(taxonInformation.gueltigerName.gueltigerName1());
        art.setGueltigerName2(StringUtils.trim(taxonInformation.gueltigerName.gueltigerName2()));
        art.setGruppe(taxonInformation.gruppe);
        art.setTaxonomie(taxonInformation.taxonomie);
        for (LandesprName ln : taxonInformation.landesprNamen) {
            switch (ln.land()) {
            case "germany":
                art.setDeutscherName(ln.landesprName());
                break;
            case "uk":
                art.setEnglischerName(ln.landesprName());
                break;
            default:
                System.err.println("unbekanntes Land : " + ln.land() + " (knotenId: "+taxonInformation.knotenId+")");
                break;
            }
        }
        
        for (Schutz schutz : taxonInformation.schutzListe) {
            String regelwerkName = schutz.regelwerk1();
            String anhangName = schutz.regelwerk2();
            
            Regelwerk regelwerk = null;
            for (Regelwerk rw : art.getRegelwerke()) {
                if (StringUtils.equals(regelwerkName, rw.getName())) {
                    regelwerk = rw;
                }
            }
            if (regelwerk == null) {
                regelwerk = new Regelwerk();
                regelwerk.setName(regelwerkName);
                art.getRegelwerke().add(regelwerk);
            }
            regelwerk.getNamenImRegelwerk().add(schutz.nameImRegelwerk());
            
            Anhang anhang = null;
            for (Anhang a : regelwerk.getAnhaenge()) {
                if (StringUtils.equals(anhangName, a.getName())) {
                    anhang = a;
                }
            }
            if (anhang == null) {
                anhang = new Anhang();
                anhang.setName(anhangName);
                regelwerk.getAnhaenge().add(anhang);
            }
            
            if (!StringUtils.isBlank(schutz.fussnote1())) {
                Fussnote fussnote = new Fussnote();
                fussnote.setId(schutz.fussnote1());
                fussnote.setText(schutz.fussnote2());
                anhang.getFussnoten().add(fussnote);
            }
        }
        
        if (taxonInformation.detaillierteSchutzdatenListe != null) {
            for (DetaillierteSchutzdaten schutz : taxonInformation.detaillierteSchutzdatenListe) {
                Unterschutzstellung unterschutz = new Unterschutzstellung();
                unterschutz.setUnterschutzstellung(schutz.unterschutzstellung());
                LocalDate datum = LocalDate.parse(schutz.datum(), DateTimeFormatter.ofPattern("dd.MM.yy"));
                unterschutz.setDatum(datum);
                unterschutz.setBemerkung(schutz.bemerkung());
                art.getDetaillierteSchutzdaten().add(unterschutz);
            }
        }
        
        
        art.setErgaenzendeAnmerkung(taxonInformation.ergaenzendeAnmerkung);
        
        
        return art;
    }
}
