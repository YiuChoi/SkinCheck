package name.caiyao.skinCheck.Model;

import java.util.List;
/**
 * Created by caiya on 2015/7/5 0005.
 */
public class City {
    public String city;
    List<Country> county_list;

    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public List<Country> getCounty_list() {
        return county_list;
    }
    public void setCounty_list(List<Country> county_list) {
        this.county_list = county_list;
    }

    @Override
    public String toString() {
        return "CityModel [city=" + city + ", county_list=" + county_list + "]";
    }
}
