package com.xyz.graphics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.xyz.core.EnvironmentSettings;
import com.xyz.resources.EffectManager;
import com.xyz.resources.TextureManager;

public class ZebraRender implements GLSurfaceView.Renderer {

	public ZebraRender(Context context, ArrayList<Model> modelList,
			TextureManager textureManager, EffectManager effectManager) {
		
	    mContext = context;
	    
	    mModelList = modelList;
	    mTextureManager = textureManager;
	    mEffectManager = effectManager;
    }

    public void onDrawFrame(GL10 glUnused) {
        // Ignore the passed-in GL10 interface, and use the GLES20
        // class's static methods instead.
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);
        checkGlError("glUseProgram");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        
        if (mModelList != null) {
	        for (Model model : mModelList) {
	        	// Set mesh information
	        	GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 0,
	        			model.getVertexBuffer());
	        	GLES20.glEnableVertexAttribArray(maPositionHandle);
	        	
	        	GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false, 0,
	        			model.getUVBuffer());
	        	GLES20.glEnableVertexAttribArray(maTextureHandle);
	        	
	        	ShortBuffer indexBuffer = model.getIndexBuffer();
	        	for (Model.Instance instance : model.getInstances()) {
	        		// TODO: Local instance transform
	        		float angle = 0.0f;
	                Matrix.setRotateM(mMMatrix, 0, angle, 0, 0, 1.0f);
	                Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mMMatrix, 0);
	                Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);

	                GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
	        		
	        		// Render instance
	                int texId = instance.getTextureId();
		            if (texId >= 0) {
		            	GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureManager.getGlTextureId(texId));
		            }
	        		GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexBuffer.limit(), GLES20.GL_UNSIGNED_SHORT, indexBuffer);
	        	}
	        }
        }
    }

    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        // Ignore the passed-in GL10 interface, and use the GLES20
        // class's static methods instead.
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
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

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Ignore the passed-in GL10 interface, and use the GLES20
        // class's static methods instead.
    	
    	try {
    		InputStream stream = mTextureManager.mAssetManager.open("effects/simple_vertex.xyz.shader");
    		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			if (reader != null) {
				mVertexShader = readSource(reader);
			}
			
			reader.close();
			stream.close();
    	} catch (IOException e) {
    	}
    	
        mProgram = createProgram(mVertexShader, mFragmentShader);
        if (mProgram == 0) {
            return;
        }
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (maPositionHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }
        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (maTextureHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }

        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        checkGlError("glGetUniformLocation uMVPMatrix");
        if (muMVPMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uMVPMatrix");
        }

        Matrix.setLookAtM(mVMatrix, 0, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        
        GLES20.glClearColor(0.01f, 0.01f, 0.01f, 1.0f);
        
        mTextureManager.generateTextures(gl);
    }

    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }

        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    private void checkGlError(String op) {
    	if (EnvironmentSettings.DEBUG) {
	        int error;
	        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
	            Log.e(TAG, op + ": glError " + error);
	            throw new RuntimeException(op + ": glError " + error);
	        }
    	}
    }

    private String mVertexShader;

    private final String mFragmentShader =
        "precision mediump float;\n" +
        "varying vec2 vTextureCoord;\n" +
        "uniform sampler2D sTexture;\n" +
        "void main() {\n" +
        "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
        "}\n";
    
    private ArrayList<Model> mModelList;
    private TextureManager mTextureManager;
    private EffectManager mEffectManager;

    private float[] mMVPMatrix = new float[16];
    private float[] mProjMatrix = new float[16];
    private float[] mMMatrix = new float[16];
    private float[] mVMatrix = new float[16];

    private int mProgram;
    private int muMVPMatrixHandle;
    private int maPositionHandle;
    private int maTextureHandle;

    @SuppressWarnings("unused")
	private Context mContext;
    private static String TAG = "render";
}
