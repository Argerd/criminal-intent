package argerd.ru;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import argerd.ru.database.CrimeBaseHelper;
import argerd.ru.database.CrimeCursorWrapper;

import static argerd.ru.database.CrimeDbSchema.*;

public class CrimeLab {
    private static CrimeLab sCrimeLab;
    private Context context;
    private SQLiteDatabase database;

    private CrimeLab(Context context) {
        this.context = context.getApplicationContext();
        database = new CrimeBaseHelper(this.context).getWritableDatabase();
    }

    public static CrimeLab get(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    public List<Crime> getCrimes() {
        List<Crime> crimes = new ArrayList<>();

        CrimeCursorWrapper crimeCursorWrapper = queryCrimes(null, null);

        try {
            crimeCursorWrapper.moveToFirst();
            while (!crimeCursorWrapper.isAfterLast()) {
                crimes.add(crimeCursorWrapper.getCrime());
                crimeCursorWrapper.moveToNext();
            }
        } finally {
            crimeCursorWrapper.close();
        }
        return crimes;
    }

    public Crime getCrime(UUID id) {
        CrimeCursorWrapper crimeCursorWrapper = queryCrimes(
                CrimeTable.Columns.UUID + " = ?",
                new String[]{id.toString()});
        try {
            if (crimeCursorWrapper.getCount() == 0) {
                return null;
            }
            crimeCursorWrapper.moveToFirst();
            return crimeCursorWrapper.getCrime();
        } finally {
            crimeCursorWrapper.close();
        }
    }

    private ContentValues getContentValues(Crime crime) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CrimeTable.Columns.UUID, crime.getId().toString());
        contentValues.put(CrimeTable.Columns.TITLE, crime.getTitle());
        contentValues.put(CrimeTable.Columns.DATE, crime.getDate().getTime());
        contentValues.put(CrimeTable.Columns.SOLVED, crime.isSolved() ? 1 : 0);
        contentValues.put(CrimeTable.Columns.POLICE, crime.isRequiresPolice() ? 1 : 0);
        contentValues.put(CrimeTable.Columns.SUSPECT, crime.getSuspect());
        return contentValues;
    }

    public void addCrime(Crime crime) {
        ContentValues contentValues = getContentValues(crime);
        database.insert(CrimeTable.NAME, null, contentValues);
    }

    public void deleteCrime(Crime crime) {
        database.delete(CrimeTable.NAME, CrimeTable.Columns.UUID + " = ?",
                new String[] {crime.getId().toString()});
    }

    public void updateCrime(Crime crime) {
        String uuidString = crime.getId().toString();
        ContentValues contentValues = getContentValues(crime);

        database.update(CrimeTable.NAME, contentValues, CrimeTable.Columns.UUID + " = ?",
                new String[]{uuidString});
    }

    /**
     * Чтение из данных из бд
     *
     * @param whereClause
     * @param whereArgs
     * @return
     */
    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs) {
        Cursor cursor = database.query(
                CrimeTable.NAME,
                null, //с null выбираются все столбцы
                whereClause,
                whereArgs,
                null,
                null,
                null);
        return new CrimeCursorWrapper(cursor);
    }
}