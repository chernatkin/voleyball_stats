package volley.volleyball.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import volley.volleyball.ResultType;
import volley.volleyball.ServeType;
import volley.volleyball.StringUtils;

public class GamesDatabaseHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "Volleyball1.db";

    private static final int DATABASE_VERSION = 1;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss", Locale.US);

    private SQLiteDatabase rdb;

    public GamesDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public long putNewGame(long gameId, Date creationDate, String firstTeam, String secondTeam){

        final ContentValues gameValues = new ContentValues();
        gameValues.put(GameEntry.COLUMN_NAME_CREATION_DATE, DATE_FORMAT.format(creationDate));
        gameValues.put(GameEntry.COLUMN_NAME_FIRST_TEAM, firstTeam.trim());
        gameValues.put(GameEntry.COLUMN_NAME_SECOND_TEAM, secondTeam.trim());

        final SQLiteDatabase db = this.getWritableDatabase();
        if(gameId == -1){
            gameId = db.insert(GameEntry.TABLE_NAME, null, gameValues);
        }
        else{
            db.update(GameEntry.TABLE_NAME, gameValues,
                    GameEntry._ID + " = ?",
                    new String[]{String.valueOf(gameId)});
        }

        db.close();
        return gameId;
    }

    public long putNewGameMember(final long gameId, TeamMemberEntry member){
        if(member.getId() == -1 && StringUtils.isEmpty(member.getFullName())){
            return -1;
        }

        final SQLiteDatabase db = this.getWritableDatabase();

         final ContentValues memberValues = new ContentValues();
         memberValues.put(TeamMemberEntry.COLUMN_NAME_FULL_NAME, member.getFullName().trim());

         if(member.getId() == -1 && !StringUtils.isEmpty(member.getFullName())){
             memberValues.put(TeamMemberEntry.COLUMN_NAME_VOLLEY_GAME_ID, gameId);
             db.insert(TeamMemberEntry.TABLE_NAME, null, memberValues);
         }
         else if(member.getId() != -1 && !StringUtils.isEmpty(member.getFullName())){
             memberValues.put(TeamMemberEntry._ID, member.getId());
             db.update(TeamMemberEntry.TABLE_NAME, memberValues,
                     String.format("%s = ? AND %s = ?", TeamMemberEntry._ID, TeamMemberEntry.COLUMN_NAME_VOLLEY_GAME_ID),
                     new String[]{String.valueOf(member.getId()), String.valueOf(gameId)});
        }
        else {
             db.delete(TeamMemberEntry.TABLE_NAME,
                     String.format("%s = ? AND %s = ?", TeamMemberEntry._ID, TeamMemberEntry.COLUMN_NAME_VOLLEY_GAME_ID),
                     new String[]{String.valueOf(member.getId()), String.valueOf(gameId)});
        }

        db.close();
        return gameId;
    }

    public void putGameActivity(long gameId, long memberId, Date time, int setNumber, ServeType serveType, ResultType resultType){
        final ContentValues gameValues = new ContentValues();
        gameValues.put(GameActivityEntry.COLUMN_NAME_VOLLEY_GAME_ID, gameId);
        gameValues.put(GameActivityEntry.COLUMN_NAME_TEAM_MEMBER_ID, memberId);
        gameValues.put(GameActivityEntry.COLUMN_NAME_CREATION_TIME, TIME_FORMAT.format(time));
        gameValues.put(GameActivityEntry.COLUMN_NAME_SET_NUMBER, setNumber);
        gameValues.put(GameActivityEntry.COLUMN_NAME_SERVE_TYPE, serveType.name());
        gameValues.put(GameActivityEntry.COLUMN_NAME_RESULT_TYPE, resultType.name());

        final SQLiteDatabase db = this.getWritableDatabase();
        db.insert(GameActivityEntry.TABLE_NAME, null, gameValues);
        db.close();
    }

    public int deleteLastGameActivity(long gameId){
        final ContentValues gameValues = new ContentValues();
        gameValues.put(GameActivityEntry.COLUMN_NAME_VOLLEY_GAME_ID, gameId);

        final SQLiteDatabase db = this.getWritableDatabase();
        final Cursor c = db.rawQuery(String.format("SELECT MAX(%s) FROM %s WHERE %s = ?",
                        GameActivityEntry._ID,
                        GameActivityEntry.TABLE_NAME,
                        GameActivityEntry.COLUMN_NAME_VOLLEY_GAME_ID),
                new String[]{String.valueOf(gameId)});


        if(!c.moveToFirst()){
            c.close();
            db.close();
            return 0;
        }

        final long actId = c.getLong(0);
        c.close();

        final int count = db.delete(GameActivityEntry.TABLE_NAME, String.format("%s = ? AND %s = ?",
                                                                    GameActivityEntry.COLUMN_NAME_VOLLEY_GAME_ID,
                                                                    GameActivityEntry._ID),
                                    new String[]{String.valueOf(gameId), String.valueOf(actId)});

        db.close();
        return count;
    }

    public List<GameEntry> fetchAllGames(){
        final SQLiteDatabase db = readableDatabase();
        final Cursor c = db.rawQuery(String.format("SELECT %s, %s, %s, %s FROM %s ORDER BY %s DESC",
                                                GameEntry._ID,
                                                GameEntry.COLUMN_NAME_CREATION_DATE,
                                                GameEntry.COLUMN_NAME_FIRST_TEAM,
                                                GameEntry.COLUMN_NAME_SECOND_TEAM,
                                                GameEntry.TABLE_NAME,
                                                GameEntry.COLUMN_NAME_CREATION_DATE),
                                null);

        final List<GameEntry> list = new ArrayList<GameEntry>();

        while(c.moveToNext()){
            list.add(new GameEntry(c.getLong(0), parseDate(c.getString(1)), c.getString(2), c.getString(3)));
        }

        c.close();
        return list;
    }

    public GameEntry fetchGame(final long gameId){
        final SQLiteDatabase db = readableDatabase();
        final Cursor c = db.rawQuery(
                String.format("SELECT %s, %s, %s, %s  FROM %s WHERE %s = ?",
                        GameEntry._ID,
                        GameEntry.COLUMN_NAME_CREATION_DATE,
                        GameEntry.COLUMN_NAME_FIRST_TEAM,
                        GameEntry.COLUMN_NAME_SECOND_TEAM,
                        GameEntry.TABLE_NAME,
                        GameEntry._ID),
                new String[] {String.valueOf(gameId)});

        if(!c.moveToFirst()){
            return null;
        }

        final GameEntry entry = new GameEntry(c.getLong(0), parseDate(c.getString(1)), c.getString(2), c.getString(3));
        c.close();

        return entry;
    }

    public List<TeamMemberEntry> fetchGameMembers(final long gameId){
        final SQLiteDatabase db = readableDatabase();
        final Cursor c = db.rawQuery(
                String.format("SELECT %s, %s FROM %s WHERE %s = ? ORDER BY LOWER(%s)",
                        TeamMemberEntry._ID,
                        TeamMemberEntry.COLUMN_NAME_FULL_NAME,
                        TeamMemberEntry.TABLE_NAME,
                        TeamMemberEntry.COLUMN_NAME_VOLLEY_GAME_ID,
                        TeamMemberEntry.COLUMN_NAME_FULL_NAME),
                new String[] {String.valueOf(gameId)});

        final List<TeamMemberEntry> list = new ArrayList<TeamMemberEntry>();

        while(c.moveToNext()){
            list.add(new TeamMemberEntry(c.getLong(0), c.getString(1)));
        }

        c.close();
        return list;
    }

    public List<PlayerStatsEntry> fetchPlayerStats(final long gameId){
        final SQLiteDatabase db = readableDatabase();

        final Cursor c = db.rawQuery(String.format("SELECT m.%s, a.%s, a.%s, COUNT(*) FROM %s AS a INNER JOIN %s AS m ON a.%s = m.%s " +
                                    "WHERE a.%s = ? GROUP BY m.%s, a.%s, a.%s ORDER BY LOWER(m.%s)",
                                TeamMemberEntry.COLUMN_NAME_FULL_NAME,
                                GameActivityEntry.COLUMN_NAME_SERVE_TYPE,
                                GameActivityEntry.COLUMN_NAME_RESULT_TYPE,

                                GameActivityEntry.TABLE_NAME,
                                TeamMemberEntry.TABLE_NAME,
                                GameActivityEntry.COLUMN_NAME_TEAM_MEMBER_ID,
                                TeamMemberEntry._ID,

                                GameActivityEntry.COLUMN_NAME_VOLLEY_GAME_ID,

                                TeamMemberEntry.COLUMN_NAME_FULL_NAME,
                                GameActivityEntry.COLUMN_NAME_SERVE_TYPE,
                                GameActivityEntry.COLUMN_NAME_RESULT_TYPE,

                                TeamMemberEntry.COLUMN_NAME_FULL_NAME)
                        , new String[] {String.valueOf(gameId)});

        final Map<String, PlayerStatsEntry> stats = new LinkedHashMap<String, PlayerStatsEntry>();

        while(c.moveToNext()){
            final String name = c.getString(0);
            final String serveType = c.getString(1);
            final String resultType = c.getString(2);
            final int count = c.getInt(3);
            final String key = name + serveType;

            PlayerStatsEntry entry = stats.get(key);
            if(entry == null){
                entry = new PlayerStatsEntry(name, ServeType.valueOf(serveType));
                stats.put(key, entry);
            }
            entry.getResults().put(ResultType.valueOf(resultType), count);
        }
        c.close();

        return new ArrayList<PlayerStatsEntry>(stats.values());
    }

    private SQLiteDatabase readableDatabase(){
        if(rdb == null || !rdb.isOpen()){
            rdb = this.getReadableDatabase();
        }
        return rdb;
    }

    private Date parseDate(final String date){
        try {
            return DATE_FORMAT.parse(date);
        }
        catch(ParseException pe){
            return null;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + GameEntry.TABLE_NAME + " ( "
                      + GameEntry._ID + " INTEGER PRIMARY KEY, "
                      + GameEntry.COLUMN_NAME_CREATION_DATE + " TEXT NOT NULL, "
                      + GameEntry.COLUMN_NAME_FIRST_TEAM + " TEXT NOT NULL, "
                      + GameEntry.COLUMN_NAME_SECOND_TEAM + " TEXT NOT NULL)"
        );

        db.execSQL("CREATE TABLE " + TeamMemberEntry.TABLE_NAME + " ( "
                        + TeamMemberEntry._ID + " INTEGER PRIMARY KEY, "
                        + TeamMemberEntry.COLUMN_NAME_VOLLEY_GAME_ID + " INTEGER NOT NULL, "
                        + TeamMemberEntry.COLUMN_NAME_FULL_NAME + " TEXT NOT NULL, "
                        + "FOREIGN KEY(" + TeamMemberEntry.COLUMN_NAME_VOLLEY_GAME_ID + ") REFERENCES " + GameEntry.TABLE_NAME + "(" + GameEntry._ID + "))"
        );

        db.execSQL("CREATE TABLE " + GameActivityEntry.TABLE_NAME + " ( "
                        + GameActivityEntry._ID + " INTEGER PRIMARY KEY, "
                        + GameActivityEntry.COLUMN_NAME_VOLLEY_GAME_ID + " INTEGER NOT NULL, "
                        + GameActivityEntry.COLUMN_NAME_TEAM_MEMBER_ID + " INTEGER NOT NULL, "
                        + GameActivityEntry.COLUMN_NAME_CREATION_TIME + " TEXT NOT NULL,"
                        + GameActivityEntry.COLUMN_NAME_SET_NUMBER + " INTEGER NOT NULL,"
                        + GameActivityEntry.COLUMN_NAME_SERVE_TYPE + " TEXT NOT NULL,"
                        + GameActivityEntry.COLUMN_NAME_RESULT_TYPE + " TEXT NOT NULL,"
                        + "FOREIGN KEY(" + GameActivityEntry.COLUMN_NAME_VOLLEY_GAME_ID + ") REFERENCES " + GameEntry.TABLE_NAME + "(" + GameEntry._ID + "),"
                        + "FOREIGN KEY(" + GameActivityEntry.COLUMN_NAME_TEAM_MEMBER_ID + ") REFERENCES " + TeamMemberEntry.TABLE_NAME + "(" + TeamMemberEntry._ID + "))"
        );

        db.execSQL("CREATE INDEX volley_game_creation_date_idx ON " + GameEntry.TABLE_NAME + "(" + GameEntry.COLUMN_NAME_CREATION_DATE + ")");
        db.execSQL("CREATE INDEX volley_game_activity_member_id_idx ON " + GameActivityEntry.TABLE_NAME + "(" + GameActivityEntry.COLUMN_NAME_TEAM_MEMBER_ID + ")");
        db.execSQL("CREATE INDEX volley_game_activity_game_id_idx ON " + GameActivityEntry.TABLE_NAME + "(" + GameActivityEntry.COLUMN_NAME_VOLLEY_GAME_ID + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    public synchronized void close() {
        super.close();
        if(rdb != null && rdb.isOpen()) {
            rdb.close();
        }
    }

    public static class GameEntry implements BaseColumns {
        public static final String TABLE_NAME = "volley_game";

        public static final String COLUMN_NAME_CREATION_DATE = "creation_date";
        public static final String COLUMN_NAME_FIRST_TEAM = "first_team";
        public static final String COLUMN_NAME_SECOND_TEAM = "second_team";

        private long id = -1;

        private Date creationDate;

        private String firstTeam;

        private String secondTeam;

        public GameEntry(long id, Date creationDate, String firstTeam, String secondTeam) {
            this.id = id;
            this.creationDate = creationDate;
            this.firstTeam = firstTeam;
            this.secondTeam = secondTeam;
        }

        public long getId() {
            return id;
        }

        public Date getCreationDate() {
            return creationDate;
        }

        public String getFirstTeam() {
            return firstTeam;
        }

        public String getSecondTeam() {
            return secondTeam;
        }

        public void setId(long id) {
            this.id = id;
        }

        public void setCreationDate(Date creationDate) {
            this.creationDate = creationDate;
        }

        public void setFirstTeam(String firstTeam) {
            this.firstTeam = firstTeam;
        }

        public void setSecondTeam(String secondTeam) {
            this.secondTeam = secondTeam;
        }

        @Override
        public String toString() {
            return firstTeam + " " + secondTeam;
        }
    }

    public static class TeamMemberEntry implements BaseColumns {
        public static final String TABLE_NAME = "volley_team_member";

        public static final String COLUMN_NAME_VOLLEY_GAME_ID = "volley_game_id";
        public static final String COLUMN_NAME_FULL_NAME = "full_name";

        private long id = -1;

        private String fullName = "";

        public TeamMemberEntry() {
        }

        public TeamMemberEntry(long id, String fullName) {
            this.id = id;
            this.fullName = fullName;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        @Override
        public String toString() {
            return fullName;
        }
    }

    public static abstract class GameActivityEntry implements BaseColumns {
        public static final String TABLE_NAME = "volley_game_activity";

        public static final String COLUMN_NAME_VOLLEY_GAME_ID = "volley_game_id";
        public static final String COLUMN_NAME_TEAM_MEMBER_ID = "volley_team_member_id";
        public static final String COLUMN_NAME_CREATION_TIME = "creation_time";
        public static final String COLUMN_NAME_SET_NUMBER = "set_number";
        public static final String COLUMN_NAME_SERVE_TYPE = "serve_type";
        public static final String COLUMN_NAME_RESULT_TYPE = "result_type";
    }

    public static class PlayerStatsEntry implements BaseColumns {

        private String playerName;

        private ServeType serveType;

        private Map<ResultType, Integer> results = new HashMap<ResultType, Integer>(2);

        protected PlayerStatsEntry(String playerName, ServeType serveType) {
            this.playerName = playerName;
            this.serveType = serveType;
        }

        public String getPlayerName() {
            return playerName;
        }

        public void setPlayerName(String playerName) {
            this.playerName = playerName;
        }

        public ServeType getServeType() {
            return serveType;
        }

        public void setServeType(ServeType serveType) {
            this.serveType = serveType;
        }

        public Map<ResultType, Integer> getResults() {
            return results;
        }

        public void setResults(Map<ResultType, Integer> results) {
            this.results = results;
        }
    }
}
