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
