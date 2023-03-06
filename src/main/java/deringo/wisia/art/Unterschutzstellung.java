package deringo.wisia.art;

import java.io.Serializable;
import java.time.LocalDate;

public class Unterschutzstellung implements Serializable {
    private static final long serialVersionUID = 1L;

    private String unterschutzstellung;
    private LocalDate datum;
    private String bemerkung;
    
    @Override
    public String toString() {
        return "Unterschutzstellung [unterschutzstellung=" + unterschutzstellung + ", datum=" + datum + ", bemerkung="
                + bemerkung + "]";
    }
    public String getUnterschutzstellung() {
        return unterschutzstellung;
    }
    public void setUnterschutzstellung(String unterschutzstellung) {
        this.unterschutzstellung = unterschutzstellung;
    }
    public LocalDate getDatum() {
        return datum;
    }
    public void setDatum(LocalDate datum) {
        this.datum = datum;
    }
    public String getBemerkung() {
        return bemerkung;
    }
    public void setBemerkung(String bemerkung) {
        this.bemerkung = bemerkung;
    }
}
