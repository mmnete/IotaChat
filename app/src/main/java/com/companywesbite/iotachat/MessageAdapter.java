package com.companywesbite.iotachat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {


    private List<Messages> smsList;

    private DatabaseReference smsRef;

    private String chatMate = null;
    private Context context;

    public MessageAdapter(List<Messages> smsList, String chatMate, Context context)
    {
        this.smsList = smsList;
        smsRef = FirebaseDatabase.getInstance().getReference().child("messages").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        this.chatMate = chatMate;
        this.context = context;
    }


    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_message_layout, parent, false);

        return new MessageViewHolder(v);
    }

    @SuppressLint({"WrongConstant", "ResourceAsColor"})
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, final int position) {

        final Messages c = smsList.get(position);
        holder.messageText.setText(c.getMessage());
        holder.timeText.setText(String.valueOf(c.getTime()));

        holder.cleanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(c.sms_id != null)
                {
                    smsRef.child(chatMate).child(c.sms_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            dataSnapshot.getRef().removeValue();
                            notifyItemRemoved(position);
                            Toast.makeText(context, "Message removed!", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }
        });


        Log.d("TAG","So now: "+c.getSender_id());
        if(c.getSender_id() != null)
        {

            if(c.getSender_id().compareTo(FirebaseAuth.getInstance().getCurrentUser().getUid()) == 0)
            {
                holder.messageText.setBackgroundColor(Color.RED);
                holder.messageText.setTextAlignment(Gravity.RIGHT);
            }else
            {

                holder.messageText.setBackgroundColor(R.color.colorPrimary);
                holder.messageText.setTextAlignment(Gravity.LEFT);

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
        public ImageView cleanButton;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.one_message);
            timeText = (TextView) itemView.findViewById(R.id.one_message_time);
            cleanButton = (ImageView) itemView.findViewById(R.id.cleanButton);
        }
    }
}
