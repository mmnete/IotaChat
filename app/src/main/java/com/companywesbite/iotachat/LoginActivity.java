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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {



    private Toolbar mToolBar;


    private EditText login_email;
    private EditText login_password;
    private Button login_button;


    private ProgressDialog mLogProgress;

    //Firebase----
    private FirebaseAuth mAuth;

    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolBar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Log In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        login_email = (EditText) findViewById(R.id.login_email);
        login_password = (EditText) findViewById(R.id.login_password);
        login_button = (Button) findViewById(R.id.login_button);

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = login_email.getText().toString().trim();
                String password = login_password.getText().toString().trim();

                ConnectivityManager connectivityManager
                        = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if(!(activeNetworkInfo != null && activeNetworkInfo.isConnected()))
                {

                    Toast.makeText(LoginActivity.this,"Error: No internet Connection!",Toast.LENGTH_LONG).show();
                    return;
                }

                if(email.length() > 1 && password.length() > 5)
                {
                    mLogProgress.setTitle("Logging in");
                    mLogProgress.setMessage("Loading..");
                    mLogProgress.setCancelable(false);
                    mLogProgress.show();
                    login_user(email, password);
                }else if(password.length() < 5)
                {
                    Toast.makeText(LoginActivity.this,"Password too short!",Toast.LENGTH_LONG).show();
                }




            }
        });

        mLogProgress = new ProgressDialog(this);


        mAuth = FirebaseAuth.getInstance();

    }



    private void login_user(String email, String password)
    {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    mLogProgress.dismiss();

                    String deviceToken  = FirebaseInstanceId.getInstance().getToken();
                    String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    databaseReference.child(user_id).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Sign in success, update UI with the signed-in user's information
                            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();
                        }
                    });



                } else {
                    // If sign in fails, display a message to the user.

                    mLogProgress.hide();
                    Toast.makeText(LoginActivity.this,"Error: Make sure this email is registered in Iota! Or You have the right credentials!",Toast.LENGTH_LONG).show();


                }


            }
        });
    }




}
