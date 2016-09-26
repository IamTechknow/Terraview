package com.iamtechknow.terraview.anim;

import android.os.Bundle;

public interface AnimPresenter {
    void attachView(AnimView v);

    void detachView();

    void newAnimation();

    void setAnimation(String start, String end, int interval, int speed, boolean loop);

    Bundle getAnimationSettings();

    void run();

    boolean isRunning();

    void stop(boolean terminate);
}
