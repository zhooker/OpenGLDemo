package com.example.opengldemo.player;

import android.os.Bundle;

import com.example.opengldemo.R;
import com.example.opengldemo.base.BaseActivity;

public class PlayerActivity extends BaseActivity implements PlayerManager.PlayerStateListener {

    private PlayerManager player;

    @Override
    protected void onCreate ( Bundle savedInstanceState )
    {
        super.onCreate ( savedInstanceState );
        setContentView (R.layout.activity_player );
        initPlayer();
    }

    private void initPlayer() {
        player = new PlayerManager(this);
        player.setFullScreenOnly(true);
        player.setScaleType(PlayerManager.SCALETYPE_FILLPARENT);
        player.playInFullScreen(true);

        player.setPlayerStateListener(this);

        player.play("http://zv.3gv.ifeng.com/live/zhongwen800k.m3u8");

    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onError() {

    }

    @Override
    public void onLoading() {

    }

    @Override
    public void onPlay() {

    }
}
