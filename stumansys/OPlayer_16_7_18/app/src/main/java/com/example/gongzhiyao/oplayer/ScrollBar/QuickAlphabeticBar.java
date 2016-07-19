package com.example.gongzhiyao.oplayer.ScrollBar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.gongzhiyao.oplayer.R;

import java.util.HashMap;


/**
 * 字母索引条
 *
 * @author Administrator
 */
@SuppressLint("ResourceAsColor")
public class QuickAlphabeticBar extends ImageButton {
    private TextView mDialogText; // 中间显示字母的文本框
    private Handler mHandler; // 处理UI的句柄
    private ListView mList; // 列表


    private float mHight; // 高度
    // 字母列表索引
//    private String[] letters = new String[]{"A", "B", "C", "D", "E",
//            "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
//            "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};
    private String[] letters = new String[]{"a", "b", "c", "d", "e",
            "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r",
            "s", "t", "u", "v", "w", "x", "y", "z", "#"};
    private int[] array_drawables = new int[]{R.drawable.letter_a, R.drawable.letter_b, R.drawable.letter_c, R.drawable.letter_d,
            R.drawable.letter_e, R.drawable.letter_f, R.drawable.letter_g, R.drawable.letter_h,
            R.drawable.letter_i, R.drawable.letter_j, R.drawable.letter_k, R.drawable.letter_l,
            R.drawable.letter_m, R.drawable.letter_n, R.drawable.letter_o, R.drawable.letter_p,
            R.drawable.letter_q, R.drawable.letter_r, R.drawable.letter_s, R.drawable.letter_t,
            R.drawable.letter_u, R.drawable.letter_v, R.drawable.letter_w, R.drawable.letter_x,
            R.drawable.letter_y, R.drawable.letter_z, R.drawable.letter_end};
    // 字母索引哈希表
    private HashMap<String, Integer> alphaIndexer;
    Paint paint = new Paint();
    boolean showBkg = false;
    int choose = -1;

    /**
     * 构造函数
     */
    public QuickAlphabeticBar(Context context) {
        super(context);
    }

    public QuickAlphabeticBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public QuickAlphabeticBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // 初始化
    public void init(View ctx) {
        mDialogText = (TextView) ctx.findViewById(R.id.tv_contact_list_fast_position);
        if (mDialogText != null)
            mDialogText.setVisibility(View.INVISIBLE);
        mHandler = new Handler();
    }

    // 设置需要索引的列表
    public void setListView(ListView mList) {
        this.mList = mList;
    }

    /**
     * String:sortKey Integer:listsContact的角标索引
     * private HashMap<String,Integer> alphaIndexer;
     */
    public void setAlphaIndexer(HashMap<String, Integer> alphaIndexer) {
        this.alphaIndexer = alphaIndexer;
        postInvalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int act = event.getAction();
        float y = event.getY();
        final int oldChoose = choose;
        //获取控件高度
        mHight = getHeight();

        final int selectIndex = (int) (y / (mHight / letters.length));

        if (selectIndex > -1 && selectIndex < letters.length) {// 防止越界
            String key = letters[selectIndex];//获取手指触碰点的字母索引
            if (alphaIndexer.containsKey(key)) {
                int pos = alphaIndexer.get(key);//获取contactList的对应字母索引的角标
                if (mList.getHeaderViewsCount() > 0) {
                    this.mList.setSelection(pos + mList.getHeaderViewsCount());
                } else {
                    this.mList.setSelection(pos);//使listview移动到索引对应的contactList的角标位置
                }

                mDialogText.setBackgroundResource(array_drawables[selectIndex]);

            }
        }
        switch (act) {
            case MotionEvent.ACTION_DOWN:
                showBkg = true;
                if (oldChoose != selectIndex) {
                    if (selectIndex >= 0 && selectIndex < letters.length) {
                        if (alphaIndexer.containsKey(letters[selectIndex]))
                            choose = selectIndex;
                        invalidate();
                    }
                }
                if (selectIndex > -1 && selectIndex < letters.length && alphaIndexer.containsKey(letters[selectIndex]))
                    setDialogTextVisiable(VISIBLE);
                break;
            case MotionEvent.ACTION_MOVE:
                if (oldChoose != selectIndex) {
                    if (selectIndex >= 0 && selectIndex < letters.length) {
                        if (alphaIndexer.containsKey(letters[selectIndex]))
                            choose = selectIndex;
                        invalidate();
                    }
                }
                if (selectIndex > -1 && selectIndex < letters.length && alphaIndexer.containsKey(letters[selectIndex]))
                    setDialogTextVisiable(VISIBLE);
                break;
            case MotionEvent.ACTION_UP:
                showBkg = false;
                setDialogTextVisiable(INVISIBLE);
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    private void setDialogTextVisiable(final int visiable) {
        if (mHandler != null) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (mDialogText != null) {
                        mDialogText.setVisibility(visiable);
                    }
                }
            });
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight();
        int width = getWidth();
        int sigleHeight = height / letters.length; // 单个字母占的高度
        for (int i = 0; i < letters.length; i++) {
            paint.setColor(Color.parseColor("#FFFFFF")); //正常字母颜色
            paint.setTextSize(getResources().getDimension(R.dimen.textSize_s5));
            paint.setAntiAlias(true);
            if (alphaIndexer == null) {
                return;
            }
            if (!alphaIndexer.containsKey(letters[i])) {
                paint.setColor(Color.parseColor("#bbbbbb")); // 没有对应联系人的首字母灰显的颜色
                paint.setFakeBoldText(true);
            }
            // 绘画的位置
            float xPos = width / 2 - paint.measureText(letters[i]) / 2;
            float yPos = sigleHeight * i + sigleHeight;
            canvas.drawText(letters[i], xPos, yPos, paint);
            paint.reset();
        }
    }
}
