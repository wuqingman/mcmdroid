/**
 * 
 */
package cn.mibcxb.android.os;

import java.lang.ref.SoftReference;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * @author mibcxb
 *
 */
public class McHandler<T> extends Handler {

    private final SoftReference<T> reference;

    public McHandler(T r) {
        this.reference = new SoftReference<T>(r);
    }

    public McHandler(T r, Callback callback) {
        super(callback);
        this.reference = new SoftReference<T>(r);
    }

    public McHandler(T r, Looper looper) {
        super(looper);
        this.reference = new SoftReference<T>(r);
    }

    public McHandler(T r, Looper looper, Callback callback) {
        super(looper, callback);
        this.reference = new SoftReference<T>(r);
    }

    @Override
    public final void handleMessage(Message msg) {
        T r = reference.get();
        if (r != null) {
            handleMessage(r, msg);
        }
    }

    public void handleMessage(T r, Message msg) {
    }

}
