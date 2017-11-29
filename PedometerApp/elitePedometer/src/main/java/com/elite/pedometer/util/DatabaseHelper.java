package com.elite.pedometer.util;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.elite.pedometer.App;
import com.elite.pedometer.StepsCountModel;
import com.elite.pedometer.StepsDateModel;
import com.elite.pedometer.StepsModel;
import com.elite.pedometer.TargetStepsModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by prashant.patel on 7/20/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private String TAG = "=DatabaseHelper=";
    // Database Info
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_STEPS = "steps";
    private static final String TABLE_TARGET_STEPS = "target_steps";

    // Common U ID - Primary key for Every table
    private static final String KEY_STEPS_U_ID = "id";

    // TABLE_STEPS
    private static final String KEY_STEPS_DATE = "steps_date";
    private static final String KEY_STEPS_STEPS = "steps_count";
    private static final String KEY_STEPS_DATE_TIME = "steps_date_time";

    // TABLE_TARGET_STEPS
    private static final String KEY_TARGET_STEPS_DATE = "target_steps_date";
    private static final String KEY_TARGET_STEPS_STEPS = "target_steps_count";

    private static DatabaseHelper sInstance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you 
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }


    public DatabaseHelper(Context context) {
        super(context, App.DB_NAME, null, DATABASE_VERSION);
        // super(context, App.DB_NAME_WITH_PATH, null, DATABASE_VERSION);
    }


    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    @SuppressLint("NewApi")
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        try
        {
            String CREATE_STEPS_TABLE = "CREATE TABLE " + TABLE_STEPS +
                    "(" +
                    KEY_STEPS_U_ID + " INTEGER PRIMARY KEY," + // Define a primary key
                    KEY_STEPS_DATE + " TEXT," +
                    KEY_STEPS_STEPS + " INTEGER," +
                    KEY_STEPS_DATE_TIME + " TEXT" +
                    ")";


            String CREATE_TARGET_STEPS_TABLE = "CREATE TABLE " + TABLE_TARGET_STEPS +
                    "(" +
                    KEY_STEPS_U_ID + " INTEGER PRIMARY KEY," + // Define a primary key
                    KEY_TARGET_STEPS_DATE + " TEXT," +
                    KEY_TARGET_STEPS_STEPS + " TEXT" +
                    ")";

            db.execSQL(CREATE_STEPS_TABLE);
            db.execSQL(CREATE_TARGET_STEPS_TABLE);

        } catch (Exception e) {e.printStackTrace();}
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion)
        {
            try
            {
                // Simplest implementation is to drop all old tables and recreate them
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_STEPS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_TARGET_STEPS);
                onCreate(db);
            } catch (Exception e) {e.printStackTrace();}
        }
    }

    public void addSteps(StepsModel stepsModel) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try
        {
            ContentValues values = new ContentValues();
            values.put(KEY_STEPS_DATE, stepsModel.steps_date);
            values.put(KEY_STEPS_STEPS, stepsModel.steps_count);
            values.put(KEY_STEPS_DATE_TIME, stepsModel.steps_date_time);

            db.insertOrThrow(TABLE_STEPS, null, values);
            db.setTransactionSuccessful();

        }
        catch (Exception e) {
            Log.d(TAG, "Error while trying to add post to database");
        }
        finally {
            db.endTransaction();
        }
    }



    public List<StepsDateModel> getAllStepsDates() {
        List<StepsDateModel> listStepsDtModel = new ArrayList<>();
        try
        {
            // ORDER BY steps_date_time DESC

            String POSTS_SELECT_QUERY = String.format("SELECT DISTINCT(steps_date) FROM %s ",TABLE_STEPS);
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery(POSTS_SELECT_QUERY, null);
            try
            {
                if (cursor.moveToFirst()) {
                    do
                    {
                        StepsDateModel stepsDtModel = new StepsDateModel();
                        stepsDtModel.steps_date = cursor.getString(cursor.getColumnIndex(KEY_STEPS_DATE));
                        listStepsDtModel.add(stepsDtModel);
                    } while(cursor.moveToNext());
                }
            }
            catch (Exception e) {
                Log.d(TAG, "Error while trying to get posts from database");
            }
            finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
        } catch (Exception e) {e.printStackTrace();}

        return listStepsDtModel;
    }



    public long getSUMCountStepsDateWise(String dt) {
        long countTotal = 0;
        try
        {
            String POSTS_SELECT_QUERY = String.format("SELECT sum(steps_count) FROM %s ",TABLE_STEPS
                    + " WHERE steps_date = '" + dt + "'");
            SQLiteDatabase db = getReadableDatabase();

            Cursor cursor = db.rawQuery(POSTS_SELECT_QUERY, null);

            if(cursor.moveToFirst())
                countTotal = cursor.getInt(0);
            else
                countTotal = 0;

        }
        catch (Exception e) {e.printStackTrace();}

        return countTotal;
    }






    public void addTargetStepsCount(TargetStepsModel targetStepsModel) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try
        {
            ContentValues values = new ContentValues();
            values.put(KEY_TARGET_STEPS_DATE, targetStepsModel.target_steps_date);
            values.put(KEY_TARGET_STEPS_STEPS, targetStepsModel.target_steps_count);

            String POSTS_SELECT_QUERY = String.format("SELECT * FROM %s ",TABLE_TARGET_STEPS + " WHERE target_steps_date = '" + targetStepsModel.target_steps_date + "'");
            Cursor cursor = db.rawQuery(POSTS_SELECT_QUERY, null);
            if (cursor.getCount() > 0) // Update query
            {
                App.showLog(TAG + "==targetSteps==UPDATE==");
//                db.update(TABLE_TARGET_STEPS, values, KEY_TARGET_STEPS_DATE + "="
//                        + targetStepsModel.target_steps_date, null);

                db.update(TABLE_TARGET_STEPS, values,
                        KEY_TARGET_STEPS_DATE + " = ?" ,
                        new String[]{targetStepsModel.target_steps_date});
            }
            else // Insert query
            {
                App.showLog(TAG + "==targetSteps==INSERT==");
                db.insertOrThrow(TABLE_TARGET_STEPS, null, values);
            }



            // db.insertWithOnConflict(TABLE_TARGET_STEPS, null, values, SQLiteDatabase.CONFLICT_REPLACE);

            db.setTransactionSuccessful();

        }
        catch (Exception e) {
            Log.d(TAG, "Error while trying to add post to database");
        }
        finally {
            db.endTransaction();
        }
    }



    public String getTargetStepsDateWise(String dt) {
        String lastRowDateTime = "";
        try
        {
            String POSTS_SELECT_QUERY = String.format("SELECT * FROM %s ",TABLE_TARGET_STEPS
                    + " WHERE target_steps_date = '" + dt + "'");

            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery(POSTS_SELECT_QUERY, null);

//            if (cursor.moveToFirst()) {
//                do
//                {
//                    App.showLog("==targetDate==" + cursor.getString(cursor.getColumnIndex(KEY_TARGET_STEPS_DATE))
//                            + "\n==targetStes==" + cursor.getString(cursor.getColumnIndex(KEY_TARGET_STEPS_STEPS)));
//
//                } while(cursor.moveToNext());
//            }

            cursor.moveToLast();
            if (cursor != null && cursor.getCount() > 0)
            {
                lastRowDateTime = cursor.getString(cursor.getColumnIndex(KEY_TARGET_STEPS_STEPS));
            }

        } catch (Exception e) {e.printStackTrace();}

        return lastRowDateTime;
    }












//
    public String getLastStepsDateTime(String dt) {
        String lastRowDateTime = "";
        try
        {
            List<StepsCountModel> listStepsDtModel = new ArrayList<>();

            String POSTS_SELECT_QUERY = String.format("SELECT * FROM %s ",TABLE_STEPS + " WHERE steps_date = '" + dt + "' ORDER BY steps_date_time DESC LIMIT 1");

            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery(POSTS_SELECT_QUERY, null);
            cursor.moveToLast();
            if (cursor != null && cursor.getCount() > 0)
            {
                lastRowDateTime = cursor.getString(cursor.getColumnIndex(KEY_STEPS_DATE_TIME));
            }

        } catch (Exception e) {e.printStackTrace();}

        return lastRowDateTime;
    }



    // Delete all posts and users in the database
    public void deleteAllSteps() {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.execSQL("delete from "+ TABLE_STEPS);
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to delete all locations");
        } finally {
            //db.endTransaction();
        }
    }


    // Delete all posts and users in the database
    public void deleteAllStepsByDate(String dt) {
        SQLiteDatabase db = getWritableDatabase();
        try
        {
            db.execSQL("delete from "+ TABLE_STEPS + " WHERE steps_date = '" + dt + "'");
        }
        catch (Exception e)
        {
            Log.d(TAG, "Error while trying to delete all locations");
        } finally {
            //db.endTransaction();
        }
    }
} 