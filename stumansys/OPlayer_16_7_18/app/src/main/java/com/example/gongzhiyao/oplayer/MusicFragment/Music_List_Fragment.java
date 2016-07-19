package com.example.gongzhiyao.oplayer.MusicFragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.gongzhiyao.oplayer.Adapter.MusicListAdapter;
import com.example.gongzhiyao.oplayer.DB.DB;
import com.example.gongzhiyao.oplayer.Local.music_Player;
import com.example.gongzhiyao.oplayer.Log.L;
import com.example.gongzhiyao.oplayer.R;
import com.example.gongzhiyao.oplayer.ScrollBar.QuickAlphabeticBar;
import com.example.gongzhiyao.oplayer.Tools.SerializableMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Music_List_Fragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Music_List_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Music_List_Fragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ListView lv;
    private L log;
    private DB db;
    private SQLiteDatabase dbread;
    private MusicListAdapter adapter;
    private musicListReceiver receiver;
    private QuickAlphabeticBar alphabeticBar;

    private OnFragmentInteractionListener mListener;

    public Music_List_Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Music_List_Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Music_List_Fragment newInstance(String param1, String param2) {
        Music_List_Fragment fragment = new Music_List_Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        log = new L();
        IntentFilter intentFilter = new IntentFilter("com.example.gongzhiyao.oplayer.action.musicListReceiver");
        receiver = new musicListReceiver();
        getActivity().registerReceiver(receiver, intentFilter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music__list_, container, false);

        db=new DB(getActivity());
        dbread=db.getReadableDatabase();
        initView(view);
        return view;
    }


    private void initView(View view) {

        lv = (ListView) view.findViewById(R.id.list_music);
        alphabeticBar = (QuickAlphabeticBar) view.findViewById(R.id.contact_list_fast_scroller);
        alphabeticBar.init(view);
        alphabeticBar.setListView(lv);
        alphabeticBar.setVisibility(View.VISIBLE);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /**
                 *
                 * 在这里要写点击音乐列表后的监听器，还有就是音乐专辑的图片的显示
                 * 再有就是more操作，还有右下角的游标的点击
                 *
                 */
            }
        });

    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    //
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);


    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }





    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }





    @Override
    public void onDestroy() {
        super.onDestroy();
        dbread.close();
        db.close();
        getActivity().unregisterReceiver(receiver);
    }



    public class musicListReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            int op=intent.getIntExtra("op",-1);
            switch (op){
                case 1:
                    String sq = "select * from " + DB.MUSIC_TABLENAME + " order by " + DB.MUSIC_LETTER;
                    Cursor cursor = dbread.rawQuery(sq, null);
                    adapter=new MusicListAdapter(getActivity(),cursor,alphabeticBar,R.layout.item_musiclist);
                    lv.setAdapter(adapter);
                    break;
            }
        }
    }
}
