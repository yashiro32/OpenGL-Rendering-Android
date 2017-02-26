package en.menghui.android.gameengine.renderEngine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import en.menghui.android.gameengine.R;
import en.menghui.android.gameengine.models.RawModel;

public class OBJLoader {
	public static RawModel loadObjModel(int resId, Loader loader, Context context) {
		/* FileReader fr = null;
		try {
			fr = new FileReader(new File("res/raw/" + fileName + ".obj"));
		} catch (FileNotFoundException e) {
			System.err.println("Couldn't load file!");
			e.printStackTrace();
		} */
		
		InputStream is = context.getResources().openRawResource(resId);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line;
		List<float[]> vertices = new ArrayList<float[]>();
		List<float[]> textures = new ArrayList<float[]>();
		List<float[]> normals = new ArrayList<float[]>();
		List<Integer> indices = new ArrayList<Integer>();
		float[] verticesArray = null;
		float[] normalsArray = null;
		float[] textureArray = null;
		int[] indicesArray = null;
		
		try {
			while(true) {
				line = reader.readLine();
				String[] currentLine = line.split(" ");
				if (line.startsWith("v ")) {
					float[] vertex = new float[3];
					vertex[0] = Float.parseFloat(currentLine[1]);
					vertex[1] = Float.parseFloat(currentLine[2]);
					vertex[2] = Float.parseFloat(currentLine[3]);
					vertices.add(vertex);
				} else if (line.startsWith("vt ")) {
					float[] texture = new float[2];
					texture[0] = Float.parseFloat(currentLine[1]);
					texture[1] = Float.parseFloat(currentLine[2]);
					textures.add(texture);
				} else if (line.startsWith("vn ")) {
					float[] normal = new float[3];
					normal[0] = Float.parseFloat(currentLine[1]);
					normal[1] = Float.parseFloat(currentLine[2]);
					normal[2] = Float.parseFloat(currentLine[3]);
					normals.add(normal);
				} else if (line.startsWith("f ")) {
					textureArray = new float[vertices.size()*2];
					normalsArray = new float[vertices.size()*3];
					break;
				}
			}
			
			while (line != null) {
				if (!line.startsWith("f ")) {
					line = reader.readLine();
					continue;
				}
				String[] currentLine = line.split(" ");
				String[] vertex1 = currentLine[1].split("/");
				String[] vertex2 = currentLine[2].split("/");
				String[] vertex3 = currentLine[3].split("/");
				
				processVertex(vertex1, indices, textures, normals, textureArray, normalsArray);
				processVertex(vertex2, indices, textures, normals, textureArray, normalsArray);
				processVertex(vertex3, indices, textures, normals, textureArray, normalsArray);
				
				line = reader.readLine();
			}
			
			reader.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		verticesArray = new float[vertices.size()*3];
		indicesArray = new int[indices.size()];
		
		int vertexPointer = 0;
		for (float[] vertex : vertices) {
			verticesArray[vertexPointer++] = vertex[0];
			verticesArray[vertexPointer++] = vertex[1];
			verticesArray[vertexPointer++] = vertex[2];
		}
		
		for (int i = 0; i < indices.size(); i++) {
			indicesArray[i] = indices.get(i);
		}
		
		return loader.loadToVAO(verticesArray, textureArray, normalsArray, indicesArray);
	}
	
	private static void processVertex(String[] vertexData, List<Integer> indices, List<float[]> textures, List<float[]> normals, float[] textureArray, float[] normalsArray) {
		int currentVertexPointer = Integer.parseInt(vertexData[0]) - 1;
		indices.add(currentVertexPointer);
		float[] currentTex = textures.get(Integer.parseInt(vertexData[1])-1);
		textureArray[currentVertexPointer*2] = currentTex[0];
		textureArray[currentVertexPointer*2+1] = 1 - currentTex[1];
		float[] currentNorm = normals.get(Integer.parseInt(vertexData[2])-1);
		normalsArray[currentVertexPointer*3] = currentNorm[0];
		normalsArray[currentVertexPointer*3+1] = currentNorm[1];
		normalsArray[currentVertexPointer*3+2] = currentNorm[2];
	}
}
