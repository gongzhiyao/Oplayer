package com.example.gongzhiyao.oplayer.Service;


import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.ProgressBar;
import android.widget.RemoteViews;

import com.example.gongzhiyao.oplayer.DB.DB;
import com.example.gongzhiyao.oplayer.Local.music_Player;
import com.example.gongzhiyao.oplayer.LocalVideo;
import com.example.gongzhiyao.oplayer.Log.L;
import com.example.gongzhiyao.oplayer.MainActivity;
import com.example.gongzhiyao.oplayer.R;

import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MusicService extends Service {
    MediaPlayer mediaPlayer;
    File audioFile;
    AudioManager am;
    L log;
    public static ArrayList<String> name;
    public static ArrayList<String> path;
    public NotificationCompat.Builder builder;
    public NotificationManager mNotificationManager;
    RemoteViews remoteViews;
    long currentTime;
    private SharedPreferences sp;
    private DB db;
    private SQLiteDatabase dbread;


    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 1) {
                if (mediaPlayer != null) {
                    currentTime = mediaPlayer.getCurrentPosition(); // 获取当前音乐播放的位置
                    Intent intent = new Intent("com.example.gongzhiyao.oplayer.action.music_playerReceiver");
                    intent.putExtra("op", 9);
                    intent.putExtra("currentTime", currentTime);
                    sendBroadcast(intent);

                    Intent intent1 = new Intent("com.example.gongzhiyao.oplayer.action.musicReceiver");
                    intent1.putExtra("op", 3);
                    intent1.putExtra("currentTime", currentTime);
                    sendBroadcast(intent1);
//                    log.d(currentTime+"");
                    handler.sendEmptyMessageDelayed(1, 1000);
                }

            }
        }

        ;
    };


    @Override
    public void onCreate() {
        /**
         * 确保服务不会自己重启
         */
        super.onCreate();
        db = new DB(this);
        dbread = db.getReadableDatabase();
        sp = getSharedPreferences("sp", MODE_PRIVATE);
        am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        log = new L();
        log.d("服务已开启");


        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        remoteViews = new RemoteViews(getPackageName(),
                R.layout.statusbar);

        remoteViews.setInt(R.id.ib_music_playStop_notification, "setBackgroundResource", R.drawable.music_stop);
        Intent buttonPlayIntent = new Intent(getApplicationContext(), MusicService.class);
        buttonPlayIntent.putExtra("op", 2);
        PendingIntent pendButtonPlayIntent = PendingIntent.getService(getApplicationContext(), 0, buttonPlayIntent, 0);
//            PendingIntent pendButtonPlayIntent = PendingIntent.getBroadcast(getActivity(), 0, buttonPlayIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.ib_music_playStop_notification, pendButtonPlayIntent);

        Intent buttonNextIntent = new Intent(getApplicationContext(), MusicService.class);
        buttonNextIntent.putExtra("op", 3);

        PendingIntent pendButtonNextIntent = PendingIntent.getService(getApplicationContext(), 1, buttonNextIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.ib_music_next_notification, pendButtonNextIntent);


        Intent buttonStopIntent = new Intent(getApplicationContext(), MusicService.class);
        buttonStopIntent.putExtra("op", 4);
        PendingIntent pendButtonStopIntent = PendingIntent.getService(getApplicationContext(), 3, buttonStopIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.ib_music_stop_notification, pendButtonStopIntent);


        builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setContent(remoteViews).setSmallIcon(R.drawable.ic_launcher)
                .setOngoing(true)
                .setTicker("music is playing");
        startForeground(1, builder.build());


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        log.d("服务已经onstartCommand");
        if (intent != null) {
            name = new ArrayList<String>();
            path = new ArrayList<String>();
            if (LocalVideo.sp != null) {
                if (LocalVideo.sp.getInt(LocalVideo.ListType, -1) == 1) {
                    name = LocalVideo.names_Audio_Dir;
                    path = LocalVideo.paths_Audio_Dir;
                } else if (LocalVideo.sp.getInt(LocalVideo.ListType, -1) == 2) {
                    name = LocalVideo.names;
                    path = LocalVideo.paths;
                }
            }
            int op = intent.getIntExtra("op", -1);
            if (op == 1) {
                /**
                 * 点击音乐列表播放音乐
                 */
                String path = intent.getStringExtra("path");
                audioFile = new File(path);

                new getMusicInfo().execute();
                am.requestAudioFocus(afChangeListener,
                        AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                if (mediaPlayer != null) {

                    mediaPlayer.pause();
                    mediaPlayer.release();
                    mediaPlayer = android.media.MediaPlayer.create(getApplicationContext(), Uri.fromFile(audioFile));
                    mediaPlayer.start();
                } else if (mediaPlayer == null) {
                    mediaPlayer = android.media.MediaPlayer.create(getApplicationContext(), Uri.fromFile(audioFile));
                    mediaPlayer.start();
                }
                new getMusicInfo().execute();

                mediaPlayer.setOnCompletionListener(listener);
            } else if (op == 2) {
                /**
                 * 播放暂停
                 */
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    /**
                     * 发广播
                     */
//                    Intent intent1 = new Intent("com.example.gongzhiyao.oplayer.action.music_playerReceiver");
//                    intent1.putExtra("op", 6);
//                    sendBroadcast(intent1);
                    stop_MainActivity();
                    stop_MusicPlayer();

                    /**
                     * 还要再写一个receiver在musicplayer中更改状态
                     *
                     */

//                    Intent intent2 = new Intent("com.example.gongzhiyao.oplayer.action.musicReceiver");
//                    intent2.putExtra("op", 1);
//                    sendBroadcast(intent2);


                    remoteViews.setInt(R.id.ib_music_playStop_notification, "setBackgroundResource", R.drawable.music_play);
                    mNotificationManager.notify(1, builder.build());


                } else if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    am.requestAudioFocus(afChangeListener,
                            AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                    mediaPlayer.start();
                    /**
                     * 发广播
                     */

//                    Intent intent1 = new Intent("com.example.gongzhiyao.oplayer.action.music_playerReceiver");
//                    intent1.putExtra("op", 7);
//                    intent1.putExtra("path", audioFile.getPath());
//                    sendBroadcast(intent1);
//
//                    Intent intent2 = new Intent("com.example.gongzhiyao.oplayer.action.musicReceiver");
//                    intent2.putExtra("op", 2);
//                    sendBroadcast(intent2);
                    start_MianActivity();
                    start_MusicPlayer();

                    remoteViews.setInt(R.id.ib_music_playStop_notification, "setBackgroundResource", R.drawable.music_stop);

                    startForeground(1, builder.build());
                }
            } else if (op == 3) {
                /**
                 *播放下一首
                 */
                log.d("接收到发送过来的33333333333333333333333");
                if (mediaPlayer != null) {
                    mediaPlayer.pause();
                    mediaPlayer.release();
                    /**
                     * 在这里要区分各种播放格式
                     * 同时还有一个问题就是怎么使其有序
                     * 还有就是怎么实现单曲循环（简单）
                     */

                    Random_Next();


                } else if (mediaPlayer == null) {
                    Random_Next();
                }
            } else if (op == 4) {
                mediaPlayer.pause();
                Intent intent1 = new Intent("com.example.gongzhiyao.oplayer.action.music_playerReceiver");
                intent1.putExtra("op", 8);
                sendBroadcast(intent1);

                stopForeground(true);
                mNotificationManager.cancel(1);

            } else if (op == 5) {
                /**
                 * 滑动给变位置
                 */
                long seekTime = intent.getLongExtra("seekTime", -1);
                log.d("得到的是" + seekTime);
                if (seekTime != -1) {
                    mediaPlayer.seekTo((int) seekTime);
                }

            }else if(op==6){
                /**上一首**/
                if (mediaPlayer != null) {
                    mediaPlayer.pause();
                    mediaPlayer.release();

                   Random_pre();
                } else if (mediaPlayer == null) {
                    Random_pre();
                }




            }




            /**
             * 想通过线程获得音乐播放进度，但是发现有时会报错
             */


            handler.sendEmptyMessage(1);


        }

        return START_NOT_STICKY;
    }

    int id = -1;

    private void Random_Next() {
        int loopType = sp.getInt("loopType", 2);
        if (loopType == 2) {
            /**随机播放**/
            int length = name.size();
            int newMusic = (int) (Math.random() * length);
            /**
             * 在这里要把传过来的数据做一个持久化
             */
            audioFile = new File(path.get(newMusic).toString());
            mediaPlayer = android.media.MediaPlayer.create(getApplicationContext(), Uri.fromFile(audioFile));
        } else if (loopType == 1) {
            /**单曲循环**/
            mediaPlayer = android.media.MediaPlayer.create(getApplicationContext(), Uri.fromFile(audioFile));
        } else if (loopType == 3) {
            /**列表播放**/
            // 在这里还不可以使用数据库，因为有可能用户并没有点击
            String sq = "select * from " + DB.MUSIC_TABLENAME;
            Cursor cursor = dbread.rawQuery(sq, null);
            String sq1 = "select * from " + DB.MUSIC_TABLENAME + " where name=?";
            Cursor cursor1 = dbread.rawQuery(sq1, new String[]{audioFile.getName()});
            if (cursor.getCount() == 0) {
                /**数据库还没有数据**/
                int length = name.size();
                int newMusic = (int) (Math.random() * length);
                audioFile = new File(path.get(newMusic).toString());
                mediaPlayer = android.media.MediaPlayer.create(getApplicationContext(), Uri.fromFile(audioFile));
            } else {
                if (name.size() != cursor.getCount() || cursor1.getCount() == 0) {
                    /**数据不同，表示用户在一个目录下打开了播放界面，而在当前目录没有打开**/
                    int length = name.size();
                    int newMusic = (int) (Math.random() * length);
                    audioFile = new File(path.get(newMusic).toString());
                    mediaPlayer = android.media.MediaPlayer.create(getApplicationContext(), Uri.fromFile(audioFile));
                } else {
                    /**当前播放的在自己的目录下，而且还拥有数据库**/
                    //感觉要写一个视图，用于查询，播放
                    String sq3="select * from "+DB.READY_MUSIC;
                    Cursor c=dbread.rawQuery(sq3,null);
                    String name = audioFile.getName();
                    String sq2="select * from "+DB.READY_MUSIC+" where "+DB.MUSIC_NAME+" =?";
                    Cursor cursor2=dbread.rawQuery(sq2,new String[]{name});
                    while (cursor2.moveToNext()){
//                        String path=cursor2.getString(cursor2.getColumnIndex(DB.MUSIC_PATH));
//                        audioFile=new File(path);
                        id=cursor2.getInt(cursor2.getColumnIndex("_id"));
                    }
                    if(id==c.getCount()){
                        id=0;
                    }
                    log.d("id是"+id);
                    c.moveToPosition(id);
                    String path=c.getString(c.getColumnIndex(DB.MUSIC_PATH));
                    audioFile=new File(path);
                    mediaPlayer = android.media.MediaPlayer.create(getApplicationContext(), Uri.fromFile(audioFile));
                }
            }

        }
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(listener);
        /**
         * 用于更新主界面
         */
        Intent intent = new Intent("com.example.gongzhiyao.oplayer.action.music_playerReceiver");
        intent.putExtra("op", 1);
        intent.putExtra("path", audioFile.getPath());
        sendBroadcast(intent);
        /**
         *用于更新详细的music界面
         */
        Intent intent1 = new Intent("com.example.gongzhiyao.oplayer.action.musicReceiver");
        intent1.putExtra("op", 4);
        intent1.putExtra("path", audioFile.getPath());
        sendBroadcast(intent1);

        new getMusicInfo().execute();
    }




    private void Random_pre() {
        int loopType = sp.getInt("loopType", 2);
        if (loopType == 2) {
            /**随机播放**/
            int length = name.size();
            int newMusic = (int) (Math.random() * length);
            audioFile = new File(path.get(newMusic).toString());
            mediaPlayer = android.media.MediaPlayer.create(getApplicationContext(), Uri.fromFile(audioFile));
        } else if (loopType == 1) {
            /**单曲循环**/
            mediaPlayer = android.media.MediaPlayer.create(getApplicationContext(), Uri.fromFile(audioFile));
        } else if (loopType == 3) {
            /**列表播放**/
            // 在这里还不可以使用数据库，因为有可能用户并没有点击
            String sq = "select * from " + DB.MUSIC_TABLENAME;
            Cursor cursor = dbread.rawQuery(sq, null);
            String sq1 = "select * from " + DB.MUSIC_TABLENAME + " where name=?";
            Cursor cursor1 = dbread.rawQuery(sq1, new String[]{audioFile.getName()});
            if (cursor.getCount() == 0) {
                /**数据库还没有数据**/
                int length = name.size();
                int newMusic = (int) (Math.random() * length);
                audioFile = new File(path.get(newMusic).toString());
                mediaPlayer = android.media.MediaPlayer.create(getApplicationContext(), Uri.fromFile(audioFile));
            } else {
                if (name.size() != cursor.getCount() || cursor1.getCount() == 0) {
                    /**数据不同，表示用户在一个目录下打开了播放界面，而在当前目录没有打开**/
                    int length = name.size();
                    int newMusic = (int) (Math.random() * length);
                    audioFile = new File(path.get(newMusic).toString());
                    mediaPlayer = android.media.MediaPlayer.create(getApplicationContext(), Uri.fromFile(audioFile));
                } else {
                    /**当前播放的在自己的目录下，而且还拥有数据库**/
                    //感觉要写一个视图，用于查询，播放
                    String sq3="select * from "+DB.READY_MUSIC;
                    Cursor c=dbread.rawQuery(sq3,null);
                    String name = audioFile.getName();
                    String sq2="select * from "+DB.READY_MUSIC+" where "+DB.MUSIC_NAME+" =?";
                    Cursor cursor2=dbread.rawQuery(sq2,new String[]{name});
                    while (cursor2.moveToNext()){
//                        String path=cursor2.getString(cursor2.getColumnIndex(DB.MUSIC_PATH));
//                        audioFile=new File(path);
                        id=cursor2.getInt(cursor2.getColumnIndex("_id"));
                    }
                    if(id==0){
                        id=c.getCount();
                    }
                    log.d("id是"+id);
                    c.moveToPosition(id-2);
                    String path=c.getString(c.getColumnIndex(DB.MUSIC_PATH));
                    audioFile=new File(path);
                    mediaPlayer = android.media.MediaPlayer.create(getApplicationContext(), Uri.fromFile(audioFile));
                }
            }

        }
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(listener);
        /**
         * 用于更新主界面
         */
        Intent intent = new Intent("com.example.gongzhiyao.oplayer.action.music_playerReceiver");
        intent.putExtra("op", 1);
        intent.putExtra("path", audioFile.getPath());
        sendBroadcast(intent);
        /**
         *用于更新详细的music界面
         */
        Intent intent1 = new Intent("com.example.gongzhiyao.oplayer.action.musicReceiver");
        intent1.putExtra("op", 4);
        intent1.putExtra("path", audioFile.getPath());
        sendBroadcast(intent1);

        new getMusicInfo().execute();
    }




    @Override
    public IBinder onBind(Intent intent) {
        return new MsgBinder();
    }

    public class MsgBinder extends Binder {
        /**
         * 获取当前Service的实例
         *
         * @return
         */
        public long getPosition() {
            if (mediaPlayer != null) {
                return mediaPlayer.getCurrentPosition();
            } else {
                return 0;
            }
        }
    }

    private android.media.MediaPlayer.OnCompletionListener listener = new android.media.MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(android.media.MediaPlayer mp) {
            /**
             * 播放完成
             */

            /**
             * 在这里要区分各种播放格式
             * 同时还有一个问题就是怎么使其有序
             * 还有就是怎么实现单曲循环（简单）
             */

            Random_Next();

        }
    };

    boolean mPausedByTransientLossOfFocus;

    AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        public void onAudioFocusChange(int focusChange) {
            /**
             * 短暂失去焦点
             */
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                if (mediaPlayer.isPlaying()) {
                    mPausedByTransientLossOfFocus = true;
                    mediaPlayer.pause();
                    //*********************
                    /**
                     * 发送广播
                     */
//                    Intent intent = new Intent("com.example.gongzhiyao.oplayer.action.music_playerReceiver");
//                    intent.putExtra("op", 3);
//                    intent.putExtra("path", audioFile.getPath());
//                    sendBroadcast(intent);
//
//
//                    Intent intent2 = new Intent("com.example.gongzhiyao.oplayer.action.musicReceiver");
//                    intent2.putExtra("op", 1);
//                    sendBroadcast(intent2);
                    stop_MusicPlayer();
                    stop_MainActivity();


                    remoteViews.setInt(R.id.ib_music_playStop_notification, "setBackgroundResource", R.drawable.music_play);
                    mNotificationManager.notify(1, builder.build());


                }

            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                /**
                 * 获得焦点
                 */
                if (mediaPlayer == null) {
                    /**
                     * 如果为空，则随机一首
                     */

                    int listType = LocalVideo.sp.getInt(LocalVideo.ListType, -1);

                    if (listType == LocalVideo.List_Type_FileDirectory) {

                        int length = name.size();

                        int newMusic = (int) (Math.random() * length);
                        audioFile = new File(path.get(newMusic).toString());

                        mediaPlayer = android.media.MediaPlayer.create(getApplicationContext(), Uri.fromFile(audioFile));

                        mediaPlayer.start();
                        mediaPlayer.setOnCompletionListener(listener);
                        /**
                         * 发送广播
                         */
//                        Intent intent = new Intent("com.example.gongzhiyao.oplayer.action.music_playerReceiver");
//                        intent.putExtra("op", 2);
//                        intent.putExtra("path", audioFile.getPath());
//                        sendBroadcast(intent);
//
//
//
//
//                        Intent intent2 = new Intent("com.example.gongzhiyao.oplayer.action.musicReceiver");
//                        intent2.putExtra("op", 2);
//                        sendBroadcast(intent2);

                        start_MusicPlayer();
                        start_MianActivity();


                    } else if (listType == LocalVideo.List_Type_OnlyAudio) {


                        int length = name.size();
                        int newMusic = (int) (Math.random() * length);
                        audioFile = new File(path.get(newMusic).toString());
                        mediaPlayer = android.media.MediaPlayer.create(getApplicationContext(), Uri.fromFile(audioFile));
                        mediaPlayer.start();
                        mediaPlayer.setOnCompletionListener(listener);
                        /**
                         * 发送广播
                         */
//                        Intent intent = new Intent("com.example.gongzhiyao.oplayer.action.music_playerReceiver");
//                        intent.putExtra("op", 2);
//                        intent.putExtra("path", audioFile.getPath());
//                        sendBroadcast(intent);
//
//
//                        Intent intent2 = new Intent("com.example.gongzhiyao.oplayer.action.musicReceiver");
//                        intent2.putExtra("op", 2);
//                        sendBroadcast(intent2);
                        start_MusicPlayer();
                        start_MianActivity();

                    }
                } else if (!mediaPlayer.isPlaying() && mPausedByTransientLossOfFocus) {
                    mPausedByTransientLossOfFocus = false;
                    mediaPlayer.start();

                    /**
                     * 发送广播
                     */
//                    Intent intent = new Intent("com.example.gongzhiyao.oplayer.action.music_playerReceiver");
//                    intent.putExtra("op", 4);
//                    intent.putExtra("path", audioFile.getPath());
//                    sendBroadcast(intent);
//
//
//                    Intent intent2 = new Intent("com.example.gongzhiyao.oplayer.action.musicReceiver");
//                    intent2.putExtra("op", 2);
//                    sendBroadcast(intent2);
                    start_MusicPlayer();
                    start_MianActivity();


                    remoteViews.setInt(R.id.ib_music_playStop_notification, "setBackgroundResource", R.drawable.music_stop);
                    mNotificationManager.notify(1, builder.build());

                }
                // Resume playback

            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                /**
                 * 会长时间失去焦点，一班播放音乐就是长时间失去音频焦点，但是不要把mediaplayer释放。
                 */
                if (mediaPlayer.isPlaying()) {
                    mPausedByTransientLossOfFocus = false;
                    mediaPlayer.pause();
                    //*********************
                    /**
                     * 发送广播
                     */
//                    Intent intent = new Intent("com.example.gongzhiyao.oplayer.action.music_playerReceiver");
//                    intent.putExtra("op", 5);
//                    intent.putExtra("path", audioFile.getPath());
//                    sendBroadcast(intent);
//
//
//                    Intent intent2 = new Intent("com.example.gongzhiyao.oplayer.action.musicReceiver");
//                    intent2.putExtra("op", 1);
//                    sendBroadcast(intent2);
                    stop_MainActivity();
                    stop_MusicPlayer();


                    remoteViews.setInt(R.id.ib_music_playStop_notification, "setBackgroundResource", R.drawable.music_play);
                    mNotificationManager.notify(1, builder.build());


                }
                am.abandonAudioFocus(afChangeListener);
                // Stop playback
            } else if (focusChange == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }

            } else if (focusChange == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        log.d("服务已销毁");
        dbread.close();
        db.close();


    }


    String tilte;
    String artist;
    Bitmap bitmap;
    long duration = 233535;
    String path_music;

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


                bitmap = MainActivity.getMusicBitemp(getApplicationContext(), ID, albumId);


            }


            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            remoteViews.setImageViewBitmap(R.id.iv_music_notification, bitmap);
            remoteViews.setTextViewText(R.id.tv_music_song_notification, tilte);
            remoteViews.setTextViewText(R.id.tv_music_singer_notification, artist);
            mNotificationManager.notify(1, builder.build());
        }
    }


    public void start_MusicPlayer() {
        Intent intent2 = new Intent("com.example.gongzhiyao.oplayer.action.musicReceiver");
        intent2.putExtra("op", 2);
        sendBroadcast(intent2);
    }

    public void stop_MusicPlayer() {
        Intent intent2 = new Intent("com.example.gongzhiyao.oplayer.action.musicReceiver");
        intent2.putExtra("op", 1);
        sendBroadcast(intent2);
    }

    public void start_MianActivity() {
        Intent intent1 = new Intent("com.example.gongzhiyao.oplayer.action.music_playerReceiver");
        intent1.putExtra("op", 7);
        intent1.putExtra("path", audioFile.getPath());
        sendBroadcast(intent1);
    }

    public void stop_MainActivity() {
        Intent intent1 = new Intent("com.example.gongzhiyao.oplayer.action.music_playerReceiver");
        intent1.putExtra("op", 6);
        sendBroadcast(intent1);
    }


}
