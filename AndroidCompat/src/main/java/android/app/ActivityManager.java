package android.app;

/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ActivityManager {

    public static class MemoryInfo {
        /**
         * The advertised memory of the system, as the end user would encounter in a retail display
         * environment. This value might be different from {@code totalMem}. This could be due to
         * many reasons. For example, the ODM could reserve part of the memory for the Trusted
         * Execution Environment (TEE) which the kernel doesn't have access or knowledge about it.
         */
        public long advertisedMem;
        /**
         * The available memory on the system.  This number should not
         * be considered absolute: due to the nature of the kernel, a significant
         * portion of this memory is actually in use and needed for the overall
         * system to run well.
         */
        public long availMem;
        /**
         * The total memory accessible by the kernel.  This is basically the
         * RAM size of the device, not including below-kernel fixed allocations
         * like DMA buffers, RAM for the baseband CPU, etc.
         */
        public long totalMem;
        /**
         * The threshold of {@link #availMem} at which we consider memory to be
         * low and start killing background services and other non-extraneous
         * processes.
         */
        public long threshold;
        /**
         * Set to true if the system considers itself to currently be in a low
         * memory situation.
         */
        public boolean lowMemory;
        /** @hide */
        public long hiddenAppThreshold;
        /** @hide */
        public long secondaryServerThreshold;
        /** @hide */
        public long visibleAppThreshold;
        /** @hide */
        public long foregroundAppThreshold;
        public MemoryInfo() {
        }
        public int describeContents() {
            return 0;
        }
    }
    /**
     * Return general information about the memory state of the system.  This
     * can be used to help decide how to manage your own memory, though note
     * that polling is not recommended and
     * {@link android.content.ComponentCallbacks2#onTrimMemory(int)
     * ComponentCallbacks2.onTrimMemory(int)} is the preferred way to do this.
     * Also see {@link #getMyMemoryState} for how to retrieve the current trim
     * level of your process as needed, which gives a better hint for how to
     * manage its memory.
     */
    public void getMemoryInfo(MemoryInfo outInfo) {
        outInfo.advertisedMem = 512 * 1024 * 1024;
        outInfo.availMem = 256 * 1024 * 1024;
        outInfo.totalMem = 512 * 1024 * 1024;
        outInfo.threshold = 64 * 1024 * 1024;
        outInfo.lowMemory = true;
    }
}
