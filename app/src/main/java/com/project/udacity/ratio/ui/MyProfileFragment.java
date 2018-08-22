package com.project.udacity.ratio.ui;


import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.udacity.ratio.MainActivity;
import com.project.udacity.ratio.R;
import com.project.udacity.ratio.data.db.CollectionContract;
import com.project.udacity.ratio.models.User;
import com.project.udacity.ratio.util.InternetCheck;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyProfileFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    @BindView(R.id.username_txt)
    TextView username;

    @BindView(R.id.email_txt)
    TextView email;

    @BindView(R.id.gender_txt)
    TextView gender;

    @BindView(R.id.age_txt)
    TextView age;

    @BindView(R.id.user_profile_icon)
    ImageView profileIcon;

    User user;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference userRef = database.getReference(CollectionContract.CollectionEntry.USER_TABLE_NAME);
    private static final int ID_USER_LOADER = 50;

    public static MyProfileFragment newInstance() {
        Bundle args = new Bundle();
        MyProfileFragment fragment = new MyProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(getString(R.string.my_profile));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);
        ButterKnife.bind(this, view);
        new InternetCheck(getContext()).isInternetConnectionAvailable(new InternetCheck.InternetCheckListener() {
            @Override
            public void onComplete(final boolean connected) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (connected) {
                            getUser();
                        } else {
                            getUserFromDb();
                        }
                    }
                });
            }
        });


        return view;
    }

    void getUser() {
        String userId = MainActivity.USER_ID;
        userRef.orderByChild("id").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        user = data.getValue(User.class);
                    }
                    displayUser(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    void getUserFromDb() {
        getLoaderManager().initLoader(ID_USER_LOADER, null, MyProfileFragment.this);
    }

    void displayUser(User user) {
        username.setText(user.getUsername());
        email.setText(user.getEmail());
        gender.setText(user.getGender());
        age.setText(String.valueOf(user.getAge()));

        if (user.getGender().equals("male")) {
            profileIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.baseline_sentiment_very_satisfied_24));
        } else {
            profileIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.baseline_face_24));
        }

    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        if (loaderId == ID_USER_LOADER) {
            String arg[] = {MainActivity.USER_ID};
            String selection = "id = ?";
            Uri uri = CollectionContract.CollectionEntry.CONTENT_URI_Users;
            return new CursorLoader(getContext(), uri, null, selection, arg, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        User user = new User();
        assert cursor != null;
        cursor.moveToFirst();
        int index;
        index = cursor.getColumnIndexOrThrow("username");
        String username = cursor.getString(index);
        user.setUsername(username);
        index = cursor.getColumnIndexOrThrow("email");
        String email = cursor.getString(index);
        user.setEmail(email);
        index = cursor.getColumnIndexOrThrow("gender");
        String gender = cursor.getString(index);
        user.setGender(gender);
        index = cursor.getColumnIndexOrThrow("age");
        int age = cursor.getInt(index);
        user.setAge(age);

        displayUser(user);
    }


    @Override
    public void onLoaderReset(Loader loader) {

    }
}
