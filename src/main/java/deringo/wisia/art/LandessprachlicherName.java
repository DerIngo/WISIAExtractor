package deringo.wisia.art;

import java.io.Serializable;

public class LandessprachlicherName implements Serializable {
    private static final long serialVersionUID = 1L;
    private String land;
    private String landessprachlicherName;

    
    public LandessprachlicherName(String land, String landessprachlicherName) {
        super();
        this.land = land;
        this.landessprachlicherName = landessprachlicherName;
    }
    @Override
    public String toString() {
        return "LandessprachlicherName [land=" + land + ", landessprachlicherName=" + landessprachlicherName + "]";
    }
    public String getLand() {
        return land;
    }
    public void setLand(String land) {
        this.land = land;
    }
    public String getLandessprachlicherName() {
        return landessprachlicherName;
    }
    public void setLandessprachlicherName(String landessprachlicherName) {
        this.landessprachlicherName = landessprachlicherName;
    }
}
