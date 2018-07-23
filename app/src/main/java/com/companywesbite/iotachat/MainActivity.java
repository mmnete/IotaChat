package com.companywesbite.iotachat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {


    //This is actually the main page

    private FirebaseAuth mAuth;

    private Toolbar mToolBar;


    private ViewPager viewPager;
    private SectionsPager sectionsPager;
    private TabLayout tabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mToolBar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Iota Chat");
        mToolBar.setTitleTextColor(Color.WHITE);



        //Tabs
        viewPager = (ViewPager) findViewById(R.id.tabPager);
        sectionsPager = new SectionsPager(getSupportFragmentManager());
        viewPager.setAdapter(sectionsPager);


        tabLayout = (TabLayout) findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(viewPager);


    }


    @Override
    protected void onStart() {
        super.onStart();
        //Check if the user is signed in (non-null) and update UI accordingly..
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null)
        {
           logout();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);

         getMenuInflater().inflate(R.menu.main_menu, menu);

         return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);

         if(item.getItemId() == R.id.main_logout_Button)
         {
             //make the user logout...
             FirebaseAuth.getInstance().signOut();
             logout();
         }else if (item.getItemId() == R.id.main_settings_Button)
         {
             Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
             startActivity(settingsIntent);
         }else
         {
             Intent addIntent = new Intent(MainActivity.this, AddFriend.class);
             startActivity(addIntent);
         }

         return true;
    }

    private void logout ()
    {
        Intent startIntent = new Intent(this, StartActivity.class);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startIntent);
        finish();
    }
}
