package en.menghui.android.gameengine.renderEngine;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;
import en.menghui.android.gameengine.models.RawModel;
import en.menghui.android.gameengine.textures.TextureData;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES10;
import android.opengl.GLES11;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class Loader {
	private Context context;
	
	private List<int[]> vaos = new ArrayList<int[]>();
	private List<int[]> vbos = new ArrayList<int[]>();
	private List<int[]> textures = new ArrayList<int[]>();
	
	public Loader(Context context) {
		this.context = context;
	}
	
	public RawModel loadToVAO(float[] positions, float[] textureCoords, float[] normals, int[] indices) {
		int vaoID = createVAO();
		bindIndicesBuffer(indices);
		storeDataInAttributeList(0, 3, positions);
		storeDataInAttributeList(1, 2, textureCoords);
		storeDataInAttributeList(2, 3, normals);
		unbindVAO();
		
		return new RawModel(vaoID, indices.length);
	}
	
	public int loadToVAO(float[] positions, float[] textureCoords) {
		int vaoID = createVAO();
		storeDataInAttributeList(0, 2, positions);
		storeDataInAttributeList(1, 2, textureCoords);
		unbindVAO();
		
		return vaoID;
	}
	
	 public RawModel loadToVAO(float[] positions) {
        int vaoID = createVAO();
        this.storeDataInAttributeList(0, 3, positions);
        unbindVAO();
        
        return new RawModel(vaoID, positions.length / 3);
    }
	
	public RawModel loadToVAO(float[] positions, float[] textureCoords, float[] normals, float[] tangents, int[] indices) {
		int vaoID = createVAO();
		bindIndicesBuffer(indices);
		storeDataInAttributeList(0, 3, positions);
		storeDataInAttributeList(1, 2, textureCoords);
		storeDataInAttributeList(2, 3, normals);
		storeDataInAttributeList(3, 3, tangents);
		unbindVAO();
		
		return new RawModel(vaoID, indices.length);
	}
	
	public int createEmptyVbo(int floatCount) {
		int[] vbo = new int[1];
		GLES30.glGenBuffers(1, vbo, 0);
		GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0]);
		GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, floatCount * 4, null, GLES30.GL_STREAM_DRAW);
		GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
		return vbo[0];
	}
	
	public void addInstancedAttribute(int vao, int vbo, int attribute, int dataSize, int instancedDataLength, int offset) {
		GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo);
		GLES30.glBindVertexArray(vao);
		GLES30.glVertexAttribPointer(attribute, dataSize, GLES30.GL_FLOAT, false, instancedDataLength * 4, offset * 4);
		GLES30.glVertexAttribDivisor(attribute, 1);
		GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
		GLES30.glBindVertexArray(0);
	}
	
	public void updateVbo(int vbo, float[] data, FloatBuffer buffer) {
		FloatBuffer buf = createFloatBuffer(data.length);
		buf.clear();
		buf.put(data);
		buf.flip();
		// System.out.println("Buffer capacity: " + data.length);
	    GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo);
		GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, buffer.capacity() * 4, null, GLES30.GL_STREAM_DRAW);
		GLES30.glBufferSubData(GLES30.GL_ARRAY_BUFFER, 0, buf.capacity() * 4, buf);
	    GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
	}
	
	/**
	 * Used to load vertices for guis and skyboxes.
	 * @param positions
	 * @param dimensions
	 * @return
	 */
	public RawModel loadToVAO(float[] positions, int dimensions) {
		int vaoID = createVAO();
		this.storeDataInAttributeList(0, dimensions, positions);
		unbindVAO();
		return new RawModel(vaoID, positions.length / dimensions);
	}
	
	public int loadTexture(int resourceId) {
		final int[] textureHandle = new int[1];
		
		GLES30.glGenTextures(1, textureHandle, 0);
		
		if (textureHandle[0] != 0) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false; // No pre-scaling
			
			// Read  in the resource
			Bitmap bitmap = BitmapFactory.decodeResource(this.context.getResources(), resourceId, options);
			
			// Bind to the texture in OpenGL.
			GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandle[0]);
			
			// Set filtering.
			GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
			GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST);
			
			// Load the bitmap into the bound texture.
			GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);
			
			// Recycle the bitmap, since its data has been loaded into OpenGL.
			bitmap.recycle();
		}
		
		if (textureHandle[0] == 0) {
			throw new RuntimeException("Error loading texture.");
		}
		
		GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
		GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
		// GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_LOD_BIAS, -0.4f);
		
		textures.add(textureHandle);
		
		return textureHandle[0];
	}
	
	public void cleanUp() {
		for (int[] vao : vaos) {
			GLES30.glDeleteVertexArrays(1, vao, 0);
		}
		
		for (int[] vbo : vbos) {
			GLES30.glDeleteBuffers(1, vbo, 0);
		}
		
		for (int[] texture : textures) {
			GLES30.glDeleteTextures(1, texture, 0);
		}
	}
	
	public int loadCubeMap(int[] textureFiles) {
		int[] texID = new int[1];
		GLES30.glGenTextures(1, texID, 0);
		GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, texID[0]);
		
		for (int i = 0; i < textureFiles.length; i++) {
			// TextureData data = decodeTextureFile("res/drawable-xhdpi/" + textureFiles[i] + ".png");
			TextureData data = decodeTextureFile(textureFiles[i]);
			GLES30.glTexImage2D(GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GLES30.GL_RGBA, data.getWidth(), data.getHeight(), 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, data.getBuffer());
		}
		GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
		GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
		
		GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
		
		textures.add(texID);
		return texID[0];
	}
	
	private TextureData decodeTextureFile(int fileName) {
		int width = 0;
		int height = 0;
		ByteBuffer buffer = null;
		try {
			// FileInputStream in = new FileInputStream(fileName);
			// InputStream in = getClass().getResourceAsStream(fileName);
			InputStream in = this.context.getResources().openRawResource(fileName);
			PNGDecoder decoder = new PNGDecoder(in);
			width = decoder.getWidth();
			height = decoder.getHeight();
			buffer = ByteBuffer.allocateDirect(4 * width * height);
			decoder.decode(buffer, width * 4, Format.RGBA);
			buffer.flip();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Tried to load texture " + fileName + ", didn't work");
			System.exit(-1);
		}
		
		return new TextureData(buffer, width, height);
	}
	
	private int createVAO() {
		// VertexArrayObject Id
		int [] vaoID = new int[1];
		
		GLES30.glGenVertexArrays (1, vaoID, 0);
		GLES30.glBindVertexArray(vaoID[0]);
		vaos.add(vaoID);
		return vaoID[0];
	}
	
	private void storeDataInAttributeList(int attributeNumber, int coordinateSize, float[] data) {
		int[] vboID = new int[1];
		
		GLES30.glGenBuffers(1, vboID, 0);
		vbos.add(vboID);
		GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboID[0]);
		FloatBuffer buffer = storeDataInFloatBuffer(data);
		GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, data.length*4, buffer, GLES30.GL_STATIC_DRAW);
		GLES30.glVertexAttribPointer(attributeNumber, coordinateSize, GLES30.GL_FLOAT, false, 0, 0);
		GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
	}
	
	private void unbindVAO() {
		GLES30.glBindVertexArray(0);
	}
	
	private void bindIndicesBuffer(int[] indices) {
		int[] vboID = new int[1];
		GLES30.glGenBuffers(1, vboID, 0);
		vbos.add(vboID);
		GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, vboID[0]);
		IntBuffer buffer = storeDataInIntBuffer(indices);
		GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, indices.length*4, buffer, GLES30.GL_STATIC_DRAW);
	}
	
	private IntBuffer storeDataInIntBuffer(int[] data) {
		ByteBuffer bb = ByteBuffer.allocateDirect(data.length*4);
		bb.order(ByteOrder.nativeOrder());
		IntBuffer buffer = bb.asIntBuffer();
		buffer.put(data);
		buffer.flip();
		return buffer;
	}
	
	private FloatBuffer storeDataInFloatBuffer(float[] data) {
		ByteBuffer bb = ByteBuffer.allocateDirect(data.length*4);
		bb.order(ByteOrder.nativeOrder());
		FloatBuffer buffer = bb.asFloatBuffer();
		
		buffer.put(data);
		buffer.flip();
		return buffer;
	}
	
	public static FloatBuffer createFloatBuffer(int size) {
		ByteBuffer bb = ByteBuffer.allocateDirect(size*4);
		bb.order(ByteOrder.nativeOrder());
		FloatBuffer buffer = bb.asFloatBuffer();
		
		return buffer;
	}
	
	
}
