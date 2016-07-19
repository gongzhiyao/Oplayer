package com.example.gongzhiyao.oplayer.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 宫智耀 on 2016/6/10.
 */
public class DB extends SQLiteOpenHelper {
    public static final String TABLE_NAME = "video_local";
    public static final String TITLE = "title";
    public static final String PATH = "path";
    public static final String POSTER = "poster";
    public static final String DESCRIPTION = "description";
    public static final String TIME = "time";
    public static final String TYPE = "type";
    public static final String DIRECTOR = "director";
    public static final String ACTOR = "actor";
    public static final String RELEASEYEAR = "releaseyear";
    public static final String ID = "_id";
    public static final String VIDEOID = "videoID";
    public static final String LENGTH = "length";


    public static final String MUSIC_TABLENAME = "musicInfo";
    public static final String MUSIC_NAME = "name";
    public static final String MUSIC_PATH = "path";
    public static final String MUSIC_LETTER = "firstLetter";
    public static final String MUSIC_SINGER = "singer";
    public static final String MUSIC_PICTURE = "picture";
    public static final String MUSIC_DURATION = "duration";
    public static final String MUSIC_SONG = "song";

    public static final String READY_MUSIC="readymusic";


    final String sq = "create table video_local ("
            + "videoID TEXT,"
            + "title TEXT,"
            + "path TEXT,"
            + "poster TEXT,"
            + "description TEXT,"
            + "time TEXT,"
            + "type TEXT,"
            + "director TEXT,"
            + "actor TEXT,"
            + "releaseyear TEXT,"
            + "length TEXT,"
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT)";
    final String sq1 = "create table musicInfo ("
            + "name TEXT,"
            + "path TEXT,"
            + "song TEXT,"
            + "singer TEXT,"
            + "picture TEXT,"
            + "firstLetter TEXT,"
            + "duration NUMBER,"
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT"
            + ")";
    final String sq2 = "create table readymusic ("
            + "name TEXT,"
            + "path TEXT,"
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT"
            + ")";


    public DB(Context context) {
        super(context, "video_local_db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(sq);
        db.execSQL(sq1);
        db.execSQL(sq2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
