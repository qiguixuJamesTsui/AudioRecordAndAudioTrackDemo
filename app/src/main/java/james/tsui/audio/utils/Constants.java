package james.tsui.audio.utils;

import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.provider.MediaStore;

import java.util.HashMap;
import java.util.Map;

import james.tsui.audio.task.AudioDeviceManager;

public class Constants {

    private static final Map<Integer, String> mSourceMap = new HashMap<Integer, String>() {{
        put(MediaRecorder.AudioSource.DEFAULT, "DEFAULT");
        put(MediaRecorder.AudioSource.MIC, "MIC");
        put(MediaRecorder.AudioSource.VOICE_CALL, "VOICE_CALL");
        put(MediaRecorder.AudioSource.VOICE_COMMUNICATION, "VOICE_COMMUNICATION");
        put(MediaRecorder.AudioSource.CAMCORDER, "CAMCORDER");
    }};

    public static int getSourceInt(String strStream) {
        for (Map.Entry<Integer, String> pair : mSourceMap.entrySet()) {
            if (pair.getValue().equals(strStream)) {
                return pair.getKey();
            }
        }

        return MediaRecorder.AudioSource.DEFAULT;
    }

    public static String getSourceString(int stream) {
        return mSourceMap.get(stream);
    }

    private static final Map<Integer, String> mStreamMap = new HashMap<Integer, String>() {{
        put(AudioManager.STREAM_SYSTEM, "STREAM_SYSTEM");
        put(AudioManager.STREAM_MUSIC, "STREAM_MUSIC");
        put(AudioManager.STREAM_VOICE_CALL, "STREAM_VOICE_CALL");
    }};

    public static int getStreamInt(String strStream) {
        for (Map.Entry<Integer, String> pair : mStreamMap.entrySet()) {
            if (pair.getValue().equals(strStream)) {
                return pair.getKey();
            }
        }

        return AudioManager.STREAM_MUSIC;
    }

    public static String getStreamString(int stream) {
        return mStreamMap.get(stream);
    }

    private static final Map<Integer, String> mChannelInMap = new HashMap<Integer, String>() {{
        put(AudioFormat.CHANNEL_IN_MONO, "CHANNEL_IN_MONO");
        put(AudioFormat.CHANNEL_IN_STEREO, "CHANNEL_IN_STEREO");
    }};

    public static int getChannelInInt(String strChannel) {
        for (Map.Entry<Integer, String> pair : mChannelInMap.entrySet()) {
            if (pair.getValue().equals(strChannel)) {
                return pair.getKey();
            }
        }

        return AudioFormat.CHANNEL_IN_STEREO;
    }

    public static String getChannelInString(int channel) {
        return mChannelInMap.get(channel);
    }

    private static final Map<Integer, String> mChannelOutMap = new HashMap<Integer, String>() {{
        put(AudioFormat.CHANNEL_OUT_MONO, "CHANNEL_OUT_MONO");
        put(AudioFormat.CHANNEL_OUT_STEREO, "CHANNEL_OUT_STEREO");
    }};

    public static int getChannelOutInt(String strChannel) {
        for (Map.Entry<Integer, String> pair : mChannelOutMap.entrySet()) {
            if (pair.getValue().equals(strChannel)) {
                return pair.getKey();
            }
        }

        return AudioFormat.CHANNEL_OUT_STEREO;
    }

    public static String getChannelOutString(int channel) {
        return mChannelOutMap.get(channel);
    }

    private static final Map<Integer, String> mUsageMap = new HashMap<Integer, String>() {{
        put(AudioAttributes.USAGE_UNKNOWN, "USAGE_UNKNOWN");
        put(AudioAttributes.USAGE_MEDIA, "USAGE_MEDIA");
        put(AudioAttributes.USAGE_VOICE_COMMUNICATION, "USAGE_VOICE_COMMUNICATION");
    }};

    public static int getUsageInt(String strUsage) {
        for (Map.Entry<Integer, String> pair : mUsageMap.entrySet()) {
            if (pair.getValue().equals(strUsage)) {
                return pair.getKey();
            }
        }

        return AudioAttributes.USAGE_UNKNOWN;
    }

    public static String getUsageString(int usage) {
        return mUsageMap.get(usage);
    }

    private static final Map<Integer, String> mContentMap = new HashMap<Integer, String>() {{
        put(AudioAttributes.CONTENT_TYPE_UNKNOWN, "CONTENT_UNKNOWN");
        put(AudioAttributes.CONTENT_TYPE_SPEECH, "CONTENT_SPEECH");
        put(AudioAttributes.CONTENT_TYPE_MUSIC, "CONTENT_MUSIC");
    }};

    public static int getContentInt(String strContent) {
        for (Map.Entry<Integer, String> pair : mContentMap.entrySet()) {
            if (pair.getValue().equals(strContent)) {
                return pair.getKey();
            }
        }

        return AudioAttributes.CONTENT_TYPE_UNKNOWN;
    }

    public static String getContentString(int content) {
        return mContentMap.get(content);
    }

    private static final Map<Integer, String> mModeMap = new HashMap<Integer, String>() {{
        put(AudioManager.MODE_NORMAL, "MODE_NORMAL");
        put(AudioManager.MODE_RINGTONE, "MODE_RINGTONE");
        put(AudioManager.MODE_IN_COMMUNICATION, "MODE_IN_COMMUNICATION");
        put(AudioManager.MODE_IN_CALL, "MODE_IN_CALL");
    }};

    public static int getModeInt(String strContent) {
        for (Map.Entry<Integer, String> pair : mModeMap.entrySet()) {
            if (pair.getValue().equals(strContent)) {
                return pair.getKey();
            }
        }

        return AudioManager.MODE_NORMAL;
    }

    public static String getModeString(int content) {
        return mModeMap.get(content);
    }

    public static final int REMOTE_SUBMIX = 25;
    private static final Map<Integer, String> mDeviceMap = new HashMap<Integer, String>() {{
        put(AudioDeviceInfo.TYPE_UNKNOWN, "UNKNOWN"); //0
        put(AudioDeviceInfo.TYPE_BUILTIN_EARPIECE, "BUILTIN_EARPIECE");
        put(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER, "BUILTIN_SPEAKER");
        put(AudioDeviceInfo.TYPE_WIRED_HEADSET, "WIRED_HEADSET");
        put(AudioDeviceInfo.TYPE_WIRED_HEADPHONES, "WIRED_HEADPHONES");
        put(AudioDeviceInfo.TYPE_LINE_ANALOG, "TYPE_LINE_ANALOG"); //5
        put(AudioDeviceInfo.TYPE_LINE_DIGITAL, "LINE_DIGITAL");
        put(AudioDeviceInfo.TYPE_BLUETOOTH_SCO, "BLUETOOTH_SCO");
        put(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP, "BLUETOOTH_A2DP");
        put(AudioDeviceInfo.TYPE_HDMI, "HDMI");
        put(AudioDeviceInfo.TYPE_HDMI_ARC, "HDMI_ARC"); //10
        put(AudioDeviceInfo.TYPE_USB_DEVICE, "USB_DEVICE");
        put(AudioDeviceInfo.TYPE_BUILTIN_MIC, "BUILTIN_MIC"); //15
        put(AudioDeviceInfo.TYPE_TELEPHONY, "TELEPHONY");
        put(AudioDeviceInfo.TYPE_IP, "IP"); //20
        put(AudioDeviceInfo.TYPE_BUS, "BUS");
        put(AudioDeviceInfo.TYPE_USB_HEADSET, "USB_HEADSET");
        put(AudioDeviceInfo.TYPE_HEARING_AID, "HEARING_AID");
        put(REMOTE_SUBMIX, "REMOTE_SUBMIX"); //25
    }};

    public static int getDeviceTypeInt(String strDevType) {
        for (Map.Entry<Integer, String> pair : mDeviceMap.entrySet()) {
            if (pair.getValue().equals(strDevType)) {
                return pair.getKey();
            }
        }

        return AudioDeviceInfo.TYPE_UNKNOWN;
    }

    public static String getDeviceTypeString(int devType) {
        return mDeviceMap.get(devType);
    }

    public static String getPlayFileName(int sampleRate) {
        if (sampleRate == 8000) {
            return "cuiniao8k.pcm";
        } else if (sampleRate == 44100) {
            return "cuiniao441k.pcm";
        } else {
            return "cuiniao.pcm";
        }
    }
}
