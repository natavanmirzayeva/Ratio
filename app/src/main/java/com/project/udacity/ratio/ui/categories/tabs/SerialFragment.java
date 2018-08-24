package com.project.udacity.ratio.ui.categories.tabs;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.project.udacity.ratio.MainActivity;
import com.project.udacity.ratio.R;
import com.project.udacity.ratio.data.CollectionApi;
import com.project.udacity.ratio.data.db.CollectionContract;
import com.project.udacity.ratio.models.Serial;
import com.project.udacity.ratio.ui.categories.detailscreens.SerialDetailActivity;
import com.project.udacity.ratio.util.ApiVariables;
import com.project.udacity.ratio.util.Global;
import com.project.udacity.ratio.util.InternetCheck;
import com.project.udacity.ratio.util.SerialAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 */
public class SerialFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.tv_series_recycler_view)
    RecyclerView recyclerView;

    GridLayoutManager gridLayoutManager;

    @SuppressLint("StaticFieldLeak")
    private static SerialAdapter serialAdapter;

    Parcelable savedRecyclerLayoutState;
    private static final String RECYCLERVIEW_STATE = "recyclerview-state-1";
    private static final String RECYCLERVIEW_STATE_ADAPTER = "recyclerview-state-adapter";

    public boolean isRatioCollection;
    public boolean isTopRated;
    public boolean isSearch;
    public boolean isOfflineData;
    public boolean isOfflineTopRated;

    private int topRatedResultLimit = 3;

    private static final int ID_SERIAL_LOADER = 47;
    private static final int ID_RATE_LOADER = 45;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference serialRef = database.getReference(CollectionContract.CollectionEntry.SERIAL_TABLE_NAME);

    public static SerialFragment newInstance() {
        Bundle args = new Bundle();
        SerialFragment fragment = new SerialFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_serial, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        isRatioCollection = getArguments().getBoolean("isRatioCollection");
        isTopRated = getArguments().getBoolean("isTopRated");
        isSearch = getArguments().getBoolean("isSearch", false);
        isOfflineData = false;

        if (isRatioCollection) {
            getLoaderManager().initLoader(ID_RATE_LOADER, null, SerialFragment.this);
        } else {
            new InternetCheck(getContext()).isInternetConnectionAvailable(new InternetCheck.InternetCheckListener() {

                @Override
                public void onComplete(final boolean connected) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (connected) {
                                if (isTopRated) {
                                    getTopRatedTvSeriesFromFirebase();
                                } else if (isSearch) {
                                    List<Serial> serials = getArguments().getParcelableArrayList("serials");
                                    displayTvSeries(serials, null);
                                } else {
                                    getTvSeriesFromApi();
                                }

                            } else {
                                if (isTopRated) {
                                    isOfflineTopRated = true;
                                    getLoaderManager().initLoader(ID_RATE_LOADER, null, SerialFragment.this);
                                } else {
                                    isOfflineData = true;
                                    getLoaderManager().initLoader(ID_SERIAL_LOADER, null, SerialFragment.this);
                                }
                            }
                        }
                    });
                }
            });
        }
        gridLayoutManager = new GridLayoutManager(getContext(), calculateNoOfColumns(getContext()));
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    private void getTopRatedTvSeriesFromFirebase() {
        serialRef.orderByChild("voteCount").limitToLast(topRatedResultLimit).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("girdi", "girdi");
                if (dataSnapshot.exists()) {
                    ArrayList<Serial> tvSeries = new ArrayList<>();

                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        tvSeries.add(data.getValue(Serial.class));
                    }
                    displayTvSeries(tvSeries, null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("dbError", databaseError.getMessage());
            }
        });

    }

    private void getTvSeriesFromApi() {
        OkHttpClient httpClient = new OkHttpClient.Builder().build();
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(ApiVariables.serialUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = builder.client(httpClient).build();
        CollectionApi tvSeriesApi = retrofit.create(CollectionApi.class);
        io.reactivex.Observable<JsonArray> call = tvSeriesApi.listTvSeries().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        call.map(new Function<JsonArray, List<Serial>>() {
            @Override
            public List<Serial> apply(JsonArray serialResponse) throws Exception {
                Gson gson = new Gson();
                List<Serial> serials = new ArrayList<>();
                for (int i = 0; i < serialResponse.size(); i++) {
                    Serial serial = gson.fromJson(serialResponse.get(i), Serial.class);
                    serials.add(serial);
                }

                return serials;
            }
        })
                .subscribe
                        (
                                new Consumer<List<Serial>>() {
                                    @Override
                                    public void accept(List<Serial> serials) throws Exception {
                                        List<Serial> serialsTemp = new ArrayList<>();

                                        for (Serial serial : serials) {
                                            if (serial.getPosterPath() != null) {
                                                serialsTemp.add(serial);
                                            }
                                        }


                                        displayTvSeries(serialsTemp, null);
                                    }
                                },
                                new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
                                        Log.i("error", "RxJava2, HTTP Error: " + throwable.getMessage());
                                    }
                                }
                        );
    }


    private void displayTvSeries(List<Serial> serials, List<byte[]> imageBlobs) {
        serialAdapter = new SerialAdapter(serials, imageBlobs, isRatioCollection, isOfflineData, isOfflineTopRated, getContext(), new SerialAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Serial serial, byte[] image, ImageView serialImage) {
                Global.image = image;

                Intent serialIntent = new Intent(getContext(), SerialDetailActivity.class);
                serialIntent.putExtra("serial", serial);
                serialIntent.putExtra("userId", MainActivity.USER_ID);

                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(getActivity(), serialImage, ViewCompat.getTransitionName(serialImage));
                startActivity(serialIntent, options.toBundle());
            }
        });

        recyclerView.setAdapter(serialAdapter);
        recyclerView.setHasFixedSize(true);
    }


    private int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int scalingFactor = 180;
        int noOfColumns = (int) (dpWidth / scalingFactor);
        if (noOfColumns < 2)
            noOfColumns = 2;
        return noOfColumns;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(RECYCLERVIEW_STATE, recyclerView.getLayoutManager().onSaveInstanceState());
        if (serialAdapter != null) {
            List<Serial> tvSeries = serialAdapter.getTvSeries();
            if (tvSeries != null && !tvSeries.isEmpty()) {
                outState.putParcelableArrayList(RECYCLERVIEW_STATE_ADAPTER, (ArrayList<? extends Parcelable>) tvSeries);
            }
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onViewStateRestored(@Nullable final Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            savedRecyclerLayoutState = savedInstanceState.getParcelable(RECYCLERVIEW_STATE);
            if (savedInstanceState.containsKey(RECYCLERVIEW_STATE_ADAPTER)) {
                List<Serial> tvSeriesResultList = savedInstanceState.getParcelableArrayList(RECYCLERVIEW_STATE_ADAPTER);
                serialAdapter.setTvSeries(tvSeriesResultList);
                recyclerView.setAdapter(serialAdapter);
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (savedRecyclerLayoutState != null) {
                        if (savedInstanceState.containsKey(RECYCLERVIEW_STATE_ADAPTER)) {
                            recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
                            savedRecyclerLayoutState = null;
                        }
                    }
                }
            }, 1500);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        if (isRatioCollection) {
            String arg[] = {"1", MainActivity.USER_ID, "3"};
            String selection = "isMyCollection=? AND userId=? AND collectionType=?";
            Uri uri = CollectionContract.CollectionEntry.CONTENT_URI_Rate;
            return new CursorLoader(
                    getContext(),
                    uri,
                    null,
                    selection,
                    arg,
                    null);

        } else if (isTopRated && isOfflineTopRated) {
            String arg[] = {"1", MainActivity.USER_ID, "3"};
            String selection = "isTopRated=? AND userId=? AND collectionType=?";
            Uri uri = CollectionContract.CollectionEntry.CONTENT_URI_Rate;
            return new CursorLoader(
                    getContext(),
                    uri,
                    null,
                    selection,
                    arg,
                    null);
        } else if (loaderId == ID_SERIAL_LOADER && isOfflineData) {
            Uri uri = CollectionContract.CollectionEntry.CONTENT_URI_TVSeries;
            return new CursorLoader(
                    getContext(),
                    uri,
                    null,
                    null,
                    null,
                    null);

        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        ArrayList<Serial> tvSeries = new ArrayList<>();
        ArrayList<byte[]> imageBlobs = new ArrayList<>();
        assert cursor != null;
        if (cursor.moveToFirst()) {
            do {
                int index;
                index = cursor.getColumnIndexOrThrow("id");
                String id = cursor.getString(index);
                index = cursor.getColumnIndexOrThrow("title");
                String original_title = cursor.getString(index);
                index = cursor.getColumnIndexOrThrow("posterPath");
                byte[] imagePoster = cursor.getBlob(index);
                index = cursor.getColumnIndexOrThrow("description");
                String overview = cursor.getString(index);
                index = cursor.getColumnIndexOrThrow("voteAverage");
                double vote_average = cursor.getDouble(index);
                index = cursor.getColumnIndexOrThrow("release_date");
                String release_date = cursor.getString(index);
                index = cursor.getColumnIndexOrThrow("language");
                String language = cursor.getString(index);
                index = cursor.getColumnIndexOrThrow("voteCount");
                int voteCount = cursor.getInt(index);
                index = cursor.getColumnIndexOrThrow("voteScore");
                int voteScore = cursor.getInt(index);
                index = cursor.getColumnIndexOrThrow("genres");
                String genres = cursor.getString(index);

                List<String> ls = new ArrayList<>();
                String[] genresS = genres.split(",");

                Collections.addAll(ls, genresS);

                tvSeries.add(new Serial(original_title, null, overview, vote_average,
                        voteCount, voteScore, release_date, language, id, ls));
                imageBlobs.add(imagePoster);
            }
            while ((cursor.moveToNext()));
        }
        displayTvSeries(tvSeries, imageBlobs);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
