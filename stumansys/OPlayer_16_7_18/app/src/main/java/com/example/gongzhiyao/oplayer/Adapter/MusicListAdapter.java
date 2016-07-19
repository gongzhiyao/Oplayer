package com.example.gongzhiyao.oplayer.Adapter;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gongzhiyao.oplayer.DB.DB;
import com.example.gongzhiyao.oplayer.MainActivity;
import com.example.gongzhiyao.oplayer.R;
import com.example.gongzhiyao.oplayer.ScrollBar.QuickAlphabeticBar;

import net.tsz.afinal.FinalBitmap;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by 宫智耀 on 2016/7/16.
 */
public class MusicListAdapter extends BaseAdapter {
    private Cursor cursor,cursorlist;
    private int layoutId;
    private Context context;
    private LayoutInflater inflater;
    private FinalBitmap fb;
    String path;
    private File audioFile;
    ViewHoler holder;
    private HashMap<String,Integer> alphaIndexer;

    public MusicListAdapter(Context context, Cursor cursor, QuickAlphabeticBar alphabet, int layoutId) {
        this.context = context;
        this.cursor = cursor;
        this.cursorlist=cursor;
        this.layoutId = layoutId;
        this.alphaIndexer=new HashMap<String,Integer>();
        initAlphabeticBar(alphabet);
        inflater = LayoutInflater.from(context);
        fb = FinalBitmap.create(context);
    }
    public void initAlphabeticBar(QuickAlphabeticBar alphabet){
        alphaIndexer=new HashMap<String,Integer>();
        for (int i=0;i<cursorlist.getCount();i++){
            cursorlist.moveToPosition(i);
            String first=cursorlist.getString(cursorlist.getColumnIndex(DB.MUSIC_LETTER));

            String sortKey=first.substring(0,1);
            if (!alphaIndexer.containsKey(sortKey)){
                alphaIndexer.put(sortKey,i);
            }
        }

        alphabet.setEnabled(false);
        alphabet.setAlphaIndexer(alphaIndexer);
    }

    @Override
    public int getCount() {
        return cursor.getCount();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        cursor.moveToPosition(position);
        holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(layoutId, null);
            holder = new ViewHoler();
            holder.iv_music = (ImageView) convertView.findViewById(R.id.iv_music_list);
            holder.tv_singer = (TextView) convertView.findViewById(R.id.tv_music_singer_list);
            holder.tv_song = (TextView) convertView.findViewById(R.id.tv_music_song_list);
            holder.moreOp = (ImageButton) convertView.findViewById(R.id.btn_music_more);
            convertView.setTag(holder);
        } else {
            holder = (ViewHoler) convertView.getTag();
        }
        /**
         * 现在因为使用的一个异步操作，导致listview的重用性更加严重，
         * 现在可以考虑的是，使用一个rececal view
         * 还有就是把所有的信息全都存入数据库
         * 具体数据库的写法，得考虑一下
         */

        /**
         * 应该在之前把所有的数据存入数据库，包括图片
         */


        /**这里的歌名不是真正的歌名，有些歌手也包括在内**/
        String song = cursor.getString(cursor.getColumnIndex(DB.MUSIC_SONG));
        String singer = cursor.getString(cursor.getColumnIndex(DB.MUSIC_SINGER));
        String path = cursor.getString(cursor.getColumnIndex(DB.MUSIC_PICTURE));
        holder.tv_song.setText(song);
        holder.tv_singer.setText(singer);
        File file = new File(path);
        fb.display(holder.iv_music, Uri.fromFile(file).toString());


//        path = cursor.getString(cursor.getColumnIndex(DB.MUSIC_PATH));
//        audioFile = new File(path);

        /**
         * 在这里先把图片去掉，打算使用二级缓存
         */
//        new getMusicInfo().execute();

        return convertView;
    }


    private class ViewHoler {
        TextView tv_song;
        TextView tv_singer;
        ImageView iv_music;
        ImageButton moreOp;
    }


}




