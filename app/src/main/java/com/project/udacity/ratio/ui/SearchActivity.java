package com.project.udacity.ratio.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.project.udacity.ratio.R;
import com.project.udacity.ratio.models.Book;
import com.project.udacity.ratio.models.Movie;
import com.project.udacity.ratio.models.Serial;
import com.project.udacity.ratio.ui.categories.tabs.BookFragment;
import com.project.udacity.ratio.ui.categories.tabs.MovieFragment;
import com.project.udacity.ratio.ui.categories.tabs.SerialFragment;
import com.project.udacity.ratio.util.SearchIntentService;
import com.project.udacity.ratio.util.TabPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchActivity extends AppCompatActivity {
    @BindView(R.id.tabLayout)
    TabLayout tabLayout;

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @BindView(R.id.txtSearch)
    EditText txtSearch;

    @BindView(R.id.btnSearch)
    Button btnSearch;

    @BindView(R.id.root)
    CoordinatorLayout root;

    @BindView(R.id.btn_back)
    ImageButton btnBack;

    int mCurrentItem;

    String searchText;

    TabPagerAdapter adapter;
    SearchReciver searchReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setTitle(getString(R.string.search));
        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            mCurrentItem = savedInstanceState.getInt("current_item");
            viewPager.setCurrentItem(mCurrentItem);
        }

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        mCurrentItem = tab.getPosition();

                    }
                });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchText = txtSearch.getText().toString();
                startSearchIntentService();
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchActivity.super.onBackPressed();
                supportFinishAfterTransition();
            }
        });

        registerSearchReceiver();
    }

    private void setupViewPager(List<Movie> movies, List<Book> books, List<Serial> serials) {
        adapter = new TabPagerAdapter(getSupportFragmentManager());

        Bundle bundleMovie = new Bundle();
        bundleMovie.putBoolean("isSearch",true);
        bundleMovie.putParcelableArrayList("movies", (ArrayList<? extends Parcelable>) movies);

        MovieFragment movieFragment = MovieFragment.newInstance();
        movieFragment.setArguments(bundleMovie);


        Bundle bundleBook = new Bundle();
        bundleBook.putBoolean("isSearch",true);
        bundleBook.putParcelableArrayList("books", (ArrayList<? extends Parcelable>) books);

        BookFragment bookFragment = BookFragment.newInstance();
        bookFragment.setArguments(bundleBook);


        Bundle bundleSerial = new Bundle();
        bundleSerial.putBoolean("isSearch",true);
        bundleSerial.putParcelableArrayList("serials", (ArrayList<? extends Parcelable>) serials);

        SerialFragment serialFragment = SerialFragment.newInstance();
        serialFragment.setArguments(bundleSerial);

        adapter.addFragment(movieFragment, getString(R.string.movies));
        adapter.addFragment(bookFragment, getString(R.string.books));
        adapter.addFragment(serialFragment, getString(R.string.serials));
        adapter.notifyDataSetChanged();
        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_item", viewPager.getCurrentItem());
    }

    @Override
    public void onResume() {
        super.onResume();
        viewPager.setCurrentItem(mCurrentItem);
        registerReceiver(searchReceiver, new IntentFilter(
               "SEARCH"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(searchReceiver);
    }

    @Override
    public void onStop() {
        super.onStop();
        viewPager.setCurrentItem(mCurrentItem);
        unregisterReceiver(searchReceiver);

    }

    private void showSnackBar(String message) {
        Snackbar snackbar = Snackbar.make(root, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }


    public void startSearchIntentService() {
        Intent cbIntent = new Intent();
        cbIntent.setClass(this, SearchIntentService.class);
        cbIntent.putExtra("searchText", searchText);
        startService(cbIntent);
    }

    private void registerSearchReceiver() {
        searchReceiver = new SearchReciver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("SEARCH");
        registerReceiver(searchReceiver, intentFilter);
    }

    private class SearchReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<Movie> movies = intent.getParcelableArrayListExtra("movies");
            List<Serial> serials = intent.getParcelableArrayListExtra("serials");
            List<Book> books = intent.getParcelableArrayListExtra("books");

            setupViewPager(movies, books, serials);
        }
    }
}
