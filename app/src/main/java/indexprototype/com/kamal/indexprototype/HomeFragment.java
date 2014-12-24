package indexprototype.com.kamal.indexprototype;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;

import indexprototype.com.kamal.indexprototype.OnlineStoriesReader.DataFetcher;
import indexprototype.com.kamal.indexprototype.StorageManager.StorageManager;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String FROM_WHERE = "fromwhere";

    public static final String FRAGMENT_TAG = "HOME_FRAGMENT";
    public static final String BUNDLE_KEY = "HOME_FRAGMENT_BUNDLE_KEY";
    public static final String INSTANCE_FROM_NAVIGATION_DRAWER = "isntancefromnavigationdrawer";
    public static final String INSTANCE_FROM_ACTIVITY_CREATED = "instancefromactivitycreated";
    // TODO: Rename and change types of parameters
    private String whereFrom;


    private static SectionsAdapter mSectionsAdapter;
    private static ViewPager mViewPager;
    private StoriesSwipeToRefreshLayout swipeRefreshLayout;
    private SlidingTabLayout mSlidingTabLayout;

    public static HomeFragment newInstance(String originLocation) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(FROM_WHERE, originLocation);
        fragment.setArguments(args);
        return fragment;
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * If the fragment was instantiated from the navigation drawer, the fragment
     * does not run a thread to download the articles and images again.
     * @param savedInstanceState    The savedInstanceState of the fragment, if
     *                              it has been created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            whereFrom = getArguments().getString(FROM_WHERE);
        }

        //set the section adapter. The section adapter
        //needs to be initialized in the onCreate method so that it can be
        //maintained when the fragment is replaced.
        mSectionsAdapter = new SectionsAdapter(getChildFragmentManager());


        if(whereFrom==null || !whereFrom.equals(INSTANCE_FROM_NAVIGATION_DRAWER)) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                for (int i = 0; i < 6; i++) {
                    new DownloadData().execute(i);
                }
                for (int i = 0; i < 6; i++) {
                    new DownloadStoryImages().execute(i);
                }
            } else {
                Toast.makeText(getActivity(), "No network detected. Please connect to the internet and try again.", Toast.LENGTH_SHORT).show();
            }
            StorageManager loadingManager = new StorageManager(getActivity().getApplicationContext());
            loadingManager.loadStories();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v  = inflater.inflate(R.layout.fragment_home, container, false);



        //Sets the ViewPager and the Adapter for it. The section adapter
        //needs to be initialized in the onCreate method so that it can be
        //maintained when the fragment is replaced.

        mViewPager = (ViewPager) v.findViewById(R.id.home_fragment_view_pager);
        mViewPager.setAdapter(mSectionsAdapter);
        //This listener implements an important override that prevents the
        //SwipeRefreshLayout(see below) from stealing the vertical operations
        //of the fragment inside the ViewPager. It simply disables the SwipeRefreshLayout
        //once the user decides to do anything but swipe-down/move-up in the screen.
        mViewPager.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                swipeRefreshLayout.setEnabled(false);
                if(event.getAction()==MotionEvent.ACTION_UP){
                        swipeRefreshLayout.setEnabled(true);}
                return false;
            }
        });

        //Sets the Swipe down to refresh layout
        swipeRefreshLayout = (StoriesSwipeToRefreshLayout) v.findViewById(R.id.home_fragment_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setAdapterAndViewPager(mSectionsAdapter, mViewPager);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.primary_color));
        swipeRefreshLayout.requestDisallowInterceptTouchEvent(true);

        //Sets the Sliding tab layout
        mSlidingTabLayout = (SlidingTabLayout) v.findViewById(R.id.home_fragment_sliding_tabs);
        mSlidingTabLayout.setCustomTabView(R.layout.scroll_tab, R.id.scroll_tab_text_view);
        mSlidingTabLayout.setViewPager(mViewPager);
        mSlidingTabLayout.setCustomTabColorizer(new CustomSlidingTabColors());
        mSlidingTabLayout.setBackgroundColor(getResources().getColor(R.color.primary_color));


        return v;
    }


    public void refreshStories(){

        mSectionsAdapter.notifyDataSetChanged();
        mSectionsAdapter.refreshFragment(mViewPager.getCurrentItem());
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        refreshStories();
        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * A class that runs the download for the stories from the internet on a separate thread.
     */
    private class DownloadData extends AsyncTask<Integer, Integer, Boolean> {

        private int runningThread;
        @Override
        protected Boolean doInBackground(Integer... params) {
            runningThread = params[0];
            return DataFetcher.run(params[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                mSectionsAdapter.notifyDataSetChanged();
                mSectionsAdapter.refreshFragment(runningThread);
                Log.d("MainActivity", "running thread: " + runningThread);
            }
        }
    }


    /**
     * A class that downloads images of stories.
     */
    private class DownloadStoryImages extends AsyncTask<Integer, Integer, Boolean>{

        private int runningThread;
        @Override
        protected Boolean doInBackground(Integer... params) {

            runningThread = params[0];
            for(Story story: StoriesBank.getStories()){
                if(!story.getImageURL().equals(Story.DEFAULT_IMAGE_URL))
                    try {
                        story.setImageBitmap(Picasso.with(getActivity().getApplicationContext()).load(story.getImageURL()).get());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            Log.d("HomeFragment", "Image download for list "+ runningThread + " is complete");
            mSectionsAdapter.notifyDataSetChanged();
            mSectionsAdapter.refreshFragment(runningThread);
        }
    }

}
