//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package android.graphics;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.os.LocaleList;
import java.util.Locale;

public class Paint {
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
        throw new RuntimeException("Stub!");
    }

    public Paint(int flags) {
        throw new RuntimeException("Stub!");
    }

    public Paint(Paint paint) {
        throw new RuntimeException("Stub!");
    }

    public void reset() {
        throw new RuntimeException("Stub!");
    }

    public void set(Paint src) {
        throw new RuntimeException("Stub!");
    }

    public int getFlags() {
        throw new RuntimeException("Stub!");
    }

    public void setFlags(int flags) {
        throw new RuntimeException("Stub!");
    }

    public int getHinting() {
        throw new RuntimeException("Stub!");
    }

    public void setHinting(int mode) {
        throw new RuntimeException("Stub!");
    }

    public final boolean isAntiAlias() {
        throw new RuntimeException("Stub!");
    }

    public void setAntiAlias(boolean aa) {
        throw new RuntimeException("Stub!");
    }

    public final boolean isDither() {
        throw new RuntimeException("Stub!");
    }

    public void setDither(boolean dither) {
        throw new RuntimeException("Stub!");
    }

    public final boolean isLinearText() {
        throw new RuntimeException("Stub!");
    }

    public void setLinearText(boolean linearText) {
        throw new RuntimeException("Stub!");
    }

    public final boolean isSubpixelText() {
        throw new RuntimeException("Stub!");
    }

    public void setSubpixelText(boolean subpixelText) {
        throw new RuntimeException("Stub!");
    }

    public final boolean isUnderlineText() {
        throw new RuntimeException("Stub!");
    }

    public float getUnderlinePosition() {
        throw new RuntimeException("Stub!");
    }

    public float getUnderlineThickness() {
        throw new RuntimeException("Stub!");
    }

    public void setUnderlineText(boolean underlineText) {
        throw new RuntimeException("Stub!");
    }

    public final boolean isStrikeThruText() {
        throw new RuntimeException("Stub!");
    }

    public float getStrikeThruPosition() {
        throw new RuntimeException("Stub!");
    }

    public float getStrikeThruThickness() {
        throw new RuntimeException("Stub!");
    }

    public void setStrikeThruText(boolean strikeThruText) {
        throw new RuntimeException("Stub!");
    }

    public final boolean isFakeBoldText() {
        throw new RuntimeException("Stub!");
    }

    public void setFakeBoldText(boolean fakeBoldText) {
        throw new RuntimeException("Stub!");
    }

    public final boolean isFilterBitmap() {
        throw new RuntimeException("Stub!");
    }

    public void setFilterBitmap(boolean filter) {
        throw new RuntimeException("Stub!");
    }

    public Style getStyle() {
        throw new RuntimeException("Stub!");
    }

    public void setStyle(Style style) {
        throw new RuntimeException("Stub!");
    }

    public int getColor() {
        throw new RuntimeException("Stub!");
    }

    public long getColorLong() {
        throw new RuntimeException("Stub!");
    }

    public void setColor(int color) {
        throw new RuntimeException("Stub!");
    }

    public void setColor(long color) {
        throw new RuntimeException("Stub!");
    }

    public int getAlpha() {
        throw new RuntimeException("Stub!");
    }

    public void setAlpha(int a) {
        throw new RuntimeException("Stub!");
    }

    public void setARGB(int a, int r, int g, int b) {
        throw new RuntimeException("Stub!");
    }

    public float getStrokeWidth() {
        throw new RuntimeException("Stub!");
    }

    public void setStrokeWidth(float width) {
        throw new RuntimeException("Stub!");
    }

    public float getStrokeMiter() {
        throw new RuntimeException("Stub!");
    }

    public void setStrokeMiter(float miter) {
        throw new RuntimeException("Stub!");
    }

    public Cap getStrokeCap() {
        throw new RuntimeException("Stub!");
    }

    public void setStrokeCap(Cap cap) {
        throw new RuntimeException("Stub!");
    }

    public Join getStrokeJoin() {
        throw new RuntimeException("Stub!");
    }

    public void setStrokeJoin(Join join) {
        throw new RuntimeException("Stub!");
    }

    public boolean getFillPath(Path src, Path dst) {
        throw new RuntimeException("Stub!");
    }

    public Shader getShader() {
        throw new RuntimeException("Stub!");
    }

    public Shader setShader(Shader shader) {
        throw new RuntimeException("Stub!");
    }

    public ColorFilter getColorFilter() {
        throw new RuntimeException("Stub!");
    }

    public ColorFilter setColorFilter(ColorFilter filter) {
        throw new RuntimeException("Stub!");
    }

    public Xfermode getXfermode() {
        throw new RuntimeException("Stub!");
    }

    @Nullable
    public BlendMode getBlendMode() {
        throw new RuntimeException("Stub!");
    }

    public Xfermode setXfermode(Xfermode xfermode) {
        throw new RuntimeException("Stub!");
    }

    public void setBlendMode(@Nullable BlendMode blendmode) {
        throw new RuntimeException("Stub!");
    }

    public PathEffect getPathEffect() {
        throw new RuntimeException("Stub!");
    }

    public PathEffect setPathEffect(PathEffect effect) {
        throw new RuntimeException("Stub!");
    }

    public MaskFilter getMaskFilter() {
        throw new RuntimeException("Stub!");
    }

    public MaskFilter setMaskFilter(MaskFilter maskfilter) {
        throw new RuntimeException("Stub!");
    }

    public Typeface getTypeface() {
        throw new RuntimeException("Stub!");
    }

    public Typeface setTypeface(Typeface typeface) {
        throw new RuntimeException("Stub!");
    }

    public void setShadowLayer(float radius, float dx, float dy, int shadowColor) {
        throw new RuntimeException("Stub!");
    }

    public void setShadowLayer(float radius, float dx, float dy, long shadowColor) {
        throw new RuntimeException("Stub!");
    }

    public void clearShadowLayer() {
        throw new RuntimeException("Stub!");
    }

    public float getShadowLayerRadius() {
        throw new RuntimeException("Stub!");
    }

    public float getShadowLayerDx() {
        throw new RuntimeException("Stub!");
    }

    public float getShadowLayerDy() {
        throw new RuntimeException("Stub!");
    }

    public int getShadowLayerColor() {
        throw new RuntimeException("Stub!");
    }

    public long getShadowLayerColorLong() {
        throw new RuntimeException("Stub!");
    }

    public Align getTextAlign() {
        throw new RuntimeException("Stub!");
    }

    public void setTextAlign(Align align) {
        throw new RuntimeException("Stub!");
    }

    @NonNull
    public Locale getTextLocale() {
        throw new RuntimeException("Stub!");
    }

    @NonNull
    public LocaleList getTextLocales() {
        throw new RuntimeException("Stub!");
    }

    public void setTextLocale(@NonNull Locale locale) {
        throw new RuntimeException("Stub!");
    }

    public void setTextLocales(@NonNull LocaleList locales) {
        throw new RuntimeException("Stub!");
    }

    public boolean isElegantTextHeight() {
        throw new RuntimeException("Stub!");
    }

    public void setElegantTextHeight(boolean elegant) {
        throw new RuntimeException("Stub!");
    }

    public float getTextSize() {
        throw new RuntimeException("Stub!");
    }

    public void setTextSize(float textSize) {
        throw new RuntimeException("Stub!");
    }

    public float getTextScaleX() {
        throw new RuntimeException("Stub!");
    }

    public void setTextScaleX(float scaleX) {
        throw new RuntimeException("Stub!");
    }

    public float getTextSkewX() {
        throw new RuntimeException("Stub!");
    }

    public void setTextSkewX(float skewX) {
        throw new RuntimeException("Stub!");
    }

    public float getLetterSpacing() {
        throw new RuntimeException("Stub!");
    }

    public void setLetterSpacing(float letterSpacing) {
        throw new RuntimeException("Stub!");
    }

    public float getWordSpacing() {
        throw new RuntimeException("Stub!");
    }

    public void setWordSpacing(float wordSpacing) {
        throw new RuntimeException("Stub!");
    }

    public String getFontFeatureSettings() {
        throw new RuntimeException("Stub!");
    }

    public void setFontFeatureSettings(String settings) {
        throw new RuntimeException("Stub!");
    }

    public String getFontVariationSettings() {
        throw new RuntimeException("Stub!");
    }

    public boolean setFontVariationSettings(String fontVariationSettings) {
        throw new RuntimeException("Stub!");
    }

    public int getStartHyphenEdit() {
        throw new RuntimeException("Stub!");
    }

    public int getEndHyphenEdit() {
        throw new RuntimeException("Stub!");
    }

    public void setStartHyphenEdit(int startHyphen) {
        throw new RuntimeException("Stub!");
    }

    public void setEndHyphenEdit(int endHyphen) {
        throw new RuntimeException("Stub!");
    }

    public float ascent() {
        throw new RuntimeException("Stub!");
    }

    public float descent() {
        throw new RuntimeException("Stub!");
    }

    public float getFontMetrics(FontMetrics metrics) {
        throw new RuntimeException("Stub!");
    }

    public FontMetrics getFontMetrics() {
        throw new RuntimeException("Stub!");
    }

    public int getFontMetricsInt(FontMetricsInt fmi) {
        throw new RuntimeException("Stub!");
    }

    public FontMetricsInt getFontMetricsInt() {
        throw new RuntimeException("Stub!");
    }

    public float getFontSpacing() {
        throw new RuntimeException("Stub!");
    }

    public float measureText(char[] text, int index, int count) {
        throw new RuntimeException("Stub!");
    }

    public float measureText(String text, int start, int end) {
        throw new RuntimeException("Stub!");
    }

    public float measureText(String text) {
        throw new RuntimeException("Stub!");
    }

    public float measureText(CharSequence text, int start, int end) {
        throw new RuntimeException("Stub!");
    }

    public int breakText(char[] text, int index, int count, float maxWidth, float[] measuredWidth) {
        throw new RuntimeException("Stub!");
    }

    public int breakText(CharSequence text, int start, int end, boolean measureForwards, float maxWidth, float[] measuredWidth) {
        throw new RuntimeException("Stub!");
    }

    public int breakText(String text, boolean measureForwards, float maxWidth, float[] measuredWidth) {
        throw new RuntimeException("Stub!");
    }

    public int getTextWidths(char[] text, int index, int count, float[] widths) {
        throw new RuntimeException("Stub!");
    }

    public int getTextWidths(CharSequence text, int start, int end, float[] widths) {
        throw new RuntimeException("Stub!");
    }

    public int getTextWidths(String text, int start, int end, float[] widths) {
        throw new RuntimeException("Stub!");
    }

    public int getTextWidths(String text, float[] widths) {
        throw new RuntimeException("Stub!");
    }

    public float getTextRunAdvances(@NonNull char[] chars, int index, int count, int contextIndex, int contextCount, boolean isRtl, @Nullable float[] advances, int advancesIndex) {
        throw new RuntimeException("Stub!");
    }

    public int getTextRunCursor(@NonNull char[] text, int contextStart, int contextLength, boolean isRtl, int offset, int cursorOpt) {
        throw new RuntimeException("Stub!");
    }

    public int getTextRunCursor(@NonNull CharSequence text, int contextStart, int contextEnd, boolean isRtl, int offset, int cursorOpt) {
        throw new RuntimeException("Stub!");
    }

    public void getTextPath(char[] text, int index, int count, float x, float y, Path path) {
        throw new RuntimeException("Stub!");
    }

    public void getTextPath(String text, int start, int end, float x, float y, Path path) {
        throw new RuntimeException("Stub!");
    }

    public void getTextBounds(String text, int start, int end, Rect bounds) {
        throw new RuntimeException("Stub!");
    }

    public void getTextBounds(@NonNull CharSequence text, int start, int end, @NonNull Rect bounds) {
        throw new RuntimeException("Stub!");
    }

    public void getTextBounds(char[] text, int index, int count, Rect bounds) {
        throw new RuntimeException("Stub!");
    }

    public boolean hasGlyph(String string) {
        throw new RuntimeException("Stub!");
    }

    public float getRunAdvance(char[] text, int start, int end, int contextStart, int contextEnd, boolean isRtl, int offset) {
        throw new RuntimeException("Stub!");
    }

    public float getRunAdvance(CharSequence text, int start, int end, int contextStart, int contextEnd, boolean isRtl, int offset) {
        throw new RuntimeException("Stub!");
    }

    public int getOffsetForAdvance(char[] text, int start, int end, int contextStart, int contextEnd, boolean isRtl, float advance) {
        throw new RuntimeException("Stub!");
    }

    public int getOffsetForAdvance(CharSequence text, int start, int end, int contextStart, int contextEnd, boolean isRtl, float advance) {
        throw new RuntimeException("Stub!");
    }

    public boolean equalsForTextMeasurement(@NonNull Paint other) {
        throw new RuntimeException("Stub!");
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
            throw new RuntimeException("Stub!");
        }

        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    public static class FontMetrics {
        public float ascent;
        public float bottom;
        public float descent;
        public float leading;
        public float top;

        public FontMetrics() {
            throw new RuntimeException("Stub!");
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
