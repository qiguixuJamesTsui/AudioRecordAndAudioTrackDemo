package james.tsui.audio.task;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import james.tsui.audio.utils.ContextUtils;

public class AudioRecordTask extends AsyncTask<AudioRecordTask.Parameters, Integer, Long> {
    private static final String TAG = AudioRecordTask.class.getSimpleName();

    // @RequiresPermission(android.Manifest.permission.CAPTURE_AUDIO_OUTPUT)
    private static final int SUBMIX = MediaRecorder.AudioSource.REMOTE_SUBMIX;
    private static final int READ_PERIOD = 10; // milliSecond

    private IRecordUiCallback mCallback;
    private volatile boolean mRunning = false;

    public static final class Parameters {
        public int source;
        public int sampleRate;
        public int channels;
        public int encoding = AudioFormat.ENCODING_PCM_16BIT;

        public boolean isCapture = false;
        public String fileName = ContextUtils.getNowDay("yyyy-MM-dd HH:mm:ss") + "record.pcm";
    }

    public interface IRecordUiCallback extends IUiCallback{
        void onCreate(final AudioRecord record);
    }

    public void registerCallback(IRecordUiCallback callback) {
        mCallback = callback;
    }

    @Override
    protected Long doInBackground(Parameters... parameters) {
        Log.i(TAG, "doInBackground");
        Parameters param = parameters[0];

        try {
            int bufferSize = getBufferSize(param.sampleRate, param.channels, param.encoding);
            Log.i(TAG, "bufferSize = " + bufferSize);
            AudioRecord audioRecord = buildAudioRecord(param.isCapture ? SUBMIX : param.source,
                    param.sampleRate, param.channels, param.encoding, bufferSize);
            if (mCallback != null) {
                mCallback.onCreate(audioRecord);
            }

            audioRecord.startRecording();
            FileOutputStream recordFile = getRecordFile(param.fileName);
            ContextUtils.initRecord(param.fileName, param.sampleRate);
            int bytesPer10ms = getBytesReadInPerPeriod(param.sampleRate, param.channels);
            startRecord2File(recordFile, audioRecord, bytesPer10ms);

            audioRecord.stop();
            audioRecord.release();
            if (recordFile != null) {
                recordFile.close();
            }
        } catch (IllegalArgumentException | IOException e) {
            e.printStackTrace();

            if (mCallback != null) {
                mCallback.onError(e.hashCode());
            }
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.i(TAG, "onPreExecute()");

        if (mCallback != null) {
            mCallback.onPreExecute();
        }
    }

    @Override
    protected void onPostExecute(Long aLong) {
        super.onPostExecute(aLong);
        Log.i(TAG, "onPostExecute()");

        if (mCallback != null) {
            mCallback.onPostExecute(aLong);
        }
    }

    private AudioRecord buildAudioRecord(int source, int sampleRate, int channels, int encoding, int bufferSize) {
        return new AudioRecord.Builder()
                .setAudioSource(source)
                .setAudioFormat(buildAudioFormat(sampleRate, channels, encoding))
                .setBufferSizeInBytes(bufferSize)
                .build();
    }

    private int getBufferSize(int sampleRate, int channels, int encoding) {
        return AudioRecord.getMinBufferSize(sampleRate, channels, encoding) * 2;
    }

    private int getBytesReadInPerPeriod(int sampleRate, int channels) {
        return 2 * channels * (sampleRate * AudioRecordTask.READ_PERIOD / 1000);
    }

    private AudioFormat buildAudioFormat(int sampleRate, int channels, int encoding) {
        return new AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setChannelMask(channels)
                .setEncoding(encoding)
                .build();
    }

    private FileOutputStream getRecordFile(@NonNull String fileName) {
        try {
            return new FileOutputStream(ContextUtils.getContext().getExternalCacheDir() + fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void startRecord2File(FileOutputStream file, @NonNull AudioRecord record, int bytesPerRead) throws IOException {
        int bytesRead;
        byte[] tempBuf = new byte[bytesPerRead];

        mRunning = true;
        while (mRunning) {
            bytesRead = record.read(tempBuf, 0, bytesPerRead);

            if (file != null && bytesRead == bytesPerRead) {
                file.write(tempBuf, 0, bytesRead);
            }
        }
    }

    public void stop() {
        mRunning = false;
    }
}