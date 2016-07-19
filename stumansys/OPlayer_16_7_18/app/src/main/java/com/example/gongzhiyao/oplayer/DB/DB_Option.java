package com.example.gongzhiyao.oplayer.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by 宫智耀 on 2016/6/13.
 */
public class DB_Option {
    DB db;
    SQLiteDatabase dbread,dbwrite;
    public DB_Option(Context context){
      db=new DB(context);
        dbread=db.getReadableDatabase();
        dbwrite=db.getWritableDatabase();
    }
    public void clearFeedTable(){
        String sql = "DELETE FROM " + "video_local" +";";

        dbwrite.execSQL(sql);
        revertSeq();

    }




    private void revertSeq() {
        String sql = "update sqlite_sequence set seq=0 where name='"+"video_local"+"'";

        dbwrite.execSQL(sql);

    }

    public void closeDB(){
        db.close();
    }




    public void clearMusicTable(){
        String sql = "DELETE FROM " + "musicInfo" +";";

        dbwrite.execSQL(sql);
        revertSeqMusic();

    }




    private void revertSeqMusic() {
        String sql = "update sqlite_sequence set seq=0 where name='"+"musicInfo"+"'";

        dbwrite.execSQL(sql);

    }

}
