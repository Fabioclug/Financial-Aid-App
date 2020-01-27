package br.com.fclug.financialaid.utils;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

/**
 * Created by Fabioclug on 2017-08-22.
 *
 * Used to show or hide a FloatingActionButton according to the ListView scrolling action
 */

public class FabScrollBehavior implements View.OnTouchListener {

    private FloatingActionButton mFab;
    private float initialY, finalY;

    public FabScrollBehavior(FloatingActionButton fab) {
        mFab = fab;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ListView listView = (ListView) v;
        int action = MotionEventCompat.getActionMasked(event);

        switch(action) {
            case (MotionEvent.ACTION_DOWN):
                initialY = event.getY();
            case (MotionEvent.ACTION_UP):
                finalY = event.getY();
                if (initialY < finalY && listView.canScrollList(MotionEvent.ACTION_UP)) {
                    // scrolling up
                    mFab.hide();
                } else if (initialY > finalY) {
                    // scrolling down
                    mFab.show();
                }
            default:
        }

        return false;
    }
}
