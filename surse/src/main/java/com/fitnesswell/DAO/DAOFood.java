package com.fitnesswell.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.fitnesswell.DAO.bodymeasures.BodyMeasure;
import com.fitnesswell.DAO.bodymeasures.DAOBodyMeasure;
import com.fitnesswell.DAO.record.DAORecord;
import com.fitnesswell.DAO.record.Record;
import com.fitnesswell.utils.DateConverter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DAOFood extends DAOBase{
    public DAOFood(Context context) {
        super(context);
    }
    /*
    // Contacts table name
    public static final String TABLE_NAME = "EFprofil";

    public static final String KEY = "_id";
    public static final String NAME = "name";
    public static final String CALORIES = "Calories";
    public static final String PROTEIN = "protein";
    public static final String CARB = "carb";
    public static final String PHOTO = "photo";
    public static final String FAT = "fat" ;

    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + CALORIES + " DATE, " + NAME + " TEXT, " + PROTEIN + " INTEGER, " + CARB + " DATE, " + PHOTO + " TEXT, " + FAT + " INTEGER);";

    public static final String TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";

    private Cursor mCursor = null;

    //DAOFonte mDAOFonte = null;


    public DAOFood(Context context) {
        super(context);
    }


    public void addFood(Food m) {
        // Check if profil already exists
        Food check = getProfile(m.getName());
        if (check != null) return;

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();

        value.put(DAOFood.NAME, m.getName());
        value.put(DAOFood.CALORIES, m.getCalories());
        value.put(DAOFood.PROTEIN, m.getProtein());
        value.put(DAOFood.PHOTO, m.getPhoto());
        value.put(DAOFood.CARB, m.getCarb());
        value.put(DAOFood.FAT, m.getFat());

        db.insert(DAOFood.TABLE_NAME, null, value);

        close();
    }

    public void addFood(String pName) {
        // Check if profil already exists
        Food check = getProfile(pName);
        if (check != null) return;

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();

        value.put(DAOFood.NAME, pName);

        db.insert(DAOFood.TABLE_NAME, null, value);

        close();
    }


    public Food getFood(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (mCursor != null) mCursor.close();
        mCursor = null;
        mCursor = db.query(TABLE_NAME,
                new String[]{KEY, CALORIES, NAME, PROTEIN, CARB, PHOTO, FAT},
                KEY + "=?",
                new String[]{String.valueOf(id)},
                null, null, null, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            mCursor.moveToFirst();

            Food value = new Food(mCursor.getLong(mCursor.getColumnIndex(DAOFood.KEY)),
                    DateConverter.DBDateStrToDate(mCursor.getString(mCursor.getColumnIndex(DAOFood.CREATIONDATE))),
                    mCursor.getString(mCursor.getColumnIndex(DAOFood.NAME)),
                    mCursor.getInt(mCursor.getColumnIndex(DAOFood.SIZE)),
                    mCursor.getString(mCursor.getColumnIndex(DAOFood.BIRTHDAY)) != null ? DateConverter.DBDateStrToDate(mCursor.getString(mCursor.getColumnIndex(DAOFood.BIRTHDAY))) : new Date(0),
                    mCursor.getString(mCursor.getColumnIndex(DAOFood.PHOTO)),
                    mCursor.getInt(mCursor.getColumnIndex(DAOFood.GENDER))
            );
            mCursor.close();
            close();

            // return value
            return value;
        } else {
            mCursor.close();
            close();
            return null;
        }

    }


    public Food getProfile(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (mCursor != null) mCursor.close();
        mCursor = null;
        mCursor = db.query(TABLE_NAME,
                new String[]{KEY, CALORIES, NAME, PROTEIN, CARB, PHOTO, FAT},
                NAME + "=?",
                new String[]{name},
                null, null, null, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            mCursor.moveToFirst();

            Food value = new Food(mCursor.getLong(mCursor.getColumnIndex(DAOFood.KEY)),
                    DateConverter.DBDateStrToDate(mCursor.getString(mCursor.getColumnIndex(DAOFood.CREATIONDATE))),
                    mCursor.getString(mCursor.getColumnIndex(DAOFood.NAME)),
                    mCursor.getInt(mCursor.getColumnIndex(DAOFood.SIZE)),
                    mCursor.getString(mCursor.getColumnIndex(DAOFood.BIRTHDAY)) != null ? DateConverter.DBDateStrToDate(mCursor.getString(mCursor.getColumnIndex(DAOFood.BIRTHDAY))) : new Date(0),
                    mCursor.getString(mCursor.getColumnIndex(DAOFood.PHOTO)),
                    mCursor.getInt(mCursor.getColumnIndex(DAOFood.GENDER))
            );

            mCursor.close();
            close();

            // return value
            return value;
        } else {
            close();
            return null;
        }

    }

    // Getting All Profils
    public List<Food> getProfilesList(SQLiteDatabase db, String pRequest) {
        List<Food> valueList = new ArrayList<>();
        // Select All Query

        mCursor = null;
        mCursor = db.rawQuery(pRequest, null);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            do {
                Food value = new Food(mCursor.getLong(mCursor.getColumnIndex(DAOFood.KEY)),
                        DateConverter.DBDateStrToDate(mCursor.getString(mCursor.getColumnIndex(DAOFood.CREATIONDATE))),
                        mCursor.getString(mCursor.getColumnIndex(DAOFood.NAME)),
                        mCursor.getInt(mCursor.getColumnIndex(DAOFood.SIZE)),
                        mCursor.getString(mCursor.getColumnIndex(DAOFood.BIRTHDAY)) != null ? DateConverter.DBDateStrToDate(mCursor.getString(mCursor.getColumnIndex(DAOFood.BIRTHDAY))) : new Date(0),
                        mCursor.getString(mCursor.getColumnIndex(DAOFood.PHOTO)),
                        mCursor.getInt(mCursor.getColumnIndex(DAOFood.GENDER))
                );

                // Adding value to list
                valueList.add(value);
            } while (mCursor.moveToNext());
        }

        //close();

        // return value list
        return valueList;
    }

    // Getting All Profils
    public List<Food> getAllProfiles(SQLiteDatabase db) {
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY " + KEY + " DESC";

        // return value list
        return getProfilesList(db, selectQuery);
    }

    // Getting All Machines
    public String[] getAllProfile() {

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;

        // Select All Machines
        String selectQuery = "SELECT DISTINCT  " + NAME + " FROM " + TABLE_NAME + " ORDER BY " + NAME + " ASC";
        mCursor = db.rawQuery(selectQuery, null);

        int size = mCursor.getCount();

        String[] valueList = new String[size];

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            int i = 0;
            do {
                String value = mCursor.getString(0);
                valueList[i] = value;
                i++;
            } while (mCursor.moveToNext());
        }

        //close();

        // return value list
        return valueList;
    }

    // Getting last record
    public Food getLastProfile() {

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;

        // Select All Machines
        String selectQuery = "SELECT MAX(" + KEY + ") FROM " + TABLE_NAME;
        mCursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        mCursor.moveToFirst();
        long value = Long.parseLong(mCursor.getString(0));

        Food prof = this.getFood(value);
        mCursor.close();
        close();

        // return value list
        return prof;
    }

    // Updating single value
    public int updateProfile(Food m) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();
        value.put(DAOFood.NAME, m.getName());
        value.put(DAOFood.BIRTHDAY, DateConverter.dateToDBDateStr(m.getBirthday()));
        value.put(DAOFood.SIZE, 0);
        value.put(DAOFood.PHOTO, m.getPhoto());
        value.put(DAOFood.GENDER, m.getGender());

        // updating row
        return db.update(TABLE_NAME, value, KEY + " = ?",
                new String[]{String.valueOf(m.getId())});
    }

    // Deleting single Food
    public void deleteProfile(Food m) {
        deleteProfile(m.getId());
    }

    // Deleting single Food
    public void deleteProfile(long id) {
        open();

        // Supprime les enregistrements de poids
        DAOProfileWeight mWeightDb = new DAOProfileWeight(null); // null car a ce moment le DatabaseHelper est cree depuis bien longtemps.
        List<ProfileWeight> valueList = mWeightDb.getWeightList(getFood(id));
        for (int i = 0; i < valueList.size(); i++) {
            mWeightDb.deleteMeasure(valueList.get(i).getId());
        }

        // Supprime les enregistrements de measure de body
        DAOBodyMeasure mBodyDb = new DAOBodyMeasure(null); // null car a ce moment le DatabaseHelper est cree depuis bien longtemps.
        List<BodyMeasure> bodyMeasuresList = mBodyDb.getBodyMeasuresList(getFood(id));
        for (int i = 0; i < bodyMeasuresList.size(); i++) {
            mBodyDb.deleteMeasure(bodyMeasuresList.get(i).getId());
        }

        DAORecord mDbRecords = new DAORecord(null);
        List<Record> recordList = mDbRecords.getAllRecordsByProfileList(getFood(id));
        for (int i = 0; i < recordList.size(); i++) {
            mDbRecords.deleteRecord(recordList.get(i).getId());
        }

        // Supprime le profile
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY + " = ?",
                new String[]{String.valueOf(id)});

        close();
    }


    // Getting Profils Count
    public int getCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        open();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int value = cursor.getCount();
        cursor.close();
        close();

        // return count
        return value;
    }
*/

}
