package james.tsui.audio;

import android.Manifest;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ArrayRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import james.tsui.audio.task.AudioDeviceManager;
import james.tsui.audio.task.AudioRecordTask;
import james.tsui.audio.task.AudioTrackTask;
import james.tsui.audio.task.DataWaveDrawer;
import james.tsui.audio.utils.Constants;
import james.tsui.audio.utils.ContextUtils;
import james.tsui.audio.utils.Variables;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks,
        View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_PERMISSION = 1314;
    private static final String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAPTURE_AUDIO_OUTPUT
    };

    private Button btn_rec_capture = null;
    private Button btn_rec_start = null;
    private Button btn_rec_dev = null;
    private Button btn_play_sample = null;
    private Button btn_play_usage = null;
    private Button btn_play_cycle = null;
    private Button btn_play_start = null;
    private Button btn_play_dev = null;
    private Button btn_speaker = null;
    private Button btn_sco = null;

    private Spinner spn_rec_source = null;
    private Spinner spn_rec_rate = null;
    private Spinner spn_rec_channel = null;
    private Spinner spn_play_stream = null;
    private Spinner spn_play_rate = null;
    private Spinner spn_play_channel = null;
    private Spinner spn_play_usage = null;
    private Spinner spn_play_content = null;
    private Spinner spn_audio_mode = null;
    private Spinner spn_device_available = null;

    private ImageView mRecordView = null;
    private TextView tv_audio_state = null;
    private ArrayAdapter<CharSequence> mDeviceAdapter;

    private Context mContext = null;

    private AudioRecordTask mAudioRecordTask;
    private AudioTrackTask mAudioTrackTask;

    private boolean mIsCapture = false;
    private boolean mIsSample = true;
    private boolean mIsUsage = false;
    private boolean mIsCycling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mContext = this.getApplicationContext();
        ContextUtils.init(this.getApplication());
        EasyPermissions.requestPermissions(this, "please", REQUEST_PERMISSION, PERMISSIONS);

        tv_audio_state = (TextView) findViewById(R.id.tv_audio_state);
        String deviceInfo = "Android SDK: " + Build.VERSION.SDK_INT + ", "
                + "Release: " + Build.VERSION.RELEASE + ", "
                + "Brand: " + Build.BRAND + ", "
                + "Device: " + Build.DEVICE + ", "
                + "Id: " + Build.ID + ", "
                + "Hardware: " + Build.HARDWARE + ", "
                + "Manufacturer: " + Build.MANUFACTURER + ", "
                + "Model: " + Build.MODEL + ", "
                + "Product: " + Build.PRODUCT;
        ((TextView) findViewById(R.id.tv_device_info)).setText(deviceInfo);

        btn_rec_capture = getButtonWithClickListener(R.id.btn_rec_capture);
        btn_rec_start = getButtonWithClickListener(R.id.btn_rec_start);
        btn_rec_dev = getButtonWithClickListener(R.id.btn_rec_dev);
        btn_play_sample = getButtonWithClickListener(R.id.btn_play_sample);
        btn_play_usage = getButtonWithClickListener(R.id.btn_play_usage);
        btn_play_cycle = getButtonWithClickListener(R.id.btn_play_cycle);
        btn_play_start = getButtonWithClickListener(R.id.btn_play_start);
        btn_play_dev = getButtonWithClickListener(R.id.btn_play_dev);
        btn_speaker = getButtonWithClickListener(R.id.btn_speaker);
        btn_sco = getButtonWithClickListener(R.id.btn_sco);
        getButtonWithClickListener(R.id.btn_set_mode);
        getButtonWithClickListener(R.id.btn_update_dev);

        mRecordView = (ImageView) findViewById(R.id.img_rec_wave);
        mDeviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);

        spn_rec_source = getSimpleSpinner(R.id.spn_rec_source, R.array.rec_source_entries);
        spn_rec_rate = getSimpleSpinner(R.id.spn_rec_rate, R.array.audio_sample_entries);
        spn_rec_channel = getSimpleSpinner(R.id.spn_rec_channel, R.array.rec_channel_entries);
        spn_play_stream = getSimpleSpinner(R.id.spn_play_stream, R.array.play_stream_type_entries);
        spn_play_rate = getSimpleSpinner(R.id.spn_play_rate, R.array.audio_sample_entries);
        spn_play_channel = getSimpleSpinner(R.id.spn_play_channel, R.array.play_channel_entries);
        spn_play_usage = getSimpleSpinner(R.id.spn_play_usage, R.array.play_usage_entries);
        spn_play_content = getSimpleSpinner(R.id.spn_play_content, R.array.play_content_entries);
        spn_audio_mode = getSimpleSpinner(R.id.spn_audio_mode, R.array.audio_mode_entries);
        spn_device_available = getDropdownSpinner(R.id.spn_dev_available, mDeviceAdapter);

        AudioDeviceManager.getInstance().registerCallback(state -> tv_audio_state.setText(state));
        AudioDeviceManager.getInstance().updateAvailableDevices(mDeviceAdapter);
    }

    private Button getButtonWithClickListener(@IdRes int id) {
        Button btn = (Button) findViewById(id);
        btn.setOnClickListener(this);
        return btn;
    }

    private Spinner getSimpleSpinner(@IdRes int id, @ArrayRes int textArrayResId) {
        Spinner spn = (Spinner) findViewById(id);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                textArrayResId,
                android.R.layout.simple_spinner_item);
        spn.setAdapter(adapter);
        spn.setSelection(0);
        return spn;
    }

    private Spinner getDropdownSpinner(@IdRes int id, ArrayAdapter<CharSequence> adapter) {
        Spinner spn = (Spinner) findViewById(id);
        spn.setAdapter(adapter);
        spn.setSelection(0);
        return spn;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "onRequestPermissionsResult requestCode = " + requestCode);
        for (String p : permissions) {
            Log.i(TAG, "onRequestPermissionsResult permissions = " + p);
        }

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.i(TAG, "onRequestPermissionsResult requestCode=" + requestCode
                + "permissions=" + perms.toString());
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsDenied " + requestCode
                + "permission=" + perms.toString());
    }

    @AfterPermissionGranted(REQUEST_PERMISSION)
    private void AfterPermissionGranted() {
        Log.d(TAG, "AfterPermissionGranted ");
        if (!EasyPermissions.hasPermissions(this, PERMISSIONS)) {
            EasyPermissions.requestPermissions(this, "please", REQUEST_PERMISSION, PERMISSIONS);
        }
    }

    private void setRecordCaptureOn() {
        mIsCapture = !mIsCapture;
        btn_rec_capture.setText(mIsCapture ? "内录" : "外录");
    }

    private final AudioRecordTask.IRecordUiCallback mRecordUiCallback = new AudioRecordTask.IRecordUiCallback() {
        @Override
        public void onCreate(final AudioRecord record) {
            runOnUiThread(() -> Toast.makeText(mContext, "AudioRecord "
                    + "session ID: " + record.getAudioSessionId() + ", "
                    + "audio format: " + record.getAudioFormat() + ", "
                    + "channels: " + record.getChannelCount() + ", "
                    + "sample rate: " + record.getSampleRate(), Toast.LENGTH_LONG).show());
        }

        @Override
        public void onPreExecute() {
            Log.i(TAG, "RecordTask.onPreExecute()");
            btn_rec_capture.setEnabled(false);
            btn_rec_dev.setEnabled(false);
        }

        @Override
        public void onPostExecute(Long aLong) {
            Log.i(TAG, "RecordTask.onPostExecute()");
            btn_rec_capture.setEnabled(true);
            btn_rec_dev.setEnabled(true);
        }

        @Override
        public void onError(int code) {
            Log.i(TAG, "RecordTask.onError()");
        }
    };

    private void startAudioRecordTask() {
        mAudioRecordTask = new AudioRecordTask();
        mAudioRecordTask.registerCallback(mRecordUiCallback);
        AudioRecordTask.Parameters para = new AudioRecordTask.Parameters();

        para.source = Constants.getSourceInt(spn_rec_source.getSelectedItem().toString());
        para.channels = Constants.getChannelInInt(spn_rec_channel.getSelectedItem().toString());
        para.sampleRate = Integer.parseInt(spn_rec_rate.getSelectedItem().toString());
        para.encoding = AudioFormat.ENCODING_PCM_16BIT;
        para.dataWaveDrawer = new DataWaveDrawer(mRecordView, mRecordView.getWidth());

        mAudioRecordTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, para);
    }

    private void stopAudioRecordTask() {
        mAudioRecordTask.stop();
    }

    private void switchBetweenSampleAndRecord() {
        mIsSample = !mIsSample;
        btn_play_sample.setText(mIsSample ? "样音" : "录音");
    }

    private void switchBetweenUsageAndStream() {
        mIsUsage = !mIsUsage;
        btn_play_usage.setText(mIsUsage ? "usage" : "stream");
    }

    private void switchCyclingSate() {
        mIsCycling = !mIsCycling;
        btn_play_cycle.setText(mIsCycling ? "循环播放" : "顺序播放");
        AudioTrackTask.setCyclingOn(mIsCycling);
    }

    private final AudioTrackTask.ITackUiCallback mTrackUiCallback = new AudioTrackTask.ITackUiCallback() {
        @Override
        public void onPreExecute() {
            Log.i(TAG, "PlayTask.onPreExecute()");
            btn_play_dev.setEnabled(false);
            btn_play_start.setEnabled(true);
            btn_play_start.setText("播放关");
            btn_play_sample.setEnabled(false);
            btn_play_usage.setEnabled(false);
            btn_play_dev.setEnabled(false);
        }

        @Override
        public void onPostExecute(Long aLong) {
            Log.i(TAG, "PlayTask.onPostExecute()");
            btn_play_dev.setEnabled(true);
            btn_play_start.setEnabled(true);
            btn_play_start.setText("播放开");
            btn_play_sample.setEnabled(true);
            btn_play_usage.setEnabled(true);
            btn_play_dev.setEnabled(true);
        }

        @Override
        public void onError(int code) {
            Log.e(TAG, "PlayTask.onError " + code);
        }

        @Override
        public void onCreate(final AudioTrack track) {
            runOnUiThread(() -> Toast.makeText(mContext, "AudioTrack "
                    + "session ID: " + track.getAudioSessionId() + ", "
                    + "audio format: " + track.getAudioFormat() + ", "
                    + "channels: " + track.getChannelCount() + ", "
                    + "sample rate: " + track.getSampleRate(), Toast.LENGTH_LONG).show());
        }
    };

    private void switchAudioTrackTask() {
        btn_play_start.setText("播放" + (mAudioTrackTask == null ? "关" : "开"));
        if (mAudioTrackTask == null) {
            startAudioTrackTask();
        } else {
            mAudioTrackTask.stop();
            setAudioTrackPreferredDevice(null);
            mAudioTrackTask = null;
        }
    }

    private void startAudioTrackTask() {
        mAudioTrackTask = new AudioTrackTask();
        mAudioTrackTask.registerCallback(mTrackUiCallback);
        AudioTrackTask.Parameters para = new AudioTrackTask.Parameters();

        para.isSample = mIsSample;
        para.isUsage = mIsUsage;
        if (para.isSample) {
            para.sampleRate = Integer.parseInt(spn_play_rate.getSelectedItem().toString());
            para.fileName = Constants.getPlayFileName(para.sampleRate);
        } else {
            para.sampleRate = Variables.getInstance().getRecordSampleRate();
            para.fileName = Variables.getInstance().getRecordName();
            if (para.fileName == null) {
                return;
            }
        }
        if (para.isUsage) {
            para.usage = Constants.getUsageInt(spn_play_usage.getSelectedItem().toString());
            para.content = Constants.getContentInt(spn_play_content.getSelectedItem().toString());
        } else {
            para.streamType = Constants.getStreamInt(spn_play_stream.getSelectedItem().toString());
            setVolumeControlStream(para.streamType);
        }
        para.channels = Constants.getChannelOutInt(spn_play_channel.getSelectedItem().toString());

        mAudioTrackTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, para);
    }

    private void setAudioTrackPreferredDevice(String dev) {
        btn_play_dev.setText("偏好:" + dev);
        AudioTrackTask.setPreferredDevice(dev == null ? null :
                AudioDeviceManager.getInstance().getSelectedAudioDeviceInfoBySpinner(dev));
    }

    private void setAudioRecordPreferredDevice(String dev) {
        btn_rec_dev.setText("偏好:" + dev);
        AudioRecordTask.setPreferredDevice(dev == null ? null :
                AudioDeviceManager.getInstance().getSelectedAudioDeviceInfoBySpinner(dev));
    }

    private void switchAudioRecordTask() {
        btn_rec_start.setText("录音" + (mAudioRecordTask == null ? "关" : "开"));
        if (mAudioRecordTask == null) {
            startAudioRecordTask();
        } else {
            stopAudioRecordTask();
            setAudioRecordPreferredDevice(null);
            mAudioRecordTask = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_rec_capture:
                setRecordCaptureOn();
                break;

            case R.id.btn_rec_start:
                switchAudioRecordTask();
                break;

            case R.id.btn_rec_dev:
                setAudioRecordPreferredDevice(spn_device_available.getSelectedItem().toString());
                break;

            case R.id.btn_play_sample:
                switchBetweenSampleAndRecord();
                break;

            case R.id.btn_play_usage:
                switchBetweenUsageAndStream();
                break;

            case R.id.btn_play_cycle:
                switchCyclingSate();
                break;


            case R.id.btn_play_start:
                switchAudioTrackTask();
                break;

            case R.id.btn_play_dev:
                setAudioTrackPreferredDevice(spn_device_available.getSelectedItem().toString());
                break;

            case R.id.btn_set_mode:
                AudioDeviceManager.getInstance().setMode(spn_audio_mode);
                break;

            case R.id.btn_speaker:
                AudioDeviceManager.getInstance().setSpeakerphoneOn(btn_speaker);
                break;

            case R.id.btn_sco:
                AudioDeviceManager.getInstance().setBluetoothScoOn(btn_sco);
                break;

            case R.id.btn_update_dev:
                AudioDeviceManager.getInstance().updateAvailableDevices(mDeviceAdapter);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}