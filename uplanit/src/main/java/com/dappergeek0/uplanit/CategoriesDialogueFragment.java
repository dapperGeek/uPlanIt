package com.dappergeek0.uplanit;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by DapperGeek0 on 1/19/2017.
 * File: FragmentCategories
 * Usage: List all categories from supplied array
 */

public class CategoriesDialogueFragment extends DialogFragment {

    String[] categories;//Categories Array string placeholder
    TypedArray icons ;
    ArrayList<HashMap<String, String>> titlesArrayList = new ArrayList<>();//ArrayList for category titles
    ArrayList<HashMap<String, String>> idsArrayList = new ArrayList<>();//ArrayList for category IDs
    ArrayList<String> titlesArrayString = new ArrayList<>();
    //HashMap holding category IDs from brands table
    HashMap<Integer, String> brandsCategories = new HashMap<>();

    // View elements
    View rootView;
    TextView brandsCount;
    private OnListItemSelectedListener mListener;

    public CategoriesDialogueFragment(){
        //Required empty constructor
    }

    public static CategoriesDialogueFragment newInstance(Serializable brandsCategories) {
        CategoriesDialogueFragment categoriesFragment = new CategoriesDialogueFragment();
        Bundle args = new Bundle();
        args.putSerializable("brandsCategories", brandsCategories);
        categoriesFragment.setArguments(args);
        return categoriesFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_categories, container, false);
        //Remember this is the FrameLayout area within your activity_main.xml
        FrameLayout contentFrameLayout = (FrameLayout) rootView.findViewById(R.id.content_frame);
        NavigationView nav_view = (NavigationView) rootView.findViewById(R.id.nav_view);
        icons = getActivity().getResources().obtainTypedArray(R.array.category_icons);

        brandsCategories = (HashMap<Integer, String>) getArguments().getSerializable("brandsCategories");

        //Set screen title
        getActivity().setTitle(getString(R.string.title_activity_categories));

        //List the categories, indicating the number of brands in each
        ListCategories();

        return rootView;
    }

    public void ProcessCategoriesArray(){

        categories = getActivity().getResources().getStringArray(R.array.categories);

        for (String str : categories){
            //HashMap to save category titles
            HashMap <String,String> titleHashMap = new HashMap<>();
            //HashMap to store category IDs
            HashMap <String,String> idHashMap = new HashMap<>();
            String[] splits = str.split("=");
            String cId = splits[0];
            String cTitle = splits[1];
            titleHashMap.put("category",cTitle);
            idHashMap.put("id",cId);
            titlesArrayList.add(titleHashMap);
            idsArrayList.add(idHashMap);
            titlesArrayString.add(cTitle);
        }
    }
    /**
     *
     */
    public void ListCategories(){
        //Create HashMaps for categories listView
        ProcessCategoriesArray();

        String[] from = {"category"};
        int[] to = {R.id.category};

        ListView listView= (ListView) rootView.findViewById(R.id.cat_list_view);
        ListAdapter adapter =
                new MyAdapter(
                        getContext(),
                        titlesArrayList,
                        R.layout.categories_list_row,
                        from,
                        to);
        listView.setAdapter(adapter);

        //Selecting item in categories list
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Selected category
                String category = titlesArrayList.get(position).get("category");
                // Selected category Id
                String catId = idsArrayList.get(position).get("id");
                Integer categoryId = Integer.parseInt(catId);
                mListener.OnListItemSelected(categoryId, category);
            }
        });
    }

    // Container Activity must implement this interface
    public interface OnListItemSelectedListener {
        public void OnListItemSelected(Integer categoryId, String category);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnListItemSelectedListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
    }

    //Create custom adapter to display images in the listView with picasso
    public class MyAdapter extends SimpleAdapter {
        public MyAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to){
            super(context, data, resource, from, to);
        }

        public View getView(int position, View convertView, ViewGroup parent){
            // here you let SimpleAdapter build the view normally.
            View v = super.getView(position, convertView, parent);
            brandsCount = (TextView) v.findViewById(R.id.brands_count);
            //Category Id for current row in View
            String categoryId = idsArrayList.get(position).get("id");
            //Count frequency in Category IDs array from brands table
            int count = 0;
            for (int i = 0; i< brandsCategories.size(); i++){
                if (categoryId.equals(brandsCategories.get(i))){
                    count++;
                }
            }
            //String showing number of brands registered in each category
            String activeBrands;
            switch (count) {
                case 0:
                    activeBrands = "No brand in here yet";
                    break;
                case 1:
                    activeBrands = count + " brand";
                    break;
                default:
                    activeBrands = count + " brands";
            }
            brandsCount.setText(activeBrands);
            brandsCount.startAnimation(AnimationUtils.loadAnimation(getContext(),android.R.anim.fade_in));


            // Then we get reference for Picasso
                ImageView img = (ImageView) v.findViewById(R.id.cat_list_image);
//                v.setTag(img); // <<< THIS LINE !!!!
            // get the url from the data you passed to the `Map`
            int icon = icons.getResourceId(position,-1);

            // do Picasso
                Picasso.with(v.getContext()).load(icon).into(img);
//             return the view
            return v;
        }
    }
}
