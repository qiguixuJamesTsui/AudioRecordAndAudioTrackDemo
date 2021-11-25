package james.tsui.audio;

import android.Manifest;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.ArrayRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import james.tsui.audio.task.AudioRecordTask;
import james.tsui.audio.task.AudioTrackTask;
import james.tsui.audio.utils.Constants;
import james.tsui.audio.utils.ContextUtils;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks ,
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
    private Button btn_rec_stop = null;
    private Button btn_play_sample = null;
    private Button btn_play_usage = null;
    private Button btn_play_cycle = null;
    private Button btn_play_start = null;
    private Button btn_play_stop = null;
    private Button btn_set_mode = null;
    private Button btn_speaker = null;

    private Spinner spn_rec_source = null;
    private Spinner spn_rec_rate = null;
    private Spinner spn_rec_channel = null;
    private Spinner spn_play_stream = null;
    private Spinner spn_play_rate = null;
    private Spinner spn_play_channel = null;
    private Spinner spn_play_usage = null;
    private Spinner spn_play_content = null;
    private Spinner spn_audio_mode = null;

    private TextView tv_audio_state = null;
    private TextView tv_device_info = null;

    private Context mContext = null;

    private AudioManager mAudioManager = null;
    private AudioRecordTask mAudioRecordTask;
    private AudioTrackTask mAudioTrackTask;

    private boolean mIsCapture = false;
    private boolean mIsSample = false;
    private boolean mIsUsage = false;
    private boolean mIsCycling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mContext = this.getApplicationContext();
        ContextUtils.init(this.getApplication());
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        tv_audio_state = (TextView) findViewById(R.id.tv_audio_state);
        tv_device_info = (TextView) findViewById(R.id.tv_device_info);
        String deviceInfo = "Android SDK: " + Build.VERSION.SDK_INT + ", "
                + "Release: " + Build.VERSION.RELEASE + ", "
                + "Brand: " + Build.BRAND + ", "
                + "Device: " + Build.DEVICE + ", "
                + "Id: " + Build.ID + ", "
                + "Hardware: " + Build.HARDWARE + ", "
                + "Manufacturer: " + Build.MANUFACTURER + ", "
                + "Model: " + Build.MODEL + ", "
                + "Product: " + Build.PRODUCT;
        tv_device_info.setText(deviceInfo);

        btn_rec_capture = getButtonWithClickListener(R.id.btn_rec_capture);
        btn_rec_start = getButtonWithClickListener(R.id.btn_rec_start);
        btn_rec_stop = getButtonWithClickListener(R.id.btn_rec_stop);
        btn_play_sample = getButtonWithClickListener(R.id.btn_play_sample);
        btn_play_usage = getButtonWithClickListener(R.id.btn_play_usage);
        btn_play_cycle = getButtonWithClickListener(R.id.btn_play_cycle);
        btn_play_start = getButtonWithClickListener(R.id.btn_play_start);
        btn_play_stop = getButtonWithClickListener(R.id.btn_play_stop);
        btn_set_mode = getButtonWithClickListener(R.id.btn_set_mode);
        btn_speaker = getButtonWithClickListener(R.id.btn_speaker);

        spn_rec_source = getSimpleSpinner(R.id.spn_rec_source, R.array.rec_source_entries);
        spn_rec_rate = getSimpleSpinner(R.id.spn_rec_rate, R.array.audio_sample_entries);
        spn_rec_channel = getSimpleSpinner(R.id.spn_rec_channel, R.array.rec_channel_entries);
        spn_play_stream = getSimpleSpinner(R.id.spn_play_stream, R.array.play_stream_type_entries);
        spn_play_rate = getSimpleSpinner(R.id.spn_play_rate, R.array.audio_sample_entries);
        spn_play_channel = getSimpleSpinner(R.id.spn_play_channel, R.array.play_channel_entries);
        spn_play_usage = getSimpleSpinner(R.id.spn_play_usage, R.array.play_usage_entries);
        spn_play_content = getSimpleSpinner(R.id.spn_play_content, R.array.play_content_entries);
        spn_audio_mode = getSimpleSpinner(R.id.spn_audio_mode, R.array.audio_mode_entries);

        EasyPermissions.requestPermissions(this, "please", REQUEST_PERMISSION, PERMISSIONS);
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
        Log.i(TAG, "onRequestPermissionsResult requestCode=" +requestCode
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
            btn_rec_start.setEnabled(false);
            btn_rec_stop.setEnabled(true);
        }

        @Override
        public void onPostExecute(Long aLong) {
            Log.i(TAG, "RecordTask.onPostExecute()");
            btn_rec_start.setEnabled(true);
            btn_rec_stop.setEnabled(true);
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
            btn_play_stop.setEnabled(true);
            btn_play_start.setEnabled(false);
            btn_play_sample.setEnabled(false);
            btn_play_usage.setEnabled(false);
        }

        @Override
        public void onPostExecute(Long aLong) {
            Log.i(TAG, "PlayTask.onPostExecute()");
            btn_play_stop.setEnabled(true);
            btn_play_start.setEnabled(true);
            btn_play_sample.setEnabled(true);
            btn_play_usage.setEnabled(true);
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
            para.sampleRate = ContextUtils.getRecordSampleRate();
            para.fileName = ContextUtils.getRecordName();
            if (para.fileName == null) {
                return;
            }
        }
        if (para.isUsage) {
            para.usage = Constants.getUsageInt(spn_play_usage.getSelectedItem().toString());
            para.content = Constants.getContentInt(spn_play_content.getSelectedItem().toString());
        } else {
            para.streamType = Constants.getStreamInt(spn_play_stream.getSelectedItem().toString());
        }
        para.channels = Constants.getChannelOutInt(spn_play_channel.getSelectedItem().toString());

        mAudioTrackTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, para);
    }

    private void setMode() {
        int defmode = mAudioManager.getMode();
        int mode = Constants.getModeInt(spn_audio_mode.getSelectedItem().toString());

        mAudioManager.setMode(mode);
        if (mAudioManager.getMode() != mode) {
            tv_audio_state.setText("模式设置失败, 可能不支持此模式.");
            mAudioManager.setMode(defmode);
        } else {
            tv_audio_state.setText("模式设置成功.");
        }
    }

    private void setSpeakerphoneOn() {
        btn_speaker.setText("spk" + (mAudioManager.isSpeakerphoneOn() ? "开" : "关"));
        mAudioManager.setSpeakerphoneOn(!mAudioManager.isSpeakerphoneOn());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_rec_capture:
                setRecordCaptureOn();
                break;

            case R.id.btn_rec_start:
                startAudioRecordTask();
                break;

            case R.id.btn_rec_stop:
                stopAudioRecordTask();
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
                startAudioTrackTask();
                break;

            case R.id.btn_play_stop:
                mAudioTrackTask.stop();
                break;

            case R.id.btn_set_mode:
                setMode();
                break;

            case R.id.btn_speaker:
                setSpeakerphoneOn();
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