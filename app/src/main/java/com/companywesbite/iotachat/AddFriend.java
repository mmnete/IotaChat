package com.companywesbite.iotachat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.companywesbite.iotachat.Util.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddFriend extends AppCompatActivity {


    private Toolbar mToolBar;

    private ListView listView;
    private SearchView searchBar;

    private DatabaseReference databaseReference;

    private StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);


        mToolBar = (Toolbar) findViewById(R.id.addUserAppBar);
        setSupportActionBar(mToolBar);
        final Drawable upArrow =  ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setTitle("Add User");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        mToolBar.setTitleTextColor(Color.WHITE);

        listView = (ListView) findViewById(R.id.currentUserList);
        searchBar = (SearchView) findViewById(R.id.searchUserInput);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
               searchUser(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
               searchUser(s);
                return false;
            }
        });

        storageReference = FirebaseStorage.getInstance().getReference();
    }





    private void searchUser (String qname)
    {
        DatabaseReference ref = databaseReference.child("Users");

        final List<User> userQueryResult = new ArrayList<>();
        ref.orderByChild("name").equalTo(qname).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                userQueryResult.clear();

                for (DataSnapshot postSnapshot: snapshot.getChildren()) {

                    // here you can access to name property like university.name
                    User user = new User();
                    user.username = postSnapshot.child("name").getValue().toString();
                    user.userid = postSnapshot.getKey();



                    if(FirebaseAuth.getInstance().getCurrentUser().getUid().compareTo(user.userid) == 0)
                    {
                        user.me = true;
                    }
                    user.status = postSnapshot.child("status").getValue().toString();
                    userQueryResult.add(user);
                }

                final MySimpleArrayAdapter[] adapter = new MySimpleArrayAdapter[1];

                for(int i = 0; i < userQueryResult.size(); i++)
                {

                    DatabaseReference tempRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests").child(userQueryResult.get(i).userid);

                    final int finalI = i;
                    tempRef.orderByChild("request").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                                User user = new User();
                                user = userQueryResult.get(finalI);
                                user.friendRequested = true;
                                userQueryResult.set(finalI, user);
                            }

                            if(finalI == userQueryResult.size() - 1)
                            {
                                //check if I am friends with this person...
                                for(int i1 = 0; i1 < userQueryResult.size(); i1++)
                                {
                                    DatabaseReference tempRef1 = FirebaseDatabase.getInstance().getReference().child("Friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                                    final int finalI1 = i1;
                                    tempRef1.orderByChild("friend").equalTo(userQueryResult.get(finalI1).userid).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                                                User user = new User();
                                                user = userQueryResult.get(finalI1);
                                                user.friend = true;
                                                userQueryResult.set(finalI1, user);
                                            }

                                            if(finalI1 == userQueryResult.size() - 1)
                                            {
                                                //This is the last value...
                                                adapter[0] = new MySimpleArrayAdapter(AddFriend.this,
                                                        R.layout.user_list_item, userQueryResult, storageReference, FirebaseAuth.getInstance().getCurrentUser());
                                                listView.setAdapter(adapter[0]);
                                            }

                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });


                                }
                            }


                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }






                }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



}


class MySimpleArrayAdapter extends ArrayAdapter<User> {


    private DatabaseReference notificationDatabase;

    private StorageReference storageReference;

    private DatabaseReference databaseReference;

    private FirebaseUser currentUser;

    List<User> values = new ArrayList<>();
    public MySimpleArrayAdapter(Context context, int textViewResourceId,
                                List<User> objects, StorageReference storageReference, FirebaseUser currentUser) {
        super(context, textViewResourceId, objects);

        values = objects;
        this.storageReference = storageReference;
        this.currentUser = currentUser;

        notificationDatabase = FirebaseDatabase.getInstance().getReference().child("notification");


    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.user_list_item, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.dpname);
        final CircleImageView circleImageView = (CircleImageView) rowView.findViewById(R.id.userdp);
        final Button add = (Button) rowView.findViewById(R.id.addButton);

        // change the icon for Windows and iPhone
        final User s = values.get(position);
        textView.setText(s.username);
        StorageReference finalRef = storageReference.child("profile_images").child(s.userid+".jpg");
        finalRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                // The picaso library helps place the image there...
                Picasso.get().load(uri).into(circleImageView);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.d("TAG",exception.getMessage());
            }
        });

        if(s.me)
        {
            add.setText("ME");
            add.setBackgroundColor(Color.GREEN);
        }else if(s.friendRequested)
        {
            add.setText("SENT");
            add.setBackgroundColor(Color.GREEN);
        } else if(s.friend)
        {
            add.setText("FRIEND");
            add.setBackgroundColor(Color.GREEN);
        }else
        {
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    databaseReference = FirebaseDatabase.getInstance().getReference().child("FriendRequests").child(s.userid);

                    Map<String, String> friendMap = new HashMap<String, String>();

                    friendMap.put("request", currentUser.getUid());

                    databaseReference.push().setValue(friendMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {


                                HashMap<String, String> notificationData = new HashMap<>();
                                notificationData.put("from",currentUser.getUid());
                                notificationData.put("type","sent_request");
                                notificationDatabase.child(s.userid).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        add.setText("SENT");
                                        add.setBackgroundColor(Color.GREEN);


                                    }
                                });


                            }else
                            {
                                Toast.makeText(getContext(),"Could not send request!",Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }
            });
        }



        return rowView;
    }

}
