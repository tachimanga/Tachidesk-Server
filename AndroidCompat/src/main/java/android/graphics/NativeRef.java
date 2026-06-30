/*
 * Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved.
 * Copyright (C) 2023 Tachimanga
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package android.graphics;

public class NativeRef {

    private volatile long address;
    public NativeRef(long address) {
        this.address = address;
    }

    public long address() {
        return address;
    }

    public void clear() {
        address = 0;
    }
}
