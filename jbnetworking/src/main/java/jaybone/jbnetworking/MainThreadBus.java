package jaybone.jbnetworking;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

/**
 * A bus that runs on the main thread. For taking requests from outside of the main thread.
 * http://stackoverflow.com/questions/26692240/thread-safety-background-thread-and-cache-avoid-race-conditions
 * Created by Justin on 9/3/15.
 */
// the bus class
public class MainThreadBus {
    private final Bus mBus;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public MainThreadBus(final Bus bus) {
        if (bus == null)
            throw new NullPointerException("ERROR: bus == null");
        mBus = bus;
    }

    public void register(Object obj) {
        mBus.register(obj);
    }

    public void unregister(Object obj) {
        mBus.unregister(obj);
    }

    public void post(final Object event) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            mBus.post(event);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mBus.post(event);
                }
            });
        }
    }
}