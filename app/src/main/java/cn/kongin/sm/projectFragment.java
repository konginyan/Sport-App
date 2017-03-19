package cn.kongin.sm;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class projectFragment extends Fragment{
    //分页
    private NoScrollViewPager mViewPager;
    private TabLayout mTabLayout;
    private ArrayList<String> mTabList;
    private List<Fragment> mFragments;
    private FragmentPagerAdapter mAdapter;

    private View view;
    private boolean firstload;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.tab_project,container,false);
        firstload = true;
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser){
            if(firstload){
                firstload = false;
                initView();
            }
        }
    }

    private void initView(){

        mViewPager  = (NoScrollViewPager) view.findViewById(R.id.graphview);
        mTabLayout = (TabLayout)view.findViewById(R.id.graphtab);

        mTabList = new ArrayList<String>();

        mFragments = new ArrayList<Fragment>();
        Fragment stepGraph = new StepGraph();
        Fragment runGraph = new RunGraph();
        mFragments.add(stepGraph);
        mFragments.add(runGraph);

        mAdapter = new FragmentPagerAdapter(getActivity().getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragments.get(position);
            }

            @Override
            public int getCount() {
                return mFragments.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mTabList.get(position);
            }
        };

        mViewPager.setAdapter(mAdapter);

        mTabList.add("步行");
        mTabList.add("跑步");
        mTabLayout.addTab(mTabLayout.newTab().setText(mTabList.get(0)));
        mTabLayout.addTab(mTabLayout.newTab().setText(mTabList.get(1)));

        mTabLayout.setTabMode(TabLayout.MODE_FIXED);

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());//点击哪个就跳转哪个界面
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        mTabLayout.setupWithViewPager(mViewPager);
    }

    public void setTabClickable(){
        mTabLayout.setClickable(!mTabLayout.isClickable());
    }
}
