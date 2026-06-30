//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package android.graphics;

/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2023 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.os.LocaleList;
import java.util.Locale;

public class Paint {
    private int color = 0xFF000000;
    private int flags = 0;
    private int hinting = HINTING_OFF;
    private int alpha = 255;
    private float strokeWidth = 0f;
    private float strokeMiter = 4f;
    private float textSize = 12f;
    private float textScaleX = 1f;
    private float textSkewX = -0.25f;
    private float letterSpacing = 0f;
    private float wordSpacing = 0f;
    private Style style = Style.FILL;
    private Cap strokeCap = Cap.BUTT;
    private Join strokeJoin = Join.MITER;
    private Align textAlign = Align.LEFT;
    private Typeface typeface;
    private Shader shader;
    private ColorFilter colorFilter;
    private Xfermode xfermode;
    private BlendMode blendMode;
    private PathEffect pathEffect;
    private MaskFilter maskFilter;
    private float shadowRadius = 0f;
    private float shadowDx = 0f;
    private float shadowDy = 0f;
    private int shadowColor = 0;
    private Locale textLocale = Locale.getDefault();
    private LocaleList textLocales = LocaleList.getDefault();
    private boolean elegantTextHeight = false;
    private String fontFeatureSettings;
    private String fontVariationSettings;
    private int startHyphenEdit = START_HYPHEN_EDIT_NO_EDIT;
    private int endHyphenEdit = END_HYPHEN_EDIT_NO_EDIT;
    
    public static final int ANTI_ALIAS_FLAG = 1;
    public static final int CURSOR_AFTER = 0;
    public static final int CURSOR_AT = 4;
    public static final int CURSOR_AT_OR_AFTER = 1;
    public static final int CURSOR_AT_OR_BEFORE = 3;
    public static final int CURSOR_BEFORE = 2;
    public static final int DEV_KERN_TEXT_FLAG = 256;
    public static final int DITHER_FLAG = 4;
    public static final int EMBEDDED_BITMAP_TEXT_FLAG = 1024;
    public static final int END_HYPHEN_EDIT_INSERT_ARMENIAN_HYPHEN = 3;
    public static final int END_HYPHEN_EDIT_INSERT_HYPHEN = 2;
    public static final int END_HYPHEN_EDIT_INSERT_MAQAF = 4;
    public static final int END_HYPHEN_EDIT_INSERT_UCAS_HYPHEN = 5;
    public static final int END_HYPHEN_EDIT_INSERT_ZWJ_AND_HYPHEN = 6;
    public static final int END_HYPHEN_EDIT_NO_EDIT = 0;
    public static final int END_HYPHEN_EDIT_REPLACE_WITH_HYPHEN = 1;
    public static final int FAKE_BOLD_TEXT_FLAG = 32;
    public static final int FILTER_BITMAP_FLAG = 2;
    public static final int HINTING_OFF = 0;
    public static final int HINTING_ON = 1;
    public static final int LINEAR_TEXT_FLAG = 64;
    public static final int START_HYPHEN_EDIT_INSERT_HYPHEN = 1;
    public static final int START_HYPHEN_EDIT_INSERT_ZWJ = 2;
    public static final int START_HYPHEN_EDIT_NO_EDIT = 0;
    public static final int STRIKE_THRU_TEXT_FLAG = 16;
    public static final int SUBPIXEL_TEXT_FLAG = 128;
    public static final int UNDERLINE_TEXT_FLAG = 8;

    public Paint() {
    }

    public Paint(int flags) {
        this.flags = flags;
    }

    public Paint(Paint paint) {
        this.color = paint.color;
        this.flags = paint.flags;
        this.hinting = paint.hinting;
        this.alpha = paint.alpha;
        this.strokeWidth = paint.strokeWidth;
        this.strokeMiter = paint.strokeMiter;
        this.textSize = paint.textSize;
        this.textScaleX = paint.textScaleX;
        this.textSkewX = paint.textSkewX;
        this.letterSpacing = paint.letterSpacing;
        this.wordSpacing = paint.wordSpacing;
        this.style = paint.style;
        this.strokeCap = paint.strokeCap;
        this.strokeJoin = paint.strokeJoin;
        this.textAlign = paint.textAlign;
        this.typeface = paint.typeface;
        this.shader = paint.shader;
        this.colorFilter = paint.colorFilter;
        this.xfermode = paint.xfermode;
        this.blendMode = paint.blendMode;
        this.pathEffect = paint.pathEffect;
        this.maskFilter = paint.maskFilter;
        this.shadowRadius = paint.shadowRadius;
        this.shadowDx = paint.shadowDx;
        this.shadowDy = paint.shadowDy;
        this.shadowColor = paint.shadowColor;
        this.textLocale = paint.textLocale;
        this.textLocales = paint.textLocales;
        this.elegantTextHeight = paint.elegantTextHeight;
        this.fontFeatureSettings = paint.fontFeatureSettings;
        this.fontVariationSettings = paint.fontVariationSettings;
        this.startHyphenEdit = paint.startHyphenEdit;
        this.endHyphenEdit = paint.endHyphenEdit;
    }

    public void reset() {
        color = 0xFF000000;
        flags = 0;
        hinting = HINTING_OFF;
        alpha = 255;
        strokeWidth = 0f;
        strokeMiter = 4f;
        textSize = 12f;
        textScaleX = 1f;
        textSkewX = -0.25f;
        letterSpacing = 0f;
        wordSpacing = 0f;
        style = Style.FILL;
        strokeCap = Cap.BUTT;
        strokeJoin = Join.MITER;
        textAlign = Align.LEFT;
        typeface = null;
        shader = null;
        colorFilter = null;
        xfermode = null;
        blendMode = null;
        pathEffect = null;
        maskFilter = null;
        shadowRadius = 0f;
        shadowDx = 0f;
        shadowDy = 0f;
        shadowColor = 0;
        textLocale = Locale.getDefault();
        textLocales = LocaleList.getDefault();
        elegantTextHeight = false;
        fontFeatureSettings = null;
        fontVariationSettings = null;
        startHyphenEdit = START_HYPHEN_EDIT_NO_EDIT;
        endHyphenEdit = END_HYPHEN_EDIT_NO_EDIT;
    }

    public void set(Paint src) {
        this.color = src.color;
        this.flags = src.flags;
        this.hinting = src.hinting;
        this.alpha = src.alpha;
        this.strokeWidth = src.strokeWidth;
        this.strokeMiter = src.strokeMiter;
        this.textSize = src.textSize;
        this.textScaleX = src.textScaleX;
        this.textSkewX = src.textSkewX;
        this.letterSpacing = src.letterSpacing;
        this.wordSpacing = src.wordSpacing;
        this.style = src.style;
        this.strokeCap = src.strokeCap;
        this.strokeJoin = src.strokeJoin;
        this.textAlign = src.textAlign;
        this.typeface = src.typeface;
        this.shader = src.shader;
        this.colorFilter = src.colorFilter;
        this.xfermode = src.xfermode;
        this.blendMode = src.blendMode;
        this.pathEffect = src.pathEffect;
        this.maskFilter = src.maskFilter;
        this.shadowRadius = src.shadowRadius;
        this.shadowDx = src.shadowDx;
        this.shadowDy = src.shadowDy;
        this.shadowColor = src.shadowColor;
        this.textLocale = src.textLocale;
        this.textLocales = src.textLocales;
        this.elegantTextHeight = src.elegantTextHeight;
        this.fontFeatureSettings = src.fontFeatureSettings;
        this.fontVariationSettings = src.fontVariationSettings;
        this.startHyphenEdit = src.startHyphenEdit;
        this.endHyphenEdit = src.endHyphenEdit;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public int getHinting() {
        return hinting;
    }

    public void setHinting(int mode) {
        this.hinting = mode;
    }

    public final boolean isAntiAlias() {
        return (flags & ANTI_ALIAS_FLAG) != 0;
    }

    public void setAntiAlias(boolean aa) {
        if (aa) {
            flags |= ANTI_ALIAS_FLAG;
        } else {
            flags &= ~ANTI_ALIAS_FLAG;
        }
    }

    public final boolean isDither() {
        return (flags & DITHER_FLAG) != 0;
    }

    public void setDither(boolean dither) {
        if (dither) {
            flags |= DITHER_FLAG;
        } else {
            flags &= ~DITHER_FLAG;
        }
    }

    public final boolean isLinearText() {
        return (flags & LINEAR_TEXT_FLAG) != 0;
    }

    public void setLinearText(boolean linearText) {
        if (linearText) {
            flags |= LINEAR_TEXT_FLAG;
        } else {
            flags &= ~LINEAR_TEXT_FLAG;
        }
    }

    public final boolean isSubpixelText() {
        return (flags & SUBPIXEL_TEXT_FLAG) != 0;
    }

    public void setSubpixelText(boolean subpixelText) {
        if (subpixelText) {
            flags |= SUBPIXEL_TEXT_FLAG;
        } else {
            flags &= ~SUBPIXEL_TEXT_FLAG;
        }
    }

    public final boolean isUnderlineText() {
        return (flags & UNDERLINE_TEXT_FLAG) != 0;
    }

    public float getUnderlinePosition() {
        return 0f;
    }

    public float getUnderlineThickness() {
        return 1f;
    }

    public void setUnderlineText(boolean underlineText) {
        if (underlineText) {
            flags |= UNDERLINE_TEXT_FLAG;
        } else {
            flags &= ~UNDERLINE_TEXT_FLAG;
        }
    }

    public final boolean isStrikeThruText() {
        return (flags & STRIKE_THRU_TEXT_FLAG) != 0;
    }

    public float getStrikeThruPosition() {
        return 0f;
    }

    public float getStrikeThruThickness() {
        return 1f;
    }

    public void setStrikeThruText(boolean strikeThruText) {
        if (strikeThruText) {
            flags |= STRIKE_THRU_TEXT_FLAG;
        } else {
            flags &= ~STRIKE_THRU_TEXT_FLAG;
        }
    }

    public final boolean isFakeBoldText() {
        return (flags & FAKE_BOLD_TEXT_FLAG) != 0;
    }

    public void setFakeBoldText(boolean fakeBoldText) {
        if (fakeBoldText) {
            flags |= FAKE_BOLD_TEXT_FLAG;
        } else {
            flags &= ~FAKE_BOLD_TEXT_FLAG;
        }
    }

    public final boolean isFilterBitmap() {
        return (flags & FILTER_BITMAP_FLAG) != 0;
    }

    public void setFilterBitmap(boolean filter) {
        if (filter) {
            flags |= FILTER_BITMAP_FLAG;
        } else {
            flags &= ~FILTER_BITMAP_FLAG;
        }
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public int getColor() {
        return color;
    }

    public long getColorLong() {
        return color & 0xFFFFFFFFL;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setColor(long color) {
        this.color = (int) (color & 0xFFFFFFFFL);
    }

    public int getAlpha() {
        return (color >> 24) & 0xFF;
    }

    public void setAlpha(int a) {
        this.color = (this.color & 0x00FFFFFF) | ((a & 0xFF) << 24);
    }

    public void setARGB(int a, int r, int g, int b) {
        this.color = (a << 24) | (r << 16) | (g << 8) | b;
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(float width) {
        this.strokeWidth = width;
    }

    public float getStrokeMiter() {
        return strokeMiter;
    }

    public void setStrokeMiter(float miter) {
        this.strokeMiter = miter;
    }

    public Cap getStrokeCap() {
        return strokeCap;
    }

    public void setStrokeCap(Cap cap) {
        this.strokeCap = cap;
    }

    public Join getStrokeJoin() {
        return strokeJoin;
    }

    public void setStrokeJoin(Join join) {
        this.strokeJoin = join;
    }

    public boolean getFillPath(Path src, Path dst) {
        return false;
    }

    public Shader getShader() {
        return shader;
    }

    public Shader setShader(Shader shader) {
        Shader old = this.shader;
        this.shader = shader;
        return old;
    }

    public ColorFilter getColorFilter() {
        return colorFilter;
    }

    public ColorFilter setColorFilter(ColorFilter filter) {
        ColorFilter old = this.colorFilter;
        this.colorFilter = filter;
        return old;
    }

    public Xfermode getXfermode() {
        return xfermode;
    }

    @Nullable
    public BlendMode getBlendMode() {
        return blendMode;
    }

    public Xfermode setXfermode(Xfermode xfermode) {
        Xfermode old = this.xfermode;
        this.xfermode = xfermode;
        return old;
    }

    public void setBlendMode(@Nullable BlendMode blendmode) {
        this.blendMode = blendmode;
    }

    public PathEffect getPathEffect() {
        return pathEffect;
    }

    public PathEffect setPathEffect(PathEffect effect) {
        PathEffect old = this.pathEffect;
        this.pathEffect = effect;
        return old;
    }

    public MaskFilter getMaskFilter() {
        return maskFilter;
    }

    public MaskFilter setMaskFilter(MaskFilter maskfilter) {
        MaskFilter old = this.maskFilter;
        this.maskFilter = maskfilter;
        return old;
    }

    public Typeface getTypeface() {
        return typeface;
    }

    public Typeface setTypeface(Typeface typeface) {
        Typeface old = this.typeface;
        this.typeface = typeface;
        return old;
    }

    public void setShadowLayer(float radius, float dx, float dy, int shadowColor) {
        this.shadowRadius = radius;
        this.shadowDx = dx;
        this.shadowDy = dy;
        this.shadowColor = shadowColor;
    }

    public void setShadowLayer(float radius, float dx, float dy, long shadowColor) {
        setShadowLayer(radius, dx, dy, (int) (shadowColor & 0xFFFFFFFFL));
    }

    public void clearShadowLayer() {
        this.shadowRadius = 0f;
        this.shadowDx = 0f;
        this.shadowDy = 0f;
        this.shadowColor = 0;
    }

    public float getShadowLayerRadius() {
        return shadowRadius;
    }

    public float getShadowLayerDx() {
        return shadowDx;
    }

    public float getShadowLayerDy() {
        return shadowDy;
    }

    public int getShadowLayerColor() {
        return shadowColor;
    }

    public long getShadowLayerColorLong() {
        return shadowColor & 0xFFFFFFFFL;
    }

    public Align getTextAlign() {
        return textAlign;
    }

    public void setTextAlign(Align align) {
        this.textAlign = align;
    }

    @NonNull
    public Locale getTextLocale() {
        return textLocale;
    }

    @NonNull
    public LocaleList getTextLocales() {
        return textLocales;
    }

    public void setTextLocale(@NonNull Locale locale) {
        this.textLocale = locale;
    }

    public void setTextLocales(@NonNull LocaleList locales) {
        this.textLocales = locales;
    }

    public boolean isElegantTextHeight() {
        return elegantTextHeight;
    }

    public void setElegantTextHeight(boolean elegant) {
        this.elegantTextHeight = elegant;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public float getTextScaleX() {
        return textScaleX;
    }

    public void setTextScaleX(float scaleX) {
        this.textScaleX = scaleX;
    }

    public float getTextSkewX() {
        return textSkewX;
    }

    public void setTextSkewX(float skewX) {
        this.textSkewX = skewX;
    }

    public float getLetterSpacing() {
        return letterSpacing;
    }

    public void setLetterSpacing(float letterSpacing) {
        this.letterSpacing = letterSpacing;
    }

    public float getWordSpacing() {
        return wordSpacing;
    }

    public void setWordSpacing(float wordSpacing) {
        this.wordSpacing = wordSpacing;
    }

    public String getFontFeatureSettings() {
        return fontFeatureSettings;
    }

    public void setFontFeatureSettings(String settings) {
        this.fontFeatureSettings = settings;
    }

    public String getFontVariationSettings() {
        return fontVariationSettings;
    }

    public boolean setFontVariationSettings(String fontVariationSettings) {
        this.fontVariationSettings = fontVariationSettings;
        return true;
    }

    public int getStartHyphenEdit() {
        return startHyphenEdit;
    }

    public int getEndHyphenEdit() {
        return endHyphenEdit;
    }

    public void setStartHyphenEdit(int startHyphen) {
        this.startHyphenEdit = startHyphen;
    }

    public void setEndHyphenEdit(int endHyphen) {
        this.endHyphenEdit = endHyphen;
    }

    public float ascent() {
        return -textSize * 0.8f;
    }

    public float descent() {
        return textSize * 0.2f;
    }

    public float getFontMetrics(FontMetrics metrics) {
        if (metrics != null) {
            metrics.top = -textSize * 1.2f;
            metrics.ascent = -textSize * 0.8f;
            metrics.descent = textSize * 0.2f;
            metrics.bottom = textSize * 0.4f;
            metrics.leading = 0f;
        }
        return 0f;
    }

    public FontMetrics getFontMetrics() {
        FontMetrics fm = new FontMetrics();
        fm.top = -textSize * 1.2f;
        fm.ascent = -textSize * 0.8f;
        fm.descent = textSize * 0.2f;
        fm.bottom = textSize * 0.4f;
        fm.leading = 0f;
        return fm;
    }

    public int getFontMetricsInt(FontMetricsInt fmi) {
        if (fmi != null) {
            fmi.top = (int) (-textSize * 1.2f);
            fmi.ascent = (int) (-textSize * 0.8f);
            fmi.descent = (int) (textSize * 0.2f);
            fmi.bottom = (int) (textSize * 0.4f);
            fmi.leading = 0;
        }
        return 0;
    }

    public FontMetricsInt getFontMetricsInt() {
        FontMetricsInt fmi = new FontMetricsInt();
        fmi.top = (int) (-textSize * 1.2f);
        fmi.ascent = (int) (-textSize * 0.8f);
        fmi.descent = (int) (textSize * 0.2f);
        fmi.bottom = (int) (textSize * 0.4f);
        fmi.leading = 0;
        return fmi;
    }

    public float getFontSpacing() {
        return textSize * 1.2f;
    }

    public float measureText(char[] text, int index, int count) {
        return textSize * count * 0.5f;
    }

    public float measureText(String text, int start, int end) {
        return textSize * (end - start) * 0.5f;
    }

    public float measureText(String text) {
        return textSize * text.length() * 0.5f;
    }

    public float measureText(CharSequence text, int start, int end) {
        return textSize * (end - start) * 0.5f;
    }

    public int breakText(char[] text, int index, int count, float maxWidth, float[] measuredWidth) {
        int chars = (int) (maxWidth / (textSize * 0.5f));
        chars = Math.min(chars, count);
        if (measuredWidth != null && measuredWidth.length > 0) {
            measuredWidth[0] = chars * textSize * 0.5f;
        }
        return chars;
    }

    public int breakText(CharSequence text, int start, int end, boolean measureForwards, float maxWidth, float[] measuredWidth) {
        int count = end - start;
        return breakText(text.toString().toCharArray(), 0, count, maxWidth, measuredWidth);
    }

    public int breakText(String text, boolean measureForwards, float maxWidth, float[] measuredWidth) {
        return breakText(text.toCharArray(), 0, text.length(), maxWidth, measuredWidth);
    }

    public int getTextWidths(char[] text, int index, int count, float[] widths) {
        float w = textSize * 0.5f;
        for (int i = 0; i < count && i < widths.length; i++) {
            widths[i] = w;
        }
        return Math.min(count, widths.length);
    }

    public int getTextWidths(CharSequence text, int start, int end, float[] widths) {
        return getTextWidths(text.toString().toCharArray(), 0, end - start, widths);
    }

    public int getTextWidths(String text, int start, int end, float[] widths) {
        return getTextWidths(text.toCharArray(), 0, end - start, widths);
    }

    public int getTextWidths(String text, float[] widths) {
        return getTextWidths(text.toCharArray(), 0, text.length(), widths);
    }

    public float getTextRunAdvances(@NonNull char[] chars, int index, int count, int contextIndex, int contextCount, boolean isRtl, @Nullable float[] advances, int advancesIndex) {
        return textSize * 0.5f * count;
    }

    public int getTextRunCursor(@NonNull char[] text, int contextStart, int contextLength, boolean isRtl, int offset, int cursorOpt) {
        return offset;
    }

    public int getTextRunCursor(@NonNull CharSequence text, int contextStart, int contextEnd, boolean isRtl, int offset, int cursorOpt) {
        return offset;
    }

    public void getTextPath(char[] text, int index, int count, float x, float y, Path path) {
    }

    public void getTextPath(String text, int start, int end, float x, float y, Path path) {
    }

    public void getTextBounds(String text, int start, int end, Rect bounds) {
        if (bounds != null) {
            bounds.set(0, (int) ascent(), (int) (textSize * (end - start) * 0.5f), (int) descent());
        }
    }

    public void getTextBounds(@NonNull CharSequence text, int start, int end, @NonNull Rect bounds) {
        getTextBounds(text.toString(), start, end, bounds);
    }

    public void getTextBounds(char[] text, int index, int count, Rect bounds) {
        getTextBounds(new String(text, index, count), 0, count, bounds);
    }

    public boolean hasGlyph(String string) {
        return true;
    }

    public float getRunAdvance(char[] text, int start, int end, int contextStart, int contextEnd, boolean isRtl, int offset) {
        return textSize * 0.5f * (offset - start);
    }

    public float getRunAdvance(CharSequence text, int start, int end, int contextStart, int contextEnd, boolean isRtl, int offset) {
        return textSize * 0.5f * (offset - start);
    }

    public int getOffsetForAdvance(char[] text, int start, int end, int contextStart, int contextEnd, boolean isRtl, float advance) {
        return start + (int) (advance / (textSize * 0.5f));
    }

    public int getOffsetForAdvance(CharSequence text, int start, int end, int contextStart, int contextEnd, boolean isRtl, float advance) {
        return start + (int) (advance / (textSize * 0.5f));
    }

    public boolean equalsForTextMeasurement(@NonNull Paint other) {
        return this.textSize == other.textSize &&
               this.typeface == other.typeface &&
               this.flags == other.flags;
    }

    public static enum Style {
        FILL,
        STROKE,
        FILL_AND_STROKE;

        private Style() {
        }
    }

    public static enum Join {
        MITER,
        ROUND,
        BEVEL;

        private Join() {
        }
    }

    public static class FontMetricsInt {
        public int ascent;
        public int bottom;
        public int descent;
        public int leading;
        public int top;

        public FontMetricsInt() {
        }

        public String toString() {
            return "FontMetricsInt: top=" + top + " ascent=" + ascent + " descent=" + descent + " bottom=" + bottom + " leading=" + leading;
        }
    }

    public static class FontMetrics {
        public float ascent;
        public float bottom;
        public float descent;
        public float leading;
        public float top;

        public FontMetrics() {
        }
    }

    public static enum Cap {
        BUTT,
        ROUND,
        SQUARE;

        private Cap() {
        }
    }

    public static enum Align {
        LEFT,
        CENTER,
        RIGHT;

        private Align() {
        }
    }
}
