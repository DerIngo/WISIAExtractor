package deringo.wisia.taxon;

import java.io.Serializable;
import java.util.List;

public class TaxonInformation implements Serializable {
    private static final long serialVersionUID = 1L;

    int knotenId;

    public TaxonInformation(int knotenId) {
        super();
        this.knotenId = knotenId;
    }
    
    GueltigerName gueltigerName;
    public record GueltigerName (String gueltigerName1, String gueltigerName2) implements Serializable {
        public GueltigerName(String gueltigerName1, String gueltigerName2) {
            this.gueltigerName1 = gueltigerName1;
            this.gueltigerName2 = gueltigerName2;
        }

        @Override
        public String toString() {
            return "GueltigerName [gueltigerName1=" + gueltigerName1 + ", gueltigerName2=" + gueltigerName2 + "]";
        }
        
    }

    
    String gruppe;
    
    List<String> taxonomie;
    
    List<String> synonyme;
    
    List<LandesprName> landesprNamen;
    public record LandesprName (String land, String landesprName) implements Serializable {
        public LandesprName (String land, String landesprName) {
            this.land = land;
            this.landesprName = landesprName;
        }

        @Override
        public String toString() {
            return "LandesprName [land=" + land + ", landesprName=" + landesprName + "]";
        }
    }
    
    List<Schutz> schutzListe;
    public record Schutz (String regelwerk1, String regelwerk2, String fussnote1, String fussnote2, String nameImRegelwerk) implements Serializable {
        public Schutz (String regelwerk1, String regelwerk2, String fussnote1, String fussnote2, String nameImRegelwerk) {
            this.regelwerk1 = regelwerk1;
            this.regelwerk2 = regelwerk2;
            this.fussnote1 = fussnote1;
            this.fussnote2 = fussnote2;
            this.nameImRegelwerk = nameImRegelwerk;
        }

        @Override
        public String toString() {
            return "Schutz [regelwerk1=" + regelwerk1 + ", regelwerk2=" + regelwerk2 + ", fussnote1=" + fussnote1
                    + ", fussnote2=" + fussnote2 + ", nameImRegelwerk=" + nameImRegelwerk + "]";
        }
        
    }
    
    List<DetaillierteSchutzdaten> detaillierteSchutzdatenListe;
    public record DetaillierteSchutzdaten (String unterschutzstellung, String datum, String bemerkung) implements Serializable {
        public DetaillierteSchutzdaten(String unterschutzstellung, String datum, String bemerkung) {
            this.unterschutzstellung = unterschutzstellung;
            this.datum = datum;
            this.bemerkung = bemerkung;
        }

        @Override
        public String toString() {
            return "DetaillierteSchutzdaten [unterschutzstellung=" + unterschutzstellung + ", datum=" + datum
                    + ", bemerkung=" + bemerkung + "]";
        }
        
    }
    
    String ergaenzendeAnmerkung;
    
    
    @Override
    public String toString() {
        return "TaxonInformation [knotenId=" + knotenId + ", gueltigerName=" + gueltigerName + ", gruppe=" + gruppe
                + ", taxonomie=" + taxonomie + ", synonyme=" + synonyme + ", landesprNamen=" + landesprNamen
                + ", schutzListe=" + schutzListe + ", detaillierteSchutzdatenListe=" + detaillierteSchutzdatenListe
                + "]";
    }
    
    
}
