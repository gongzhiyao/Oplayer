package com.example.gongzhiyao.oplayer.WEB;

import android.content.Context;
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
import android.widget.GridView;

import com.example.gongzhiyao.oplayer.Adapter.grideAdapter;
import com.example.gongzhiyao.oplayer.DB.DB;
import com.example.gongzhiyao.oplayer.Log.L;
import com.example.gongzhiyao.oplayer.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Movie.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Movie#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Movie extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private DB db;
    private SQLiteDatabase dbread;
    private PullToRefreshGridView refresh_Movie;
    private grideAdapter adapter;
    private L log;


    private OnFragmentInteractionListener mListener;

    public Movie() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Movie.
     */
    // TODO: Rename and change types and number of parameters
    public static Movie newInstance(String param1, String param2) {
        Movie fragment = new Movie();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie, container, false);
        refresh_Movie = (PullToRefreshGridView) view.findViewById(R.id.refresh_moive);
        log = new L();
        db = new DB(getContext());
        dbread = db.getReadableDatabase();
        String sql = "select * from " + DB.TABLE_NAME + " where " + DB.TYPE + " like ?";
        Cursor cursor = dbread.rawQuery(sql, new String[]{"%"+"电影"+"%"});
        adapter=new grideAdapter(getContext(),cursor,R.layout.videolist);
        refresh_Movie.setAdapter(adapter);
        refresh_Movie.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<GridView>() {
            @Override
            public void onRefresh(PullToRefreshBase<GridView> refreshView) {

                new SelectInfo().execute();
                log.d("刷新");
            }
        });


        /**
         * 这里还要写一下列表的监听器
         */

        refresh_Movie.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });





        return view;
    }

    private class SelectInfo extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            String sql = "select * from " + DB.TABLE_NAME + " where " + DB.TYPE + " like ?";
            Cursor cursor = dbread.rawQuery(sql, new String[]{"%"+"电影"+"%"});
            adapter=new grideAdapter(getContext(),cursor,R.layout.videolist);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            refresh_Movie.setAdapter(adapter);
            refresh_Movie.onRefreshComplete();
        }
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
}
