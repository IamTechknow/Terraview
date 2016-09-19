package com.iamtechknow.worldview.anim;

import android.os.Bundle;

public interface AnimPresenter {
    void setAnimation(String start, String end, int interval, int speed, boolean loop);

    Bundle getAnimationSettings();

    void run();

    boolean isRunning();

    void stop(boolean terminate);
}
