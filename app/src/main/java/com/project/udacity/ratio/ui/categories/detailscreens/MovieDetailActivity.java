package com.project.udacity.ratio.ui.categories.detailscreens;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.udacity.ratio.R;
import com.project.udacity.ratio.data.db.CollectionContract;
import com.project.udacity.ratio.models.Movie;
import com.project.udacity.ratio.util.ApiVariables;
import com.project.udacity.ratio.util.Global;
import com.project.udacity.ratio.util.InternetCheck;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.willy.ratingbar.ScaleRatingBar;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieDetailActivity extends AppCompatActivity {

    @BindView(R.id.background_img)
    ImageView movieBackgroundImage;

    @BindView(R.id.description_txt)
    TextView descriptionTxt;

    @BindView(R.id.title_txt)
    TextView titleTxt;

    @BindView(R.id.releaseDate_txt)
    TextView releaseDateTxt;

    @BindView(R.id.language_txt)
    TextView languageTxt;

    @BindView(R.id.vote_average_text)
    TextView voteAverageText;

    @BindView(R.id.vote_count_text)
    TextView voteCountText;

    @BindView(R.id.movie_toolbar)
    Toolbar movieToolbar;

    @BindView(R.id.vote_fab)
    FloatingActionButton voteFab;

    @BindView(R.id.ratingBar)
    ScaleRatingBar ratingBar;

    @BindView(R.id.root)
    CoordinatorLayout root;

    @BindView(R.id.your_vote_container)
    LinearLayout yourVoteContainer;

    @BindView(R.id.your_vote)
    TextView yourVoteText;

    @BindView(R.id.rating)
    RelativeLayout rating;

    @BindView(R.id.adView)
    AdView mAdView;

    @BindView(R.id.ads_text)
    TextView adsText;

    @BindView(R.id.ads_container)
    LinearLayout adsContainer;


    Intent intent;
    final boolean[] moviesExists = new boolean[1], isUserVoted = new boolean[1];
    private String movieId;
    private Movie movie;
    private String userId;
    private int oldVote;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference movieRef = database.getReference(CollectionContract.CollectionEntry.MOVIE_TABLE_NAME);
    private DatabaseReference rateRef = database.getReference(CollectionContract.CollectionEntry.RATE_TABLE_NAME);

    String voteCountName = CollectionContract.CollectionEntry.COLUMN_VOTE_COUNT;
    String voteScoreName = CollectionContract.CollectionEntry.COLUMN_VOTE_SCORE;
    String voteAverageName = CollectionContract.CollectionEntry.COLUMN_VOTE_AVERAGE;
    String voteName = CollectionContract.CollectionEntry.COLUMN_VOTE;

    public enum voteOperation {
        ADD,
        UPDATE,
        DELETE
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        ButterKnife.bind(this);

        setToolbar();

        intent = getIntent();
        movie = intent.getParcelableExtra("movie");
        userId = intent.getStringExtra("userId");
        movieId = movie.getId();

        setMovieUi();

        voteFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showVoteDialog();
            }
        });

        new InternetCheck(getApplicationContext()).isInternetConnectionAvailable(new InternetCheck.InternetCheckListener() {

            @Override
            public void onComplete(final boolean connected) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (connected) {
                            checkMovieExistsOnFirebase();
                            new LoadAds().execute();
                        } else {
                            checkMovieExistsOnLocal();
                            adsContainer.setVisibility(View.GONE);
                            adsText.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

    }

    /*
     *   Toolbar operations
     *
     */
    private void setToolbar() {
        if (movieToolbar != null) {
            setSupportActionBar(movieToolbar);
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        movieToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MovieDetailActivity.super.onBackPressed();
                supportFinishAfterTransition();

            }
        });
    }

    /*
     *   Movie attributes setting
     *
     */
    private void setMovieUi() {
        movieBackgroundImage.setContentDescription(movie.getTitle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            movieBackgroundImage.setTransitionName(movie.getTitle());
        }

        if (movie.getPosterPath() == null || movie.getPosterPath().equals("")) {

            if (Global.image != null) {
                Drawable image = new BitmapDrawable(
                        getResources(),
                        BitmapFactory.decodeByteArray(Global.image,
                                0,
                                Global.image.length)
                );
                voteFab.setVisibility(View.VISIBLE);
                movieBackgroundImage.setImageDrawable(image);
            }
        } else {
            Picasso.get().load(ApiVariables.imagePathForMovie + movie.getPosterPath()).into(movieBackgroundImage, new Callback() {
                @Override
                public void onSuccess() {
                    voteFab.setVisibility(View.VISIBLE);
                    scheduleStartPostponedTransition(movieBackgroundImage);
                }

                @Override
                public void onError(Exception e) {
                }
            });
        }

        descriptionTxt.setText(movie.getDescription());
        titleTxt.setText(movie.getTitle());
        releaseDateTxt.setText(movie.getReleaseDate());
        languageTxt.setText(movie.getLanguage());
    }

    /*
     * Ads initialize
     *
     */
    private void initializeAds() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        mAdView.loadAd(adRequest);
    }

    /*
     * Fab butonuna tıklandığında kullanıcın movie'yi oylaması için
     * bir dialog açıyor. Eğer movie yoksa firebase'e ekliyor. Eğer varsa
     * kullanıcı daha önce oylamışsa oyunu güncelliyor. Yoksa yeni oy ekliyor
     *
     * */
    private void showVoteDialog() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(MovieDetailActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_rate, null);
        final ScaleRatingBar ratingBar = dialogView.findViewById(R.id.simpleRatingBar);

        if (isUserVoted[0]) {
            ratingBar.setRating(oldVote);
        }
        builder.setTitle(getString(R.string.vote_it))
                .setMessage(null)
                .setPositiveButton(getString(R.string.vote), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        new InternetCheck(getApplicationContext()).isInternetConnectionAvailable(new InternetCheck.InternetCheckListener() {

                            @Override
                            public void onComplete(boolean connected) {
                                if (connected) {
                                    float newVote = ratingBar.getRating();
                                    if (newVote > 0.f) {
                                        if (moviesExists[0]) {
                                            if (isUserVoted[0]) {
                                                updateMovieOnFirebase((int) newVote, oldVote, voteOperation.UPDATE);
                                            } else {
                                                updateMovieOnFirebase((int) newVote, 0, voteOperation.ADD);
                                            }

                                        } else {
                                            addMovieFirebase((int) newVote);
                                        }
                                    } else {
                                        showSnackBar(getString(R.string.snackbar_rating_min_vote));
                                    }
                                } else {
                                    showSnackBar(getString(R.string.snackbar_rating_no_internet));
                                }
                            }
                        });

                    }
                })
                .setIcon(null)
                .setView(dialogView);

        if (isUserVoted[0]) {
            builder.setNegativeButton(getString(R.string.vote_delete), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    new InternetCheck(getApplicationContext()).isInternetConnectionAvailable(new InternetCheck.InternetCheckListener() {

                        @Override
                        public void onComplete(boolean connected) {
                            if (connected) {
                                updateMovieOnFirebase(0, oldVote, voteOperation.DELETE);

                            } else {
                                showSnackBar(getString(R.string.snackbar_rating_no_internet));
                            }
                        }
                    });
                }
            });
        }
        builder.show();
    }

    /*
     *
     * Vote UI kısımlarını ayarlar.
     *
     */
    private void setRatingUi(float voteAvg, int voteCount) {
        String voteAverage = String.format("%.1f", voteAvg);

        voteAverageText.setText(voteAverage);
        ratingBar.setRating(voteAvg);
        voteCountText.setText(voteCount + "");
    }

    /*
     *
     * YourVote UI kısımlarını ayarlar.
     *
     */
    private void setYourRatingUi(int vote) {
        if (vote != 0) {
            yourVoteContainer.setVisibility(View.VISIBLE);
            yourVoteText.setText(vote + "");
        }
    }

    /*
     *
     * Movie Firebase Veritabanında var mı diye kontrol eden fonksiyon
     * Eğer varsa gelen veriler ile güncelleniyor
     *
     * */
    private void checkMovieExistsOnFirebase() {
        movieRef.orderByChild("id").equalTo(movieId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    moviesExists[0] = true;
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        float voteAverage = data.child(voteAverageName).getValue(Float.class);
                        int voteCount = data.child(voteCountName).getValue(Integer.class);

                        setRatingUi(voteAverage, voteCount);
                        checkExistVoteOnFirebase();
                    }

                } else {
                    setRatingUi(0.f, 0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    /*
     *
     * Movie Local Veritabanında var mı diye kontrol eden fonksiyon
     *
     *
     * */
    private void checkMovieExistsOnLocal() {
        if (!checkMovieExistsOnLocalRateTable()) {
            yourVoteContainer.setVisibility(View.GONE);
            if (!checkMovieExistsOnLocalMovieTable()) {
                rating.setVisibility(View.GONE);
            }
        }
    }

    /*
     *
     * Movie Local Veritabanı Movie tablosunda var mı diye kontrol eden fonksiyon
     *
     *
     * */
    private boolean checkMovieExistsOnLocalMovieTable() {
        Cursor mCursor = getContentResolver().query(
                CollectionContract.CollectionEntry.CONTENT_URI_Movies,
                null,
                "id = ?",
                new String[]{String.valueOf(movieId)},
                null);

        assert mCursor != null;
        if (mCursor.moveToFirst()) {
            float voteAverage;
            int voteCount;

            do {
                int index = mCursor.getColumnIndexOrThrow(voteAverageName);
                voteAverage = mCursor.getFloat(index);

                index = mCursor.getColumnIndexOrThrow(voteCountName);
                voteCount = mCursor.getInt(index);


            }
            while ((mCursor.moveToNext()));

            setRatingUi(voteAverage, voteCount);
            return voteCount != 0;
        }
        mCursor.close();

        return false;
    }

    /*
     *
     * Movie Local Veritabanı Rate tablosunda var mı diye kontrol eden fonksiyon
     *
     *
     * */
    private boolean checkMovieExistsOnLocalRateTable() {
        Cursor mCursor = getContentResolver().query(
                CollectionContract.CollectionEntry.CONTENT_URI_Rate,
                null,
                "id = ?",
                new String[]{String.valueOf(movieId)},
                null);

        assert mCursor != null;
        if (mCursor.moveToFirst()) {
            do {
                int index = mCursor.getColumnIndexOrThrow(voteAverageName);
                float voteAverage = mCursor.getFloat(index);

                index = mCursor.getColumnIndexOrThrow(voteName);
                int vote = mCursor.getInt(index);

                index = mCursor.getColumnIndexOrThrow(voteCountName);
                int voteCount = mCursor.getInt(index);

                setRatingUi(voteAverage, voteCount);
                setYourRatingUi(vote);
                oldVote = vote;
                isUserVoted[0] = true;

            }
            while ((mCursor.moveToNext()));

            return true;
        }
        mCursor.close();

        return false;
    }

    /*
     *
     * Movie Firebase Veritabanında kullanıcının bu filmi oylayıp
     * oylamadığını kontrol eden fonksiyon eğer varsa;
     * ui'de ilgili alanlar doldurulup görünür oluyor
     *
     *
     * */
    private void checkExistVoteOnFirebase() {
        String ratingId = userId + "_" + movieId;
        rateRef.orderByChild("userIdCollectionId").equalTo(ratingId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    isUserVoted[0] = true;
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        int vote = data.child(voteName).getValue(Integer.class);
                        setYourRatingUi(vote);
                        oldVote = vote;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    /*
     *
     * Firebase movie'yi ekler
     *
     * */
    private void addMovieFirebase(final int vote) {
        String id = movieRef.push().getKey();
        assert id != null;

        movie.setVoteCount(1);
        movie.setVoteScore(vote);
        movie.setVoteAverage(vote);

        movieRef.child(id).setValue(movie, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null) {
                    System.out.println("Data could not be saved " + databaseError.getMessage());
                    showSnackBar(getString(R.string.snackbar_rating_fail));
                } else {
                    System.out.println("Data saved successfully.");
                    moviesExists[0] = true;
                    setRatingUi(vote, 1);
                    addUserVoteFirebase(vote, 1, vote, vote);
                }
            }
        });
    }

    /*
     *
     * Firebase rate tablosuna kullanıcı oyunu ekler
     *
     * */
    private void addUserVoteFirebase(final int vote, final int finalVoteCount, final int finalVoteScore, final float voteAverage) {
        String id = rateRef.push().getKey();
        assert id != null;

        Map<String, Object> ratingMap = new HashMap<>();
        ratingMap.put("userIdCollectionId", userId + "_" + movieId);
        ratingMap.put("userId", userId);
        ratingMap.put("collectionId", movieId);
        ratingMap.put("vote", vote);
        ratingMap.put("type", 1);

        rateRef.child(id).setValue(ratingMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null) {
                    System.out.println("Data could not be saved " + databaseError.getMessage());
                    showSnackBar(getString(R.string.snackbar_rating_fail));
                } else {
                    System.out.println("Data saved successfully.");
                    showSnackBar(getString(R.string.snackbar_rating_success));
                    setYourRatingUi(vote);
                    isUserVoted[0] = true;
                    oldVote = vote;
                    addLocalRate(vote, finalVoteCount, finalVoteScore, voteAverage);
                }
            }
        });
    }


    /*
     *
     * Firebase rate tablosunda kullanıcın oyunu günceller
     *
     * */
    private void updateUserVoteFirebase(final int vote, final int finalVoteCount, final int finalVoteScore, final float voteAverage) {
        String ratingId = userId + "_" + movieId;
        rateRef.orderByChild("userIdCollectionId").equalTo(ratingId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {

                        rateRef.child(data.getKey()).child(voteName).setValue(vote, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    showSnackBar(getString(R.string.snackbar_rating_update_fail));
                                } else {
                                    Log.i("SAMI", "geldim");
                                    showSnackBar(getString(R.string.snackbar_rating_update_success));
                                    setYourRatingUi(vote);
                                    isUserVoted[0] = true;
                                    oldVote = vote;
                                    addLocalRate(vote, finalVoteCount, finalVoteScore, voteAverage);
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    /**
     * Firebaseden kullanıcı oyu silen fonskiyon
     */
    private void deleteUserVoteOnFirebase() {
        String ratingId = userId + "_" + movieId;
        rateRef.orderByChild("userIdCollectionId").equalTo(ratingId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {

                        rateRef.child(data.getKey()).removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    showSnackBar(getString(R.string.snackbar_rating_delete_fail));
                                } else {
                                    showSnackBar(getString(R.string.snackbar_rating_delete_success));
                                    yourVoteContainer.setVisibility(View.GONE);
                                    isUserVoted[0] = false;
                                    oldVote = 0;
                                    deleteUserVoteOnLocal();
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }


    /*
     *
     * Firebase movie'ye yeni oy ekler
     *
     * */
    private void updateMovieOnFirebase(final int newValue, final int oldVote, final voteOperation voteOperation) {
        movieRef.orderByChild("id").equalTo(movieId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    int voteScore = data.child(voteScoreName).getValue(Integer.class);
                    int voteCount = data.child(voteCountName).getValue(Integer.class);

                    if (voteOperation == voteOperation.ADD) {
                        voteScore += newValue;
                        voteCount += 1;
                    } else if (voteOperation == voteOperation.UPDATE) {
                        int diff = newValue - oldVote;
                        voteScore += diff;
                    } else if (voteOperation == voteOperation.DELETE) {
                        voteCount -= 1;
                        voteScore -= oldVote;
                    }

                    final float voteAverage;
                    if (voteCount == 0) {
                        voteScore = 0;
                        voteAverage = 0.f;
                    } else {
                        voteAverage = (float) voteScore / voteCount;
                    }

                    final int finalvoteCount = voteCount;
                    final int finalVoteCount = voteCount;
                    final int finalVoteScore = voteScore;

                    String dataKey = data.getKey();
                    assert dataKey != null;
                    movieRef.child(dataKey).child(voteScoreName).setValue(voteScore);
                    movieRef.child(dataKey).child(voteCountName).setValue(voteCount);
                    movieRef.child(dataKey).child(voteAverageName).setValue(voteAverage,
                            new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    if (databaseError != null) {
                                        showSnackBar(getString(R.string.snackbar_rating_fail));
                                    } else {
                                        setRatingUi(voteAverage, finalvoteCount);
                                        if (voteOperation == voteOperation.ADD) {
                                            addUserVoteFirebase(newValue, finalVoteCount, finalVoteScore, voteAverage);
                                        } else if (voteOperation == voteOperation.UPDATE) {
                                            updateUserVoteFirebase(newValue, finalVoteCount, finalVoteScore, voteAverage);
                                        } else {
                                            deleteUserVoteOnFirebase();
                                        }
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }


    private boolean checkExistVoteOnLocal() {
        Cursor cursor = getContentResolver().query(
                CollectionContract.CollectionEntry.CONTENT_URI_Rate,
                null,
                "id = ?",
                new String[]{String.valueOf(movieId)},
                null);
        assert cursor != null;
        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    /*
     * Movie Local Rate tablosunda top rated mı kontrolü.
     *
     * */
    private boolean checkMovieTopRated() {
        Cursor cursor = getContentResolver().query(
                CollectionContract.CollectionEntry.CONTENT_URI_Rate,
                null,
                CollectionContract.CollectionEntry.id + " = ? AND " +
                        CollectionContract.CollectionEntry.COLUMN_IS_TOP_RATED + " = ?",
                new String[]{String.valueOf(movieId), String.valueOf(1)},
                null);

        assert cursor != null;
        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }

    }

    private void addLocalRate(int vote, int voteCount, int voteScore, float voteAverage) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CollectionContract.CollectionEntry.COLUMN_VOTE, vote);
        contentValues.put(CollectionContract.CollectionEntry.COLUMN_VOTE_COUNT, voteCount);
        contentValues.put(CollectionContract.CollectionEntry.COLUMN_VOTE_SCORE, voteScore);
        contentValues.put(CollectionContract.CollectionEntry.COLUMN_VOTE_AVERAGE, voteAverage);
        contentValues.put(CollectionContract.CollectionEntry.COLUMN_IS_MY_COLLECTION, 1);

        if (checkExistVoteOnLocal()) {
            getContentResolver().update(
                    CollectionContract.CollectionEntry.CONTENT_URI_Rate,
                    contentValues,
                    CollectionContract.CollectionEntry.id + "=?",
                    new String[]{String.valueOf(movieId)});
        } else {
            BitmapDrawable drawable = (BitmapDrawable) movieBackgroundImage.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            byte[] image = getBytes(bitmap);

            contentValues.put(CollectionContract.CollectionEntry.id, movieId);
            contentValues.put(CollectionContract.CollectionEntry.COLUMN_USER_ID, userId);
            contentValues.put(CollectionContract.CollectionEntry.COLUMN_ORIGINAL_TITLE, movie.getTitle());
            contentValues.put(CollectionContract.CollectionEntry.COLUMN_POSTER_PATH, image);
            contentValues.put(CollectionContract.CollectionEntry.COLUMN_DESCRIPTION, movie.getDescription());
            contentValues.put(CollectionContract.CollectionEntry.COLUMN_BACKDROP_PATH, image);
            contentValues.put(CollectionContract.CollectionEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
            contentValues.put(CollectionContract.CollectionEntry.COLUMN_LANGUAGE, movie.getLanguage());
            contentValues.put(CollectionContract.CollectionEntry.COLUMN_COLLECTION_TYPE, 1);
            contentValues.put(CollectionContract.CollectionEntry.COLUMN_IS_TOP_RATED, 0);
            contentValues.put(CollectionContract.CollectionEntry.COLUMN_AUTHORS, 0);
            contentValues.put(CollectionContract.CollectionEntry.COLUMN_GENRES, 0);
            getContentResolver().insert(CollectionContract.CollectionEntry.CONTENT_URI_Rate, contentValues);
        }
    }


    private void deleteUserVoteOnLocal() {
        if (checkMovieTopRated()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(CollectionContract.CollectionEntry.COLUMN_IS_MY_COLLECTION, 0);
            getContentResolver().update(
                    CollectionContract.CollectionEntry.CONTENT_URI_Rate,
                    contentValues,
                    CollectionContract.CollectionEntry.id + "=?",
                    new String[]{String.valueOf(movieId)});
        } else {
            getContentResolver().delete(
                    CollectionContract.CollectionEntry.CONTENT_URI_Rate,
                    CollectionContract.CollectionEntry.id + "=?",
                    new String[]{String.valueOf(movieId)});
        }
    }


    public byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    private void showSnackBar(String message) {
        Snackbar snackbar = Snackbar.make(root, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public class LoadAds extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            initializeAds();
        }
    }

    private void scheduleStartPostponedTransition(final View sharedElement) {
        sharedElement.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                        startPostponedEnterTransition();
                        return true;
                    }
                });
    }
}
