package com.fitnesswell.DAO.bodymeasures;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.core.content.res.ResourcesCompat;

import com.fitnesswell.DAO.DAOBase;
import com.fitnesswell.DAO.Profile;
import com.fitnesswell.enums.Unit;
import com.fitnesswell.utils.DateConverter;
import com.fitnesswell.utils.Value;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DAOBodyMeasure extends DAOBase {

    public static final String TABLE_NAME = "EFbodymeasures";

    public static final String KEY = "_id";
    public static final String BODYPART_ID = "bodypart_id";
    public static final String MEASURE = "mesure";
    public static final String DATE = "date";
    public static final String UNIT = "unit";
    public static final String PROFIL_KEY = "profil_id";

    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + DATE + " DATE, " + BODYPART_ID + " INTEGER, " + MEASURE + " REAL , " + PROFIL_KEY + " INTEGER, " + UNIT  + " INTEGER);";

    public static final String TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
    private final Profile mProfile = null;
    private Cursor mCursor = null;

    public DAOBodyMeasure(Context context) {
        super(context);
    }


    public void addBodyMeasure(Date pDate, long pBodyPartId, Value pValue, long pProfileId) {
        SQLiteDatabase db = this.getWritableDatabase();
        addBodyMeasure(db, pDate, pBodyPartId, pValue, pProfileId);
    }


    public void addBodyMeasure(SQLiteDatabase db, Date pDate, long pBodyPartId, Value pValue, long pProfileId) {
        ContentValues value = new ContentValues();

        BodyMeasure existingBodyMeasure = getBodyMeasuresFromDate(db, pBodyPartId, pDate, pProfileId);
        if (existingBodyMeasure == null) {

            String dateString = DateConverter.dateToDBDateStr(pDate);
            value.put(DAOBodyMeasure.DATE, dateString);
            value.put(DAOBodyMeasure.BODYPART_ID, pBodyPartId);
            value.put(DAOBodyMeasure.MEASURE, pValue.getValue());
            value.put(DAOBodyMeasure.PROFIL_KEY, pProfileId);
            value.put(DAOBodyMeasure.UNIT, pValue.getUnit().ordinal());

            db.insert(DAOBodyMeasure.TABLE_NAME, null, value);

        } else {
            existingBodyMeasure.setBodyMeasure(pValue);

            updateMeasure(db, existingBodyMeasure);
        }
    }

    public BodyMeasure getMeasure(long id) {

        SQLiteDatabase db = this.getReadableDatabase();

        mCursor = null;
        mCursor = db.query(TABLE_NAME,
                new String[]{KEY, DATE, BODYPART_ID, MEASURE, PROFIL_KEY, UNIT},
                KEY + "=?",
                new String[]{String.valueOf(id)},
                null, null, null, null);
        if (mCursor != null)
            mCursor.moveToFirst();

        Date date = DateConverter.DBDateStrToDate(mCursor.getString(mCursor.getColumnIndex(DATE)));

        Value value = new Value(
                mCursor.getFloat(mCursor.getColumnIndex(MEASURE)),
                Unit.fromInteger(mCursor.getInt(mCursor.getColumnIndex(UNIT))),
                null,
                ResourcesCompat.ID_NULL
        );

        return new BodyMeasure(mCursor.getLong(mCursor.getColumnIndex(KEY)),
                date,
                mCursor.getInt(mCursor.getColumnIndex(BODYPART_ID)),
                value,
                mCursor.getLong(mCursor.getColumnIndex(PROFIL_KEY))
        );
    }

    public List<BodyMeasure> getMeasuresList(SQLiteDatabase db, String pRequest) {
        List<BodyMeasure> valueList = new ArrayList<>();


        mCursor = null;
        mCursor = db.rawQuery(pRequest, null);

        if (mCursor.moveToFirst()) {
            do {
                Date date = DateConverter.DBDateStrToDate(mCursor.getString(mCursor.getColumnIndex(DATE)));

                Value value = new Value(
                        mCursor.getFloat(mCursor.getColumnIndex(MEASURE)),
                        Unit.fromInteger(mCursor.getInt(mCursor.getColumnIndex(UNIT))),
                        null,
                        ResourcesCompat.ID_NULL
                );

                BodyMeasure measure = new BodyMeasure(mCursor.getLong(mCursor.getColumnIndex(KEY)),
                        date,
                        mCursor.getInt(mCursor.getColumnIndex(BODYPART_ID)),
                        value,
                        mCursor.getLong(mCursor.getColumnIndex(PROFIL_KEY))
                );

                valueList.add(measure);
            } while (mCursor.moveToNext());
        }
        return valueList;
    }

    public Cursor getCursor() {
        return mCursor;
    }


    public List<BodyMeasure> getBodyPartMeasuresList(long pBodyPartID, Profile pProfile) {
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + BODYPART_ID + "=" + pBodyPartID + " AND " + PROFIL_KEY + "=" + pProfile.getId() + " ORDER BY date(" + DATE + ") DESC";

        return getMeasuresList(getReadableDatabase(), selectQuery);
    }

    public List<BodyMeasure> getBodyPartMeasuresListTop4(long pBodyPartID, Profile pProfile) {
        if (pProfile == null) return null;
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + BODYPART_ID + "=" + pBodyPartID + " AND " + PROFIL_KEY + "=" + pProfile.getId() + " ORDER BY date(" + DATE + ") DESC LIMIT 4;";
        return getMeasuresList(getReadableDatabase(), selectQuery);
    }

    public List<BodyMeasure> getBodyMeasuresList(Profile pProfile) {
        if (pProfile == null) return null;
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + PROFIL_KEY + "=" + pProfile.getId() + " ORDER BY date(" + DATE + ") DESC";
        return getMeasuresList(getReadableDatabase(), selectQuery);
    }


    public List<BodyMeasure> getAllBodyMeasures() {
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " ORDER BY date(" + DATE + ") DESC";
        return getMeasuresList(getReadableDatabase(), selectQuery);
    }


    public BodyMeasure getLastBodyMeasures(long pBodyPartID, Profile pProfile) {
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + BODYPART_ID + "=" + pBodyPartID + " AND " + PROFIL_KEY + "=" + pProfile.getId() + " ORDER BY date(" + DATE + ") DESC LIMIT 1";

        List<BodyMeasure> array = getMeasuresList(getReadableDatabase(), selectQuery);
        if (array.size() <= 0) {
            return null;
        }

        return array.get(0);
    }

    public BodyMeasure getBodyMeasuresFromDate(SQLiteDatabase db,  long pBodyPartID, Date pDate, long pProfileId) {
        String dateString = DateConverter.dateToDBDateStr(pDate);

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + BODYPART_ID + "=" + pBodyPartID + " AND " + DATE + "=\"" + dateString + "\" AND " + PROFIL_KEY + "=" + pProfileId + " ORDER BY date(" + DATE + ") DESC LIMIT 1";

        List<BodyMeasure> array = getMeasuresList(db, selectQuery);
        if (array.size() <= 0) {
            return null;
        }

        return array.get(0);
    }

    public int updateMeasure(BodyMeasure m) {
        return updateMeasure(getWritableDatabase(), m);
    }


    public int updateMeasure(SQLiteDatabase db, BodyMeasure m) {
        ContentValues value = new ContentValues();
        String dateString = DateConverter.dateToDBDateStr(m.getDate());
        value.put(DAOBodyMeasure.DATE, dateString);
        value.put(DAOBodyMeasure.BODYPART_ID, m.getBodyPartID());
        value.put(DAOBodyMeasure.MEASURE, m.getBodyMeasure().getValue());
        value.put(DAOBodyMeasure.PROFIL_KEY, m.getProfileID());
        value.put(DAOBodyMeasure.UNIT, m.getBodyMeasure().getUnit().ordinal());

        return db.update(TABLE_NAME, value, KEY + " = ?",
                new String[]{String.valueOf(m.getId())});
    }

    public void deleteMeasure(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY + " = ?",
                new String[]{String.valueOf(id)});
    }

    public int getCount() {
        String countQuery = "SELECT * FROM " + TABLE_NAME;
        open();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int value = cursor.getCount();
        cursor.close();
        close();


        return value;
    }

}


