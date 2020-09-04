package info.androidhive.glide.activity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.HashMap;

import info.androidhive.glide.R;
import info.androidhive.glide.model.GlideImage;
import com.dappergeek0.applibrary.*;

import org.json.JSONObject;


public class SlideshowDialogFragment extends DialogFragment {
    private String TAG = SlideshowDialogFragment.class.getSimpleName();
    private ArrayList<GlideImage> glideImages;
    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private TextView lblCount, lblTitle, lblDate, confirmText;
   private RelativeLayout deleteLayout;
    private int selectedPosition = 0;
   private String id = null;

   //
   public ImageView actionDismiss, deleteImage, confirmDelete;

    public static SlideshowDialogFragment newInstance() {
        SlideshowDialogFragment f = new SlideshowDialogFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_image_slider, container, false);
        viewPager = (ViewPager) v.findViewById(R.id.viewpager);
        lblCount = (TextView) v.findViewById(R.id.lbl_count);
        lblTitle = (TextView) v.findViewById(R.id.title);
        lblDate = (TextView) v.findViewById(R.id.date);
        confirmText = (TextView) v.findViewById(R.id.confirm_text);
       actionDismiss = (ImageView) v.findViewById(R.id.action_dismiss);
       deleteImage = (ImageView) v.findViewById(R.id.delete_image);
       confirmDelete = (ImageView) v.findViewById(R.id.confirm_delete);
       deleteLayout = (RelativeLayout) v.findViewById(R.id.delete_layout);

       // back arrow dismisses view
       actionDismiss.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
             dismiss();
          }
       });

        glideImages = (ArrayList<GlideImage>) getArguments().getSerializable("glideImages");
        selectedPosition = getArguments().getInt("position");
       try {
          id = getArguments().getString(Constants.brandIdString);
       }catch (NullPointerException e) {
          e.printStackTrace();
       }

       // show the delete image button on the admin screen, hide otherwise
       deleteImage.setVisibility(id != null ? View.VISIBLE : View.GONE);

       // set delete button onClickListener
       deleteImage.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
             deleteImage.setVisibility(View.GONE);

             // show the confirm delete layout
             deleteLayout.setVisibility(View.VISIBLE);
          }
       });

        Log.e(TAG, "position: " + selectedPosition);
        Log.e(TAG, "glideImages size: " + glideImages.size());

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        setCurrentItem(selectedPosition);

        return v;
    }

    private void setCurrentItem(int position) {
        viewPager.setCurrentItem(position, false);
        displayMetaInfo(position);
       initiateDelete(position);
    }

    //	page change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            displayMetaInfo(position);
           initiateDelete(position);

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    private void displayMetaInfo(int position) {
        lblCount.setText((position + 1) + " of " + glideImages.size());

        GlideImage glideImage = glideImages.get(position);
        lblTitle.setText(glideImage.getName());
        lblDate.setText(glideImage.getTimestamp());
    }

   public void initiateDelete(int position){

      final int delPosition = position;

      deleteLayout.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            deleteImage.setVisibility(View.VISIBLE);
            deleteLayout.setVisibility(View.GONE);
         }
      });

      // confirm delete onClickListener(image click)
      confirmDelete.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            deleteConfirmed(delPosition);
         }
      });

      // confirm delete onClickListener(text click)
      confirmText.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            deleteConfirmed(delPosition);
         }
      });
   }

   // todo: my added scripts to glide module: dappergeek0
   // delete current image once confirmed
   private void deleteConfirmed(int delPosition) {

      GlideImage glideImage = glideImages.get(delPosition);
      String[] splits = glideImage.getLarge().split("/");
      String dImage = splits[splits.length - 1];

      // initiate AsyncTask to delete image from server DB
      new DeleteImageTask(id, dImage).execute();

      // remove image from arrayList
      glideImages.remove(delPosition);
      Toast.makeText(getContext(),
          "picture deleted",
          Toast.LENGTH_SHORT).show();

      // fix for not updating count when deleted image is not last
      if (delPosition < glideImages.size() - 1) {
         lblCount.setText((delPosition + 1)
             + " of " + (glideImages.size()));
      }
      myViewPagerAdapter.notifyDataSetChanged();
      deleteImage.setVisibility(View.VISIBLE);
      deleteLayout.setVisibility(View.GONE);
   }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    //	adapter
    public class MyViewPagerAdapter extends PagerAdapter {

        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.image_fullscreen_preview, container, false);

            ImageView imageViewPreview = (ImageView) view.findViewById(R.id.image_preview);

            GlideImage glideImage = glideImages.get(position);

            Glide.with(getActivity()).load(glideImage.getLarge())
                    .thumbnail(0.5f)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageViewPreview);

            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return glideImages.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == ((View) obj);
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

       @Override
       public int getItemPosition(Object object){
          return PagerAdapter.POSITION_NONE;
       }
    }


   /** AsyncTask to delete image from DB on server */
   public class DeleteImageTask extends AsyncTask<String, String, String > {

      JSONParser jsonParser = new JSONParser();
      final String id, image;

      DeleteImageTask(String id, String image) {
         this.id = id;
         this.image = image;
      }

      @Override
      protected String doInBackground(String... args) {
         try {
            HashMap<String, String > params = new HashMap<>();
            params.put(Constants.modeString, "deleteImage");
            params.put("id", id);
            params.put("image", image);

            JSONObject json = jsonParser.makeHttpRequest(Constants.deleteFromGalleryUrl, Constants.parserPost, params);

            if (json != null) {
               String success = "successful";

            }
         }catch (Exception e) {
            e.printStackTrace();
         }

         return null;
      }
   }
}
