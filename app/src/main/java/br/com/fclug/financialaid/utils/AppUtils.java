package br.com.fclug.financialaid.utils;

import android.app.Application;
import android.app.DatePickerDialog;
import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import br.com.fclug.financialaid.R;

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

    public static String handleValueInput(EditText editText) {
        String text = editText.getText().toString();
        String result = null;
        if(!text.matches("^(\\d+)(\\.\\d{2})$")) {
            int originalCursorPosition = editText.getSelectionStart();
            int cursorOffset = 0;

            boolean cursorAtEnd = originalCursorPosition == text.length();

            String userInput= ""+text.replaceAll("[^\\d]", "");
            StringBuilder cashAmountBuilder = new StringBuilder(userInput);

            while (cashAmountBuilder.length() > 3 && cashAmountBuilder.charAt(0) == '0')           {
                cashAmountBuilder.deleteCharAt(0);
                cursorOffset--;
            }
            while (cashAmountBuilder.length() < 3) {
                cashAmountBuilder.insert(0, '0');
                cursorOffset++;
            }
            cashAmountBuilder.insert(cashAmountBuilder.length() - 2, '.');
            result = cashAmountBuilder.toString();

            editText.setText(result);
            editText.setSelection(cursorAtEnd ? editText.getText().length() : originalCursorPosition + cursorOffset);
        }
        return result;
    }

    public static long extractCurrencyValue(String moneyNotationValue) {
        Log.d("extractCurrencyValue", moneyNotationValue);
        String[] parts = moneyNotationValue.split("\\.");
        long dollars = Long.parseLong(parts[0]) * 100;
        int cents = Integer.parseInt(parts[1]);
        if(parts[1].length() == 1) {
            cents *= 10;
        }
        return dollars + cents;
    }

    public static String formatEditableCurrencyValue(long valueInCents) {
        return String.valueOf(valueInCents / 100.0);
    }

    public static String formatCurrencyValue(long valueInCents) {
        Log.d("formatCurrencyValue", "value: " + valueInCents);
        return NumberFormat.getCurrencyInstance().format(valueInCents / 100.0); //String.format("%.2f", value).replace(".", ",");
    }

    public static void attachCalendarToEditText(final Context context, final EditText editText, final SimpleDateFormat formatter) {
        final Calendar myCalendar = Calendar.getInstance();
        editText.setFocusable(false);

        // build the calendar to pick a date
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                editText.setText(formatter.format(myCalendar.getTime()));

            }

        };

        // show the calendar when the field is clicked
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(context, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }
}
