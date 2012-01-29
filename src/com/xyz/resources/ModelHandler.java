package com.xyz.resources;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.util.Log;

import com.xyz.graphics.Model;

public class ModelHandler {
	
	private ArrayList<Model> mModels;
	
	private ModelHandler()
	{
		mModels = null;
	}
	
	public static ModelHandler getInstance(InputStream stream, TextureManager textureManager,
			EffectManager effectManager) {
		
		ModelHandler handler = new ModelHandler();
		return handler.load(stream, textureManager, effectManager) ? handler : null;
	}
	
	private boolean load(InputStream stream, TextureManager textureManager, EffectManager effectManager) {
		InputSource source = new InputSource(stream);
		XPath reader = XPathFactory.newInstance().newXPath();
		
		try {
			NodeList meshes = (NodeList)reader.evaluate("/Model/Meshes/Mesh", source, XPathConstants.NODESET);
			mModels = new ArrayList<Model>();
			
			for (int i = 0; i < meshes.getLength(); i++) {
				Element meshNode = (Element)meshes.item(i);
				
				String name = meshNode.getAttribute("name");
				if (name == null || name.isEmpty()) {
					Log.e("finput", "You have to set the mesh name attribute");
					return false;
				}
				
				// Read vertices
				Node vertNode = (Node)reader.evaluate("Vertices", meshNode, XPathConstants.NODE);
				float[] vertices = valueToFloatArray(vertNode.getTextContent());
				
				// Read normals
				Node normNode = (Node)reader.evaluate("Normals", meshNode, XPathConstants.NODE);
				float[] normals = null;
				if (normNode != null)
					normals = valueToFloatArray(normNode.getTextContent());
				
				// Read colours
				Node colourNode = (Node)reader.evaluate("Colours", meshNode, XPathConstants.NODE);
				float[] colours = null;
				if (colourNode != null)
					colours = valueToFloatArray(colourNode.getTextContent());
				
				// Read UV coordinates
				Node uvNode = (Node)reader.evaluate("TexUV", meshNode, XPathConstants.NODE);
				float[] uvs = null;
				if (uvNode != null)
					uvs = valueToFloatArray(uvNode.getTextContent());
				
				// Read indices
				Node indexNode = (Node)reader.evaluate("Indices", meshNode, XPathConstants.NODE);
				short[] indices = valueToShortArray(indexNode.getTextContent());
				
				// Create the Model object
				Model model = new Model(name, vertices, normals, colours, uvs, indices);
				
				// Load all instances
				NodeList instanceList = (NodeList)reader.evaluate("Instances/Instance", meshNode, XPathConstants.NODESET);
				int numOfInstances = instanceList.getLength();
				for (int j = 0; j < numOfInstances; j++) {
					Element instance = (Element)instanceList.item(j);
					
					// Get name
					String instName = instance.getAttribute("name");
					if (instName == null || instName.isEmpty()) {
						Log.e("finput", "You have to set the instance name attribute");
						return false;
					}
					
					// Read textures
					Node texNode	= (Node)reader.evaluate("Texture", instance, XPathConstants.NODE);
					int textureId	= -1;
					if (texNode != null) {
						String texPath = texNode.getTextContent();
						if (texPath != null && !texPath.isEmpty()) {
							textureId = textureManager.createTexture(texPath);
						}
					}
					
					// Read effects
					Node vertexNode		= (Node)reader.evaluate("VertexShader", instance, XPathConstants.NODE);
					Node fragmentNode	= (Node)reader.evaluate("FragmentShader", instance, XPathConstants.NODE);
					int effectId	= -1;
					if (vertexNode != null && fragmentNode != null) {
						String vertexPath = vertexNode.getTextContent();
						String fragmentPath = fragmentNode.getTextContent();
						if (vertexPath != null && !vertexPath.isEmpty() &&
								fragmentPath != null && !fragmentPath.isEmpty()) {
							
							effectId = effectManager.createEffect(vertexPath, fragmentPath);
						}
					}
				
					// Read transformations
					Node transNode	= (Node)reader.evaluate("Translation", instance, XPathConstants.NODE);
					float[] translation = null;
					if (transNode != null)
						translation = valueToFloatArray(transNode.getTextContent());
					
					Node rotNode	= (Node)reader.evaluate("Rotation", instance, XPathConstants.NODE);
					float[] rotation = null;
					if (rotNode != null)
						rotation = valueToFloatArray(rotNode.getTextContent());
					
					Node scaleNode	= (Node)reader.evaluate("Scale", instance, XPathConstants.NODE);
					float[] scale = null;
					if (scaleNode != null)
						scale = valueToFloatArray(scaleNode.getTextContent());
					
					model.createInstance(instName, textureId, effectId, translation, rotation, scale);
				}

				// Add it to the list
				mModels.add(model);
				
			}
		} catch (XPathExpressionException e) {
			Log.e("data", "Invalid mesh data", e);
		}
		
		return true;
	}
	
	private static float[] valueToFloatArray(String value) {
		value = value.trim();
		String[] nodeValueArray = value.split("[ \t\r\n]+");
		
		int length = nodeValueArray.length;
		float[] converted = new float[length];
		try {
			for (int i = 0; i < length; i++) {
				converted[i] = Float.parseFloat(nodeValueArray[i]);
			}
		} catch (NumberFormatException e) {
			Log.e("data", "Can't convert to float", e);
		}
		
		return converted;
	}
	
	private static short[] valueToShortArray(String value) {
		value = value.trim();
		String[] nodeValueArray = value.split("[ \t\r\n]+");
		
		int length = nodeValueArray.length;
		short[] converted = new short[length];
		try {
			for (int i = 0; i < length; i++) {
				converted[i] = Short.parseShort(nodeValueArray[i]);
			}
		} catch (NumberFormatException e) {
			Log.e("data", "Can't convert to float", e);
		}
		
		return converted;
	}
	
	public ArrayList<Model> getModels() {
		return mModels;
	}
}
