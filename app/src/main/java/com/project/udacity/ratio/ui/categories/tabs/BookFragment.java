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
import com.project.udacity.ratio.MainActivity;
import com.project.udacity.ratio.R;
import com.project.udacity.ratio.data.BookResponse;
import com.project.udacity.ratio.data.CollectionApi;
import com.project.udacity.ratio.data.db.CollectionContract;
import com.project.udacity.ratio.models.Book;
import com.project.udacity.ratio.models.Items;
import com.project.udacity.ratio.ui.categories.detailscreens.BookDetailActivity;
import com.project.udacity.ratio.util.ApiVariables;
import com.project.udacity.ratio.util.BookAdapter;
import com.project.udacity.ratio.util.Global;
import com.project.udacity.ratio.util.InternetCheck;

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
public class BookFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    @BindView(R.id.bookRecycler)
    RecyclerView recyclerView;

    @SuppressLint("StaticFieldLeak")
    private static BookAdapter booksAdapter;

    private static final String RECYCLERVIEW_STATE = "recyclerview-state-1";
    private static final String RECYCLERVIEW_STATE_ADAPTER = "recyclerview-state-adapter";

    Parcelable savedRecyclerLayoutState;
    GridLayoutManager gridLayoutManager;

    public boolean isRatioCollection;
    public boolean isTopRated;
    public boolean isSearch;
    public boolean isOfflineData;
    public boolean isOfflineTopRated;

    private int topRatedResultLimit = 3;

    private static final int ID_BOOK_LOADER = 46;
    private static final int ID_RATE_LOADER = 45;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference bookRef = database.getReference(CollectionContract.CollectionEntry.BOOK_TABLE_NAME);

    public static BookFragment newInstance() {
        Bundle args = new Bundle();
        BookFragment fragment = new BookFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_book, container, false);
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
            getLoaderManager().initLoader(ID_RATE_LOADER, null, BookFragment.this);
        } else {
            new InternetCheck(getContext()).isInternetConnectionAvailable(new InternetCheck.InternetCheckListener() {

                @Override
                public void onComplete(final boolean connected) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (connected) {
                                if (isTopRated) {
                                    getTopRatedBooksFromFirebase();
                                } else if (isSearch) {
                                    List<Book> books = getArguments().getParcelableArrayList("books");
                                    displayBooks(books, null);
                                } else {
                                    getBooksFromApi();
                                }

                            } else {
                                if (isTopRated) {
                                    isOfflineTopRated = true;
                                    getLoaderManager().initLoader(ID_RATE_LOADER, null, BookFragment.this);
                                } else {
                                    isOfflineData = true;
                                    getLoaderManager().initLoader(ID_BOOK_LOADER, null, BookFragment.this);
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

    private void getTopRatedBooksFromFirebase() {
        bookRef.orderByChild("voteCount").limitToLast(topRatedResultLimit).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ArrayList<Book> books = new ArrayList<>();

                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        books.add(data.getValue(Book.class));
                    }
                    displayBooks(books, null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("dbError", databaseError.getMessage());
            }
        });

    }


    private void getBooksFromApi() {
        OkHttpClient httpClient = new OkHttpClient.Builder().build();
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(ApiVariables.bookUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.client(httpClient).build();

        CollectionApi booksApi = retrofit.create(CollectionApi.class);
        io.reactivex.Observable<BookResponse> call = booksApi
                .listBooks(ApiVariables.bookApiKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        call.map(new Function<BookResponse, List<Items>>() {
            @Override
            public List<Items> apply(BookResponse bookResponse) throws Exception {
                return bookResponse.getItems();
            }
        })
                .subscribe
                        (
                                new Consumer<List<Items>>() {
                                    @Override
                                    public void accept(List<Items> items) throws Exception {

                                        ArrayList<Book> books = new ArrayList<>();

                                        for (Items i : items) {
                                            books.add(i.getBook());
                                        }
                                        displayBooks(books, null);


                                    }
                                },
                                new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
                                        Log.i("error", "RxJava2, HTTP Error: " + throwable.getLocalizedMessage());
                                    }
                                }
                        );
    }


    private void displayBooks(List<Book> bookList, List<byte[]> imageBlobs) {
        booksAdapter = new BookAdapter(bookList, imageBlobs, isRatioCollection, isOfflineData, isOfflineTopRated, getContext(), new BookAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Book book, byte[] image, ImageView bookImage) {
                Global.image = image;
                Intent bookIntent = new Intent(getContext(), BookDetailActivity.class);
                bookIntent.putExtra("book", book);
                bookIntent.putExtra("userId", MainActivity.USER_ID);

                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(getActivity(), bookImage, ViewCompat.getTransitionName(bookImage));
                startActivity(bookIntent, options.toBundle());
            }
        });

        recyclerView.setAdapter(booksAdapter);
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
        if (booksAdapter != null) {
            List<Book> books = booksAdapter.getBooks();
            if (books != null && !books.isEmpty()) {
                outState.putParcelableArrayList(RECYCLERVIEW_STATE_ADAPTER, (ArrayList<? extends Parcelable>) books);
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
                List<Book> bookResultList = savedInstanceState.getParcelableArrayList(RECYCLERVIEW_STATE_ADAPTER);
                booksAdapter.setBooks(bookResultList);
                recyclerView.setAdapter(booksAdapter);
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
            String arg[] = {"1", MainActivity.USER_ID, "2"};
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
            String arg[] = {"1", MainActivity.USER_ID, "2"};
            String selection = "isTopRated=? AND userId=? AND collectionType=?";
            Uri uri = CollectionContract.CollectionEntry.CONTENT_URI_Rate;
            return new CursorLoader(
                    getContext(),
                    uri,
                    null,
                    selection,
                    arg,
                    null);
        } else if (loaderId == ID_BOOK_LOADER && isOfflineData) {
            Uri uri = CollectionContract.CollectionEntry.CONTENT_URI_BOOKS;
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
        List<Book> books = new ArrayList<>();
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
                index = cursor.getColumnIndexOrThrow("authors");
                String author = cursor.getString(index);

                List<String> ls = new ArrayList<>();
                String[] authors = author.split(",");

                Collections.addAll(ls, authors);

                books.add(new Book(original_title, null, overview, vote_average, voteCount, voteScore,
                        release_date, language, id, ls));
                imageBlobs.add(imagePoster);
            }
            while ((cursor.moveToNext()));
        }
        displayBooks(books, imageBlobs);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
