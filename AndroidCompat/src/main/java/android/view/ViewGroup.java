//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package android.view;

/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2026 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import android.animation.LayoutTransition;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewDebug.IntToString;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;
import android.view.animation.Transformation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class ViewGroup extends View implements ViewParent, ViewManager {
    protected static final int CLIP_TO_PADDING_MASK = 34;
    public static final int FOCUS_AFTER_DESCENDANTS = 262144;
    public static final int FOCUS_BEFORE_DESCENDANTS = 131072;
    public static final int FOCUS_BLOCK_DESCENDANTS = 393216;
    public static final int LAYOUT_MODE_CLIP_BOUNDS = 0;
    public static final int LAYOUT_MODE_OPTICAL_BOUNDS = 1;
    /** @deprecated */
    @Deprecated
    public static final int PERSISTENT_ALL_CACHES = 3;
    /** @deprecated */
    @Deprecated
    public static final int PERSISTENT_ANIMATION_CACHE = 1;
    /** @deprecated */
    @Deprecated
    public static final int PERSISTENT_NO_CACHE = 0;
    /** @deprecated */
    @Deprecated
    public static final int PERSISTENT_SCROLLING_CACHE = 2;

    private int mDescendantFocusability = FOCUS_BEFORE_DESCENDANTS;
    private boolean mClipChildren = true;
    private boolean mClipToPadding = true;
    private boolean mMotionEventSplittingEnabled = true;
    private boolean mTransitionGroup = false;
    private boolean mSuppressLayout = false;
    private int mLayoutMode = LAYOUT_MODE_CLIP_BOUNDS;
    private int mNestedScrollAxes = 0;
    private LayoutTransition mLayoutTransition;
    private LayoutAnimationController mLayoutAnimation;
    private OnHierarchyChangeListener mOnHierarchyChangeListener;
    private ViewGroupOverlay mOverlay;
    private final ArrayList<View> mChildren = new ArrayList<>();

    public ViewGroup(Context context) {
        super(context);
    }

    public ViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ViewGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @ExportedProperty(
            category = "focus",
            mapping = {@IntToString(
                    from = 131072,
                    to = "FOCUS_BEFORE_DESCENDANTS"
            ), @IntToString(
                    from = 262144,
                    to = "FOCUS_AFTER_DESCENDANTS"
            ), @IntToString(
                    from = 393216,
                    to = "FOCUS_BLOCK_DESCENDANTS"
            )}
    )
    public int getDescendantFocusability() {
        return mDescendantFocusability;
    }

    public void setDescendantFocusability(int focusability) {
        mDescendantFocusability = focusability;
    }

    public void requestChildFocus(View child, View focused) {
    }

    public void focusableViewAvailable(View v) {
    }

    public boolean showContextMenuForChild(View originalView) {
        return false;
    }

    public boolean showContextMenuForChild(View originalView, float x, float y) {
        return false;
    }

    public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback) {
        return null;
    }

    public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback, int type) {
        return null;
    }

    public View focusSearch(View focused, int direction) {
        return null;
    }

    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
        return false;
    }

    public boolean requestSendAccessibilityEvent(View child, AccessibilityEvent event) {
        return false;
    }

    public boolean onRequestSendAccessibilityEvent(View child, AccessibilityEvent event) {
        return false;
    }

    public void childHasTransientStateChanged(View child, boolean childHasTransientState) {
    }

    public boolean hasTransientState() {
        return false;
    }

    public boolean dispatchUnhandledMove(View focused, int direction) {
        return false;
    }

    public void clearChildFocus(View child) {
    }

    public void clearFocus() {
    }

    public View getFocusedChild() {
        return null;
    }

    public boolean hasFocus() {
        return super.hasFocus();
    }

    public View findFocus() {
        return super.findFocus();
    }

    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
    }

    public void addKeyboardNavigationClusters(Collection<View> views, int direction) {
    }

    public void setTouchscreenBlocksFocus(boolean touchscreenBlocksFocus) {
    }

    @ExportedProperty(
            category = "focus"
    )
    public boolean getTouchscreenBlocksFocus() {
        return false;
    }

    public void findViewsWithText(ArrayList<View> outViews, CharSequence text, int flags) {
    }

    public void dispatchWindowFocusChanged(boolean hasFocus) {
    }

    public void addTouchables(ArrayList<View> views) {
    }

    public void dispatchDisplayHint(int hint) {
    }

    protected void dispatchVisibilityChanged(View changedView, int visibility) {
    }

    public void dispatchWindowVisibilityChanged(int visibility) {
    }

    public void dispatchConfigurationChanged(Configuration newConfig) {
    }

    public void recomputeViewAttributes(View child) {
    }

    public void bringChildToFront(View child) {
    }

    public boolean dispatchDragEvent(DragEvent event) {
        return false;
    }

    /** @deprecated */
    @Deprecated
    public void dispatchWindowSystemUiVisiblityChanged(int visible) {
    }

    /** @deprecated */
    @Deprecated
    public void dispatchSystemUiVisibilityChanged(int visible) {
    }

    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        return false;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        return false;
    }

    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        return false;
    }

    public boolean dispatchTrackballEvent(MotionEvent event) {
        return false;
    }

    public boolean dispatchCapturedPointerEvent(MotionEvent event) {
        return false;
    }

    public void dispatchPointerCaptureChanged(boolean hasCapture) {
    }

    public PointerIcon onResolvePointerIcon(MotionEvent event, int pointerIndex) {
        return null;
    }

    protected boolean dispatchHoverEvent(MotionEvent event) {
        return false;
    }

    public void addChildrenForAccessibility(ArrayList<View> outChildren) {
    }

    public boolean onInterceptHoverEvent(MotionEvent event) {
        return false;
    }

    protected boolean dispatchGenericPointerEvent(MotionEvent event) {
        return false;
    }

    protected boolean dispatchGenericFocusedEvent(MotionEvent event) {
        return false;
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        return false;
    }

    public void setMotionEventSplittingEnabled(boolean split) {
        mMotionEventSplittingEnabled = split;
    }

    public boolean isMotionEventSplittingEnabled() {
        return mMotionEventSplittingEnabled;
    }

    public boolean isTransitionGroup() {
        return mTransitionGroup;
    }

    public void setTransitionGroup(boolean isTransitionGroup) {
        mTransitionGroup = isTransitionGroup;
    }

    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        return super.requestFocus(direction, previouslyFocusedRect);
    }

    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        return false;
    }

    public boolean restoreDefaultFocus() {
        return false;
    }

    public void dispatchStartTemporaryDetach() {
    }

    public void dispatchFinishTemporaryDetach() {
    }

    public void dispatchProvideStructure(ViewStructure structure) {
    }

    public void dispatchProvideAutofillStructure(ViewStructure structure, int flags) {
    }

    public void addExtraDataToAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info, @NonNull String extraDataKey, @Nullable Bundle arguments) {
    }

    public CharSequence getAccessibilityClassName() {
        return "ViewGroup";
    }

    public void notifySubtreeAccessibilityStateChanged(View child, View source, int changeType) {
    }

    public boolean onNestedPrePerformAccessibilityAction(View target, int action, Bundle args) {
        return false;
    }

    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
    }

    protected void dispatchFreezeSelfOnly(SparseArray<Parcelable> container) {
    }

    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
    }

    protected void dispatchThawSelfOnly(SparseArray<Parcelable> container) {
    }

    /** @deprecated */
    @Deprecated
    protected void setChildrenDrawingCacheEnabled(boolean enabled) {
    }

    protected void dispatchDraw(Canvas canvas) {
    }

    public ViewGroupOverlay getOverlay() {
        if (mOverlay == null) {
            mOverlay = new ViewGroupOverlay();
        }
        return mOverlay;
    }

    protected int getChildDrawingOrder(int childCount, int drawingPosition) {
        return drawingPosition;
    }

    public final int getChildDrawingOrder(int drawingPosition) {
        return drawingPosition;
    }

    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        return false;
    }

    @ExportedProperty(
            category = "drawing"
    )
    public boolean getClipChildren() {
        return mClipChildren;
    }

    public void setClipChildren(boolean clipChildren) {
        mClipChildren = clipChildren;
    }

    public void setClipToPadding(boolean clipToPadding) {
        mClipToPadding = clipToPadding;
    }

    @ExportedProperty(
            category = "drawing"
    )
    public boolean getClipToPadding() {
        return mClipToPadding;
    }

    public void dispatchSetSelected(boolean selected) {
    }

    public void dispatchSetActivated(boolean activated) {
    }

    protected void dispatchSetPressed(boolean pressed) {
    }

    public void dispatchDrawableHotspotChanged(float x, float y) {
    }

    protected void setStaticTransformationsEnabled(boolean enabled) {
    }

    protected boolean getChildStaticTransformation(View child, Transformation t) {
        return false;
    }

    public void addView(View child) {
        addView(child, -1);
    }

    public void addView(View child, int index) {
        addView(child, index, generateDefaultLayoutParams());
    }

    public void addView(View child, int width, int height) {
        final LayoutParams params = generateDefaultLayoutParams();
        params.width = width;
        params.height = height;
        addView(child, -1, params);
    }

    public void addView(View child, LayoutParams params) {
        addView(child, -1, params);
    }

    public void addView(View child, int index, LayoutParams params) {
        if (child.getParent() != null) {
            ((ViewGroup) child.getParent()).removeView(child);
        }
        child.setLayoutParams(params);
        if (index < 0 || index >= mChildren.size()) {
            mChildren.add(child);
        } else {
            mChildren.add(index, child);
        }
        if (mOnHierarchyChangeListener != null) {
            mOnHierarchyChangeListener.onChildViewAdded(this, child);
        }
    }

    public void updateViewLayout(View view, LayoutParams params) {
        view.setLayoutParams(params);
    }

    protected boolean checkLayoutParams(LayoutParams p) {
        return p != null;
    }

    public void setOnHierarchyChangeListener(OnHierarchyChangeListener listener) {
        mOnHierarchyChangeListener = listener;
    }

    public void onViewAdded(View child) {
    }

    public void onViewRemoved(View child) {
    }

    protected void onAttachedToWindow() {
    }

    protected void onDetachedFromWindow() {
    }

    protected boolean addViewInLayout(View child, int index, LayoutParams params) {
        return false;
    }

    protected boolean addViewInLayout(View child, int index, LayoutParams params, boolean preventRequestLayout) {
        return false;
    }

    protected void cleanupLayoutState(View child) {
    }

    protected void attachLayoutAnimationParameters(View child, LayoutParams params, int index, int count) {
    }

    public void removeView(View view) {
        removeViewInternal(view);
    }

    public void removeViewInLayout(View view) {
        removeViewInternal(view);
    }

    public void removeViewsInLayout(int start, int count) {
        for (int i = 0; i < count; i++) {
            if (start < mChildren.size()) {
                mChildren.remove(start);
            }
        }
    }

    public void removeViewAt(int index) {
        View child = getChildAt(index);
        if (child != null) {
            removeViewInternal(child);
        }
    }

    public void removeViews(int start, int count) {
        for (int i = 0; i < count; i++) {
            if (start < mChildren.size()) {
                mChildren.remove(start);
            }
        }
    }

    private void removeViewInternal(View view) {
        if (mChildren.remove(view)) {
            if (mOnHierarchyChangeListener != null) {
                mOnHierarchyChangeListener.onChildViewRemoved(this, view);
            }
        }
    }

    public void setLayoutTransition(LayoutTransition transition) {
        mLayoutTransition = transition;
    }

    public LayoutTransition getLayoutTransition() {
        return mLayoutTransition;
    }

    public void removeAllViews() {
        mChildren.clear();
    }

    public void removeAllViewsInLayout() {
        mChildren.clear();
    }

    protected void removeDetachedView(View child, boolean animate) {
    }

    protected void attachViewToParent(View child, int index, LayoutParams params) {
    }

    protected void detachViewFromParent(View child) {
    }

    protected void detachViewFromParent(int index) {
    }

    protected void detachViewsFromParent(int start, int count) {
    }

    protected void detachAllViewsFromParent() {
    }

    public void onDescendantInvalidated(@NonNull View child, @NonNull View target) {
    }

    /** @deprecated */
    @Deprecated
    public final void invalidateChild(View child, Rect dirty) {
    }

    /** @deprecated */
    @Deprecated
    public ViewParent invalidateChildInParent(int[] location, Rect dirty) {
        return null;
    }

    public final void offsetDescendantRectToMyCoords(View descendant, Rect rect) {
    }

    public final void offsetRectIntoDescendantCoords(View descendant, Rect rect) {
    }

    public boolean getChildVisibleRect(View child, Rect r, Point offset) {
        return false;
    }

    public final void layout(int l, int t, int r, int b) {
        super.layout(l, t, r, b);
    }

    protected abstract void onLayout(boolean var1, int var2, int var3, int var4, int var5);

    protected boolean canAnimate() {
        return false;
    }

    public void startLayoutAnimation() {
    }

    public void scheduleLayoutAnimation() {
    }

    public void setLayoutAnimation(LayoutAnimationController controller) {
        mLayoutAnimation = controller;
    }

    public LayoutAnimationController getLayoutAnimation() {
        return mLayoutAnimation;
    }

    /** @deprecated */
    @Deprecated
    public boolean isAnimationCacheEnabled() {
        return false;
    }

    /** @deprecated */
    @Deprecated
    public void setAnimationCacheEnabled(boolean enabled) {
    }

    /** @deprecated */
    @Deprecated
    public boolean isAlwaysDrawnWithCacheEnabled() {
        return false;
    }

    /** @deprecated */
    @Deprecated
    public void setAlwaysDrawnWithCacheEnabled(boolean always) {
    }

    /** @deprecated */
    @Deprecated
    protected boolean isChildrenDrawnWithCacheEnabled() {
        return false;
    }

    /** @deprecated */
    @Deprecated
    protected void setChildrenDrawnWithCacheEnabled(boolean enabled) {
    }

    @ExportedProperty(
            category = "drawing"
    )
    protected boolean isChildrenDrawingOrderEnabled() {
        return false;
    }

    protected void setChildrenDrawingOrderEnabled(boolean enabled) {
    }

    /** @deprecated */
    @Deprecated
    @ExportedProperty(
            category = "drawing",
            mapping = {@IntToString(
                    from = 0,
                    to = "NONE"
            ), @IntToString(
                    from = 1,
                    to = "ANIMATION"
            ), @IntToString(
                    from = 2,
                    to = "SCROLLING"
            ), @IntToString(
                    from = 3,
                    to = "ALL"
            )}
    )
    public int getPersistentDrawingCache() {
        return 0;
    }

    /** @deprecated */
    @Deprecated
    public void setPersistentDrawingCache(int drawingCacheToKeep) {
    }

    public int getLayoutMode() {
        return mLayoutMode;
    }

    public void setLayoutMode(int layoutMode) {
        mLayoutMode = layoutMode;
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new LayoutParams(p);
    }

    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    protected void debug(int depth) {
    }

    public int indexOfChild(View child) {
        return mChildren.indexOf(child);
    }

    public int getChildCount() {
        return mChildren.size();
    }

    public View getChildAt(int index) {
        if (index < 0 || index >= mChildren.size()) {
            return null;
        }
        return mChildren.get(index);
    }

    protected void measureChildren(int widthMeasureSpec, int heightMeasureSpec) {
    }

    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
    }

    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
    }

    public static int getChildMeasureSpec(int spec, int padding, int childDimension) {
        int mode = MeasureSpec.getMode(spec);
        int size = MeasureSpec.getSize(spec);
        if (childDimension >= 0) {
            return MeasureSpec.makeMeasureSpec(childDimension, MeasureSpec.EXACTLY);
        } else if (childDimension == LayoutParams.MATCH_PARENT) {
            return MeasureSpec.makeMeasureSpec(Math.max(0, size - padding), mode);
        } else if (childDimension == LayoutParams.WRAP_CONTENT) {
            return MeasureSpec.makeMeasureSpec(Math.max(0, size - padding), mode == MeasureSpec.UNSPECIFIED ? MeasureSpec.UNSPECIFIED : MeasureSpec.AT_MOST);
        }
        return 0;
    }

    public void clearDisappearingChildren() {
    }

    public void startViewTransition(View view) {
    }

    public void endViewTransition(View view) {
    }

    public void suppressLayout(boolean suppress) {
        mSuppressLayout = suppress;
    }

    public boolean isLayoutSuppressed() {
        return mSuppressLayout;
    }

    public boolean gatherTransparentRegion(Region region) {
        return false;
    }

    public void requestTransparentRegion(View child) {
    }

    public WindowInsets dispatchApplyWindowInsets(WindowInsets insets) {
        return null;
    }

    public void setWindowInsetsAnimationCallback(@Nullable WindowInsetsAnimation.Callback callback) {
    }

    public void dispatchWindowInsetsAnimationPrepare(@NonNull WindowInsetsAnimation animation) {
    }

    @NonNull
    public WindowInsetsAnimation.Bounds dispatchWindowInsetsAnimationStart(@NonNull WindowInsetsAnimation animation, @NonNull WindowInsetsAnimation.Bounds bounds) {
        return bounds;
    }

    @NonNull
    public WindowInsets dispatchWindowInsetsAnimationProgress(@NonNull WindowInsets insets, @NonNull List<WindowInsetsAnimation> runningAnimations) {
        return null;
    }

    public void dispatchWindowInsetsAnimationEnd(@NonNull WindowInsetsAnimation animation) {
    }

    public Animation.AnimationListener getLayoutAnimationListener() {
        return null;
    }

    protected void drawableStateChanged() {
    }

    public void jumpDrawablesToCurrentState() {
    }

    protected int[] onCreateDrawableState(int extraSpace) {
        return new int[0];
    }

    public void setAddStatesFromChildren(boolean addsStates) {
    }

    public boolean addStatesFromChildren() {
        return false;
    }

    public void childDrawableStateChanged(View child) {
    }

    public void setLayoutAnimationListener(Animation.AnimationListener animationListener) {
    }

    public boolean shouldDelayChildPressedState() {
        return true;
    }

    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return false;
    }

    public void onNestedScrollAccepted(View child, View target, int axes) {
    }

    public void onStopNestedScroll(View child) {
    }

    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
    }

    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
    }

    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return false;
    }

    public int getNestedScrollAxes() {
        return mNestedScrollAxes;
    }

    public interface OnHierarchyChangeListener {
        void onChildViewAdded(View var1, View var2);

        void onChildViewRemoved(View var1, View var2);
    }

    public static class MarginLayoutParams extends LayoutParams {
        @ExportedProperty(
                category = "layout"
        )
        public int bottomMargin;
        @ExportedProperty(
                category = "layout"
        )
        public int leftMargin;
        @ExportedProperty(
                category = "layout"
        )
        public int rightMargin;
        @ExportedProperty(
                category = "layout"
        )
        public int topMargin;

        public MarginLayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public MarginLayoutParams(int width, int height) {
            super(width, height);
        }

        public MarginLayoutParams(MarginLayoutParams source) {
            super(source);
            this.leftMargin = source.leftMargin;
            this.topMargin = source.topMargin;
            this.rightMargin = source.rightMargin;
            this.bottomMargin = source.bottomMargin;
        }

        public MarginLayoutParams(LayoutParams source) {
            super(source);
        }

        public void setMargins(int left, int top, int right, int bottom) {
            leftMargin = left;
            topMargin = top;
            rightMargin = right;
            bottomMargin = bottom;
        }

        public void setMarginStart(int start) {
            leftMargin = start;
        }

        public int getMarginStart() {
            return leftMargin;
        }

        public void setMarginEnd(int end) {
            rightMargin = end;
        }

        public int getMarginEnd() {
            return rightMargin;
        }

        public boolean isMarginRelative() {
            return false;
        }

        public void setLayoutDirection(int layoutDirection) {
        }

        public int getLayoutDirection() {
            return 0;
        }

        public void resolveLayoutDirection(int layoutDirection) {
        }
    }

    public static class LayoutParams {
        /** @deprecated */
        @Deprecated
        public static final int FILL_PARENT = -1;
        public static final int MATCH_PARENT = -1;
        public static final int WRAP_CONTENT = -2;
        @ExportedProperty(
                category = "layout",
                mapping = {@IntToString(
                        from = -1,
                        to = "MATCH_PARENT"
                ), @IntToString(
                        from = -2,
                        to = "WRAP_CONTENT"
                )}
        )
        public int height;
        public LayoutAnimationController.AnimationParameters layoutAnimationParameters;
        @ExportedProperty(
                category = "layout",
                mapping = {@IntToString(
                        from = -1,
                        to = "MATCH_PARENT"
                ), @IntToString(
                        from = -2,
                        to = "WRAP_CONTENT"
                )}
        )
        public int width;

        public LayoutParams(Context c, AttributeSet attrs) {
        }

        public LayoutParams(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public LayoutParams(LayoutParams source) {
            this.width = source.width;
            this.height = source.height;
        }

        protected void setBaseAttributes(TypedArray a, int widthAttr, int heightAttr) {
        }

        public void resolveLayoutDirection(int layoutDirection) {
        }
    }
}
