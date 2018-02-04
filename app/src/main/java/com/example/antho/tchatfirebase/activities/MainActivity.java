package com.example.antho.tchatfirebase.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.antho.tchatfirebase.R;
import com.example.antho.tchatfirebase.entities.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.example.antho.tchatfirebase.utils.Constants.DB_USERNAMES;
import static com.example.antho.tchatfirebase.utils.Constants.DB_USERS;
import static com.example.antho.tchatfirebase.utils.Constants.PREF_PSEUDO;
import static com.example.antho.tchatfirebase.utils.Constants.PREF_TCHAT;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String ACT_MAIN_BTN_LOGIN = "act_main_btn_login";
    private static final String TAG = "ERROR_MAIN_ACT";

    private EditText actMainPseudo;
    private ProgressBar actMainLoader;
    private Button actMainBtnLogin;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initFirebase();

        preferences = getSharedPreferences(PREF_TCHAT, MODE_PRIVATE);

        if (auth.getCurrentUser() != null && preferences.getString(PREF_PSEUDO, null) != null) {
            startActivity(new Intent(getApplicationContext(), TchatActivity.class));
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() == ACT_MAIN_BTN_LOGIN) {
            actMainLoader.setVisibility(View.VISIBLE);
            String username = actMainPseudo.getText().toString();
            if (!TextUtils.isEmpty(username)) {
                registerUser(username);
            }
        }
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
    }

    private void initView() {
        actMainPseudo = findViewById(R.id.actMain_pseudo);
        actMainLoader = findViewById(R.id.actMain_loader);
        actMainBtnLogin = findViewById(R.id.actMain_btnLogin);

        actMainBtnLogin.setOnClickListener(this);
        actMainBtnLogin.setTag(ACT_MAIN_BTN_LOGIN);
    }

    private void registerUser(final String username) {
        auth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Connexion impossible", Toast.LENGTH_SHORT).show();
                    actMainLoader.setVisibility(View.INVISIBLE);
                } else {
                    final String userId = task.getResult().getUser().getUid();

                    checkUsername(username, new CheckUsernameCallback() {
                        @Override
                        public void isValid(final String username) {
                            User user = new User(userId, username);

                            reference.child(DB_USERS).child(userId).setValue(user).addOnCompleteListener(MainActivity.this, new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        reference.child(DB_USERNAMES).child(username).setValue(userId);
                                        preferences.edit().putString(PREF_PSEUDO, username).apply();

                                        startActivity(new Intent(getApplicationContext(), TchatActivity.class));
                                        finish();
                                    }
                                }
                            });
                        }

                        @Override
                        public void isToken() {
                            actMainLoader.setVisibility(View.INVISIBLE);
                            Toast.makeText(MainActivity.this, "Veuillez choisir un autre pseudo", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void checkUsername(final String username, final CheckUsernameCallback callback) {
        reference.child(DB_USERNAMES).child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    callback.isToken();
                } else {
                    callback.isValid(username);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, String.valueOf(databaseError));
                actMainLoader.setVisibility(View.INVISIBLE);
            }
        });
    }

    interface CheckUsernameCallback {

        void isValid(String username);

        void isToken();
    }
}
