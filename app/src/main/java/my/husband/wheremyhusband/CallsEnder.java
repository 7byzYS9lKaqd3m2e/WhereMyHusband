package my.husband.wheremyhusband;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

public class CallsEnder {

    public boolean disconnectCall(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            final TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager != null && ContextCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED) {
                return telecomManager.endCall();
            }
            return false;
        }
        try {
            TelephonyManager tm = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            Class c = Class.forName(tm.getClass().getName());
            Method m = c.getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            Object telephonyService = m.invoke(tm); // Get the internal ITelephony object
            c = Class.forName(telephonyService.getClass().getName()); // Get its class
            m = c.getDeclaredMethod("endCall"); // Get the "endCall()" method
            m.setAccessible(true); // Make it accessible
            m.invoke(telephonyService); // invoke endCall()
            return true;
        } catch (Exception ex) {
            //return false;
        }
        try {
            String serviceManagerName = "android.os.ServiceManager";
            String serviceManagerNativeName = "android.os.ServiceManagerNative";
            String telephonyName = "com.android.internal.telephony.ITelephony";
            Class<?> telephonyClass;
            Class<?> telephonyStubClass;
            Class<?> serviceManagerClass;
            Class<?> serviceManagerNativeClass;
            Method telephonyEndCall;
            Object telephonyObject;
            Object serviceManagerObject;
            telephonyClass = Class.forName(telephonyName);
            telephonyStubClass = telephonyClass.getClasses()[0];
            serviceManagerClass = Class.forName(serviceManagerName);
            serviceManagerNativeClass = Class.forName(serviceManagerNativeName);
            Method getService = // getDefaults[29];
                    serviceManagerClass.getMethod("getService", String.class);
            Method tempInterfaceMethod = serviceManagerNativeClass.getMethod("asInterface", IBinder.class);
            Binder tmpBinder = new Binder();
            tmpBinder.attachInterface(null, "fake");
            serviceManagerObject = tempInterfaceMethod.invoke(null, tmpBinder);
            IBinder retbinder = (IBinder) getService.invoke(serviceManagerObject, "phone");
            Method serviceMethod = telephonyStubClass.getMethod("asInterface", IBinder.class);
            telephonyObject = serviceMethod.invoke(null, retbinder);
            telephonyEndCall = telephonyClass.getMethod("endCall");
            telephonyEndCall.invoke(telephonyObject);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

}

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

package my.husband.wheremyhusband;

import java.util.Iterator;
import java.util.Timer;

public class CallsSetting {

    /** Action to take when this outgoing call completed */
    public static int END_CALL_OPTION;
    /** Hang up after this duration in second */
    public static int END_CALL_DELAY_DURATION_SECOND;
    // /** Number of calls to perform */ public static int CALL_AGAIN_COUNT;
    /** After hang up, wait for this second before making another call */
    public static int CALL_AGAIN_DELAY_DURATION_SECOND;

    //public static String CALL_NUMBER;
    /**
     * Iterator to provide the next call number, if the call is to be repeated
     */
    public static Iterator<String> CALL_NUMBER_NEXT;

    /**
         * Whether all subsequent re-dialing should be stopped. Bugs can occur when the delay between calls is too large, and this
         * variable is flipped before the delay completed
         */
    public static transient boolean FORCE_STOP = false;

    /**
         *  A timer to perform all serial events
         */
    public static final Timer t = new Timer();

    // SECTION CONSTANTS
    @Deprecated
    /** Hang up asap (supposedly after the call is receievd...) {@deprecated NOT SUPPORTED} */
    public static final int END_CALL_OPTION_ASAP = 0;
    /** Hang up after a constant delay */
    public static final int END_CALL_OPTION_AFTER_DURATION = 1;
    /** Do not hang up automatically */
    public static final int END_CALL_OPTION_CONTINUE = 2;
    /** Hang up after a delay, and redial with given phone numbers serially */
    public static final int END_CALL_OPTION_CALL_ANOTHER_SERIALLY = 11;
    /** Hang up after a delay, and redial with a random number from a given list of numbers */
    public static final int END_CALL_OPTION_CALL_ANOTHER_RANDOM = 12;

}

package my.husband.wheremyhusband;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;

import android.text.TextUtils;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import static my.husband.wheremyhusband.CallsSetting.*;

public class MainActivity extends AppCompatActivity {

    private Set<String> phones = new LinkedHashSet<>();
    private SharedPreferences preferences;
    private transient boolean isAppInForeground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        preferences = getSharedPreferences("mypref", MODE_PRIVATE);

        tryRequestPermissions();

        // Normal request
        Button send = findViewById(R.id.button);
        final RadioGroup gp = findViewById(R.id.radioGroup);
        final TextView phoneNo = findViewById(R.id.editText);
        final TextView durationSec = findViewById(R.id.cutSeconds);
        final CheckBox retry = findViewById(R.id.retry);
        final TextView retrySec = findViewById(R.id.retrySeconds);
        final TextView retryCount = findViewById(R.id.retryTimes);
        final Button stop = findViewById(R.id.forceStop);
        // End of Normal request

        // God mode
        final Button b3 = findViewById(R.id.button3);
        final Button b4 = findViewById(R.id.button4);
        final Button b5 = findViewById(R.id.button5);
        final Button b2 = findViewById(R.id.button2);
        final Button b6 = findViewById(R.id.button6);
        final TextView t2 = findViewById(R.id.editText2);
        Set<String> stored = preferences.getStringSet("Phones", null);
        if (stored != null) {
            phones.addAll(stored);
            StringBuilder sb = new StringBuilder();
            String sss = TextUtils.join("\n", phones);
            t2.setText(sss);
        }
        // End of God mode

        final Context ctx = this;
        final CallsSender sd = new CallsSender();

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FORCE_STOP = false;
                final String phone = phoneNo.getText().toString();
                switch (gp.getCheckedRadioButtonId()) {
                    case R.id.radioCut: // This button is disabled, shouldn't happen
                        END_CALL_OPTION = END_CALL_OPTION_ASAP;
                        END_CALL_DELAY_DURATION_SECOND = 0;
                        break;
                    case R.id.radioCutDelay:
                        END_CALL_OPTION = END_CALL_OPTION_AFTER_DURATION;
                        END_CALL_DELAY_DURATION_SECOND = getSecond(durationSec);
                        break;
                    case R.id.radioNotCut:
                        END_CALL_OPTION = END_CALL_OPTION_CONTINUE;
                        END_CALL_DELAY_DURATION_SECOND = getSecond(durationSec);
                        break;
                }
                if (retry.isChecked()) {
                    final int callRetry = getRetryCount(retryCount);
                    CALL_AGAIN_DELAY_DURATION_SECOND = getSecond(retrySec);
                    CALL_NUMBER_NEXT = new StringItr(callRetry) {
                        @Override
                        protected String getNextString() {
                            return phone;
                        }
                    };
                } else {
                    CALL_AGAIN_DELAY_DURATION_SECOND = 0;
                }
                sd.sendCall(ctx, phone);
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            // Easter egg here: if you have more than one husband to call, just press stop multiple times quickly to enable
            // god mode...
            private int i = 0;
            private long t = System.currentTimeMillis();
            @Override
            public void onClick(View view) {
                FORCE_STOP = true; // this is to stop all subsequent recalls
                if (i == 0) {
                    // Start countdown: press sufficient numbers during countdown...
                    t = System.currentTimeMillis();
                    i++;
                } else if ((System.currentTimeMillis() - t) > 5000) {
                    // 5 seconds exceeded; reset counter
                    i = 0;
                } else if (i > 5) {
                    // Sufficient clicks already, enable advance settings
                    b2.setVisibility(View.VISIBLE);
                    b3.setVisibility(View.VISIBLE);
                    b4.setVisibility(View.VISIBLE);
                    b5.setVisibility(View.VISIBLE);
                    b6.setVisibility(View.VISIBLE);
                    t2.setVisibility(View.VISIBLE);
                    b2.setEnabled(true);
                    b3.setEnabled(true);
                    b4.setEnabled(true);
                    b5.setEnabled(true);
                    b6.setEnabled(true);
                } else {
                    // Keep counting
                    i++;
                }
            }
        });
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // add
                phones.add(phoneNo.getText().toString());
                t2.setText(t2.getText() + "\n" + phoneNo.getText().toString());
            }
        });
        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // random call
                FORCE_STOP = false;
                final int callCount = getRetryCount(retryCount);
                END_CALL_OPTION = END_CALL_OPTION_CALL_ANOTHER_RANDOM;
                CALL_AGAIN_DELAY_DURATION_SECOND = getSecond(retrySec);
                CALL_NUMBER_NEXT = new StringItr(callCount) {
                    private final String[] phone = phones.toArray(new String[]{});
                    private final Random m = new Random();

                    @Override
                    protected String getNextString() {
                        return phone[m.nextInt(phone.length)];
                    }
                };
                sd.sendCall(ctx, CALL_NUMBER_NEXT.next());
            }
        });
        b5.setOnClickListener(new View.OnClickListener() { // serial call
            @Override
            public void onClick(View view) {
                FORCE_STOP = false;
                final int callCount = retryCount.getText().length() > 0 ? Integer.parseInt(retryCount.getText().toString()) : Integer.MAX_VALUE;
                END_CALL_OPTION = END_CALL_OPTION_CALL_ANOTHER_SERIALLY;
                CALL_NUMBER_NEXT = new StringItr(callCount) {
                    private final String[] phone = phones.toArray(new String[]{});
                    @Override
                    protected String getNextString() {
                        return phone[ccount % phone.length];
                    }
                };
                sd.sendCall(ctx, CALL_NUMBER_NEXT.next());
            }
        });
        b6.setOnClickListener(new View.OnClickListener() { // Save
            @Override
            public void onClick(View view) {
                preferences.edit().putStringSet("Phones", phones).apply();
            }
        });
        b2.setOnClickListener(new View.OnClickListener() { // Clear
            @Override
            public void onClick(View view) {
                phones.clear();
                preferences.edit().remove("Phones").apply();
                t2.setText("");
            }
        });
    }

    //@OnLifecycleEvent(Lifecycle.Event.ON_START)
    @Override
    public void onStart() {
        super.onStart();
        isAppInForeground = true;
    }

    @Override
    public void onStop() {
        isAppInForeground = false;
        super.onStop();
    }

    @Override
    public void onDestroy() {
        isAppInForeground = false;
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    boolean isAppInForeground() {
        return isAppInForeground;
    }

    private void tryRequestPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                String[] permissions = Build.VERSION.SDK_INT >= 26 ? new String[] {
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.PROCESS_OUTGOING_CALLS,
                        Manifest.permission.ANSWER_PHONE_CALLS,
                        Manifest.permission.MANAGE_OWN_CALLS,
                } : new String[] {
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.PROCESS_OUTGOING_CALLS,
                };
                this.requestPermissions(permissions, 99);
            }
        } else {
            //
        }
    }

    private int getRetryCount(TextView retryCount) {
        if (retryCount.getText().length() > 0) {
            return Integer.parseInt(retryCount.getText().toString());
        } else {
            return Integer.MAX_VALUE;
        }
    }

    private int getSecond(TextView retrySec) {
        if (retrySec.getText().length() > 0) {
            return Integer.parseInt(retrySec.getText().toString());
        } else {
            return 0;
        }
    }

    private abstract class StringItr implements Iterator<String> {
        int ccount;

        StringItr(int c) {
            this.ccount = c;
        }

        @Override
        public boolean hasNext() {
            return ccount > 0 && !FORCE_STOP;
        }

        @Override
        public String next() {
            if (!hasNext()) { // If FORCE_STOP, clear ccount as well
                ccount = 0;
                return null;
            }
            ccount--;
            return getNextString();
        }

        protected abstract String getNextString();

    }
}

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

package my.husband.wheremyhusband;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import java.util.Date;

public abstract class PhonecallReceiver extends BroadcastReceiver {

    public static final String OUTGOING_CALL_RECEIVED = "OUTGOING_CALL_RECEIVED";

    //The receiver will be recreated whenever android feels like it.  We need a static variable to remember data between instantiations

    protected static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String savedNumber;  //because the passed incoming is only valid in ringing


    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println(intent);
        if (intent.getAction().equals("my.husband.wheremyhusband.PhonecallReceiver.OUTGOING_CALL_RECEIVED")) {
            onNotificationReceived(context, savedNumber);
        } else if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
            savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
            //System.out.println("Should be a new outgoing call...");
        } else {
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            int state = 0;
            if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                state = TelephonyManager.CALL_STATE_IDLE;
            } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                state = TelephonyManager.CALL_STATE_OFFHOOK;
            } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                state = TelephonyManager.CALL_STATE_RINGING;
            }


            onCallStateChanged(context, state, number);
        }
    }

    //Derived classes should override these to respond to specific events of interest
    protected abstract void onIncomingCallReceived(Context ctx, String number, Date start);

    protected abstract void onIncomingCallAnswered(Context ctx, String number, Date start);

    protected abstract void onIncomingCallEnded(Context ctx, String number, Date start, Date end);

    protected abstract void onOutgoingCallStarted(Context ctx, String number, Date start);

    protected abstract void onOutgoingCallEnded(Context ctx, String number, Date start, Date end);

    protected abstract void onMissedCall(Context ctx, String number, Date start);

    protected abstract void onOutgoingCallReceived(Context ctx, String number, Date received);

    //Deals with actual events

    public void onNotificationReceived(Context context, String number) {
        onOutgoingCallReceived(context, number, new Date());
    }

    //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
    public void onCallStateChanged(Context context, int state, String number) {
        if (lastState == state) {
            //No change, debounce extras
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                // Incoming Calls Received
                isIncoming = true;
                callStartTime = new Date();
                savedNumber = number;
                onIncomingCallReceived(context, number, callStartTime);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    // Outgoing Calls Started
                    isIncoming = false;
                    callStartTime = new Date();
                    onOutgoingCallStarted(context, savedNumber, callStartTime);
                } else {
                    // Incoming Calls Answered
                    isIncoming = true;
                    callStartTime = new Date();
                    onIncomingCallAnswered(context, savedNumber, callStartTime);
                }

                break;
            case TelephonyManager.CALL_STATE_IDLE:
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    // Incoming Calls Missed
                    //Ring but no pickup-  a miss
                    onMissedCall(context, savedNumber, callStartTime);
                } else if (isIncoming) {
                    // Incoming Calls Ended
                    onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                } else {
                    // Outgoing Calls Ended
                    onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());
                }
                break;
        }
        lastState = state;
    }
}
package my.husband.wheremyhusband;

import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.Lifecycle;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static my.husband.wheremyhusband.CallsSetting.*;

public class CallReceiver extends PhonecallReceiver {

    private final CallsEnder ed = new CallsEnder();
    private final CallsSender sd = new CallsSender();

    /**
         *  Hang up automatically if the app is in foreground. This minimize the chance of someone calling backs...
         */
    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start)
    {
        //
        if (!(ctx instanceof MainActivity && ((MainActivity) ctx).isAppInForeground())) {
            return;
        }
        ed.disconnectCall(ctx); // Do not receive any calls when doing my business
        System.out.println("Incoming call received");
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start)
    {
        // Why would you receive any calls lol
        System.out.println("Incoming call answered");
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end)
    {
        //
        System.out.println("Incoming acll ended");
    }

    @Override
    protected void onOutgoingCallStarted(final Context ctx, String number, Date start)
    {
        Intent returnActivity = new Intent(ctx, MainActivity.class);
        ctx.startActivity(returnActivity);
        switch (END_CALL_OPTION) {
            case END_CALL_OPTION_ASAP:
                // Hard to identify if the call is received or not... rather skip this option
                ed.disconnectCall(ctx);
                break;
            case END_CALL_OPTION_CONTINUE:
                // Does nothing
                break;
            case END_CALL_OPTION_AFTER_DURATION:
            case END_CALL_OPTION_CALL_ANOTHER_RANDOM:
            case END_CALL_OPTION_CALL_ANOTHER_SERIALLY:
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        ed.disconnectCall(ctx);
                        if (CALL_NUMBER_NEXT != null && CALL_NUMBER_NEXT.hasNext()) {
                            if (CALL_AGAIN_DELAY_DURATION_SECOND > 0) {
                                t.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        String s = CALL_NUMBER_NEXT.next();
                                        if (s == null) {
                                            return;
                                        }
                                        sd.sendCall(ctx, s);
                                    }
                                }, CALL_AGAIN_DELAY_DURATION_SECOND * 1000);
                            } else {
                                String s = CALL_NUMBER_NEXT.next();
                                if (s == null) {
                                    return;
                                }
                                sd.sendCall(ctx, s);
                            }
                        } // else return;
                    }
                }, END_CALL_DELAY_DURATION_SECOND * 1000);
                break;
        }
        //        Timer t = new Timer(); t.schedule(new TimerTask() {
        //            @Override
        //            public void run() {
        //                ed.disconnectCall(ctx);
        //            }
        //        }, 10000);
        System.out.println("Outgoing call started");
    }

    @Override
    protected void onOutgoingCallEnded(final Context ctx, final String number, Date start, Date end)
    {
        //
        System.out.println("Outgoing call ended");
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start)
    {
        // Don't care
        System.out.println("Missed call");
    }

}
