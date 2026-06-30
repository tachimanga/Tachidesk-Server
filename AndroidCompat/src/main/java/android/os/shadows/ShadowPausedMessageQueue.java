package android.os.shadows;

/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2026 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

// package org.robolectric.shadows;
//  and badly gutted

import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.MessageQueue.IdleHandler;
import android.os.SystemClock;
import android.util.Log;
import java.time.Duration;
import java.util.ArrayList;

/**
 * The shadow {@link} MessageQueue} for {@link LooperMode.Mode.PAUSED}
 *
 * <p>This class should not be referenced directly. Use {@link ShadowMessageQueue} instead.
 */
@SuppressWarnings("SynchronizeOnNonFinalField")
public class ShadowPausedMessageQueue {

  // just use this class as the native object
  private static NativeObjRegistry<ShadowPausedMessageQueue> nativeQueueRegistry =
      new NativeObjRegistry<ShadowPausedMessageQueue>(ShadowPausedMessageQueue.class);
  private boolean isPolling = false;
  private Exception uncaughtException = null;

  // shadow constructor instead of nativeInit because nativeInit signature has changed across SDK
  // versions
  public static long nativeInit() {
    return nativeQueueRegistry.register(new ShadowPausedMessageQueue());
  }

  public static void nativeDestroy(long ptr) {
    nativeQueueRegistry.unregister(ptr);
  }

  public static void nativePollOnce(long ptr, int timeoutMillis) {
    ShadowPausedMessageQueue obj = nativeQueueRegistry.getNativeObject(ptr);
    obj.nativePollOnce(timeoutMillis);
  }

  public void nativePollOnce(int timeoutMillis) {
    if (timeoutMillis == 0) {
      return;
    }
    synchronized (this) {
      isPolling = true;
      try {
        if (timeoutMillis < 0) {
          this.wait();
        } else {
          this.wait(timeoutMillis);
        }
      } catch (InterruptedException e) {
        // ignore
      }
      isPolling = false;
    }
  }

  public static void nativeWake(long ptr) {
    ShadowPausedMessageQueue obj = nativeQueueRegistry.getNativeObject(ptr);
    synchronized (obj) {
      obj.notifyAll();
    }
  }

  public static boolean nativeIsPolling(long ptr) {
    return nativeQueueRegistry.getNativeObject(ptr).isPolling;
  }
}

