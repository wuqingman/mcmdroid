package cn.mibcxb.android;

public final class McMath {
    public static final int clip(final int n, final int min, final int max) {
        return Math.min(Math.max(n, min), max);
    }

    public static final double clip(final double n, final double min,
            final double max) {
        return Math.min(Math.max(n, min), max);
    }

    private McMath() {
    }
}
