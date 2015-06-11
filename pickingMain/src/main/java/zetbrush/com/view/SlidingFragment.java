package zetbrush.com.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.picsartvideo.R;

import zetbrush.generatingmain.MainGenFragment;


public class SlidingFragment extends Fragment  {
    RelativeLayout rlLayout;

    private static final String KEY_DEMO = "SlidingAct";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        FragmentProvider demo = FragmentProvider.INDICATOR_TRICK1;

        rlLayout  = (RelativeLayout)  inflater.inflate(R.layout.activity_slidingact, container, false);



        Toolbar toolbar = (Toolbar) rlLayout.findViewById(R.id.toolbar);
        toolbar.setTitle(demo.titleResId);


        ViewGroup tab = (ViewGroup)rlLayout. findViewById(R.id.tab);
        tab.addView(LayoutInflater.from(super.getActivity()).inflate(demo.layoutResId, tab, false));

        ViewPager viewPager = (ViewPager) rlLayout.findViewById(R.id.viewpager);
        SmartTabLayout viewPagerTab = (SmartTabLayout) rlLayout.findViewById(R.id.viewpagertab);
        demo.setup(viewPagerTab);

        FragmentPagerItems pages = new FragmentPagerItems(super.getActivity());
        for (int titleResId : demo.tabs()) {
            pages.add(FragmentPagerItem.of(getString(titleResId), MainGenFragment.class));
        }

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                super.getActivity().getSupportFragmentManager(), pages);

        viewPager.setAdapter(adapter);
        viewPagerTab.setViewPager(viewPager);

        return rlLayout;

    }


}
