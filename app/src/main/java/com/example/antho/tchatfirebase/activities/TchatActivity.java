package com.example.antho.tchatfirebase.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.antho.tchatfirebase.R;
import com.example.antho.tchatfirebase.adapters.TchatAdapter;
import com.example.antho.tchatfirebase.entities.Message;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.antho.tchatfirebase.eventListener.TchatChildEventListener;

import static android.view.View.GONE;
import static com.example.antho.tchatfirebase.utils.Constants.DB_MESSAGES;
import static com.example.antho.tchatfirebase.utils.Constants.PREF_PSEUDO;
import static com.example.antho.tchatfirebase.utils.Constants.PREF_TCHAT;
import static com.example.antho.tchatfirebase.utils.Constants.STORAGE_PATH;
import static com.example.antho.tchatfirebase.utils.Constants.STORAGE_REF;

public class TchatActivity extends AppCompatActivity implements View.OnClickListener, TextView.OnEditorActionListener {

    private static final String STATE_STORAGE_REFERENCE = "storageReference";
    private static final String ACT_TCHAT_SEND_BTN = "act_tchat_send_btn";
    private static final String ACT_TCHAT_IMG_BTN = "act_tchat_img_btn";
    private static final int SELECT_PHOTO = 1;

    private EditText actTchatMessage;
    private ImageButton actTchatImageBtn;
    private ImageButton actTchatSendBtn;
    private RecyclerView actTchatRecycler;
    private ProgressBar actTchatLoader;
    private TchatAdapter adapter;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseAuth.AuthStateListener authStateListener;
    private ChildEventListener childEventListener;

    private SharedPreferences preferences;

    private UploadTask uploadTask;

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
        adapter.clearMessage();
    }

    @Override
    protected void onStop() {
        super.onStop();
        actTchatLoader.setVisibility(GONE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (storageReference != null) {
            outState.putString(STATE_STORAGE_REFERENCE, storageReference.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String ref = savedInstanceState.getString(STATE_STORAGE_REFERENCE);

        if (ref != null) {
            storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(ref);
            List tasks = storageReference.getActiveUploadTasks();

            if (tasks.size() > 0) {
                actTchatImageBtn.setEnabled(false);
                actTchatLoader.setVisibility(View.VISIBLE);
                uploadTask = (UploadTask) tasks.get(0);
                addUploadListener(uploadTask);
            }
        }

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
        if (v.getTag() == ACT_TCHAT_SEND_BTN) {
            sendMessage(null);
        } else if (v.getTag() == ACT_TCHAT_IMG_BTN) {
            pickImage();
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            sendMessage(null);
            InputMethodManager imm = (InputMethodManager) actTchatMessage.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(actTchatMessage.getWindowToken(), 0);
            return true;
        }

        return false;
    }

    /**
     * Récupère l'image pour le message
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            uploadImage(imageUri);
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
        actTchatLoader = findViewById(R.id.tchatAct_loader);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        actTchatRecycler.setLayoutManager(manager);
        adapter = new TchatAdapter();
        actTchatRecycler.setAdapter(adapter);

        actTchatSendBtn.setOnClickListener(this);
        actTchatSendBtn.setTag(ACT_TCHAT_SEND_BTN);
        actTchatMessage.setOnEditorActionListener(this);
        actTchatImageBtn.setOnClickListener(this);
        actTchatImageBtn.setTag(ACT_TCHAT_IMG_BTN);
    }

    /**
     * Initialisation des éléments nécessaires à l'utilisation de Firebase pour l'activité
     */
    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReferenceFromUrl(STORAGE_PATH).child(STORAGE_REF);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    attachChildListener();
                    username = preferences.getString(PREF_PSEUDO, null);
                    userId = user.getUid();
                    adapter.setUser(user);
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
            childEventListener = new TchatChildEventListener(adapter, actTchatRecycler);
        }
        databaseReference.child(DB_MESSAGES).limitToLast(100).addChildEventListener(childEventListener);
    }

    /**
     * Retire l'évenement pour le tchat
     */
    private void detachChildListener() {
        if (childEventListener != null) {
            databaseReference.child(DB_MESSAGES).removeEventListener(childEventListener);
            childEventListener = null;
        }
    }

    /**
     * Envoie un message
     */
    private void sendMessage(String imageUrl) {
        Message message = null;

        if (imageUrl == null) {
            String content = actTchatMessage.getText().toString();
            if (!TextUtils.isEmpty(content)) {
                message = new Message(userId, username, content, null);
            }
        } else {
            message = new Message(userId, username, null, imageUrl);
        }

        databaseReference.child(DB_MESSAGES).push().setValue(message);
        actTchatMessage.setText("");
    }

    /**
     * Permet de rechercher une image dans son téléphone
     */
    private void pickImage() {
        Intent picker = new Intent(Intent.ACTION_GET_CONTENT);
        picker.setType("image/jpeg");
        picker.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(picker, "Sélectionner une image"), SELECT_PHOTO);
    }

    /**
     * Upload l'image
     * @param imageUri
     */
    private void uploadImage(Uri imageUri) {
        uploadTask = storageReference.child(UUID.randomUUID() + ".jpg").putFile(imageUri);
        actTchatLoader.setVisibility(View.VISIBLE);
        actTchatImageBtn.setEnabled(false);
        addUploadListener(uploadTask);
    }

    /**
     * Regarde l'avancement de l'upload d'image
     * @param task
     */
    private void addUploadListener(UploadTask task) {
        OnCompleteListener<UploadTask.TaskSnapshot> completeListener = new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    Uri imageUrl = task.getResult().getDownloadUrl();
                    if (imageUrl != null) {
                        sendMessage(imageUrl.toString());
                    }
                } else {
                    Toast.makeText(TchatActivity.this, "Impossible d'envoyer l'image", Toast.LENGTH_SHORT).show();
                }
                actTchatLoader.setVisibility(GONE);
                actTchatImageBtn.setEnabled(true);
            }
        };

        OnProgressListener<UploadTask.TaskSnapshot> onProgressListener = new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double percent = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                actTchatLoader.setProgress((int) percent);
            }
        };

        task.addOnCompleteListener(this, completeListener);
        task.addOnProgressListener(this, onProgressListener);
    }
}
