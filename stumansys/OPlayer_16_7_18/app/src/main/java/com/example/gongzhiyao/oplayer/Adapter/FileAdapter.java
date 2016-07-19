package com.example.gongzhiyao.oplayer.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gongzhiyao.oplayer.LocalVideo;
import com.example.gongzhiyao.oplayer.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by 宫智耀 on 2016/6/28.
 */
public class FileAdapter extends BaseAdapter {
    private LayoutInflater inflater;
//    private Bitmap directory, file,video,audio;
    //存储文件名称
    private ArrayList<String> names = null;
    //存储文件路径
    private ArrayList<String> paths = null;

    private TextView tv_Title, tv_num_dir, tv_num_media;
    private ImageView iv;


    //参数初始化
    public FileAdapter(Context context, ArrayList<String> na, ArrayList<String> pa) {
        names = na;
        paths = pa;
//        directory = BitmapFactory.decodeResource(context.getResources(), R.drawable.d);
//        file = BitmapFactory.decodeResource(context.getResources(), R.drawable.f);
//        video=BitmapFactory.decodeResource(context.getResources(),R.drawable.video);
//        audio=BitmapFactory.decodeResource(context.getResources(),R.drawable.audio);
        //缩小图片
//        directory = small(directory, 0.12f);
//        file = small(file, 0.1f);
//        video=small(video,0.1f);
//        audio=small(audio,0.1f);
        inflater = LayoutInflater.from(context);


    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return names.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder holder;
        if (null == convertView) {
            convertView = inflater.inflate(R.layout.listitem_local, null);
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.textView);
            holder.image = (ImageView) convertView.findViewById(R.id.imageView);



            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        File f = new File(paths.get(position).toString());

        if (names.get(position).equals("@1")) {
            holder.text.setText("/");

            holder.image.setImageResource(R.drawable.d);


        } else if (names.get(position).equals("@2")) {
            holder.text.setText("..");
            holder.image.setImageResource(R.drawable.d);


        } else {
            holder.text.setText(f.getName());

            if (f.isDirectory()) {
                holder.image.setImageResource(R.drawable.d);

            } else if (f.isFile()) {
                if (LocalVideo.getMIMEType(f).equals("video/*")) {
                    holder.image.setImageResource(R.drawable.video);
                }else if(LocalVideo.getMIMEType(f).equals("audio/*")){
                    holder.image.setImageResource(R.drawable.audio);
                }
            } else {
                System.out.println(f.getName());
            }
        }
        return convertView;
    }

    private class ViewHolder {
        private TextView text;
        private ImageView image;

    }

//    private Bitmap small(Bitmap map, float num) {
//        Matrix matrix = new Matrix();
//        matrix.postScale(num, num);
//        return Bitmap.createBitmap(map, 0, 0, map.getWidth(), map.getHeight(), matrix, true);
//    }


}