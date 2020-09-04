package com.dappergeek0.uplanit;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dappergeek0.uplanit.fragments.BrandDashDetailsFragment;
import com.dappergeek0.uplanit.fragments.BrandDashGalleryFragment;

import java.util.HashMap;

/**
 * Created by DapperGeek0 on 2/3/2017.
 * File: FragmentBrandDashboard
 * Usage: Brand admin dashboard, creates tabs
 */

public class FragmentBrandDashboard extends Fragment {

   private Toolbar toolbar;
   private TabLayout tabLayout;
   private ViewPager viewPager;
   private int[] tabIcons = {
       R.drawable.ic_tab_contacts,
       R.drawable.ic_gallery,
       R.drawable.ic_tab_call
   };
   /**
    * Session manager class
    */
   SessionManager sessionManager;
   //temp hashMap for session details
   HashMap<String, String> sessionDetails = new HashMap<>();

   View rootView;
//   Integer tabPosition = 1;

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

   public FragmentBrandDashboard(){
      // Empty constructor required for fragment subclasses
   }

   // New instance of fragment
   public static FragmentBrandDashboard newInstance() {
      return new FragmentBrandDashboard();
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
   }

   @Nullable
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {

      // Inflate the layout for this fragment
      rootView = inflater.inflate(R.layout.fragment_brand_dashboard, container, false);
      sessionManager = new SessionManager(getContext());
      /**
       * Get active session details from sharedPreferences
       */
      sessionDetails = sessionManager.getUserDetails();
      //Set the Brand name as appbar title
      String title = sessionManager.getPreference(SessionManager.nameKey);
      getActivity().setTitle(title);

      toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

      // Create the adapter that will return a fragment for each of the
      // primary sections of the activity.
      if (mSectionsPagerAdapter == null) {
         mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
      }

      // Set up the ViewPager with the sections adapter.
      mViewPager = (ViewPager) rootView.findViewById(R.id.viewpager);
      mViewPager.setAdapter(mSectionsPagerAdapter);

//      mViewPager.setCurrentItem(tabPosition);

      TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
      tabLayout.setupWithViewPager(mViewPager);

      return rootView;
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
               return BrandDashDetailsFragment.newInstance();
            case 1: // Fragment # 0 - This will show FirstFragment different title
               return BrandDashGalleryFragment.newInstance();
            case 2: // Fragment # 1 - This will show SecondFragment
               return BrandDashDetailsFragment.newInstance();
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
