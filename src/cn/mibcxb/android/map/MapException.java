package cn.mibcxb.android.map;

public class MapException extends Exception {
    private static final long serialVersionUID = 2828837751150567798L;

    public MapException() {
        super();
    }

    public MapException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public MapException(String detailMessage) {
        super(detailMessage);
    }

    public MapException(Throwable throwable) {
        super(throwable);
    }

}
