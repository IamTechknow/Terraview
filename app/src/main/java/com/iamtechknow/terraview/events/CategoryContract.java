package com.iamtechknow.terraview.events;

import com.iamtechknow.terraview.model.EventCategory;

import java.util.ArrayList;

public interface CategoryContract {
    interface View {
        void insertList(ArrayList<EventCategory> list);
    }

    interface Presenter {
        void detachView();

        void loadCategories();

        void emitEvent(int catId);
    }
}
