package br.com.fclug.financialaid.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import br.com.fclug.financialaid.R;
import br.com.fclug.financialaid.models.Category;

/**
 * Created by Fabioclug on 2017-01-08.
 */

public class CategoryDao {

    private Context mContext;
    private DatabaseHandler mDbHandler;

    public CategoryDao(Context context) {
        mContext = context;
        mDbHandler = new DatabaseHandler(context);
    }

    public boolean save(Category category) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", category.getName());
        contentValues.put("color", category.getColor());
        long result = mDbHandler.getWritableDatabase().insert(DatabaseHandler.CATEGORY_TABLE, null, contentValues);
        return result > 0;
    }

    public static boolean insertDefaultValues(Context context, SQLiteDatabase db) {
        boolean result = true;
        Resources res = context.getResources();
        String[] categoryNames = res.getStringArray(R.array.category_names);
        int[] categoryColors = res.getIntArray(R.array.category_colors);

        for(int i = 0; i < categoryNames.length; i++) {
            Category category = new Category(categoryNames[i], categoryColors[i]);
            ContentValues contentValues = new ContentValues();
            contentValues.put("name", category.getName());
            contentValues.put("color", category.getColor());
            long returnCode = db.insert(DatabaseHandler.CATEGORY_TABLE, null, contentValues);
            result &= (returnCode > 0);
        }
        return result;
    }

    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();
        Cursor cursor = mDbHandler.getReadableDatabase().rawQuery("SELECT * FROM " + DatabaseHandler.CATEGORY_TABLE, null);
        if(cursor.moveToFirst()) {
            while(!cursor.isAfterLast()) {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                int color = cursor.getInt(cursor.getColumnIndex("color"));
                Category category = new Category(name, color);
                categories.add(category);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return categories;
    }

    public Category findByName(String name) {
        String whereClause = "name = ?";
        String[] whereArgs = new String[] {name};

        Category category = null;
        Cursor cursor = mDbHandler.getReadableDatabase().query(DatabaseHandler.CATEGORY_TABLE, null, whereClause, whereArgs, null, null, null);
        if(cursor.moveToFirst()) {
            int color = cursor.getInt(cursor.getColumnIndex("color"));
            category = new Category(name, color);
        }
        return category;
    }

    public boolean delete(Category category) {
        String id = category.getName();
        String whereClause = "name = ?";
        String[] whereArgs = new String[] { String.valueOf(id) };
        int result = mDbHandler.getWritableDatabase().delete(DatabaseHandler.CATEGORY_TABLE, whereClause, whereArgs);
        return result > 0;
    }
}
