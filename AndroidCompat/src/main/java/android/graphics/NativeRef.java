/*
 * Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved.
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

class NativeRef {

    private volatile long address;
    NativeRef(long address) {
        this.address = address;
    }

    long address() {
        return address;
    }

    void clear() {
        address = 0;
    }
}
