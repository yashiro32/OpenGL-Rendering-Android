package en.menghui.android.gameengine.shaders;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.annotation.TargetApi;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.os.Build;
import android.renderscript.Matrix4f;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public abstract class ShaderProgram {
	private Context context;
	
	private int programID;
	private int vertexShaderID;
	private int fragmentShaderID;
	
	private static FloatBuffer matrixBuffer;
	
	public ShaderProgram(String vertexFile, String fragmentFile) {
		vertexShaderID = loadShader(vertexFile, GLES30.GL_VERTEX_SHADER);
		fragmentShaderID = loadShader(fragmentFile, GLES30.GL_FRAGMENT_SHADER);
		programID = GLES30.glCreateProgram();
		GLES30.glAttachShader(programID, vertexShaderID);
		GLES30.glAttachShader(programID, fragmentShaderID);
		bindAttributes();
		GLES30.glLinkProgram(programID);
		GLES30.glValidateProgram(programID);
		getAllUniformLocations();
		
		ByteBuffer bb = ByteBuffer.allocateDirect(64);
		bb.order(ByteOrder.nativeOrder());
		matrixBuffer = bb.asFloatBuffer();
	}
	
	public ShaderProgram(int vertexFileRes, int fragmentFileRes, Context context) {
		this.context = context;
		
		vertexShaderID = loadShader(vertexFileRes, GLES30.GL_VERTEX_SHADER);
		fragmentShaderID = loadShader(fragmentFileRes, GLES30.GL_FRAGMENT_SHADER);
		programID = GLES30.glCreateProgram();
		GLES30.glAttachShader(programID, vertexShaderID);
		GLES30.glAttachShader(programID, fragmentShaderID);
		bindAttributes();
		GLES30.glLinkProgram(programID);
		GLES30.glValidateProgram(programID);
		getAllUniformLocations();
		
		ByteBuffer bb = ByteBuffer.allocateDirect(64);
		bb.order(ByteOrder.nativeOrder());
		matrixBuffer = bb.asFloatBuffer();
	}
	
	protected abstract void getAllUniformLocations();
	
	protected int getUniformLocation(String uniformName) {
		return GLES30.glGetUniformLocation(programID, uniformName);
	}
	
	public void start() {
		GLES30.glUseProgram(programID);
	}
	
	public void stop() {
		GLES30.glUseProgram(0);
	}
	
	public void cleanUp() {
		stop();
		GLES30.glDetachShader(programID, vertexShaderID);
		GLES30.glDetachShader(programID, fragmentShaderID);
		GLES30.glDeleteShader(vertexShaderID);
		GLES30.glDeleteShader(fragmentShaderID);
		GLES30.glDeleteProgram(programID);
	}
	
	protected abstract void bindAttributes();
	
	protected void bindAttribute(int attribute, String variableName) {
		GLES30.glBindAttribLocation(programID, attribute, variableName);
	}
	
	protected void bindFragOutput(int attachment, String variableName) {
		// GLES30.glBindFragDataLocation(programID, attachment, variableName);
	}
	
	protected void loadFloat(int location, float value) {
		GLES30.glUniform1f(location, value);
	}
	
	protected void loadInt(int location, int value) {
		GLES30.glUniform1i(location, value);
	}
	
	protected void loadVector(int location, float[] vector) {
		GLES30.glUniform3f(location, vector[0], vector[1], vector[2]);
	}
	
	protected void load4DVector(int location, float[] vector) {
		GLES30.glUniform4f(location, vector[0], vector[1], vector[2], vector[3]);
	}
	
	protected void load2DVector(int location, float[] vector) {
		GLES30.glUniform2f(location, vector[0], vector[1]);
	}
	
	protected void loadBoolean(int location, boolean value) {
		float toLoad = 0;
		if (value) {
			toLoad = 1;
		}
		GLES20.glUniform1f(location, toLoad);
	}
	
	protected void loadMatrix(int location, float[] matrix) {
		GLES30.glUniformMatrix4fv(location, 1, false, matrix, 0);
	}
	
	private int loadShader(String file, int type) {
		InputStream is = getClass().getResourceAsStream(file);
		
		StringBuilder shaderSource = new StringBuilder();
		try {
			// BufferedReader reader = new BufferedReader(new FileReader(file));
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line;
			while((line = reader.readLine()) != null) {
				shaderSource.append(line).append("//\n");
			}
			reader.close();
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		int shaderID = GLES30.glCreateShader(type);
		GLES30.glShaderSource(shaderID, shaderSource.toString());
		GLES30.glCompileShader(shaderID);
		
		int[] compiled = new int[1];
		GLES30.glGetShaderiv(shaderID, GLES30.GL_COMPILE_STATUS, compiled, 0);
		if (compiled[0] == GLES30.GL_FALSE) {
			System.out.println(GLES30.glGetShaderInfoLog(shaderID));
			System.err.println("Could not compile shader!");
			System.exit(-1);
		}
		
		return shaderID;
	}
	
	private int loadShader(int res, int type) {
		InputStream is = context.getResources().openRawResource(res);
		StringBuilder shaderSource = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line;
			while((line = reader.readLine()) != null) {
				shaderSource.append(line).append("//\n");
			}
			reader.close();
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		int shaderID = GLES30.glCreateShader(type);
		GLES30.glShaderSource(shaderID, shaderSource.toString());
		GLES30.glCompileShader(shaderID);
		
		int[] compiled = new int[1];
		GLES30.glGetShaderiv(shaderID, GLES30.GL_COMPILE_STATUS, compiled, 0);
		if (compiled[0] == GLES30.GL_FALSE) {
			System.out.println(GLES30.glGetShaderInfoLog(shaderID));
			System.err.println("Could not compile shader!");
			System.exit(-1);
		}
		
		return shaderID;
	}
	
	
}
