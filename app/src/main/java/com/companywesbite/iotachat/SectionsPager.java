package com.companywesbite.iotachat;


import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class SectionsPager extends FragmentPagerAdapter {


    RequestFragment requestFragment = new RequestFragment();
    FriendsFragment friendsFragment = new FriendsFragment();

    int pos = 0;
    public SectionsPager(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {


        switch(position)
        {
            case 0:
                return friendsFragment;
            case 1:
                return requestFragment;
            default:
                return null;
        }


    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch(position)
        {
            case 0:
                 return "Friends";
            case 1:
                return "Friend Requests";
            default:
                return null;
        }
    }
}
