package android.graphics;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public final class Bitmap {
    private int width;
    private int height;

    //private long start;

    private NativeRef nativeImageRef;
    private NativeRef nativeCanvasRef;

    public static Bitmap createBitmap(byte[] bytes) {
        Bitmap bitmap = new Bitmap();
        //long s = System.currentTimeMillis();
        System.out.println("nativeImg createBitmap bytes.len:" + bytes.length
                + ", bitmap:" + bitmap);

        long[] r = bitmap.createNativeImage(bytes);

        bitmap.nativeImageRef = new NativeRef(r[0]);
        bitmap.width = (int) r[1];
        bitmap.height = (int) r[2];

        //System.out.println("nativeImg createBitmap cost:" + (System.currentTimeMillis() - s) + "ms");
        return bitmap;
    }

    public Bitmap copy(Config config, boolean isMutable) {
        throw new RuntimeException("Tachimanga does not support this extension.");
    }

    public boolean isMutable() {
        return nativeCanvasRef != null;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public Config getConfig() {
        return Config.ARGB_8888;
    }

    public enum CompressFormat {
        JPEG          (0),
        PNG           (1),
        WEBP          (2),
        WEBP_LOSSY    (3),
        WEBP_LOSSLESS (4);

        CompressFormat(int nativeInt) {
            this.nativeInt = nativeInt;
        }

        final int nativeInt;
    }

    public enum Config {
        ALPHA_8(1),
        RGB_565(3),
        ARGB_4444(4),
        ARGB_8888(5),
        RGBA_F16(6),
        HARDWARE(7),
        RGBA_1010102(8);

        final int nativeInt;

        private static Config sConfigs[] = {
            null, ALPHA_8, null, RGB_565, ARGB_4444, ARGB_8888, RGBA_F16, HARDWARE, RGBA_1010102
        };

        Config(int ni) {
            this.nativeInt = ni;
        }

        static Config nativeToConfig(int ni) {
            return sConfigs[ni];
        }
    }

    public static Bitmap createBitmap(Bitmap source, int x, int y, int width, int height) {
        if (source == null) {
            throw new IllegalArgumentException("Source bitmap is null");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be > 0");
        }
        if (x < 0 || y < 0 || x + width > source.width || y + height > source.height) {
            throw new IllegalArgumentException("Crop region is out of bounds: x=" + x + ", y=" + y
                    + ", width=" + width + ", height=" + height + ", sourceWidth=" + source.width
                    + ", sourceHeight=" + source.height);
        }
        if (source.nativeImageRef == null && source.nativeCanvasRef == null) {
            throw new IllegalArgumentException("Source bitmap has no native image data");
        }

        Bitmap bitmap = new Bitmap();
        long imageRef = bitmap.createCroppedImage(
                source.nativeImageRef != null ? source.nativeImageRef.address() : 0,
                source.nativeCanvasRef != null ? source.nativeCanvasRef.address() : 0,
                x, y, width, height);
        bitmap.nativeImageRef = new NativeRef(imageRef);
        bitmap.width = width;
        bitmap.height = height;
        return bitmap;
    }

    public static Bitmap createBitmap(int width, int height, Config config) {
        Bitmap bitmap = new Bitmap();
        //bitmap.start = System.currentTimeMillis();
        System.out.println("nativeImg createBitmap canvas width:" + width + ", height:" + height
                + ", bitmap:" + bitmap);

        long r = bitmap.createNativeCanvas(width, height);
        bitmap.nativeCanvasRef = new NativeRef(r);
        bitmap.width = width;
        bitmap.height = height;

        return bitmap;
    }

    public void drawBitmap(Bitmap sourceBitmap, Rect src, Rect dst, Paint paint) {
        //System.out.println("nativeImg drawBitmap src:" + src.toShortString() + ", dst:" + dst.toShortString());
        if (sourceBitmap.nativeImageRef == null) {
            System.out.println("nativeImg sourceBitmap.nativeImageRef is null");
            return;
        }
        if (this.nativeCanvasRef == null) {
            System.out.println("nativeImg this.nativeCanvasRef is null");
            return;
        }
        drawBitmap(sourceBitmap.nativeImageRef.address(),
                this.nativeCanvasRef.address(),
                new int[]{src.left, src.top, src.right, src.bottom},
                new int[]{dst.left, dst.top, dst.right, dst.bottom}
        );
    }

    public void drawPoint(int x, int y, int color) {
        if (nativeCanvasRef == null) {
            throw new RuntimeException("nativeCanvasRef is null");
        }
        drawPointNative(nativeCanvasRef.address(), x, y, color);
    }

    public int getPixel(int x, int y) {
        if (nativeImageRef == null) {
            throw new RuntimeException("nativeImageRef is null");
        }
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IllegalArgumentException("x=" + x + ", y=" + y + ", width=" + width + ", height=" + height);
        }
        return getPixel(nativeImageRef.address(), x, y);
    }

    public void getPixels(int[] pixels, int offset, int stride,
                          int x, int y, int width, int height) {
        if (nativeImageRef == null) {
            throw new RuntimeException("nativeImageRef is null");
        }
        if (x < 0 || y < 0 || x + width > this.width || y + height > this.height) {
            throw new IllegalArgumentException("x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + ", bitmapWidth=" + this.width + ", bitmapHeight=" + this.height);
        }
        getPixels(nativeImageRef.address(), pixels, offset, stride, x, y, width, height);
    }

    public boolean compress(CompressFormat format, int quality, OutputStream stream) {
        System.out.println("nativeImg compress format:" + format + ", quality:" + quality);

        if (stream == null) {
            throw new NullPointerException("stream is null");
        }

        if (quality < 0 || quality > 100) {
            throw new IllegalArgumentException("quality must be 0..100");
        }

        if (quality == 0 || quality > 90) {
            quality = 90;
        }

        if (nativeCanvasRef != null) {
            byte[] buffer = getImage(nativeCanvasRef.address(), format.nativeInt, quality);
            writeByteBufferToOutputStream(buffer, stream);
            //System.out.println("nativeImg drawBitmap cost:" + (System.currentTimeMillis() - this.start) + "ms");
            return true;
        }

        if (nativeImageRef != null) {
            byte[] buffer = compressImage(nativeImageRef.address(), format.nativeInt, quality);
            writeByteBufferToOutputStream(buffer, stream);
            //System.out.println("nativeImg compressImage cost:" + (System.currentTimeMillis() - this.start) + "ms");
            return true;
        }

        return true;
    }

    public void recycle() {
        // do nothing
    }

    private void writeByteBufferToOutputStream(byte[] buff, OutputStream stream) {
        try {
            stream.write(buff);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void releaseNativeObject() {
        System.out.println("nativeImg releaseNativeObject bitmap:" + this);
        if (nativeImageRef != null) {
            long addr = nativeImageRef.address();
            nativeImageRef.clear();
            if (addr != 0) {
                releaseNativeImage(addr);
            }
        }
        if (nativeCanvasRef != null) {
            long addr = nativeCanvasRef.address();
            nativeCanvasRef.clear();
            if (addr != 0) {
                releaseNativeCanvas(addr);
            }
        }
    }

    protected void finalize() {
        //System.out.println("nativeImg finalize");
        releaseNativeObject();
    }

    private native long[] createNativeImage(byte[] bytes);

    private native long createNativeCanvas(int width, int height);

    private native void drawBitmap(long imageRef, long canvasRef, int[] src, int []dst);

    private native void drawPointNative(long canvasRef, int x, int y, int color);

    // format  CompressFormat.nativeInt
    // quality 0 100
    private native byte[] getImage(long canvasRef, int format, int quality);

    private native byte[] compressImage(long imageRef, int format, int quality);

    private native int getPixel(long imageRef, int x, int y);

    private native void getPixels(long imageRef, int[] pixels, int offset, int stride,
                                  int x, int y, int width, int height);

    private native long createCroppedImage(long sourceImageRef, long sourceCanvasRef,
                                           int x, int y, int width, int height);

    private native void releaseNativeImage(long imageRef);
    private native void releaseNativeCanvas(long canvasRef);
}
