package com.example.gongzhiyao.oplayer.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gongzhiyao.oplayer.DB.DB;
import com.example.gongzhiyao.oplayer.R;

import net.tsz.afinal.FinalBitmap;

public class grideAdapter extends BaseAdapter {

        private Context context;
        private Cursor c;
        private LayoutInflater inflater;
        private int CellLayoutID;
        private ImageView iv;
        private TextView tv;
        private FinalBitmap fb;

    public grideAdapter(Context context, Cursor c, int layoutID) {
        this.context = context;
        this.c = c;
        inflater = LayoutInflater.from(context);
        CellLayoutID = layoutID;
        /**
         * 这里也要使用一个Finalbitmap
         */
        fb = FinalBitmap.create(context);
//           fb.configLoadingImage();
    }

    @Override
    public int getCount() {
        return c.getCount();
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
        c.moveToPosition(position);
        if (convertView == null) {
            convertView = inflater.inflate(CellLayoutID, null);

        }

        tv = (TextView) convertView.findViewById(R.id.name);
        iv = (ImageView) convertView.findViewById(R.id.poster);

        String title = c.getString(c.getColumnIndex("title"));
        tv.setText(title);
//           String uri = "http://121.42.163.185:10010/ServletAndJDBC/servlet/DealPicture/comedy/xialuotefannao.jpg";
        String uri = c.getString(c.getColumnIndex(DB.POSTER));
        fb.display(iv, uri);


        return convertView;
    }
}
