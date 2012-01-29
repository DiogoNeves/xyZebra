package com.xyz.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

public class TextureManager {
	
	private final class TextureInfo {
		@SuppressWarnings("unused")
		public int mId;
		public int mGlId;
		public Bitmap mBitmap;
		
		public TextureInfo(int id, int glId, Bitmap bitmap) {
			mId		= id;
			mGlId	= glId;
			mBitmap	= bitmap;
		}
	}
	
	private static TextureManager mInstance;
	
	public AssetManager mAssetManager;
	private HashMap<String, Integer> mFileMap; // Filename, tex id (not GL) to avoid repeating loads
	private HashMap<Integer, TextureInfo> mTextureInfoList;
	
	private TextureManager(AssetManager assetManager)
	{
		mAssetManager		= assetManager;
		mFileMap			= new HashMap<String, Integer>();
		mTextureInfoList	= new HashMap<Integer, TextureInfo>();
	}
	
	public static TextureManager createInstance(AssetManager assetManager) {
		if (mInstance == null)
			mInstance = new TextureManager(assetManager);
		return mInstance;
	}
	
	public int createTexture(String filename) {
		Integer id = mFileMap.get(filename);
		if (id == null) {
			id = -1;
			
			// Add a new texture
			InputStream stream;
			try {
				stream = mAssetManager.open(filename);
				
				Bitmap texture = BitmapFactory.decodeStream(stream);
				if (texture != null) {
					id = mTextureInfoList.size();
					mTextureInfoList.put(id, new TextureInfo(id, -1, texture));
					mFileMap.put(filename, id);
				}
				
				stream.close();
			} catch (IOException e) {
				Log.e("texman", "Failed to open " + filename);
			}
		}
		
		return id;
	}
	
	public void generateTextures(GL10 gl) {
		int[] glTextures = new int[mTextureInfoList.size()];
		int i = 0;
		for (TextureInfo info : mTextureInfoList.values()) {
			if (info.mGlId == -1) {
				// Generate OpenGl Texture
				GLES20.glGenTextures(	1, glTextures, i);
		        
				GLES20.glBindTexture(	GLES20.GL_TEXTURE_2D, glTextures[i]);
				GLES20.glTexParameterf(	GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
		        						GLES20.GL_LINEAR);
				GLES20.glTexParameterf(	GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
		        						GLES20.GL_LINEAR);
				GLES20.glTexParameterf(	GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
		        						GLES20.GL_REPEAT);
				GLES20.glTexParameterf(	GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
		        						GLES20.GL_REPEAT);
		        GLUtils.texImage2D(		GLES20.GL_TEXTURE_2D, 0, info.mBitmap, 0);
		        
		        info.mGlId = glTextures[i];
		        ++i;
			}
		}
	}
	
	public int getGlTextureId(int id) {
		TextureInfo info = mTextureInfoList.get(id);
		return info != null ? info.mGlId : -1;
	}
}