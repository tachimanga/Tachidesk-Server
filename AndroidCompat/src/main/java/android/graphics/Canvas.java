package android.graphics;

/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) Contributors to the Suwayomi project
 * Copyright (C) 2023 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

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
