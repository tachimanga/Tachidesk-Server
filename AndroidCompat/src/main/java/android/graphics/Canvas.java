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

    public void drawBitmap(Bitmap bitmap, float left, float top, Paint paint) {
        Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        // left, top, right, bottom
        Rect dst = new Rect((int)left, (int)top, (int)left + bitmap.getWidth(), (int)top + bitmap.getHeight());
        this.bitmap.drawBitmap(bitmap, src, dst, paint);
    }

    public void drawPoint(float x, float y, Paint paint) {
        this.bitmap.drawPoint((int) x, (int) y, paint.getColor());
    }
}
