package jaybone.jbnetworking;

/**
 * The custom Application subclass to enable Otto's bus stuff / set up context for the App.
 *
 * If you wish to use this library, you'll want your applications 'App' class to inherit from JayboneApp.
 *
 * Created by Justin on 8/4/14.
 */

import android.app.Application;
import android.content.Context;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public class JayboneApp extends Application {

    /**
     * This is the bus that everyone should post to and read from. This will be alive as long as the app is alive.
     */
    private static final MainThreadBus apiBus = new MainThreadBus(new Bus(ThreadEnforcer.MAIN));

    private static Context mContextRef;

    /**
     * Update: This is thread safe I think.
     * @return The shared app bus.
     */
    public static MainThreadBus getBus() {
        return apiBus;
    }

    /**
     * Returns the static application context.
     * @return The static app context.
     */
    public static Context getAppContext() {
        return mContextRef;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContextRef = getApplicationContext();
    }
}
