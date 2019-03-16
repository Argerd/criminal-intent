package argerd.ru.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.util.Date;
import java.util.UUID;

import argerd.ru.Crime;

import static argerd.ru.database.CrimeDbSchema.*;

public class CrimeCursorWrapper extends CursorWrapper {
    public CrimeCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Crime getCrime() {
        String uuidString = getString(getColumnIndex(CrimeTable.Columns.UUID));
        String title = getString(getColumnIndex(CrimeTable.Columns.TITLE));
        long date = getLong(getColumnIndex(CrimeTable.Columns.DATE));
        int isSolved = getInt(getColumnIndex(CrimeTable.Columns.SOLVED));
        int isPolice = getInt(getColumnIndex(CrimeTable.Columns.POLICE));

        Crime crime = new Crime(UUID.fromString(uuidString));
        crime.setTitle(title);
        crime.setDate(new Date(date));
        crime.setSolved(isSolved != 0);
        crime.setRequiresPolice(isPolice != 0);

        return crime;
    }
}
