package com.project.udacity.ratio;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.auth.FirebaseAuth;
import com.project.udacity.ratio.data.db.CollectionContract;
import com.project.udacity.ratio.models.Book;
import com.project.udacity.ratio.models.Movie;
import com.project.udacity.ratio.models.Serial;
import com.project.udacity.ratio.ui.MyProfileFragment;
import com.project.udacity.ratio.ui.MyRatioCollectionFragment;
import com.project.udacity.ratio.ui.SearchActivity;
import com.project.udacity.ratio.ui.TopRatedFragment;
import com.project.udacity.ratio.ui.categories.CategoriesFragment;
import com.project.udacity.ratio.ui.categories.detailscreens.BookDetailActivity;
import com.project.udacity.ratio.ui.categories.detailscreens.MovieDetailActivity;
import com.project.udacity.ratio.ui.categories.detailscreens.SerialDetailActivity;
import com.project.udacity.ratio.util.Global;
import com.project.udacity.ratio.util.InternetCheck;
import com.project.udacity.ratio.util.WidgetProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "Gms";
    @BindView(R.id.nav_view)
    NavigationView navigationView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;


    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @BindView(R.id.searchButton)
    ImageButton searchButton;


    private View navHeader;
    public static String USER_ID;

    public static final String AUTHORITY = CollectionContract.AUTHORITY;

    private static final String ACCOUNT_TYPE = "com.project.udacity.ratio.sync";

    private static final String ACCOUNT = "ratio";

    public static final int SYNC_INTERVAL = 120; //2 minutes
    public static final int WIDGET_INTERVAL = 1 * 60000; // 1 minutes


    private InterstitialAd mInterstitialAd;

    private Account mAccount;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        /*
         *
         * Drawer Operations
         *
         * */
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        navHeader = navigationView.getHeaderView(0);

        //Select Home by default
        navigationView.setCheckedItem(R.id.nav_categories);

        if (savedInstanceState == null) {
            Fragment fragment = new CategoriesFragment();
            displaySelectedFragment(fragment);
        }

        setUserInfo();

        setSyncAdapter();

        setWidget();

        /*
         *
         * Google Analytic Operations
         *
         * */
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        Tracker mTracker = application.getDefaultTracker();
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("Share")
                .build());



        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new InternetCheck(getApplicationContext()).isInternetConnectionAvailable(
                        new InternetCheck.InternetCheckListener() {
                            @Override
                            public void onComplete(boolean connected) {
                                if (connected) {
                                    startActivity(new Intent(MainActivity.this, SearchActivity.class));
                                } else {
                                    showSnackBar(getString(R.string.snackbar_rating_no_internet));
                                }
                            }
                        });


            }
        });


        String collectionId = getIntent().getStringExtra("id");
        if (collectionId != null && savedInstanceState == null) {
            getRatedCollection(collectionId);
        }

        new LoadAds().execute();
    }

    private void getRatedCollection(String collectionId) {
        String args[] = {collectionId};
        String selection = "id = ?";
        Uri uri = CollectionContract.CollectionEntry.CONTENT_URI_Rate;

        Cursor cursor = getContentResolver().query(
                uri,
                null, selection,
                args,
                null);
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
                index = cursor.getColumnIndexOrThrow("collectionType");
                int collectionType = cursor.getInt(index);
                index = cursor.getColumnIndexOrThrow("genres");
                String genres = cursor.getString(index);

                List<String> authorList = new ArrayList<>();
                String[] authors = author.split(",");

                Collections.addAll(authorList, authors);

                List<String> genresList = new ArrayList<>();
                String[] genresS = genres.split(",");

                Collections.addAll(genresList, genresS);

                Global.image = imagePoster;

                switch (collectionType) {
                    case 1:
                        Movie m = new Movie("", "",
                                original_title, release_date, language, vote_average, voteCount,
                                voteScore, overview, id);
                        openDetailActivity("movie", m, MovieDetailActivity.class);
                        break;
                    case 2:
                        Book b = new Book(original_title, null, overview, vote_average, voteCount, voteScore,
                                release_date, language, id, authorList);
                        openDetailActivity("book", b, BookDetailActivity.class);
                        break;
                    case 3:
                        Serial s = new Serial(original_title, null, overview, vote_average,
                                voteCount, voteScore, release_date, language, id, genresList);
                        openDetailActivity("serial", s, SerialDetailActivity.class);
                        break;
                }
            }
            while ((cursor.moveToNext()));
        }
        cursor.close();
    }

    private void openDetailActivity(String collectionName, Parcelable collection, Class<?> collectionClass) {
        Intent i = new Intent(MainActivity.this, collectionClass);
        i.putExtra(collectionName, collection);
        i.putExtra("userId", USER_ID);

        startActivity(i);
    }

    private void setWidget() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(this, WidgetProvider.class));

        Intent intent = new Intent(MainActivity.this, WidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                ids);
        PendingIntent pending = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
        AlarmManager alarm = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        assert alarm != null;
        alarm.cancel(pending);
        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), WIDGET_INTERVAL, pending);
    }

    private void setUserInfo() {
        String[] projection = {CollectionContract.CollectionEntry.COLUMN_USERNAME,
                CollectionContract.CollectionEntry.id, CollectionContract.CollectionEntry.COLUMN_USER_PHOTO};

        Cursor mCursor = getContentResolver().query(
                CollectionContract.CollectionEntry.CONTENT_URI_Users,  // The content URI of the words table
                projection,                       // The columns to return for each row
                CollectionContract.CollectionEntry.COLUMN_ACTIVE + " = 1",             // Either null, or the word the user entered
                null,                    // Either empty, or the string the user entered
                null);

        assert mCursor != null;
        if (mCursor.moveToFirst()) {
            do {
                int index = mCursor.getColumnIndexOrThrow(projection[0]);
                String userName = mCursor.getString(index);

                index = mCursor.getColumnIndexOrThrow(projection[1]);
                USER_ID = mCursor.getString(index);

                index = mCursor.getColumnIndex(projection[2]);
                byte[] userImageBlob = mCursor.getBlob(index);

                if (userImageBlob != null) {
                    Drawable image = new BitmapDrawable(
                            getResources(),
                            BitmapFactory.decodeByteArray(userImageBlob,
                                    0,
                                    userImageBlob.length)
                    );

                    ((ImageView) navHeader.findViewById(R.id.profileImage)).setImageDrawable(image);
                }

                ((TextView) navHeader.findViewById(R.id.user_name)).setText(userName);
            }
            while ((mCursor.moveToNext()));
        }
        mCursor.close();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            }
            openExitDialog();
        }


    }

    private void openExitDialog() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(getString(R.string.close_app_dialog))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.super.onBackPressed();
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    ;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
     /*   if (id == R.id.action_settings) {
            return true;*
        }*/

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment;
        if (id == R.id.nav_categories) {
            fragment = CategoriesFragment.newInstance();
            displaySelectedFragment(fragment);

        } else if (id == R.id.nav_my_ratio_collections) {
            fragment = MyRatioCollectionFragment.newInstance();
            displaySelectedFragment(fragment);

        } else if (id == R.id.nav_top_rated) {
            fragment = TopRatedFragment.newInstance();
            displaySelectedFragment(fragment);

        } else if (id == R.id.nav_my_profile) {
            fragment = MyProfileFragment.newInstance();
            displaySelectedFragment(fragment);

        } else if (id == R.id.nav_log_out) {
            mAuth.signOut();
            setUserOnLocal();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initializeAds() {
        final AdRequest adRequestw = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        mInterstitialAd = new InterstitialAd(getApplicationContext());
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_id));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {

                mInterstitialAd.loadAd(adRequestw);
            }
        });

        mInterstitialAd.loadAd(adRequestw);
    }

    private void displaySelectedFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commit();
    }


    private void setUserOnLocal() {

        ContentValues contentValues = new ContentValues();
        contentValues.put(CollectionContract.CollectionEntry.COLUMN_ACTIVE, 0);

        getContentResolver().update(
                CollectionContract.CollectionEntry.CONTENT_URI_Users, contentValues,
                CollectionContract.CollectionEntry.id + " = ?",
                new String[]{USER_ID});

        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }

    void setSyncAdapter() {
        mAccount = CreateSyncAccount();

        if (ContentResolver.isSyncPending(mAccount, AUTHORITY) ||
                ContentResolver.isSyncActive(mAccount, AUTHORITY)) {
            Log.i("ContentResolver", "SyncPending, canceling");
            ContentResolver.cancelSync(mAccount, AUTHORITY);
        }

        ContentResolver.setIsSyncable(mAccount, AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(mAccount, AUTHORITY, true);
        ContentResolver.addPeriodicSync(
                mAccount,
                AUTHORITY,
                Bundle.EMPTY,
                SYNC_INTERVAL);

        // runSyncAdapter();
    }

    public void runSyncAdapter() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        ContentResolver.requestSync(mAccount, AUTHORITY, bundle);
    }

    public Account CreateSyncAccount() {
        Account newAccount = new Account(ACCOUNT, ACCOUNT_TYPE);
        AccountManager accountManager =
                (AccountManager) getSystemService(ACCOUNT_SERVICE);


        assert accountManager != null;
        Account accounts[] = accountManager.getAccountsByType(ACCOUNT_TYPE);
        if (accounts.length < 1) {
            if (accountManager.addAccountExplicitly(newAccount, null, null)) {
                return newAccount;
            } else {
                Log.i("sync activity", "error creating account");
            }
        } else {
            return accounts[0];
        }
        return null;
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

    private void showSnackBar(String message) {
        Snackbar snackbar = Snackbar.make(drawerLayout, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }
}