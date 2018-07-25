package com.companywesbite.iotachat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {



    private EditText email;
    private Button changeButton;
    private Button backButton;
    private TextView alert;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);


        email = (EditText) findViewById(R.id.forgotEmail);
        changeButton = (Button) findViewById(R.id.forgotButton);
        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePassword();
            }
        });
        backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ForgotPassword.this, StartActivity.class);
                startActivity(i);
                finish();
            }
        });
        alert = (TextView) findViewById(R.id.alert);

        mAuth = FirebaseAuth.getInstance();
    }



    private void changePassword()
    {

        String emailValue = email.getText().toString().trim();

        if(emailValue.length() > 0)
        {

            mAuth.sendPasswordResetEmail(emailValue)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                               alert.setText("An reset password email has been sent to your email!");
                            }else
                            {
                                alert.setText("Error: Make sure you have the right email please!");
                            }
                        }
                    });

        }
    }



}
