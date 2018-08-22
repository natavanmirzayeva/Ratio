package com.project.udacity.ratio.ui.categories.tabs;


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
import com.project.udacity.ratio.MainActivity;
import com.project.udacity.ratio.R;
import com.project.udacity.ratio.data.CollectionApi;
import com.project.udacity.ratio.data.MovieResponse;
import com.project.udacity.ratio.data.db.CollectionContract;
import com.project.udacity.ratio.models.Movie;
import com.project.udacity.ratio.ui.categories.detailscreens.MovieDetailActivity;
import com.project.udacity.ratio.util.ApiVariables;
import com.project.udacity.ratio.util.Global;
import com.project.udacity.ratio.util.InternetCheck;
import com.project.udacity.ratio.util.MovieAdapter;

import java.util.ArrayList;
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
public class MovieFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.movies_recycler_view)
    RecyclerView recyclerView;

    private MovieAdapter moviesAdapter;

    private static final String RECYCLERVIEW_STATE = "recyclerview-state-1";
    private static final String RECYCLERVIEW_STATE_ADAPTER = "recyclerview-state-adapter";

    Parcelable savedRecyclerLayoutState;

    GridLayoutManager gridLayoutManager;
    public boolean isRatioCollection;
    public boolean isTopRated;
    public boolean isOfflineData;
    public boolean isOfflineTopRated;
    public boolean isSearch;

    private static final int ID_MOVIE_LOADER = 44;
    private static final int ID_RATE_LOADER = 45;

    private int topRatedResultLimit = 3;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference movieRef = database.getReference(CollectionContract.CollectionEntry.MOVIE_TABLE_NAME);

    public static MovieFragment newInstance() {
        Bundle args = new Bundle();
        MovieFragment fragment = new MovieFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_movie, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        isRatioCollection = getArguments().getBoolean("isRatioCollection");
        isTopRated = getArguments().getBoolean("isTopRated");
        isSearch = getArguments().getBoolean("isSearch",false);
        isOfflineData = false;
        isOfflineTopRated = false;

        if (isRatioCollection) {
            getLoaderManager().initLoader(ID_RATE_LOADER, null, MovieFragment.this);
        } else {
            new InternetCheck(getContext()).isInternetConnectionAvailable(new InternetCheck.InternetCheckListener() {

                @Override
                public void onComplete(final boolean connected) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (connected) {
                                if (isTopRated) {
                                    getTopRatedMoviesFromFirebase();
                                } else if (isSearch) {
                                    List<Movie> movies = getArguments().getParcelableArrayList("movies");
                                    displayMovies(movies, null);

                                } else {
                                    getMoviesFromApi();
                                }

                            } else {
                                if (isTopRated) {
                                    isOfflineTopRated = true;
                                    getLoaderManager().initLoader(ID_RATE_LOADER, null, MovieFragment.this);
                                } else {
                                    isOfflineData = true;
                                    getLoaderManager().initLoader(ID_MOVIE_LOADER, null, MovieFragment.this);
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

    private void getTopRatedMoviesFromFirebase() {
        movieRef.orderByChild("voteCount").limitToLast(topRatedResultLimit).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Movie> movies = new ArrayList<>();

                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        movies.add(data.getValue(Movie.class));
                    }
                    displayMovies(movies, null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("dbError", databaseError.getMessage().toString());
            }
        });

    }

    private void getMoviesFromApi() {
        OkHttpClient httpClient = new OkHttpClient.Builder().build();
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(ApiVariables.movieUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = builder.client(httpClient).build();
        CollectionApi moviesApi = retrofit.create(CollectionApi.class);
        io.reactivex.Observable<MovieResponse> call = moviesApi.listMovies(ApiVariables.movieApiKey).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        call.map(new Function<MovieResponse, List<Movie>>() {
            @Override
            public List<Movie> apply(MovieResponse movieResponse) throws Exception {
                return movieResponse.getMovies();
            }
        })
                .subscribe
                        (
                                new Consumer<List<Movie>>() {
                                    @Override
                                    public void accept(List<Movie> movies) throws Exception {
                                        displayMovies(movies, null);
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

    private void displayMovies(List<Movie> movies, List<byte[]> imageBlobs) {
        moviesAdapter = new MovieAdapter(movies, imageBlobs, isRatioCollection, isOfflineData,
                isOfflineTopRated, getContext(), new MovieAdapter.OnItemClickListener() {
            public void onItemClick(Movie movie, byte[] image, ImageView movieImage) {
                Global.image = image;

                Intent movieIntent = new Intent(getContext(), MovieDetailActivity.class);
                movieIntent.putExtra("movie", movie);
                movieIntent.putExtra("userId", MainActivity.USER_ID);


                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(getActivity(), movieImage, ViewCompat.getTransitionName(movieImage));
                startActivity(movieIntent, options.toBundle());

            }
        });


        recyclerView.setAdapter(moviesAdapter);
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
        if (moviesAdapter != null) {
            List<Movie> movie = moviesAdapter.getMovies();
            if (movie != null && !movie.isEmpty()) {
                outState.putParcelableArrayList(RECYCLERVIEW_STATE_ADAPTER, (ArrayList<? extends Parcelable>) movie);
            }
        }

    }

    @Override
    public void onViewStateRestored(@Nullable final Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            savedRecyclerLayoutState = savedInstanceState.getParcelable(RECYCLERVIEW_STATE);
            if (savedInstanceState.containsKey(RECYCLERVIEW_STATE_ADAPTER)) {
                List<Movie> movieResultList = savedInstanceState.getParcelableArrayList(RECYCLERVIEW_STATE_ADAPTER);
                moviesAdapter.setMovies(movieResultList);
                recyclerView.setAdapter(moviesAdapter);
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
            String arg[] = {"1", MainActivity.USER_ID, "1"};
            String selection = "isMyCollection=? AND userId=? AND collectionType=?";
            // getLoaderManager().initLoader(ID_RATE_LOADER, null,MovieFragment.this);
            Uri uri = CollectionContract.CollectionEntry.CONTENT_URI_Rate;
            return new CursorLoader(
                    getContext(),
                    uri,
                    null,
                    selection,
                    arg,
                    null);


            // getMoviesFromLocal(args, selection, uri);
        } else if (isTopRated && isOfflineTopRated) {
            String arg[] = {"1", MainActivity.USER_ID, "1"};
            String selection = "isTopRated=? AND userId=? AND collectionType=?";
            Uri uri = CollectionContract.CollectionEntry.CONTENT_URI_Rate;
            return new CursorLoader(
                    getContext(),
                    uri,
                    null,
                    selection,
                    arg,
                    null);
        } else if (loaderId == ID_MOVIE_LOADER && isOfflineData) {
            Uri uri = CollectionContract.CollectionEntry.CONTENT_URI_Movies;
            return new CursorLoader(
                    getContext(),
                    uri,
                    null,
                    null,
                    null,
                    null);

        }

       /* Cursor cursor = getContext().getContentResolver().query(
                uri,
                null,
                null,
                null,
                null);*/

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        List<Movie> movies = new ArrayList<>();
        List<byte[]> imageBlobs = new ArrayList<>();
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

                movies.add(new Movie("", "",
                        original_title, release_date, language, vote_average, voteCount,
                        voteScore, overview, id));
                imageBlobs.add(imagePoster);
            }
            while ((cursor.moveToNext()));
        }
        displayMovies(movies, imageBlobs);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


}
