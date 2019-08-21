package my.husband.wheremyhusband;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.util.Timer;
import java.util.TimerTask;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static androidx.core.content.ContextCompat.checkSelfPermission;
import static androidx.core.content.ContextCompat.startActivity;

public class CallsSender {
    public void sendCall(Context activity, String number) {
        if (number.startsWith("tel:")) {
            //
        } else {
            number = "tel:" + number;
        }
        Uri uri = Uri.parse(number);
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(uri);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        try {
            activity.startActivity(intent);
        } catch (SecurityException ex) {
            // Why you do this to me
            ex.printStackTrace();
        }
    }
}
