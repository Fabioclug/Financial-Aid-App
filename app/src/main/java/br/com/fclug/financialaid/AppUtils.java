package br.com.fclug.financialaid;

import android.app.Application;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Fabioclug on 2016-07-29.
 */
public final class AppUtils extends Application {

    public static int dpToPixels(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getApplicationContext().getResources().getDisplayMetrics());
    }

    public static double roundValue(double value) {
        DecimalFormat twoDForm = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));
        return Double.valueOf(twoDForm.format(value));
    }

    public static View getViewByPosition(int pos, ListView listView, int headerItems) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos + headerItems, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex + headerItems);
        }
    }

    public static void setTime(Calendar date, int hours, int minutes, int seconds, int milliseconds) {
        date.set(Calendar.HOUR_OF_DAY, hours);
        date.set(Calendar.MINUTE, minutes);
        date.set(Calendar.SECOND, seconds);
        date.set(Calendar.MILLISECOND, milliseconds);
    }

    public static int getAccountTypeIndex(Context context, String type) {
        String[] accountTypes = context.getApplicationContext().getResources().getStringArray(R.array.account_types);
        int result = -1;
        for(int i = 0; i < accountTypes.length; i++) {
            if(accountTypes[i].equals(type)) {
                result = i;
                break;
            }
        }
        return result;
    }
}
