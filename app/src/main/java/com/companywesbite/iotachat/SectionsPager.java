package com.companywesbite.iotachat;


import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class SectionsPager extends FragmentPagerAdapter {


    RequestFragment requestFragment = new RequestFragment();
    ChatsFragment chatsFragment = new ChatsFragment();
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
                return requestFragment;
            case 1:
                return chatsFragment;
            case 2:
                return friendsFragment;
            default:
                return null;
        }


    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch(position)
        {
            case 0:
                 return "Friend Requests";
            case 1:
                return "Chats";
            case 2:
                return "Friends";
            default:
                return null;
        }
    }
}
