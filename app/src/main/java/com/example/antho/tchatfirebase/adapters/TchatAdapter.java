package com.example.antho.tchatfirebase.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.antho.tchatfirebase.R;
import com.example.antho.tchatfirebase.entities.Message;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

/**
 * Created by antho on 04/02/2018.
 */

public class TchatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int SELF_MESSAGE = 0;
    private static final int OTHER_MESSAGE = 1;

    private List<Message> messages;
    private FirebaseUser user;

    public TchatAdapter(List<Message> messages) {
        this.messages = messages;
        setHasStableIds(true);
    }

    public void setUser(FirebaseUser user) {
        this.user = user;
    }

    public void addMessage(Message message) {
        messages.add(message);
        notifyDataSetChanged();
    }

    public void deleteMessage(Message message) {
        int index = messages.indexOf(message);
        messages.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public long getItemId(int position) {
        return messages.get(position).getUid().hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.size() > 0) {
            if (messages.get(position).getUserId().equals(user.getUid())) {
                return SELF_MESSAGE;
            } else {
                return OTHER_MESSAGE;
            }
        }

        return super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        switch (viewType) {
            case SELF_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_self_message, parent,  false);
                return new SelfMessageViewHolder(view);
            case OTHER_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_other_message, parent,  false);
                return new OtherMessageViewHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position) ;

        if (holder.getItemViewType() == OTHER_MESSAGE) {
            ((OtherMessageViewHolder) holder).bind(message);
        } else if (holder.getItemViewType() == SELF_MESSAGE) {
            ((SelfMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class SelfMessageViewHolder extends RecyclerView.ViewHolder {

        private TextView selfMessage;

        public SelfMessageViewHolder(View itemView) {
            super(itemView);
            selfMessage = itemView.findViewById(R.id.rowSelfMessage_message);
        }

        void bind(Message message) {
            selfMessage.setText(message.getContent());
        }
    }

    class OtherMessageViewHolder extends RecyclerView.ViewHolder {

        private TextView otherMessage;
        private TextView username;

        public OtherMessageViewHolder(View itemView) {
            super(itemView);
            otherMessage = itemView.findViewById(R.id.rowOtherMessage_message);
            username = itemView.findViewById(R.id.rowOtherMessage_username);
        }

        void bind(Message message) {
            username.setText(message.getUsername());
            otherMessage.setText(message.getContent());
        }
    }
}
