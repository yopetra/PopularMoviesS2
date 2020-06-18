package com.example.android.popularmoviess1v02;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmoviess1v02.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class ReviewsActivity extends AppCompatActivity {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    private RecyclerView mReviewsRecyclerView;
    private ReviewsAdapter mReviewsAdapter;
    private ProgressBar mProgressBar;
    private TextView mNotAvailableMessage;
    private int mReviewPageNumber = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        mReviewsRecyclerView = (RecyclerView) findViewById(R.id.rv_reviews);
        mNotAvailableMessage = (TextView) findViewById(R.id.tv_reviews_not_available_message);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false );
        mReviewsRecyclerView.setLayoutManager(linearLayoutManager);

        mReviewsAdapter = new ReviewsAdapter();
        mReviewsRecyclerView.setAdapter(mReviewsAdapter);

        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading_spinner);

        mReviewsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                // Here we define end if scrolling
                if(!mReviewsRecyclerView.canScrollVertically(1 ) && newState == mReviewsRecyclerView.SCROLL_STATE_IDLE){
                    mReviewPageNumber++;
                    loadReviewData();
                }
            }
        });

        loadReviewData();
    }

    private void loadReviewData() {
        showListOfReviews();

        int movieId = getIntent().getIntExtra(getString(R.string.movieId), -1);
        new FetchReviewsTask().execute(movieId, mReviewPageNumber);
    }

    private void showListOfReviews() {
        mNotAvailableMessage.setVisibility(View.INVISIBLE);
        mReviewsRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showMessageReviewsNotAvailable() {
        mReviewsRecyclerView.setVisibility(View.INVISIBLE);
        mNotAvailableMessage.setVisibility(View.VISIBLE);
    }

    class FetchReviewsTask extends AsyncTask<Integer, Void, JSONArray>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONArray doInBackground(Integer... params) {
            if(params.length == 0){
                return null;
            }

            int movieId = params[0];
            int reviewPageNumber = params[1];
            String reviewsStringJson = null;
            JSONObject reviewsJson = null;
            JSONArray reviewsJsonArr = null;

            URL urlToReviews = NetworkUtils.buildMovieReviewsUrl(movieId, reviewPageNumber);
            try {
                reviewsStringJson = NetworkUtils.getResponseFromHttpUrl(urlToReviews);
                reviewsJson = new JSONObject(reviewsStringJson);
                reviewsJsonArr = new JSONArray(reviewsJson.getString("results"));
                return reviewsJsonArr;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONArray reviewsData) {
            mProgressBar.setVisibility(View.INVISIBLE);

            // If incoming data not null OR gotten data available, then show the content
            if(reviewsData.length() > 0 || mReviewsAdapter.getItemCount() > 0){
                showListOfReviews();

                mReviewsAdapter.setReviewData(reviewsData);
            }else{
                Log.d(TAG, "Reviews not available");
                showMessageReviewsNotAvailable();
            }
        }
    }
}
