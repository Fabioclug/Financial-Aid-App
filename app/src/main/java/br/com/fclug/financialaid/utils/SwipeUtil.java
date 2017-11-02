package br.com.fclug.financialaid.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import br.com.fclug.financialaid.R;

/**
 * Created by Fabioclug on 2017-09-02.
 *
 * This class is used to draw the layout when swiping a list row
 *
 * Credits: https://github.com/hdpavan/RecyclerViewItemTouchHelperSwipe
 */

public abstract class SwipeUtil extends ItemTouchHelper.SimpleCallback {
    private Drawable background;
    private Drawable deleteIcon;
    private Drawable editIcon;
    private Paint mRemoveText;

    private int xMarkMargin;

    private boolean initiated;
    private Context mContext;

    private int mLeftColorCode;
    private int mRightColorCode;
    private String leftSwipeLabel;

    private RectF mLeftButtonArea;

    public SwipeUtil(Context context) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        mContext = context;
    }


    public SwipeUtil(int dragDirs, int swipeDirs, Context context) {
        super(dragDirs, swipeDirs);
        mContext = context;
    }


    private void init() {

        TypedValue typedValue = new TypedValue();
        mContext.getTheme().resolveAttribute(android.R.attr.windowBackground, typedValue, true);
        int color = typedValue.data;

        background = new ColorDrawable();
        xMarkMargin = (int) mContext.getResources().getDimension(R.dimen.swipe_right_margin);

        //Setting Left Swipe Icon
        deleteIcon = ContextCompat.getDrawable(mContext, R.drawable.ic_delete).mutate();
        deleteIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        //Setting Right Swipe Icon
        editIcon = ContextCompat.getDrawable(mContext, R.drawable.ic_edit).mutate();
        editIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        //Setting Swipe Text
        mRemoveText = new Paint();
        mRemoveText.setColor(color);
        float scaledSizeInPixels = mContext.getResources().getDimensionPixelSize(R.dimen.swipe_text_size);
        mRemoveText.setTextSize(scaledSizeInPixels);
        mRemoveText.setTextAlign(Paint.Align.CENTER);

        initiated = true;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public abstract void onSwiped(RecyclerView.ViewHolder viewHolder, int direction);

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            View itemView = viewHolder.itemView;

            if (!initiated) {
                init();
            }

            int itemHeight = itemView.getBottom() - itemView.getTop();

            if (dX < 0) {

                //Setting Swipe Background
                ((ColorDrawable) background).setColor(mLeftColorCode);
                background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);

                int intrinsicWidth = deleteIcon.getIntrinsicWidth();
                int intrinsicHeight = deleteIcon.getIntrinsicHeight();

                int xMarkLeft = itemView.getRight() - xMarkMargin - intrinsicWidth;
                int xMarkRight = itemView.getRight() - xMarkMargin;
                int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                int xMarkBottom = xMarkTop + intrinsicHeight;


                //Draw Swipe Icon
                deleteIcon.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);
                deleteIcon.draw(c);

                //Draw Swipe Text
                c.drawText(getLeftSwipeLabel(), xMarkLeft - 140, itemView.getTop() + itemHeight / 2 - (mRemoveText.descent() + mRemoveText.ascent()) / 2, mRemoveText);

            } else if (dX > 0) {
                ((ColorDrawable) background).setColor(mRightColorCode);
                background.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + (int) dX, itemView.getBottom());
                mLeftButtonArea = new RectF(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + (int) dX, itemView.getBottom());
                background.draw(c);

                int intrinsicWidth = editIcon.getIntrinsicWidth();
                int intrinsicHeight = editIcon.getIntrinsicHeight();

                int xMarkLeft = itemView.getLeft() + xMarkMargin;
                int xMarkRight = itemView.getLeft() + xMarkMargin + intrinsicWidth;
                int xMarkTop = itemView.getTop() + (itemView.getHeight() - intrinsicHeight) / 2;
                int xMarkBottom = xMarkTop + intrinsicHeight;

                editIcon.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);
                editIcon.draw(c);

                c.drawText("Edit", xMarkLeft + intrinsicWidth + 100, itemView.getTop() + itemHeight / 2 - (mRemoveText.descent() + mRemoveText.ascent()) / 2, mRemoveText);

                //setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    public void setTouchListener(final Canvas c, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final float dX, final float dY, final int actionState, final boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (mLeftButtonArea != null && mLeftButtonArea.contains(event.getX(), event.getY())) {
                        int position = viewHolder.getAdapterPosition();
                        onLeftClick(position);
                    }
                }
                return false;
            }
        });
    }

    public abstract void onLeftClick(int position);

    public String getLeftSwipeLabel() {
        return leftSwipeLabel;
    }

    public void setLeftSwipeLabel(String leftSwipeLabel) {
        this.leftSwipeLabel = leftSwipeLabel;
    }

    public void setLeftSwipeColor(int leftColorCode) {
        mLeftColorCode = leftColorCode;
    }

    public void setRightColorCode(int rightColorCode) {
        mRightColorCode = rightColorCode;
    }
}
