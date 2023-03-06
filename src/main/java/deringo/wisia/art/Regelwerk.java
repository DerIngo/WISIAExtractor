package deringo.wisia.art;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Regelwerk implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private Set<String> namenImRegelwerk = new LinkedHashSet<>();
    private List<Anhang> anhaenge = new ArrayList<>();

    @Override
    public String toString() {
        return "Regelwerk [name=" + name + ", namenImRegelwerk=" + namenImRegelwerk + ", anhaenge=" + anhaenge + "]";
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Set<String> getNamenImRegelwerk() {
        return namenImRegelwerk;
    }
    public void setNamenImRegelwerk(Set<String> namenImRegelwerk) {
        this.namenImRegelwerk = namenImRegelwerk;
    }
    public List<Anhang> getAnhaenge() {
        return anhaenge;
    }
    public void setAnhaenge(List<Anhang> anhaenge) {
        this.anhaenge = anhaenge;
    }
}
