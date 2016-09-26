package com.iamtechknow.terraview.anim;

import com.iamtechknow.terraview.map.WorldPresenter;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.co.ribot.androidboilerplate.util.RxSchedulersOverrideRule;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AnimPresenterTest {
    //Needed to allow mocking of AndroidSchedulers.mainThread()
    @Rule
    public RxSchedulersOverrideRule rule = new RxSchedulersOverrideRule();

    @Mock
    private AnimView view;

    private WorldPresenter presenter;

    @Before
    public void setup() {
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
