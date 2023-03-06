package deringo.wisia.taxon;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import deringo.wisia.WisiaExtraktor;
import deringo.wisia.taxon.TaxonInformation.Schutz;
import deringo.wisia.util.Utils;

public class TaxonInformationChecker {
    
    
    public static void main(String[] args) {
        check();
    }
    
    public static void check() {
        checkTaxonInformation();
        checkTaxonomie();
    }
    
    public static void checkTaxonInformation() {
        List<TaxonInformation> alleTaxonInformation = WisiaExtraktor.getAllTaxonInformation();
        checkTaxonInformation(alleTaxonInformation);
    }
    
    public static void checkTaxonInformation(List<TaxonInformation> alleTaxonInformation) {
        for (TaxonInformation information : alleTaxonInformation) {
            int knotenId = information.knotenId;
            // gebe alle Einträge ohne Taxonomie aus.
            if (information.taxonomie == null) {
                System.out.println("(knotenId: " + knotenId + ") " +"taxonomie NULL");
            }
            // überspringe leere Einträge
            if (TaxonInformationService.isEmpty(information)) {
                continue;
            }
            // stelle sicher, dass der gueltigeName1 gesetzt ist
            if (Utils.isBlank(information.gueltigerName.gueltigerName1())) {
                System.err.println("(knotenId: " + knotenId + ") " + "gueltigerName != null, aber gueltigerName1 ist blank");
            }
        }
        
        // Der Name im Regelwerk muss immer derselbe sein -> NEIN
        // Der Name muss pro Regelwerk und Anhang eindeutig sein -> NEIN
        // Der Name innerhalb eines Regelwerkes ist NICHT eindeutig -> kein Check
        boolean nameInRegelwerkEindeutig = true;
        if (nameInRegelwerkEindeutig) {
            for (TaxonInformation information : alleTaxonInformation) {
                Map<String, Set<String>> nameImRegelwerkMap = new HashMap<>();
                for (Schutz schutz : information.schutzListe) {
                    String regelwerk = schutz.regelwerk1() + ", " + schutz.regelwerk2();
                    String nameImRegelwerk = schutz.nameImRegelwerk();
                    Set<String> nameImRegelwerkSet = nameImRegelwerkMap.get(regelwerk);
                    if (nameImRegelwerkSet == null) {
                        nameImRegelwerkSet = new HashSet<>();
                        nameImRegelwerkMap.put(regelwerk, nameImRegelwerkSet);
                    }
                    nameImRegelwerkSet.add(nameImRegelwerk);
                }
                nameImRegelwerkMap.forEach((key, value) -> {
                    if (value.size() != 1) {
                        int knotenId = information.knotenId;
                        String message = String.format("(knotenId: %d) Name im Regelwerk uneindeutig.", knotenId);
                        System.err.println(message);
                        
//                        List<Schutz> schutzs = new ArrayList<>();
//                        for (Schutz schutz : information.schutzListe) {
//                            String regelwerk = schutz.regelwerk1() + ", " + schutz.regelwerk2();
//                            if (key.equals(regelwerk) || schutz.regelwerk1() == null) {
//                                schutzs.add(schutz);
//                            }
//                        }
//                        
//                        schutzs.forEach(schutz -> System.out.println(schutz));
                        
                    }
                });
            }
        }
        
        // TODO weitere Checks
        
    }
    
    public static void checkTaxonomie() {
        List<TaxonInformation> alleTaxonInformation = WisiaExtraktor.getAllTaxonInformation();
        checkTaxonomie(alleTaxonInformation);

    }
    
    public static void checkTaxonomie(List<TaxonInformation> alleTaxonInformation) {
        Map<String, Set<String>> taxonomie = extractTaxonomie(alleTaxonInformation);
        System.out.println("Prüfe, ob die Pfade der Taxonomie eindeutig sind. SPOILER: Sind sie nicht!");
        taxonomie.forEach((key, value) -> { 
            if((value == null) || ( value.size() != 1)) {
                System.err.println(key + " : " + value); 
            }
            });
        // Beispiel: Gompus
        // https://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?mode=Info&id=107809
        // Duplicate name. This name, above species rank, is duplicated within the NCBI classification
    }
    
    
    public static Map<String, Set<String>> extractTaxonomie(List<TaxonInformation> alleTaxonInformation) {
        Map<String, Set<String>> taxonomie = new HashMap<>();
        for (TaxonInformation information : alleTaxonInformation) {
            if (TaxonInformationService.isEmpty(information)) {
                continue;
            }
            for (int i= information.taxonomie.size()-1; i>=0; i--) {
                String element = information.taxonomie.get(i);
                String parent = "root";
                if (i>0) parent = information.taxonomie.get(i-1);
                Set<String> parentSet = taxonomie.get(element);
                if (parentSet == null) {
                    parentSet = new HashSet<>();
                    taxonomie.put(element, parentSet);
                }
                parentSet.add(parent);
            }
        }
        return taxonomie;
    }
}
