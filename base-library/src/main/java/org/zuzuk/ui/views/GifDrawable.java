package org.zuzuk.ui.views;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import org.apache.http.util.ByteArrayBuffer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Drawable that draws GIF images
 */
public class GifDrawable extends Drawable implements Runnable {
    public enum GifCacheMode {
        CachePixels, CachePixelIndices, NoCache
    }

    private enum GifFrameAction {
        DoNothing, FillBackground
    }

    private static final String LOGGER_TAG = "GifDrawableDecoder";
    private static final String GIF_HEADER = "GIF";
    private static final String GIF_VERSION_87A = "87a";
    private static final String GIF_VERSION_89A = "89a";
    private static final int NO_TRANSPARENCY = -1;
    private static final int[] DEFAULT_PALETTE = new int[]{Color.BLACK, Color.WHITE};

    private final GifState mState;
    private int mCurrentFrameIndex = 0;
    private int mLastDrawedFrameIndex = -1;
    private Bitmap mTempBitmap;
    private WeakReference<Callback> mOldCallback;

    private Callback getInternalCallback() {
        if (Build.VERSION.SDK_INT >= 11)
            return getCallback();
        else
            try {
                Field f = Drawable.class.getDeclaredField("mCallback");
                f.setAccessible(true);
                return (Callback) f.get(this);
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
    }

    public GifDrawable(Resources res, int rawResourceId) {
        this(res, rawResourceId, GifCacheMode.CachePixelIndices);
    }

    public GifDrawable(Resources res, int rawResourceId, GifCacheMode cacheMode) {
        mState = new GifState(cacheMode);
        try {
            InputStream is = res.openRawResource(rawResourceId);
            decodeGif(is);
            is.close();
        } catch (Throwable ex) {
            Log.e(LOGGER_TAG, "GifDrawable cannot decode " + ex.getMessage());
            if (mTempBitmap != null) {
                mTempBitmap.recycle();
                mTempBitmap = null;
            }
        }
    }

    private GifDrawable(GifState state) {
        mState = state;
        clearOrCreateTempBitmap(true);
    }

    public GifDrawable(Resources res, InputStream is) {
        this(res, is, GifCacheMode.CachePixelIndices);
    }

    public GifDrawable(Resources res, InputStream is, GifCacheMode cacheMode) {
        mState = new GifState(cacheMode);
        try {
            decodeGif(is);
        } catch (Throwable ex) {
            Log.e(LOGGER_TAG, "GifDrawable cannot decode " + ex.getMessage());
            if (mTempBitmap != null) {
                mTempBitmap.recycle();
                mTempBitmap = null;
            }
        }
    }

    private void clearOrCreateTempBitmap(boolean recreate) {
        if (mTempBitmap == null || recreate) {
            if (mTempBitmap != null)
                mTempBitmap.recycle();
            mTempBitmap = Bitmap.createBitmap(mState.mWidth,
                    mState.mHeight,
                    mState.mHaveTransparency ? Bitmap.Config.ARGB_8888
                            : Bitmap.Config.RGB_565
            );
        }
        mTempBitmap.eraseColor(mState.mBackgroundColor);
    }

    private void checkGifImage(InputStream is) throws Throwable {
        byte[] headerBytes = new byte[3];
        is.read(headerBytes);
        String header = new String(headerBytes, "UTF-8");
        if (!header.equals(GIF_HEADER))
            throw new Throwable("Wrong GIF header: " + header);
    }

    private void checkGifVersion(InputStream is) throws Throwable {
        byte[] versionBytes = new byte[3];
        is.read(versionBytes);
        String version = new String(versionBytes, "UTF-8");

        if (!version.equals(GIF_VERSION_87A) && !version.equals(GIF_VERSION_89A))
            throw new Throwable("Wrong GIF version: " + version);
    }

    private int parseUnsignedShort(InputStream is) throws Throwable {
        return is.read() & 0xff | (is.read() & 0xff) << 8;
    }

    private int[] parsePalette(InputStream is, int colorsCount) throws Throwable {
        int[] palette = new int[colorsCount];
        for (int i = 0; i < colorsCount; i++)
            palette[i] = 0xff << 24 | (is.read() & 0xff) << 16 | (is.read() & 0xff) << 8 | is.read() & 0xff;
        return palette;
    }

    private int[] parseGlobalPalette(InputStream is) throws Throwable {
        int packedFields = is.read();
        int backgroundColorIndex = is.read() & 0xff;
        // useless field
        int pixelForm = is.read();

        int[] palette;
        if ((packedFields & 0x80) > 0) {
            int colorsCount = 2 << (packedFields & 0x07);
            palette = parsePalette(is, colorsCount);
        } else
            palette = DEFAULT_PALETTE;

        mState.mBackgroundColor = palette[backgroundColorIndex];

        return palette;
    }

    private void parseExtension(InputStream is) throws Throwable {
        // application/comment extension - skip it
        int blockSize = is.read() & 0xff;
        while (blockSize != 0x0) {
            is.skip(blockSize);
            blockSize = is.read() & 0xff;
        }
    }

    private GifFrameExtension parseFrameExtension(InputStream is) throws Throwable {
        // skip block size - we know it must be 4
        is.skip(1);

        int flags = is.read();
        GifFrameAction frameAction = (flags & 0x1c) >> 2 == 2 ? GifFrameAction.FillBackground
                : GifFrameAction.DoNothing;

        int delay = parseUnsignedShort(is) * 10;
        int transparentIndex = is.read() & 0xff;
        if ((flags & 0x1) != 1)
            transparentIndex = NO_TRANSPARENCY;
        is.skip(1);
        return new GifFrameExtension(frameAction, delay, transparentIndex);
    }

    private GifFrame parseFrame(InputStream is, GifFrameExtension frameExtension) throws Throwable {
        GifFrame frame = new GifFrame(is, mState.mCacheMode, frameExtension, mState.mGlobalPalette);
        if (mState.mCacheMode == GifCacheMode.CachePixels) {
            frame.applyFrame(mTempBitmap);
            if (frame.extension.frameAction == GifFrameAction.FillBackground)
                clearOrCreateTempBitmap(false);
        }

        if (frame.haveTransparency)
            mState.mHaveTransparency = true;

        return frame;
    }

    private void decodeGif(InputStream is) throws Throwable {
        // check header
        checkGifImage(is);
        checkGifVersion(is);

        // check logical screen
        mState.mWidth = parseUnsignedShort(is);
        mState.mHeight = parseUnsignedShort(is);
        mState.mGlobalPalette = parseGlobalPalette(is);

        clearOrCreateTempBitmap(true);
        mState.mHaveTransparency = false;

        ArrayList<GifFrame> frames = new ArrayList<>();
        GifFrameExtension frameExtension = new GifFrameExtension(GifFrameAction.DoNothing, 0, NO_TRANSPARENCY);
        boolean firstFrameExtensionChecked = false;
        int blockType;

        do {
            blockType = is.read();
            switch (blockType & 0xff) {
                case 0x21: {
                    int extensionType = is.read() & 0xff;
                    if (extensionType == 0xf9) {
                        frameExtension = parseFrameExtension(is);
                        if (!firstFrameExtensionChecked) {
                            firstFrameExtensionChecked = true;
                            if (frameExtension.transparentIndex != NO_TRANSPARENCY) {
                                mState.mBackgroundColor = 0;
                                clearOrCreateTempBitmap(false);
                            }
                        }
                    } else
                        parseExtension(is);
                    break;
                }
                case 0x2C: {
                    GifFrame frame = parseFrame(is, frameExtension);
                    frameExtension = new GifFrameExtension(GifFrameAction.DoNothing, 0, NO_TRANSPARENCY);
                    frames.add(frame);
                    break;
                }
            }
        } while (blockType != -1 && (blockType & 0xff) != 0x3b);

        mState.mFrames = new GifFrame[frames.size()];
        frames.toArray(mState.mFrames);
        clearOrCreateTempBitmap(true);
    }

    public void draw(Canvas canvas) {
        Callback currentCallback = getInternalCallback();
        if (currentCallback == null || mState.mFrames == null || mState.mFrames.length == 0 || mTempBitmap == null || mTempBitmap
                .isRecycled())
            return;

        if (mOldCallback == null || mOldCallback.get() != currentCallback) {
            mCurrentFrameIndex = 0;
            mLastDrawedFrameIndex = -1;
            unscheduleSelf(this);
        }

        if (mCurrentFrameIndex == mLastDrawedFrameIndex) {
            canvas.drawBitmap(mTempBitmap, null, getBounds(), mState.mPaint);
            return;
        }

        GifFrame currentFrame = mState.mFrames[mCurrentFrameIndex];

        synchronized (mTempBitmap) {
            if (mCurrentFrameIndex == 0 || mState.mFrames[mCurrentFrameIndex - 1].extension.frameAction == GifFrameAction.FillBackground)
                clearOrCreateTempBitmap(false);

            currentFrame.applyFrame(mTempBitmap);
            canvas.drawBitmap(mTempBitmap, null, getBounds(), mState.mPaint);
            mLastDrawedFrameIndex = mCurrentFrameIndex;
        }

        if (mOldCallback == null || mOldCallback.get() != currentCallback) {
            mOldCallback = new WeakReference<Callback>(currentCallback);
            run();
        }
    }

    @Override
    public int getIntrinsicHeight() {
        return mState.mHeight;
    }

    @Override
    public int getIntrinsicWidth() {
        return mState.mWidth;
    }

    @Override
    public int getMinimumHeight() {
        return mState.mHeight;
    }

    @Override
    public int getMinimumWidth() {
        return mState.mWidth;
    }

    @Override
    public ConstantState getConstantState() {
        return mState;
    }

    public Paint getPaint() {
        return mState.mPaint;
    }

    @Override
    public void setDither(boolean dither) {
        mState.mPaint.setDither(dither);
        invalidateSelf();
    }

    @Override
    public void setFilterBitmap(boolean filter) {
        mState.mPaint.setFilterBitmap(filter);
        invalidateSelf();
    }

    public int getOpacity() {
        return mState.mFrames.length == 0 || mState.mHaveTransparency || mTempBitmap.hasAlpha() || mState.mPaint
                .getAlpha() < 255 ? PixelFormat.OPAQUE : PixelFormat.TRANSLUCENT;
    }

    public void setAlpha(int alpha) {
        mState.mPaint.setAlpha(alpha);
        invalidateSelf();
    }

    public void setColorFilter(ColorFilter cf) {
        mState.mPaint.setColorFilter(cf);
        invalidateSelf();
    }

    @Override
    public void run() {
        if (mState.mFrames.length > 1) {
            GifFrame currentFrame = mState.mFrames[mCurrentFrameIndex];
            mCurrentFrameIndex = (mCurrentFrameIndex + 1) % mState.mFrames.length;
            scheduleSelf(this, SystemClock.uptimeMillis() + currentFrame.extension.delay);
        }

        if (mLastDrawedFrameIndex != -1)
            invalidateSelf();
    }

    public void recycle() {
        unscheduleSelf(this);
        if (mOldCallback != null)
            mOldCallback.clear();
        setCallback(null);

        if (mTempBitmap != null)
            synchronized (mTempBitmap) {
                mTempBitmap.recycle();
            }
    }

    final static class GifState extends ConstantState {
        int mWidth;
        int mHeight;
        int[] mGlobalPalette;
        GifFrame[] mFrames;
        int mBackgroundColor;
        Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        final GifCacheMode mCacheMode;
        boolean mHaveTransparency = true;

        GifState(GifCacheMode cacheMode) {
            mCacheMode = cacheMode;
        }

        @Override
        public Drawable newDrawable() {
            return new GifDrawable(this);
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new GifDrawable(this);
        }

        @Override
        public int getChangingConfigurations() {
            return 0;
        }
    }

    final class GifFrame {
        final int top;
        final int left;
        final int width;
        final int height;
        final boolean isInterlacedOrder;
        final GifFrameExtension extension;
        final int[] palette;
        final boolean haveTransparency;
        final GifCacheMode mCacheMode;
        byte[] mRawData = null;
        byte[] mPixelIndicies = null;
        int[] mPixels = null;

        GifFrame(InputStream is, GifCacheMode cacheMode, GifFrameExtension extension, int[] globalPalette) throws Throwable {
            mCacheMode = cacheMode;
            this.extension = extension;

            // get frame bounds
            left = parseUnsignedShort(is);
            top = parseUnsignedShort(is);
            width = parseUnsignedShort(is);
            height = parseUnsignedShort(is);

            // get frame flags
            int frameFlags = is.read();
            isInterlacedOrder = (frameFlags & 0x40) > 0;
            boolean isLocalPaletteUsing = (frameFlags & 0x80) > 0;
            haveTransparency = extension.transparentIndex != NO_TRANSPARENCY;

            // load frame palette
            int localPaletteColorsCount = 2 << (frameFlags & 0x07);
            palette = isLocalPaletteUsing ? parsePalette(is, localPaletteColorsCount) : globalPalette;

            if (mCacheMode == GifCacheMode.NoCache)
                mRawData = getRawData(is);
            else
                mPixelIndicies = decodeBitmapData(is, width, height);
        }

        void applyFrame(Bitmap bitmap) {
            int[] pixels = mPixels;
            if (mPixels == null) {
                byte[] pixelIndicies = mPixelIndicies;
                if (pixelIndicies == null)
                    try {
                        InputStream is = new ByteArrayInputStream(mRawData);
                        pixelIndicies = decodeBitmapData(is, width, height);
                        is.close();

                        if (mCacheMode != GifCacheMode.NoCache) {
                            mPixelIndicies = pixelIndicies;
                            mRawData = null;
                        }
                    } catch (Throwable ex) {
                        return;
                    }

                pixels = new int[width * height];
                bitmap.getPixels(pixels, 0, width, left, top, width, height);

                if (isInterlacedOrder) {
                    int rowIndex = 0;
                    for (int l = 0; l < height; l += 8, rowIndex++)
                        for (int j = 0; j < width; j++) {
                            int colorIndex = pixelIndicies[rowIndex * width + j] & 0xff;
                            if (!haveTransparency || colorIndex != extension.transparentIndex)
                                pixels[l * width + j] = palette[colorIndex];
                        }
                    for (int l = 4; l < height; l += 8, rowIndex++)
                        for (int j = 0; j < width; j++) {
                            int colorIndex = pixelIndicies[rowIndex * width + j] & 0xff;
                            if (!haveTransparency || colorIndex != extension.transparentIndex)
                                pixels[l * width + j] = palette[colorIndex];
                        }
                    for (int l = 0; l < height; l += 2) {
                        if (l % 8 == 0 || (l - 4) % 8 == 0)
                            continue;
                        for (int j = 0; j < width; j++) {
                            int colorIndex = pixelIndicies[rowIndex * width + j] & 0xff;
                            if (!haveTransparency || colorIndex != extension.transparentIndex)
                                pixels[l * width + j] = palette[colorIndex];
                        }
                        rowIndex++;
                    }
                    for (int l = 1; l < height; l += 2, rowIndex++)
                        for (int j = 0; j < width; j++) {
                            int colorIndex = pixelIndicies[rowIndex * width + j] & 0xff;
                            if (!haveTransparency || colorIndex != extension.transparentIndex)
                                pixels[l * width + j] = palette[colorIndex];
                        }
                } else
                    for (int l = 0; l < height; l++)
                        for (int j = 0; j < width; j++) {
                            int colorIndex = pixelIndicies[l * width + j] & 0xff;
                            if (!haveTransparency || colorIndex != extension.transparentIndex)
                                pixels[l * width + j] = palette[colorIndex];
                        }

                if (mCacheMode == GifCacheMode.CachePixels) {
                    mPixels = pixels;
                    mPixelIndicies = null;
                }
            }

            bitmap.setPixels(pixels, 0, width, left, top, width, height);
        }

        byte[] getRawData(InputStream is) throws Throwable {
            ByteArrayBuffer buffer = new ByteArrayBuffer(1024);
            buffer.append((byte) is.read());

            byte[] dataBlock = new byte[256];
            int availableBytes = is.read() & 0xff;
            while (availableBytes > 0) {
                buffer.append((byte) availableBytes);
                is.read(dataBlock, 0, availableBytes);
                buffer.append(dataBlock, 0, availableBytes);
                availableBytes = is.read() & 0xff;
            }
            return buffer.toByteArray();
        }

        byte[] decodeBitmapData(InputStream is, int width, int height) throws Throwable {
            final int maxStackSize = 4096;
            final int nullCode = -1;

            byte[] dataBlock = new byte[256];
            int pixelsLength = width * height;
            byte[] pixels = new byte[pixelsLength];
            int readedChunkBits, availableBytes, readedChunk, dataSize, first, readedPixelsStackSize, currentByte, currentPixel;
            short[] prefix = new short[maxStackSize];
            byte[] suffix = new byte[maxStackSize];
            byte[] readedPixelsStack = new byte[maxStackSize + 1];
            // Initialize GIF data stream decoder.
            dataSize = is.read();
            int tempCode, code;
            int prevCode = nullCode;
            int resetCode = 1 << dataSize;
            int endCode = resetCode + 1;
            int maxAvailableCode = resetCode + 2;
            int sizeOfCode = dataSize + 1;
            int codeMask = (1 << sizeOfCode) - 1;
            for (code = 0; code < resetCode; code++) {
                prefix[code] = 0;
                suffix[code] = (byte) code;
            }
            // Decode GIF pixel stream.
            readedChunk = readedChunkBits = availableBytes = first = readedPixelsStackSize = currentPixel = currentByte = 0;
            while (currentPixel < pixelsLength) {
                if (readedPixelsStackSize == 0) {
                    if (readedChunkBits < sizeOfCode) {
                        // Load bytes until there are enough bits for a code
                        if (availableBytes == 0) {
                            // Read a new data block
                            availableBytes = is.read() & 0xff;
                            if (availableBytes <= 0)
                                break;
                            is.read(dataBlock, 0, availableBytes);
                            currentByte = 0;
                        }
                        readedChunk += (dataBlock[currentByte] & 0xff) << readedChunkBits;
                        readedChunkBits += 8;
                        currentByte++;
                        availableBytes--;
                        continue;
                    }
                    // Get next code
                    code = readedChunk & codeMask;
                    readedChunk >>= sizeOfCode;
                    readedChunkBits -= sizeOfCode;
                    // Interpret the code
                    if (code > maxAvailableCode || code == endCode)
                        break;
                    // Reset decoder
                    if (code == resetCode) {
                        sizeOfCode = dataSize + 1;
                        codeMask = (1 << sizeOfCode) - 1;
                        maxAvailableCode = resetCode + 2;
                        prevCode = nullCode;
                        continue;
                    }
                    if (prevCode == nullCode) {
                        readedPixelsStack[readedPixelsStackSize++] = suffix[code];
                        prevCode = code;
                        first = code;
                        continue;
                    }
                    tempCode = code;
                    if (code == maxAvailableCode) {
                        readedPixelsStack[readedPixelsStackSize++] = (byte) first;
                        code = prevCode;
                    }
                    while (code > resetCode) {
                        readedPixelsStack[readedPixelsStackSize++] = suffix[code];
                        code = prefix[code];
                    }
                    first = suffix[code] & 0xff;
                    if (maxAvailableCode >= maxStackSize)
                        break;
                    readedPixelsStack[readedPixelsStackSize++] = (byte) first;
                    prefix[maxAvailableCode] = (short) prevCode;
                    suffix[maxAvailableCode] = (byte) first;
                    maxAvailableCode++;
                    if ((maxAvailableCode & codeMask) == 0 && maxAvailableCode < maxStackSize) {
                        sizeOfCode++;
                        codeMask += maxAvailableCode;
                    }
                    prevCode = tempCode;
                }
                // Pop a pixel off the pixel stack
                readedPixelsStackSize--;
                pixels[currentPixel++] = readedPixelsStack[readedPixelsStackSize];
            }
            // Fill missing pixels
            while (currentPixel < pixelsLength)
                pixels[currentPixel++] = 0;

            is.skip(1);

            return pixels;
        }
    }

    final class GifFrameExtension {
        // answer why 6/100sec:
        // http://humpy77.deviantart.com/journal/Frame-Delay-Times-for-Animated-GIFs-214150546
        final static int MinimumDelay = 60;
        final GifFrameAction frameAction;
        final int delay;
        final int transparentIndex;

        GifFrameExtension(GifFrameAction frameAction, int delay, int transparentIndex) {
            this.frameAction = frameAction;
            this.delay = Math.max(MinimumDelay, delay);
            this.transparentIndex = transparentIndex;
        }
    }
}
