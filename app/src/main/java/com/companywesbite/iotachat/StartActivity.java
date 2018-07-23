package com.companywesbite.iotachat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {


    private Button startRegisterButton;
    private Button startLoginButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        startRegisterButton = (Button) findViewById(R.id.startRegisterButton);
        startRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startRegister = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(startRegister);
            }
        });

        startLoginButton = (Button) findViewById(R.id.startLoginButton);
        startLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startLogin = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(startLogin);
            }
        });


    }



}
