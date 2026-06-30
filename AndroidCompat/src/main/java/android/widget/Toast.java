package android.widget;

/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) Contributors to the Suwayomi project
 * Copyright (C) 2025 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import org.json.JSONObject;
import org.tachiyomi.NativeChannel;

public class Toast {
    public static final int LENGTH_LONG = 1;
    public static final int LENGTH_SHORT = 0;

    private CharSequence text;
    private int duration = LENGTH_LONG;

    private Toast(CharSequence text) {
        this.text = text;
    }

    public Toast(CharSequence text, int duration) {
        this.text = text;
        this.duration = duration;
    }

    private android.view.View mView;
    private int mGravity = 0;
    private int mXOffset = 0;
    private int mYOffset = 0;
    private float mHorizontalMargin = 0f;
    private float mVerticalMargin = 0f;

    public Toast(android.content.Context context) {
    }

    public void show() {
        System.out.printf("made a Toast: \"%s\"\n", text.toString());
        JSONObject object = new JSONObject();
        object.put("text", text.toString());
        object.put("duration", duration);
        NativeChannel.call("TOAST:JSON", object.toString());
    }

    public void cancel() {
    }

    public void setView(android.view.View view) {
        mView = view;
    }

    public android.view.View getView() {
        return mView;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public void setMargin(float horizontalMargin, float verticalMargin) {
        mHorizontalMargin = horizontalMargin;
        mVerticalMargin = verticalMargin;
    }

    public float getHorizontalMargin() {
        return mHorizontalMargin;
    }

    public float getVerticalMargin() {
        return mVerticalMargin;
    }

    public void setGravity(int gravity, int xOffset, int yOffset) {
        mGravity = gravity;
        mXOffset = xOffset;
        mYOffset = yOffset;
    }

    public int getGravity() {
        return mGravity;
    }

    public int getXOffset() {
        return mXOffset;
    }

    public int getYOffset() {
        return mYOffset;
    }

    public static Toast makeText(android.content.Context context, java.lang.CharSequence text, int duration) {
        return new Toast(text, duration);
    }

    public static android.widget.Toast makeText(android.content.Context context, int resId, int duration) throws android.content.res.Resources.NotFoundException {
        return new Toast("Toast_" + resId, duration);
    }

    public void setText(int resId) {
        this.text = "Toast_" + resId;
    }

    public void setText(java.lang.CharSequence s) {
        this.text = s;
    }
}