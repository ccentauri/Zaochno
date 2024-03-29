package ru.zaochno.zaochno.ui.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import ru.zaochno.zaochno.R;
import ru.zaochno.zaochno.data.model.Message;
import ru.zaochno.zaochno.data.utils.DateUtils;
import ru.zaochno.zaochno.ui.holder.MessageListViewHolder;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListViewHolder> {
    private List<Message> messages;
    private Context context;
    private OnMessageActionListener onMessageActionListener;

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
        final Message message = messages.get(position);

        if (onMessageActionListener != null) {
            holder.containerContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onMessageActionListener.onMessageClick(message);
                }
            });

            holder.ibDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onMessageActionListener.onMessageDelete(message);
                }
            });
        }

        if (message.getDate() != null)
            holder.tvDate.setText(DateUtils.millisToPattern(message.getDate(), DateUtils.PATTERN_DEFAULT));
        else
            holder.tvDate.setText(R.string.message_no_date);

        if (message.getTitle() != null)
            holder.tvTitle.setText(message.getTitle());

        if (message.getMessage() != null)
            holder.tvText.setText(message.getMessage());

        if (message.getRead() != null) {
            if (message.getRead()) {
                // Message is read
                holder.containerContent.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.md_light_primary_text));
                holder.ibDelete.setBackgroundResource(R.drawable.oval_grey);
                holder.tvStatus.setText("(прочитанно)");
            } else if (!message.getRead() && TextUtils.isEmpty(message.getAnswer())) {
                // Message is not read & has no answer
                holder.containerContent.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.md_light_primary_text));
                holder.ibDelete.setBackgroundResource(R.drawable.oval_grey);
                holder.tvStatus.setText("(прочитанно)");
            } else if (!message.getRead() && !TextUtils.isEmpty(message.getAnswer())) {
                // Message is not read & has answer
                holder.containerContent.setBackgroundColor(ContextCompat.getColor(context, R.color.colorBackgroundPrimary));
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.md_red_500));
                holder.ibDelete.setBackgroundResource(R.drawable.oval_white);
                holder.tvStatus.setText("Не прочтено");
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

    public interface OnMessageActionListener {
        void onMessageClick(Message message);

        void onMessageDelete(Message message);
    }

    public void setOnMessageActionListener(OnMessageActionListener onMessageActionListener) {
        this.onMessageActionListener = onMessageActionListener;
    }
}
