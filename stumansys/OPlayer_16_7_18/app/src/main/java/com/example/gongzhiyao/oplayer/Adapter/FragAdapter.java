package com.example.gongzhiyao.oplayer.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import java.util.List;

public class FragAdapter extends FragmentStatePagerAdapter {
      
    private List<Fragment> fragments;
    FragmentManager fm;
  
    public FragAdapter(FragmentManager fm) {
        super(fm);
        this.fm=fm;
    }  
      
    public FragAdapter(FragmentManager fm, List<Fragment> fragments) {  
        super(fm);  
        this.fragments = fragments;
        this.fm=fm;
    }  
  
    @Override  
    public Fragment getItem(int position) {

//        FragAdapter f = new FragAdapter(fm);
//           return f;
        return fragments.get(position);
    }  
  
    @Override  
    public int getCount() {  
        return fragments.size();  
    }


    @Override
    public int getItemPosition(Object object) {


        return PagerAdapter.POSITION_NONE;
    }
}