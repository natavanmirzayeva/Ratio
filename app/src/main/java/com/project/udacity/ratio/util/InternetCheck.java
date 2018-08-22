package com.project.udacity.ratio.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class InternetCheck extends AsyncTask<Void, Void, Void> {


    private WeakReference<Context> contextRef;
    private InternetCheckListener listener;

    public InternetCheck(Context context) {
        contextRef = new WeakReference<>(context);

    }

    @Override
    protected Void doInBackground(Void... params) {


        boolean b = hasInternetAccess();
        listener.onComplete(b);

        return null;
    }


    public void isInternetConnectionAvailable(InternetCheckListener x) {
        listener = x;
        execute();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) contextRef.get().getSystemService(CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private boolean hasInternetAccess() {
        if (isNetworkAvailable()) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://clients3.google.com/generate_204").openConnection());
                urlc.setRequestProperty("User-Agent", "Android");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 204 &&
                        urlc.getContentLength() == 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("TAG", "No network available!");
            return false;
        }
        return false;
    }

    public interface InternetCheckListener {
        void onComplete(boolean connected);
    }

}
