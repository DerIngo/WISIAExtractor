package deringo.wisia.art;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;


public class Art implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer knoten_id;

    private String wissenschaftlicherName; // gueltigerName1
    private String gueltigerName2;
    
    private String gruppe;
    private List<String> taxonomie = new ArrayList<>();
    private List<String> synonyme = new ArrayList<>();
    private List<LandessprachlicherName> landesprNamen = new ArrayList<>();

    private List<Regelwerk> regelwerke = new ArrayList<>();
    private List<Unterschutzstellung> detaillierteSchutzdaten = new ArrayList<>();
    
    private String ergaenzendeAnmerkung;

    public String getDeutscherName() {
        for (LandessprachlicherName name : landesprNamen) {
            if (StringUtils.equalsIgnoreCase("germany", name.getLand())) {
                return name.getLandessprachlicherName();
            }
        }
        return null;
    }
    
    
    @Override
    public String toString() {
        return "Art [knoten_id=" + knoten_id + ", wissenschaftlicherName=" + wissenschaftlicherName
                + ", gueltigerName2=" + gueltigerName2 + ", gruppe=" + gruppe + ", taxonomie=" + taxonomie
                + ", synonyme=" + synonyme + ", landesprNamen=" + landesprNamen + ", regelwerke=" + regelwerke
                + ", detaillierteSchutzdaten=" + detaillierteSchutzdaten + ", ergaenzendeAnmerkung="
                + ergaenzendeAnmerkung + "]";
    }

    public Integer getKnoten_id() {
        return knoten_id;
    }

    public void setKnoten_id(Integer knoten_id) {
        this.knoten_id = knoten_id;
    }

    public String getWissenschaftlicherName() {
        return wissenschaftlicherName;
    }

    public void setWissenschaftlicherName(String wissenschaftlicherName) {
        this.wissenschaftlicherName = wissenschaftlicherName;
    }

    public String getGueltigerName2() {
        return gueltigerName2;
    }

    public void setGueltigerName2(String gueltigerName2) {
        this.gueltigerName2 = gueltigerName2;
    }

    public String getGruppe() {
        return gruppe;
    }

    public void setGruppe(String gruppe) {
        this.gruppe = gruppe;
    }

    public List<String> getTaxonomie() {
        return taxonomie;
    }

    public void setTaxonomie(List<String> taxonomie) {
        this.taxonomie = taxonomie;
    }

    public List<String> getSynonyme() {
        return synonyme;
    }

    public void setSynonyme(List<String> synonyme) {
        this.synonyme = synonyme;
    }

    public List<LandessprachlicherName> getLandesprNamen() {
        return landesprNamen;
    }

    public void setLandesprNamen(List<LandessprachlicherName> landesprNamen) {
        this.landesprNamen = landesprNamen;
    }

    public List<Regelwerk> getRegelwerke() {
        return regelwerke;
    }

    public void setRegelwerke(List<Regelwerk> regelwerke) {
        this.regelwerke = regelwerke;
    }

    public List<Unterschutzstellung> getDetaillierteSchutzdaten() {
        return detaillierteSchutzdaten;
    }

    public void setDetaillierteSchutzdaten(List<Unterschutzstellung> detaillierteSchutzdaten) {
        this.detaillierteSchutzdaten = detaillierteSchutzdaten;
    }

    public String getErgaenzendeAnmerkung() {
        return ergaenzendeAnmerkung;
    }

    public void setErgaenzendeAnmerkung(String ergaenzendeAnmerkung) {
        this.ergaenzendeAnmerkung = ergaenzendeAnmerkung;
    }
}
