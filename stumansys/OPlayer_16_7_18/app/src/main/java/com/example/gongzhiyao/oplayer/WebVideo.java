package com.example.gongzhiyao.oplayer;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ScrollingView;
import android.support.v4.view.ViewPager;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gongzhiyao.oplayer.Adapter.FragAdapter;
import com.example.gongzhiyao.oplayer.Log.L;
import com.example.gongzhiyao.oplayer.Net_Status.NetWork;
import com.example.gongzhiyao.oplayer.WEB.Home;
import com.example.gongzhiyao.oplayer.WEB.Movie;
import com.example.gongzhiyao.oplayer.WEB.TVPlay;
import com.example.gongzhiyao.oplayer.WEB.VarietyShow;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WebVideo.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WebVideo#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WebVideo extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView tv_AppTitle;
    private Button btn_Home, btn_Movie, btn_TV_Play, btn_Variety;
    private NetWork netWork;
    private ImageView iv1, iv2, iv3, iv4;

    private L logs;
    private ViewPager viewPager;
    private FragAdapter adapter;
    private HorizontalScrollView scrollView_Hori;
    List<Fragment> fragments;

    private OnFragmentInteractionListener mListener;

    public WebVideo() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WebVideo.
     */
    // TODO: Rename and change types and number of parameters
    public static WebVideo newInstance(String param1, String param2) {
        WebVideo fragment = new WebVideo();
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

        View view1 = inflater.inflate(R.layout.fragment_web_video, container, false);
        initView(view1);
        setTabHost(iv1);


//        getFragmentManager().beginTransaction().replace(R.id.web_Container, new Home()).commit();
        netWork = new NetWork(getContext());
        if (netWork.isNetworkAvailable(getActivity())) {


            if (netWork.isWifi()) {
                Toast.makeText(getActivity(), "现在正在使用wifi", Toast.LENGTH_SHORT).show();
            } else {
                if (netWork.isMobile()) {
                    Toast.makeText(getActivity(), "当前正在使用2G/3G/4G网络", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(getActivity(), "当前网络无连接", Toast.LENGTH_SHORT).show();
        }


        fragments = new ArrayList<Fragment>();
        fragments.add(new Home());
        fragments.add(new Movie());
        fragments.add(new TVPlay());
        fragments.add(new VarietyShow());
        adapter = new FragAdapter(getActivity().getSupportFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                logs.d("!!!!!!!!!!" + position);
            }

            @Override
            public void onPageSelected(int position) {
                logs.d("?????????????" + position);
                switch (position) {

                    case 0:
                        scrollView_Hori.scrollTo(0, 0);
                        setTabHost(iv1);
                        break;
                    case 1:
                        setTabHost(iv2);
                        break;
                    case 2:
                        setTabHost(iv3);
                        break;
                    case 3:
                        scrollView_Hori.scrollTo(500, 0);
                        setTabHost(iv4);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        return view1;
    }

    public void initView(View view1) {
        tv_AppTitle = (TextView) view1.findViewById(R.id.APPTitle);
        btn_Home = (Button) view1.findViewById(R.id.btn_Home);
        btn_Movie = (Button) view1.findViewById(R.id.btn_Movie);
        btn_TV_Play = (Button) view1.findViewById(R.id.btn_TV_Play);
        btn_Variety = (Button) view1.findViewById(R.id.btn_Variety);
        iv1 = (ImageView) view1.findViewById(R.id.iv1);
        iv2 = (ImageView) view1.findViewById(R.id.iv2);
        iv3 = (ImageView) view1.findViewById(R.id.iv3);
        iv4 = (ImageView) view1.findViewById(R.id.iv4);
        viewPager = (ViewPager) view1.findViewById(R.id.viewPager);
        scrollView_Hori = (HorizontalScrollView) view1.findViewById(R.id.scrollView_Hori);

        tv_AppTitle.setOnClickListener(this);
        btn_Home.setOnClickListener(this);
        btn_Movie.setOnClickListener(this);
        btn_TV_Play.setOnClickListener(this);
        btn_Variety.setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.APPTitle:
//                getFragmentManager().beginTransaction().replace(R.id.web_Container, new Home()).commit();
                break;
            case R.id.btn_Home:
                setTabHost(iv1);

                viewPager.setCurrentItem(0);
//                getFragmentManager().beginTransaction().replace(R.id.web_Container, new Home()).commit();
                break;
            case R.id.btn_Movie:
                setTabHost(iv2);
                viewPager.setCurrentItem(1);
                scrollView_Hori.scrollTo(0, 0);
//                getFragmentManager().beginTransaction().replace(R.id.web_Container, new Movie()).commit();
                break;
            case R.id.btn_TV_Play:
                setTabHost(iv3);
                viewPager.setCurrentItem(2);
                scrollView_Hori.scrollTo(500, 0);
//                getFragmentManager().beginTransaction().replace(R.id.web_Container, new TVPlay()).commit();

                break;
            case R.id.btn_Variety:
                setTabHost(iv4);

                viewPager.setCurrentItem(3);
//                getFragmentManager().beginTransaction().replace(R.id.web_Container, new VarietyShow()).commit();
                break;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        logs.d("onDestroy!!!!!!!!");


    }

    public void setTabHost(View view) {
        switch (view.getId()) {

            case R.id.iv1:
                iv1.setVisibility(View.VISIBLE);
                iv2.setVisibility(View.GONE);
                iv3.setVisibility(View.GONE);
                iv4.setVisibility(View.GONE);

                break;
            case R.id.iv2:
                iv1.setVisibility(View.GONE);
                iv2.setVisibility(View.VISIBLE);
                iv3.setVisibility(View.GONE);
                iv4.setVisibility(View.GONE);
                break;
            case R.id.iv3:
                iv1.setVisibility(View.GONE);
                iv2.setVisibility(View.GONE);
                iv3.setVisibility(View.VISIBLE);
                iv4.setVisibility(View.GONE);
                break;
            case R.id.iv4:
                iv1.setVisibility(View.GONE);
                iv2.setVisibility(View.GONE);
                iv3.setVisibility(View.GONE);
                iv4.setVisibility(View.VISIBLE);
                break;
        }
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
}
