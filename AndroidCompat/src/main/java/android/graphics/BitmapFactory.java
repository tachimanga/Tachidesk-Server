package android.graphics;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;

public class BitmapFactory {
    public static Bitmap decodeStream(InputStream inputStream) {
        //System.out.println("nativeImg decodeStream");
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[1024];

        try {
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        byte[] targetArray = buffer.toByteArray();
        return Bitmap.createBitmap(targetArray);
    }

    public static Bitmap decodeByteArray(byte[] data, int offset, int length) {
        //System.out.println("nativeImg decodeByteArray offset:" + offset + ", length:" + length);
        byte[] subData = offset == 0 && length == data.length ?
                data : Arrays.copyOfRange(data, offset, length);
        return Bitmap.createBitmap(subData);
    }
}
