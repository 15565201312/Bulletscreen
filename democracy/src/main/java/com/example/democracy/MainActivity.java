package com.example.democracy;

import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.VideoView;

import java.util.Random;

import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.ui.widget.DanmakuView;

public class MainActivity extends AppCompatActivity {



    private VideoView mVideoView;
    private DanmakuView mDanmakuView;
    private boolean showDanmaku;
    private DanmakuContext danmakuContext;

    /**
     * 弹幕解析器
     */
    private BaseDanmakuParser parser = new BaseDanmakuParser() {
        @Override
        protected IDanmakus parse() {
            return new Danmakus();
        }
    };
    private LinearLayout mOperationLayout;
    private Button mSend;
    private EditText mText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initDanmaku();


        //加载测试视频，需要在sd卡根目录提前放入测试视频,某些手机路径可能不一样，可以先运行一下看日志

        mVideoView.setVideoPath("http://live.greatchef.com.cn/record/live/1526271675/hls/1526271675-1526271675.m3u8");
        mVideoView.start();
    }

    /**
     * 初始化View组件
     */
    private void initView() {
        mVideoView = (VideoView) findViewById(R.id.video_view);
        mDanmakuView = (DanmakuView) findViewById(R.id.danmaku_view);
        mOperationLayout = (LinearLayout) findViewById(R.id.operation_layout);
        mSend = (Button) findViewById(R.id.send);
        mText = (EditText) findViewById(R.id.edit_text);
        //给弹幕层设置点击事件，判断是否显示发弹幕操作层
        mDanmakuView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOperationLayout.getVisibility()==View.GONE){
                    mOperationLayout.setVisibility(View.VISIBLE);
                }else {
                    mOperationLayout.setVisibility(View.GONE);
                }
            }
        });
        //给发送按钮设置点击事件，发送弹幕
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = mText.getText().toString();
                if (!TextUtils.isEmpty(data)){
                    addDanmaku(data,true);
                    mText.setText("");
                }
            }
        });

        //监听由于输入法弹出所致的沉浸问题
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener (new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == View.SYSTEM_UI_FLAG_VISIBLE) {
                    onWindowFocusChanged(true);
                }
            }
        });
    }

    /**
     * 初始化弹幕组件
     */
    private void initDanmaku() {
        //给弹幕视图设置回调，在准备阶段获取弹幕信息并开始
        mDanmakuView.setCallback(new DrawHandler.Callback() {
            @Override
            public void prepared() {
                showDanmaku = true;
                mDanmakuView.start();
         
            }

            @Override
            public void updateTimer(DanmakuTimer timer) {

            }

            @Override
            public void danmakuShown(BaseDanmaku danmaku) {

            }

            @Override
            public void drawingFinished() {

            }
        });
        //缓存，提升绘制效率
        mDanmakuView.enableDanmakuDrawingCache(true);
        //DanmakuContext主要用于弹幕样式的设置
        danmakuContext = DanmakuContext.create();
        danmakuContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN,3);//描边
        danmakuContext.setDuplicateMergingEnabled(true);//重复合并
        danmakuContext.setScrollSpeedFactor(1.2f);//弹幕滚动速度
        //让弹幕进入准备状态，传入弹幕解析器和样式设置
        mDanmakuView.prepare(parser,danmakuContext);
        //显示fps、时间等调试信息
        mDanmakuView.showFPS(true);
    }

//    /**
//     * 随机生成一些弹幕内容以供测试
//     */
//    private void generateSomeDanmaku() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (showDanmaku) {
//                    int time = new Random().nextInt(300);
//                    String content = "" + time + time;
//                    addDanmaku(content, false);
//                    try {
//                        Thread.sleep(time);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }).start();
//    }


    /**
     * 向弹幕View中添加一条弹幕
     *
     * @param content    弹幕的具体内容
     * @param withBorder 弹幕是否有边框
     */
    private void addDanmaku(String content, boolean withBorder) {
        //弹幕实例BaseDanmaku,传入参数是弹幕方向
        BaseDanmaku danmaku = danmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        danmaku.text = content;
        danmaku.padding = 5;
        danmaku.textSize = sp2px(20);
        danmaku.textColor = Color.WHITE;
        danmaku.setTime(mDanmakuView.getCurrentTime());
        //加边框
        if (withBorder) {
            danmaku.borderColor = Color.GREEN;
        }
        mDanmakuView.addDanmaku(danmaku);
    }

    /**
     * sp转px的方法。
     */
    public int sp2px(float spValue) {
        final float fontScale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mDanmakuView != null && mDanmakuView.isPrepared()) {
            mDanmakuView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDanmakuView != null && mDanmakuView.isPrepared() && mDanmakuView.isPaused()) {
            mDanmakuView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        showDanmaku = false;
        if (mDanmakuView != null) {
            mDanmakuView.release();
            mDanmakuView = null;
        }
    }
}


