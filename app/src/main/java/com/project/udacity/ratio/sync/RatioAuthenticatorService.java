package com.project.udacity.ratio.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by mehseti on 8.8.2018.
 */

public class RatioAuthenticatorService extends Service {
    private RatioAuthenticator ratioAuthenticator;

    @Override
    public void onCreate() {
        ratioAuthenticator = new RatioAuthenticator(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return ratioAuthenticator.getIBinder();
    }
}
