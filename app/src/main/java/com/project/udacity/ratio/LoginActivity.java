package com.project.udacity.ratio;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.udacity.ratio.data.db.CollectionContract;
import com.project.udacity.ratio.models.User;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;


public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.user_email_txt)
    TextInputEditText userEmail;

    @BindView(R.id.user_passwd_txt)
    TextInputEditText userPasswd;

    @BindView(R.id.register_txt)
    TextView registerText;

    @BindView(R.id.root)
    LinearLayout root;

    @BindView(R.id.login_button)
    AppCompatButton loginButton;

    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private String TAG = "LoginError: ";

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference ref = database.getReference(CollectionContract.CollectionEntry.USER_TABLE_NAME);
    private Uri usersURI = CollectionContract.CollectionEntry.CONTENT_URI_Users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);


        mAuth = FirebaseAuth.getInstance();

        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                if (isFormValid()) {
                    loginButton.setEnabled(false);
                    login(userEmail.getText().toString().trim(), userPasswd.getText().toString().trim());
                }
            }
        });
    }

    private boolean isFormValid() {

        if (isEmpty(userEmail)) {
            userEmail.setError(getString(R.string.error_email));
            return false;
        }

        if (!isEmail()) {
            userEmail.setError(getString(R.string.error_email_valid));
            return false;
        }

        if (isEmpty(userPasswd)) {
            userPasswd.setError(getString(R.string.error_password));
            return false;
        }
        return true;
    }

    private boolean isEmail() {
        CharSequence str = userEmail.getText().toString();
        return (!TextUtils.isEmpty(str) && Patterns.EMAIL_ADDRESS.matcher(str).matches());
    }

    private boolean isEmpty(TextInputEditText text) {
        CharSequence str = text.getText().toString();
        return TextUtils.isEmpty(str);
    }

    private void showSnackBar(String message) {
        Snackbar snackbar = Snackbar.make(root, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public void login(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            currentUser = mAuth.getCurrentUser();
                            assert currentUser != null;
                            String userId = currentUser.getUid();
                            getUserFromFirebase(userId);

                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            showSnackBar(getString(R.string.snackbar_auth_failed));
                            loginButton.setEnabled(true);
                        }
                    }
                });
    }

    private void getUserFromFirebase(String userId) {
        ref.orderByChild("id").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    User user = eventSnapshot.getValue(User.class);
                    assert user != null;
                    checkUserPhoto(user);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    private void checkUserPhoto(final User user) {

        Uri uri = currentUser.getPhotoUrl();
        if (uri != null) {

            Picasso.get().load(uri).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    insertLocalDatabase(user, getBytes(bitmap));
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });
        } else {
            insertLocalDatabase(user, null);
        }


    }

    private void insertLocalDatabase(User user, byte[] photo) {
        String userId = user.getId();

        ContentValues contentValues = new ContentValues();
        contentValues.put(CollectionContract.CollectionEntry.COLUMN_ACTIVE, 1);
        contentValues.put(CollectionContract.CollectionEntry.COLUMN_USER_PHOTO, photo);

        if (checkUserExists(userId)) {
            getContentResolver().update(
                    usersURI, contentValues,
                    CollectionContract.CollectionEntry.id + " = ?",
                    new String[]{userId});
        } else {
            contentValues.put(CollectionContract.CollectionEntry.id, user.getId());
            contentValues.put(CollectionContract.CollectionEntry.COLUMN_USERNAME, user.getUsername());
            contentValues.put(CollectionContract.CollectionEntry.COLUMN_EMAIL, user.getEmail());
            contentValues.put(CollectionContract.CollectionEntry.COLUMN_GENDER, user.getGender());
            contentValues.put(CollectionContract.CollectionEntry.COLUMN_AGE, user.getAge());

            getContentResolver().insert(usersURI, contentValues);
        }


        openMainActivity();
    }


    private boolean checkUserExists(String userId) {
        Cursor mCursor = getContentResolver().query(
                CollectionContract.CollectionEntry.CONTENT_URI_Users,  // The content URI of the words table
                null,                       // The columns to return for each row
                CollectionContract.CollectionEntry.id + " = ?",             // Either null, or the word the user entered
                new String[]{userId},                    // Either empty, or the string the user entered
                null);

        assert mCursor != null;
        boolean result = mCursor.moveToFirst();
        mCursor.close();
        return result;
    }

    private void openMainActivity() {

        showSnackBar(getString(R.string.snackbar_login_success));

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                openIntent(MainActivity.class);
            }
        }, 2000);
    }

    private void openIntent(Class<?> mClass) {
        startActivity(new Intent(LoginActivity.this, mClass));
        finish();
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        assert imm != null;
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();
    }

    public byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        openIntent(RegisterActivity.class);
    }
}
