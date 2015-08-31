package name.caiyao.skinCheck.Model;

import java.util.List;

/**
 * Created by caiya on 2015/7/5 0005.
 */
public class Province {
    public String province;
    List<City> city_list;

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public List<City> getCity_list() {
        return city_list;
    }

    public void setCity_list(List<City> city_list) {
        this.city_list = city_list;
    }

    @Override
    public String toString() {
        return "ProvinceModel [province=" + province + ", city_list="
                + city_list + "]";
    }

}
