package br.com.fclug.financialaid.interfaces;

import android.view.View;

/**
 * Created by Fabioclug on 2017-09-02.
 */

public interface OnListClickListener {
    void onItemClick(Object o, int position);
    void onItemLongClick(View v, Object o);
}
