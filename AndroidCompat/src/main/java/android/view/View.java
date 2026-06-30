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

import android.animation.StateListAnimator;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.ClipData;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Property;
import android.util.SparseArray;
import android.view.ViewDebug.CapturedViewProperty;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewDebug.FlagToString;
import android.view.ViewDebug.IntToString;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityEventSource;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.animation.Animation;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.view.contentcapture.ContentCaptureSession;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class View implements Drawable.Callback, KeyEvent.Callback, AccessibilityEventSource {
    private Context mContext;
    private int mId = NO_ID;
    private int mVisibility = VISIBLE;
    private boolean mEnabled = true;
    private boolean mClickable = false;
    private boolean mLongClickable = false;
    private boolean mFocusable = false;
    private boolean mFocusableInTouchMode = false;
    private boolean mPressed = false;
    private boolean mSelected = false;
    private boolean mActivated = false;
    private boolean mSaveEnabled = true;
    private boolean mFilterTouchesWhenObscured = false;
    private boolean mDuplicateParentStateEnabled = false;
    private boolean mKeepScreenOn = false;
    private boolean mWillNotDraw = false;
    private boolean mHorizontalScrollBarEnabled = true;
    private boolean mVerticalScrollBarEnabled = true;
    private boolean mScrollbarFadingEnabled = true;
    private boolean mSoundEffectsEnabled = true;
    private boolean mHapticFeedbackEnabled = true;
    private boolean mScreenReaderFocusable = false;
    private boolean mAccessibilityHeading = false;
    private boolean mKeyboardNavigationCluster = false;
    private boolean mFocusedByDefault = false;
    private boolean mDefaultFocusHighlightEnabled = true;
    private boolean mNestedScrollingEnabled = true;
    private boolean mHardwareAccelerated = false;
    private boolean mInEditMode = false;
    private boolean mClipToOutline = false;
    private boolean mForceDarkAllowed = false;
    private int mOverScrollMode = OVER_SCROLL_IF_CONTENT_SCROLLS;
    private int mLayoutDirection = LAYOUT_DIRECTION_INHERIT;
    private int mTextDirection = TEXT_DIRECTION_INHERIT;
    private int mTextAlignment = TEXT_ALIGNMENT_INHERIT;
    private int mImportantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_AUTO;
    private int mImportantForAutofill = IMPORTANT_FOR_AUTOFILL_AUTO;
    private int mImportantForContentCapture = IMPORTANT_FOR_CONTENT_CAPTURE_AUTO;
    private int mAccessibilityLiveRegion = ACCESSIBILITY_LIVE_REGION_NONE;
    private int mLayerType = LAYER_TYPE_NONE;
    private int mScrollBarStyle = SCROLLBARS_INSIDE_OVERLAY;
    private int mDrawingCacheQuality = DRAWING_CACHE_QUALITY_AUTO;
    private int mScrollX = 0;
    private int mScrollY = 0;
    private int mWidth = 0;
    private int mHeight = 0;
    private int mMeasuredWidth = 0;
    private int mMeasuredHeight = 0;
    private int mMeasuredWidthAndState = 0;
    private int mMeasuredHeightAndState = 0;
    private int mPaddingLeft = 0;
    private int mPaddingTop = 0;
    private int mPaddingRight = 0;
    private int mPaddingBottom = 0;
    private int mPaddingStart = 0;
    private int mPaddingEnd = 0;
    private boolean mPaddingRelative = false;
    private int mMinHeight = 0;
    private int mMinWidth = 0;
    private int mVerticalScrollbarPosition = SCROLLBAR_POSITION_RIGHT;
    private int mScrollIndicators = 0;
    private int mScrollBarSize = 0;
    private int mScrollBarFadeDuration = 0;
    private int mScrollBarDefaultDelayBeforeFade = 0;
    private float mAlpha = 1.0f;
    private float mTransitionAlpha = 1.0f;
    private float mRotation = 0f;
    private float mRotationX = 0f;
    private float mRotationY = 0f;
    private float mScaleX = 1f;
    private float mScaleY = 1f;
    private float mPivotX = 0f;
    private float mPivotY = 0f;
    private float mX = 0f;
    private float mY = 0f;
    private float mZ = 0f;
    private float mElevation = 0f;
    private float mTranslationX = 0f;
    private float mTranslationY = 0f;
    private float mTranslationZ = 0f;
    private float mCameraDistance = 0f;
    private Object mTag = null;
    private SparseArray<Object> mKeyedTags = null;
    private ViewGroup.LayoutParams mLayoutParams = null;
    private ViewParent mParent = null;
    private ViewOutlineProvider mOutlineProvider = null;
    private OnClickListener mOnClickListener = null;
    private OnLongClickListener mOnLongClickListener = null;
    private OnKeyListener mOnKeyListener = null;
    private OnTouchListener mOnTouchListener = null;
    private OnHoverListener mOnHoverListener = null;
    private OnGenericMotionListener mOnGenericMotionListener = null;
    private OnDragListener mOnDragListener = null;
    private OnFocusChangeListener mOnFocusChangeListener = null;
    private OnLayoutChangeListener mOnLayoutChangeListener = null;
    private OnScrollChangeListener mOnScrollChangeListener = null;
    private OnApplyWindowInsetsListener mOnApplyWindowInsetsListener = null;
    private OnCapturedPointerListener mOnCapturedPointerListener = null;
    private OnSystemUiVisibilityChangeListener mOnSystemUiVisibilityChangeListener = null;
    private Drawable mBackground = null;
    private Drawable mForeground = null;
    private int mForegroundGravity = 0;
    private ColorStateList mBackgroundTintList = null;
    private PorterDuff.Mode mBackgroundTintMode = null;
    private BlendMode mBackgroundTintBlendMode = null;
    private ColorStateList mForegroundTintList = null;
    private PorterDuff.Mode mForegroundTintMode = null;
    private BlendMode mForegroundTintBlendMode = null;
    private Animation mAnimation = null;
    private StateListAnimator mStateListAnimator = null;
    private CharSequence mContentDescription = null;
    private CharSequence mAccessibilityPaneTitle = null;
    private CharSequence mStateDescription = null;
    private CharSequence mTooltipText = null;
    private String mTransitionName = null;
    private String[] mAutofillHints = null;
    private AccessibilityDelegate mAccessibilityDelegate = null;
    private ContentCaptureSession mContentCaptureSession = null;
    private TouchDelegate mTouchDelegate = null;
    private WindowInsets mRootWindowInsets = null;
    private int mSystemUiVisibility = 0;
    private int mLabelFor = NO_ID;
    private int mAccessibilityTraversalBefore = NO_ID;
    private int mAccessibilityTraversalAfter = NO_ID;
    private int mNextFocusLeftId = NO_ID;
    private int mNextFocusRightId = NO_ID;
    private int mNextFocusUpId = NO_ID;
    private int mNextFocusDownId = NO_ID;
    private int mNextFocusForwardId = NO_ID;
    private int mNextClusterForwardId = NO_ID;
    private int mOutlineSpotShadowColor = 0;
    private int mOutlineAmbientShadowColor = 0;
    private boolean mHasTransientState = false;
    private boolean mRevealOnFocusHint = true;
    private List<Rect> mSystemGestureExclusionRects = new ArrayList<>();
    private Rect mClipBounds = null;
    private Matrix mMatrix = null;
    private ViewPropertyAnimator mViewPropertyAnimator = null;
    private Handler mHandler = null;
    private boolean mIsScrollContainer = false;
    private boolean mHorizontalFadingEdgeEnabled = false;
    private boolean mVerticalFadingEdgeEnabled = true;
    private int mFadingEdgeLength = 0;
    private int mVerticalScrollbarWidth = 0;
    private int mHorizontalScrollbarHeight = 0;
    private Drawable mVerticalScrollbarThumbDrawable = null;
    private Drawable mVerticalScrollbarTrackDrawable = null;
    private Drawable mHorizontalScrollbarThumbDrawable = null;
    private Drawable mHorizontalScrollbarTrackDrawable = null;
    private int mExplicitStyle = 0;
    private boolean mIsShowingLayoutBounds = false;
    private int mSourceLayoutResId = 0;
    private long mUniqueDrawingId = 0;
    private int mSolidColor = 0;
    private int mScrollBarPosition = 0;
    private PointerIcon mPointerIcon = null;
    private boolean mPointerCapture = false;
    private AutofillId mAutofillId = null;
    private IBinder mWindowToken = null;
    private int[] mDrawableState = null;
    private int mResolvedLayoutDirection = LAYOUT_DIRECTION_INHERIT;
    private boolean mLayoutDirectionResolved = false;
    private int mResolvedTextDirection = TEXT_DIRECTION_INHERIT;
    private boolean mTextDirectionResolved = false;
    private int mResolvedTextAlignment = TEXT_ALIGNMENT_INHERIT;
    private boolean mTextAlignmentResolved = false;
    private int mMeasuredState = 0;
    private boolean mIsLayoutRequested = false;
    private boolean mIsInLayout = false;
    private boolean mIsPaddingOffsetRequired = false;
    private int mLeftPaddingOffset = 0;
    private int mRightPaddingOffset = 0;
    private int mTopPaddingOffset = 0;
    private int mBottomPaddingOffset = 0;
    private int mBaseline = -1;
    private boolean mIsOpaque = false;
    private boolean mHasOverlappingRendering = true;
    private boolean mIsHovered = false;
    private boolean mIsDirty = false;
    private boolean mIsAttachedToWindow = false;
    private boolean mIsLaidOut = false;
    private int mWindowAttachCount = 0;
    private int mWindowVisibility = VISIBLE;
    private boolean mHasWindowFocus = false;
    private boolean mIsInTouchMode = false;
    private int mScreenStateException = SCREEN_STATE_ON;
    private WindowId mWindowId = null;
    private IBinder mApplicationWindowToken = null;
    private Display mDisplay = null;

    public static final int ACCESSIBILITY_LIVE_REGION_ASSERTIVE = 2;
    public static final int ACCESSIBILITY_LIVE_REGION_NONE = 0;
    public static final int ACCESSIBILITY_LIVE_REGION_POLITE = 1;
    public static final Property<View, Float> ALPHA = null;
    public static final int AUTOFILL_FLAG_INCLUDE_NOT_IMPORTANT_VIEWS = 1;
    public static final String AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DATE = "creditCardExpirationDate";
    public static final String AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DAY = "creditCardExpirationDay";
    public static final String AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH = "creditCardExpirationMonth";
    public static final String AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR = "creditCardExpirationYear";
    public static final String AUTOFILL_HINT_CREDIT_CARD_NUMBER = "creditCardNumber";
    public static final String AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE = "creditCardSecurityCode";
    public static final String AUTOFILL_HINT_EMAIL_ADDRESS = "emailAddress";
    public static final String AUTOFILL_HINT_NAME = "name";
    public static final String AUTOFILL_HINT_PASSWORD = "password";
    public static final String AUTOFILL_HINT_PHONE = "phone";
    public static final String AUTOFILL_HINT_POSTAL_ADDRESS = "postalAddress";
    public static final String AUTOFILL_HINT_POSTAL_CODE = "postalCode";
    public static final String AUTOFILL_HINT_USERNAME = "username";
    public static final int AUTOFILL_TYPE_DATE = 4;
    public static final int AUTOFILL_TYPE_LIST = 3;
    public static final int AUTOFILL_TYPE_NONE = 0;
    public static final int AUTOFILL_TYPE_TEXT = 1;
    public static final int AUTOFILL_TYPE_TOGGLE = 2;
    public static final int DRAG_FLAG_GLOBAL = 256;
    public static final int DRAG_FLAG_GLOBAL_PERSISTABLE_URI_PERMISSION = 64;
    public static final int DRAG_FLAG_GLOBAL_PREFIX_URI_PERMISSION = 128;
    public static final int DRAG_FLAG_GLOBAL_URI_READ = 1;
    public static final int DRAG_FLAG_GLOBAL_URI_WRITE = 2;
    public static final int DRAG_FLAG_OPAQUE = 512;
    /** @deprecated */
    @Deprecated
    public static final int DRAWING_CACHE_QUALITY_AUTO = 0;
    /** @deprecated */
    @Deprecated
    public static final int DRAWING_CACHE_QUALITY_HIGH = 1048576;
    /** @deprecated */
    @Deprecated
    public static final int DRAWING_CACHE_QUALITY_LOW = 524288;
    protected static final int[] EMPTY_STATE_SET = new int[0];
    protected static final int[] ENABLED_FOCUSED_SELECTED_STATE_SET = new int[0];
    protected static final int[] ENABLED_FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET = new int[0];
    protected static final int[] ENABLED_FOCUSED_STATE_SET = new int[0];
    protected static final int[] ENABLED_FOCUSED_WINDOW_FOCUSED_STATE_SET = new int[0];
    protected static final int[] ENABLED_SELECTED_STATE_SET = new int[0];
    protected static final int[] ENABLED_SELECTED_WINDOW_FOCUSED_STATE_SET = new int[0];
    protected static final int[] ENABLED_STATE_SET = new int[0];
    protected static final int[] ENABLED_WINDOW_FOCUSED_STATE_SET = new int[0];
    public static final int FIND_VIEWS_WITH_CONTENT_DESCRIPTION = 2;
    public static final int FIND_VIEWS_WITH_TEXT = 1;
    public static final int FOCUSABLE = 1;
    public static final int FOCUSABLES_ALL = 0;
    public static final int FOCUSABLES_TOUCH_MODE = 1;
    public static final int FOCUSABLE_AUTO = 16;
    protected static final int[] FOCUSED_SELECTED_STATE_SET = new int[0];
    protected static final int[] FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET = new int[0];
    protected static final int[] FOCUSED_STATE_SET = new int[0];
    protected static final int[] FOCUSED_WINDOW_FOCUSED_STATE_SET = new int[0];
    public static final int FOCUS_BACKWARD = 1;
    public static final int FOCUS_DOWN = 130;
    public static final int FOCUS_FORWARD = 2;
    public static final int FOCUS_LEFT = 17;
    public static final int FOCUS_RIGHT = 66;
    public static final int FOCUS_UP = 33;
    public static final int GONE = 8;
    public static final int HAPTIC_FEEDBACK_ENABLED = 268435456;
    public static final int IMPORTANT_FOR_ACCESSIBILITY_AUTO = 0;
    public static final int IMPORTANT_FOR_ACCESSIBILITY_NO = 2;
    public static final int IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS = 4;
    public static final int IMPORTANT_FOR_ACCESSIBILITY_YES = 1;
    public static final int IMPORTANT_FOR_AUTOFILL_AUTO = 0;
    public static final int IMPORTANT_FOR_AUTOFILL_NO = 2;
    public static final int IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS = 8;
    public static final int IMPORTANT_FOR_AUTOFILL_YES = 1;
    public static final int IMPORTANT_FOR_AUTOFILL_YES_EXCLUDE_DESCENDANTS = 4;
    public static final int IMPORTANT_FOR_CONTENT_CAPTURE_AUTO = 0;
    public static final int IMPORTANT_FOR_CONTENT_CAPTURE_NO = 2;
    public static final int IMPORTANT_FOR_CONTENT_CAPTURE_NO_EXCLUDE_DESCENDANTS = 8;
    public static final int IMPORTANT_FOR_CONTENT_CAPTURE_YES = 1;
    public static final int IMPORTANT_FOR_CONTENT_CAPTURE_YES_EXCLUDE_DESCENDANTS = 4;
    public static final int INVISIBLE = 4;
    public static final int KEEP_SCREEN_ON = 67108864;
    public static final int LAYER_TYPE_HARDWARE = 2;
    public static final int LAYER_TYPE_NONE = 0;
    public static final int LAYER_TYPE_SOFTWARE = 1;
    public static final int LAYOUT_DIRECTION_INHERIT = 2;
    public static final int LAYOUT_DIRECTION_LOCALE = 3;
    public static final int LAYOUT_DIRECTION_LTR = 0;
    public static final int LAYOUT_DIRECTION_RTL = 1;
    public static final int MEASURED_HEIGHT_STATE_SHIFT = 16;
    public static final int MEASURED_SIZE_MASK = 16777215;
    public static final int MEASURED_STATE_MASK = -16777216;
    public static final int MEASURED_STATE_TOO_SMALL = 16777216;
    public static final int NOT_FOCUSABLE = 0;
    public static final int NO_ID = -1;
    public static final int OVER_SCROLL_ALWAYS = 0;
    public static final int OVER_SCROLL_IF_CONTENT_SCROLLS = 1;
    public static final int OVER_SCROLL_NEVER = 2;
    protected static final int[] PRESSED_ENABLED_FOCUSED_SELECTED_STATE_SET = new int[0];
    protected static final int[] PRESSED_ENABLED_FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET = new int[0];
    protected static final int[] PRESSED_ENABLED_FOCUSED_STATE_SET = new int[0];
    protected static final int[] PRESSED_ENABLED_FOCUSED_WINDOW_FOCUSED_STATE_SET = new int[0];
    protected static final int[] PRESSED_ENABLED_SELECTED_STATE_SET = new int[0];
    protected static final int[] PRESSED_ENABLED_SELECTED_WINDOW_FOCUSED_STATE_SET = new int[0];
    protected static final int[] PRESSED_ENABLED_STATE_SET = new int[0];
    protected static final int[] PRESSED_ENABLED_WINDOW_FOCUSED_STATE_SET = new int[0];
    protected static final int[] PRESSED_FOCUSED_SELECTED_STATE_SET = new int[0];
    protected static final int[] PRESSED_FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET = new int[0];
    protected static final int[] PRESSED_FOCUSED_STATE_SET = new int[0];
    protected static final int[] PRESSED_FOCUSED_WINDOW_FOCUSED_STATE_SET = new int[0];
    protected static final int[] PRESSED_SELECTED_STATE_SET = new int[0];
    protected static final int[] PRESSED_SELECTED_WINDOW_FOCUSED_STATE_SET = new int[0];
    protected static final int[] PRESSED_STATE_SET = new int[0];
    protected static final int[] PRESSED_WINDOW_FOCUSED_STATE_SET = new int[0];
    public static final Property<View, Float> ROTATION = null;
    public static final Property<View, Float> ROTATION_X = null;
    public static final Property<View, Float> ROTATION_Y = null;
    public static final Property<View, Float> SCALE_X = null;
    public static final Property<View, Float> SCALE_Y = null;
    public static final int SCREEN_STATE_OFF = 0;
    public static final int SCREEN_STATE_ON = 1;
    public static final int SCROLLBARS_INSIDE_INSET = 16777216;
    public static final int SCROLLBARS_INSIDE_OVERLAY = 0;
    public static final int SCROLLBARS_OUTSIDE_INSET = 50331648;
    public static final int SCROLLBARS_OUTSIDE_OVERLAY = 33554432;
    public static final int SCROLLBAR_POSITION_DEFAULT = 0;
    public static final int SCROLLBAR_POSITION_LEFT = 1;
    public static final int SCROLLBAR_POSITION_RIGHT = 2;
    public static final int SCROLL_AXIS_HORIZONTAL = 1;
    public static final int SCROLL_AXIS_NONE = 0;
    public static final int SCROLL_AXIS_VERTICAL = 2;
    public static final int SCROLL_INDICATOR_BOTTOM = 2;
    public static final int SCROLL_INDICATOR_END = 32;
    public static final int SCROLL_INDICATOR_LEFT = 4;
    public static final int SCROLL_INDICATOR_RIGHT = 8;
    public static final int SCROLL_INDICATOR_START = 16;
    public static final int SCROLL_INDICATOR_TOP = 1;
    protected static final int[] SELECTED_STATE_SET = new int[0];
    protected static final int[] SELECTED_WINDOW_FOCUSED_STATE_SET = new int[0];
    public static final int SOUND_EFFECTS_ENABLED = 134217728;
    /** @deprecated */
    @Deprecated
    public static final int STATUS_BAR_HIDDEN = 1;
    /** @deprecated */
    @Deprecated
    public static final int STATUS_BAR_VISIBLE = 0;
    /** @deprecated */
    @Deprecated
    public static final int SYSTEM_UI_FLAG_FULLSCREEN = 4;
    /** @deprecated */
    @Deprecated
    public static final int SYSTEM_UI_FLAG_HIDE_NAVIGATION = 2;
    /** @deprecated */
    @Deprecated
    public static final int SYSTEM_UI_FLAG_IMMERSIVE = 2048;
    /** @deprecated */
    @Deprecated
    public static final int SYSTEM_UI_FLAG_IMMERSIVE_STICKY = 4096;
    /** @deprecated */
    @Deprecated
    public static final int SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN = 1024;
    /** @deprecated */
    @Deprecated
    public static final int SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION = 512;
    /** @deprecated */
    @Deprecated
    public static final int SYSTEM_UI_FLAG_LAYOUT_STABLE = 256;
    /** @deprecated */
    @Deprecated
    public static final int SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR = 16;
    /** @deprecated */
    @Deprecated
    public static final int SYSTEM_UI_FLAG_LIGHT_STATUS_BAR = 8192;
    /** @deprecated */
    @Deprecated
    public static final int SYSTEM_UI_FLAG_LOW_PROFILE = 1;
    /** @deprecated */
    @Deprecated
    public static final int SYSTEM_UI_FLAG_VISIBLE = 0;
    /** @deprecated */
    @Deprecated
    public static final int SYSTEM_UI_LAYOUT_FLAGS = 1536;
    public static final int TEXT_ALIGNMENT_CENTER = 4;
    public static final int TEXT_ALIGNMENT_GRAVITY = 1;
    public static final int TEXT_ALIGNMENT_INHERIT = 0;
    public static final int TEXT_ALIGNMENT_TEXT_END = 3;
    public static final int TEXT_ALIGNMENT_TEXT_START = 2;
    public static final int TEXT_ALIGNMENT_VIEW_END = 6;
    public static final int TEXT_ALIGNMENT_VIEW_START = 5;
    public static final int TEXT_DIRECTION_ANY_RTL = 2;
    public static final int TEXT_DIRECTION_FIRST_STRONG = 1;
    public static final int TEXT_DIRECTION_FIRST_STRONG_LTR = 6;
    public static final int TEXT_DIRECTION_FIRST_STRONG_RTL = 7;
    public static final int TEXT_DIRECTION_INHERIT = 0;
    public static final int TEXT_DIRECTION_LOCALE = 5;
    public static final int TEXT_DIRECTION_LTR = 3;
    public static final int TEXT_DIRECTION_RTL = 4;
    public static final Property<View, Float> TRANSLATION_X = null;
    public static final Property<View, Float> TRANSLATION_Y = null;
    public static final Property<View, Float> TRANSLATION_Z = null;
    protected static final String VIEW_LOG_TAG = "View";
    public static final int VISIBLE = 0;
    protected static final int[] WINDOW_FOCUSED_STATE_SET = new int[0];
    public static final Property<View, Float> X = null;
    public static final Property<View, Float> Y = null;
    public static final Property<View, Float> Z = null;

    public View(Context context) {
        mContext = context;
    }

    public View(Context context, @Nullable AttributeSet attrs) {
        mContext = context;
    }

    public View(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        mContext = context;
    }

    public View(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mContext = context;
    }

    @NonNull
    public int[] getAttributeResolutionStack(int attribute) {
        return new int[0];
    }

    @NonNull
    public Map<Integer, Integer> getAttributeSourceResourceMap() {
        return null;
    }

    public int getExplicitStyle() {
        return 0;
    }

    public final boolean isShowingLayoutBounds() {
        return false;
    }

    public final void saveAttributeDataForStyleable(@NonNull Context context, @NonNull int[] styleable, @Nullable AttributeSet attrs, @NonNull TypedArray t, int defStyleAttr, int defStyleRes) {
    }

    public String toString() {
        return null;
    }

    public int getVerticalFadingEdgeLength() {
        return 0;
    }

    public void setFadingEdgeLength(int length) {
    }

    public int getHorizontalFadingEdgeLength() {
        return 0;
    }

    public int getVerticalScrollbarWidth() {
        return 0;
    }

    protected int getHorizontalScrollbarHeight() {
        return 0;
    }

    public void setVerticalScrollbarThumbDrawable(@Nullable Drawable drawable) {
    }

    public void setVerticalScrollbarTrackDrawable(@Nullable Drawable drawable) {
    }

    public void setHorizontalScrollbarThumbDrawable(@Nullable Drawable drawable) {
    }

    public void setHorizontalScrollbarTrackDrawable(@Nullable Drawable drawable) {
    }

    @Nullable
    public Drawable getVerticalScrollbarThumbDrawable() {
        return null;
    }

    @Nullable
    public Drawable getVerticalScrollbarTrackDrawable() {
        return null;
    }

    @Nullable
    public Drawable getHorizontalScrollbarThumbDrawable() {
        return null;
    }

    @Nullable
    public Drawable getHorizontalScrollbarTrackDrawable() {
        return null;
    }

    public void setVerticalScrollbarPosition(int position) {
    }

    public int getVerticalScrollbarPosition() {
        return 0;
    }

    public void setScrollIndicators(int indicators) {
    }

    public void setScrollIndicators(int indicators, int mask) {
    }

    public int getScrollIndicators() {
        return 0;
    }

    public void setOnScrollChangeListener(OnScrollChangeListener l) {
    }

    public void setOnFocusChangeListener(OnFocusChangeListener l) {
    }

    public void addOnLayoutChangeListener(OnLayoutChangeListener listener) {
    }

    public void removeOnLayoutChangeListener(OnLayoutChangeListener listener) {
    }

    public void addOnAttachStateChangeListener(OnAttachStateChangeListener listener) {
    }

    public void removeOnAttachStateChangeListener(OnAttachStateChangeListener listener) {
    }

    public OnFocusChangeListener getOnFocusChangeListener() {
        return null;
    }

    public void setOnClickListener(@Nullable OnClickListener l) {
    }

    public boolean hasOnClickListeners() {
        return false;
    }

    public void setOnLongClickListener(@Nullable OnLongClickListener l) {
    }

    public boolean hasOnLongClickListeners() {
        return false;
    }

    public void setOnContextClickListener(@Nullable OnContextClickListener l) {
    }

    public void setOnCreateContextMenuListener(OnCreateContextMenuListener l) {
    }

    public boolean performClick() {
        return false;
    }

    public boolean callOnClick() {
        return false;
    }

    public boolean performLongClick() {
        return false;
    }

    public boolean performLongClick(float x, float y) {
        return false;
    }

    public boolean performContextClick(float x, float y) {
        return false;
    }

    public boolean performContextClick() {
        return false;
    }

    public boolean showContextMenu() {
        return false;
    }

    public boolean showContextMenu(float x, float y) {
        return false;
    }

    public ActionMode startActionMode(ActionMode.Callback callback) {
        return null;
    }

    public ActionMode startActionMode(ActionMode.Callback callback, int type) {
        return null;
    }

    public void setOnKeyListener(OnKeyListener l) {
    }

    public void setOnTouchListener(OnTouchListener l) {
    }

    public void setOnGenericMotionListener(OnGenericMotionListener l) {
    }

    public void setOnHoverListener(OnHoverListener l) {
    }

    public void setOnDragListener(OnDragListener l) {
    }

    public final void setRevealOnFocusHint(boolean revealOnFocus) {
    }

    public final boolean getRevealOnFocusHint() {
        return false;
    }

    public boolean requestRectangleOnScreen(Rect rectangle) {
        return false;
    }

    public boolean requestRectangleOnScreen(Rect rectangle, boolean immediate) {
        return false;
    }

    public void clearFocus() {
    }

    @ExportedProperty(
            category = "focus"
    )
    public boolean hasFocus() {
        return false;
    }

    public boolean hasFocusable() {
        return false;
    }

    public boolean hasExplicitFocusable() {
        return false;
    }

    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
    }

    public void setAccessibilityPaneTitle(@Nullable CharSequence accessibilityPaneTitle) {
    }

    @Nullable
    public CharSequence getAccessibilityPaneTitle() {
        return null;
    }

    public void sendAccessibilityEvent(int eventType) {
    }

    public void announceForAccessibility(CharSequence text) {
    }

    public void sendAccessibilityEventUnchecked(AccessibilityEvent event) {
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return false;
    }

    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
    }

    public AccessibilityNodeInfo createAccessibilityNodeInfo() {
        return null;
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
    }

    public CharSequence getAccessibilityClassName() {
        return null;
    }

    public void onProvideStructure(ViewStructure structure) {
    }

    public void onProvideAutofillStructure(ViewStructure structure, int flags) {
    }

    public void onProvideContentCaptureStructure(@NonNull ViewStructure structure, int flags) {
    }

    public void onProvideVirtualStructure(ViewStructure structure) {
    }

    public void onProvideAutofillVirtualStructure(ViewStructure structure, int flags) {
    }

    public void autofill(AutofillValue value) {
    }

    public void autofill(@NonNull SparseArray<AutofillValue> values) {
    }

    public final AutofillId getAutofillId() {
        return null;
    }

    public void setAutofillId(@Nullable AutofillId id) {
    }

    public int getAutofillType() {
        return 0;
    }

    @ExportedProperty
    @Nullable
    public String[] getAutofillHints() {
        return null;
    }

    @Nullable
    public AutofillValue getAutofillValue() {
        return null;
    }

    @ExportedProperty(
            mapping = {@IntToString(
                    from = 0,
                    to = "auto"
            ), @IntToString(
                    from = 1,
                    to = "yes"
            ), @IntToString(
                    from = 2,
                    to = "no"
            ), @IntToString(
                    from = 4,
                    to = "yesExcludeDescendants"
            ), @IntToString(
                    from = 8,
                    to = "noExcludeDescendants"
            )}
    )
    public int getImportantForAutofill() {
        return 0;
    }

    public void setImportantForAutofill(int mode) {
    }

    public final boolean isImportantForAutofill() {
        return false;
    }

    @ExportedProperty(
            mapping = {@IntToString(
                    from = 0,
                    to = "auto"
            ), @IntToString(
                    from = 1,
                    to = "yes"
            ), @IntToString(
                    from = 2,
                    to = "no"
            ), @IntToString(
                    from = 4,
                    to = "yesExcludeDescendants"
            ), @IntToString(
                    from = 8,
                    to = "noExcludeDescendants"
            )}
    )
    public int getImportantForContentCapture() {
        return 0;
    }

    public void setImportantForContentCapture(int mode) {
    }

    public final boolean isImportantForContentCapture() {
        return false;
    }

    public void setContentCaptureSession(@Nullable ContentCaptureSession contentCaptureSession) {
    }

    @Nullable
    public final ContentCaptureSession getContentCaptureSession() {
        return null;
    }

    public void dispatchProvideStructure(ViewStructure structure) {
    }

    public void dispatchProvideAutofillStructure(@NonNull ViewStructure structure, int flags) {
    }

    public void addExtraDataToAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info, @NonNull String extraDataKey, @Nullable Bundle arguments) {
    }

    public boolean isVisibleToUserForAutofill(int virtualId) {
        return false;
    }

    public AccessibilityDelegate getAccessibilityDelegate() {
        return null;
    }

    public void setAccessibilityDelegate(@Nullable AccessibilityDelegate delegate) {
    }

    public AccessibilityNodeProvider getAccessibilityNodeProvider() {
        return null;
    }

    @ExportedProperty(
            category = "accessibility"
    )
    @Nullable
    public final CharSequence getStateDescription() {
        return null;
    }

    @ExportedProperty(
            category = "accessibility"
    )
    public CharSequence getContentDescription() {
        return null;
    }

    public void setStateDescription(@Nullable CharSequence stateDescription) {
    }

    public void setContentDescription(CharSequence contentDescription) {
    }

    public void setAccessibilityTraversalBefore(int beforeId) {
    }

    public int getAccessibilityTraversalBefore() {
        return 0;
    }

    public void setAccessibilityTraversalAfter(int afterId) {
    }

    public int getAccessibilityTraversalAfter() {
        return 0;
    }

    @ExportedProperty(
            category = "accessibility"
    )
    public int getLabelFor() {
        return 0;
    }

    public void setLabelFor(int id) {
    }

    @ExportedProperty(
            category = "focus"
    )
    public boolean isFocused() {
        return false;
    }

    public View findFocus() {
        return null;
    }

    public boolean isScrollContainer() {
        return false;
    }

    public void setScrollContainer(boolean isScrollContainer) {
    }

    /** @deprecated */
    @Deprecated
    public int getDrawingCacheQuality() {
        return 0;
    }

    /** @deprecated */
    @Deprecated
    public void setDrawingCacheQuality(int quality) {
    }

    public boolean getKeepScreenOn() {
        return false;
    }

    public void setKeepScreenOn(boolean keepScreenOn) {
    }

    public int getNextFocusLeftId() {
        return 0;
    }

    public void setNextFocusLeftId(int nextFocusLeftId) {
    }

    public int getNextFocusRightId() {
        return 0;
    }

    public void setNextFocusRightId(int nextFocusRightId) {
    }

    public int getNextFocusUpId() {
        return 0;
    }

    public void setNextFocusUpId(int nextFocusUpId) {
    }

    public int getNextFocusDownId() {
        return 0;
    }

    public void setNextFocusDownId(int nextFocusDownId) {
    }

    public int getNextFocusForwardId() {
        return 0;
    }

    public void setNextFocusForwardId(int nextFocusForwardId) {
    }

    public int getNextClusterForwardId() {
        return 0;
    }

    public void setNextClusterForwardId(int nextClusterForwardId) {
    }

    public boolean isShown() {
        return false;
    }

    /** @deprecated */
    @Deprecated
    protected boolean fitSystemWindows(Rect insets) {
        return false;
    }

    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        return null;
    }

    public void setOnApplyWindowInsetsListener(OnApplyWindowInsetsListener listener) {
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
        return null;
    }

    @NonNull
    public WindowInsets dispatchWindowInsetsAnimationProgress(@NonNull WindowInsets insets, @NonNull List<WindowInsetsAnimation> runningAnimations) {
        return null;
    }

    public void dispatchWindowInsetsAnimationEnd(@NonNull WindowInsetsAnimation animation) {
    }

    public void setSystemGestureExclusionRects(@NonNull List<Rect> rects) {
    }

    @NonNull
    public List<Rect> getSystemGestureExclusionRects() {
        return mSystemGestureExclusionRects;
    }

    public void getLocationInSurface(@NonNull int[] location) {
    }

    public WindowInsets getRootWindowInsets() {
        return null;
    }

    @Nullable
    public WindowInsetsController getWindowInsetsController() {
        return null;
    }

    public WindowInsets computeSystemWindowInsets(WindowInsets in, Rect outLocalInsets) {
        return null;
    }

    public void setFitsSystemWindows(boolean fitSystemWindows) {
    }

    @ExportedProperty
    public boolean getFitsSystemWindows() {
        return false;
    }

    /** @deprecated */
    @Deprecated
    public void requestFitSystemWindows() {
    }

    public void requestApplyInsets() {
    }

    @ExportedProperty(
            mapping = {@IntToString(
                    from = 0,
                    to = "VISIBLE"
            ), @IntToString(
                    from = 4,
                    to = "INVISIBLE"
            ), @IntToString(
                    from = 8,
                    to = "GONE"
            )}
    )
    public int getVisibility() {
        return 0;
    }

    public void setVisibility(int visibility) {
    }

    @ExportedProperty
    public boolean isEnabled() {
        return false;
    }

    public void setEnabled(boolean enabled) {
    }

    public void setFocusable(boolean focusable) {
    }

    public void setFocusable(int focusable) {
    }

    public void setFocusableInTouchMode(boolean focusableInTouchMode) {
    }

    public void setAutofillHints(@Nullable String... autofillHints) {
    }

    public void setSoundEffectsEnabled(boolean soundEffectsEnabled) {
    }

    @ExportedProperty
    public boolean isSoundEffectsEnabled() {
        return false;
    }

    public void setHapticFeedbackEnabled(boolean hapticFeedbackEnabled) {
    }

    @ExportedProperty
    public boolean isHapticFeedbackEnabled() {
        return false;
    }

    public void setLayoutDirection(int layoutDirection) {
    }

    @ExportedProperty(
            category = "layout",
            mapping = {@IntToString(
                    from = 0,
                    to = "RESOLVED_DIRECTION_LTR"
            ), @IntToString(
                    from = 1,
                    to = "RESOLVED_DIRECTION_RTL"
            )}
    )
    public int getLayoutDirection() {
        return 0;
    }

    @ExportedProperty(
            category = "layout"
    )
    public boolean hasTransientState() {
        return false;
    }

    public void setHasTransientState(boolean hasTransientState) {
    }

    public boolean isAttachedToWindow() {
        return false;
    }

    public boolean isLaidOut() {
        return false;
    }

    public void setWillNotDraw(boolean willNotDraw) {
    }

    @ExportedProperty(
            category = "drawing"
    )
    public boolean willNotDraw() {
        return false;
    }

    /** @deprecated */
    @Deprecated
    public void setWillNotCacheDrawing(boolean willNotCacheDrawing) {
    }

    /** @deprecated */
    @Deprecated
    @ExportedProperty(
            category = "drawing"
    )
    public boolean willNotCacheDrawing() {
        return false;
    }

    @ExportedProperty
    public boolean isClickable() {
        return false;
    }

    public void setClickable(boolean clickable) {
    }

    public boolean isLongClickable() {
        return false;
    }

    public void setLongClickable(boolean longClickable) {
    }

    public boolean isContextClickable() {
        return false;
    }

    public void setContextClickable(boolean contextClickable) {
    }

    public void setPressed(boolean pressed) {
    }

    protected void dispatchSetPressed(boolean pressed) {
    }

    @ExportedProperty
    public boolean isPressed() {
        return false;
    }

    public boolean isSaveEnabled() {
        return false;
    }

    public void setSaveEnabled(boolean enabled) {
    }

    @ExportedProperty
    public boolean getFilterTouchesWhenObscured() {
        return false;
    }

    public void setFilterTouchesWhenObscured(boolean enabled) {
    }

    public boolean isSaveFromParentEnabled() {
        return false;
    }

    public void setSaveFromParentEnabled(boolean enabled) {
    }

    @ExportedProperty(
            category = "focus"
    )
    public final boolean isFocusable() {
        return false;
    }

    @ExportedProperty(
            mapping = {@IntToString(
                    from = 0,
                    to = "NOT_FOCUSABLE"
            ), @IntToString(
                    from = 1,
                    to = "FOCUSABLE"
            ), @IntToString(
                    from = 16,
                    to = "FOCUSABLE_AUTO"
            )},
            category = "focus"
    )
    public int getFocusable() {
        return 0;
    }

    @ExportedProperty(
            category = "focus"
    )
    public final boolean isFocusableInTouchMode() {
        return false;
    }

    public boolean isScreenReaderFocusable() {
        return false;
    }

    public void setScreenReaderFocusable(boolean screenReaderFocusable) {
    }

    public boolean isAccessibilityHeading() {
        return false;
    }

    public void setAccessibilityHeading(boolean isHeading) {
    }

    public View focusSearch(int direction) {
        return null;
    }

    @ExportedProperty(
            category = "focus"
    )
    public final boolean isKeyboardNavigationCluster() {
        return false;
    }

    public void setKeyboardNavigationCluster(boolean isCluster) {
    }

    @ExportedProperty(
            category = "focus"
    )
    public final boolean isFocusedByDefault() {
        return false;
    }

    public void setFocusedByDefault(boolean isFocusedByDefault) {
    }

    public View keyboardNavigationClusterSearch(View currentCluster, int direction) {
        return null;
    }

    public boolean dispatchUnhandledMove(View focused, int direction) {
        return false;
    }

    public void setDefaultFocusHighlightEnabled(boolean defaultFocusHighlightEnabled) {
    }

    @ExportedProperty(
            category = "focus"
    )
    public final boolean getDefaultFocusHighlightEnabled() {
        return false;
    }

    public ArrayList<View> getFocusables(int direction) {
        return null;
    }

    public void addFocusables(ArrayList<View> views, int direction) {
    }

    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
    }

    public void addKeyboardNavigationClusters(@NonNull Collection<View> views, int direction) {
    }

    public void findViewsWithText(ArrayList<View> outViews, CharSequence searched, int flags) {
    }

    public ArrayList<View> getTouchables() {
        return null;
    }

    public void addTouchables(ArrayList<View> views) {
    }

    public boolean isAccessibilityFocused() {
        return false;
    }

    public final boolean requestFocus() {
        return false;
    }

    public boolean restoreDefaultFocus() {
        return false;
    }

    public final boolean requestFocus(int direction) {
        return false;
    }

    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        return false;
    }

    public final boolean requestFocusFromTouch() {
        return false;
    }

    @ExportedProperty(
            category = "accessibility",
            mapping = {@IntToString(
                    from = 0,
                    to = "auto"
            ), @IntToString(
                    from = 1,
                    to = "yes"
            ), @IntToString(
                    from = 2,
                    to = "no"
            ), @IntToString(
                    from = 4,
                    to = "noHideDescendants"
            )}
    )
    public int getImportantForAccessibility() {
        return 0;
    }

    public void setAccessibilityLiveRegion(int mode) {
    }

    public int getAccessibilityLiveRegion() {
        return 0;
    }

    public void setImportantForAccessibility(int mode) {
    }

    public boolean isImportantForAccessibility() {
        return false;
    }

    public ViewParent getParentForAccessibility() {
        return null;
    }

    public void addChildrenForAccessibility(ArrayList<View> outChildren) {
    }

    public void setTransitionVisibility(int visibility) {
    }

    public boolean dispatchNestedPrePerformAccessibilityAction(int action, Bundle arguments) {
        return false;
    }

    public boolean performAccessibilityAction(int action, Bundle arguments) {
        return false;
    }

    public final boolean isTemporarilyDetached() {
        return false;
    }

    public void dispatchStartTemporaryDetach() {
    }

    public void onStartTemporaryDetach() {
    }

    public void dispatchFinishTemporaryDetach() {
    }

    public void onFinishTemporaryDetach() {
    }

    public KeyEvent.DispatcherState getKeyDispatcherState() {
        return null;
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

    public boolean dispatchTouchEvent(MotionEvent event) {
        return false;
    }

    public boolean onFilterTouchEventForSecurity(MotionEvent event) {
        return false;
    }

    public boolean dispatchTrackballEvent(MotionEvent event) {
        return false;
    }

    public boolean dispatchCapturedPointerEvent(MotionEvent event) {
        return false;
    }

    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        return false;
    }

    protected boolean dispatchHoverEvent(MotionEvent event) {
        return false;
    }

    protected boolean dispatchGenericPointerEvent(MotionEvent event) {
        return false;
    }

    protected boolean dispatchGenericFocusedEvent(MotionEvent event) {
        return false;
    }

    public void dispatchWindowFocusChanged(boolean hasFocus) {
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
    }

    public boolean hasWindowFocus() {
        return false;
    }

    protected void dispatchVisibilityChanged(@NonNull View changedView, int visibility) {
    }

    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
    }

    public void dispatchDisplayHint(int hint) {
    }

    protected void onDisplayHint(int hint) {
    }

    public void dispatchWindowVisibilityChanged(int visibility) {
    }

    protected void onWindowVisibilityChanged(int visibility) {
    }

    public void onVisibilityAggregated(boolean isVisible) {
    }

    public int getWindowVisibility() {
        return 0;
    }

    public void getWindowVisibleDisplayFrame(Rect outRect) {
    }

    public void dispatchConfigurationChanged(Configuration newConfig) {
    }

    protected void onConfigurationChanged(Configuration newConfig) {
    }

    @ExportedProperty
    public boolean isInTouchMode() {
        return false;
    }

    @CapturedViewProperty
    public final Context getContext() {
        return null;
    }

    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return false;
    }

    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean onCheckIsTextEditor() {
        return false;
    }

    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return null;
    }

    public boolean checkInputConnectionProxy(View view) {
        return false;
    }

    public void createContextMenu(ContextMenu menu) {
    }

    protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
        return null;
    }

    protected void onCreateContextMenu(ContextMenu menu) {
    }

    public boolean onTrackballEvent(MotionEvent event) {
        return false;
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        return false;
    }

    public boolean onHoverEvent(MotionEvent event) {
        return false;
    }

    @ExportedProperty
    public boolean isHovered() {
        return false;
    }

    public void setHovered(boolean hovered) {
    }

    public void onHoverChanged(boolean hovered) {
    }

    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    public void cancelLongPress() {
    }

    public void setTouchDelegate(TouchDelegate delegate) {
    }

    public TouchDelegate getTouchDelegate() {
        return null;
    }

    public final void requestUnbufferedDispatch(MotionEvent event) {
    }

    public final void requestUnbufferedDispatch(int source) {
    }

    public void bringToFront() {
    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    }

    protected void dispatchDraw(Canvas canvas) {
    }

    public final ViewParent getParent() {
        return null;
    }

    public void setScrollX(int value) {
    }

    public void setScrollY(int value) {
    }

    public final int getScrollX() {
        return 0;
    }

    public final int getScrollY() {
        return 0;
    }

    @ExportedProperty(
            category = "layout"
    )
    public final int getWidth() {
        return 0;
    }

    @ExportedProperty(
            category = "layout"
    )
    public final int getHeight() {
        return 0;
    }

    public void getDrawingRect(Rect outRect) {
    }

    public final int getMeasuredWidth() {
        return 0;
    }

    @ExportedProperty(
            category = "measurement",
            flagMapping = {@FlagToString(
                    mask = -16777216,
                    equals = 16777216,
                    name = "MEASURED_STATE_TOO_SMALL"
            )}
    )
    public final int getMeasuredWidthAndState() {
        return 0;
    }

    public final int getMeasuredHeight() {
        return 0;
    }

    @ExportedProperty(
            category = "measurement",
            flagMapping = {@FlagToString(
                    mask = -16777216,
                    equals = 16777216,
                    name = "MEASURED_STATE_TOO_SMALL"
            )}
    )
    public final int getMeasuredHeightAndState() {
        return 0;
    }

    public final int getMeasuredState() {
        return 0;
    }

    public Matrix getMatrix() {
        return null;
    }

    public float getCameraDistance() {
        return 0f;
    }

    public void setCameraDistance(float distance) {
    }

    @ExportedProperty(
            category = "drawing"
    )
    public float getRotation() {
        return 0f;
    }

    public void setRotation(float rotation) {
    }

    @ExportedProperty(
            category = "drawing"
    )
    public float getRotationY() {
        return 0f;
    }

    public void setRotationY(float rotationY) {
    }

    @ExportedProperty(
            category = "drawing"
    )
    public float getRotationX() {
        return 0f;
    }

    public void setRotationX(float rotationX) {
    }

    @ExportedProperty(
            category = "drawing"
    )
    public float getScaleX() {
        return 0f;
    }

    public void setScaleX(float scaleX) {
    }

    @ExportedProperty(
            category = "drawing"
    )
    public float getScaleY() {
        return 0f;
    }

    public void setScaleY(float scaleY) {
    }

    @ExportedProperty(
            category = "drawing"
    )
    public float getPivotX() {
        return 0f;
    }

    public void setPivotX(float pivotX) {
    }

    @ExportedProperty(
            category = "drawing"
    )
    public float getPivotY() {
        return 0f;
    }

    public void setPivotY(float pivotY) {
    }

    public boolean isPivotSet() {
        return false;
    }

    public void resetPivot() {
    }

    @ExportedProperty(
            category = "drawing"
    )
    public float getAlpha() {
        return 0f;
    }

    public void forceHasOverlappingRendering(boolean hasOverlappingRendering) {
    }

    public final boolean getHasOverlappingRendering() {
        return false;
    }

    @ExportedProperty(
            category = "drawing"
    )
    public boolean hasOverlappingRendering() {
        return false;
    }

    public void setAlpha(float alpha) {
    }

    public void setTransitionAlpha(float alpha) {
    }

    @ExportedProperty(
            category = "drawing"
    )
    public float getTransitionAlpha() {
        return 0f;
    }

    public void setForceDarkAllowed(boolean allow) {
    }

    @ExportedProperty(
            category = "drawing"
    )
    public boolean isForceDarkAllowed() {
        return false;
    }

    @CapturedViewProperty
    public final int getTop() {
        return 0;
    }

    public final void setTop(int top) {
    }

    @CapturedViewProperty
    public final int getBottom() {
        return 0;
    }

    public boolean isDirty() {
        return false;
    }

    public final void setBottom(int bottom) {
    }

    @CapturedViewProperty
    public final int getLeft() {
        return 0;
    }

    public final void setLeft(int left) {
    }

    @CapturedViewProperty
    public final int getRight() {
        return 0;
    }

    public final void setRight(int right) {
    }

    @ExportedProperty(
            category = "drawing"
    )
    public float getX() {
        return 0f;
    }

    public void setX(float x) {
    }

    @ExportedProperty(
            category = "drawing"
    )
    public float getY() {
        return 0f;
    }

    public void setY(float y) {
    }

    @ExportedProperty(
            category = "drawing"
    )
    public float getZ() {
        return 0f;
    }

    public void setZ(float z) {
    }

    @ExportedProperty(
            category = "drawing"
    )
    public float getElevation() {
        return 0f;
    }

    public void setElevation(float elevation) {
    }

    @ExportedProperty(
            category = "drawing"
    )
    public float getTranslationX() {
        return 0f;
    }

    public void setTranslationX(float translationX) {
    }

    @ExportedProperty(
            category = "drawing"
    )
    public float getTranslationY() {
        return 0f;
    }

    public void setTranslationY(float translationY) {
    }

    @ExportedProperty(
            category = "drawing"
    )
    public float getTranslationZ() {
        return 0f;
    }

    public void setTranslationZ(float translationZ) {
    }

    public void setAnimationMatrix(@Nullable Matrix matrix) {
    }

    @Nullable
    public Matrix getAnimationMatrix() {
        return null;
    }

    public StateListAnimator getStateListAnimator() {
        return null;
    }

    public void setStateListAnimator(StateListAnimator stateListAnimator) {
    }

    public final boolean getClipToOutline() {
        return false;
    }

    public void setClipToOutline(boolean clipToOutline) {
    }

    public void setOutlineProvider(ViewOutlineProvider provider) {
    }

    public ViewOutlineProvider getOutlineProvider() {
        return null;
    }

    public void invalidateOutline() {
    }

    public void setOutlineSpotShadowColor(int color) {
    }

    public int getOutlineSpotShadowColor() {
        return 0;
    }

    public void setOutlineAmbientShadowColor(int color) {
    }

    public int getOutlineAmbientShadowColor() {
        return 0;
    }

    public void getHitRect(Rect outRect) {
    }

    public void getFocusedRect(Rect r) {
    }

    public boolean getGlobalVisibleRect(Rect r, Point globalOffset) {
        return false;
    }

    public final boolean getGlobalVisibleRect(Rect r) {
        return false;
    }

    public final boolean getLocalVisibleRect(Rect r) {
        return false;
    }

    public void offsetTopAndBottom(int offset) {
    }

    public void offsetLeftAndRight(int offset) {
    }

    @ExportedProperty(
            deepExport = true,
            prefix = "layout_"
    )
    public ViewGroup.LayoutParams getLayoutParams() {
        return null;
    }

    public void setLayoutParams(ViewGroup.LayoutParams params) {
    }

    public void scrollTo(int x, int y) {
    }

    public void scrollBy(int x, int y) {
    }

    protected boolean awakenScrollBars() {
        return false;
    }

    protected boolean awakenScrollBars(int startDelay) {
        return false;
    }

    protected boolean awakenScrollBars(int startDelay, boolean invalidate) {
        return false;
    }

    /** @deprecated */
    @Deprecated
    public void invalidate(Rect dirty) {
    }

    /** @deprecated */
    @Deprecated
    public void invalidate(int l, int t, int r, int b) {
    }

    public void invalidate() {
    }

    @ExportedProperty(
            category = "drawing"
    )
    public boolean isOpaque() {
        return false;
    }

    public Handler getHandler() {
        return null;
    }

    public boolean post(Runnable action) {
        return false;
    }

    public boolean postDelayed(Runnable action, long delayMillis) {
        return false;
    }

    public void postOnAnimation(Runnable action) {
    }

    public void postOnAnimationDelayed(Runnable action, long delayMillis) {
    }

    public boolean removeCallbacks(Runnable action) {
        return false;
    }

    public void postInvalidate() {
    }

    public void postInvalidate(int left, int top, int right, int bottom) {
    }

    public void postInvalidateDelayed(long delayMilliseconds) {
    }

    public void postInvalidateDelayed(long delayMilliseconds, int left, int top, int right, int bottom) {
    }

    public void postInvalidateOnAnimation() {
    }

    public void postInvalidateOnAnimation(int left, int top, int right, int bottom) {
    }

    public void computeScroll() {
    }

    public boolean isHorizontalFadingEdgeEnabled() {
        return false;
    }

    public void setHorizontalFadingEdgeEnabled(boolean horizontalFadingEdgeEnabled) {
    }

    public boolean isVerticalFadingEdgeEnabled() {
        return false;
    }

    public void setVerticalFadingEdgeEnabled(boolean verticalFadingEdgeEnabled) {
    }

    protected float getTopFadingEdgeStrength() {
        return 0f;
    }

    protected float getBottomFadingEdgeStrength() {
        return 0f;
    }

    protected float getLeftFadingEdgeStrength() {
        return 0f;
    }

    protected float getRightFadingEdgeStrength() {
        return 0f;
    }

    public boolean isHorizontalScrollBarEnabled() {
        return false;
    }

    public void setHorizontalScrollBarEnabled(boolean horizontalScrollBarEnabled) {
    }

    public boolean isVerticalScrollBarEnabled() {
        return false;
    }

    public void setVerticalScrollBarEnabled(boolean verticalScrollBarEnabled) {
    }

    public void setScrollbarFadingEnabled(boolean fadeScrollbars) {
    }

    public boolean isScrollbarFadingEnabled() {
        return false;
    }

    public int getScrollBarDefaultDelayBeforeFade() {
        return 0;
    }

    public void setScrollBarDefaultDelayBeforeFade(int scrollBarDefaultDelayBeforeFade) {
    }

    public int getScrollBarFadeDuration() {
        return 0;
    }

    public void setScrollBarFadeDuration(int scrollBarFadeDuration) {
    }

    public int getScrollBarSize() {
        return 0;
    }

    public void setScrollBarSize(int scrollBarSize) {
    }

    public void setScrollBarStyle(int style) {
    }

    @ExportedProperty(
            mapping = {@IntToString(
                    from = 0,
                    to = "INSIDE_OVERLAY"
            ), @IntToString(
                    from = 16777216,
                    to = "INSIDE_INSET"
            ), @IntToString(
                    from = 33554432,
                    to = "OUTSIDE_OVERLAY"
            ), @IntToString(
                    from = 50331648,
                    to = "OUTSIDE_INSET"
            )}
    )
    public int getScrollBarStyle() {
        return 0;
    }

    protected int computeHorizontalScrollRange() {
        return 0;
    }

    protected int computeHorizontalScrollOffset() {
        return 0;
    }

    protected int computeHorizontalScrollExtent() {
        return 0;
    }

    protected int computeVerticalScrollRange() {
        return 0;
    }

    protected int computeVerticalScrollOffset() {
        return 0;
    }

    protected int computeVerticalScrollExtent() {
        return 0;
    }

    public boolean canScrollHorizontally(int direction) {
        return false;
    }

    public boolean canScrollVertically(int direction) {
        return false;
    }

    protected final void onDrawScrollBars(Canvas canvas) {
    }

    protected void onDraw(Canvas canvas) {
    }

    protected void onAttachedToWindow() {
    }

    public void onScreenStateChanged(int screenState) {
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
    }

    public boolean canResolveLayoutDirection() {
        return false;
    }

    public boolean isLayoutDirectionResolved() {
        return false;
    }

    protected void onDetachedFromWindow() {
    }

    protected int getWindowAttachCount() {
        return 0;
    }

    public IBinder getWindowToken() {
        return null;
    }

    public WindowId getWindowId() {
        return null;
    }

    public IBinder getApplicationWindowToken() {
        return null;
    }

    public Display getDisplay() {
        return null;
    }

    public final void cancelPendingInputEvents() {
    }

    public void onCancelPendingInputEvents() {
    }

    public void saveHierarchyState(SparseArray<Parcelable> container) {
    }

    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
    }

    @Nullable
    protected Parcelable onSaveInstanceState() {
        return null;
    }

    public void restoreHierarchyState(SparseArray<Parcelable> container) {
    }

    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
    }

    protected void onRestoreInstanceState(Parcelable state) {
    }

    public long getDrawingTime() {
        return 0;
    }

    public void setDuplicateParentStateEnabled(boolean enabled) {
    }

    public boolean isDuplicateParentStateEnabled() {
        return false;
    }

    public void setLayerType(int layerType, @Nullable Paint paint) {
    }

    public void setLayerPaint(@Nullable Paint paint) {
    }

    public int getLayerType() {
        return 0;
    }

    public void buildLayer() {
    }

    /** @deprecated */
    @Deprecated
    public void setDrawingCacheEnabled(boolean enabled) {
    }

    /** @deprecated */
    @Deprecated
    @ExportedProperty(
            category = "drawing"
    )
    public boolean isDrawingCacheEnabled() {
        return false;
    }

    /** @deprecated */
    @Deprecated
    public Bitmap getDrawingCache() {
        return null;
    }

    /** @deprecated */
    @Deprecated
    public Bitmap getDrawingCache(boolean autoScale) {
        return null;
    }

    /** @deprecated */
    @Deprecated
    public void destroyDrawingCache() {
    }

    /** @deprecated */
    @Deprecated
    public void setDrawingCacheBackgroundColor(int color) {
    }

    /** @deprecated */
    @Deprecated
    public int getDrawingCacheBackgroundColor() {
        return 0;
    }

    /** @deprecated */
    @Deprecated
    public void buildDrawingCache() {
    }

    /** @deprecated */
    @Deprecated
    public void buildDrawingCache(boolean autoScale) {
    }

    public boolean isInEditMode() {
        return false;
    }

    protected boolean isPaddingOffsetRequired() {
        return false;
    }

    protected int getLeftPaddingOffset() {
        return 0;
    }

    protected int getRightPaddingOffset() {
        return 0;
    }

    protected int getTopPaddingOffset() {
        return 0;
    }

    protected int getBottomPaddingOffset() {
        return 0;
    }

    @ExportedProperty(
            category = "drawing"
    )
    public boolean isHardwareAccelerated() {
        return false;
    }

    public void setClipBounds(Rect clipBounds) {
    }

    public Rect getClipBounds() {
        return null;
    }

    public boolean getClipBounds(Rect outRect) {
        return false;
    }

    public void draw(Canvas canvas) {
    }

    public ViewOverlay getOverlay() {
        return null;
    }

    @ExportedProperty(
            category = "drawing"
    )
    public int getSolidColor() {
        return 0;
    }

    public boolean isLayoutRequested() {
        return false;
    }

    public void layout(int l, int t, int r, int b) {
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    }

    public final void setLeftTopRightBottom(int left, int top, int right, int bottom) {
    }

    protected void onFinishInflate() {
    }

    public Resources getResources() {
        return null;
    }

    public void invalidateDrawable(@NonNull Drawable drawable) {
    }

    public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
    }

    public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
    }

    public void unscheduleDrawable(Drawable who) {
    }

    protected boolean verifyDrawable(@NonNull Drawable who) {
        return false;
    }

    protected void drawableStateChanged() {
    }

    public void drawableHotspotChanged(float x, float y) {
    }

    public void dispatchDrawableHotspotChanged(float x, float y) {
    }

    public void refreshDrawableState() {
    }

    public final int[] getDrawableState() {
        return new int[]{};
    }

    protected int[] onCreateDrawableState(int extraSpace) {
        return new int[0];
    }

    protected static int[] mergeDrawableStates(int[] baseState, int[] additionalState) {
        return new int[0];
    }

    public void jumpDrawablesToCurrentState() {
    }

    public void setBackgroundColor(int color) {
    }

    public void setBackgroundResource(int resid) {
    }

    public void setBackground(Drawable background) {
    }

    /** @deprecated */
    @Deprecated
    public void setBackgroundDrawable(Drawable background) {
    }

    public Drawable getBackground() {
        return null;
    }

    public void setBackgroundTintList(@Nullable ColorStateList tint) {
    }

    @Nullable
    public ColorStateList getBackgroundTintList() {
        return null;
    }

    public void setBackgroundTintMode(@Nullable PorterDuff.Mode tintMode) {
    }

    public void setBackgroundTintBlendMode(@Nullable BlendMode blendMode) {
    }

    @Nullable
    public PorterDuff.Mode getBackgroundTintMode() {
        return null;
    }

    @Nullable
    public BlendMode getBackgroundTintBlendMode() {
        return null;
    }

    public Drawable getForeground() {
        return null;
    }

    public void setForeground(Drawable foreground) {
    }

    public int getForegroundGravity() {
        return 0;
    }

    public void setForegroundGravity(int gravity) {
    }

    public void setForegroundTintList(@Nullable ColorStateList tint) {
    }

    @Nullable
    public ColorStateList getForegroundTintList() {
        return null;
    }

    public void setForegroundTintMode(@Nullable PorterDuff.Mode tintMode) {
    }

    public void setForegroundTintBlendMode(@Nullable BlendMode blendMode) {
    }

    @Nullable
    public PorterDuff.Mode getForegroundTintMode() {
        return null;
    }

    @Nullable
    public BlendMode getForegroundTintBlendMode() {
        return null;
    }

    public void onDrawForeground(Canvas canvas) {
    }

    public void setPadding(int left, int top, int right, int bottom) {
    }

    public void setPaddingRelative(int start, int top, int end, int bottom) {
    }

    public int getSourceLayoutResId() {
        return 0;
    }

    public int getPaddingTop() {
        return 0;
    }

    public int getPaddingBottom() {
        return 0;
    }

    public int getPaddingLeft() {
        return 0;
    }

    public int getPaddingStart() {
        return 0;
    }

    public int getPaddingRight() {
        return 0;
    }

    public int getPaddingEnd() {
        return 0;
    }

    public boolean isPaddingRelative() {
        return false;
    }

    public void setSelected(boolean selected) {
    }

    protected void dispatchSetSelected(boolean selected) {
    }

    @ExportedProperty
    public boolean isSelected() {
        return false;
    }

    public void setActivated(boolean activated) {
    }

    protected void dispatchSetActivated(boolean activated) {
    }

    @ExportedProperty
    public boolean isActivated() {
        return false;
    }

    public ViewTreeObserver getViewTreeObserver() {
        return null;
    }

    public View getRootView() {
        return null;
    }

    public void transformMatrixToGlobal(@NonNull Matrix matrix) {
    }

    public void transformMatrixToLocal(@NonNull Matrix matrix) {
    }

    public void getLocationOnScreen(int[] outLocation) {
    }

    public void getLocationInWindow(int[] outLocation) {
    }

    public final <T extends View> T findViewById(int id) {
        return null;
    }

    @NonNull
    public final <T extends View> T requireViewById(int id) {
        return null;
    }

    public final <T extends View> T findViewWithTag(Object tag) {
        return null;
    }

    public void setId(int id) {
    }

    @CapturedViewProperty
    public int getId() {
        return 0;
    }

    public long getUniqueDrawingId() {
        return 0;
    }

    @ExportedProperty
    public Object getTag() {
        return null;
    }

    public void setTag(Object tag) {
    }

    public Object getTag(int key) {
        return null;
    }

    public void setTag(int key, Object tag) {
    }

    @ExportedProperty(
            category = "layout"
    )
    public int getBaseline() {
        return 0;
    }

    public boolean isInLayout() {
        return false;
    }

    public void requestLayout() {
    }

    public void forceLayout() {
    }

    public final void measure(int widthMeasureSpec, int heightMeasureSpec) {
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    }

    protected final void setMeasuredDimension(int measuredWidth, int measuredHeight) {
    }

    public static int combineMeasuredStates(int curState, int newState) {
        return 0;
    }

    public static int resolveSize(int size, int measureSpec) {
        return 0;
    }

    public static int resolveSizeAndState(int size, int measureSpec, int childMeasuredState) {
        return 0;
    }

    public static int getDefaultSize(int size, int measureSpec) {
        return 0;
    }

    protected int getSuggestedMinimumHeight() {
        return 0;
    }

    protected int getSuggestedMinimumWidth() {
        return 0;
    }

    public int getMinimumHeight() {
        return 0;
    }

    public void setMinimumHeight(int minHeight) {
    }

    public int getMinimumWidth() {
        return 0;
    }

    public void setMinimumWidth(int minWidth) {
    }

    public Animation getAnimation() {
        return null;
    }

    public void startAnimation(Animation animation) {
    }

    public void clearAnimation() {
    }

    public void setAnimation(Animation animation) {
    }

    protected void onAnimationStart() {
    }

    protected void onAnimationEnd() {
    }

    protected boolean onSetAlpha(int alpha) {
        return false;
    }

    public void playSoundEffect(int soundConstant) {
    }

    public boolean performHapticFeedback(int feedbackConstant) {
        return false;
    }

    public boolean performHapticFeedback(int feedbackConstant, int flags) {
        return false;
    }

    /** @deprecated */
    @Deprecated
    public void setSystemUiVisibility(int visibility) {
    }

    /** @deprecated */
    @Deprecated
    public int getSystemUiVisibility() {
        return 0;
    }

    /** @deprecated */
    @Deprecated
    public int getWindowSystemUiVisibility() {
        return 0;
    }

    /** @deprecated */
    @Deprecated
    public void onWindowSystemUiVisibilityChanged(int visible) {
    }

    /** @deprecated */
    @Deprecated
    public void dispatchWindowSystemUiVisiblityChanged(int visible) {
    }

    /** @deprecated */
    @Deprecated
    public void setOnSystemUiVisibilityChangeListener(OnSystemUiVisibilityChangeListener l) {
    }

    /** @deprecated */
    @Deprecated
    public void dispatchSystemUiVisibilityChanged(int visibility) {
    }

    /** @deprecated */
    @Deprecated
    public final boolean startDrag(ClipData data, DragShadowBuilder shadowBuilder, Object myLocalState, int flags) {
        return false;
    }

    public final boolean startDragAndDrop(ClipData data, DragShadowBuilder shadowBuilder, Object myLocalState, int flags) {
        return false;
    }

    public final void cancelDragAndDrop() {
    }

    public final void updateDragShadow(DragShadowBuilder shadowBuilder) {
    }

    public boolean onDragEvent(DragEvent event) {
        return false;
    }

    public boolean dispatchDragEvent(DragEvent event) {
        return false;
    }

    public static View inflate(Context context, int resource, ViewGroup root) {
        return null;
    }

    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        return false;
    }

    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
    }

    public int getOverScrollMode() {
        return 0;
    }

    public void setOverScrollMode(int overScrollMode) {
    }

    public void setNestedScrollingEnabled(boolean enabled) {
    }

    public boolean isNestedScrollingEnabled() {
        return false;
    }

    public boolean startNestedScroll(int axes) {
        return false;
    }

    public void stopNestedScroll() {
    }

    public boolean hasNestedScrollingParent() {
        return false;
    }

    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow) {
        return false;
    }

    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow) {
        return false;
    }

    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return false;
    }

    public void setTextDirection(int textDirection) {
    }

    @ExportedProperty(
            category = "text",
            mapping = {@IntToString(
                    from = 0,
                    to = "INHERIT"
            ), @IntToString(
                    from = 1,
                    to = "FIRST_STRONG"
            ), @IntToString(
                    from = 2,
                    to = "ANY_RTL"
            ), @IntToString(
                    from = 3,
                    to = "LTR"
            ), @IntToString(
                    from = 4,
                    to = "RTL"
            ), @IntToString(
                    from = 5,
                    to = "LOCALE"
            ), @IntToString(
                    from = 6,
                    to = "FIRST_STRONG_LTR"
            ), @IntToString(
                    from = 7,
                    to = "FIRST_STRONG_RTL"
            )}
    )
    public int getTextDirection() {
        return 0;
    }

    public boolean canResolveTextDirection() {
        return false;
    }

    public boolean isTextDirectionResolved() {
        return false;
    }

    public void setTextAlignment(int textAlignment) {
    }

    @ExportedProperty(
            category = "text",
            mapping = {@IntToString(
                    from = 0,
                    to = "INHERIT"
            ), @IntToString(
                    from = 1,
                    to = "GRAVITY"
            ), @IntToString(
                    from = 2,
                    to = "TEXT_START"
            ), @IntToString(
                    from = 3,
                    to = "TEXT_END"
            ), @IntToString(
                    from = 4,
                    to = "CENTER"
            ), @IntToString(
                    from = 5,
                    to = "VIEW_START"
            ), @IntToString(
                    from = 6,
                    to = "VIEW_END"
            )}
    )
    public int getTextAlignment() {
        return 0;
    }

    public boolean canResolveTextAlignment() {
        return false;
    }

    public boolean isTextAlignmentResolved() {
        return false;
    }

    public static int generateViewId() {
        return 0;
    }

    public PointerIcon onResolvePointerIcon(MotionEvent event, int pointerIndex) {
        return null;
    }

    public void setPointerIcon(PointerIcon pointerIcon) {
    }

    public PointerIcon getPointerIcon() {
        return null;
    }

    public boolean hasPointerCapture() {
        return false;
    }

    public void requestPointerCapture() {
    }

    public void releasePointerCapture() {
    }

    public void onPointerCaptureChange(boolean hasCapture) {
    }

    public void dispatchPointerCaptureChanged(boolean hasCapture) {
    }

    public boolean onCapturedPointerEvent(MotionEvent event) {
        return false;
    }

    public void setOnCapturedPointerListener(OnCapturedPointerListener l) {
    }

    public ViewPropertyAnimator animate() {
        return null;
    }

    public final void setTransitionName(String transitionName) {
    }

    @ExportedProperty
    public String getTransitionName() {
        return null;
    }

    public void setTooltipText(@Nullable CharSequence tooltipText) {
    }

    @Nullable
    public CharSequence getTooltipText() {
        return null;
    }

    public void addOnUnhandledKeyEventListener(OnUnhandledKeyEventListener listener) {
    }

    public void removeOnUnhandledKeyEventListener(OnUnhandledKeyEventListener listener) {
    }

    public interface OnUnhandledKeyEventListener {
        boolean onUnhandledKeyEvent(View var1, KeyEvent var2);
    }

    public interface OnTouchListener {
        boolean onTouch(View var1, MotionEvent var2);
    }

    /** @deprecated */
    @Deprecated
    public interface OnSystemUiVisibilityChangeListener {
        /** @deprecated */
        @Deprecated
        void onSystemUiVisibilityChange(int var1);
    }

    public interface OnScrollChangeListener {
        void onScrollChange(View var1, int var2, int var3, int var4, int var5);
    }

    public interface OnLongClickListener {
        boolean onLongClick(View var1);
    }

    public interface OnLayoutChangeListener {
        void onLayoutChange(View var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9);
    }

    public interface OnKeyListener {
        boolean onKey(View var1, int var2, KeyEvent var3);
    }

    public interface OnHoverListener {
        boolean onHover(View var1, MotionEvent var2);
    }

    public interface OnGenericMotionListener {
        boolean onGenericMotion(View var1, MotionEvent var2);
    }

    public interface OnFocusChangeListener {
        void onFocusChange(View var1, boolean var2);
    }

    public interface OnDragListener {
        boolean onDrag(View var1, DragEvent var2);
    }

    public interface OnCreateContextMenuListener {
        void onCreateContextMenu(ContextMenu var1, View var2, ContextMenu.ContextMenuInfo var3);
    }

    public interface OnContextClickListener {
        boolean onContextClick(View var1);
    }

    public interface OnClickListener {
        void onClick(View var1);
    }

    public interface OnCapturedPointerListener {
        boolean onCapturedPointer(View var1, MotionEvent var2);
    }

    public interface OnAttachStateChangeListener {
        void onViewAttachedToWindow(View var1);

        void onViewDetachedFromWindow(View var1);
    }

    public interface OnApplyWindowInsetsListener {
        WindowInsets onApplyWindowInsets(View var1, WindowInsets var2);
    }

    public static class MeasureSpec {
        private static final int MODE_MASK = 0xC0000000;
        public static final int AT_MOST = Integer.MIN_VALUE;
        public static final int EXACTLY = 1073741824;
        public static final int UNSPECIFIED = 0;

        public MeasureSpec() {
        }

        public static int makeMeasureSpec(int size, int mode) {
            return size + mode;
        }

        public static int getMode(int measureSpec) {
            return measureSpec & MODE_MASK;
        }

        public static int getSize(int measureSpec) {
            return measureSpec & ~MODE_MASK;
        }

        public static String toString(int measureSpec) {
            int mode = getMode(measureSpec);
            int size = getSize(measureSpec);
            return "MeasureSpec: " + size + " " + (mode == EXACTLY ? "EXACTLY" : mode == AT_MOST ? "AT_MOST" : "UNSPECIFIED");
        }
    }

    public static class DragShadowBuilder {
        private View mView;

        public DragShadowBuilder(View view) {
            mView = view;
        }

        public DragShadowBuilder() {
        }

        public final View getView() {
            return mView;
        }

        public void onProvideShadowMetrics(Point outShadowSize, Point outShadowTouchPoint) {
        }

        public void onDrawShadow(Canvas canvas) {
        }
    }

    public static class BaseSavedState extends AbsSavedState {
        @NonNull
        public static final Parcelable.Creator<BaseSavedState> CREATOR = null;

        public BaseSavedState(Parcel source) {
            super((Parcelable)null);
        }

        public BaseSavedState(Parcel source, ClassLoader loader) {
            super((Parcelable)null);
        }

        public BaseSavedState(Parcelable superState) {
            super((Parcelable)null);
        }

        public void writeToParcel(Parcel out, int flags) {
        }
    }

    public static class AccessibilityDelegate {
        public AccessibilityDelegate() {
        }

        public void sendAccessibilityEvent(View host, int eventType) {
        }

        public boolean performAccessibilityAction(View host, int action, Bundle args) {
            return false;
        }

        public void sendAccessibilityEventUnchecked(View host, AccessibilityEvent event) {
        }

        public boolean dispatchPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
            return false;
        }

        public void onPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
        }

        public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
        }

        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
        }

        public void addExtraDataToAccessibilityNodeInfo(@NonNull View host, @NonNull AccessibilityNodeInfo info, @NonNull String extraDataKey, @Nullable Bundle arguments) {
        }

        public boolean onRequestSendAccessibilityEvent(ViewGroup host, View child, AccessibilityEvent event) {
            return false;
        }

        public AccessibilityNodeProvider getAccessibilityNodeProvider(View host) {
            return null;
        }
    }
}
