package com.example.android.popularmoviess1v02.data;

public class Trailer {

    private String mName;
    private String mKey;

    public Trailer(String name, String key){
        mName = name;
        mKey = key;
    }

    public String getName(){
        return mName;
    }

    public String getKey(){
        return mKey;
    }
}
