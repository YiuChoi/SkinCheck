package name.caiyao.skinCheck.UI;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import name.caiyao.skinCheck.Model.City;
import name.caiyao.skinCheck.Model.Country;
import name.caiyao.skinCheck.Model.Province;
import name.caiyao.skinCheck.R;

public class SetInfoActivity extends Activity implements View.OnClickListener {
    private Button birth_set,btn_pro,btn_city,btn_dis,btn_skin_check;
    private Spinner skin_set;

    private int pPosition;
    private int cPosition;
    private List<Province> provinces;
    private String AddressXML;
    private boolean isCity = true;
    private boolean isCounty = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_info);
        initView();
        initData();
    }

    private void initView() {
        ActionBar actionbar = getActionBar();
        actionbar.setTitle("请输入您的信息");

        birth_set = (Button) findViewById(R.id.birth_set);
        btn_pro = (Button) findViewById(R.id.btn_pro);
        btn_city= (Button) findViewById(R.id.btn_city);
        btn_dis= (Button) findViewById(R.id.btn_dis);
        btn_skin_check = (Button) findViewById(R.id.btn_skin_check);
        skin_set = (Spinner) findViewById(R.id.skin_set);

        birth_set.setOnClickListener(this);
        btn_pro.setOnClickListener(this);
        btn_city.setOnClickListener(this);
        btn_dis.setOnClickListener(this);
        btn_skin_check.setOnClickListener(this);
        skin_set.setOnItemSelectedListener (new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Toast.makeText(SetInfoActivity.this, "请选择您的肤质", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        Toast.makeText(SetInfoActivity.this, "中性", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(SetInfoActivity.this, "干性", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(SetInfoActivity.this, "油性", Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        Toast.makeText(SetInfoActivity.this, "混合性", Toast.LENGTH_SHORT).show();
                        break;
                    case 5:
                        Toast.makeText(SetInfoActivity.this, "敏感性", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
                TextView tv=(TextView)view;//更改下拉框选择后的字体颜色
                tv.setTextColor(Color.BLACK);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initData() {
        AddressXML = getRawAddress().toString();//获取中国省市区信息
        try {
            analysisXML(AddressXML);
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //初始化button数据
        btn_pro.setText(provinces.get(provinces.size()/2).getProvince());
        btn_city.setText(provinces.get(provinces.size()/2).getCity_list().get(0).getCity());
        btn_dis.setText(provinces.get(provinces.size()/2).getCity_list().get(0).getCounty_list().get(0).getCounty());
        //初始化列表下标
        pPosition = provinces.size()/2;
        cPosition = 0;
    }

    protected void dateSelect(){
        AlertDialog.Builder builder = new AlertDialog.Builder(SetInfoActivity.this);
        View view = View.inflate(SetInfoActivity.this,R.layout.dialog_date_select,null);
        builder.setView(view);

        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);
        Calendar calendar = Calendar.getInstance();
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);
        builder.setTitle("选取日期");

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                StringBuffer sb = new StringBuffer();
                sb.append(datePicker.getYear());
                sb.append("年");
                sb.append(datePicker.getMonth()+1);
                sb.append("月");
                sb.append(datePicker.getDayOfMonth());
                sb.append("日");

                birth_set.setText(sb);
                dialog.cancel();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.birth_set:
                dateSelect();
                break;
            case R.id.btn_pro:
                createDialog(1);
                break;
            case R.id.btn_city:
                if(isCity == true){
                    createDialog(2);
                }
                break;
            case R.id.btn_dis:
                if(isCounty == true){
                    createDialog(3);
                }
                break;
            case R.id.btn_skin_check:
                Intent intent = new Intent(SetInfoActivity.this,SkinCheckActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void createDialog(final  int type) {
        ListView lv = new ListView(this);
        final Dialog dialog = new Dialog(this);
        dialog.setTitle("列表选择框");

        if(type == 1){
            ProvinceAdapter pAdapter = new ProvinceAdapter(provinces);
            lv.setAdapter(pAdapter);

        }else if(type == 2){
            CityAdapter cAdapter = new CityAdapter(provinces.get(pPosition).getCity_list());
            lv.setAdapter(cAdapter);
        }else if(type ==3){
            CountyAdapter coAdapter = new CountyAdapter(provinces.get(pPosition).getCity_list().get(cPosition).getCounty_list());
            lv.setAdapter(coAdapter);
        }

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
                if(type == 1){
                    pPosition = position;
                    btn_pro.setText(provinces.get(position).getProvince());
                    //判断该省下是否有市级
                    if(provinces.get(position).getCity_list().size() < 1){
                        btn_city.setText("");
                        btn_dis.setText("");
                        isCity = false;
                        isCounty = false;
                    }else{
                        isCity = true;
                        btn_city.setText(provinces.get(position).getCity_list().get(0).getCity());
                        cPosition = 0;
                        //判断该市下是否有区级或县级
                        if (provinces.get(position).getCity_list().get(0).getCounty_list().size() < 1) {
                            btn_dis.setText("");
                            isCounty = false;

                        }else{
                            isCounty = true;
                            btn_dis.setText(provinces.get(position).getCity_list().get(0).getCounty_list().get(0).getCounty());
                        }

                    }

                }else if(type == 2){
                    cPosition = position;
                    btn_city.setText(provinces.get(pPosition).getCity_list().get(position).getCity());
                    if (provinces.get(pPosition).getCity_list().get(position).getCounty_list().size() < 1) {
                        btn_dis.setText("");
                        isCounty = false;
                    }else{
                        isCounty = true;
                        btn_dis.setText(provinces.get(pPosition).getCity_list().get(cPosition).getCounty_list().get(0).getCounty());
                    }

                }else if(type == 3){
                    btn_dis.setText(provinces.get(pPosition).getCity_list().get(cPosition).getCounty_list().get(position).getCounty());
                }
                dialog.dismiss();
            }
        });

        dialog.setContentView(lv);
        dialog.show();
    }

    class ProvinceAdapter extends BaseAdapter {
        public List<Province> adapter_list;
        public ProvinceAdapter(List<Province> list){
            adapter_list = list;
        }

        @Override
        public int getCount() {
            return adapter_list.size();
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int position, View arg1, ViewGroup arg2) {
            TextView tv = new TextView(SetInfoActivity.this);
            tv.setPadding(20, 20, 20, 20);
            tv.setTextSize(18);
            tv.setText(adapter_list.get(position).getProvince());
            return tv;
        }
    }

    class CityAdapter extends BaseAdapter{
        public List<City> adapter_list;
        public CityAdapter(List<City> list){
            adapter_list = list;
        }

        @Override
        public int getCount() {
            return adapter_list.size();
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int position, View arg1, ViewGroup arg2) {
            TextView tv = new TextView(SetInfoActivity.this);
            tv.setPadding(20, 20, 20, 20);
            tv.setTextSize(18);
            tv.setText(adapter_list.get(position).getCity());
            return tv;
        }
    }

    class CountyAdapter extends BaseAdapter{
        public List<Country> adapter_list;
        public CountyAdapter(List<Country> list){
            adapter_list = list;
        }

        @Override
        public int getCount() {
            return adapter_list.size();
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int position, View arg1, ViewGroup arg2) {
            TextView tv = new TextView(SetInfoActivity.this);
            tv.setPadding(20, 20, 20, 20);
            tv.setTextSize(18);
            tv.setText(adapter_list.get(position).getCounty());
            return tv;
        }
    }

    /**
     * 获取地区raw里的地址xml内容
     * */
    public StringBuffer getRawAddress(){
        InputStream in = getResources().openRawResource(R.raw.address);
        InputStreamReader isr = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(isr);
        StringBuffer sb = new StringBuffer();
        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try {
            br.close();
            isr.close();
            in.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return sb;
    }

    /**
     * 解析省市区xml，
     * 采用的是pull解析，
     * 为什么选择pull解析：因为pull解析简单浅显易懂！
     * */
    public void analysisXML(String data) throws XmlPullParserException{
        try {
            Province province = null;
            City city= null;
            Country county= null;
            List<City> cityList = null;
            List<Country> countyList= null;

            InputStream xmlData = new ByteArrayInputStream(data.getBytes("UTF-8"));
            XmlPullParserFactory factory = null;
            factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser;
            parser = factory.newPullParser();
            parser.setInput(xmlData, "utf-8");
            String currentTag = null;

            String province_r;
            String city_r;
            String county_r;

            int type = parser.getEventType();
            while (type != XmlPullParser.END_DOCUMENT) {
                String typeName = parser.getName();

                if (type == XmlPullParser.START_TAG) {
                    if("root".equals(typeName)){
                        provinces = new ArrayList<Province>();
                    }else if("province".equals(typeName)){
                        province_r = parser.getAttributeValue(0);//获取标签里第一个属性,例如<city name="北京市" index="1">中的name属性
                        province = new Province();
                        province.setProvince(province_r);
                        cityList = new ArrayList<City>();
                    }else if("city".equals(typeName)){
                        city_r = parser.getAttributeValue(0);
                        city = new City();
                        city.setCity(city_r);
                        countyList = new ArrayList<Country>();
                    }else if("area".equals(typeName)){
                        county_r = parser.getAttributeValue(0);
                        county  = new Country();
                        county.setCounty(county_r);

                    }
                    currentTag = typeName;
                } else if (type == XmlPullParser.END_TAG) {
                    if("root".equals(typeName)){
                    }else if("province".equals(typeName)){
                        province.setCity_list(cityList);
                        provinces.add(province);

                    }else if("city".equals(typeName)){
                        city.setCounty_list(countyList);
                        cityList.add(city);

                    }else if("area".equals(typeName)){
                        countyList.add(county);
                    }
                } else if (type == XmlPullParser.TEXT) {
                    currentTag = null;
                }
                type = parser.next();
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
