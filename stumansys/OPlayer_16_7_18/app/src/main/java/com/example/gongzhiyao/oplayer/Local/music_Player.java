package com.example.gongzhiyao.oplayer.Local;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gongzhiyao.oplayer.Adapter.MusicListAdapter;
import com.example.gongzhiyao.oplayer.Application.SysApplication;
import com.example.gongzhiyao.oplayer.DB.DB;
import com.example.gongzhiyao.oplayer.DB.DB_Option;
import com.example.gongzhiyao.oplayer.Log.L;
import com.example.gongzhiyao.oplayer.MainActivity;
import com.example.gongzhiyao.oplayer.MusicFragment.Music_List_Fragment;
import com.example.gongzhiyao.oplayer.R;
import com.example.gongzhiyao.oplayer.Service.MusicService;
import com.example.gongzhiyao.oplayer.Tools.SerializableMap;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

public class music_Player extends AppCompatActivity implements View.OnClickListener {


    /**
     * 获取首字母
     * 排序数据库
     */
    private ImageView music_Pic;
    private TextView music_song, music_singer, tv_music_playTime, tv_music_titalTime;
    private ImageButton btn_music_poster, btn_music_more, btn_music_last, btn_music_next, btn_music_play_stop, btn_music_loop, btn_music_list;
    private L log;
    private SeekBar seekBar;
    String path;
    File audioFile;
    musicReceiver receiver;
    private int LoopType;
    private SharedPreferences mSharedPreferences;

    private ArrayList<String> names;
    private ArrayList<String> paths;
    private DB db;
    SQLiteDatabase dbread, dbwrite;
    private MusicListAdapter adapter;


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music__player);
        initView();
        getSupportFragmentManager().beginTransaction().replace(R.id.music_Container, new Music_List_Fragment()).commit();


        db = new DB(this);
        dbread = db.getReadableDatabase();
        dbwrite = db.getWritableDatabase();

        mSharedPreferences = getSharedPreferences("sp", MODE_PRIVATE);
        LoopType = mSharedPreferences.getInt("loopType", 2);
        switch (LoopType) {
            case 1:
                Drawable drawable_single = getResources().getDrawable(R.drawable.music_loop_single);
                btn_music_loop.setBackground(drawable_single);
                break;
            case 2:
                Drawable drawable_random = getResources().getDrawable(R.drawable.music_loop_random);
                btn_music_loop.setBackground(drawable_random);
                break;
            case 3:
                Drawable drawable_loop = getResources().getDrawable(R.drawable.music_loop);
                btn_music_loop.setBackground(drawable_loop);
                break;
        }


        IntentFilter intentFilter = new IntentFilter("com.example.gongzhiyao.oplayer.action.musicReceiver");
        receiver = new musicReceiver();
        registerReceiver(receiver, intentFilter);
        SysApplication.getInstance().addActivity(this);
        log = new L();
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        names = (ArrayList) bundle.getSerializable("names");
        paths = (ArrayList) bundle.getSerializable("paths");
        log.d("得到的歌曲的长度" + names.size() + "!!!!!!!!!!!!!!!!!!!!");
        path = bundle.getString("music_Info");
        audioFile = new File(path);
        new getMusicInfo().execute();
        new sortMusic().execute();


    }

    private void initView() {

        music_Pic = (ImageView) findViewById(R.id.music_Pic);
        music_singer = (TextView) findViewById(R.id.music_Singer);
        music_song = (TextView) findViewById(R.id.music_Song);
        btn_music_poster = (ImageButton) findViewById(R.id.music_Poster);
        btn_music_more = (ImageButton) findViewById(R.id.music_more);
        tv_music_playTime = (TextView) findViewById(R.id.tv_music_playTime);
        tv_music_titalTime = (TextView) findViewById(R.id.tv_music_totalTime);
        seekBar = (SeekBar) findViewById(R.id.sb_music);
        btn_music_loop = (ImageButton) findViewById(R.id.music_loop);
        btn_music_list = (ImageButton) findViewById(R.id.music_list);
        btn_music_last = (ImageButton) findViewById(R.id.music_last);
        btn_music_next = (ImageButton) findViewById(R.id.music_next);
        btn_music_play_stop = (ImageButton) findViewById(R.id.music_play_stop);

        btn_music_play_stop.setOnClickListener(this);
        btn_music_more.setOnClickListener(this);
        btn_music_next.setOnClickListener(this);
        btn_music_last.setOnClickListener(this);
        btn_music_list.setOnClickListener(this);
        btn_music_loop.setOnClickListener(this);
        btn_music_poster.setOnClickListener(this);

        seekBar.setOnSeekBarChangeListener(seekbarListener);


    }

    String changename = "";
    char c;

    private class sortMusic extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String sq = "select * from " + DB.MUSIC_TABLENAME + " where name=?";
            Cursor cursor = dbread.rawQuery(sq, new String[]{audioFile.getName()});
            String sq1 = "select * from " + DB.MUSIC_TABLENAME;
            Cursor cursor1 = dbread.rawQuery(sq1, null);
            /**
             * 正在播放的音乐不在当前数据库中，更新数据库，数据库中长度和传过来的列表长度不同时也要更新数据库
             */
            if (cursor.getCount() == 0 || cursor1.getCount() != names.size()) {
//            if (cursor.getCount() != 0 || cursor1.getCount() == names.size()) {
                clearDB();
                for (int i = 0; i < names.size(); i++) {
                    ContentValues cv = new ContentValues();
                    String name = names.get(i);
                    if (name.contains("-")) {
                        int position = name.indexOf("-");
                        changename = name.substring(position + 1).trim();
                        c = changename.charAt(0);

                    } else {
                        c = name.charAt(0);
                    }

                    String firstLetter = getSpelling(c);
                    String music_path = paths.get(i);
                    File f = new File(music_path);
                    getMusicInfoFromFile(music_path, f);
                    log.d(music_artist);
                    cv.put(DB.MUSIC_SONG, music_title);
                    cv.put(DB.MUSIC_SINGER, music_artist);
                    cv.put(DB.MUSIC_DURATION, music_duration);

                    /**
                     * 在这里要建一个文件夹，用来存储图片
                     *
                     */
                    getMediaDir();
                    /**
                     * 保存图片
                     */
                    try {
                        savePic(name, music_bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String path = Environment.getExternalStorageDirectory() + File.separator + "OPlayer" + File.separator + "pic" + File.separator + name + ".jpeg";
//                    log.d(path);
                    cv.put(DB.MUSIC_PICTURE, path);
                    cv.put(DB.MUSIC_LETTER, firstLetter);
                    cv.put(DB.MUSIC_NAME, name);
                    cv.put(DB.MUSIC_PATH, paths.get(i));
                    dbwrite.insert(DB.MUSIC_TABLENAME, null, cv);
                }
                String sq2 = "create view musiclist as select * from " + DB.MUSIC_TABLENAME + " order by " + DB.MUSIC_LETTER;
                dbwrite.execSQL(sq2);
            } else if (cursor.getCount() > 0) {
                log.d("此时不需要更新数据库");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            String sq = "select * from " + DB.MUSIC_TABLENAME + " order by " + DB.MUSIC_LETTER;
            Cursor cursor = dbread.rawQuery(sq, null);
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(DB.MUSIC_NAME));
                String path = cursor.getString(cursor.getColumnIndex(DB.MUSIC_PATH));
                ContentValues cv = new ContentValues();
                cv.put(DB.MUSIC_NAME, name);
                cv.put(DB.MUSIC_PATH, path);
                dbwrite.insert(DB.READY_MUSIC, null, cv);

            }
            /**
             * 下面要写的是  listview的adapter 等东西  还有就是侧边滑动栏
             *
             */


            /**
             * 在activity中写一个接口，然后写一个有callback参数的方法
             * 然后再fragment中调用即可。
             * 但是最后没有完成，这里打算使用广播
             *
             */
            Intent i = new Intent("com.example.gongzhiyao.oplayer.action.musicListReceiver");
            i.putExtra("op", 1);
            sendBroadcast(i);
        }
    }


    public void getMediaDir() {
        String path = Environment.getExternalStorageDirectory() + File.separator + "OPlayer" + File.separator + "pic";
//        log.d(path);
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }


    public void savePic(String name, Bitmap bitmap) throws IOException {
        String path = Environment.getExternalStorageDirectory() + File.separator + "OPlayer" + File.separator + "pic" + File.separator;
        File file = new File(path, name + ".jpeg");
        if (!file.exists()) {
            /**
             * 如果不存在，把bitmap存入file
             */

            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();

//                Log.i(TAG, "已经保存");
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }


//    public void saveBitmap() {
//        Log.e(TAG, "保存图片");
//        File f = new File("/sdcard/namecard/", picName);
//        if (f.exists()) {
//            f.delete();
//        }
//        try {
//            FileOutputStream out = new FileOutputStream(f);
//            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
//            out.flush();
//            out.close();
//            Log.i(TAG, "已经保存");
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//    }


    private int seek_Progress = 0;
    SeekBar.OnSeekBarChangeListener seekbarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            if (fromUser == true) {
                seek_Progress = progress;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            /**
             * 在这里要向service发送数据
             */
            long seekTime = seek_Progress * duration / 100;
//            log.d("滑动的进度是"+seek_Progress);
            Intent i = new Intent(getApplicationContext(), MusicService.class);
            i.putExtra("seekTime", seekTime);
            i.putExtra("op", 5);
            startService(i);

        }
    };

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.music_play_stop:
                Intent intent = new Intent(this, MusicService.class);
                intent.putExtra("op", 2);
                startService(intent);
                log.d("点击了播放暂停按钮");
                break;
            case R.id.music_loop:
                LoopType = mSharedPreferences.getInt("loopType", 1);
                log.d("loopType=" + LoopType);
                Drawable drawable_single = getResources().getDrawable(R.drawable.music_loop_single);
                Drawable drawable_random = getResources().getDrawable(R.drawable.music_loop_random);
                Drawable drawable_loop = getResources().getDrawable(R.drawable.music_loop);
                switch (LoopType) {
                    case 1://单曲转换到随机播放
                        btn_music_loop.setBackground(drawable_random);
                        mSharedPreferences.edit().putInt("loopType", 2).commit();
                        Toast.makeText(getApplicationContext(), "随机播放", Toast.LENGTH_SHORT).show();
                        break;
                    case 2://随机切换到顺序播放
                        btn_music_loop.setBackground(drawable_loop);
                        mSharedPreferences.edit().putInt("loopType", 3).commit();
                        Toast.makeText(getApplicationContext(), "列表循环", Toast.LENGTH_SHORT).show();
                        break;
                    case 3://顺序切换到单曲循环
                        btn_music_loop.setBackground(drawable_single);
                        mSharedPreferences.edit().putInt("loopType", 1).commit();
                        Toast.makeText(getApplicationContext(), "单曲循环", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;

            case R.id.music_next:
                Intent intent1 = new Intent(this, MusicService.class);
                intent1.putExtra("op", 3);
                startService(intent1);

                break;
            case R.id.music_last:
                Intent intent2 = new Intent(this, MusicService.class);
                intent2.putExtra("op", 6);
                startService(intent2);

                break;

        }
    }


    String path_music;
    String tilte, artist;
    long duration = 233535;
    Bitmap bitmap;


    private class getMusicInfo extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            ContentResolver musicResolver = getContentResolver();
            Cursor cursor = null;
            try {

                log.d(path.substring(18, 19));
                String sign = path.substring(18, 19);

                if (!sign.equals("0")) {
                    path_music = "/storage/emulated/0" + audioFile.getCanonicalPath().substring(24);
                } else if (sign.equals("0")) {
                    path_music = audioFile.getCanonicalPath().toString();
                }
                log.d(path_music);
            } catch (IOException e) {
                e.printStackTrace();
            }
            cursor = musicResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media.DATA + "= ? ",
                    new String[]{path_music}, null);
//                    cursor = musicResolver.query(
//                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media.DATA +"= ? or "+ MediaStore.Audio.Media.DATA+"= ?",
//                            new String[]{path_music,}, null);

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();

                long ID = cursor.getLong(cursor
                        .getColumnIndex(MediaStore.Audio.Media._ID));    //音乐id
                tilte = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                artist = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.ARTIST)); // 艺术家
                String album = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.ALBUM));    //专辑
//                            long albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                duration = cursor.getLong(cursor
                        .getColumnIndex(MediaStore.Audio.Media.DURATION)); // 时长
//                            long size = cursor.getLong(cursor
//                                    .getColumnIndex(MediaStore.Audio.Media.SIZE));
//                            String url = cursor.getString(cursor
//                                    .getColumnIndex(MediaStore.Audio.Media.DATA));

                long albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));


                bitmap = getMusicBitemp(getApplicationContext(), ID, albumId);


            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            music_song.setText(tilte);
            music_singer.setText(artist);
            music_Pic.setImageBitmap(bitmap);
            String time = MainActivity.formatTime(duration);
            tv_music_titalTime.setText(time);

            /**
             * 开启一个线程，实现定时更新seekbar和textview
             */


        }
    }


    private static final Uri sArtworkUri = Uri
            .parse("content://media/external/audio/albumart");

    public static Bitmap getMusicBitemp(Context context, long songid,
                                        long albumid) {
        Bitmap bm = null;
// 专辑id和歌曲id小于0说明没有专辑、歌曲，并抛出异常
        if (albumid < 0 && songid < 0) {
            throw new IllegalArgumentException(
                    "Must specify an album or a song id");
        }
        try {
            if (albumid < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/"
                        + songid + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver()
                        .openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            } else {
                Uri uri = ContentUris.withAppendedId(sArtworkUri, albumid);
                ParcelFileDescriptor pfd = context.getContentResolver()
                        .openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);


                } else {
                    return null;
                }
            }
        } catch (FileNotFoundException ex) {
        }
        if (bm == null) {
            Resources resources = context.getResources();
            Drawable drawable = resources.getDrawable(R.drawable.back_iv);
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            bm = bitmapDrawable.getBitmap();
        }
//        return bm;
        return Bitmap.createScaledBitmap(bm, 150, 150, true);
    }


//    public static String formatTime(long time) {
//        String min = time / (1000 * 60) + "";
//        String sec = time % (1000 * 60) + "";
//        if (min.length() < 2) {
//            min = "0" + time / (1000 * 60) + "";
//        } else {
//            min = time / (1000 * 60) + "";
//        }
//        if (sec.length() == 4) {
//            sec = "0" + (time % (1000 * 60)) + "";
//        } else if (sec.length() == 3) {
//            sec = "00" + (time % (1000 * 60)) + "";
//        } else if (sec.length() == 2) {
//            sec = "000" + (time % (1000 * 60)) + "";
//        } else if (sec.length() == 1) {
//            sec = "0000" + (time % (1000 * 60)) + "";
//        }
//        return min + ":" + sec.trim().substring(0, 2);
//    }


    public class musicReceiver extends BroadcastReceiver {


        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onReceive(Context context, Intent intent) {
            int op = intent.getIntExtra("op", -1);
            switch (op) {


                /**
                 * 有一个比较重要的地方
                 * 就是如何使播放界面进度条
                 * 跟随播放改动
                 *
                 *
                 * 暂停播放 ，上一首，下一首 还有 播放循环方式
                 * 能够实现
                 *
                 *
                 * 应该在根源上解决问题
                 * 得到服务每秒发送来的广播
                 *
                 *
                 */


                case 1:
                    /**
                     * 暂停
                     */
                    Drawable drawable = getResources().getDrawable(R.drawable.music_play);
                    btn_music_play_stop.setBackground(drawable);

                    break;
                case 2:
                    /**
                     * 播放
                     */
                    Drawable drawable1 = getResources().getDrawable(R.drawable.music_stop);
                    btn_music_play_stop.setBackground(drawable1);
                    break;

                case 3:
                    /**
                     * 用于更新进度条
                     */
                    long currentTime = intent.getLongExtra("currentTime", -1);
                    if (currentTime != -1) {
                        int progress = (int) (currentTime * 100 / duration);
                        seekBar.setProgress(progress);
                        String time = MainActivity.formatTime(currentTime);
                        tv_music_playTime.setText(time);
                    }
                    break;

                case 4:
                    /**
                     * 音乐播放器切换下一首，更新界面
                     */
                    path = intent.getStringExtra("path");
                    audioFile = new File(path);
                    new getMusicInfo().execute();
                    break;


            }
        }


    }


    public void clearDB() {
        DB_Option db_option = new DB_Option(getApplicationContext());
        db_option.clearMusicTable();
        db_option.closeDB();

    }


    public String getSpelling(char c) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();

        // UPPERCASE：大写  (ZHONG)
        // LOWERCASE：小写  (zhong)
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);

        // WITHOUT_TONE：无音标  (zhong)
        // WITH_TONE_NUMBER：1-4数字表示英标  (zhong4)
        // WITH_TONE_MARK：直接用音标符（必须WITH_U_UNICODE否则异常）  (zhòng)
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

        // WITH_V：用v表示ü  (nv)
        // WITH_U_AND_COLON：用"u:"表示ü  (nu:)
        // WITH_U_UNICODE：直接用ü (nü)
        format.setVCharType(HanyuPinyinVCharType.WITH_V);

        try {
            String[] pinyin = PinyinHelper.toHanyuPinyinStringArray(c, format);
            if (pinyin != null) {
                if (pinyin.length > 0) {
                    return pinyin[0];
                } else {
                    return String.valueOf(c);
                }
            } else {
                return String.valueOf(c).toLowerCase();
            }

        } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
            badHanyuPinyinOutputFormatCombination.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
        dbread.close();
        unregisterReceiver(receiver);
        log.d("已销毁");
    }

    @Override
    protected void onStop() {
        super.onStop();
        log.d("已暂停");
    }


    String music_title, music_artist;
    long music_duration = 233535;
    Bitmap music_bitmap;
    String music_path;


    public void getMusicInfoFromFile(String path, File audioFile) {
        ContentResolver musicResolver = getContentResolver();
        Cursor cursor = null;
        try {

            log.d(path.substring(18, 19));
            String sign = path.substring(18, 19);

            if (!sign.equals("0")) {
                music_path = "/storage/emulated/0" + audioFile.getCanonicalPath().substring(24);
            } else if (sign.equals("0")) {
                music_path = audioFile.getCanonicalPath().toString();
            }
            log.d(music_path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        cursor = musicResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media.DATA + "= ? ",
                new String[]{music_path}, null);
//                    cursor = musicResolver.query(
//                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media.DATA +"= ? or "+ MediaStore.Audio.Media.DATA+"= ?",
//                            new String[]{path_music,}, null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();

            long ID = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Audio.Media._ID));    //音乐id
            music_title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            music_artist = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.ARTIST)); // 艺术家
//            String album = cursor.getString(cursor
//                    .getColumnIndex(MediaStore.Audio.Media.ALBUM));    //专辑
//                            long albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            music_duration = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DURATION)); // 时长
//                            long size = cursor.getLong(cursor
//                                    .getColumnIndex(MediaStore.Audio.Media.SIZE));
//                            String url = cursor.getString(cursor
//                                    .getColumnIndex(MediaStore.Audio.Media.DATA));

            long albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));


            music_bitmap = getMusicBitemp(getApplicationContext(), ID, albumId);


        }
    }
}
