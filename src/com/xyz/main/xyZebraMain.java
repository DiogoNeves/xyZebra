package com.xyz.main;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;

import com.xyz.graphics.Model;
import com.xyz.graphics.ZebraRender;
import com.xyz.resources.EffectManager;
import com.xyz.resources.ModelHandler;
import com.xyz.resources.TextureManager;

public class xyZebraMain extends Activity {
	
	private ZebraView mSurfaceView;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Load models
		ArrayList<Model> modelList = null;
		AssetManager assets = getResources().getAssets();
		TextureManager texManager = TextureManager.createInstance(assets);
		EffectManager fxManager = EffectManager.createInstance(assets);
		if (assets != null)
		{
			InputStream stream;
			try {
				stream = assets.open("models/newplane.xyz.model");
				ModelHandler handler = ModelHandler.getInstance(stream, texManager, fxManager);
				modelList = handler.getModels();
				stream.close();
			} catch (IOException e) {
				Log.e("finput", "Trouble opening the model file", e);
			}
		}
		
		// Create our Preview view and set it as the content of our
        // Activity
        mSurfaceView = new ZebraView(this, new ZebraRender(this, modelList, texManager, fxManager));
        setContentView(mSurfaceView);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		
		mSurfaceView.onPause();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
		mSurfaceView.onResume();
	}
}