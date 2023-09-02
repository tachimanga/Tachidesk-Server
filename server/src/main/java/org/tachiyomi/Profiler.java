package org.tachiyomi;

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */
 

public class Profiler {
    private static final ThreadLocal<Long> TIME_THREADLOCAL = new ThreadLocal<>();
    private static final ThreadLocal<Long> TIME_THREAD_LOCAL_ALL = new ThreadLocal<>();
    private static final ThreadLocal<Long> TIME_THREAD_LOCAL_NET = new ThreadLocal<>();

    public static void start() {
        TIME_THREADLOCAL.set(System.currentTimeMillis());
        TIME_THREAD_LOCAL_ALL.set(System.currentTimeMillis());
        TIME_THREAD_LOCAL_NET.set(0L);
    }

    public static void split(String key) {
        Long aLong = TIME_THREADLOCAL.get();
        if (aLong != null) {
            System.out.println("Profiler: " + key
                    + " cost:" + (System.currentTimeMillis() - aLong) + "ms");
        }
        TIME_THREADLOCAL.set(System.currentTimeMillis());
    }

    public static void incrNet(String tag, long cost) {
        if (TIME_THREAD_LOCAL_NET.get() != null) {
            TIME_THREAD_LOCAL_NET.set(TIME_THREAD_LOCAL_NET.get() + cost);
        }
    }

    public static void all() {
        Long t = TIME_THREAD_LOCAL_ALL.get();
        if (t != null) {
            long all = System.currentTimeMillis() - t;
            long net = TIME_THREAD_LOCAL_NET.get();
            System.out.println("Profiler: all cost " + all + "ms, "
                    + "net cost: " + net + "ms, "
                    + "left cost:" + (all - net) + "ms");
        }
    }
}

