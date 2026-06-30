package android.widget;

/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) Contributors to the Suwayomi project
 * Copyright (C) 2026 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

public class EditText extends TextView {
    private android.text.Editable mEditable;
    private int mSelectionStart = 0;
    private int mSelectionEnd = 0;

    public EditText(android.content.Context context) {
        super(context);
    }

    public EditText(android.content.Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
    }

    public EditText(android.content.Context context, android.util.AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public EditText(android.content.Context context, android.util.AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public boolean getFreezesText() { return true; }

    protected boolean getDefaultEditable() { return true; }

    protected android.text.method.MovementMethod getDefaultMovementMethod() { return null; }

    public android.text.Editable getText() { return mEditable; }

    public void setText(java.lang.CharSequence text, android.widget.TextView.BufferType type) {
        super.setText(text, type);
        if (text instanceof android.text.Editable) {
            mEditable = (android.text.Editable) text;
        }
    }

    public void setSelection(int start, int stop) { mSelectionStart = start; mSelectionEnd = stop; }

    public void setSelection(int index) { mSelectionStart = index; mSelectionEnd = index; }

    public void selectAll() { mSelectionStart = 0; mSelectionEnd = getText() != null ? getText().length() : 0; }

    public void extendSelection(int index) { mSelectionEnd = index; }

    public void setEllipsize(android.text.TextUtils.TruncateAt ellipsis) { super.setEllipsize(ellipsis); }

    public java.lang.CharSequence getAccessibilityClassName() { return "EditText"; }
}
