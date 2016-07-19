package com.example.gongzhiyao.oplayer;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.transition.Explode;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gongzhiyao.oplayer.Adapter.FileAdapter;
import com.example.gongzhiyao.oplayer.Application.SysApplication;
import com.example.gongzhiyao.oplayer.Local.music_Player;
import com.example.gongzhiyao.oplayer.Log.L;
import com.example.gongzhiyao.oplayer.SelectPopMenu.SelectPopMenu;
import com.example.gongzhiyao.oplayer.Service.MusicService;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import io.vov.vitamio.MediaPlayer;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LocalVideo.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LocalVideo#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LocalVideo extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private TextView tv_APPTitle, tv_Local_path;
    private ImageButton ib_Refresh_Local, ib_More_Local;
    private ListView lv_Local;
    private static final int SEND_TO_POP = 1;
    private OnFragmentInteractionListener mListener;
    public static final String ROOT_PATH = "/mnt";
    //存储文件名称
    public static ArrayList<String> names = null;
    //存储文件路径
    public static ArrayList<String> paths = null;
    //当前文件夹下的音乐文件名
    private ArrayList<String> names_In_Folder = null;
    //当前文件夹下的音乐路径
    private ArrayList<String> path_In_Folder = null;


    public static ArrayList<String> names_Audio = null;
    public static ArrayList<String> paths_Audio = null;
    public static ArrayList<String> names_Audio_Dir = null;
    public static ArrayList<String> paths_Audio_Dir = null;


    public static SharedPreferences sp;


    private EditText editText;
    private Button btn_comfirm, btn_dismiss;
    private TextView tv_Local, tv_Info;
    private File file_current;
    private L log;

    public static final int List_Type_FileDirectory = 1;
    public static final int List_Type_OnlyAudio = 2;
    private static final int Circle_Single = 1;
    private static final int Circle_Random = 2;
    private static final int Circle_List = 3;
    public static final String ListType = "listType";
    public NotificationManager mNotificationManager;

    AudioManager am;



    public LocalVideo() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LocalVideo.
     */
    // TODO: Rename and change types and number of parameters
    public static LocalVideo newInstance(String param1, String param2) {
        LocalVideo fragment = new LocalVideo();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log.d("播放界面onCreate");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        sp = getActivity().getSharedPreferences("sp", Context.MODE_PRIVATE);
        IntentFilter intentFilter = new IntentFilter("com.example.gongzhiyao.oplayer.action.UIReceiver");
//        getActivity().registerReceiver(new UIReceiver(), intentFilter);




    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_local_video, container, false);
        am = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);


//        NotificationCompat.Builder mbuilder=new NotificationCompat.Builder(getActivity());


        sp.edit().putInt("listType", List_Type_FileDirectory).commit();
        initView(view);
        showFileDir(ROOT_PATH);
        lv_Local.setOnItemClickListener(list_catalog_Listener);
        lv_Local.setOnItemLongClickListener(list_catalog_Long_Listener);
        log = new L();
        return view;
    }

    private void initView(View view) {
        tv_APPTitle = (TextView) view.findViewById(R.id.APPTitle_Local);
        ib_Refresh_Local = (ImageButton) view.findViewById(R.id.ib_Refresh_Local);
        ib_More_Local = (ImageButton) view.findViewById(R.id.ib_More_Local);
        lv_Local = (ListView) view.findViewById(R.id.lv_Local);
        tv_Local = (TextView) view.findViewById(R.id.tv_Local);
        tv_Local_path = (TextView) view.findViewById(R.id.tv_Local_path);
//        Local_Music_player = (LinearLayout) view.findViewById(R.id.Local_Music_Player);
//        iv_Local_Music = (ImageButton) view.findViewById(R.id.iv_music);
//        btn_Local_Playe_Or_Stop = (ImageButton) view.findViewById(R.id.btn_Local_Play_Or_Stop);
//        tv_Local_Music_Name = (TextView) view.findViewById(R.id.tv_Local_Music_Name);
//        tv_Local_Music_Singer = (TextView) view.findViewById(R.id.tv_Local_Music_Singer);
//        timeOfSong = (Chronometer) view.findViewById(R.id.time_Of_Music);
//        progressBar_Local = (ProgressBar) view.findViewById(R.id.progress_Bar_Local);
//        timeOfSong.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
//            @Override
//            public void onChronometerTick(Chronometer chronometer) {
//                String time = chronometer.getText().toString();
//                String min1 = time.substring(0, 1);
//                if (min1.equals("0")) {
//                    String min2 = time.substring(1, 2);
//                    int min = Integer.parseInt(min2);
//                    long ms_min = min * 60000;
//                    String sec1 = time.substring(3);
//                    int sec = Integer.parseInt(sec1);
//                    long ms_sec = sec * 1000;
//                    long ms_total = ms_min + ms_sec;
//                    /**
//                     * progress有问题
//                     */
//
//
//                    int progress = (int) ((float) (ms_total * 50000 / duration));
////                    log.d(progress + "");
//                    progressBar_Local.setProgress(progress);
//
////                    log.d(ms_total + "                 111111111111111           " + duration);
//                } else {
//                    String min2 = time.substring(0, 2);
//                    int min = Integer.parseInt(min2);
//                    long ms_min = min * 60000;
//                    String sec2 = time.substring(3);
//                    int sec = Integer.parseInt(sec2);
//                    long ms_sec = sec * 1000;
//                    long ms_total = ms_min + ms_sec;
//
//                    log.d(ms_total + "           22222222222222222222           " + duration);
//                    int progress1 = (int) ((float) (ms_total * 50000 / duration));
////                    log.d(progress + "");
//                    progressBar_Local.setProgress(progress1);
//                }
//            }
//        });


        tv_APPTitle.setOnClickListener(this);
        ib_Refresh_Local.setOnClickListener(this);
        ib_More_Local.setOnClickListener(this);

//        iv_Local_Music.setOnClickListener(this);
//        btn_Local_Playe_Or_Stop.setOnClickListener(this);
//        tv_Local_Music_Name.setOnClickListener(this);
//        tv_Local_Music_Singer.setOnClickListener(this);

    }


    private void showFileDir(String path) {
        names = new ArrayList<String>();
        paths = new ArrayList<String>();
        names_Audio_Dir = new ArrayList<String>();
        paths_Audio_Dir = new ArrayList<String>();
        tv_Local_path.setText(path);

        File file = new File(path);
        File[] files = file.listFiles();
        file_current = file;
//        System.out.println(file_current.getAbsolutePath());
        //如果当前目录不是根目录
        if (!ROOT_PATH.equals(path)) {
            names.add("@1");
            paths.add(ROOT_PATH);
            names.add("@2");
            paths.add(file.getParent());
            //添加所有文件
            for (File f : files) {
                if (f.isDirectory() || getMIMEType(f).equals("video/*") || getMIMEType(f).equals("audio/*")) {
                    names.add(f.getName());
                    paths.add(f.getPath());
                }
                if (getMIMEType(f).equals("audio/*")) {
                    names_Audio_Dir.add(f.getName());
                    paths_Audio_Dir.add(f.getPath());
                }
            }
        } else if (ROOT_PATH.equals(path)) {

            for (File f : files) {
                if (f.getName().equals("usb") || f.getName().equals("sdcard")) {
                    names.add(f.getName());
                    paths.add(f.getPath());
                }
            }
        }
        lv_Local.setAdapter(new FileAdapter(getActivity(), names, paths));
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


    private void displayToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    String path_music;
    private File audioFile;
    RemoteViews remoteViews;
    private AdapterView.OnItemClickListener list_catalog_Listener = new AdapterView.OnItemClickListener() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {


            String path = paths.get(position);
            final File file = new File(path);
            // 文件存在并可读
            if (file.exists() && file.canRead()) {
                if (file.isDirectory()) {
                    showFileDir(path);
                } else if (getMIMEType(file).equals("video/*")) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), LocalPlayer.class);
                    intent.putExtra("path", file.getAbsolutePath().toString());
                    startActivity(intent);
                } else if (getMIMEType(file).equals("audio/*")) {


                    Intent i1=new Intent("com.example.gongzhiyao.oplayer.action.music_playerReceiver");
                    i1.putExtra("op",11);
                    i1.putExtra("path",path);
                    getActivity().sendBroadcast(i1);


//                    sp.edit().putBoolean("player_visible",true).commit();
//                    audioFile = file;
//                    Intent i = new Intent(getActivity(), MusicService.class);
//                    i.putExtra("op", 1);
//                    i.putExtra("path", path);
//                    log.d("要发送的路径是" + path);
//                    getActivity().startService(i);
//                    Local_Music_player.setVisibility(View.VISIBLE);
//                    Resources resources = getActivity().getResources();
//                    Drawable drawable_stop = resources.getDrawable(R.drawable.stop);
//                    btn_Local_Playe_Or_Stop.setBackground(drawable_stop);
//                    new getMusicInfo().execute();
//                    timeOfSong.setBase(SystemClock.elapsedRealtime());
//                    timeOfSong.start();
//
//
//                    remoteViews = new RemoteViews(getActivity().getPackageName(),
//                            R.layout.statusbar);
//
//                    remoteViews.setInt(R.id.ib_music_playStop_notification, "setBackgroundResource", R.drawable.music_stop);





////                    remoteViews.setBitmap(R.id.iv_music_notification,null,bitmap);
//                    remoteViews.setTextViewText(R.id.tv_music_song_notification,"3333333333333");
//                    remoteViews.setTextViewText(R.id.tv_music_singer_notification,"22222222222");
//
//                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext());
//                    builder.setContent(remoteViews).setSmallIcon(R.drawable.ic_launcher)
//                            .setOngoing(true)
//                            .setTicker("music is playing");
//                    mNotificationManager.notify(1, builder.build());


//


                /*    audioFile = file;
                    Local_Music_player.setVisibility(View.VISIBLE);
                    Resources resources = getActivity().getResources();
                    Drawable drawable_stop = resources.getDrawable(R.drawable.stop);
                    btn_Local_Playe_Or_Stop.setBackground(drawable_stop);
                   // pauseMusic();

                    am.requestAudioFocus(afChangeListener,
                            AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

                    if (mediaPlayer != null) {

                        mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer = android.media.MediaPlayer.create(getActivity(), Uri.fromFile(file));
                        mediaPlayer.start();
                    } else if (mediaPlayer == null) {
                        mediaPlayer = android.media.MediaPlayer.create(getActivity(), Uri.fromFile(file));
                        mediaPlayer.start();
                    }
                    new getMusicInfo().execute();

                    mediaPlayer.setOnCompletionListener(new android.media.MediaPlayer.OnCompletionListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        @Override
                        public void onCompletion(android.media.MediaPlayer mp) {


//                            timeOfSong.setText("00:00");
//                            Resources resources = getActivity().getResources();
//                            Drawable drawable_play = resources.getDrawable(R.drawable.play);
//                            btn_Local_Playe_Or_Stop.setBackground(drawable_play);
//

                            *//**
                     * 在每次开始都会初始化为单曲循环，要向更改循环方式就需要进入列表项，然后使用sp
                     * 存储个数大小还有信息，或是直接在数据库中，然后在进行其他的循环就可以了
                     * 在这里都能get到了
                     *//*

                            int listType = sp.getInt(ListType, -1);
                            log.d("list的类别是" + listType);
                            log.d("播放结束");
                            if (listType == List_Type_FileDirectory) {
                                log.d("播放结束1111111111111111111");
                                log.d("其中的音乐文件有   " + names_Audio_Dir.size());
                                int length = names_Audio_Dir.size();

                                int newMusic = (int) (Math.random() * length);
                                while (true) {
                                    if (newMusic == position) {
                                        newMusic = (int) (Math.random() * length);
                                    } else {
                                        break;
                                    }
                                }

                                audioFile = new File(paths_Audio_Dir.get(newMusic).toString());

                                mediaPlayer = android.media.MediaPlayer.create(getActivity(), Uri.fromFile(audioFile));

                                mediaPlayer.start();
                                mediaPlayer.setOnCompletionListener(this);
                                progressBar_Local.setProgress(0);
                                timeOfSong.setBase(SystemClock.elapsedRealtime());
                                timeOfSong.start();
                                new getMusicInfo().execute();
                            } else if (listType == List_Type_OnlyAudio) {

                                log.d("播放结束22222222222222222222222");
                                int length = names.size();
                                int newMusic = (int) (Math.random() * length);
                                while (true) {
                                    if (newMusic == position) {
                                        newMusic = (int) (Math.random() * length);
                                    } else {
                                        break;
                                    }
                                }
                                audioFile = new File(paths.get(newMusic).toString());
                                mediaPlayer = android.media.MediaPlayer.create(getActivity(), Uri.fromFile(audioFile));
                                mediaPlayer.start();
                                mediaPlayer.setOnCompletionListener(this);
                                progressBar_Local.setProgress(0);
                                timeOfSong.setBase(SystemClock.elapsedRealtime());
                                timeOfSong.start();
                                new getMusicInfo().execute();
                            }


                            *//**
                     * 如果是全部音频的话 就用names和paths来获取数据
                     *//*


//                            int Loop_Mode = sp.getInt("loop_Mode", -1);
//                            if (Loop_Mode == Circle_Single) {
//                                progressBar_Local.setProgress(0);
//                                timeOfSong.setBase(SystemClock.elapsedRealtime());
//                                timeOfSong.start();
//                                mediaPlayer.start();
//                            } else if (Loop_Mode == Circle_Random) {
//
//                            } else if (Loop_Mode == Circle_List) {
//
//                            }

                            *//**
                     *
                     * 再下个activity中要设置播放方式，存到sp里面，然后在这里得到并且设置相应的播放方式
                     *
                     *
                     * 在这里写成播放完成后，继续播放下一首
                     *//*


                        }

                    });

                    timeOfSong.setBase(SystemClock.elapsedRealtime());

                    timeOfSong.start();*/


                }
            }
            //没有权限
            else {

                new AlertDialog.Builder(getActivity()).setTitle("提醒")
                        .setMessage("检测不到U盘！")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
            }

        }
    };


//    private android.media.MediaPlayer.OnCompletionListener listener = new android.media.MediaPlayer.OnCompletionListener() {
//        @Override
//        public void onCompletion(android.media.MediaPlayer mp) {
//            int listType = sp.getInt(ListType, -1);
//            log.d("list的类别是" + listType);
//            log.d("播放结束");
//            if (listType == List_Type_FileDirectory) {
//                log.d("播放结束1111111111111111111");
//                log.d("其中的音乐文件有   " + names_Audio_Dir.size());
//                int length = names_Audio_Dir.size();
//
//                int newMusic = (int) (Math.random() * length);
//                audioFile = new File(paths_Audio_Dir.get(newMusic).toString());
//
//                mediaPlayer = android.media.MediaPlayer.create(getActivity(), Uri.fromFile(audioFile));
//
//                mediaPlayer.start();
//                mediaPlayer.setOnCompletionListener(listener);
//                progressBar_Local.setProgress(0);
//                timeOfSong.setBase(SystemClock.elapsedRealtime());
//                timeOfSong.start();
//                new getMusicInfo().execute();
//            } else if (listType == List_Type_OnlyAudio) {
//
//                log.d("播放结束22222222222222222222222");
//                int length = names.size();
//                int newMusic = (int) (Math.random() * length);
//                audioFile = new File(paths.get(newMusic).toString());
//                mediaPlayer = android.media.MediaPlayer.create(getActivity(), Uri.fromFile(audioFile));
//                mediaPlayer.start();
//                mediaPlayer.setOnCompletionListener(listener);
//                progressBar_Local.setProgress(0);
//                timeOfSong.setBase(SystemClock.elapsedRealtime());
//                timeOfSong.start();
//                new getMusicInfo().execute();
//            }
//
//        }
//    };




//    String tilte;
//    String artist;
//    Bitmap bitmap;
//    long duration = 233535;
//
//    private class getMusicInfo extends AsyncTask<Void, Void, Void> {
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            ContentResolver musicResolver = getActivity().getContentResolver();
//            Cursor cursor = null;
//            try {
////                log.d(audioFile.getCanonicalPath().toString());
//                String path = audioFile.getCanonicalPath().toString();
//                log.d(path.substring(18, 19));
//                String sign = path.substring(18, 19);
//
//                if (!sign.equals("0")) {
//                    path_music = "/storage/emulated/0" + audioFile.getCanonicalPath().substring(24);
//                } else if (sign.equals("0")) {
//                    path_music = audioFile.getCanonicalPath().toString();
//                }
//                log.d(path_music);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            cursor = musicResolver.query(
//                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media.DATA + "= ? ",
//                    new String[]{path_music}, null);
////                    cursor = musicResolver.query(
////                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media.DATA +"= ? or "+ MediaStore.Audio.Media.DATA+"= ?",
////                            new String[]{path_music,}, null);
//
//            if (cursor != null && cursor.getCount() > 0) {
//                cursor.moveToFirst();
//
//                long ID = cursor.getLong(cursor
//                        .getColumnIndex(MediaStore.Audio.Media._ID));    //音乐id
//                tilte = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
//                artist = cursor.getString(cursor
//                        .getColumnIndex(MediaStore.Audio.Media.ARTIST)); // 艺术家
//                String album = cursor.getString(cursor
//                        .getColumnIndex(MediaStore.Audio.Media.ALBUM));    //专辑
////                            long albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
//                duration = cursor.getLong(cursor
//                        .getColumnIndex(MediaStore.Audio.Media.DURATION)); // 时长
////                            long size = cursor.getLong(cursor
////                                    .getColumnIndex(MediaStore.Audio.Media.SIZE));
////                            String url = cursor.getString(cursor
////                                    .getColumnIndex(MediaStore.Audio.Media.DATA));
//
//                long albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
//
//
//                bitmap = getMusicBitemp(getActivity(), ID, albumId);
//
//
//            }
//
//
//            return null;
//        }
//
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//            tv_Local_Music_Name.setText(tilte);
//            tv_Local_Music_Singer.setText(artist);
//            iv_Local_Music.setImageBitmap(bitmap);
//
//
////            View view=LayoutInflater.from(getActivity()).inflate(R.layout.statusbar,null);
////            ImageView iv= (ImageView) view.findViewById(R.id.iv_music_notification);
////            iv.setImageBitmap(bitmap);
//            remoteViews.setImageViewBitmap(R.id.iv_music_notification, bitmap);
//            remoteViews.setTextViewText(R.id.tv_music_song_notification, tilte);
//            remoteViews.setTextViewText(R.id.tv_music_singer_notification, artist);
//
//            Intent buttonPlayIntent = new Intent(getActivity(), MusicService.class);
//            buttonPlayIntent.putExtra("op", 2);
//            PendingIntent pendButtonPlayIntent = PendingIntent.getService(getActivity(), 0, buttonPlayIntent, 0);
////            PendingIntent pendButtonPlayIntent = PendingIntent.getBroadcast(getActivity(), 0, buttonPlayIntent, 0);
//            remoteViews.setOnClickPendingIntent(R.id.ib_music_playStop_notification, pendButtonPlayIntent);
//
//            Intent buttonNextIntent = new Intent(getActivity(), MusicService.class);
//            buttonNextIntent.putExtra("op", 3);
//
//            PendingIntent pendButtonNextIntent = PendingIntent.getService(getActivity(), 1, buttonNextIntent, 0);
//            remoteViews.setOnClickPendingIntent(R.id.ib_music_next_notification, pendButtonNextIntent);
//
//
//            Intent buttonStopIntent = new Intent(getActivity(), MusicService.class);
//            buttonStopIntent.putExtra("op", 4);
//            PendingIntent pendButtonStopIntent = PendingIntent.getService(getActivity(), 3, buttonStopIntent, 0);
//            remoteViews.setOnClickPendingIntent(R.id.ib_music_stop_notification, pendButtonStopIntent);
//
//
//            builder = new NotificationCompat.Builder(getContext());
//            builder.setContent(remoteViews).setSmallIcon(R.drawable.ic_launcher)
//                    .setOngoing(true)
//                    .setTicker("music is playing");
//            mNotificationManager.notify(1, builder.build());
//
//
////
////            remoteViews.setBitmap(R.id.iv_music_notification,null,bitmap);
////            remoteViews.setTextViewText(R.id.tv_music_song_notification,tilte);
////            remoteViews.setTextViewText(R.id.tv_music_singer_notification,artist);
////
//
////            timeOfSong.setText(formatTime(duration));
//
////            timeOfSong.setBase(System.currentTimeMillis());
//
//
//        }
//    }


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
//        return bm;
        return Bitmap.createScaledBitmap(bm, 150, 150, true);
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    File file;
    AlertDialog dialog_Rename_Local;
    AlertDialog dialog_Delete_Local;
    AlertDialog dialog_Cover_Local;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SEND_TO_POP && resultCode == getActivity().RESULT_OK) {
            int Op = data.getIntExtra("Op", -1);
            String path = data.getStringExtra("path");
            file = new File(path);
            if (Op > -1) {
                switch (Op) {
                    case 1:
                        log.d("进行重命名操作");
                        AlertDialog.Builder dialog_rename = new AlertDialog.Builder(getActivity());
                        View view = LayoutInflater.from(getActivity()).inflate(R.layout.rename_dialog, null);
                        view.findViewById(R.id.editText);
                        editText = (EditText) view.findViewById(R.id.editText);
                        btn_comfirm = (Button) view.findViewById(R.id.btn_comfirm);
                        btn_dismiss = (Button) view.findViewById(R.id.btn_dismiss);
                        btn_dismiss.setOnClickListener(this);
                        btn_comfirm.setOnClickListener(this);
                        editText.setText(file.getName());
//                        dialog_rename.setTitle("修改文件名");
                        dialog_rename.setView(view);
                        dialog_Rename_Local = dialog_rename.create();
                        dialog_Rename_Local.show();


                        break;
                    case 2:
                        log.d("进行删除操作");
                        AlertDialog.Builder dialog_Delete = new AlertDialog.Builder(getActivity());
                        View view1 = LayoutInflater.from(getActivity()).inflate(R.layout.delete_dialog, null);

                        tv_Info = (TextView) view1.findViewById(R.id.tv_info);
                        btn_comfirm = (Button) view1.findViewById(R.id.btn_comfirm_Delete);
                        btn_dismiss = (Button) view1.findViewById(R.id.btn_dismiss_Delete);
                        btn_dismiss.setOnClickListener(this);
                        btn_comfirm.setOnClickListener(this);
                        tv_Info.setText("确定要删除" + file.getName() + "?");
                        dialog_Delete.setView(view1);
                        dialog_Delete_Local = dialog_Delete.create();
                        dialog_Delete_Local.show();

                        break;
                    case 3:
                        Intent i1=new Intent("com.example.gongzhiyao.oplayer.action.music_playerReceiver");
                        i1.putExtra("op",11);
                        i1.putExtra("path",path);
                        getActivity().sendBroadcast(i1);
                        break;

                }
            }
        }

    }


    String fpath;
    private AdapterView.OnItemLongClickListener list_catalog_Long_Listener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            File f = new File(paths.get(position));
            if (f.isDirectory()) {
                return true;
            } else {
                Intent i = new Intent(getActivity(), SelectPopMenu.class);
                String name = names.get(position).toString();
                String path = paths.get(position).toString();
                String type=getMIMEType(f);
                Bundle b = new Bundle();
                b.putString("name", name);
                b.putString("path", path);
                b.putString("type",type);
                i.putExtras(b);
                startActivityForResult(i, SEND_TO_POP);
            }

            return true;
        }
    };

    File newFile;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.APPTitle_Local:
                /**
                 *
                 */
                sp.edit().putInt("listType", List_Type_FileDirectory).commit();
                String parent_Path = file_current.getParentFile().getAbsolutePath();
                System.out.println(parent_Path);
                File rootFile = new File(ROOT_PATH);
                if (!parent_Path.equals(rootFile.getParentFile().getAbsolutePath().toString())) {
                    showFileDir(parent_Path);
                }

                break;
            case R.id.ib_Refresh_Local:
                log.d("4444444444444");
                break;
            case R.id.ib_More_Local:
                showPopMenu(ib_More_Local);

                break;

            case R.id.btn_comfirm:

                String modifyName = editText.getText().toString();
                fpath = file.getParentFile().getPath();
                newFile = new File(fpath + "/" + modifyName);
                if (newFile.exists()) {
                    //排除没有修改情况
                    if (!modifyName.equals(file.getName())) {

                        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                        View view = LayoutInflater.from(getActivity()).inflate(R.layout.cover_file_dialog, null);
                        TextView tv_Cover = (TextView) view.findViewById(R.id.tv_Cover);
                        btn_comfirm = (Button) view.findViewById(R.id.btn_comfirm_Cover);
                        btn_dismiss = (Button) view.findViewById(R.id.btn_dismiss_Cover);
                        String warn = modifyName + "已存在，是否覆盖？";
                        tv_Cover.setText(warn);
                        dialog.setView(view);
                        btn_comfirm.setOnClickListener(this);
                        btn_dismiss.setOnClickListener(this);
                        dialog_Cover_Local = dialog.create();
                        dialog_Cover_Local.show();
                    }
                } else {
                    if (file.renameTo(newFile)) {
                        showFileDir(fpath);
                        displayToast("重命名成功！");
                    } else {
                        displayToast("重命名失败！");
                    }
                }
                dialog_Rename_Local.dismiss();
                break;
            case R.id.btn_dismiss:
                dialog_Rename_Local.dismiss();
                break;

            case R.id.btn_comfirm_Delete:
                if (file.delete()) {
                    showFileDir(file.getParent());
                    displayToast("删除成功！");
                } else {
                    displayToast("删除失败！");
                }
                dialog_Delete_Local.dismiss();
                break;
            case R.id.btn_dismiss_Delete:
                dialog_Delete_Local.dismiss();
                break;

            case R.id.btn_comfirm_Cover:
                if (file.renameTo(newFile)) {
                    showFileDir(fpath);
                    displayToast("重命名成功！");
                } else {
                    displayToast("重命名失败！");
                }

                dialog_Cover_Local.dismiss();
                break;
            case R.id.btn_dismiss_Cover:
                dialog_Cover_Local.dismiss();

                break;

//            case R.id.btn_Local_Play_Or_Stop:
//                /**
//                 * 发送给service
//                 */
//
//                Intent intent = new Intent(getActivity(), MusicService.class);
//                intent.putExtra("op", 2);
//                getActivity().startService(intent);
//
//                break;
//            case R.id.iv_music:
//            case R.id.tv_Local_Music_Name:
//            case R.id.tv_Local_Music_Singer:
//                int list_type3 = sp.getInt("listType", -1);
//                if (list_type3 == 1) {
//                    new scan_In_Folder().execute();
//                } else if (list_type3 == 2) {
//                    timeOfSong.stop();
//                    String stoptime2 = timeOfSong.getText().toString();
//                    stop_Time_MS = StringToMilliseconds(stoptime2);
//                    Bundle bundle = new Bundle();
//                    bundle.putSerializable("names", names_Audio);
//                    bundle.putSerializable("paths", paths_Audio);
//                    Intent intent2 = new Intent(getActivity(), music_Player.class);
//                    intent2.putExtras(bundle);
//                    intent2.putExtra("stopTime", stop_Time_MS);
//                    intent2.putExtra("music_Info", audioFile.getPath());
//                    startActivity(intent2);
//                    /**
//                     *
//                     */
//
//                }
//                break;


        }
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


//    private class scan_In_Folder extends AsyncTask<Void, Void, Void> {
//
//        @Override
//        protected Void doInBackground(Void... params) {
//
//            File fileParent = audioFile.getParentFile();
//            getAudiosOfFolder(fileParent);
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//            timeOfSong.stop();
//            String stoptime2 = timeOfSong.getText().toString();
//            stop_Time_MS = StringToMilliseconds(stoptime2);
//            Bundle bundle = new Bundle();
//            bundle.putSerializable("names", names_In_Folder);
//            bundle.putSerializable("paths", path_In_Folder);
//            Intent intent = new Intent(getActivity(), music_Player.class);
//            intent.putExtras(bundle);
//            intent.putExtra("stopTime", stop_Time_MS);
//            intent.putExtra("music_Info", audioFile.getPath());
//            startActivity(intent);
//        }
//    }


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


    int Op = 0;

    public void showPopMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), view);
        popupMenu.getMenuInflater().inflate(R.menu.popmenu_local, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.Only_Video:
                        Op = 1;
                        tv_Local_path.setVisibility(View.GONE);
                        new searchAllVideo().execute();
                        break;
                    case R.id.Only_Audio:
                        Op = 2;
                        tv_Local_path.setVisibility(View.GONE);
                        sp.edit().putInt("listType", List_Type_OnlyAudio).commit();
                        names_Audio = new ArrayList<String>();
                        paths_Audio = new ArrayList<String>();
                        new searchAllVideo().execute();

                        break;

                    case R.id.all_Media:
                        tv_Local_path.setVisibility(View.VISIBLE);
                        /**
                         *
                         */
                        sp.edit().putInt("listType", List_Type_FileDirectory).commit();
                        showFileDir(ROOT_PATH);
                        tv_Local.setText("文件目录");

                        break;
                }


                return false;
            }
        });

        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {

            }
        });

        popupMenu.show();


    }

    ProgressDialog dialog;

    private class searchAllVideo extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(getActivity());
            if (Op == 1) {
                tv_Local.setText("视频文件检索中");

            } else if (Op == 2) {
                tv_Local.setText("音频文件检索中");
            }
            dialog.setMessage("正在查询，请稍后...");
            dialog.setCancelable(false);
            dialog.show();

            names.clear();
            paths.clear();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (Op == 1) {
                eachAllVideo(Environment.getExternalStorageDirectory());
            } else if (Op == 2) {
                eachAllAudio(Environment.getExternalStorageDirectory());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            FileAdapter adapter = new FileAdapter(getActivity(), names, paths);
            lv_Local.setAdapter(adapter);
            dialog.dismiss();
            if (Op == 1)
                tv_Local.setText("视频文件");
            else if (Op == 2) {
                tv_Local.setText("音频文件");
            }
            Op = 0;


        }
    }


    private void eachAllVideo(File f) {
        if (f.exists() && f != null && f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        eachAllVideo(file);
                    } else if (file.exists() && file.canRead() && getMIMEType(file).equals("video/*")) {
                        names.add(file.getName().toString());
                        paths.add(file.getAbsolutePath().toString());

                    }
                }
            }

        } else if (f.exists() && f.canRead() && getMIMEType(f).equals("video/*")) {
            names.add(f.getName().toString());
            paths.add(f.getAbsolutePath().toString());

        }

    }


    private void eachAllAudio(File f) {
        if (f.exists() && f != null && f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        eachAllAudio(file);
                    } else if (file.exists() && file.canRead() && getMIMEType(file).equals("audio/*")) {
                        names.add(file.getName().toString());
                        names_Audio.add(file.getName().toString());
                        paths.add(file.getAbsolutePath().toString());
                        paths_Audio.add(file.getAbsolutePath().toString());

                    }
                }
            }

        } else if (f.exists() && f.canRead() && getMIMEType(f).equals("audio/*")) {
            names.add(f.getName().toString());
            paths.add(f.getAbsolutePath().toString());

        }

    }


    @Override
    public void onStart() {
        super.onStart();
        lv_Local.setFriction(ViewConfiguration.getScrollFriction() * 2);
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


//    AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
//        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
//        public void onAudioFocusChange(int focusChange) {
//            /**
//             * 短暂失去焦点
//             */
//            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
//                if (mediaPlayer.isPlaying()) {
//                    mediaPlayer.pause();
//                    //*********************
//                    timeOfSong.stop();
//                    String stoptime = timeOfSong.getText().toString();
//                    stop_Time_MS = StringToMilliseconds(stoptime);
//                    Resources resources = getActivity().getResources();
//                    Drawable drawable_play = resources.getDrawable(R.drawable.play);
//                    btn_Local_Playe_Or_Stop.setBackground(drawable_play);
//                }
//
//            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
//                /**
//                 * 获得焦点
//                 */
//                if (mediaPlayer == null) {
//                    /**
//                     * 如果为空，则随机一首
//                     */
//                    int listType = sp.getInt(ListType, -1);
//
//                    if (listType == List_Type_FileDirectory) {
//                        log.d("播放结束33333333333333333333");
//                        log.d("其中的音乐文件有   " + names_Audio_Dir.size());
//                        int length = names_Audio_Dir.size();
//
//                        int newMusic = (int) (Math.random() * length);
//                        audioFile = new File(paths_Audio_Dir.get(newMusic).toString());
//
//                        mediaPlayer = android.media.MediaPlayer.create(getActivity(), Uri.fromFile(audioFile));
//
//                        mediaPlayer.start();
//                        mediaPlayer.setOnCompletionListener(listener);
//                        progressBar_Local.setProgress(0);
//                        timeOfSong.setBase(SystemClock.elapsedRealtime());
//                        timeOfSong.start();
//                        new getMusicInfo().execute();
//                    } else if (listType == List_Type_OnlyAudio) {
//
//                        log.d("播放结束22222222222222222222222");
//                        int length = names.size();
//                        int newMusic = (int) (Math.random() * length);
//                        audioFile = new File(paths.get(newMusic).toString());
//                        mediaPlayer = android.media.MediaPlayer.create(getActivity(), Uri.fromFile(audioFile));
//                        mediaPlayer.start();
//                        mediaPlayer.setOnCompletionListener(listener);
//                        progressBar_Local.setProgress(0);
//                        timeOfSong.setBase(SystemClock.elapsedRealtime());
//                        timeOfSong.start();
//                        new getMusicInfo().execute();
//                    }
//                } else if (!mediaPlayer.isPlaying()) {
//
//                    mediaPlayer.start();
//                    if (stop_Time_MS != 0) {
//                        timeOfSong.setBase(SystemClock.elapsedRealtime() - stop_Time_MS);
//                        stop_Time_MS = 0;
//                    }
//                    timeOfSong.start();
//
//                    Resources resources = getActivity().getResources();
//                    Drawable drawable_stop = resources.getDrawable(R.drawable.stop);
//                    btn_Local_Playe_Or_Stop.setBackground(drawable_stop);
//
//                }
//                // Resume playback
//
//            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
//                /**
//                 * 会长时间失去焦点，一班播放音乐就是长时间失去音频焦点，但是不要把mediaplayer释放。
//                 */
//                if (mediaPlayer.isPlaying()) {
//
////                    mediaPlayer.stop();
//                    mediaPlayer.pause();
//                    //*********************
//                    timeOfSong.stop();
//                    String stoptime = timeOfSong.getText().toString();
//                    stop_Time_MS = StringToMilliseconds(stoptime);
//                    Resources resources = getActivity().getResources();
//                    Drawable drawable_play = resources.getDrawable(R.drawable.play);
//                    btn_Local_Playe_Or_Stop.setBackground(drawable_play);
//                }
//                am.abandonAudioFocus(afChangeListener);
//                // Stop playback
//            } else if (focusChange == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//                if (mediaPlayer.isPlaying()) {
//                    mediaPlayer.stop();
//                }
//
//            } else if (focusChange == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
//                if (mediaPlayer.isPlaying()) {
//                    mediaPlayer.stop();
//                }
//
//            }
//        }
//    };
//

//
//    public class UIReceiver extends BroadcastReceiver {
//
//        public UIReceiver() {
//
//        }
//
//        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//            int op = intent.getIntExtra("op", -1);
//            switch (op) {
//                case 1:
//                case 2:
//                    String path = intent.getStringExtra("path");
//                    progressBar_Local.setProgress(0);
//                    timeOfSong.setBase(SystemClock.elapsedRealtime());
//                    timeOfSong.start();
//                    audioFile = new File(path);
//                    new getMusicInfo().execute();
//                    break;
//                case 3:
//                    timeOfSong.stop();
//                    String stoptime = timeOfSong.getText().toString();
//                    stop_Time_MS = StringToMilliseconds(stoptime);
//                    Resources resources = getActivity().getResources();
//                    Drawable drawable_play = resources.getDrawable(R.drawable.play);
//                    btn_Local_Playe_Or_Stop.setBackground(drawable_play);
//                    remoteViews.setInt(R.id.ib_music_playStop_notification, "setBackgroundResource", R.drawable.music_play);
//                    mNotificationManager.notify(1, builder.build());
//
//                    break;
//                case 4:
//
//                    if (stop_Time_MS != 0) {
//                        timeOfSong.setBase(SystemClock.elapsedRealtime() - stop_Time_MS);
//                        stop_Time_MS = 0;
//                    }
//                    timeOfSong.start();
//                    Resources resources1 = getActivity().getResources();
//                    Drawable drawable_stop = resources1.getDrawable(R.drawable.stop);
//                    btn_Local_Playe_Or_Stop.setBackground(drawable_stop);
//                    remoteViews.setInt(R.id.ib_music_playStop_notification, "setBackgroundResource", R.drawable.music_stop);
//                    mNotificationManager.notify(1, builder.build());
//                    break;
//                case 5:
//
//                    timeOfSong.stop();
//                    String stoptime1 = timeOfSong.getText().toString();
//                    stop_Time_MS = StringToMilliseconds(stoptime1);
//                    Resources resources2 = getActivity().getResources();
//                    Drawable drawable_play1 = resources2.getDrawable(R.drawable.play);
//                    btn_Local_Playe_Or_Stop.setBackground(drawable_play1);
//                    remoteViews.setInt(R.id.ib_music_playStop_notification, "setBackgroundResource", R.drawable.music_play);
//                    mNotificationManager.notify(1, builder.build());
//                    break;
//                case 6:
//                    timeOfSong.stop();
//                    String stoptime2 = timeOfSong.getText().toString();
//                    stop_Time_MS = StringToMilliseconds(stoptime2);
//                    if (sp.getBoolean("player_visible",false)) {
//                        log.d("此时需要设置播放栏");
//                        Resources resources3 = getActivity().getResources();
//                        Drawable drawable_play2 = resources3.getDrawable(R.drawable.play);
//                        btn_Local_Playe_Or_Stop.setBackground(drawable_play2);
//                    }
//
//                    remoteViews.setInt(R.id.ib_music_playStop_notification, "setBackgroundResource", R.drawable.music_play);
////                    builder = new NotificationCompat.Builder(getContext());
////                    builder.setContent(remoteViews).setSmallIcon(R.drawable.ic_launcher)
////                            .setOngoing(true)
////                            .setTicker("music is playing");
//                    mNotificationManager.notify(1, builder.build());
//                    break;
//
//                case 7:
//
//                    if (stop_Time_MS != 0) {
//                        timeOfSong.setBase(SystemClock.elapsedRealtime() - stop_Time_MS);
//                        stop_Time_MS = 0;
//                    }
//                    timeOfSong.start();
//                    if (sp.getBoolean("player_visible",false)) {
//                        log.d("此时需要设置播放栏");
//                        Resources resources4 = getActivity().getResources();
//                        Drawable drawable_stop3 = resources4.getDrawable(R.drawable.stop);
//                        btn_Local_Playe_Or_Stop.setBackground(drawable_stop3);
//                    }
//                    remoteViews.setInt(R.id.ib_music_playStop_notification, "setBackgroundResource", R.drawable.music_stop);
//                    mNotificationManager.notify(1, builder.build());
//                    break;
//
//                case 8:
//                    mNotificationManager.cancel(1);
//                    Local_Music_player.setVisibility(View.GONE);
////                    SysApplication.getInstance().exit();
//                    break;
//            }
//        }
//
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        log.d("播放界面已销毁");
//        sp.edit().putBoolean("player_visible",false).commit();
//        Local_Music_player.setVisibility(View.GONE);
//        mNotificationManager.cancel(1);
//    }
}
