package deringo.wisia.art;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Anhang implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private List<Fussnote> fussnoten = new ArrayList<>();

    @Override
    public String toString() {
        return "Anhang [name=" + name + ", fussnoten=" + fussnoten + "]";
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public List<Fussnote> getFussnoten() {
        return fussnoten;
    }
    public void setFussnoten(List<Fussnote> fussnoten) {
        this.fussnoten = fussnoten;
    }
}
