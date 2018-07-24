package com.companywesbite.iotachat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {


    private Toolbar mToolBar;


    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;

    private TextView username;
    private TextView status;
    private CircleImageView userdp;
    private Button updateStatusButton;
    private Button updateUsernameButton;


    private static final int PICK_FROM_GALLERY = 1;

    private StorageReference storageReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        mToolBar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolBar);
        final Drawable upArrow =  ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        mToolBar.setTitleTextColor(Color.WHITE);

        username = (TextView) findViewById(R.id.displayName);
        status = (TextView) findViewById(R.id.userStatus);
        userdp = (CircleImageView) findViewById(R.id.profile_image);

        //get image from gallery
        userdp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImage();
            }
        });


        updateStatusButton = (Button) findViewById(R.id.changeStatusButton);
        updateStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                View view1 = getLayoutInflater().inflate(R.layout.changestatus_dialog, null);
                final EditText newStatus = (EditText) view1.findViewById(R.id.current_status);
                Button changeStatusButton = (Button) view1.findViewById(R.id.updateStatusButton);
                builder.setView(view1);
                final AlertDialog dialog = builder.create();
                dialog.show();
                changeStatusButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String statusValue = newStatus.getText().toString().trim();
                        if(statusValue.length() > 0)
                        {
                            databaseReference.child("status").setValue(statusValue);
                        }
                        dialog.dismiss();
                    }
                });
            }
        });

        updateUsernameButton = (Button) findViewById(R.id.changeUsernameButton);
        updateUsernameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                View view1 = getLayoutInflater().inflate(R.layout.changeusername_dialog, null);
                final EditText newUsername = (EditText) view1.findViewById(R.id.current_username);
                Button changeUsernameButton = (Button) view1.findViewById(R.id.updateUsernameButton);
                builder.setView(view1);
                final AlertDialog dialog = builder.create();
                dialog.show();
                changeUsernameButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String usernameValue = newUsername.getText().toString().trim();
                        if(usernameValue.length() > 0)
                        {
                            databaseReference.child("name").setValue(usernameValue);
                        }
                        dialog.dismiss();
                    }
                });
            }
        });


        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        String uid = currentUser.getUid();


        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        databaseReference.keepSynced(true);


        storageReference = FirebaseStorage.getInstance().getReference();


    }

    @Override
    protected void onResume() {
        super.onResume();

        //Update the current information....



        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Now we can get the data....
                username.setText(dataSnapshot.child("name").getValue().toString());
                status.setText(dataSnapshot.child("status").getValue().toString());

                StorageReference finalRef = storageReference.child("profile_images").child(currentUser.getUid()+".jpg");
                finalRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(final Uri uri) {

                        // The picaso library helps place the image there...
                        Picasso.get().load(uri).networkPolicy(NetworkPolicy.OFFLINE).into(userdp, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(uri).into(userdp);
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                        Log.d("TAG",exception.getMessage());
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getImage()
    {
        try {
            if (ActivityCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
            } else {
               /* Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, PICK_FROM_GALLERY);
               */
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
    {
        switch (requestCode) {
            case PICK_FROM_GALLERY:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                     getImage();
                } else {
                    //do something like displaying a message that he didn`t allow the app to access gallery and you wont be able to let him select from gallery
                    Toast.makeText(SettingsActivity.this, "You need to allow the application to Access Gallery!",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (reqCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try {
                    final Uri imageUri = resultUri;

                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    StorageReference finalRef = storageReference.child("profile_images").child(currentUser.getUid()+".jpg");
                    finalRef.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if(task.isSuccessful())
                            {
                                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                                userdp.setImageBitmap(selectedImage);
                            }else
                            {
                                Toast.makeText(SettingsActivity.this,"Could not update Image!",Toast.LENGTH_LONG).show();
                            }

                        }
                    });


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(SettingsActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this,"You have not picked an image",Toast.LENGTH_LONG).show();
            }
        }


    }



}
