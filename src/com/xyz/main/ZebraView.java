package com.xyz.main;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.xyz.graphics.ZebraRender;

public class ZebraView extends GLSurfaceView {

	public ZebraView(Context context, ZebraRender renderer) {
        super(context);
        setEGLContextClientVersion(2);
        setRenderer(renderer);
    }
}
