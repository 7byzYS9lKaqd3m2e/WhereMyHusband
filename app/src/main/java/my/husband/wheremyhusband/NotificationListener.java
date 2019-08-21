package my.husband.wheremyhusband;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class NotificationListener extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Bundle extras = sbn.getNotification().extras;
        if ("Ongoing call".equals(extras.getString(Notification.EXTRA_TEXT))) {
            Intent intent = new Intent("my.husband.wheremyhusband.PhonecallReceiver.OUTGOING_CALL_RECEIVED");
            sendBroadcast(intent, "android.permission.READ_PHONE_STATE");
        }
        //else if ("Dialing".equals(extras.getString(Notification.EXTRA_TEXT))) { // Does nothing here }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Does nothing here
        //super.onNotificationRemoved(sbn);
    }
}
