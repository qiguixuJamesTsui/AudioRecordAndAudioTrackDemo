package james.tsui.audio.utils;

import android.annotation.SuppressLint;
import android.media.AudioDeviceInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Variables {
    private static final String TAG = Variables.class.getSimpleName();

    private Variables() {
    }

    private static class Singleton {
        private static final Variables sInstance = new Variables();
    }

    public static Variables getInstance() {
        return Singleton.sInstance;
    }

    private final Map<AudioDeviceInfo, CharSequence> mAudioDeviceInfoCollection = new ConcurrentHashMap<>();

    public CharSequence addAudioDeviceInfo(AudioDeviceInfo info, CharSequence tag) {
        return mAudioDeviceInfoCollection.put(info, tag);
    }

    public CharSequence removeAudioDeviceInfo(AudioDeviceInfo info) {
        return mAudioDeviceInfoCollection.remove(info);
    }

    public void clearAudioDeviceInfo() {
        mAudioDeviceInfoCollection.clear();
    }

    public AudioDeviceInfo getAudioDeviceInfoByTag(CharSequence tag) {
        for (Map.Entry<AudioDeviceInfo, CharSequence> pair : mAudioDeviceInfoCollection.entrySet()) {
            if (pair.getValue().equals(tag)) {
                return pair.getKey();
            }
        }

        return null;
    }

    private static String mRecordName;
    private static int mRecordSampleRate;

    public void initRecord(String name, int sampleRate) {
        mRecordName = name;
        mRecordSampleRate = sampleRate;
    }

    public String getRecordName() {
        return mRecordName;
    }

    public int getRecordSampleRate() {
        return mRecordSampleRate;
    }

    @SuppressLint("SimpleDateFormat")
    public String getNowDay(String format) {
        if (format == null) {
            format = "yyyy-MM-dd-HH-mm-ss-SSS";
        }

        return new SimpleDateFormat(format).format(new Date());
    }
}
