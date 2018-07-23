package com.companywesbite.iotachat;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.companywesbite.iotachat.Util.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {


    private ListView listView;
    private Button refreshButton;

    private DatabaseReference storageReference;
    private FirebaseUser current_user;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friends2, container, false);
        listView = (ListView) view.findViewById(R.id.friendList);
        refreshButton = (Button) view.findViewById(R.id.refreshButton);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getFriendList();
    }

    private void getFriendList()
    {

        current_user = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(current_user.getUid());
        final List<String> myFriends_userIds = new ArrayList<>();
        final List<User> users = new ArrayList<>();

        storageReference.orderByChild("friend").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {



                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    myFriends_userIds.add(postSnapshot.child("friend").getValue().toString());
                }


                //Now we need to get the values and display them....
                for(int i = 0; i < myFriends_userIds.size(); i++)
                {

                    DatabaseReference tempRef = FirebaseDatabase.getInstance().getReference().child("Users").child(myFriends_userIds.get(i));


                    final int finalI = i;
                    tempRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = new User();

                            user.username = dataSnapshot.child("name").getValue().toString();
                            user.userid = myFriends_userIds.get(finalI);
                            users.add(user);
                            if(finalI == users.size() - 1)
                            {
                                final MyFriendListArrayAdapter myFriendListArrayAdapter = new MyFriendListArrayAdapter(getContext(), R.layout.friend_request_element, users, FirebaseStorage.getInstance().getReference(), FirebaseAuth.getInstance().getCurrentUser());
                                listView.setAdapter(myFriendListArrayAdapter);
                                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                        Intent profile_intent = new Intent(getContext(), ProfileActivity.class);
                                        profile_intent.putExtra("userid", myFriendListArrayAdapter.getItem(i).userid);
                                        startActivity(profile_intent);
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

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });





    }
}


class MyFriendListArrayAdapter extends ArrayAdapter<User> {


    private StorageReference storageReference;

    private DatabaseReference databaseReference;

    private FirebaseUser currentUser;

    public TextView textView = null;



    List<User> values = new ArrayList<>();

    public MyFriendListArrayAdapter(Context context, int textViewResourceId,
                                List<User> objects, StorageReference storageReference, FirebaseUser currentUser) {
        super(context, textViewResourceId, objects);

        values = objects;
        this.storageReference = storageReference;
        this.currentUser = currentUser;

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // change the icon for Windows and iPhone
        final User s = values.get(position);

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.friend_list_element, parent, false);
        textView = (TextView) rowView.findViewById(R.id.dpname);

        final CircleImageView circleImageView = (CircleImageView) rowView.findViewById(R.id.userdp);
        final Button chat = (Button) rowView.findViewById(R.id.chatButton);


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


        return rowView;
    }


    @Nullable
    @Override
    public User getItem(int position) {
        return values.get(position);
    }
}
