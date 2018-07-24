package com.companywesbite.iotachat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class ProfileActivity extends AppCompatActivity {


    private ImageView profileDp;
    private TextView profileDisplayName;
    private TextView profileStatus;
    private Button unfriendButton;
    private Button chatButton;
    private String userid = null;
    private FirebaseUser current_user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        userid = getIntent().getStringExtra("userid");

        current_user = FirebaseAuth.getInstance().getCurrentUser();

        profileDp = (ImageView) findViewById(R.id.profileUserdp);
        profileDisplayName = (TextView) findViewById(R.id.profileDisplayName);
        profileStatus = (TextView) findViewById(R.id.profileStatus);

        unfriendButton = (Button) findViewById(R.id.unfriendButton);
        unfriendButton.setOnClickListener(new View.OnClickListener() {

            //Now you unfriend the user...


            @Override
            public void onClick(View view) {
                DatabaseReference tempref = FirebaseDatabase.getInstance().getReference();
                Query requestQuery = tempref.child("Friends").child(current_user.getUid()).orderByChild("friend").equalTo(userid);

                requestQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                            appleSnapshot.getRef().removeValue();

                            DatabaseReference tempref1 = FirebaseDatabase.getInstance().getReference();
                            Query requestQuery1 = tempref1.child("Friends").child(userid).orderByChild("friend").equalTo(current_user.getUid());

                            requestQuery1.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot1) {
                                    for(DataSnapshot appleSnapshot1: dataSnapshot1.getChildren()){

                                        appleSnapshot1.getRef().removeValue();

                                        Intent removeFriendIntent = new Intent(ProfileActivity.this, MainActivity.class);
                                        startActivity(removeFriendIntent);
                                        finish();

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
        });



        chatButton = (Button) findViewById(R.id.chatButton);


    }


    @Override
    protected void onStart() {
        super.onStart();
        putValues();
    }

    public void putValues()
    {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference finalRef = storageReference.child("profile_images").child(userid+".jpg");
        finalRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                // The picaso library helps place the image there...
                Picasso.get().load(uri).into(profileDp);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.d("TAG",exception.getMessage());
            }
        });
        DatabaseReference tempRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userid);

        tempRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                profileDisplayName.setText(dataSnapshot.child("name").getValue().toString());
                profileStatus.setText(dataSnapshot.child("status").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }





}
