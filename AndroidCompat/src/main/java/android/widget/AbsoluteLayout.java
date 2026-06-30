//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package android.widget;

/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2026 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RemoteViews.RemoteView;

/** @deprecated */
@Deprecated
@RemoteView
public class AbsoluteLayout extends ViewGroup {
    /** @deprecated */
    @Deprecated
    public AbsoluteLayout(Context context) {
        super(context);
    }

    /** @deprecated */
    @Deprecated
    public AbsoluteLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /** @deprecated */
    @Deprecated
    public AbsoluteLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /** @deprecated */
    @Deprecated
    public AbsoluteLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /** @deprecated */
    @Deprecated
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    }

    /** @deprecated */
    @Deprecated
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(getContext(), null);
    }

    /** @deprecated */
    @Deprecated
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    }

    /** @deprecated */
    @Deprecated
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    /** @deprecated */
    @Deprecated
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    /** @deprecated */
    @Deprecated
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    /** @deprecated */
    @Deprecated
    public boolean shouldDelayChildPressedState() {
        return true;
    }

    /** @deprecated */
    @Deprecated
    public static class LayoutParams extends ViewGroup.LayoutParams {
        /** @deprecated */
        @Deprecated
        public int x;
        /** @deprecated */
        @Deprecated
        public int y;

        /** @deprecated */
        @Deprecated
        public LayoutParams(int width, int height, int x, int y) {
            super(width, height);
            this.x = x;
            this.y = y;
        }

        /** @deprecated */
        @Deprecated
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        /** @deprecated */
        @Deprecated
        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        /** @deprecated */
        @Deprecated
        public String debug(String output) {
            return output;
        }
    }
}
