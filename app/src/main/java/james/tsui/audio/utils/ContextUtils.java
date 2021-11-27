package james.tsui.audio.utils;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ContextUtils {
    private static final String TAG = ContextUtils.class.getSimpleName();
    private static Application mApplication;

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
}
