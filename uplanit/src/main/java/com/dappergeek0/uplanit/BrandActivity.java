package com.dappergeek0.uplanit;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.dappergeek0.uplanit.fragments.BrandDetailsFragment;
import com.dappergeek0.uplanit.fragments.BrandGalleryFragment;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity loads the fragments displaying brand details and stuff to the basic user
 */

public class BrandActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private int[] tabIcons = {
            R.drawable.ic_tab_contacts,
            R.drawable.ic_tab_favourite,
            R.drawable.ic_tab_call
    };

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    /**
     * Placeholder string for the intent extra from the FragmentBrandsListing activity
     * @param extra_string
     */
    public static String extra_string;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brand);
        //Get extra string from the FragmentBrandsListing activity
        extra_string = getIntent().getStringExtra(Constants.intentExtra);
        //Get data from the intent EXTRA_MESSAGE
        extra_string = extra_string.substring(1,extra_string.length()-1);
        String[] keyValuePairs = extra_string.split(",") ;
        Map<String,String> map = new HashMap<>();
        for(String pair : keyValuePairs){
            try {
                String[] entry = pair.split("=");
                map.put(entry[0].trim(),entry[1].trim());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
//        Set the appbar title
        setTitle(map.get(Constants.brandNameString));

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){

            // handle clicking on the back button
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0: // Fragment # 0 - This will show FirstFragment
                    return BrandDetailsFragment.newInstance(0, "Details", extra_string);
                case 1: // Fragment # 0 - This will show FirstFragment different title
                    return BrandGalleryFragment.newInstance(1, "Gallery", extra_string);
                case 2: // Fragment # 1 - This will show SecondFragment
                    return BrandDetailsFragment.newInstance(2, "Details", extra_string);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Details";
                case 1:
                    return "Gallery";
                case 2:
                    return "Testimonials";
            }
            return null;
        }
    }

}