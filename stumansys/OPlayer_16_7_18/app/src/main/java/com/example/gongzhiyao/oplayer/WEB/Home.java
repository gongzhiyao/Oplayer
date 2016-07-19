package com.example.gongzhiyao.oplayer.WEB;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gongzhiyao.oplayer.Adapter.grideAdapter;
import com.example.gongzhiyao.oplayer.DB.DB;
import com.example.gongzhiyao.oplayer.DB.DB_Option;
import com.example.gongzhiyao.oplayer.Log.L;
import com.example.gongzhiyao.oplayer.MainActivity;
import com.example.gongzhiyao.oplayer.R;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;

import net.tsz.afinal.FinalBitmap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Home.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Home#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Home extends Fragment  {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private PullToRefreshGridView refresh_Home;

    private OnFragmentInteractionListener mListener;
    private DB db;
    private SQLiteDatabase dbread,dbwrite;
    public static SharedPreferences sp;
    private L log;
    private int count =1;
    private grideAdapter adapter;
    private StringBuilder stringBuilder;
    private ProgressBar progress_Home;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            progress_Home.setVisibility(View.GONE);
        }
    };





    public Home() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Home.
     */
    // TODO: Rename and change types and number of parameters
    public static Home newInstance(String param1, String param2) {
        Home fragment = new Home();
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
        log.d("界面onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_home,container,false);
        refresh_Home= (PullToRefreshGridView)view.findViewById(R.id.refresh_home);
        progress_Home= (ProgressBar) view.findViewById(R.id.progress_Home);
        progress_Home.setVisibility(View.VISIBLE);
        initDB();

        sp=getActivity().getSharedPreferences("sp",Context.MODE_PRIVATE);

        boolean getInfo = sp.getBoolean("get", false);
        if (!getInfo) {
            /**
             * 把数据库清空，为了防止由于间断的网络，存入了不完整的数据
             */
            clearDB();
            String urL = "http://121.42.163.185:10010/ServletAndJDBC/1servlet/Sevlet";
            new SubmitAsyncTask().execute(urL);

            log.d("第一次执行get");
        } else {
            String urL = "http://121.42.163.185:10010/ServletAndJDBC/1servlet/Sevlet";
            new PostAsyncTask().execute(urL);
            log.d("后面的都执行post");
        }


//        String sq1 = "select * from video_local";
//        Cursor c1 = dbread.rawQuery(sq1, null);
//        adapter = new grideAdapter(getActivity(), c1, R.layout.videolist);
//        refresh_Home.setAdapter(adapter);



//        ILoadingLayout startLabels = refresh_Home
//                .getLoadingLayoutProxy(true, false);
//        startLabels.setPullLabel("你可劲拉，拉...");// 刚下拉时，显示的提示
//        startLabels.setRefreshingLabel("好嘞，正在刷新...");// 刷新时
//        startLabels.setReleaseLabel("你敢放，我就敢刷新...");

        refresh_Home.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<GridView>() {
            @Override
            public void onRefresh(PullToRefreshBase<GridView> refreshView) {
                String urL = "http://121.42.163.185:10010/ServletAndJDBC/1servlet/Sevlet";
                new PostAsyncTask().execute(urL);

            }
        });


        /**
         * 列表的监听器还需要补充
         */
        refresh_Home.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                log.d("点击的是"+position);
            }
        });









        return view;
    }

    private void initDB() {
        db = new DB(getActivity());
        dbread = db.getReadableDatabase();
        dbwrite = db.getWritableDatabase();
    }
    public void clearDB() {
        DB_Option db_option = new DB_Option(getActivity());
        db_option.clearFeedTable();
        db_option.closeDB();

    }
















    public class SubmitAsyncTask extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub

            String json = doGet(params[0]);
            /**
             * 解析json数据
             * 把解析的数据存入到数据库中，每次刷新会更新数据库，（如果在一直服务器端没有改变的情况下，就不会更新数据库，而是直接调用数据库的内容）
             */
            JsonToDB(json);

            sp.edit().putBoolean("get", true).commit();

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            String sq1 = "select * from video_local";
            Cursor c1 = dbread.rawQuery(sq1, null);
            adapter = new grideAdapter(getActivity(), c1, R.layout.videolist);
            refresh_Home.setAdapter(adapter);
            handler.sendEmptyMessage(1);


        }


    }





    private class PostAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            /**
             * 在这里只进行的是更新操作，不需要查询
             * 但是要重新获取网络资源
             * 最好要有一个进度条对话框
             */
            String sq = "select * from video_local order by releaseyear desc";
            Cursor c = dbread.rawQuery(sq, null);
            int length = c.getCount();
            c.moveToFirst();
            String lastModification = c.getString(c.getColumnIndex("releaseyear"));
//            System.out.println("最近的更改是" + lastModification);

            doPost(params[0], 1, length, lastModification, null);


            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            String sq2 = "select * from video_local";
            Cursor c2 = dbread.rawQuery(sq2, null);
            adapter = new grideAdapter(getContext(), c2, R.layout.videolist);

            refresh_Home.setAdapter(adapter);
            handler.sendEmptyMessage(1);
            refresh_Home.onRefreshComplete();

        }
    }












    private void doPost(String url1, int op, int length, String lastChange, String query) {
        try {

            URL url = new URL(url1);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

//            conn.setConnectTimeout(10000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);//不使用缓存
            conn.connect();

//            String content = "test=" + URLEncoder.encode("it is a test", "UTF-8");
//            byte[] data=sb.toString().getBytes();
            OutputStream os = conn.getOutputStream();
            PrintWriter printWriter = new PrintWriter(os);
//            printWriter.print("op="+1);
            JSONObject mjsonObject = new JSONObject();
            mjsonObject.put("op", op);
            mjsonObject.put("query", query);
            mjsonObject.put("length", length);
            mjsonObject.put("lastChange", lastChange);
            printWriter.print(mjsonObject.toString());
            printWriter.flush();
            printWriter.close();
//            os.write(data,0,data.length);
//            os.flush();
//            os.close();
            /**
             * 发送完成，接下来是接收
             */

            int responseCode = conn.getResponseCode();
            System.out.println("状态码是" + responseCode);
            if (responseCode == 200) {
                // 取回响应的结果
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder result = new StringBuilder();
                String readline = "";
                String backOp = "";

                int i = 1;
                while ((readline = br.readLine()) != null) {
                    if (i == 1) {
                        backOp = readline;
                    } else if (i == 2) {
                        count = Integer.parseInt(readline);
                    } else {
                        result.append(readline);
                    }
                    i++;

//                    result.append(readline);
                }
                if (backOp.equals("Op1")) {
                    String json = result.toString();
//                    System.out.println("json数据是"+json);


//                    String sq1 = "delete from video_local";
//                    String sq2 = "select * from sqlite_sequence";
//                    String sq3 = "update sqlite_sequence set seq=0 where name= 'video_local' ";
//                    dbwrite.execSQL(sq1);
//                    dbwrite.execSQL(sq2);
//                    dbwrite.execSQL(sq3);

                    clearDB();
                    JsonToDB(json);
//                    System.out.println("数据库内容已经重新写入");
//                System.out.println("接收到的是"+result.toString());
                    br.close();
                    conn.disconnect();

                } else if (backOp.equals("Op2")) {
                    log.d("服务器内容没有变化");
                }

            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }











    private String doGet(String url) {
        try {
            URL url1 = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url1.openConnection();
            httpURLConnection.setRequestMethod("GET");
//            httpURLConnection.setConnectTimeout(10000);

            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line = "";
            String num = "";
            stringBuilder = new StringBuilder();
            int i = 1;
            while ((line = br.readLine()) != null) {
                if (i == 1) {
                    num = line;
                    count = Integer.parseInt(num);
                } else {
                    stringBuilder.append(line);
                }
                i++;
            }


//            System.out.println("得到的个数是" + num);
//            System.out.println("json数据是" + stringBuilder.toString());


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
//            e.printStackTrace();
            Toast.makeText(getActivity(), "当前无网络连接", Toast.LENGTH_SHORT).show();
        }

        return stringBuilder.toString();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        log.d("界面已销毁");
//        db.close();
//        dbread.close();
//        dbwrite.close();
    }

    @Override
    public void onPause() {
        super.onPause();
        log.d("界面已暂停");
    }

    @Override
    public void onResume() {
        super.onResume();
        log.d("界面resume");
    }


    public void JsonToDB(String jsonString) {
        try {
            JSONObject mjsonObject = new JSONObject(jsonString);
            System.out.println("count是" + count);
            /**
             * 这里的count 分别使用的是 get和post中的count，所以应该先运行get或post再运行本方法
             */
            if (count != -1) {
                for (int i = 1; i <= count; i++) {
                    JSONObject jsonObject = mjsonObject.getJSONObject("object" + i);
                    String videoID = jsonObject.getString("videoID");
                    String title = jsonObject.getString("title");
                    String path = jsonObject.getString("path");
                    String poster = jsonObject.getString("poster");
                    String description = jsonObject.getString("description");
                    String time = jsonObject.getString("time");
                    String type = jsonObject.getString("type");
                    String director = jsonObject.getString("director");
                    String actor = jsonObject.getString("actor");
                    String releaseyear = jsonObject.getString("releaseyear");
                    String length = jsonObject.getString("length");


//                    System.out.println(title + "   " + path + "   " + poster + "   " + description + "   " + time + "   " + type + "   " + director + "  " + actor + "   " + releaseyear);

                    ContentValues cv = new ContentValues();
                    cv.put("videoID", videoID);
                    cv.put("title", title);
                    cv.put("path", path);
                    cv.put("poster", poster);
                    cv.put("description", description);
                    cv.put("time", time);
                    cv.put("type", type);
                    cv.put("director", director);
                    cv.put("actor", actor);
                    cv.put("releaseyear", releaseyear);
                    cv.put("length", length);
                    dbwrite.insert("video_local", null, cv);
                }
            } else {
                /**
                 * 没有得到任何的数据
                 */
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }















}
