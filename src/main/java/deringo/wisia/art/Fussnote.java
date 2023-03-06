package deringo.wisia.art;

import java.io.Serializable;

public class Fussnote implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String text;

    @Override
    public String toString() {
        return "Fussnote [id=" + id + ", text=" + text + "]";
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
}
