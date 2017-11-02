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
import br.com.fclug.financialaid.database.FinancialAppContract.CategoryTable;

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
        contentValues.put(CategoryTable.COLUMN_NAME, category.getName());
        contentValues.put(CategoryTable.COLUMN_COLOR, category.getColor());
        contentValues.put(CategoryTable.COLUMN_TYPE, category.isIncoming());
        long result = mDbHandler.getWritableDatabase()
                                .insert(CategoryTable.TABLE_NAME, null, contentValues);
        return result > 0;
    }

    private static boolean save(SQLiteDatabase db, Category category) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CategoryTable.COLUMN_NAME, category.getName());
        contentValues.put(CategoryTable.COLUMN_COLOR, category.getColor());
        contentValues.put(CategoryTable.COLUMN_TYPE, category.isIncoming());
        long result = db.insert(CategoryTable.TABLE_NAME, null, contentValues);
        return result > 0;
    }

    public static boolean insertDefaultValues(Context context, SQLiteDatabase db) {
        boolean result = true;
        Resources res = context.getResources();
        String[] incomingCategoryNames = res.getStringArray(R.array.incoming_category_names);
        int[] incomingCategoryColors = res.getIntArray(R.array.incoming_category_colors);
        String[] outgoingCategoryNames = res.getStringArray(R.array.outgoing_category_names);
        int[] outgoingCategoryColors = res.getIntArray(R.array.outgoing_category_colors);

        for(int i = 0; i < incomingCategoryNames.length; i++) {
            Category category = new Category(incomingCategoryNames[i], incomingCategoryColors[i], true);
            result &= save(db, category);
        }

        for(int i = 0; i < outgoingCategoryNames.length; i++) {
            Category category = new Category(outgoingCategoryNames[i], outgoingCategoryColors[i], false);
            result &= save(db, category);
        }
        return result;
    }

    public List<Category> find(boolean incoming) {
        List<Category> categories = new ArrayList<>();
        String whereClause = CategoryTable.COLUMN_TYPE + " = ?";
        String[] whereArgs = new String[] {incoming? "1" : "0"};
        Cursor cursor = mDbHandler.getReadableDatabase()
                .query(CategoryTable.TABLE_NAME, null, whereClause, whereArgs, null, null, null);
        if(cursor.moveToFirst()) {
            while(!cursor.isAfterLast()) {
                String name = cursor.getString(cursor.getColumnIndex(CategoryTable.COLUMN_NAME));
                int color = cursor.getInt(cursor.getColumnIndex(CategoryTable.COLUMN_COLOR));
                Category category = new Category(name, color, incoming);
                categories.add(category);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return categories;
    }

    public List<Category> findIncoming() {
        return find(true);
    }

    public List<Category> findOutgoing() {
        return find(false);
    }

    public Category findByName(String name) {
        String whereClause = CategoryTable.COLUMN_NAME + " = ?";
        String[] whereArgs = new String[] {name};

        Category category = null;
        Cursor cursor = mDbHandler.getReadableDatabase()
                                  .query(CategoryTable.TABLE_NAME, null, whereClause, whereArgs, null, null, null);
        if(cursor.moveToFirst()) {
            int color = cursor.getInt(cursor.getColumnIndex(FinancialAppContract.CategoryTable.COLUMN_COLOR));
            boolean incoming = cursor.getInt(cursor.getColumnIndex(FinancialAppContract.CategoryTable.COLUMN_TYPE)) > 0;
            category = new Category(name, color, incoming);
        }
        return category;
    }

    public boolean delete(Category category) {
        String id = category.getName();
        String whereClause = CategoryTable.COLUMN_NAME + " = ?";
        String[] whereArgs = new String[] { String.valueOf(id) };
        int result = mDbHandler.getWritableDatabase().delete(CategoryTable.TABLE_NAME, whereClause, whereArgs);
        return result > 0;
    }
}
