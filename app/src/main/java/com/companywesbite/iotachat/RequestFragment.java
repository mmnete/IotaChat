package com.companywesbite.iotachat;


import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {


    private DatabaseReference databaseReference;

    private ProgressDialog progressDialog;

    private ListView listView;
    private Button refreshButton;

    public RequestFragment() {
        // Required empty public constructor
        databaseReference = FirebaseDatabase.getInstance().getReference();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        progressDialog = new ProgressDialog(getContext());
        View myView = inflater.inflate(R.layout.fragment_request, container, false);
        listView = (ListView) myView.findViewById(R.id.friendRequestList);
        refreshButton = (Button) myView.findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getRequestList();
            }
        });
        return myView;

    }

    @Override
    public void onStart() {
        super.onStart();
        getRequestList();
    }

    private void getRequestList ()
    {

        DatabaseReference ref = databaseReference.child("FriendRequests").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        final List<String> userIDs = new ArrayList<>();
        ref.orderByChild("request").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                userIDs.clear();

                progressDialog.setTitle("Refreshing..");
                progressDialog.setMessage("Checking for new requests");
                progressDialog.show();

                for (DataSnapshot postSnapshot: snapshot.getChildren()) {

                    // here you can access to name property like university.name
                    userIDs.add(postSnapshot.child("request").getValue().toString());

                }

                final List<User> users = new ArrayList<>();
                for(int i = 0; i < userIDs.size(); i++)
                {
                    DatabaseReference tempRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userIDs.get(i));


                    final int finalI = i;
                    tempRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = new User();

                            user.username = dataSnapshot.child("name").getValue().toString();
                            user.userid = userIDs.get(finalI);
                            users.add(user);
                            if(finalI == users.size() - 1)
                            {
                                final MyFriendRequestArrayAdapter myFriendRequestArrayAdapter = new MyFriendRequestArrayAdapter(getContext(), R.layout.friend_request_element, users, FirebaseStorage.getInstance().getReference(), FirebaseAuth.getInstance().getCurrentUser());
                                listView.setAdapter(myFriendRequestArrayAdapter);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }

                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }

}


class MyFriendRequestArrayAdapter extends ArrayAdapter<User> {


    private StorageReference storageReference;

    private DatabaseReference databaseReference;

    private FirebaseUser currentUser;

    List<User> values = new ArrayList<>();
    public MyFriendRequestArrayAdapter (Context context, int textViewResourceId,
                                List<User> objects, StorageReference storageReference, FirebaseUser currentUser) {
        super(context, textViewResourceId, objects);

        values = objects;
        this.storageReference = storageReference;
        this.currentUser = currentUser;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final User s = values.get(position);

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.friend_request_element, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.dpname);
        final CircleImageView circleImageView = (CircleImageView) rowView.findViewById(R.id.userdp);
        final Button add = (Button) rowView.findViewById(R.id.addButton);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Accepting a friend request...
                databaseReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUser.getUid());

                Map<String, String> friendMap = new HashMap<String, String>();

                friendMap.put("friend", s.userid);

                databaseReference.push().setValue(friendMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            add.setText("ADDED");
                            add.setBackgroundColor(Color.GREEN);

                            DatabaseReference tempref = FirebaseDatabase.getInstance().getReference();
                            Query requestQuery = tempref.child("FriendRequests").child(currentUser.getUid()).orderByChild("request").equalTo(s.userid);

                            requestQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                        appleSnapshot.getRef().removeValue();
                                        values.remove(position);
                                        MyFriendRequestArrayAdapter.this.notifyDataSetChanged();
                                    }

                                    DatabaseReference  databaseReference1 = FirebaseDatabase.getInstance().getReference().child("Friends").child(s.userid);


                                    Map<String, String> friendMap1 = new HashMap<String, String>();

                                    friendMap1.put("friend", currentUser.getUid());

                                    databaseReference1.push().setValue(friendMap1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                Toast.makeText(getContext(),"Friend Added!",Toast.LENGTH_LONG).show();
                                            }else
                                            {
                                                Toast.makeText(getContext(),"Error!",Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });



                        }else
                        {
                            Toast.makeText(getContext(),"Error!",Toast.LENGTH_LONG).show();
                        }
                    }
                });




            }
        });

        final Button reject = (Button) rowView.findViewById(R.id.rejectButton);
        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Rejecting friend request..

                            reject.setBackgroundColor(Color.GREEN);

                            DatabaseReference tempref = FirebaseDatabase.getInstance().getReference();
                            Query requestQuery = tempref.child("FriendRequests").child(currentUser.getUid()).orderByChild("request").equalTo(s.userid);

                            requestQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                        appleSnapshot.getRef().removeValue();
                                        values.remove(position);
                                        MyFriendRequestArrayAdapter.this.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });






            }
        });

        // change the icon for Windows and iPhone

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

}

