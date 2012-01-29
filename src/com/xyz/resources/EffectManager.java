package com.xyz.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

public class EffectManager {
	
	private final class EffectInfo {
		@SuppressWarnings("unused")
		public int mId;
		public int mGlId;
		public String mSource;
		
		public EffectInfo(int id, int glId, String source) {
			mId		= id;
			mGlId	= glId;
			mSource	= source;
		}
	}

	private static EffectManager mInstance;
	
	private AssetManager mAssetManager;
	private HashMap<String, Integer> mFileMap; // Filename, shader id to avoid repeating loads
	private HashMap<Integer, EffectInfo> mEffectList; // Id, effect
	
	private EffectManager(AssetManager assetManager) {
		mAssetManager = assetManager;
		mFileMap = new HashMap<String, Integer>();
		mEffectList = new HashMap<Integer, EffectInfo>();
	}
	
	public static EffectManager createInstance(AssetManager assetManager) {
		if (mInstance == null)
			mInstance = new EffectManager(assetManager);
		return mInstance;
	}
	
	public int createEffect(String vertexFilename, String fragmentFilename) {
		Integer id = mFileMap.get(vertexFilename);
		if (id == null) {
			id = -1;
			
			// Add a new effect
			InputStream stream;
			try {
				stream = mAssetManager.open(vertexFilename);
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
				if (reader != null) {
					String source = readSource(reader);
					
					if (source != null && !source.isEmpty()) {
						id = mEffectList.size();
						mEffectList.put(id, new EffectInfo(id, -1, source));
						mFileMap.put(vertexFilename, id);
					}
				}
				
				reader.close();
				stream.close();
			} catch (IOException e) {
				Log.e("fxman", "Failed to open " + vertexFilename);
			}
		}
		
		return id;
	}
	
	private String readSource(BufferedReader reader) {
		StringBuilder source = new StringBuilder();
		
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				source.append(line);
			}
		} catch (IOException e) {
		}
		
		return source.toString();
	}
	
	// TODO: Finish this
	public void generateEffects(GL10 gl) {
		
	}
	
	public int getGLEffectId(int id) {
		EffectInfo info = mEffectList.get(id);
		return info == null ? -1 : info.mGlId;
	}
}
