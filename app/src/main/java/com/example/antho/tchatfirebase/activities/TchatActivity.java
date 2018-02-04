package com.example.antho.tchatfirebase.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.antho.tchatfirebase.R;
import com.example.antho.tchatfirebase.entities.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import EventListener.TchatChildEventListener;

import static com.example.antho.tchatfirebase.utils.Constants.DB_MESSAGES;
import static com.example.antho.tchatfirebase.utils.Constants.PREF_PSEUDO;
import static com.example.antho.tchatfirebase.utils.Constants.PREF_TCHAT;

public class TchatActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String ACT_TCHAT_SEND_MSG = "act_tchat_send_msg";

    private EditText actTchatMessage;
    private ImageButton actTchatImageBtn;
    private ImageButton actTchatSendBtn;
    private RecyclerView actTchatRecycler;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseAuth.AuthStateListener authStateListener;
    private ChildEventListener childEventListener;

    private SharedPreferences preferences;

    private String username;
    private String userId;

    /**
     * Création de l'activité pour le tchat
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tchat);

        Toolbar toolbar = findViewById(R.id.tchatAct_toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        preferences = getSharedPreferences(PREF_TCHAT, MODE_PRIVATE);
        initViews();
        initFirebase();
    }

    /**
     * Activité prête
     */
    @Override
    protected void onResume() {
        super.onResume();
        auth.addAuthStateListener(authStateListener);
    }

    /**
     * Activité en pause
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (authStateListener != null) {
            auth.removeAuthStateListener(authStateListener);
        }
        detachChildListener();
    }

    /**
     * Menu pour se déconnecter
     * @param menu
     * @return boolean
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Permet de détecter l'item cliqué dan le menu
     * @param item
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            // TODO: déconnecter l'utilisateur
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Permet de connaitre le bouton cliqué
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (v.getTag() == ACT_TCHAT_SEND_MSG) {
            sendMessage();
        }
    }

    /**
     * Initialisation de la vue de l'activité
     */
    private void initViews() {
        actTchatMessage = findViewById(R.id.tchatAct_message);
        actTchatImageBtn  = findViewById(R.id.tchatAct_imageBtn);
        actTchatSendBtn = findViewById(R.id.tchatAct_sendBtn);
        actTchatRecycler = findViewById(R.id.tchatAct_recycler);

        actTchatSendBtn.setOnClickListener(this);
        actTchatSendBtn.setTag(ACT_TCHAT_SEND_MSG);
    }

    /**
     * Initialisation des éléments nécessaires à l'utilisation de Firebase pour l'activité
     */
    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    attachChildListener();
                    username = preferences.getString(PREF_PSEUDO, null);
                    userId = user.getUid();
                } else {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
            }
        };
    }

    /**
     * Initialise l'évenement pour le tchat
     */
    private void attachChildListener() {
        if (childEventListener == null) {
            childEventListener = new TchatChildEventListener();
        }
        reference.child(DB_MESSAGES).limitToLast(100).addChildEventListener(childEventListener);
    }

    /**
     * Retire l'évenement pour le tchat
     */
    private void detachChildListener() {
        if (childEventListener != null) {
            reference.child(DB_MESSAGES).removeEventListener(childEventListener);
            childEventListener = null;
        }
    }

    /**
     * Envoie un message
     */
    private void sendMessage() {
        String content = actTchatMessage.getText().toString();
        if (!TextUtils.isEmpty(content)) {
            Message message = new Message(username, userId, content, null);
            reference.child(DB_MESSAGES).push().setValue(message);
            actTchatMessage.setText("");
        }
    }
}
