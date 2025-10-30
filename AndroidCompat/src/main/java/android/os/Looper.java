//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package android.os;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.util.Printer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class Looper {
    public static final ScheduledExecutorService MAIN_EXECUTOR = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "MainDispatcher");
        thread.setDaemon(true);
        thread.setPriority(Thread.MAX_PRIORITY);
        return thread;
    });

    public static final Looper MAIN_LOOPER = new Looper(MAIN_EXECUTOR);

    public ScheduledExecutorService executor;

    private Looper(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    public static void prepare() {
    }

    /** @deprecated */
    @Deprecated
    public static void prepareMainLooper() {
    }

    public static Looper getMainLooper() {
        return MAIN_LOOPER;
    }

    public static void loop() {
    }

    @Nullable
    public static Looper myLooper() {
        return MAIN_LOOPER;
    }

    @NonNull
    public static MessageQueue myQueue() {
        throw new RuntimeException("Stub!");
    }

    public boolean isCurrentThread() {
        throw new RuntimeException("Stub!");
    }

    public void setMessageLogging(@Nullable Printer printer) {
        throw new RuntimeException("Stub!");
    }

    public void quit() {
        throw new RuntimeException("Stub!");
    }

    public void quitSafely() {
        throw new RuntimeException("Stub!");
    }

    @NonNull
    public Thread getThread() {
        throw new RuntimeException("Stub!");
    }

    @NonNull
    public MessageQueue getQueue() {
        throw new RuntimeException("Stub!");
    }

    public void dump(@NonNull Printer pw, @NonNull String prefix) {
        throw new RuntimeException("Stub!");
    }
}
