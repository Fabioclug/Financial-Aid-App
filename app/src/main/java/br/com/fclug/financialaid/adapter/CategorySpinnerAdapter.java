package br.com.fclug.financialaid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import br.com.fclug.financialaid.R;
import br.com.fclug.financialaid.models.Category;

/**
 * Created by Fabioclug on 2017-01-21.
 */

public class CategorySpinnerAdapter extends BaseAdapter {

    private Context mContext;
    private List<Category> mCategories;

    public CategorySpinnerAdapter(Context context, List<Category> categories) {
        mContext = context;
        mCategories = categories;
    }

    public void setCategories(List<Category> categories) {
        mCategories = categories;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mCategories.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        return mCategories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(position < mCategories.size()) {
            Category category = mCategories.get(position);
            convertView = inflater.inflate(R.layout.category_select_spinner_row, parent, false);

            TextView categoryName = (TextView) convertView.findViewById(R.id.category_name);
            View categoryColor = convertView.findViewById(R.id.category_color);

            categoryName.setText(category.getName());
            categoryColor.setBackgroundColor(category.getColor());
        } else {
            convertView = inflater.inflate(R.layout.support_simple_spinner_dropdown_item, parent, false);
            TextView view = (TextView) convertView;
            view.setText(R.string.add_category);
//            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
//            view.setTextColor(ContextCompat.getColor(mContext, R.color.divider_color));
        }

        return convertView;
    }
}
