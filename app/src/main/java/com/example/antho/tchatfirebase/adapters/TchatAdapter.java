package com.example.antho.tchatfirebase.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.antho.tchatfirebase.R;
import com.example.antho.tchatfirebase.entities.Message;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by antho on 04/02/2018.
 */

public class TchatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int SELF_MESSAGE = 0;
    private static final int OTHER_MESSAGE = 1;
    private static final int IMG_MESSAGE = 2;

    private List<Message> messages;
    private FirebaseUser user;

    public TchatAdapter() {
        messages = new ArrayList<>();
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

    public void clearMessages() {
        messages.clear();
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return messages.get(position).getUid().hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.size() > 0) {
            Message message = messages.get(position);

            if (message.getContent() == null && message.getImageUrl() != null) {
                return IMG_MESSAGE;
            }

            if (message.getUserId().equals(user.getUid())) {
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
            case IMG_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_image, parent,  false);
                return new ImageMessageViewHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position) ;

        switch (holder.getItemViewType()) {
            case OTHER_MESSAGE:
                ((OtherMessageViewHolder) holder).bind(message);
                break;
            case SELF_MESSAGE:
                ((SelfMessageViewHolder) holder).bind(message);
                break;
            case IMG_MESSAGE:
                ((ImageMessageViewHolder) holder).bind(message);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private class SelfMessageViewHolder extends RecyclerView.ViewHolder {

        private TextView selfMessage;

        public SelfMessageViewHolder(View itemView) {
            super(itemView);
            selfMessage = itemView.findViewById(R.id.rowSelfMessage_message);
        }

        void bind(Message message) {
            selfMessage.setText(message.getContent());
        }
    }

    private class OtherMessageViewHolder extends RecyclerView.ViewHolder {

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

    private class ImageMessageViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;
        private TextView username;

        public ImageMessageViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.rowImage_image);
            username = itemView.findViewById(R.id.rowImage_imageUsername);
        }

        void bind(Message message) {
            username.setText(message.getUsername());
            Glide.with(image.getContext()).load(message.getImageUrl()).override(500, 500).fitCenter().placeholder(R.drawable.ic_launcher_background).into(image);
        }
    }
}
