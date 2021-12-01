package james.tsui.audio.task;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.widget.ImageView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DataWaveDrawer {
    private static final String TAG = DataWaveDrawer.class.getSimpleName();
    private static final float SHORT_MAX = 32767f; // 2^15 - 1
    private static final float REFERENCE = 0.5f;

    private final ImageView mView;
    private final Paint mPaint = new Paint();
    private final int mTotalWidth;

    private Canvas mCanvas;

    public DataWaveDrawer(ImageView view, int width) {
        mView = view;
        mTotalWidth = width;
        initDrawer();
    }

    private void initDrawer() {
        if (mView != null) {
            Bitmap bitmap = Bitmap.createBitmap(mView.getWidth(), mView.getHeight(), Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(bitmap);
            mView.setImageBitmap(bitmap);

            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            mView.post(mView::invalidate);
        }

        mPaint.setColor(Color.WHITE);
    }

    public void clearWave() {
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        mView.post(mView::invalidate);
    }

    public void draw(short[] shorData) {
        if (shorData == null) {
            return;
        }

        int height = mView.getHeight();
        int width = Math.min(mTotalWidth, shorData.length);
        float[] points = new float[4 * width];

        for (int i = 0; i < width; i++) {
            points[4 * i] = (float) i;
            points[4 * i + 1] = height * (REFERENCE + shorData[i] / SHORT_MAX);
            points[4 * i + 2] = (float) (i + 1);
            points[4 * i + 3] = height * (REFERENCE + shorData[i + 1] / SHORT_MAX);
        }

        mCanvas.drawColor(Color.BLACK);
        mCanvas.drawLines(points, mPaint);
        mView.post(mView::invalidate);
    }

    public void draw(byte[] byteData) {
        short[] shortData = bytes2shorts(byteData);
        draw(shortData);
    }

    private static short[] bytes2shorts(byte[] byteData) {
        short[] shortData = null;
        if (byteData != null) {
            shortData = new short[byteData.length / 2];
            ByteBuffer.wrap(byteData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortData);
        }
        return shortData;
    }
}
