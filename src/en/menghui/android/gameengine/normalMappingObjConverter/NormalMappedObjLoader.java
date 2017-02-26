package en.menghui.android.gameengine.normalMappingObjConverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import en.menghui.android.gameengine.models.RawModel;
import en.menghui.android.gameengine.renderEngine.Loader;

public class NormalMappedObjLoader {
	private static final String RES_LOC = "res/";
	
	/* public static RawModel loadOBJ(int resId, Loader loader, Context context) {
		InputStream is = context.getResources().openRawResource(resId);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line;
		List<VertexNM> vertices = new ArrayList<VertexNM>();
		List<float[]> textures = new ArrayList<float[]>();
		List<float[]> normals = new ArrayList<float[]>();
		List<Integer> indices = new ArrayList<Integer>();
		try {
			while (true) {
				line = reader.readLine();
				if (line.startsWith("v ")) {
					String[] currentLine = line.split(" ");
					float[] vertex = {(float) Float.valueOf(currentLine[1]), (float) Float.valueOf(currentLine[2]), (float) Float.valueOf(currentLine[3])};
					VertexNM newVertex = new VertexNM(vertices.size(), vertex);
					vertices.add(newVertex);
				} else if (line.startsWith("vt ")) {
					String[] currentLine = line.split(" ");
					float[] texture = {(float) Float.valueOf(currentLine[1]), (float) Float.valueOf(currentLine[2])};
					textures.add(texture);
				} else if (line.startsWith("vn ")) {
					String[] currentLine = line.split(" ");
					float[] normal = {(float) Float.valueOf(currentLine[1]), (float) Float.valueOf(currentLine[2]), (float) Float.valueOf(currentLine[3])};
					normals.add(normal);
				} else if (line.startsWith("f ")) {
					break;
				}
			} 
			while (line != null && line.startsWith("f ")) {
				String[] currentLine = line.split(" ");
				String[] vertex1 = currentLine[1].split("/");
				String[] vertex2 = currentLine[2].split("/");
				String[] vertex3 = currentLine[3].split("/");
				VertexNM v0 = processVertex(vertex1, vertices, indices);
				VertexNM v1 = processVertex(vertex2, vertices, indices);
				VertexNM v2 = processVertex(vertex3, vertices, indices);
				calculateTangents(v0 ,v1, v2, textures); // NEW
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			System.err.println("Error reading the file");
		}
		removeUnusedVertices(vertices);
		float[] verticesArray = new float[vertices.size() * 3];
		float[] texturesArray = new float[vertices.size() * 2];
		float[] normalsArray = new float[vertices.size() * 3];
		float[] tangentsArray = new float[vertices.size() * 3];
		float furthest = convertDataToArrays(vertices, textures, normals, verticesArray, texturesArray, normalsArray, tangentsArray);
		int[] indicesArray = convertIndicesListToArray(indices);
		
		return loader.loadToVAO(verticesArray, texturesArray, normalsArray, tangentsArray, indicesArray);
	}
	
	// NEW
	private static void calculateTangents(VertexNM v0, VertexNM v1, VertexNM v2, List<float[]> textures) {
		float[] delatPos1 = new float[3];
		float[] delatPos2 = new float[3];
		for (int i = 0; i < delatPos1.length; i++) {
			delatPos1[i] = v1.getPosition()[i] - v0.getPosition()[i];
			delatPos2[i] = v2.getPosition()[i] - v0.getPosition()[i];
		}
		float[] uv0 = textures.get(v0.getTextureIndex());
		float[] uv1 = textures.get(v1.getTextureIndex());
		float[] uv2 = textures.get(v2.getTextureIndex());
		
		float[] deltaUv1 = new float[2];
		float[] deltaUv2 = new float[2];
		for (int i = 0; i < deltaUv1.length; i++) {
			deltaUv1[i] = uv1[i] - uv0[i];
			deltaUv2[i] = uv2[i] - uv0[i]; 
		}
		
		float r = 1.0f / (deltaUv1[0] * deltaUv2[1] - deltaUv1[1] * deltaUv2[0]);
		for (int i = 0; i < delatPos1.length; i++) {
			delatPos1[i] *= deltaUv2[1];
			delatPos2[i] *= deltaUv1[1];
		}
		
		float[] tangent = new float[3];
		for (int i = 0; i < tangent.length; i++) {
			tangent[i] = delatPos1[i] - delatPos2[i];
		}
		for (int i = 0; i < tangent.length; i++) {
			tangent[i] *= r;
		}
		
		v0.addTangent(tangent);
		v1.addTangent(tangent);
		v2.addTangent(tangent);
	}
	
	private static VertexNM processVertex(String[] vertex, List<VertexNM> vertices, List<Integer> indices) {
		int index = Integer.parseInt(vertex[0]) - 1;
		VertexNM currentVertex = vertices.get(index);
		int textureIndex = Integer.parseInt(vertex[1]) - 1;
		int normalIndex = Integer.parseInt(vertex[2]) - 1;
		if (!currentVertex.isSet()) {
			currentVertex.setTextureIndex(textureIndex);
			currentVertex.setNormalIndex(normalIndex);
			indices.add(index);
			return currentVertex;
		} else {
			return dealWithAlreadyProcessedVertex(currentVertex, textureIndex, normalIndex, indices, vertices);
		}
	}
	
	private static int[] convertIndicesListToArray(List<Integer> indices) {
		int[] indicesArray = new int[indices.size()];
		for (int i = 0; i < indicesArray.length; i++) {
			indicesArray[i] = indices.get(i);
		}
		return indicesArray;
	}
	
	private static float convertDataToArrays(List<VertexNM> vertices, List<float[]> textures, List<float[]> normals, float[] verticesArray, float[] texturesArray, float[] normalsArray, float[] tangentsArray) {
		float furthestPoint = 0;
		for (int i = 0; i < vertices.size(); i++) {
			VertexNM currentVertex = vertices.get(i);
			if (currentVertex.getLength() > furthestPoint) {
				furthestPoint = currentVertex.getLength();
			}
			float[] position = currentVertex.getPosition();
			float[] textureCoord = textures.get(currentVertex.getTextureIndex());
			float[] normalVector = normals.get(currentVertex.getNormalIndex());
			float[] tangent = currentVertex.getAverageTangent();
			verticesArray[i * 3] = position[0];
			verticesArray[i * 3 + 1] = position[1];
			verticesArray[i * 3 + 2] = position[2];
			texturesArray[i * 2] = textureCoord[0];
			texturesArray[i * 2 + 1] = 1 - textureCoord[1];
			normalsArray[i * 3] = normalVector[0];
			normalsArray[i * 3 + 1] = normalVector[1];
			normalsArray[i * 3 + 2] = normalVector[2];
			tangentsArray[i * 3] = tangent[0];
			tangentsArray[i * 3 + 1] = tangent[1];
			tangentsArray[i * 3 + 2] = tangent[2];
		}
		
		return furthestPoint;
	}
	
	private static VertexNM dealWithAlreadyProcessedVertex(VertexNM previousVertex, int newTextureIndex, int newNormalIndex, List<Integer> indices, List<VertexNM> vertices) {
		if (previousVertex.hasSameTextureAndNormal(newTextureIndex, newNormalIndex)) {
			indices.add(previousVertex.getIndex());
			return previousVertex;
		} else {
			VertexNM anotherVertex = previousVertex.getDuplicateVertex();
			if (anotherVertex != null) {
				return dealWithAlreadyProcessedVertex(anotherVertex, newTextureIndex, newNormalIndex, indices, vertices);
			} else {
				VertexNM duplicateVertex = previousVertex.duplicate(vertices.size()); // NEW
				duplicateVertex.setTextureIndex(newTextureIndex);
				duplicateVertex.setNormalIndex(newNormalIndex);
				previousVertex.setDuplicateVertex(duplicateVertex);
				vertices.add(duplicateVertex);
				indices.add(duplicateVertex.getIndex());
				return duplicateVertex;
			}
		}
	}
	
	private static void removeUnusedVertices(List<VertexNM> vertices) {
		for (VertexNM vertex : vertices) {
			vertex.averageTangents();
			if (!vertex.isSet()) {
				vertex.setTextureIndex(0);
				vertex.setNormalIndex(0);
			}
		}
	} */
	
	
	
    public static RawModel loadOBJ(int resId, Loader loader, Context context) {
    	InputStream is = context.getResources().openRawResource(resId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        
        String line;
        List<VertexNM> vertices = new ArrayList<VertexNM>();
        List<float[]> textures = new ArrayList<float[]>();
        List<float[]> normals = new ArrayList<float[]>();
        List<Integer> indices = new ArrayList<Integer>();
        try {
            while (true) {
                line = reader.readLine();
                if (line.startsWith("v ")) {
                    String[] currentLine = line.split(" ");
                    float[] vertex = {(float) Float.valueOf(currentLine[1]), (float) Float.valueOf(currentLine[2]), (float) Float.valueOf(currentLine[3])};
                    VertexNM newVertex = new VertexNM(vertices.size(), vertex);
                    vertices.add(newVertex);
                } else if (line.startsWith("vt ")) {
                    String[] currentLine = line.split(" ");
                    float[] texture = {(float) Float.valueOf(currentLine[1]), (float) Float.valueOf(currentLine[2])};
                    textures.add(texture);
                } else if (line.startsWith("vn ")) {
                    String[] currentLine = line.split(" ");
                    float[] normal = {(float) Float.valueOf(currentLine[1]), (float) Float.valueOf(currentLine[2]), (float) Float.valueOf(currentLine[3])};
                    normals.add(normal);
                } else if (line.startsWith("f ")) {
                    break;
                }
            }
            while (line != null && line.startsWith("f ")) {
                String[] currentLine = line.split(" ");
                String[] vertex1 = currentLine[1].split("/");
                String[] vertex2 = currentLine[2].split("/");
                String[] vertex3 = currentLine[3].split("/");
                VertexNM v0 = processVertex(vertex1, vertices, indices);
                VertexNM v1 = processVertex(vertex2, vertices, indices);
                VertexNM v2 = processVertex(vertex3, vertices, indices);
                calculateTangents(v0, v1, v2, textures);//NEW
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Error reading the file");
        }
        removeUnusedVertices(vertices);
        float[] verticesArray = new float[vertices.size() * 3];
        float[] texturesArray = new float[vertices.size() * 2];
        float[] normalsArray = new float[vertices.size() * 3];
        float[] tangentsArray = new float[vertices.size() * 3];
        float furthest = convertDataToArrays(vertices, textures, normals, verticesArray, texturesArray, normalsArray, tangentsArray);
        int[] indicesArray = convertIndicesListToArray(indices);
 
        return loader.loadToVAO(verticesArray, texturesArray, normalsArray, tangentsArray, indicesArray);
    }
    
    //NEW 
    private static void calculateTangents(VertexNM v0, VertexNM v1, VertexNM v2, List<float[]> textures) {
    	float[] delatPos1 = new float[3];
		float[] delatPos2 = new float[3];
		for (int i = 0; i < delatPos1.length; i++) {
			delatPos1[i] = v1.getPosition()[i] - v0.getPosition()[i];
			delatPos2[i] = v2.getPosition()[i] - v0.getPosition()[i];
		}
		float[] uv0 = textures.get(v0.getTextureIndex());
		float[] uv1 = textures.get(v1.getTextureIndex());
		float[] uv2 = textures.get(v2.getTextureIndex());
		
		float[] deltaUv1 = new float[2];
		float[] deltaUv2 = new float[2];
		for (int i = 0; i < deltaUv1.length; i++) {
			deltaUv1[i] = uv1[i] - uv0[i];
			deltaUv2[i] = uv2[i] - uv0[i]; 
		}
		
		float r = 1.0f / (deltaUv1[0] * deltaUv2[1] - deltaUv1[1] * deltaUv2[0]);
		for (int i = 0; i < delatPos1.length; i++) {
			delatPos1[i] *= deltaUv2[1];
			delatPos2[i] *= deltaUv1[1];
		}
		
		float[] tangent = new float[3];
		for (int i = 0; i < tangent.length; i++) {
			tangent[i] = delatPos1[i] - delatPos2[i];
		}
		for (int i = 0; i < tangent.length; i++) {
			tangent[i] *= r;
		}
		
		v0.addTangent(tangent);
        v1.addTangent(tangent);
        v2.addTangent(tangent);
    }
 
    private static VertexNM processVertex(String[] vertex, List<VertexNM> vertices, List<Integer> indices) {
        int index = Integer.parseInt(vertex[0]) - 1;
        VertexNM currentVertex = vertices.get(index);
        int textureIndex = Integer.parseInt(vertex[1]) - 1;
        int normalIndex = Integer.parseInt(vertex[2]) - 1;
        if (!currentVertex.isSet()) {
            currentVertex.setTextureIndex(textureIndex);
            currentVertex.setNormalIndex(normalIndex);
            indices.add(index);
            return currentVertex;
        } else {
            return dealWithAlreadyProcessedVertex(currentVertex, textureIndex, normalIndex, indices, vertices);
        }
    }
 
    private static int[] convertIndicesListToArray(List<Integer> indices) {
        int[] indicesArray = new int[indices.size()];
        for (int i = 0; i < indicesArray.length; i++) {
            indicesArray[i] = indices.get(i);
        }
        return indicesArray;
    }
 
    private static float convertDataToArrays(List<VertexNM> vertices, List<float[]> textures, List<float[]> normals, float[] verticesArray, float[] texturesArray, float[] normalsArray, float[] tangentsArray) {
        float furthestPoint = 0;
        for (int i = 0; i < vertices.size(); i++) {
            VertexNM currentVertex = vertices.get(i);
            if (currentVertex.getLength() > furthestPoint) {
                furthestPoint = currentVertex.getLength();
            }
            float[] position = currentVertex.getPosition();
            float[] textureCoord = textures.get(currentVertex.getTextureIndex());
            float[] normalVector = normals.get(currentVertex.getNormalIndex());
            float[] tangent = currentVertex.getAverageTangent();
            verticesArray[i * 3] = position[0];
            verticesArray[i * 3 + 1] = position[1];
            verticesArray[i * 3 + 2] = position[2];
            texturesArray[i * 2] = textureCoord[0];
            texturesArray[i * 2 + 1] = 1 - textureCoord[1];
            normalsArray[i * 3] = normalVector[0];
            normalsArray[i * 3 + 1] = normalVector[1];
            normalsArray[i * 3 + 2] = normalVector[2];
            tangentsArray[i * 3] = tangent[0];
            tangentsArray[i * 3 + 1] = tangent[1];
            tangentsArray[i * 3 + 2] = tangent[1];
 
        }
        return furthestPoint;
    }
 
    private static VertexNM dealWithAlreadyProcessedVertex(VertexNM previousVertex, int newTextureIndex, int newNormalIndex, List<Integer> indices, List<VertexNM> vertices) {
        if (previousVertex.hasSameTextureAndNormal(newTextureIndex, newNormalIndex)) {
            indices.add(previousVertex.getIndex());
            return previousVertex;
        } else {
            VertexNM anotherVertex = previousVertex.getDuplicateVertex();
            if (anotherVertex != null) {
                return dealWithAlreadyProcessedVertex(anotherVertex, newTextureIndex,
                        newNormalIndex, indices, vertices);
            } else {
                VertexNM duplicateVertex = previousVertex.duplicate(vertices.size());//NEW
                duplicateVertex.setTextureIndex(newTextureIndex);
                duplicateVertex.setNormalIndex(newNormalIndex);
                previousVertex.setDuplicateVertex(duplicateVertex);
                vertices.add(duplicateVertex);
                indices.add(duplicateVertex.getIndex());
                return duplicateVertex;
            }
        }
    }
 
    private static void removeUnusedVertices(List<VertexNM> vertices) {
        for (VertexNM vertex : vertices) {
            vertex.averageTangents();
            if (!vertex.isSet()) {
                vertex.setTextureIndex(0);
                vertex.setNormalIndex(0);
            }
        }
    }
    
    
}
