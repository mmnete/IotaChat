package com.companywesbite.iotachat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {


    private EditText reg_username;
    private EditText reg_email;
    private EditText reg_password;
    private Button reg_button;

    private Toolbar mToolBar;

    //Registration progress...
    private ProgressDialog mRegProgress;

    //Firebase ---
    private FirebaseAuth mAuth;

    //Firebase database---
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        reg_username = (EditText) findViewById(R.id.reg_display_name);
        reg_email = (EditText) findViewById(R.id.reg_email);
        reg_password = (EditText) findViewById(R.id.reg_password);
        reg_button = (Button) findViewById(R.id.reg_button);
        mToolBar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        reg_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String display_name = reg_username.getText().toString().trim();
                String display_email = reg_email.getText().toString().trim();
                String display_password = reg_password.getText().toString().trim();

                ConnectivityManager connectivityManager
                        = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if(!(activeNetworkInfo != null && activeNetworkInfo.isConnected()))
                {

                    Toast.makeText(RegisterActivity.this,"Error: No internet Connection!",Toast.LENGTH_LONG).show();
                    return;
                }

                if(display_name.length() > 1  && display_email.length() > 1  & display_password.length() > 5)
                {
                    mRegProgress.setTitle("Creating account");
                    mRegProgress.setMessage("Loading..");
                    mRegProgress.setCancelable(false);
                    mRegProgress.show();
                    register_user(display_name, display_email, display_password);
                }else if(display_password.length() < 5)
                {
                    Toast.makeText(RegisterActivity.this,"Password too short!",Toast.LENGTH_LONG).show();
                }



            }
        });

        mRegProgress = new ProgressDialog(this);


        mAuth = FirebaseAuth.getInstance();





    }



    private void register_user(final String username, String email, String password)
    {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {


                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                            String userId = current_user.getUid();

                            databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

                            HashMap<String, String> userHash = new HashMap<String, String>();
                            userHash.put("name",username);
                            userHash.put("status","Hi there I'm using Iota chat.");
                            userHash.put("image","default");
                            userHash.put("thumb_image","def");

                            databaseReference.setValue(userHash).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        mRegProgress.dismiss();

                                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();

                                    }else
                                    {
                                        mRegProgress.hide();

                                        Toast.makeText(RegisterActivity.this, "Lost connection to database",Toast.LENGTH_LONG).show();
                                        try {
                                            throw task.getException();
                                        } catch(Exception e) {
                                            Log.d("TAG",e.getMessage());
                                        }

                                    }

                                }
                            });


                            // Sign in success, update UI with the signed-in user's information
                          /* Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                           finish();

                           */

                        } else {
                            // If sign in fails, display a message to the user.

                            mRegProgress.hide();

                           try {
                                throw task.getException();
                            } catch(FirebaseAuthWeakPasswordException e) {
                               Toast.makeText(RegisterActivity.this, "Error: Weak Password. Make it a bit longer!",Toast.LENGTH_LONG).show();
                           } catch(FirebaseAuthInvalidCredentialsException e) {
                               Toast.makeText(RegisterActivity.this, "Error: Invalid email!",Toast.LENGTH_LONG).show();
                           } catch(FirebaseAuthUserCollisionException e) {
                               Toast.makeText(RegisterActivity.this, "Error: User already exists. Please try logging in!",Toast.LENGTH_LONG).show();
                           } catch(Exception e) {
                               Toast.makeText(RegisterActivity.this, "Error: Could not create account!",Toast.LENGTH_LONG).show();
                           }


                        }

                        // ...
                    }
                });
    }
}
