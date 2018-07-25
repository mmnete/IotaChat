package com.companywesbite.iotachat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.companywesbite.iotachat.Util.UserActions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {


    private Toolbar mToolBar;


    //Saving notifications....
    private DatabaseReference notificationDatabase;


    private DatabaseReference databaseReference;


    private EditText messageContainer;
    private Button sendButton;

    private DatabaseReference mRoot;

    private String current_userid;
    private String chatMateuserid;

    private RecyclerView mChatList;

    private List<Messages> smsList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;

    private DatabaseReference smsDatabase;

    private static final int TOTAL_LOAD_ITEMS_NO = 10;
    private int currentPage = 1;

    private SwipeRefreshLayout swipeRefreshLayout;


    private int itemPos = 0;
    private String lastKey = "";
    private String prev_key = "";

    private ImageView cleanButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        //To send notifications...
        notificationDatabase = FirebaseDatabase.getInstance().getReference().child("sms_notification");


        final String user_id = getIntent().getStringExtra("userid");

        chatMateuserid = user_id;
        current_userid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mToolBar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(mToolBar);

        final ActionBar actionBar = getSupportActionBar();

        final Drawable upArrow =  ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP);


        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setHomeAsUpIndicator(upArrow);
        mToolBar.setTitleTextColor(Color.WHITE);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View actionBarView = inflater.inflate(R.layout.chat_custom_bar, null);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        databaseReference.keepSynced(true);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                String username = dataSnapshot.child("name").getValue().toString();
                ((TextView) actionBarView.findViewById(R.id.username)).setText(username);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        actionBar.setCustomView(actionBarView);


        messageContainer = (EditText) findViewById(R.id.messageContainer);
        sendButton = (Button) findViewById(R.id.sendButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(messageContainer.getText().toString().trim());
            }
        });

        mRoot = FirebaseDatabase.getInstance().getReference();

        //If it is the first time...
        createChat();

        messageAdapter = new MessageAdapter(smsList, chatMateuserid, getApplicationContext());
        mChatList = (RecyclerView) findViewById(R.id.chat_message_list);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
        linearLayoutManager = new LinearLayoutManager(this);

        mChatList.setHasFixedSize(true);
        mChatList.setLayoutManager(linearLayoutManager);


        mChatList.setAdapter(messageAdapter);

        smsDatabase = FirebaseDatabase.getInstance().getReference();


        loadMessages();


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                currentPage++;
                loadMoreMessages();

            }
        });

        cleanButton = (ImageView) findViewById(R.id.clear_messages_button);
        cleanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserActions.deleteAllMessages(chatMateuserid);
                smsList.clear();
                messageAdapter.notifyDataSetChanged();
            }
        });

    }

    //Create chat if this is opened for the first time...
    private void createChat()
    {
        //If the chat is already there then do not worry....
        mRoot.child("Chat").child(current_userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(chatMateuserid))
                {

                    Map chatMap = new HashMap<>();
                    chatMap.put("seen", false);
                    chatMap.put("timestamp", ServerValue.TIMESTAMP);
                    chatMap.put("sms_no", 0);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/"+current_userid+"/"+chatMateuserid, chatMap);
                    chatUserMap.put("Chat/"+chatMateuserid+"/"+current_userid, chatMap);


                    mRoot.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null)
                            {

                                Toast.makeText(ChatActivity.this,"Chat Started",Toast.LENGTH_LONG).show();

                            }

                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }




    //The method to send the message....
    private void sendMessage(String sms)
    {

       if(sms.length() < 1)
           return;

       String current_user_ref = "messages/"+current_userid+"/"+chatMateuserid;
       String chat_user_ref = "messages/" + chatMateuserid + "/" + current_userid;

       DatabaseReference user_message_push = mRoot.child("messages").child(current_userid).child(chatMateuserid).push();

       String push_id = user_message_push.getKey();

       Map messageMap = new HashMap();
       messageMap.put("message", sms );
       messageMap.put("seen", false);
       messageMap.put("type", "text");
       messageMap.put("time", ServerValue.TIMESTAMP);
       messageMap.put("sender_id", FirebaseAuth.getInstance().getCurrentUser().getUid());

       Map messageUserMap = new HashMap();
       messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
       messageUserMap.put(chat_user_ref + "/"+push_id, messageMap);

       mRoot.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
           @Override
           public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


               if(databaseError == null)
               {

                   HashMap<String, String> notificationData = new HashMap<>();
                   notificationData.put("from",current_userid);
                   notificationData.put("type","sms_notification");
                   notificationDatabase.child(chatMateuserid).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                       @Override
                       public void onSuccess(Void aVoid) {




                       }
                   });




               }else
               {
                   Toast.makeText(ChatActivity.this,"Could not send Message", Toast.LENGTH_LONG).show();
               }

           }
       });


        //At the end remove all the text...
        messageContainer.setText("");
    }


    private void loadMoreMessages()
    {

        itemPos = 0;

        DatabaseReference smsRef = mRoot.child("messages").child(current_userid).child(chatMateuserid);

        Query smsQuery = smsRef.orderByKey().endAt(lastKey).limitToLast(TOTAL_LOAD_ITEMS_NO * currentPage);


        smsQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages sms = dataSnapshot.getValue(Messages.class);
                sms.sms_id = dataSnapshot.getKey();


                if(!prev_key.equals(dataSnapshot.getKey()))
                {
                    smsList.add(itemPos++, sms);
                } else {
                    prev_key = lastKey;
                }

                if(itemPos == 1)
                {
                    lastKey = dataSnapshot.getKey();
                }

                messageAdapter.notifyDataSetChanged();

                swipeRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }


    //This gets the messages from the database...
    private void loadMessages(){

        DatabaseReference smsRef = mRoot.child("messages").child(current_userid).child(chatMateuserid);

        Query smsQuery = smsRef.limitToLast(TOTAL_LOAD_ITEMS_NO);

        smsQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages sms = dataSnapshot.getValue(Messages.class);
                sms.sms_id = dataSnapshot.getKey();

                itemPos++;

                if(itemPos == 1)
                {
                   lastKey = dataSnapshot.getKey();
                   prev_key = lastKey;
                }

                Log.d("TAG","The sender is: " + sms.getSender_id()+" The id of this mobile phone "+current_userid);
                smsList.add(sms);
                messageAdapter.notifyDataSetChanged();



                mChatList.scrollToPosition(smsList.size() - 1);

                swipeRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                    loadMessages();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }
}
