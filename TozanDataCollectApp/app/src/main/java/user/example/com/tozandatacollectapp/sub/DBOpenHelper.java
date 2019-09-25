package user.example.com.tozandatacollectapp.sub;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class DBOpenHelper extends SQLiteOpenHelper {

    // データーベースのバージョン
    private static final int DATABASE_VERSION = 1;

    // データーベース情報を変数に格納
    private static final String
            DATABASE_NAME = "Mountain.db",   //データベースファイルの名前
            TABLE_NAME_CATEGORIES = "PrefCategoriesTable", //地方名テーブル
            TABLE_NAME_PREFECTURES = "PrefecturesTable", //各都道府県の名前と所属地方のテーブル
            TABLE_NAME_MOUNTAINS = "MountainsTable", //各山の名前と所属県、所属地方のテーブル
            COLUMN_NAME_CATEGORY_ID = "ColumnCategoryId",
            COLUMN_NAME_CATEGORY_NAME = "ColumnCategoryName",
            COLUMN_NAME_PREF_ID = "ColumnPrefectureId",
            COLUMN_NAME_PREF_NAME = "ColumnPrefectureName",
            COLUMN_NAME_MOUNTAIN_ID = "ColumnMountainId",
            COLUMN_NAME_MOUNTAIN_NAME = "ColumnMountainName";

    //カテゴリテーブル作成
    private static final String SQL_CREATE_TABLE_CATEGORIES =
            String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s TEXT)",
                    //テーブル名
                    TABLE_NAME_CATEGORIES,
                    //列名
                    COLUMN_NAME_CATEGORY_ID,
                    COLUMN_NAME_CATEGORY_NAME
            );

    //県テーブル作成
    private static final String SQL_CREATE_TABLE_PREFECTURES =
            String.format("CREATE TABLE %s (%s INTEGER,%s INTEGER PRIMARY KEY,%s TEXT)",
                    //テーブル名
                    TABLE_NAME_PREFECTURES,
                    //列名
                    COLUMN_NAME_CATEGORY_ID,
                    COLUMN_NAME_PREF_ID,
                    COLUMN_NAME_PREF_NAME
            );

    //県テーブル作成
    private static final String SQL_CREATE_TABLE_MOUNTAINS =
            String.format("CREATE TABLE %s (%s INTEGER,%s INTEGER PRIMARY KEY,%s TEXT)",
                    //テーブル名
                    TABLE_NAME_MOUNTAINS,
                    //列名
                    COLUMN_NAME_PREF_ID,
                    COLUMN_NAME_MOUNTAIN_ID,
                    COLUMN_NAME_MOUNTAIN_NAME
            );

    private static final String SQL_DELETE_TABLE_CATEGORIES =
            String.format("DROP TABLE IF EXISTS %s", TABLE_NAME_CATEGORIES);

    private static final String SQL_DELETE_TABLE_PREFECTURES =
            String.format("DROP TABLE IF EXISTS %s", TABLE_NAME_PREFECTURES);

    private static final String SQL_DELETE_TABLE_MOUNTAIN =
            String.format("DROP TABLE IF EXISTS %s", TABLE_NAME_MOUNTAINS);

    public static final String SQL_SELECT_CATEGORIES =
            "SELECT * FROM " + TABLE_NAME_CATEGORIES;

    public static final String SQL_SELECT_MOUNTAINS =
            "SELECT * FROM " + TABLE_NAME_MOUNTAINS;

    private static final String SQL_SELECT_PREFS =
            "SELECT * FROM " + TABLE_NAME_PREFECTURES;

    public static final String SQL_SELECT_PREF_WITH_CATEGORY =
            "SELECT * FROM " + TABLE_NAME_PREFECTURES +
                    " WHERE " + COLUMN_NAME_CATEGORY_ID + " = %d";

    public static final String SQL_SELECT_MOUNTAIN_WITH_PREF =
            "SELECT * FROM " + TABLE_NAME_MOUNTAINS +
                    " WHERE " + COLUMN_NAME_PREF_ID + " = %d";

    public static final String SQL_FIND_MOUNTAIN_FROM_ID =
            "SELECT * FROM " + TABLE_NAME_CATEGORIES +
            " NATURAL JOIN " + TABLE_NAME_PREFECTURES +
            " NATURAL JOIN " + TABLE_NAME_MOUNTAINS +
            " WHERE " + COLUMN_NAME_MOUNTAIN_ID + " = %s";

    public DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_CATEGORIES);
        db.execSQL(SQL_CREATE_TABLE_PREFECTURES);
        db.execSQL(SQL_CREATE_TABLE_MOUNTAINS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_TABLE_CATEGORIES);
        db.execSQL(SQL_DELETE_TABLE_PREFECTURES);
        db.execSQL(SQL_DELETE_TABLE_MOUNTAIN);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void registerCategories(List<String> data){

        if(data.isEmpty()) return;

        //先頭の列名部分を捨てる
        data.remove(0);

        SQLiteDatabase db = getWritableDatabase();

        db.execSQL(SQL_DELETE_TABLE_CATEGORIES);
        db.execSQL(SQL_CREATE_TABLE_CATEGORIES);

        ContentValues values;

        db.beginTransaction();
        try {
            /** Insert等のDB操作 */
            for(String str : data) {
                String[] cateData = str.split(",");
                values = new ContentValues();
                values.put(COLUMN_NAME_CATEGORY_ID, Integer.parseInt(cateData[0]));
                values.put(COLUMN_NAME_CATEGORY_NAME, cateData[1]);
                db.insertWithOnConflict(TABLE_NAME_CATEGORIES, null, values, SQLiteDatabase.CONFLICT_ROLLBACK);
                Log.d(DBOpenHelper.class.getSimpleName(), "data");
            }
            db.setTransactionSuccessful();
        } catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            // トランザクション終了
            db.endTransaction();
        }

    }

    public void registerPrefectures(List<String> data){

        if(data.isEmpty()) return;

        //先頭の列名部分を捨てる
        data.remove(0);

        SQLiteDatabase db = getWritableDatabase();

        db.execSQL(SQL_DELETE_TABLE_PREFECTURES);
        db.execSQL(SQL_CREATE_TABLE_PREFECTURES);

        ContentValues values;

        db.beginTransaction();
        try {
            /** Insert等のDB操作 */
            for(String str : data) {
                String[] prefData = str.split(",");
                values = new ContentValues();
                values.put(COLUMN_NAME_CATEGORY_ID, Integer.parseInt(prefData[0]));
                values.put(COLUMN_NAME_PREF_ID, Integer.parseInt(prefData[1]));
                values.put(COLUMN_NAME_PREF_NAME, prefData[2]);
                db.insertWithOnConflict(TABLE_NAME_PREFECTURES, null, values, SQLiteDatabase.CONFLICT_ROLLBACK);
                Log.d(DBOpenHelper.class.getSimpleName(), "data");
            }
            db.setTransactionSuccessful();
        } catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            // トランザクション終了
            db.endTransaction();
        }

    }

    public void registerMountains(List<String> data){

        if(data.isEmpty()) return;

        //先頭の列名部分を捨てる
        data.remove(0);

        SQLiteDatabase db = getWritableDatabase();

        db.execSQL(SQL_DELETE_TABLE_MOUNTAIN);
        db.execSQL(SQL_CREATE_TABLE_MOUNTAINS);

        ContentValues values;

        db.beginTransaction();
            /** Insert等のDB操作 */
            for(String str : data) {
                try{
                    String[] mountainData = str.split(",");
                    if(mountainData.length < 3) continue;
                    values = new ContentValues();
                    values.put(COLUMN_NAME_PREF_ID, Integer.parseInt(mountainData[0]));
                    values.put(COLUMN_NAME_MOUNTAIN_ID, Integer.parseInt(mountainData[1]));
                    values.put(COLUMN_NAME_MOUNTAIN_NAME, mountainData[2]);
                    db.insertWithOnConflict(TABLE_NAME_MOUNTAINS, null, values, SQLiteDatabase.CONFLICT_ROLLBACK);
                    Log.d(DBOpenHelper.class.getSimpleName(), "data");
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }
            }

            db.setTransactionSuccessful();

            // トランザクション終了
            db.endTransaction();

    }

    public TreeMap<Integer, String> getCategories(){

        SQLiteDatabase db = getReadableDatabase();

        TreeMap<Integer, String> value = new TreeMap<>();

        Cursor cursor = db.rawQuery(SQL_SELECT_CATEGORIES, null);
        cursor.moveToFirst();

        int cId;
        String cName;
        for (int i = 0; i < cursor.getCount(); i++) {
            cId = cursor.getInt(0);
            cName = cursor.getString(1);
            value.put(cId, cName);
            cursor.moveToNext();
        }

        return value;
    }

    public TreeMap<Integer, String> getCategoryPref(Integer cId){

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(String.format(SQL_SELECT_PREF_WITH_CATEGORY, cId), null);
        cursor.moveToFirst();

        TreeMap<Integer, String> prefs = new TreeMap<>();

        int pId;
        String pName;
        for (int i = 0; i < cursor.getCount(); i++) {
            pId = cursor.getInt(1);
            pName = cursor.getString(2);
            prefs.put(pId, pName);
            cursor.moveToNext();
        }

        return prefs;
    }

    public TreeMap<Integer, TreeMap<Integer, String>> getCategorizedPrefList(){

        SQLiteDatabase db = getReadableDatabase();

        TreeMap<Integer, TreeMap<Integer, String>> value = new TreeMap<>();

        for(Integer cId : getCategories().keySet()){
            Cursor cursor = db.rawQuery(String.format(SQL_SELECT_PREF_WITH_CATEGORY, cId), null);
            cursor.moveToFirst();

            TreeMap<Integer, String> pref = new TreeMap<>();

            int pId;
            String pName;
            for (int i = 0; i < cursor.getCount(); i++) {
                pId = cursor.getInt(1);
                pName = cursor.getString(2);
                pref.put(pId, pName);
                cursor.moveToNext();
            }

            value.put(cId, pref);
        }

        return value;
    }

    public TreeMap<Integer, String> getPrefMountain(Integer pId){

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(String.format(SQL_SELECT_MOUNTAIN_WITH_PREF, pId), null);
        cursor.moveToFirst();

        TreeMap<Integer, String> mountains = new TreeMap<>();

        int mId;
        String mName;
        for (int i = 0; i < cursor.getCount(); i++) {
            mId = cursor.getInt(1);
            mName = cursor.getString(2);
            mountains.put(mId, mName);
            cursor.moveToNext();
        }

        return mountains;
    }

    public TreeMap<Integer, String> getMountains(){

        SQLiteDatabase db = getReadableDatabase();

        TreeMap<Integer, String> value = new TreeMap<>();

        Cursor cursor = db.rawQuery(SQL_SELECT_MOUNTAINS, null);
        cursor.moveToFirst();

        int mId;
        String mName;
        for (int i = 0; i < cursor.getCount(); i++) {
            mId = cursor.getInt(1);
            mName = cursor.getString(2);
            value.put(mId, mName);
            cursor.moveToNext();
        }

        return value;
    }


    public List<MountainData> getMountainsFromIdStr(List<String> idList) {

        SQLiteDatabase db = getReadableDatabase();

        List<MountainData> value = new ArrayList<>();

        for(String id : idList){
            if(!isNum(id)) continue;
            Cursor cursor = db.rawQuery(String.format(SQL_FIND_MOUNTAIN_FROM_ID, id), null);
            cursor.moveToFirst();
            int cId, pId, mId;
            String cName, pName, mName;
            for (int i = 0; i < cursor.getCount(); i++) {
                cId = cursor.getInt(0);
                cName = cursor.getString(1);
                pId = cursor.getInt(2);
                pName = cursor.getString(3);
                mId = cursor.getInt(4);
                mName = cursor.getString(5);
                value.add(new MountainData(cId, cName, pId, pName, mId, mName));
                cursor.moveToNext();
            }
        }

        return value;
    }

    public List<MountainData> getMountainsFromId(List<Integer> idList) {
        SQLiteDatabase db = getReadableDatabase();

        List<MountainData> value = new ArrayList<>();

        for(Integer id : idList){
            Cursor cursor = db.rawQuery(String.format(SQL_FIND_MOUNTAIN_FROM_ID, id+""), null);
            cursor.moveToFirst();
            int cId, pId, mId;
            String cName, pName, mName;
            for (int i = 0; i < cursor.getCount(); i++) {
                cId = cursor.getInt(0);
                cName = cursor.getString(1);
                pId = cursor.getInt(2);
                pName = cursor.getString(3);
                mId = cursor.getInt(4);
                mName = cursor.getString(5);
                value.add(new MountainData(cId, cName, pId, pName, mId, mName));
                cursor.moveToNext();
            }
        }

        return value;
    }

        public boolean isNum(String str){
        try{
            Integer.parseInt(str);
            return true;
        }catch(Exception e){

        }
        return false;
    }

    public TreeMap<Integer, String> getPrefs() {
        SQLiteDatabase db = getReadableDatabase();

        TreeMap<Integer, String> value = new TreeMap<>();

        Cursor cursor = db.rawQuery(SQL_SELECT_PREFS, null);
        cursor.moveToFirst();

        int pId;
        String pName;
        for (int i = 0; i < cursor.getCount(); i++) {
            pId = cursor.getInt(1);
            pName = cursor.getString(2);
            value.put(pId, pName);
            cursor.moveToNext();
        }

        return value;
    }
}
