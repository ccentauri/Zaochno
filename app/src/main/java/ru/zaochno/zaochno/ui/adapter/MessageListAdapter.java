package ru.zaochno.zaochno.ui.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import ru.zaochno.zaochno.R;
import ru.zaochno.zaochno.data.model.Message;
import ru.zaochno.zaochno.ui.holder.MessageListViewHolder;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListViewHolder> {
    private List<Message> messages;
    private Context context;

    public MessageListAdapter(List<Message> messages, Context context) {
        this.messages = messages;
        this.context = context;
    }

    @Override
    public MessageListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MessageListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false));
    }

    @Override
    public void onBindViewHolder(MessageListViewHolder holder, int position) {
        Message message = messages.get(position);

        if (message.getTitle() != null)
            holder.tvTitle.setText(message.getTitle());

        if (message.getRead() != null)
            holder.tvStatus.setText(message.getRead() ? "(прочитанно)" : "Не прочтено");

        if (message.getMessage() != null)
            holder.tvText.setText(message.getMessage());

        if (message.getRead() != null) {
            if (message.getRead()) {
                // Message is read
                holder.containerContent.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.md_light_primary_text));
                holder.ibCancel.setBackgroundResource(R.drawable.oval_grey);
            } else {
                // Message is not read
                holder.containerContent.setBackgroundColor(ContextCompat.getColor(context, R.color.colorBackgroundPrimary));
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.md_red_500));
                holder.ibCancel.setBackgroundResource(R.drawable.oval_white);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public List<Message> getMessages() {
        return messages;
    }
}