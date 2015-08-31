package name.caiyao.skinCheck.UI;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import name.caiyao.skinCheck.R;

public class WelcomeActivity extends Activity implements View.OnClickListener{
    private View view1,view2,view3;
    private Button btn_share01,btn_share02;
    private TextView dot1,dot2,dot3;
    private ArrayList<View> viewList;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        initView();
        initDot();
    }

    private void initView() {
        getActionBar().hide();
        LayoutInflater inflater = getLayoutInflater().from(this);
        view1 = inflater.inflate(R.layout.activity_welcome01, null);
        view2 = inflater.inflate(R.layout.activity_welcome02, null);
        view3 = inflater.inflate(R.layout.activity_welcome03,null);

        viewList = new ArrayList<>();
        viewList.add(view1);
        viewList.add(view2);
        viewList.add(view3);

        btn_share01 = (Button) view1.findViewById(R.id.btn_share01);
        btn_share02 = (Button) view2.findViewById(R.id.btn_share02);

        btn_share01.setOnClickListener(this);
        btn_share02.setOnClickListener(this)
        ;
        viewPager = (ViewPager) findViewById(R.id.viewp_ager);
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(viewList.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(viewList.get(position));
                return viewList.get(position);
            }

            @Override
            public int getCount() {
                return viewList.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }
        });
        viewPager.setCurrentItem(0);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        dot1.setTextColor(Color.WHITE);
                        dot2.setTextColor(Color.BLACK);
                        dot3.setTextColor(Color.BLACK);
                        break;

                    case 1:
                        dot1.setTextColor(Color.BLACK);
                        dot2.setTextColor(Color.WHITE);
                        dot3.setTextColor(Color.BLACK);
                        break;

                    case 2:
                        dot1.setTextColor(Color.BLACK);
                        dot2.setTextColor(Color.BLACK);
                        dot3.setTextColor(Color.WHITE);
                        break;
                }

            }

            boolean isLast = true;

            @Override
            public void onPageScrollStateChanged(int state) {
                if(state == 2) {
                    isLast = false;
                } else if(state == 0 && isLast) {
                    //此处为你需要的情况，再加入当前页码判断可知道是第一页还是最后一页
                    if (viewPager.getCurrentItem()==2){
                        Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);
                        startActivity(intent);
                    }
                } else {
                    isLast = true;
                }
            }
        });
    }

    private void initDot(){
        dot1=(TextView) this.findViewById(R.id.textView1);
        dot2=(TextView) this.findViewById(R.id.textView2);
        dot3=(TextView) this.findViewById(R.id.textView3);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_share01:
                Toast.makeText(this,"睡眠",Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_share02:
                Toast.makeText(this,"运动",Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }
}
