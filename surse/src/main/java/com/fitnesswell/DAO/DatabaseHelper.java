package com.fitnesswell.DAO;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.fitnesswell.DAO.bodymeasures.BodyPartExtensions;
import com.fitnesswell.DAO.bodymeasures.DAOBodyMeasure;
import com.fitnesswell.DAO.bodymeasures.DAOBodyPart;
import com.fitnesswell.DAO.program.DAOProgram;
import com.fitnesswell.DAO.program.DAOProgramHistory;
import com.fitnesswell.DAO.record.DAORecord;
import com.fitnesswell.enums.Muscle;
import java.io.File;
import java.util.List;
import java.util.Set;

public class DatabaseHelper extends SQLiteOpenHelper {

        public static final int DATABASE_VERSION = 25;
        public static final String OLD09_DATABASE_NAME = "easyfitness";
        public static final String DATABASE_NAME = "easyfitness.db";
        private static DatabaseHelper sInstance;
        private Context mContext = null;

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mContext = context;
        }

        public static DatabaseHelper getInstance(Context context) {

            // Use the application context, which will ensure that you
            // don't accidentally leak an Activity's context.
            // See this article for more information: http://bit.ly/6LRzfx
            if (sInstance == null) {
                sInstance = new DatabaseHelper(context.getApplicationContext());
            }
            return sInstance;
        }

        public static void renameOldDatabase(Activity activity) {
            File oldDatabaseFile = activity.getDatabasePath(OLD09_DATABASE_NAME);
            if (oldDatabaseFile.exists()) {
                File newDatabaseFile = new File(oldDatabaseFile.getParentFile(), DATABASE_NAME);
                oldDatabaseFile.renameTo(newDatabaseFile);
            }
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DAORecord.TABLE_CREATE); // Covers Fonte and Cardio and Static
            db.execSQL(DAOProfile.TABLE_CREATE);
            db.execSQL(DAOProfileWeight.TABLE_CREATE);
            db.execSQL(DAOMachine.TABLE_CREATE);
            db.execSQL(DAOBodyMeasure.TABLE_CREATE);
            db.execSQL(DAOBodyPart.TABLE_CREATE);
            db.execSQL(DAOProgram.TABLE_CREATE);
            db.execSQL(DAOProgramHistory.TABLE_CREATE);
            initBodyPartTable(db);
        }

        @Override
        public void onUpgrade(
                final SQLiteDatabase db, final int oldVersion,
                final int newVersion) {

        }

        private void updateMusclesToUseNewIds(SQLiteDatabase db) {
            List<Machine> machines = new DAOMachine(mContext).getAllMachinesUsingDb(db);
            for (Machine machine : machines) {
                updateMachineToUseNewId(machine, db);
            }
        }

        private void updateMachineToUseNewId(Machine machine, SQLiteDatabase db) {
            Set<Muscle> usedMuscles = Muscle.setFromBodyParts(machine.getBodyParts(), mContext.getResources());
            machine.setBodyParts(Muscle.migratedBodyPartStringFor(usedMuscles));
            new DAOMachine(mContext).updateMachineUsingDb(machine, db);
        }

        @Override
        public void onDowngrade(
                final SQLiteDatabase db, final int oldVersion,
                final int newVersion) {
        }
        public void initBodyPartTable(SQLiteDatabase db) {
            int display_order = 0;

            addInitialBodyPart(db, BodyPartExtensions.LEFTBICEPS, "", "", display_order++, BodyPartExtensions.TYPE_MUSCLE);
            addInitialBodyPart(db, BodyPartExtensions.RIGHTBICEPS, "", "", display_order++, BodyPartExtensions.TYPE_MUSCLE);
            addInitialBodyPart(db, BodyPartExtensions.PECTORAUX, "", "", display_order++, BodyPartExtensions.TYPE_MUSCLE);
            addInitialBodyPart(db, BodyPartExtensions.WAIST, "", "", display_order++, BodyPartExtensions.TYPE_MUSCLE);
            addInitialBodyPart(db, BodyPartExtensions.BEHIND, "", "", display_order++, BodyPartExtensions.TYPE_MUSCLE);
            addInitialBodyPart(db, BodyPartExtensions.LEFTTHIGH, "", "", display_order++, BodyPartExtensions.TYPE_MUSCLE);
            addInitialBodyPart(db, BodyPartExtensions.RIGHTTHIGH, "", "", display_order++, BodyPartExtensions.TYPE_MUSCLE);
            addInitialBodyPart(db, BodyPartExtensions.LEFTCALVES, "", "", display_order++, BodyPartExtensions.TYPE_MUSCLE);
            addInitialBodyPart(db, BodyPartExtensions.RIGHTCALVES, "", "", display_order++, BodyPartExtensions.TYPE_MUSCLE);
            addInitialBodyPart(db, BodyPartExtensions.WEIGHT, "", "", 0, BodyPartExtensions.TYPE_WEIGHT);
            addInitialBodyPart(db, BodyPartExtensions.MUSCLES, "", "", 0, BodyPartExtensions.TYPE_WEIGHT);
            addInitialBodyPart(db, BodyPartExtensions.WATER, "", "", 0, BodyPartExtensions.TYPE_WEIGHT);
            addInitialBodyPart(db, BodyPartExtensions.FAT, "", "", 0, BodyPartExtensions.TYPE_WEIGHT);
            addInitialBodyPart(db, BodyPartExtensions.SIZE, "", "", 0, BodyPartExtensions.TYPE_WEIGHT);
        }

        public long addInitialBodyPart(SQLiteDatabase db, long pKey, String pCustomName, String pCustomPicture, int pDisplay, int pType) {
            ContentValues value = new ContentValues();

            value.put(DAOBodyPart.KEY, pKey);
            value.put(DAOBodyPart.BODYPART_RESID, pKey);
            value.put(DAOBodyPart.CUSTOM_NAME, pCustomName);
            value.put(DAOBodyPart.CUSTOM_PICTURE, pCustomPicture);
            value.put(DAOBodyPart.DISPLAY_ORDER, pDisplay);
            value.put(DAOBodyPart.TYPE, pType);

            return db.insert(DAOBodyPart.TABLE_NAME, null, value);
        }

    }
