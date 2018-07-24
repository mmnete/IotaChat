package com.companywesbite.iotachat;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {


    private List<Messages> smsList;

    public MessageAdapter(List<Messages>  smsList)
    {
        this.smsList = smsList;
    }


    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_message_layout, parent, false);

        return new MessageViewHolder(v);
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

        Messages c = smsList.get(position);
        holder.messageText.setText(c.getMessage());
        holder.timeText.setText(String.valueOf(c.getTime()));

        if(c.getSender_id() != null)
        {
            if(c.getSender_id().compareTo(FirebaseAuth.getInstance().getCurrentUser().getUid()) == 0)
            {
                holder.messageText.setBackgroundColor(Color.argb(200,220,217,205));
                holder.messageText.setTextAlignment(Gravity.RIGHT);
            }
        }




    }

    @Override
    public int getItemCount() {
        return smsList.size();
    }



    public class MessageViewHolder extends RecyclerView.ViewHolder
    {

        public TextView messageText;
        public TextView timeText;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.one_message);
            timeText = (TextView) itemView.findViewById(R.id.one_message_time);
        }
    }
}
