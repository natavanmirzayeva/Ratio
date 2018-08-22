package com.project.udacity.ratio.ui;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.project.udacity.ratio.R;
import com.project.udacity.ratio.ui.categories.tabs.BookFragment;
import com.project.udacity.ratio.ui.categories.tabs.MovieFragment;
import com.project.udacity.ratio.ui.categories.tabs.SerialFragment;
import com.project.udacity.ratio.util.TabPagerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class TopRatedFragment extends Fragment {
    @BindView(R.id.tabLayout)
    TabLayout tabLayout;

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    int mCurrentItem;


    public static TopRatedFragment newInstance() {
        Bundle args = new Bundle();
        TopRatedFragment fragment = new TopRatedFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(getString(R.string.top_rated));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_top_rated, container, false);
        ButterKnife.bind(this, view);

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        if (savedInstanceState != null) {
            mCurrentItem = savedInstanceState.getInt("current_item");
            viewPager.setCurrentItem(mCurrentItem);
        }

        tabLayout.addOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        mCurrentItem = tab.getPosition();

                    }
                });
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_item", viewPager.getCurrentItem());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentItem = savedInstanceState.getInt("current_item");
            viewPager.setCurrentItem(mCurrentItem);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        TabPagerAdapter adapter = new TabPagerAdapter(getChildFragmentManager());
        Bundle bundle = new Bundle();
        bundle.putBoolean("isTopRated", true);
        bundle.putBoolean("isRatioCollection", false);

        MovieFragment movieFragment = MovieFragment.newInstance();
        movieFragment.setArguments(bundle);

        BookFragment bookFragment = BookFragment.newInstance();
        bookFragment.setArguments(bundle);

        SerialFragment serialFragment = SerialFragment.newInstance();
        serialFragment.setArguments(bundle);

        adapter.addFragment(movieFragment, getString(R.string.movies));
        adapter.addFragment(bookFragment, getString(R.string.books));
        adapter.addFragment(serialFragment, getString(R.string.serials));

        adapter.notifyDataSetChanged();
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewPager.setCurrentItem(mCurrentItem);
    }

    @Override
    public void onStop() {
        super.onStop();
        viewPager.setCurrentItem(mCurrentItem);
    }

}
