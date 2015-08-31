package name.caiyao.skinCheck.UI;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;

import name.caiyao.skinCheck.R;
import name.caiyao.skinCheck.UI.fragment.FunctionFragment;
import name.caiyao.skinCheck.UI.fragment.HomeFragment;
import name.caiyao.skinCheck.UI.fragment.MeFragment;
import name.caiyao.skinCheck.UI.fragment.SayFragment;

public class MainActivity extends FragmentActivity implements MeFragment.OnFragmentInteractionListener,SayFragment.OnFragmentInteractionListener,FunctionFragment.OnFragmentInteractionListener,HomeFragment.OnFragmentInteractionListener {
    private Button[] mTabs;
    private Fragment[] fragments;
    private Fragment homeFragment,functionFragment,sayFragment,meFragment;
    private int index,currentTabIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initTab();
    }

    public void initView(){
        mTabs = new Button[4];
        mTabs[0] = (Button) findViewById(R.id.btn_home);
        mTabs[1] = (Button) findViewById(R.id.btn_function);
        mTabs[2] = (Button) findViewById(R.id.btn_say);
        mTabs[3] = (Button) findViewById(R.id.btn_me);
        mTabs[0].setSelected(true);

       getActionBar().hide();
    }

    public void initTab(){
        homeFragment = new HomeFragment();
        functionFragment = new FunctionFragment();
        sayFragment = new SayFragment();
        meFragment = new MeFragment();
        fragments = new Fragment[] { homeFragment, functionFragment,
                sayFragment, meFragment };
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container,homeFragment,"homeFragment")
                .add(R.id.fragment_container,functionFragment,"functionFragment")
                .add(R.id.fragment_container,sayFragment,"sayFragment")
                .add(R.id.fragment_container,meFragment,"meFragment")
                .hide(functionFragment)
                .hide(sayFragment)
                .hide(meFragment)
                .show(homeFragment).commit();
    }

    public void onTabSelect(View v){
        switch (v.getId()){
            case R.id.btn_home:
                index = 0;
                break;
            case R.id.btn_function:
                index = 1;
                break;
            case R.id.btn_say:
                index = 2;
                break;
            case R.id.btn_me:
                index = 3;
                break;
            default:
                break;
        }
        if (currentTabIndex != index) {
            FragmentTransaction trx = getSupportFragmentManager()
                    .beginTransaction();
            trx.hide(fragments[currentTabIndex]);
            if (!fragments[index].isAdded()) {
                trx.add(R.id.fragment_container, fragments[index]);
            }
            trx.show(fragments[index]).commit();
        }
        mTabs[currentTabIndex].setSelected(false);
        // 把当前tab设为选中状态
        mTabs[index].setSelected(true);
        currentTabIndex = index;
    }

    @Override
    public void onHomeFragmentInteraction(String string) {}

    @Override
    public void onFunctionFragmentInteraction(String string) {}

    @Override
    public void onSayFragmentInteraction(String s) {}

    @Override
    public void onMeFragmentInteraction(String s) {}
}
