package james.tsui.audio.utils;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ContextUtils {
    private static final String TAG = ContextUtils.class.getSimpleName();
    private static Application mApplication;

    private static String mRecordName;
    private static int mRecordSampleRate;

    public static void init(Application application) {
        if (mApplication == null) {
            mApplication = application;
        } else {
            throw new IllegalAccessError("Application has been init in " + TAG);
        }
    }

    public static Context getContext() {
        return mApplication;
    }

    @SuppressLint("SimpleDateFormat")
    public static String getNowDay(String format){
        return new SimpleDateFormat(format).format(new Date());
    }

    public static void initRecord(String name, int sampleRate) {
        mRecordName = name;
        mRecordSampleRate = sampleRate;
    }

    public static String getRecordName() {
        return mRecordName;
    }

    public static int getRecordSampleRate() {
        return mRecordSampleRate;
    }
}
