package name.caiyao.skinCheck.Model;

/**
 * Created by caiya on 2015/7/5 0005.
 */
public class Country {
    public String county;

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    @Override
    public String toString() {
        return "CountyModel [county=" + county + "]";
    }
}
