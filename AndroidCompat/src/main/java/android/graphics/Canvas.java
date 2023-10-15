package android.graphics;


public final class Canvas {
    private Bitmap bitmap;

    public Canvas(Bitmap bitmap) {
        System.out.println("nativeImg Canvas bitmap:" + bitmap);
        this.bitmap = bitmap;
    }

    public void drawBitmap(Bitmap sourceBitmap, Rect src, Rect dst, Paint paint) {        
        this.bitmap.drawBitmap(sourceBitmap, src, dst, paint);
    }
}
