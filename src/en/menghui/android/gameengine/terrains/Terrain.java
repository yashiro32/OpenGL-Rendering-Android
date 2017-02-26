package en.menghui.android.gameengine.terrains;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import en.menghui.android.gameengine.models.RawModel;
import en.menghui.android.gameengine.renderEngine.Loader;
import en.menghui.android.gameengine.textures.ModelTexture;
import en.menghui.android.gameengine.textures.TerrainTexture;
import en.menghui.android.gameengine.textures.TerrainTexturePack;
import en.menghui.android.gameengine.toolbox.Maths;

public class Terrain {
	private static final String TAG = "Terrain";
	
	private Context context;
	
	private static final float SIZE = 800;
	private static final float MAX_HEIGHT = 40;
	private static final float MAX_PIXEL_COLOUR = 256 * 256 * 256;
	
	private float x;
	private float z;
	private RawModel model; 
	private TerrainTexturePack texturePack;
	private TerrainTexture blendMap;
	
	private float[][] heights;
	
	public Terrain (int gridX, int gridZ, Loader loader, TerrainTexturePack texturePack, TerrainTexture blendMap, int resId, Context context) {
		this.context = context;
		
		this.texturePack = texturePack;
		this.blendMap = blendMap;
		this.x = gridX * SIZE;
		this.z = gridZ * SIZE;
		this.model = generateTerrain(loader, resId);
	}
	
	private RawModel generateTerrain(Loader loader, int resId) {
		HeightsGenerator generator = new HeightsGenerator();
		
		// Read  in the resource
		Bitmap image = BitmapFactory.decodeResource(this.context.getResources(), resId);
		
		// int VERTEX_COUNT = image.getHeight();
		int VERTEX_COUNT = 128;
		
		int count = VERTEX_COUNT * VERTEX_COUNT;
		heights = new float[VERTEX_COUNT][VERTEX_COUNT];
        float[] vertices = new float[count * 3];
        float[] normals = new float[count * 3];
        float[] textureCoords = new float[count*2];
        int[] indices = new int[6*(VERTEX_COUNT-1)*(VERTEX_COUNT*1)];
        int vertexPointer = 0;
        for(int i=0;i<VERTEX_COUNT;i++){
            for(int j=0;j<VERTEX_COUNT;j++){
                vertices[vertexPointer*3] = -(float)j/((float)VERTEX_COUNT - 1) * SIZE;
                float height = getHeight(j, i, generator);
                heights[j][i] = height;
                vertices[vertexPointer*3+1] = height;
                vertices[vertexPointer*3+2] = -(float)i/((float)VERTEX_COUNT - 1) * SIZE;
                float[] normal = calculateNormal(j, i, generator);
                normals[vertexPointer*3] = normal[0];
                normals[vertexPointer*3+1] = normal[1];
                normals[vertexPointer*3+2] = normal[2];
                textureCoords[vertexPointer*2] = (float)j/((float)VERTEX_COUNT - 1);
                textureCoords[vertexPointer*2+1] = (float)i/((float)VERTEX_COUNT - 1);
                vertexPointer++;
            }
        }
        int pointer = 0;
        for(int gz=0;gz<VERTEX_COUNT-1;gz++){
            for(int gx=0;gx<VERTEX_COUNT-1;gx++){
                int topLeft = (gz*VERTEX_COUNT)+gx;
                int topRight = topLeft + 1;
                int bottomLeft = ((gz+1)*VERTEX_COUNT)+gx;
                int bottomRight = bottomLeft + 1;
                indices[pointer++] = topLeft;
                indices[pointer++] = bottomLeft;
                indices[pointer++] = topRight;
                indices[pointer++] = topRight;
                indices[pointer++] = bottomLeft;
                indices[pointer++] = bottomRight;
            }
        }
        return loader.loadToVAO(vertices, textureCoords, normals, indices);
    }
	
	/* private float[] calculateNormal(int x, int z, Bitmap image) {
		float heightL = getHeight(x-1, z, image);
		float heightR = getHeight(x+1, z, image);
		float heightD = getHeight(x, z-1, image);
		float heightU = getHeight(x, z+1, image);
		
		float[] normal = {heightL-heightR, 2f, heightD - heightU};
		
		normal = Maths.normalizeArray(normal);
		
		return normal;
	} */
	
	private float[] calculateNormal(int x, int z, HeightsGenerator generator) {
		float heightL = getHeight(x-1, z, generator);
		float heightR = getHeight(x+1, z, generator);
		float heightD = getHeight(x, z-1, generator);
		float heightU = getHeight(x, z+1, generator);
		
		float[] normal = {heightL-heightR, 2f, heightD - heightU};
		
		normal = Maths.normalizeArray(normal);
		
		return normal;
	}
	
	/* private float getHeight(int x, int z, Bitmap image) {
		if (x < 0 || x >= image.getHeight() || z < 0 || z >= image.getHeight()) {
			return 0;
		}
		
		float height = image.getPixel(x, z);
		height += MAX_PIXEL_COLOUR/2f;
		height /= MAX_PIXEL_COLOUR/2f;
		height *= MAX_HEIGHT;
		
		return height;
	} */
	
	public float getHeight(int x, int z, HeightsGenerator generator) {
		return generator.generateHeight(x, z);
	}
	
	public float getX() {
		return x;
	}

	public float getZ() {
		return z;
	}

	public RawModel getModel() {
		return model;
	}

	public TerrainTexturePack getTexturePack() {
		return texturePack;
	}

	public TerrainTexture getBlendMap() {
		return blendMap;
	}
	
	public float getHeightOfTerrain(float worldX, float worldZ) {
		float terrainX = worldX - this.x;
		float terrainZ = worldZ - this.z;
		float gridSquareSize = SIZE / (float)heights.length - 1;
		int gridX = (int) Math.floor(terrainX / gridSquareSize);
		int gridZ = (int) Math.floor(terrainZ / gridSquareSize);
		
		if (gridX >= heights.length - 1 || gridZ >= heights.length - 1 || gridX < 0 || gridZ < 0) {
			return 0;
		}
		
		float xCoord = (terrainX % gridSquareSize) / gridSquareSize;
		float zCoord = (terrainZ % gridSquareSize) / gridSquareSize;
		
		float answer;
		if (xCoord <= (1-zCoord)) {
			float[] p1 = {0, heights[gridX][gridZ], 0};
			float[] p2 = {1, heights[gridX + 1][gridZ], 0};
			float[] p3 = {0, heights[gridX][gridZ + 1], 1};
			float[] pos = {xCoord, zCoord};
			answer = Maths.barryCentric(p1, p2, p3, pos);
		} else {
			float[] p1 = {1, heights[gridX + 1][gridZ], 0};
			float[] p2 = {1, heights[gridX + 1][gridZ + 1], 1};
			float[] p3 = {0, heights[gridX][gridZ + 1], 1};
			float[] pos = {xCoord, zCoord};
			answer = Maths.barryCentric(p1, p2, p3, pos);
		}
		
		return answer;
	}
	
	
}
