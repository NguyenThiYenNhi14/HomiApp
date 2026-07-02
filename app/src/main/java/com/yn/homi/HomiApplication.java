package com.yn.homi;

import android.app.Application;
import com.yn.homi.utils.AppSessionTracker;

public class HomiApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new AppSessionTracker());
    }
}
