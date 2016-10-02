package io.lostinreality.lir_android_app;

import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ProgressBar;

/**
 * Created by jose on 30/08/16.
 */
public class LayoutVisibilityAnimation extends Animation {
    private View view;
    private int visibility;

    public LayoutVisibilityAnimation(View view, int visibility) {
        super();
        this.view = view;
        this.visibility = visibility;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        view.setVisibility(visibility);
    }
}
