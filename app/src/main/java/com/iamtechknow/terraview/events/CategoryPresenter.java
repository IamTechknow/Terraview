package com.iamtechknow.terraview.events;

public interface CategoryPresenter {
    void detachView();

    void loadCategories();

    void emitEvent(int catId);
}
