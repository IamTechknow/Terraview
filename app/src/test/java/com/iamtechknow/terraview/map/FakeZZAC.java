package com.iamtechknow.terraview.map;

import android.os.IBinder;
import android.os.RemoteException;

import com.google.android.gms.internal.maps.zzac;

/**
 * A fake object used only for mocking and testing purposes to create a TileProvider.
 * This runs on an unit testing JVM and not on Android
 */
public class FakeZZAC implements zzac {
    @Override
    public void remove() {}

    @Override
    public void clearTileCache() {}

    @Override
    public String getId() {
        return null;
    }

    @Override
    public void setZIndex(float v) {}

    @Override
    public float getZIndex() {
        return 0;
    }

    @Override
    public void setVisible(boolean b) {}

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public boolean zza(zzac zzac) {
        return false;
    }

    @Override
    public int zzi() {
        return 0;
    }

    @Override
    public void setFadeIn(boolean b) {}

    @Override
    public boolean getFadeIn() {
        return false;
    }

    @Override
    public void setTransparency(float v) {}

    @Override
    public float getTransparency() {
        return 0;
    }

    @Override
    public IBinder asBinder() {
        return null;
    }
}
