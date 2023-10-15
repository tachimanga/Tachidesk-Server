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

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
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

    public void getPixels(int[] pixels, int offset, int stride,
                          int x, int y, int width, int height) {
        throw new RuntimeException("请在设置中禁用移除包子漫画横幅");
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
            ByteBuffer buffer = getImage(nativeCanvasRef.address(), format.nativeInt, quality);
            writeByteBufferToOutputStream(buffer, stream);
            //System.out.println("nativeImg drawBitmap cost:" + (System.currentTimeMillis() - this.start) + "ms");
            return true;
        }

        if (nativeImageRef != null) {
            ByteBuffer buffer = compressImage(nativeImageRef.address(), format.nativeInt, quality);
            writeByteBufferToOutputStream(buffer, stream);
            //System.out.println("nativeImg compressImage cost:" + (System.currentTimeMillis() - this.start) + "ms");
            return true;
        }

        return true;
    }

    private void writeByteBufferToOutputStream(ByteBuffer buffer, OutputStream stream) {
        try {
            byte[] buff = new byte[buffer.remaining()];
            buffer.get(buff);
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

    private native ByteBuffer getImage(long canvasRef, int format, int quality);

    private native ByteBuffer compressImage(long imageRef, int format, int quality);

    private native void releaseNativeImage(long imageRef);
    private native void releaseNativeCanvas(long canvasRef);
}
