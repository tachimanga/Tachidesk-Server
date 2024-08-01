//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package android.os;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.util.Printer;

public class Handler {
    private Looper looper;

    /** @deprecated */
    @Deprecated
    public Handler() {
        throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    public Handler(@Nullable Callback callback) {
        throw new RuntimeException("Stub!");
    }

    public Handler(@NonNull Looper looper) {
        this.looper = looper;
    }

    public Handler(@NonNull Looper looper, @Nullable Callback callback) {
        this.looper = looper;
    }

    public void handleMessage(@NonNull Message msg) {
        throw new RuntimeException("Stub!");
    }

    public void dispatchMessage(@NonNull Message msg) {
        throw new RuntimeException("Stub!");
    }

    @NonNull
    public static Handler createAsync(@NonNull Looper looper) {
        throw new RuntimeException("Stub!");
    }

    @NonNull
    public static Handler createAsync(@NonNull Looper looper, @NonNull Callback callback) {
        throw new RuntimeException("Stub!");
    }

    @NonNull
    public String getMessageName(@NonNull Message message) {
        throw new RuntimeException("Stub!");
    }

    @NonNull
    public final Message obtainMessage() {
        throw new RuntimeException("Stub!");
    }

    @NonNull
    public final Message obtainMessage(int what) {
        throw new RuntimeException("Stub!");
    }

    @NonNull
    public final Message obtainMessage(int what, @Nullable Object obj) {
        throw new RuntimeException("Stub!");
    }

    @NonNull
    public final Message obtainMessage(int what, int arg1, int arg2) {
        throw new RuntimeException("Stub!");
    }

    @NonNull
    public final Message obtainMessage(int what, int arg1, int arg2, @Nullable Object obj) {
        throw new RuntimeException("Stub!");
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
        throw new RuntimeException("Stub!");
    }

    public final boolean postAtTime(@NonNull Runnable r, @Nullable Object token, long uptimeMillis) {
        throw new RuntimeException("Stub!");
    }

    public final boolean postDelayed(@NonNull Runnable r, long delayMillis) {
        return postDelayed(r, null, delayMillis);
    }

    public final boolean postDelayed(@NonNull Runnable r, @Nullable Object token, long delayMillis) {
        this.looper.executor.execute(() -> {
            try {
                Thread.sleep(delayMillis);
                r.run();
            } catch (Exception e) {
                System.out.println("looper postDelayed run err:" + e);
                e.printStackTrace();
            }
        });
        return true;
    }

    public final boolean postAtFrontOfQueue(@NonNull Runnable r) {
        throw new RuntimeException("Stub!");
    }

    public final void removeCallbacks(@NonNull Runnable r) {
        throw new RuntimeException("Stub!");
    }

    public final void removeCallbacks(@NonNull Runnable r, @Nullable Object token) {
        throw new RuntimeException("Stub!");
    }

    public final boolean sendMessage(@NonNull Message msg) {
        throw new RuntimeException("Stub!");
    }

    public final boolean sendEmptyMessage(int what) {
        throw new RuntimeException("Stub!");
    }

    public final boolean sendEmptyMessageDelayed(int what, long delayMillis) {
        throw new RuntimeException("Stub!");
    }

    public final boolean sendEmptyMessageAtTime(int what, long uptimeMillis) {
        throw new RuntimeException("Stub!");
    }

    public final boolean sendMessageDelayed(@NonNull Message msg, long delayMillis) {
        throw new RuntimeException("Stub!");
    }

    public boolean sendMessageAtTime(@NonNull Message msg, long uptimeMillis) {
        throw new RuntimeException("Stub!");
    }

    public final boolean sendMessageAtFrontOfQueue(@NonNull Message msg) {
        throw new RuntimeException("Stub!");
    }

    public final void removeMessages(int what) {
        throw new RuntimeException("Stub!");
    }

    public final void removeMessages(int what, @Nullable Object object) {
        throw new RuntimeException("Stub!");
    }

    public final void removeCallbacksAndMessages(@Nullable Object token) {
        throw new RuntimeException("Stub!");
    }

    public final boolean hasMessages(int what) {
        throw new RuntimeException("Stub!");
    }

    public final boolean hasMessages(int what, @Nullable Object object) {
        throw new RuntimeException("Stub!");
    }

    public final boolean hasCallbacks(@NonNull Runnable r) {
        throw new RuntimeException("Stub!");
    }

    @NonNull
    public final Looper getLooper() {
        throw new RuntimeException("Stub!");
    }

    public final void dump(@NonNull Printer pw, @NonNull String prefix) {
        throw new RuntimeException("Stub!");
    }

    public String toString() {
        throw new RuntimeException("Stub!");
    }

    public interface Callback {
        boolean handleMessage(@NonNull Message var1);
    }
}
