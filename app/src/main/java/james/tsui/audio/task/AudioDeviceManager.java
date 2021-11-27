package james.tsui.audio.task;

import android.content.Context;
import android.media.AudioDeviceCallback;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.AudioRecordingConfiguration;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.List;

import james.tsui.audio.utils.Constants;
import james.tsui.audio.utils.ContextUtils;
import james.tsui.audio.utils.Variables;

public class AudioDeviceManager {
    private static final String TAG = AudioDeviceManager.class.getSimpleName();

    private final Handler mHandler = new Handler();
    private final AudioRecordingCallbackImpl mRecordingCallback = new AudioRecordingCallbackImpl();
    private final AudioDeviceCallbackImpl mAudioDeviceCallback = new AudioDeviceCallbackImpl();

    private AudioManager mAudioManager;
    private IStateCallback mCallback;

    private static class Singleton {
        public static final AudioDeviceManager sManager = new AudioDeviceManager();
    }

    public static AudioDeviceManager getInstance() {
        return Singleton.sManager;
    }

    private AudioDeviceManager() {
    }

    public interface IStateCallback {
        void onStateChanged(String state);
    }

    public void registerCallback(IStateCallback callback) {
        mCallback = callback;
        mAudioManager = (AudioManager) ContextUtils.getContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.registerAudioDeviceCallback(mAudioDeviceCallback, mHandler);
        mAudioManager.registerAudioRecordingCallback(mRecordingCallback, mHandler);
    }

    private static class AudioDeviceCallbackImpl extends AudioDeviceCallback {
        @Override
        public void onAudioDevicesAdded(AudioDeviceInfo[] addedDevices) {
            super.onAudioDevicesAdded(addedDevices);

            for (AudioDeviceInfo info : addedDevices) {
                Log.i(TAG, "onAudioDevicesAdded: " + info.getType() + ", address=" + info.getAddress()
                        + ", name=" + info.getProductName().toString() + ", ID=" + info.getId());
            }
        }

        @Override
        public void onAudioDevicesRemoved(AudioDeviceInfo[] removedDevices) {
            super.onAudioDevicesRemoved(removedDevices);

            for (AudioDeviceInfo info : removedDevices) {
                Log.i(TAG, "onAudioDevicesRemoved: " + info.getType() + ", address=" + info.getAddress()
                        + ", name=" + info.getProductName().toString() + ", ID=" + info.getId());
            }
        }
    }

    private static class AudioRecordingCallbackImpl extends AudioManager.AudioRecordingCallback {
        @Override
        public void onRecordingConfigChanged(List<AudioRecordingConfiguration> configs) {
            super.onRecordingConfigChanged(configs);
            Log.i(TAG, "onRecordingConfigChanged " + configs.toString());
        }
    }

    public void showAudioInputDevice() {
        AudioDeviceInfo[] infos = mAudioManager.getDevices(AudioManager.GET_DEVICES_INPUTS);
        for (AudioDeviceInfo info : infos) {
            Log.i(TAG, "input device: " + info.getType() + ", address=" + info.getAddress()
                    + ", name=" + info.getProductName().toString() + ", ID=" + info.getId());
        }
    }

    public void showAudioOutputDevice() {
        AudioDeviceInfo[] infos = mAudioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
        for (AudioDeviceInfo info : infos) {
            Log.i(TAG, "input device: " + info.getType() + ", address=" + info.getAddress()
                    + ", name=" + info.getProductName().toString() + ", ID=" + info.getId());
        }
    }

    public void updateAvailableDevices(@NonNull ArrayAdapter<CharSequence> devAdapter) {
        devAdapter.clear();
        Variables.getInstance().clearAudioDeviceInfo();

        AudioDeviceInfo[] infos = mAudioManager.getDevices(AudioManager.GET_DEVICES_ALL);
        for (AudioDeviceInfo info : infos) {
            int devType = info.getType();
            CharSequence devTypeInCharSequence = Constants.getDeviceTypeString(devType);
            if (devTypeInCharSequence != null) {
                CharSequence tag = devTypeInCharSequence;
                if (devType == AudioDeviceInfo.TYPE_BUILTIN_MIC
                        || devType == AudioDeviceInfo.TYPE_IP
                        || devType == AudioDeviceInfo.TYPE_BUS
                        || devType == Constants.REMOTE_SUBMIX) {
                    tag += "@" + info.getAddress();
                } else if (devType == AudioDeviceInfo.TYPE_TELEPHONY) {
                    tag += "#id-" + info.getId();
                }
                devAdapter.add(tag);
                Variables.getInstance().addAudioDeviceInfo(info, tag);
            }
        }

        updateAudioSate("updateAvailableDevices");
    }

    public AudioDeviceInfo getSelectedAudioDeviceInfoBySpinner(@NonNull String dev) {
        return Variables.getInstance().getAudioDeviceInfoByTag(dev);
    }

    public void setSpeakerphoneOn(Button btn_speaker) {
        btn_speaker.setText("spk" + (mAudioManager.isSpeakerphoneOn() ? "开" : "关"));
        updateAudioSate("setSpeakerphoneOn(" + !mAudioManager.isSpeakerphoneOn() + ")");
        mAudioManager.setSpeakerphoneOn(!mAudioManager.isSpeakerphoneOn());
    }

    public void setBluetoothScoOn(Button btn_bt_sco) {
        btn_bt_sco.setText("sco" + (mAudioManager.isBluetoothScoOn() ? "开" : "关"));
        updateAudioSate("setBluetoothScoOn(" + !mAudioManager.isBluetoothScoOn() + ")");
        if (mAudioManager.isBluetoothScoOn()) {
            mAudioManager.setBluetoothScoOn(false);
            mAudioManager.stopBluetoothSco();
        } else {
            mAudioManager.startBluetoothSco();
            mAudioManager.setBluetoothScoOn(true);
        }
    }

    public void setMode(Spinner spn_audio_mode) {
        int defMode = mAudioManager.getMode();
        int mode = Constants.getModeInt(spn_audio_mode.getSelectedItem().toString());

        String ret = "setMode(" + Constants.getModeString(mode) + ")";
        mAudioManager.setMode(mode);
        if (mAudioManager.getMode() != mode) {
            ret += "un-successfully.";
            mAudioManager.setMode(defMode);
        } else {
            ret += "successfully.";
        }

        updateAudioSate(ret);
    }

    public void updateAudioSate(String pre) {
        String state = "";
        if (pre != null) {
            state += pre + "\n";
        }

        state += "current mode(" + Constants.getModeString(mAudioManager.getMode()) + ")\n"
                + "speaker(" + mAudioManager.isSpeakerphoneOn() + ")"
                + "sco(" + mAudioManager.isBluetoothScoOn() + ")"
                + "scoAvailableOffCall(" + mAudioManager.isBluetoothScoAvailableOffCall() + ")\n";

        mCallback.onStateChanged(state);
    }
}
