package com.companywesbite.iotachat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notificationTitle = remoteMessage.getNotification().getTitle();
        String notificationMessage = remoteMessage.getNotification().getBody();
        String click_action = remoteMessage.getNotification().getClickAction();
        //Whenever a message is recieved.....
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "001")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notificationTitle)
                .setContentText(notificationMessage)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent resultIntent = new Intent(click_action);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pendingIntent);


        int notificationID = (int) System.currentTimeMillis();

        NotificationManager mNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyManager.notify(notificationID, mBuilder.build());



    }
}
