package com.iamtechknow.terraview.anim;

import com.iamtechknow.terraview.map.WorldPresenter;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.schedulers.Schedulers;

import static org.mockito.Mockito.verify;

public class AnimPresenterTest {
    @Mock
    private AnimView view;

    private WorldPresenter presenter;

    @BeforeClass
    public void setupClass() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(__ -> Schedulers.trampoline());
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        presenter = new WorldPresenter();
        presenter.attachView(view);
    }

    @Test
    public void showDialogTest() {
        //When user chooses new animation menu option
        presenter.newAnimation();

        //Presenter tells view to show dialog
        verify(view).showAnimDialog();
    }

    @Test
    public void animationReadyTest() {
        //When dialog has been filled and presenter receives result
        presenter.setAnimation("2016-09-01", "2016-09-05", 0, 30, false);

        //Presenter tells view to show the play button
        verify(view).setAnimButton(true);
    }

    @Test
    public void runAnimationThenStop() {
        //Create and run an animation
        presenter.setAnimation("2016-09-01", "2016-09-05", 0, 30, false);
        presenter.run();

        //Done manually because RxJava subscription won't run
        presenter.onNextFrame();
        presenter.onNextFrame();
        presenter.onNextFrame();
        presenter.onNextFrame();
        presenter.onNextFrame();
        presenter.stop(true);

        //Verify when animation stops, play button is hidden
        verify(view).setAnimButton(false);
    }
}
