package deringo.wisia.fussnote;

public class Fussnote {
    private String regelwerk;
    private String fussnoteId;
    private String fussnoteText;

    public Fussnote() {
    }
    
    public Fussnote(String regelwerk, String fussnoteId, String fussnoteText) {
        super();
        this.regelwerk = regelwerk;
        this.fussnoteId = fussnoteId;
        this.fussnoteText = fussnoteText;
    }

    public String getRegelwerk() {
        return regelwerk;
    }
    public void setRegelwerk(String regelwerk) {
        this.regelwerk = regelwerk;
    }
    public String getFussnoteId() {
        return fussnoteId;
    }
    public void setFussnoteId(String fussnoteId) {
        this.fussnoteId = fussnoteId;
    }
    public String getFussnoteText() {
        return fussnoteText;
    }
    public void setFussnoteText(String fussnoteText) {
        this.fussnoteText = fussnoteText;
    }
}
