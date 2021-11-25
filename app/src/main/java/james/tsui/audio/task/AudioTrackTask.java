package james.tsui.audio.task;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import james.tsui.audio.utils.ContextUtils;

public class AudioTrackTask extends AsyncTask<AudioTrackTask.Parameters, Integer, Long> {
    private static final String TAG = AudioTrackTask.class.getSimpleName();
    private ITackUiCallback mCallback;
    private volatile boolean mRunning = false;
    private static boolean mCycling = false;

    public static final class Parameters {
        public boolean isSample;
        public boolean isUsage;

        public int usage;
        public int content;

        public int streamType;

        public int sampleRate;
        public int channels = AudioFormat.CHANNEL_OUT_STEREO;
        public int encoding = AudioFormat.ENCODING_PCM_16BIT;
        public int mode = AudioTrack.MODE_STREAM;
        public int capturePolicy = AudioAttributes.ALLOW_CAPTURE_BY_ALL;

        public String fileName;
    }

    public interface ITackUiCallback extends IUiCallback {
        void onCreate(final AudioTrack track);
    }

    public void registerCallback(ITackUiCallback callback) {
        mCallback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.i(TAG, "PlayTask.onPreExecute()");

        if (mCallback != null) {
            mCallback.onPreExecute();
        }
    }

    @Override
    protected void onPostExecute(Long aLong) {
        super.onPostExecute(aLong);
        Log.i(TAG, "PlayTask.onPostExecute()");

        if (mCallback != null) {
            mCallback.onPostExecute(aLong);
        }
    }

    @Override
    protected Long doInBackground(AudioTrackTask.Parameters... params) {
        Log.i(TAG, "PlayTask.doInBackground()");
        AudioTrackTask.Parameters para = params[0];

        try {
            int bufferSize = getBufferSize(para.sampleRate, para.channels, para.encoding);
            Log.i(TAG, "bufferSize = " + bufferSize);
            AudioTrack audioTrack = para.isUsage ?
                    buildAudioTrackByUsage(
                            buildAudioAttributes(para.capturePolicy, para.usage, para.content),
                            buildAudioFormat(para.sampleRate, para.channels, para.encoding),
                            bufferSize,
                            para.mode) :
                    buildAudioTrackByStreamType(para.streamType,
                            para.sampleRate, para.channels, para.encoding,
                            bufferSize, para.mode);
            if (mCallback != null) {
                mCallback.onCreate(audioTrack);
            }

            InputStream inputStream = getPlayFile(para.isSample, para.fileName);
            audioTrack.play();
            int bytesPer10ms = 2 * para.channels * (para.sampleRate * 10 / 1000);
            startFilePlay(inputStream, audioTrack, bytesPer10ms);

            audioTrack.stop();
            audioTrack.release();
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
            if (mCallback != null) {
                mCallback.onError(e.hashCode());
            }
        }

        return null;
    }

    private AudioTrack buildAudioTrackByUsage(AudioAttributes attributes, AudioFormat audioFormat, int buffSize, int mode) {
        return new AudioTrack.Builder()
                .setAudioAttributes(attributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(buffSize)
                .setTransferMode(mode)
                .build();
    }

    private AudioAttributes buildAudioAttributes(int capturePolicy, int usage, int content) {
        return new AudioAttributes.Builder()
                .setAllowedCapturePolicy(capturePolicy)
                .setUsage(usage)
                .setContentType(content)
                .build();
    }

    private AudioTrack buildAudioTrackByStreamType(int streamType, int sampleRate, int channels, int encoding, int bufferSize, int mode) {
        return new AudioTrack(streamType, sampleRate, channels, encoding, bufferSize, mode);
    }

    private int getBufferSize(int sampleRate, int channels, int encoding) {
        return AudioTrack.getMinBufferSize(sampleRate, channels, encoding) * 2;
    }

    private AudioFormat buildAudioFormat(int sampleRate, int channels, int encoding) {
        return new AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setChannelMask(channels)
                .setEncoding(encoding)
                .build();
    }


    private InputStream getPlayFile(boolean isSample, String fileName) throws IOException {
        if (isSample) {
            return ContextUtils.getContext().getResources().getAssets().open(fileName);
        } else {
            return new FileInputStream(ContextUtils.getContext().getExternalCacheDir() + fileName);
        }
    }

    private void startFilePlay(InputStream inputStream, @NonNull AudioTrack audioTrack, int bytesPerRead) throws IOException {
        if (inputStream == null || (inputStream.available() <= 0)) {
            return;
        }

        int bytesRead;
        byte[] tempBuf = new byte[bytesPerRead];

        mRunning = true;
        while (mRunning) {
            bytesRead = inputStream.read(tempBuf, 0, bytesPerRead);
            if (bytesRead != bytesPerRead) {
                if (mCycling) {
                    inputStream.reset();
                } else {
                    break;
                }
            }

            audioTrack.write(tempBuf, 0, bytesPerRead);
        }
    }

    public void stop() {
        mRunning = false;
    }

    public static void setCyclingOn(boolean cycling) {
        mCycling = cycling;
    }
}
