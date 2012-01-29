package com.xyz.graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public final class Model {
	
	public final class Instance {
		protected float[]	mTranslation;
		protected float[]	mRotation;
		protected float[]	mScale;
		private int 		mTextureId;
		private int			mEffectId;
		
		protected Instance() {
			mTranslation	= new float[3];
			mRotation		= new float[3];
			mScale			= new float[3];
			mTextureId		= -1;
			mEffectId		= -1;
		}
		
		protected Instance(int textureId, int effectId, float[] translation, float[] rotation, float[] scale) {
			assert textureId >= -1;
			assert effectId >= -1;
			assert translation != null && translation.length == 3;
			assert rotation != null && rotation.length == 3;
			assert scale != null && scale.length == 3;
			
			mTextureId		= textureId;
			mEffectId		= effectId;
			mTranslation	= translation;
			mRotation		= rotation;
			mScale			= scale;
		}
		
		
		public int getTextureId() {
			return mTextureId;
		}
		
		public int getEffectId() {
			return mEffectId;
		}
		
		public float[] getTranslation() {
			return mTranslation;
		}
		
		public float[] getRotation() {
			return mRotation;
		}
		
		public float[] getScale() {
			return mScale;
		}
	}
	
	private String		mName;
	private FloatBuffer	mVertexBuffer;
	private FloatBuffer	mNormalBuffer;
	private FloatBuffer mColourBuffer;
	private FloatBuffer mUVBuffer;
	private ShortBuffer	mIndexBuffer;
	
	private ArrayList<Instance> mInstances;
	
	
	public Model(String name, float[] vertices, float[] normals, float[] colours, float[] uvCoords, short[] indices) {
		
		assert name != null && !name.isEmpty();
		assert vertices != null && vertices.length > 0;
		assert normals == null || normals.length > 0;
		assert colours == null || colours.length > 0;
		assert uvCoords == null || uvCoords.length > 0;
		assert indices != null && indices.length > 0;
		assert indices.length % 3 == 0;
		
		mName		= name;
		mInstances	= new ArrayList<Instance>();
		
		// Set vertices
		ByteBuffer buffer = ByteBuffer.allocateDirect(vertices.length * 4);
		buffer.order(ByteOrder.nativeOrder());
		mVertexBuffer = buffer.asFloatBuffer();
		mVertexBuffer.put(vertices);
		mVertexBuffer.position(0);
		
		// Set normals
		if (normals != null) {
			buffer = ByteBuffer.allocateDirect(normals.length * 4);
			buffer.order(ByteOrder.nativeOrder());
			mNormalBuffer = buffer.asFloatBuffer();
			mNormalBuffer.put(normals);
			mNormalBuffer.position(0);
		}
		else
			mNormalBuffer = null;
		
		// Set colours
		if (colours == null) {
			// If colours doesn't exist, create all white
			int numOfVerts = vertices.length / 3;
			colours = new float[numOfVerts * 4];
			Arrays.fill(colours, 1.0f);
		}
		
		buffer = ByteBuffer.allocateDirect(colours.length * 4);
		buffer.order(ByteOrder.nativeOrder());
		mColourBuffer = buffer.asFloatBuffer();
		mColourBuffer.put(colours);
		mColourBuffer.position(0);
		
		// Set UVs
		buffer = ByteBuffer.allocateDirect(uvCoords.length * 4);
		buffer.order(ByteOrder.nativeOrder());
		mUVBuffer = buffer.asFloatBuffer();
		mUVBuffer.put(uvCoords);
		mUVBuffer.position(0);
		
		// Set indices
		buffer = ByteBuffer.allocateDirect(indices.length * 2);
		buffer.order(ByteOrder.nativeOrder());
		mIndexBuffer = buffer.asShortBuffer();
		mIndexBuffer.put(indices);
		mIndexBuffer.position(0);
	}
	
	public String getName() {
		return mName;
	}
	
	public FloatBuffer getVertexBuffer() {
		return mVertexBuffer;
	}
	
	public FloatBuffer getNormalBuffer() {
		return mNormalBuffer;
	}
	
	public FloatBuffer getColourBuffer() {
		return mColourBuffer;
	}
	
	public FloatBuffer getUVBuffer() {
		return mUVBuffer;
	}
	
	public ShortBuffer getIndexBuffer() {
		return mIndexBuffer;
	}
	
	
	public void createInstance(final String instanceName, int textureId, int effectId,
			float[] translation, float[] rotation, float[] scale) {
		
		mInstances.add(new Instance(textureId, effectId, translation, rotation, scale));
	}
	
	public Iterable<Instance> getInstances() {
		return mInstances;
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String separator = System.getProperty("line.separator");
		
		result.append(getClass().getName() + " Object {" + separator);
		result.append("Model Name: " + mName + separator);
		result.append("Has VertexBuffer: " + (mVertexBuffer != null ? "true" : "false") + separator);
		result.append("Has NormalBuffer: " + (mNormalBuffer != null ? "true" : "false") + separator);
		result.append("Has IndexBuffer: " + (mIndexBuffer != null ? "true" : "false") + separator);
		result.append("}");
		
		return result.toString();
	}
}