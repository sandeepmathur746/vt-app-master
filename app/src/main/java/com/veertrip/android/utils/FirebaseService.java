package com.veertrip.android.utils;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;
import com.veertrip.android.MainActivity;
import com.veertrip.android.R;
import com.veertrip.android.client.PostTokenClient;

import static android.content.ContentValues.TAG;

public class FirebaseService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        PostTokenClient client = new PostTokenClient(null, token, "app");
        client.run();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            try {

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "1");
                mBuilder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND);
                mBuilder.setColor(Color.parseColor("#FF4300"));
                mBuilder.setSmallIcon(R.drawable.app_icon);
                mBuilder.setContentTitle(remoteMessage.getData().get("title"));
                mBuilder.setContentText(remoteMessage.getData().get("body"));
                mBuilder.setAutoCancel(true);

                String imageURL = remoteMessage.getData().get("image");

                if (imageURL != null && !imageURL.isEmpty()) {
                    try {
                        NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle();
                        style.bigPicture(Picasso.get()
                                .load(imageURL)
                                .resize(800, 400)
                                .centerInside()
                                .get());
                        mBuilder.setStyle(style);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                String action = remoteMessage.getData().get("action");
                if (action != null && !action.isEmpty()) {

                    Intent intent2 = new Intent(this, MainActivity.class);
                    intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent2.putExtra("URL", action);

                    int requestID = (int) System.currentTimeMillis();

                    PendingIntent pendingIntent = PendingIntent.getActivity(
                            getApplicationContext(), requestID, intent2,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

                    mBuilder.setContentIntent(pendingIntent);
                }

                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (mNotificationManager != null) {

                    if (Build.VERSION.SDK_INT >= 26) {

                        String id = "veertrip_channel";
                        CharSequence name = "veertrip_channel";
                        String description = "Veertrip Communications";
                        int importance = NotificationManager.IMPORTANCE_LOW;

                        NotificationChannel mChannel = new NotificationChannel(id, name,importance);
                        mChannel.setDescription(description);
                        mChannel.enableLights(true);
                        mChannel.setLightColor(Color.RED);
                        mChannel.enableVibration(true);
                        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                        mNotificationManager.createNotificationChannel(mChannel);

                        mBuilder.setChannelId(id);
                    }

                    mNotificationManager.notify(0, mBuilder.build());

                }

            } catch (Exception e) {
                //Log.d("Exception", e.getMessage());
                e.printStackTrace();
            }

        }
    }

}
