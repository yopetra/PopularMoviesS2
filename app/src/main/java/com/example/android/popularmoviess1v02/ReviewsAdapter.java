package com.example.android.popularmoviess1v02;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewAdapterViewHolder> {

    private JSONArray mReviewsData = new JSONArray();

    public ReviewsAdapter(){}

    public int getDataSize(){
        return mReviewsData.length();
    }

    public class ReviewAdapterViewHolder extends RecyclerView.ViewHolder{

        public final TextView reviewerName;
        public final TextView reviewerContent;

        public ReviewAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            reviewerName = (TextView) itemView.findViewById(R.id.tv_reviewer_name);
            reviewerContent = (TextView) itemView.findViewById(R.id.tv_review_text);
        }
    }

    @NonNull
    @Override
    public ReviewAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.review_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParent = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParent);

        return new ReviewAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewAdapterViewHolder reviewAdapterViewHolder, int position) {
        String currentReviewerName = null;
        String currentReviewContent = null;
        try{
            JSONObject currentReviewItem = (JSONObject) mReviewsData.get(position);
            currentReviewerName = currentReviewItem.getString("author");
            currentReviewContent = currentReviewItem.getString("content");

        }catch(JSONException e){
            e.printStackTrace();
        }

        reviewAdapterViewHolder.reviewerName.setText(currentReviewerName);

        // Convert HTML to plain text and assign to the TextView
        reviewAdapterViewHolder.reviewerContent.setText(Html.fromHtml(Html.fromHtml(currentReviewContent).toString()));
    }

    @Override
    public int getItemCount() {
        if(mReviewsData == null) return 0;
        return mReviewsData.length();
    }

    public void setReviewData(JSONArray reviewData){

        int arraySize = reviewData.length();
        for(int i = 0; i < arraySize; i++){
            try {
                JSONObject currentJsonItem = (JSONObject) reviewData.getJSONObject(i);
                mReviewsData.put(currentJsonItem);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        notifyDataSetChanged();
    }
}
