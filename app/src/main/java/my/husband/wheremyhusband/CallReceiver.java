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
