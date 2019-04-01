package argerd.ru.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import argerd.ru.database.CrimeDbSchema.CrimeTable;

public class CrimeBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "crimeBase.db";

    public CrimeBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + CrimeTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                CrimeTable.Columns.UUID + ", " +
                CrimeTable.Columns.TITLE + ", " +
                CrimeTable.Columns.DATE + ", " +
                CrimeTable.Columns.SOLVED + ", " +
                CrimeTable.Columns.POLICE + ", " +
                CrimeTable.Columns.SUSPECT + ", " +
                CrimeTable.Columns.PHONE_NUMBER_OF_SUSPECT + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}