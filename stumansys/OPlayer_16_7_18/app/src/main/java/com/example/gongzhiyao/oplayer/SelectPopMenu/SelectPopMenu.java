package com.example.gongzhiyao.oplayer.SelectPopMenu;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gongzhiyao.oplayer.LocalPlayer;
import com.example.gongzhiyao.oplayer.LocalVideo;
import com.example.gongzhiyao.oplayer.R;

public class SelectPopMenu extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_Local_name;
    private Button btn_Play_As_Video, btn_Play_As_Audio, btn_Rename, btn_Delete;
    private String name, path,type;
    public static int ACTION_RENAME = 1;
    public static int ACTION_DELETE = 2;
    public static int ACTION_PLAY_AS_AUDIO=3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_select_pop_menu);
        setTitle("");
        initView();
        getData();
        tv_Local_name.setText(name);


    }

    private void initView() {
        tv_Local_name = (TextView) findViewById(R.id.tv_Local_name);
        btn_Play_As_Video = (Button) findViewById(R.id.btn_play_As_Video);
        btn_Play_As_Audio = (Button) findViewById(R.id.btn_play_As_Audio);
        btn_Rename = (Button) findViewById(R.id.btn_Local_Rename);
        btn_Delete = (Button) findViewById(R.id.btn_Local_Delete);
        btn_Play_As_Video.setOnClickListener(this);
        btn_Play_As_Audio.setOnClickListener(this);
        btn_Rename.setOnClickListener(this);
        btn_Delete.setOnClickListener(this);

    }


    private void getData() {
        Intent intent = getIntent();
        if (intent != null) {
            Bundle b = intent.getExtras();
            if (b != null) {
                name = b.getString("name");
                path = b.getString("path");
                type=b.getString("type");

            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_play_As_Video:
                if(type.equals("audio/*")){
                    Toast.makeText(getApplicationContext(),"无法作为视频播放",Toast.LENGTH_SHORT).show();
                }else if(type.equals("video/*")){
                    Intent i = new Intent(this, LocalPlayer.class);
                    i.putExtra("path", path);
                    startActivity(i);
                }

                finish();
                break;
            case R.id.btn_play_As_Audio:

                if(type.equals("audio/*")){
                    Intent intent = new Intent();
                    intent.putExtra("Op", ACTION_PLAY_AS_AUDIO);
                    intent.putExtra("path",path);
                    setResult(RESULT_OK, intent);
                }else if(type.equals("vedio/*")){
                    /**
                     * 在这里把视频作为音频播放
                     */
                }
                finish();
                break;
            case R.id.btn_Local_Rename:

                Intent intent1 = new Intent();
                intent1.putExtra("Op", ACTION_RENAME);
                intent1.putExtra("path",path);
                setResult(RESULT_OK, intent1);
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        finish();
                    }
                }.start();


                break;
            case R.id.btn_Local_Delete:

                Intent intent2 = new Intent();
                intent2.putExtra("Op", ACTION_DELETE);
                intent2.putExtra("path",path);
                setResult(RESULT_OK, intent2);
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        finish();
                    }
                }.start();

                break;

        }
    }
}
