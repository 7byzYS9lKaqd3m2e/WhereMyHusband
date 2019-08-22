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
    private static transient boolean isAppInForeground = false;

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
                    t2.setEnabled(true);
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

    static boolean isAppInForeground() {
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
                        Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE,
                } : new String[] {
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.PROCESS_OUTGOING_CALLS,
                        Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE,
                };
                this.requestPermissions(permissions, 99);
            }
        } else if (Build.VERSION.SDK_INT >= 19) {
            String[] permissions = new String[] {
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.PROCESS_OUTGOING_CALLS,
                    Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE,
            };
            // probably no need
        }
    }

    private int getRetryCount(TextView retryCount) {
        if (retryCount.getText().length() > 0) {
            return Integer.parseInt(retryCount.getText().toString());
        } else {
            return Integer.MAX_VALUE;
        }
    }

    private int getSecond(TextView textView) {
        if (textView.getText().length() > 0) {
            return Integer.parseInt(textView.getText().toString());
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
