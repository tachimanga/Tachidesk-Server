//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package android.os;

/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2023 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.util.Printer;

import java.util.concurrent.TimeUnit;

public class Handler {
    private Looper looper;
    private Callback mCallback;

    /** @deprecated */
    @Deprecated
    public Handler() {
        this.looper = Looper.myLooper();
    }

    /** @deprecated */
    @Deprecated
    public Handler(@Nullable Callback callback) {
        this.looper = Looper.myLooper();
        this.mCallback = callback;
    }

    public Handler(@NonNull Looper looper) {
        this.looper = looper;
    }

    public Handler(@NonNull Looper looper, @Nullable Callback callback) {
        this.looper = looper;
        this.mCallback = callback;
    }

    public void handleMessage(@NonNull Message msg) {
    }

    public void dispatchMessage(@NonNull Message msg) {
        if (msg.callback != null) {
            msg.callback.run();
        } else {
            if (mCallback != null) {
                if (mCallback.handleMessage(msg)) {
                    return;
                }
            }
            handleMessage(msg);
        }
    }

    @NonNull
    public static Handler createAsync(@NonNull Looper looper) {
        return new Handler(looper);
    }

    @NonNull
    public static Handler createAsync(@NonNull Looper looper, @NonNull Callback callback) {
        return new Handler(looper, callback);
    }

    @NonNull
    public String getMessageName(@NonNull Message message) {
        if (message.callback != null) {
            return message.callback.getClass().getName();
        }
        return "0x" + Integer.toHexString(message.what);
    }

    @NonNull
    public final Message obtainMessage() {
        return Message.obtain(this);
    }

    @NonNull
    public final Message obtainMessage(int what) {
        return Message.obtain(this, what);
    }

    @NonNull
    public final Message obtainMessage(int what, @Nullable Object obj) {
        return Message.obtain(this, what, obj);
    }

    @NonNull
    public final Message obtainMessage(int what, int arg1, int arg2) {
        return Message.obtain(this, what, arg1, arg2);
    }

    @NonNull
    public final Message obtainMessage(int what, int arg1, int arg2, @Nullable Object obj) {
        return Message.obtain(this, what, arg1, arg2, obj);
    }

    public final boolean post(@NonNull Runnable r) {
        this.looper.executor.execute(() -> {
            try {
                r.run();
            } catch (Exception e) {
                System.out.println("looper post run err:" + e);
                e.printStackTrace();
            }
        });
        return true;
    }

    public final boolean postAtTime(@NonNull Runnable r, long uptimeMillis) {
        return postDelayed(r, uptimeMillis - System.currentTimeMillis());
    }

    public final boolean postAtTime(@NonNull Runnable r, @Nullable Object token, long uptimeMillis) {
        return postDelayed(r, token, uptimeMillis - System.currentTimeMillis());
    }

    public final boolean postDelayed(@NonNull Runnable r, long delayMillis) {
        return postDelayed(r, null, delayMillis);
    }

    public final boolean postDelayed(@NonNull Runnable r, @Nullable Object token, long delayMillis) {
        this.looper.executor.schedule(() -> {
            try {
                r.run();
            } catch (Exception e) {
                System.out.println("looper postDelayed run err:" + e);
                e.printStackTrace();
            }
        }, delayMillis, TimeUnit.MILLISECONDS);
        return true;
    }

    public final boolean postAtFrontOfQueue(@NonNull Runnable r) {
        return post(r);
    }

    public final void removeCallbacks(@NonNull Runnable r) {
    }

    public final void removeCallbacks(@NonNull Runnable r, @Nullable Object token) {
    }

    public final boolean sendMessage(@NonNull Message msg) {
        return sendMessageDelayed(msg, 0);
    }

    public final boolean sendEmptyMessage(int what) {
        return sendEmptyMessageDelayed(what, 0);
    }

    public final boolean sendEmptyMessageDelayed(int what, long delayMillis) {
        Message msg = Message.obtain();
        msg.what = what;
        return sendMessageDelayed(msg, delayMillis);
    }

    public final boolean sendEmptyMessageAtTime(int what, long uptimeMillis) {
        Message msg = Message.obtain();
        msg.what = what;
        return sendMessageAtTime(msg, uptimeMillis);
    }

    public final boolean sendMessageDelayed(@NonNull Message msg, long delayMillis) {
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        msg.target = this;
        this.looper.executor.schedule(() -> {
            try {
                dispatchMessage(msg);
            } catch (Exception e) {
                System.out.println("looper sendMessage run err:" + e);
                e.printStackTrace();
            }
        }, delayMillis, TimeUnit.MILLISECONDS);
        return true;
    }

    public boolean sendMessageAtTime(@NonNull Message msg, long uptimeMillis) {
        return sendMessageDelayed(msg, uptimeMillis - SystemClock.uptimeMillis());
    }

    public final boolean sendMessageAtFrontOfQueue(@NonNull Message msg) {
        return sendMessage(msg);
    }

    public final void removeMessages(int what) {
    }

    public final void removeMessages(int what, @Nullable Object object) {
    }

    public final void removeCallbacksAndMessages(@Nullable Object token) {
    }

    public final boolean hasMessages(int what) {
        return false;
    }

    public final boolean hasMessages(int what, @Nullable Object object) {
        return false;
    }

    public final boolean hasCallbacks(@NonNull Runnable r) {
        return false;
    }

    @NonNull
    public final Looper getLooper() {
        return looper;
    }

    public final void dump(@NonNull Printer pw, @NonNull String prefix) {
        pw.println(prefix + "Handler{looper=" + looper + "}");
    }

    public String toString() {
        return "Handler{looper=" + looper + "}";
    }

    public interface Callback {
        boolean handleMessage(@NonNull Message var1);
    }
}
