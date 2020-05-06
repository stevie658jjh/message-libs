package lib.message.theme.utils;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

public class MyApplication extends Application {
    public static final String TAG = MyApplication.class
            .getSimpleName();

    /**
     * Using this flat to detect where the current Message is
     * Using for {@link lib.message.theme.receiver.SmsReceiver#onReceive(Context, Intent)}
     * If the current message is showing, so do not show the notification when the new message come
     */
    public static long currentMessageId = 0;

    private static MyApplication mInstance;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    /**
     * See more {@link #currentMessageId}
     *
     * @return #currentMessageId
     */
    public static long getCurrentMessageId() {
        return currentMessageId;
    }

    /**
     * See more {@link #currentMessageId}
     *
     * @param currentMessageId currentMessage user is chatting
     */
    public static void setCurrentMessageId(long currentMessageId) {
        MyApplication.currentMessageId = currentMessageId;
    }
}
