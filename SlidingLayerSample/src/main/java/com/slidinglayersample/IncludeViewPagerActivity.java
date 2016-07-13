package com.slidinglayersample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.wunderlist.slidinglayer.SlidingLayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tubingbing on 15/12/17.
 */
public class IncludeViewPagerActivity extends AppCompatActivity{

    ViewPager vp;
    SlidingLayer slidingLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_include_viewpager);
        vp = (ViewPager) findViewById(R.id.vp);
        slidingLayer = (SlidingLayer) findViewById(R.id.slidingLayer);

        List<Fragment> fragmentList = new ArrayList<>(10);
        for (int i=0; i< 10; i++){
            Fragment fragment = TestFragment.newInstance();
            fragmentList.add(fragment);
        }

        TestFragmentAdapter adapter = new TestFragmentAdapter(getSupportFragmentManager(), fragmentList);
        vp.setAdapter(adapter);
    }

    public static class TestFragmentAdapter extends FragmentPagerAdapter{

        private List<Fragment> fragmentList;

        public TestFragmentAdapter(FragmentManager fm, List<Fragment> fragmentList) {
            super(fm);
            this.fragmentList = fragmentList;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }
    }

    public static class TestFragment extends Fragment{

        public static Fragment newInstance(){
            return new TestFragment();
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_include_viewpager, container, false);
            View tvGo = view.findViewById(R.id.tvGo);
            tvGo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), "GO GO GO", Toast.LENGTH_SHORT).show();
                }
            });
            return view;
        }
    }
}
