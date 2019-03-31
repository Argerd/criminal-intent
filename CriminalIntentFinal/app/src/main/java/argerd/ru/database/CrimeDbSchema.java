package argerd.ru.database;

public class CrimeDbSchema {

    public static final class CrimeTable {
        public static final String NAME = "crimes";

        public static final class Columns {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String DATE = "date";
            public static final String SOLVED = "solved";
            public static final String POLICE = "police";
            public static final String SUSPECT = "suspect";
            public static final String PHONE_NUMBER_OF_SUSPECT = "phone_number_of_suspect";
        }
    }
}
