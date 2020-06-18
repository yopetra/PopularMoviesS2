package com.example.android.popularmoviess1v02;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmoviess1v02.data.AppDatabase;
import com.example.android.popularmoviess1v02.data.MovieEntry;
import com.example.android.popularmoviess1v02.utils.NetworkUtils;
import com.example.android.popularmoviess1v02.utils.OpenMovieJsonUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MoviesAdapter.MovieAdapterOnClickHandler {

    private static final String TAG = NetworkUtils.class.getSimpleName();
    private static final int MOST_POPULAR = 1;
    private static final int TOP_RATED = 2;
    private static final int FAVORITE = 3;
    private static final String LIVECYCLE_CALLBACK_KEY = "callbacks";

    private RecyclerView mMoviesRecyclerView;
    private MoviesAdapter mAdapter;
    private TextView mErrorToDisplay;
    private TextView mMessageEmptyFavorites;
    private ProgressBar mLoadingSpinner;
    private int mPageNumberValue = 1;
    private int mSortingType = MOST_POPULAR; // 1- popular; 2- top rated

    private AppDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState != null){
            if(savedInstanceState.containsKey(LIVECYCLE_CALLBACK_KEY)){
                int prevSavedSortingState = savedInstanceState.getInt(LIVECYCLE_CALLBACK_KEY);
                mSortingType = prevSavedSortingState;

            }
        }

        mMoviesRecyclerView = (RecyclerView) findViewById(R.id.rv_movies);
        mErrorToDisplay = (TextView) findViewById(R.id.tv_error_message);
        mMessageEmptyFavorites = (TextView) findViewById(R.id.tv_message_no_favorite_data);
        mLoadingSpinner = (ProgressBar) findViewById(R.id.pb_loading_spinner);

        mMoviesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                // Here we define end if scrolling
                if(!mMoviesRecyclerView.canScrollVertically(1 ) && newState == mMoviesRecyclerView.SCROLL_STATE_IDLE){
                    if(mSortingType != FAVORITE){
                        mPageNumberValue++;
                        loadMovieData();
                    }
                }
            }
        });

        mMoviesRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
                Log.d(TAG, "Touched item");
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean b) {

            }
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        mMoviesRecyclerView.setLayoutManager(gridLayoutManager);
        mMoviesRecyclerView.setHasFixedSize(true);
        mAdapter = new MoviesAdapter(this);
        mMoviesRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sorting, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Clear all data for recyclerview, scroll up to the top, and clear page number to 1
        mAdapter.clearData();
        mMoviesRecyclerView.scrollToPosition(0);
        mPageNumberValue = 1;

        int id = item.getItemId();
        if(id == R.id.action_popular){
            mSortingType = MOST_POPULAR;
        }

        if(id == R.id.action_top_rated){
            mSortingType = TOP_RATED;
        }

        if(id == R.id.action_favorite){
            mSortingType = FAVORITE;
        }

        loadMovieData();

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        mAdapter.clearData();
        mDb = AppDatabase.getInstance(getApplicationContext());
        loadMovieData();
        super.onResume();
    }

    @Override
    public void onClick(JSONObject movieItem) {
        Integer movieId = null;

        try {
            movieId = movieItem.getInt("posterId");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra(getString(R.string.movieId), movieId);
        intent.putExtra(getString(R.string.sorting_type), mSortingType);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(LIVECYCLE_CALLBACK_KEY, mSortingType);
    }

    private void loadMovieData() {
        boolean isIntAnailable = NetworkUtils.isConnected();
        if( (isIntAnailable && mSortingType != FAVORITE) || (mSortingType == FAVORITE) ) {
            showMovieDataView();
            new FetchMovieTask().execute(mPageNumberValue, mSortingType);
        }else{
            showErrorMessage();
        }



    }

    private void showMovieDataView() {
        mErrorToDisplay.setVisibility(View.INVISIBLE);
        mMessageEmptyFavorites.setVisibility(View.INVISIBLE);
        mMoviesRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage(){
        mErrorToDisplay.setVisibility(View.VISIBLE);
        mMoviesRecyclerView.setVisibility(View.INVISIBLE);
    }

    private void showMessageEmptyFavorites() {
        mMessageEmptyFavorites.setVisibility(View.VISIBLE);
        mMoviesRecyclerView.setVisibility(View.INVISIBLE);
    }

    public class FetchMovieTask extends AsyncTask<Integer, Void, JSONArray> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingSpinner.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONArray doInBackground(Integer... params) {

            if(params.length == 0){
                return null;
            }

            int pageNumber = params[0];
            int sortingType = params[1];

            if(sortingType == MOST_POPULAR || sortingType == TOP_RATED){
                URL movieRequestUrl = NetworkUtils.buildUrl(pageNumber, sortingType);

                try{
                    String jsonMoviewResponse = NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);

                    JSONArray simpleJsonMovieData = OpenMovieJsonUtils.getSimpleMovieStringFromJson(jsonMoviewResponse);

                    return simpleJsonMovieData;
                }catch(Exception e){
                    e.printStackTrace();
                    return null;
                }
            }

            mDb = AppDatabase.getInstance(getApplicationContext());
            if(sortingType == FAVORITE){
                final JSONArray[] simpleJsonMovieData = {null};
                final List<MovieEntry> movieEntries = mDb.movieDao().loadAllMovies();
                try{
                    simpleJsonMovieData[0] = OpenMovieJsonUtils.getSimpleMovieStringFromList(movieEntries);
                }catch(JSONException e){
                    e.printStackTrace();
                }

                return simpleJsonMovieData[0];
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONArray movieData) {
            mLoadingSpinner.setVisibility(View.INVISIBLE);

            if(movieData.length() > 0){
                showMovieDataView();

                mAdapter.setMovieData(movieData, mSortingType);
            }else{
                if(mSortingType != FAVORITE){
                    showErrorMessage();
                }else{
                    showMessageEmptyFavorites();
                }
            }
        }
    }
}
