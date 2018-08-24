package com.project.udacity.ratio.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.project.udacity.ratio.data.BookResponse;
import com.project.udacity.ratio.data.CollectionApi;
import com.project.udacity.ratio.data.MovieResponse;
import com.project.udacity.ratio.data.db.CollectionContract;
import com.project.udacity.ratio.models.Book;
import com.project.udacity.ratio.models.Items;
import com.project.udacity.ratio.models.Movie;
import com.project.udacity.ratio.models.Serial;
import com.project.udacity.ratio.util.ApiVariables;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by mehseti on 8.8.2018.
 */


public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private final int MAX_CACHE_ITEM_COUNT = 10;
    private final int MAX_CACHE_RATE_ITEM_COUNT = 3;
    private final int SYNC_START_INDEX = 4;
    private ContentResolver mContentResolver;
    private Context mContext;
    private int loadedImageCount;
    private int collectionSize;
    private String userId;
    private List<Items> bookList;
    private List<Movie> movieList;
    private List<Serial> serialList;
    private List<byte[]> imageBlobs;
    private Uri tableUri;

    private DatabaseReference bookRef, movieRef, serialRef, rateRef, currenRef;

    public enum operation {
        GET_BOOKS,
        GET_MOVIES,
        GET_TV_SERIES,
        UPDATE_RATING,
        GET_TOP_RATED
    }

    private SharedPreferences getGlobalPrefs(Context context) {
        return mContext.getSharedPreferences(context.getPackageName() + "_prefs", 0);
    }

    private operation syncOperation;

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContext = context;
        mContentResolver = context.getContentResolver();

    }

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();

        setUserId();

        FirebaseApp.initializeApp(context);
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        bookRef = database.getReference(CollectionContract.CollectionEntry.BOOK_TABLE_NAME);
        movieRef = database.getReference(CollectionContract.CollectionEntry.MOVIE_TABLE_NAME);
        serialRef = database.getReference(CollectionContract.CollectionEntry.SERIAL_TABLE_NAME);
        rateRef = database.getReference(CollectionContract.CollectionEntry.RATE_TABLE_NAME);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        imageBlobs = new ArrayList<>();

        for (int i = 0; i < MAX_CACHE_ITEM_COUNT; i++) {
            imageBlobs.add(null);
        }

        loadedImageCount = 0;
        int tmpNext = 1;
        switch (getNextOperationNo()) {
            case 1:
                Log.d("adaptersync:", "books");
                currenRef = bookRef;
                tableUri = CollectionContract.CollectionEntry.CONTENT_URI_BOOKS;
                syncOperation = operation.GET_BOOKS;
                getBooks();
                tmpNext = 2;
                break;
            case 2:
                Log.d("adaptersync:", "movies");
                currenRef = movieRef;
                tableUri = CollectionContract.CollectionEntry.CONTENT_URI_Movies;
                syncOperation = operation.GET_MOVIES;
                getMovies();
                tmpNext = 3;
                break;
            case 3:
                Log.d("adaptersync:", "tv series");
                currenRef = serialRef;
                tableUri = CollectionContract.CollectionEntry.CONTENT_URI_TVSeries;
                syncOperation = operation.GET_TV_SERIES;
                getTvSeries();
                tmpNext = 4;
                break;
            case 4:
                Log.d("adaptersync:", "rating");
                currenRef = rateRef;
                tableUri = CollectionContract.CollectionEntry.CONTENT_URI_Rate;
                syncOperation = operation.UPDATE_RATING;
                getUserRatedCollections();
                tmpNext = 5;
                break;
            case 5:
                Log.d("adaptersync:", "top_rated");
                currenRef = rateRef;
                tableUri = CollectionContract.CollectionEntry.CONTENT_URI_Rate;
                syncOperation = operation.GET_TOP_RATED;
                getTopRatedBooksFromFirebase();
                tmpNext = 1;
                break;
        }

        setNextOperation(tmpNext);

    }

    private synchronized int getNextOperationNo() {
        SharedPreferences sharedPreferences = getGlobalPrefs(mContext);
        return sharedPreferences.getInt("nextOperationNo", SYNC_START_INDEX);
    }

    private synchronized void setNextOperation(int next) {
        SharedPreferences sharedPreferences = getGlobalPrefs(mContext);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putInt("nextOperationNo", next);
        edit.commit();
    }

    private void getBooks() {
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
                        (new Consumer<List<Items>>() {
                             @Override
                             public void accept(List<Items> items) throws Exception {
                                 bookList = items;
                                 Collections.shuffle(bookList);

                                 collectionSize = items.size() > MAX_CACHE_ITEM_COUNT ? MAX_CACHE_ITEM_COUNT : items.size();

                                 getCollectionImageBlobs();
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

    private void getMovies() {
        OkHttpClient httpClient = new OkHttpClient.Builder().build();
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(ApiVariables.movieUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.client(httpClient).build();
        CollectionApi moviesApi = retrofit.create(CollectionApi.class);

        io.reactivex.Observable<MovieResponse> call = moviesApi
                .listMovies(ApiVariables.movieApiKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

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
                                        movieList = movies;
                                        Collections.shuffle(movieList);

                                        collectionSize = movies.size() > MAX_CACHE_ITEM_COUNT ? MAX_CACHE_ITEM_COUNT : movies.size();

                                        getCollectionImageBlobs();
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


    private void getTvSeries() {
        OkHttpClient httpClient = new OkHttpClient.Builder().build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(ApiVariables.serialUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(GsonConverterFactory.create(gson));

        Retrofit retrofit = builder.client(httpClient).build();
        CollectionApi tvSeriesApi = retrofit.create(CollectionApi.class);

        io.reactivex.Observable<JsonArray> call = tvSeriesApi
                .listTvSeries()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

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
                                        serialList = serials;
                                        Collections.shuffle(serialList);

                                        collectionSize = serials.size() > MAX_CACHE_ITEM_COUNT ? MAX_CACHE_ITEM_COUNT : serials.size();

                                        getCollectionImageBlobs();
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

    private void getCollectionImageBlobs() {
        String collectionImageUrl = null;

        for (int i = 0; i < collectionSize; i++) {
            switch (syncOperation) {
                case GET_BOOKS:
                    collectionImageUrl = bookList.get(i).getBook().getPosterPath().getSmallThumbnail();
                    break;
                case GET_MOVIES:
                    collectionImageUrl = ApiVariables.imagePathForMovie + movieList.get(i).getPosterPath();
                    break;
                case GET_TV_SERIES:
                    collectionImageUrl = serialList.get(i).getPosterPath().getSmallThumbnail();
                    break;
            }

            Picasso.Builder picassoBuilder = new Picasso.Builder(mContext);
            Picasso picasso = picassoBuilder.build();

            final ImageView tempImage = new ImageView(mContext);
            final int finalI = i;
            picasso.load(collectionImageUrl).into(tempImage, new Callback() {
                @Override
                public void onSuccess() {
                    loadedImageCount++;
                    BitmapDrawable drawable = (BitmapDrawable) tempImage.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();
                    byte[] image = getBytes(bitmap);

                    imageBlobs.set(finalI, image);

                    if (loadedImageCount == collectionSize) {
                        deleteAllCollectionRecords();
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.i("SYNCIMAGE", e.getLocalizedMessage());
                }
            });
        }
    }


    private void getVoteIfExistOnFirebase(final Object o, final int index) {

        String collectionId = "";
        switch (syncOperation) {
            case GET_BOOKS:
                collectionId = ((Book) o).getId();
                break;
            case GET_MOVIES:
                collectionId = ((Movie) o).getId();
                break;
            case GET_TV_SERIES:
                collectionId = ((Serial) o).getId();
                break;
        }

        currenRef.orderByChild("id").equalTo(collectionId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double voteAverage = 0.f;
                int voteCount = 0;
                if (dataSnapshot.exists()) {

                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        voteAverage = data.child(CollectionContract.CollectionEntry.COLUMN_VOTE_AVERAGE).getValue(Float.class);
                        voteCount = data.child(CollectionContract.CollectionEntry.COLUMN_VOTE_COUNT).getValue(Integer.class);
                    }
                }

                switch (syncOperation) {
                    case GET_BOOKS:
                        Book b = ((Book) o);
                        b.setVoteCount(voteCount);
                        b.setVoteAverage(voteAverage);
                        addBook(b, index);
                        break;
                    case GET_MOVIES:
                        Movie m = ((Movie) o);
                        m.setVoteCount(voteCount);
                        m.setVoteAverage(voteAverage);
                        addMovie(m, index);
                        break;
                    case GET_TV_SERIES:
                        Serial s = ((Serial) o);
                        s.setVoteCount(voteCount);
                        s.setVoteAverage(voteAverage);
                        addSerial(s, index);
                        break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
                switch (syncOperation) {
                    case GET_BOOKS:
                        addBook(((Book) o), index);
                        break;
                    case GET_MOVIES:
                        addMovie(((Movie) o), index);
                        break;
                    case GET_TV_SERIES:
                        addSerial(((Serial) o), index);
                        break;
                }
            }
        });
    }

    private void addCollectionItems() {
        for (int i = 0; i < collectionSize; i++) {
            switch (syncOperation) {
                case GET_BOOKS:
                    getVoteIfExistOnFirebase(bookList.get(i).getBook(), i);
                    break;
                case GET_MOVIES:
                    getVoteIfExistOnFirebase(movieList.get(i), i);
                    break;
                case GET_TV_SERIES:
                    getVoteIfExistOnFirebase(serialList.get(i), i);
                    break;
            }
        }
    }


    private void addSerial(Serial serial, int index) {

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < serial.getGenres().size(); i++) {
            if (i == serial.getGenres().size() - 1) {
                stringBuilder.append(serial.getGenres().get(i));
            } else {
                stringBuilder.append(serial.getGenres().get(i)).append(", ");
            }
        }

        String genres = stringBuilder.toString();

        if (serial.getDescription() == null) {
            serial.setDescription("");
        }

        setAndInsertCollection(
                serial.getId(),
                serial.getVoteCount(),
                serial.getVoteAverage(),
                serial.getTitle(),
                imageBlobs.get(index),
                serial.getDescription(),
                serial.getReleaseDate(),
                serial.getLanguage(),
                null,
                genres);
    }

    private void addMovie(Movie movie, int index) {
        if (movie.getDescription() == null) {
            movie.setDescription("");
        }
        setAndInsertCollection(
                movie.getId(),
                movie.getVoteCount(),
                movie.getVoteAverage(),
                movie.getTitle(),
                imageBlobs.get(index),
                movie.getDescription(),
                movie.getReleaseDate(),
                movie.getLanguage(),
                null,
                null);
    }


    private void addBook(Book book, int index) {
        StringBuilder stringBuilder = new StringBuilder();
        if (book.getAuthors() != null) {
            for (int i = 0; i < book.getAuthors().size(); i++) {
                if (i == book.getAuthors().size() - 1) {
                    stringBuilder.append(book.getAuthors().get(i));
                } else {
                    stringBuilder.append(book.getAuthors().get(i)).append(", ");
                }
            }
        }
        String authors = stringBuilder.toString();

        if (book.getDescription() == null) {
            book.setDescription("");
        }

        setAndInsertCollection(
                book.getId(),
                book.getVoteCount(),
                book.getVoteAverage(),
                book.getTitle(),
                imageBlobs.get(index),
                book.getDescription(),
                book.getReleaseDate(),
                book.getLanguage(),
                authors,
                null);
    }


    private void setAndInsertCollection(String id, int voteCount, double voteAverage, String title,
                                        byte[] imagePoster, String description, String releaseDate,
                                        String language, String authors, String genres) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(CollectionContract.CollectionEntry.id, id);
        contentValues.put(CollectionContract.CollectionEntry.COLUMN_VOTE_COUNT, voteCount);
        contentValues.put(CollectionContract.CollectionEntry.COLUMN_VOTE_SCORE, 0);
        contentValues.put(CollectionContract.CollectionEntry.COLUMN_VOTE_AVERAGE, voteAverage);
        contentValues.put(CollectionContract.CollectionEntry.COLUMN_ORIGINAL_TITLE, title);
        contentValues.put(CollectionContract.CollectionEntry.COLUMN_POSTER_PATH, imagePoster);
        contentValues.put(CollectionContract.CollectionEntry.COLUMN_DESCRIPTION, description);
        contentValues.put(CollectionContract.CollectionEntry.COLUMN_RELEASE_DATE, releaseDate);
        contentValues.put(CollectionContract.CollectionEntry.COLUMN_LANGUAGE, language);

        switch (syncOperation) {
            case GET_BOOKS:
                contentValues.put(CollectionContract.CollectionEntry.COLUMN_AUTHORS, authors);
                break;
            case GET_TV_SERIES:
                contentValues.put(CollectionContract.CollectionEntry.COLUMN_GENRES, genres);
                break;
        }

        mContentResolver.insert(tableUri, contentValues);
    }

    private void deleteAllCollectionRecords() {
        try {
            mContentResolver.delete(
                    tableUri,
                    null,
                    null);
        } finally {
            addCollectionItems();
        }
    }


    private void getUserRatedCollections() {
        currenRef.orderByChild("userId").equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {

                        long type = data.child("type").getValue(Long.class);
                        String id = data.child("collectionId").getValue(String.class);
                        int vote = data.child("vote").getValue(Integer.class);

                        DatabaseReference ref = null;
                        switch ((int) type) {
                            case 1:
                                ref = movieRef;
                                break;
                            case 2:
                                ref = bookRef;
                                break;
                            case 3:
                                ref = serialRef;
                                break;
                        }
                        assert ref != null;
                        getCollectionFromFirebase(ref, id, vote, (int) type);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    private void getCollectionFromFirebase(DatabaseReference ref, final String id,
                                           final int vote, final int collectionType) {
        ref.orderByChild("id").equalTo(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (DataSnapshot data : dataSnapshot.getChildren()) {

                        addCollectionLocalRateTable(data, vote, collectionType, false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    private void getTopRatedBooksFromFirebase() {

        deleteTopRatedCollection();

        addTopRatedCollections(movieRef, 1);
        addTopRatedCollections(bookRef, 2);
        addTopRatedCollections(serialRef, 3);
    }

    private void addTopRatedCollections(DatabaseReference ref, final int collectionType) {

        ref.orderByChild("voteAverage").limitToLast(MAX_CACHE_RATE_ITEM_COUNT).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (final DataSnapshot data : dataSnapshot.getChildren()) {

                        String ratingId = userId + "_" + data.child("id").getValue(String.class);

                        rateRef.orderByChild("userIdCollectionId").equalTo(ratingId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                int vote = 0;
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                                        vote = data.child("vote").getValue(Integer.class);
                                    }
                                }

                                addCollectionLocalRateTable(data, vote, collectionType, true);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                System.out.println("The read failed: " + databaseError.getCode());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("dbError", databaseError.getMessage());
            }
        });
    }

    private void deleteTopRatedCollection() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CollectionContract.CollectionEntry.COLUMN_IS_TOP_RATED, 0);

        mContentResolver.update(
                tableUri,
                contentValues,
                CollectionContract.CollectionEntry.COLUMN_IS_MY_COLLECTION + " = 1",
                null);

        mContentResolver.delete(
                tableUri,
                CollectionContract.CollectionEntry.COLUMN_USER_ID + " = ? AND "
                        + CollectionContract.CollectionEntry.COLUMN_IS_MY_COLLECTION + " = 0",
                new String[]{userId});
    }


    private void addCollectionLocalRateTable(DataSnapshot data, int vote,
                                             final int collectionType, final boolean isTopRated) {

        final String id = data.child("id").getValue(String.class);

        final int voteAverage = data.child("voteAverage").getValue(Integer.class);
        final int voteCount = data.child("voteCount").getValue(Integer.class);
        final int voteScore = data.child("voteScore").getValue(Integer.class);

        final ContentValues contentValues = new ContentValues();
        contentValues.put(CollectionContract.CollectionEntry.COLUMN_VOTE, vote);
        contentValues.put(CollectionContract.CollectionEntry.COLUMN_VOTE_COUNT, voteCount);
        contentValues.put(CollectionContract.CollectionEntry.COLUMN_VOTE_SCORE, voteScore);
        contentValues.put(CollectionContract.CollectionEntry.COLUMN_VOTE_AVERAGE, voteAverage);
        contentValues.put(CollectionContract.CollectionEntry.COLUMN_USER_ID, userId);

        if (!checkExistVoteOnLocal(id)) {
            String imageLinkSmall = "";
            String genres = "";
            String author = "";

            switch (collectionType) {
                case 1:
                    imageLinkSmall = ApiVariables.imagePathForMovie + data.child("posterPath").getValue(String.class);
                    break;
                case 2:
                    if (data.child("authors").getChildren().iterator().hasNext()) {
                        author += data.child("authors").getChildren().iterator().next().getValue(String.class);
                    }
                    imageLinkSmall = data.child("posterPath").child("smallThumbnail").getValue(String.class);
                    break;
                case 3:
                    if (data.child("genres").getChildren().iterator().hasNext()) {
                        genres += data.child("genres").getChildren().iterator().next().getValue(String.class);
                    }
                    imageLinkSmall = data.child("posterPath").child("smallThumbnail").getValue(String.class);
                    break;
            }

            final String title = data.child("title").getValue(String.class);
            final String releaseDate = data.child("releaseDate").getValue(String.class);
            final String description = data.child("description").getValue(String.class);
            final String language = data.child("language").getValue(String.class);

            Picasso.Builder picassoBuilder = new Picasso.Builder(mContext);
            Picasso picasso = picassoBuilder.build();

            final String finalAuthor = author;
            final String finalGenres = genres;
            final ImageView tempImage = new ImageView(mContext);

            picasso.load(imageLinkSmall).into(tempImage, new Callback() {
                @Override
                public void onSuccess() {
                    BitmapDrawable drawable = (BitmapDrawable) tempImage.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();
                    byte[] image = getBytes(bitmap);

                    contentValues.put(CollectionContract.CollectionEntry.id, id);
                    contentValues.put(CollectionContract.CollectionEntry.COLUMN_ORIGINAL_TITLE, title);
                    contentValues.put(CollectionContract.CollectionEntry.COLUMN_POSTER_PATH, image);
                    contentValues.put(CollectionContract.CollectionEntry.COLUMN_DESCRIPTION, description);
                    contentValues.put(CollectionContract.CollectionEntry.COLUMN_BACKDROP_PATH, image);
                    contentValues.put(CollectionContract.CollectionEntry.COLUMN_RELEASE_DATE, releaseDate);
                    contentValues.put(CollectionContract.CollectionEntry.COLUMN_LANGUAGE, language);
                    contentValues.put(CollectionContract.CollectionEntry.COLUMN_COLLECTION_TYPE, collectionType);
                    contentValues.put(CollectionContract.CollectionEntry.COLUMN_AUTHORS, finalAuthor);
                    contentValues.put(CollectionContract.CollectionEntry.COLUMN_GENRES, finalGenres);

                    if (isTopRated) {
                        contentValues.put(CollectionContract.CollectionEntry.COLUMN_IS_MY_COLLECTION, 0);
                        contentValues.put(CollectionContract.CollectionEntry.COLUMN_IS_TOP_RATED, 1);
                    } else {
                        contentValues.put(CollectionContract.CollectionEntry.COLUMN_IS_MY_COLLECTION, 1);
                        contentValues.put(CollectionContract.CollectionEntry.COLUMN_IS_TOP_RATED, 0);
                    }

                    mContentResolver.insert(tableUri, contentValues);
                }

                @Override
                public void onError(Exception e) {

                }
            });

        } else {
            if (isTopRated) {
                contentValues.put(CollectionContract.CollectionEntry.COLUMN_IS_TOP_RATED, 1);
            } else {
                contentValues.put(CollectionContract.CollectionEntry.COLUMN_IS_MY_COLLECTION, 1);
            }

            mContentResolver.update(
                    tableUri,
                    contentValues,
                    CollectionContract.CollectionEntry.id + "=?",
                    new String[]{String.valueOf(id)});
        }
    }

    private void setUserId() {
        Cursor mCursor = mContentResolver.query(
                CollectionContract.CollectionEntry.CONTENT_URI_Users,  // The content URI of the words table
                null,                       // The columns to return for each row
                CollectionContract.CollectionEntry.COLUMN_ACTIVE + " = 1",             // Either null, or the word the user entered
                null,                    // Either empty, or the string the user entered
                null);

        assert mCursor != null;
        if (mCursor.moveToFirst()) {
            do {
                int index = mCursor.getColumnIndexOrThrow(CollectionContract.CollectionEntry.id);
                userId = mCursor.getString(index);
            }
            while ((mCursor.moveToNext()));
        }
        mCursor.close();
    }

    private boolean checkExistVoteOnLocal(String id) {
        Cursor cursor = mContentResolver.query(
                CollectionContract.CollectionEntry.CONTENT_URI_Rate,
                null,
                "id = ?",
                new String[]{String.valueOf(id)},
                null);
        assert cursor != null;
        boolean result = cursor.moveToFirst();
        cursor.close();
        return result;

    }

    private byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
}
