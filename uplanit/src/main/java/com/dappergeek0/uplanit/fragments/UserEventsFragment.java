package com.dappergeek0.uplanit.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dappergeek0.uplanit.DatabaseHandler;
import com.dappergeek0.uplanit.Event;
import com.dappergeek0.uplanit.ItemClickSupport;
import com.dappergeek0.uplanit.R;

import com.dappergeek0.uplanit.*;
import com.dappergeek0.uplanit.dialogs.DialogActionClear;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class UserEventsFragment extends Fragment implements View.OnClickListener {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARGUMENT_EXTRAS = "extra_string";

    // Declare Views
    TextView noEventText;
    Button planFirst;
    FloatingActionButton fab;

    HashMap<String,String> sessionDetails = new HashMap<>();
    List<Event> eventsList;
    private View rootView;

    // classes
    DatabaseHandler databaseHandler;
    ShowDialogFragmentClass showDialogFragmentClass;

    //
    SpacesItemDecoration decoration;
    EventsAdapter eventsAdapter;

    // UI views
    ImageView connErrorImage;
    TextView connErrorText;

    public UserEventsFragment() {
        // Required empty public constructor
    }

    /**
     * Declaration of new instance for BrandDetailsFragment
     */
    public static UserEventsFragment newInstance() {
        return new UserEventsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate UI layout
        rootView = inflater.inflate(R.layout.fragment_user_events, container, false);
        // Setup layout views
        connErrorImage = (ImageView) rootView.findViewById(R.id.conn_error_image);
        connErrorText = (TextView) rootView.findViewById(R.id.conn_error_text);

        // Set screen title
        getActivity().setTitle(R.string.title_my_events);

        setHasOptionsMenu(true);//Set fragment options menu
        planFirst = (Button) rootView.findViewById(R.id.plan_first);
        noEventText = (TextView) rootView.findViewById(R.id.no_events_textView);
        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.show();

        // Set onClickListener on the plan event button
        planFirst.setOnClickListener(this);

        // Screen has options menu
        setHasOptionsMenu(true);

        // assign classes
        databaseHandler = new DatabaseHandler(getContext());
        showDialogFragmentClass = new ShowDialogFragmentClass();

        setEventsList();

        return rootView;//Show view
    }

    public void setEventsList(){

        // Check DB for planned events
        int eventsCount = 0;

        // get events count from database
        try {
            eventsCount = databaseHandler.getEventsCount();
        }catch (NumberFormatException e){
            e.printStackTrace();
        }

        // show events saved in database
        if (eventsCount  > 0) {

            // Hide the empty event list string
            noEventText.setVisibility(View.GONE);
            planFirst.setVisibility(View.GONE);

            //
            eventsList = databaseHandler.getAllEvents();

            // Lookup the recyclerView in activity layout
            RecyclerView rvEvents = (RecyclerView) rootView.findViewById(R.id.rvEvents);

            // Add decoration (Divider)
            RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
            rvEvents.addItemDecoration(itemDecoration);

            // Add decoration
            rvEvents.setItemAnimator(new SlideInUpAnimator());
            decoration = decoration == null ? new SpacesItemDecoration(16) : new SpacesItemDecoration(0);
            rvEvents.addItemDecoration(decoration);

            // Implement ItemClickSupport class for onItemClick in recyclerView item
            ItemClickSupport.addTo(rvEvents).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                @Override
                public void onItemClicked(RecyclerView recyclerView, int position, View v) {

                    Event event = eventsList.get(position);
                    int eventID = event.getID();

                    // New intent for view event action
                    Intent viewEvent = new Intent(getContext(), EventViewerActivity.class);

                    // Add extras
                    viewEvent.putExtra(Constants.intentExtra, eventID);

                    // Start Intent
                    startActivity(viewEvent);
                }
            });
            // Create adapter passing in the sample user data
            eventsAdapter = new EventsAdapter(getContext(), eventsList);
            // Attach the adapter to the recyclerView to populate items
            rvEvents.setAdapter(eventsAdapter);
            // Set layout manager to position the items
            rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        }
    }

    @Override
    public void onResume(){
        setEventsList();
        super.onResume();
    }

    /**
     * Create ActionBar Menu for Fragment
     */
    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_my_events, menu);

        // set visibility on the clear button depending on event count
        MenuItem clearEvents = menu.findItem(R.id.clear_events);
        clearEvents.setVisible(databaseHandler.getEventsCount() > 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){

            // handle the clear button (clear events) click
            case R.id.clear_events:
//                databaseHandler.clearEvents();

                // show the clear task dialog fragment
                DialogFragment dialogFragment = DialogActionClear.newInstance(Constants.myEventsFragTag);
                String dialogTag = Constants.clearTaskDialog;
                showDialog(dialogFragment, dialogTag);
                return true;

            // handle the add event click
            case R.id.plan_event:
                planEvent();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showDialog(DialogFragment fragment, String fragmentTag){

        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(fragmentTag);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = fragment;
        newFragment.show(ft, fragmentTag);
    }

    @Override
    public void onClick(View v) {

        // Handle the onClickListener on the plan event button
        if (v == planFirst){
            planEvent();
        }
    }

    // method open the plan event activity
    public void planEvent(){
        Intent createEvent = new Intent(getContext(),ActivityPlanEvent.class);
        createEvent.putExtra(Constants.SYNC_TYPE, Constants.SYNC_CREATE);
        startActivity(createEvent);
    }

    // Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
    public class EventsAdapter extends
        RecyclerView.Adapter<EventsAdapter.ViewHolder> {

        // Store a member variable for the events
        private List<Event> mEventsList;
        // Store the context for easy access
        private Context mContext;

        // Pass in the contact array into the constructor
        public EventsAdapter(Context context, List<Event> eventsList) {
            mEventsList = eventsList;
            mContext = context;
        }

        // Easy access to the context object in the recyclerView
        private Context getContext() {
            return mContext;
        }

        // Provide a direct reference to each of the views within a data item
        // Used to cache the views within the item layout for fast access
        public class ViewHolder extends RecyclerView.ViewHolder{
            // Your holder should contain a member variable
            // for any view that will be set as you render a row
            public TextView eventName, eventTime, eventLocation;
            public ImageView eventImage;

            // We also create a constructor that accepts the entire item row
            // and does the view lookups to find each subview
            public ViewHolder(View itemView) {
                // Stores the itemView in a public final member variable that can be used
                // to access the context from any ViewHolder instance.
                super(itemView);

                eventImage = (ImageView) itemView.findViewById(R.id.event_thumbnail);
                eventName = (TextView) itemView.findViewById(R.id.event_name);
                eventTime = (TextView) itemView.findViewById(R.id.event_time);
                eventLocation = (TextView) itemView.findViewById(R.id.event_location);
            }
        }

        @Override
        public EventsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            View brandView = inflater.inflate(R.layout.event_list_row, parent, false);

            // Return a new holder instance
            ViewHolder viewHolder = new ViewHolder(brandView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(EventsAdapter.ViewHolder viewHolder, int position) {
            // Get the data model based on position
//            Contact contact = mContacts.get(position);

            ImageView eventImage = viewHolder.eventImage;
            TextView eventName = viewHolder.eventName;
            TextView eventTime = viewHolder.eventTime;
            TextView eventLocation = viewHolder.eventLocation;

            // get the url from the data you passed to the `Map`
            Event event = eventsList.get(position);

            String eTime = event.getStartDate()+" "+event.getStartTime();
            Bitmap listImage = ImageEncoder.decodeSampledBitmapFromFile(event.getImage(), 100, 100);
            //
            eventName.setText(event.getName());
            eventTime.setText(eTime);
            eventLocation.setText(event.getLocation());

            // Set event image to imageView
            if(listImage != null){
                eventImage.setImageBitmap(listImage);
            }
        }

        @Override
        public int getItemCount() {
            return mEventsList.size();
        }

        // Clean all elements of the recycler
        public void clear() {
            mEventsList.clear();
            notifyDataSetChanged();
        }

        // Add a list of items
        public void addAll(ArrayList<Event> list) {
            mEventsList.addAll(list);
            notifyDataSetChanged();
        }
    }
/*

Decorator which adds spacing around the tiles in a Grid layout RecyclerView. Apply to a RecyclerView with:
Feel free to add any value you wish for SpacesItemDecoration. That value determines the amount of spacing.
Source: http://blog.grafixartist.com/pinterest-masonry-layout-staggered-grid/
*/

    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private final int mSpace;
        public SpacesItemDecoration(int space) {
            this.mSpace = space;
        }
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
//            outRect.left = mSpace;
//            outRect.right = mSpace;
            if (outRect.bottom < 1 ) {
                outRect.bottom = mSpace;
            }
            // Add top margin only for the first item to avoid double space between items
//            if (parent.getChildAdapterPosition(view) == 0)
//                outRect.top = mSpace;
        }
    }
}