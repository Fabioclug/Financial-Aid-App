package br.com.fclug.financialaid.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.ColorInt;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.github.danielnilsson9.colorpickerview.view.ColorPanelView;
import com.github.danielnilsson9.colorpickerview.view.ColorPickerView;

import br.com.fclug.financialaid.R;
import br.com.fclug.financialaid.database.CategoryDao;
import br.com.fclug.financialaid.models.Category;

/**
 * Created by Fabioclug on 2017-03-04.
 */

public class AddCategoryDialog extends Dialog implements ColorPickerView.OnColorChangedListener, View.OnClickListener {

    private Context mContext;

    @ColorInt
    private int mSelectedColor;

    private EditText mCategoryName;
    private ColorPickerView mColorPicker;
    private ColorPanelView mColorViewer;
    private Button mConfirmCategoryButton;
    private RadioGroup mTypeRadioGroup;

    public AddCategoryDialog(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.add_category_layout);
        mColorPicker.setOnColorChangedListener(this);
        mSelectedColor = mColorPicker.getColor();
        mConfirmCategoryButton.setOnClickListener(this);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        mCategoryName = (EditText) findViewById(R.id.add_category_name);
        mColorPicker = (ColorPickerView) findViewById(R.id.add_category_color_picker);
        mColorViewer = (ColorPanelView) findViewById(R.id.add_category_color_viewer);
        mTypeRadioGroup = (RadioGroup) findViewById(R.id.add_category_type_radio_group);
        mConfirmCategoryButton = (Button) findViewById(R.id.add_category_button);
    }

    @Override
    public void onColorChanged(int newColor) {
        mColorViewer.setColor(newColor);
        mSelectedColor = newColor;
    }

    @Override
    public void onClick(View v) {
        CategoryDao categoryDao = new CategoryDao(mContext);
        int selectedTypeId = mTypeRadioGroup.getCheckedRadioButtonId();
        View selectedTypeView = mTypeRadioGroup.findViewById(selectedTypeId);
        int selectedTypeIndex = mTypeRadioGroup.indexOfChild(selectedTypeView);
        boolean incoming = true;
        if (selectedTypeIndex == 1) {
            incoming = false;
        }

        Category newCategory = new Category(mCategoryName.getText().toString(), mSelectedColor, incoming);
        categoryDao.save(newCategory);
        Toast.makeText(mContext, "Category created!", Toast.LENGTH_LONG).show();
        dismiss();
    }
}
