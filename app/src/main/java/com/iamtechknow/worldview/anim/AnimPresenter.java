package com.iamtechknow.worldview.anim;

public interface AnimPresenter {
    void setAnimation(String start, String end, int interval, int speed, boolean loop);

    void run();

    boolean isRunning();

    void stop();
}
