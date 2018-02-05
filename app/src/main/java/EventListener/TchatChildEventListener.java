package EventListener;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.antho.tchatfirebase.adapters.TchatAdapter;
import com.example.antho.tchatfirebase.entities.Message;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

/**
 * Created by antho on 04/02/2018.
 */

public class TchatChildEventListener implements ChildEventListener {

    private static final String TAG = "TCHAT_CHILD_EVENT_LISTENER";

    private TchatAdapter adapter;
    private RecyclerView actTchatRecycler;

    public TchatChildEventListener(TchatAdapter adapter, RecyclerView actTchatRecycler) {
        this.adapter = adapter;
        this.actTchatRecycler = actTchatRecycler;
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Log.d(TAG, "onChildAdded");

        Message message = dataSnapshot.getValue(Message.class);
        message.setUid(dataSnapshot.getKey());
        adapter.addMessage(message);
        actTchatRecycler.scrollToPosition(adapter.getItemCount() - 1);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        Message message = dataSnapshot.getValue(Message.class);
        message.setUid(dataSnapshot.getKey());
        adapter.deleteMessage(message);
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
