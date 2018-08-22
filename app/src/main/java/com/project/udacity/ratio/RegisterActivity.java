package com.project.udacity.ratio;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.project.udacity.ratio.data.db.CollectionContract;
import com.project.udacity.ratio.models.User;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegisterActivity extends AppCompatActivity {

    @BindView(R.id.register_button)
    Button register_btn;

    @BindView(R.id.user_name_txt)
    TextInputEditText userName;

    @BindView(R.id.male)
    RadioButton genderMale;

    @BindView(R.id.female)
    RadioButton genderFemale;

    @BindView(R.id.user_email_txt)
    TextInputEditText email;

    @BindView(R.id.user_age_txt)
    TextInputEditText age;

    @BindView(R.id.user_passwd_txt)
    TextInputEditText password;

    @BindView(R.id.user_passwd_confirm_txt)
    TextInputEditText passwordConfirm;

    @BindView(R.id.login_txt)
    TextView alreadyMemberText;

    @BindView(R.id.root)
    ScrollView root;

    private FirebaseAuth mAuth;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference(CollectionContract.CollectionEntry.USER_TABLE_NAME);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        if (checkUserLogin()) {
            openIntent(MainActivity.class);
        }
        mAuth = FirebaseAuth.getInstance();
        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                if (isFormValid()) {
                    register_btn.setEnabled(false);
                    registerNewUser(
                            email.getText().toString().trim(),
                            password.getText().toString().trim(),
                            userName.getText().toString().trim(),
                            genderMale.isChecked() ? "male" : "female",
                            age.getText().toString().trim());
                }
            }
        });

        alreadyMemberText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openIntent(LoginActivity.class);
            }
        });


    }

    private void openIntent(Class<?> mClass) {
        startActivity(new Intent(RegisterActivity.this, mClass));
        finish();
    }

    private boolean checkUserLogin() {
        Cursor mCursor = getContentResolver().query(
                CollectionContract.CollectionEntry.CONTENT_URI_Users,  // The content URI of the words table
                null,                       // The columns to return for each row
                CollectionContract.CollectionEntry.COLUMN_ACTIVE + " = 1",             // Either null, or the word the user entered
                null,                    // Either empty, or the string the user entered
                null);

        assert mCursor != null;
        return mCursor.moveToFirst();
    }

    private boolean isFormValid() {

        if (isEmpty(userName)) {
            userName.setError(getString(R.string.error_name));
            return false;
        }

        if (isEmpty(email)) {
            email.setError(getString(R.string.error_email));
            return false;
        }

        if (!isEmail()) {
            email.setError(getString(R.string.error_email_valid));
            return false;
        }

        if (isEmpty(password)) {
            password.setError(getString(R.string.error_password));
            return false;
        }

        if (!isPasswordValid()) {
            password.setError(getString(R.string.error_password_valid));
            return false;
        }

        if (isEmpty(passwordConfirm)) {
            passwordConfirm.setError(getString(R.string.error_password_confirm));
            return false;
        }

        if (!isPasswordMatch()) {
            passwordConfirm.setError(getString(R.string.error_password_match));
            return false;
        }

        if (isEmpty(age)) {
            age.setError(getString(R.string.error_age));
            return false;
        }

        return true;

    }

    private boolean isPasswordMatch() {
        return password.getText().toString().equals(passwordConfirm.getText().toString());
    }

    private boolean isPasswordValid() {
        CharSequence str = password.getText().toString();
        return str.length() >= 6;
    }

    private boolean isEmail() {
        CharSequence str = email.getText().toString();
        return (!TextUtils.isEmpty(str) && Patterns.EMAIL_ADDRESS.matcher(str).matches());
    }

    private boolean isEmpty(TextInputEditText text) {
        CharSequence str = text.getText().toString();
        return TextUtils.isEmpty(str);
    }

    public void registerNewUser(final String email, final String password, final String userName, final String gender, final String age) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "createUserWithEmail:success");
                            //FirebaseUser user = mAuth.getCurrentUser();

                            final String ID = myRef.push().getKey();
                            String userId = task.getResult().getUser().getUid();

                            User user = new User(userName, email, gender, Integer.parseInt(age), userId);

                            assert ID != null;
                            myRef.child(ID).setValue(user, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    if (databaseError != null) {
                                        System.out.println("Data could not be saved " + databaseError.getMessage());
                                        showSnackBar(getString(R.string.snackbar_error_occurred));
                                    } else {
                                        System.out.println("Data saved successfully.");
                                    }
                                }
                            });

                            openLoginActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "createUserWithEmail:failure", task.getException());
                            showSnackBar(getString(R.string.snackbar_auth_failed));

                            register_btn.setEnabled(true);
                        }
                    }
                });
    }

    private void openLoginActivity() {

        showSnackBar(getString(R.string.snackbar_register_success));

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                openIntent(LoginActivity.class);
            }
        }, 2000);
    }

    private void showSnackBar(String message) {
        Snackbar snackbar = Snackbar.make(root, message, Snackbar.LENGTH_LONG);
        snackbar.show();
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
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }
}
