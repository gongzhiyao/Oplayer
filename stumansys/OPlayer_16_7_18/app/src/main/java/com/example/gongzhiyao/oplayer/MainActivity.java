package com.example.gongzhiyao.oplayer;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gongzhiyao.oplayer.Application.SysApplication;
import com.example.gongzhiyao.oplayer.DB.DB;
import com.example.gongzhiyao.oplayer.DB.DB_Option;
import com.example.gongzhiyao.oplayer.Local.music_Player;
import com.example.gongzhiyao.oplayer.Log.L;
import com.example.gongzhiyao.oplayer.Service.MusicService;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageButton btn_Web, btn_Local, btn_Search, btn_Me;
    private FrameLayout frameLayout;
    private ImageButton iv_Local_Music, btn_Local_Playe_Or_Stop;
    private TextView tv_Local_Music_Name, tv_Local_Music_Singer;
    private TextView timeOfSong;
    private ProgressBar progressBar_Local;
    private LinearLayout Local_Music_player;
//    private static ArrayList<String> names_Audio = null;
//    private static ArrayList<String> paths_Audio = null;
//    /**
//     * 还好有广播  可以使用广播来
//     */
    private static ArrayList<String> names_In_Folder = null;
    private static ArrayList<String> path_In_Folder = null;
    private L log;
    private music_playerReceiver receiver;
    private MusicService musicService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SysApplication.getInstance().addActivity(this);

        log = new L();
        init();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new WebVideo()).commit();
        IntentFilter intentFilter = new IntentFilter("com.example.gongzhiyao.oplayer.action.music_playerReceiver");
        receiver = new music_playerReceiver();
        registerReceiver(receiver, intentFilter);
        Resources resources = getResources();
        Drawable drawable_bg = resources.getDrawable(R.drawable.back_iv);
        drawableToBitamp(drawable_bg);

    }


    public void init() {
        btn_Web = (ImageButton) findViewById(R.id.btn_Web);
        btn_Local = (ImageButton) findViewById(R.id.btn_Local);
        btn_Search = (ImageButton) findViewById(R.id.btn_Search);
        btn_Me = (ImageButton) findViewById(R.id.btn_Me);
        btn_Web.setOnClickListener(this);
        btn_Local.setOnClickListener(this);
        btn_Search.setOnClickListener(this);
        btn_Me.setOnClickListener(this);
        frameLayout = (FrameLayout) findViewById(R.id.container);
        btn_Web.setSelected(true);
        Local_Music_player = (LinearLayout) findViewById(R.id.Local_Music_Player);
        iv_Local_Music = (ImageButton) findViewById(R.id.iv_music);
        btn_Local_Playe_Or_Stop = (ImageButton) findViewById(R.id.btn_Local_Play_Or_Stop);
        tv_Local_Music_Name = (TextView) findViewById(R.id.tv_Local_Music_Name);
        tv_Local_Music_Singer = (TextView) findViewById(R.id.tv_Local_Music_Singer);
        timeOfSong = (TextView) findViewById(R.id.time_Of_Music);
        progressBar_Local = (ProgressBar) findViewById(R.id.progress_Bar_Local);
/**
 *
 */
        iv_Local_Music.setOnClickListener(this);
        btn_Local_Playe_Or_Stop.setOnClickListener(this);
        tv_Local_Music_Name.setOnClickListener(this);
        tv_Local_Music_Singer.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_Web:
                btn_Web.setSelected(true);
                btn_Local.setSelected(false);
                btn_Search.setSelected(false);
                btn_Me.setSelected(false);

                getSupportFragmentManager().beginTransaction().replace(R.id.container, new WebVideo()).commit();
                break;
            case R.id.btn_Local:
                btn_Local.setSelected(true);
                btn_Web.setSelected(false);
                btn_Search.setSelected(false);
                btn_Me.setSelected(false);
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new LocalVideo()).commit();
                break;
            case R.id.btn_Search:
                btn_Search.setSelected(true);
                btn_Web.setSelected(false);
                btn_Local.setSelected(false);
                btn_Me.setSelected(false);
                break;
            case R.id.btn_Me:
                btn_Me.setSelected(true);
                btn_Web.setSelected(false);
                btn_Local.setSelected(false);
                btn_Search.setSelected(false);


                break;
            case R.id.btn_Local_Play_Or_Stop:
                /**
                 * 发送给service
                 */

                Intent intent = new Intent(this, MusicService.class);
                intent.putExtra("op", 2);
                startService(intent);

                break;
            case R.id.iv_music:
            case R.id.tv_Local_Music_Name:
            case R.id.tv_Local_Music_Singer:
                /**
                 * 需要传送的有 歌名 歌手 歌曲长度  专辑界面
                 */
                int list_type3 = LocalVideo.sp.getInt("listType", -1);
                if (list_type3 == 1) {
                    new scan_In_Folder().execute();
                } else if (list_type3 == 2) {


                    Bundle bundle = new Bundle();
                    bundle.putSerializable("names", LocalVideo.names_Audio);
                    bundle.putSerializable("paths", LocalVideo.paths_Audio);
                    bundle.putString("music_Info", audioFile.getPath());
                    Intent intent2 = new Intent(this, music_Player.class);
                    intent2.putExtras(bundle);
                    startActivity(intent2);
                    /**
                     *
                     */

                }
                break;
        }
    }

    private long mExitTime;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - mExitTime < 2000) {
                SysApplication.getInstance().exit();

            } else {
                Toast.makeText(getApplicationContext(), "再按一次退出", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            }
            return true;
        }


        return super.onKeyDown(keyCode, event);
    }


    public class music_playerReceiver extends BroadcastReceiver {

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onReceive(Context context, Intent intent) {
            int op = intent.getIntExtra("op", -1);
            switch (op) {
                case 1:
                case 2:
                    /**
                     * 获取播放路径，更新信息
                     */
                    String path = intent.getStringExtra("path");
                    progressBar_Local.setProgress(0);
                    audioFile = new File(path);
                    new getMusicInfo().execute();
                    break;
                case 3:
                case 5:
                case 6:


                    Resources resources = getResources();
                    Drawable drawable_play = resources.getDrawable(R.drawable.play);
                    btn_Local_Playe_Or_Stop.setBackground(drawable_play);

                    break;

                case 4:
                case 7:


                    Resources resources1 = getResources();
                    Drawable drawable_stop = resources1.getDrawable(R.drawable.stop);
                    btn_Local_Playe_Or_Stop.setBackground(drawable_stop);

                    break;

//                case 5:
//
//
//                    Resources resources2 = getResources();
//                    Drawable drawable_play1 = resources2.getDrawable(R.drawable.play);
//                    btn_Local_Playe_Or_Stop.setBackground(drawable_play1);
//
//                    break;
//                case 6:
//
//                    Resources resources3 = getResources();
//                    Drawable drawable_play2 = resources3.getDrawable(R.drawable.play);
//                    btn_Local_Playe_Or_Stop.setBackground(drawable_play2);
//
//                    break;

//                case 7:
//
//
//                    Resources resources4 = getResources();
//                    Drawable drawable_stop3 = resources4.getDrawable(R.drawable.stop);
//                    btn_Local_Playe_Or_Stop.setBackground(drawable_stop3);
//
//                    break;

                case 8:

                    Local_Music_player.setVisibility(View.GONE);

                    break;

                case 9:

                    long currentTime = intent.getLongExtra("currentTime", -1);
                    if (currentTime != -1) {
                        int progress = (int) (currentTime * 100 / duration);
//                        log.d("!!!!!!!!!!!!!!!!!!!!!!!!!!" + progress);

                        progressBar_Local.setProgress(progress);
                        String time = formatTime(currentTime);
                        timeOfSong.setText(time);

                    }
                    break;


                case 11:
                    String path1 = intent.getStringExtra("path");
                    audioFile = new File(path1);
                    Intent i = new Intent(getApplicationContext(), MusicService.class);
                    i.putExtra("op", 1);
                    i.putExtra("path", path1);
                    log.d("要发送的路径是" + path1);
                    startService(i);
                    Local_Music_player.setVisibility(View.VISIBLE);
                    Resources resources5 = getResources();
                    Drawable drawable_stop4 = resources5.getDrawable(R.drawable.stop);
                    btn_Local_Playe_Or_Stop.setBackground(drawable_stop4);
                    new getMusicInfo().execute();


                    remoteViews = new RemoteViews(getPackageName(),
                            R.layout.statusbar);

                    remoteViews.setInt(R.id.ib_music_playStop_notification, "setBackgroundResource", R.drawable.music_stop);
                    break;
            }
        }


    }


    private String tilte;
    private String artist;
    private Bitmap bitmap;
    private long duration = 233535;
    private String path_music;
    private File audioFile;
    private RemoteViews remoteViews;


    private void drawableToBitamp(Drawable drawable) {
        BitmapDrawable bd = (BitmapDrawable) drawable;
        bitmap = bd.getBitmap();
    }

    private class getMusicInfo extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            ContentResolver musicResolver = getContentResolver();
            Cursor cursor = null;
            try {
//                log.d(audioFile.getCanonicalPath().toString());
                String path = audioFile.getCanonicalPath().toString();
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
            tv_Local_Music_Name.setText(tilte);
            tv_Local_Music_Singer.setText(artist);
            iv_Local_Music.setImageBitmap(bitmap);


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


    private long StringToMilliseconds(String time) {

        String min1 = time.substring(0, 1);
        if (min1.equals("0")) {
            String min2 = time.substring(1, 2);
            int min = Integer.parseInt(min2);
            long ms_min = min * 60000;
            String sec1 = time.substring(3);
            int sec = Integer.parseInt(sec1);
            long ms_sec = sec * 1000;
            long ms_total = ms_min + ms_sec;
            return ms_total;
        } else {
            String min2 = time.substring(0, 2);
            int min = Integer.parseInt(min2);
            long ms_min = min * 60000;
            String sec2 = time.substring(3);
            int sec = Integer.parseInt(sec2);
            long ms_sec = sec * 1000;
            long ms_total = ms_min + ms_sec;
            return ms_total;
        }
    }


    private class scan_In_Folder extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            File fileParent = audioFile.getParentFile();
            getAudiosOfFolder(fileParent);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            /**
             * 在这里关掉stopTime后，要获取到播放的时间进度，然后传给下一个activity，等到返回的时候，同样也要返回seekbar的时间进度
             */


            /**
             * 先试一下
             * 写入数据库的那种
             *在这里判断一下，如果没有改变播放列表位置，那么就不用更新数据库
             *
             */
//            String sq = "select * from " + DB.MUSIC_TABLENAME + " where name=?";
//            Cursor cursor = dbread.rawQuery(sq, new String[]{audioFile.getName()});
//            if (cursor.getCount() == 0) {
//                clearDB();
//                for (int i = 0; i < names_In_Folder.size(); i++) {
//                    ContentValues cv = new ContentValues();
//                    cv.put(DB.MUSIC_NAME, names_In_Folder.get(i));
//                    cv.put(DB.MUSIC_PATH, path_In_Folder.get(i));
//                    dbwrite.insert(DB.MUSIC_TABLENAME, null, cv);
//                }
//            } else if (cursor.getCount() > 0) {
//                log.d("此时不需要更新数据库");
//            }


            Bundle bundle = new Bundle();
            bundle.putSerializable("names", names_In_Folder);
            bundle.putSerializable("paths", path_In_Folder);
            bundle.putString("music_Info", audioFile.getPath());
            Intent intent = new Intent(getApplicationContext(), music_Player.class);
            intent.putExtras(bundle);

            startActivity(intent);
        }
    }

    private void getAudiosOfFolder(File file) {
        names_In_Folder = new ArrayList<String>();
        path_In_Folder = new ArrayList<String>();
        File[] files = file.listFiles();
        for (File file1 : files) {
            if (getMIMEType(file1).equals("audio/*")) {
                names_In_Folder.add(file1.getName());
                path_In_Folder.add(file1.getPath());
            }
        }

    }


    public static String getMIMEType(File file) {
        String type = "";
        String name = file.getName();
        //文件扩展名
        String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
        if (end.equals("m4a") || end.equals("mp3") || end.equals("wav")) {
            type = "audio";
        } else if (end.equals("mp4") || end.equals("3gp") || end.equals("wmv") || end.equals("flv") || end.equals("rmvb") || end.equals("mkv") || end.equals("mov") || end.equals("m4v") || end.equals("avi")) {
            type = "video";
        } else if (end.equals("jpg") || end.equals("png") || end.equals("jpeg") || end.equals("bmp") || end.equals("gif")) {
            type = "image";
        } else {
            //如果无法直接打开，跳出列表由用户选择
            type = "*";
        }
        type += "/*";

        return type;
    }


    public static String formatTime(long time) {
        String min = time / (1000 * 60) + "";
        String sec = time % (1000 * 60) + "";
        if (min.length() < 2) {
            min = "0" + time / (1000 * 60) + "";
        } else {
            min = time / (1000 * 60) + "";
        }
        if (sec.length() == 4) {
            sec = "0" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 3) {
            sec = "00" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 2) {
            sec = "000" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 1) {
            sec = "0000" + (time % (1000 * 60)) + "";
        }
        return min + ":" + sec.trim().substring(0, 2);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(receiver);

        } catch (Exception e) {
        }
    }








}
