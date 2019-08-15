package com.pivoto.facedetection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Face;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

@SuppressWarnings("deprecation")
public class FaceDebugView extends View {

    public static final String TAG = "Face:DebugView";

    private Paint mFacePaint;

    private TextPaint mTextPaint;

    private RectF mFaceRect;

    @Nullable
    private Face[] mCameraFaces;

    private boolean mIsDrawing;

    private int mMinLeft, mMinTop, mMaxRight, mMaxBottom;

    private Matrix mFaceMatrix;

    private int mCameraFacing;

    private int mOrientation;

    public FaceDebugView(final Context context) {
        this(context, null, 0);
    }

    public FaceDebugView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FaceDebugView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mFacePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFacePaint.setColor(Color.argb(100, 0, 255, 0));

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.RED);
        mTextPaint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.text_size));

        mFaceMatrix = new Matrix();
        mFaceRect = new RectF();
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        recalculateMatrix();
    }

    @Override
    protected void onDraw(final Canvas canvas) {

        super.onDraw(canvas);

        if (mCameraFaces != null && mCameraFaces.length > 0) {
            for (Face face : mCameraFaces) {

                if (face.rect.left < mMinLeft) {
                    mMinLeft = face.rect.left;
                }
                if (face.rect.top < mMinTop) {
                    mMinTop = face.rect.top;
                }
                if (face.rect.right > mMaxRight) {
                    mMaxRight = face.rect.right;
                }
                if (face.rect.bottom > mMaxBottom) {
                    mMaxBottom = face.rect.bottom;
                }

                mFaceRect.set(face.rect);
                mFaceMatrix.mapRect(mFaceRect);
                canvas.drawRect(mFaceRect, mFacePaint);

            }
        }

        canvas.drawText("Min Left Received: " + mMinLeft, 20, 100, mTextPaint);
        canvas.drawText("Max Right Received: " + mMaxRight, 20, 200, mTextPaint);
        canvas.drawText("Min Top Received: " + mMinTop, 20, 300, mTextPaint);
        canvas.drawText("Max Bottom Received: " + mMaxBottom, 20, 400, mTextPaint);
        canvas.drawText(
                "Range Received: H: [" + mMinLeft + ", " + mMaxRight + "] V: [" + mMinTop + ", "
                        + mMaxBottom + "]", 20, 500, mTextPaint);

        mIsDrawing = false;
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
        recalculateMatrix();
    }

    public void setCameraFacing(int cameraFacing) {
        mCameraFacing = cameraFacing;
        recalculateMatrix();
    }

    private void recalculateMatrix() {

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        Log.e(TAG, "DebugView dimension w: " + width + " h: " + height);

        mFaceMatrix.reset();

        if (width > 0 && height > 0) {
            if (mCameraFacing == CameraInfo.CAMERA_FACING_FRONT) {
                mFaceMatrix.setScale(-1, 1);
            } else {
                mFaceMatrix.setScale(1, 1);
            }

            mFaceMatrix.postRotate(mOrientation);
            mFaceMatrix.postScale(width / 2000f, height / 2000f);
            mFaceMatrix.postTranslate(width / 2f, height / 2f);
        }
    }

    public void setFacePosition(@Nullable Face[] faces) {
        if (faces != null) {
            if (!mIsDrawing) {
                mIsDrawing = true;
                mCameraFaces = faces;
                invalidate();
            }
        } else {
            mCameraFaces = null;
            invalidate();
        }
    }
}
