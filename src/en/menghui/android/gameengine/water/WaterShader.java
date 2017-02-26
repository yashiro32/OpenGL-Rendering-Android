package en.menghui.android.gameengine.water;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.renderscript.Matrix4f;
import en.menghui.android.gameengine.entities.Camera;
import en.menghui.android.gameengine.entities.Light;
import en.menghui.android.gameengine.shaders.ShaderProgram;
import en.menghui.android.gameengine.toolbox.Maths;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WaterShader extends ShaderProgram {
	
	private static final String VERTEX_FILE = "/en/menghui/android/gameengine/water/water_vertex_shader.txt";
	private static final String FRAGMENT_FILE = "/en/menghui/android/gameengine/water/water_fragment_shader.txt";
	
	private int location_modelMatrix;
	private int location_viewMatrix;
	private int location_projectionMatrix;
	
	private int location_reflectionTexture;
	private int location_refractionTexture;
	
	private int location_dudvMap;
	private int location_moveFactor;
	
	private int location_cameraPosition;
	
	private int location_normalMap;
	private int location_lightColour;
	private int location_lightPosition;
	
	private int location_depthMap;

	public WaterShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}
	
	@Override
	protected void bindAttributes() {
		bindAttribute(0, "position");
	}

	@Override
	protected void getAllUniformLocations() {
		location_projectionMatrix = getUniformLocation("projectionMatrix");
		location_viewMatrix = getUniformLocation("viewMatrix");
		location_modelMatrix = getUniformLocation("modelMatrix");
		location_reflectionTexture = getUniformLocation("reflectionTexture");
		location_refractionTexture = getUniformLocation("refractionTexture");
		location_dudvMap = getUniformLocation("dudvMap");
		location_moveFactor = getUniformLocation("moveFactor");
		location_cameraPosition = getUniformLocation("cameraPosition");
		location_normalMap = getUniformLocation("normalMap");
		location_lightColour = getUniformLocation("lightColour");
		location_lightPosition = getUniformLocation("lightPosition");
		location_depthMap = getUniformLocation("depthMap");
	}
	
	public void connectTextureUnits() {
		super.loadInt(location_reflectionTexture, 0);
		super.loadInt(location_refractionTexture, 1);
		super.loadInt(location_dudvMap, 2);
		super.loadInt(location_normalMap, 3);
		super.loadInt(location_depthMap, 4);
	}
	
	public void loadLight(Light sun) {
		super.loadVector(location_lightColour, sun.getColour());
		super.loadVector(location_lightPosition, sun.getPosition());
	}
	
	public void loadMoveFactor(float factor) {
		super.loadFloat(location_moveFactor, factor);
	}

	public void loadProjectionMatrix(Matrix4f projection) {
		loadMatrix(location_projectionMatrix, projection.getArray());
	}
	
	public void loadViewMatrix(Camera camera) {
		Matrix4f viewMatrix = Maths.createViewMatrix(camera);
		loadMatrix(location_viewMatrix, viewMatrix.getArray());
		super.loadVector(location_cameraPosition, camera.getPosition());
	}
	
	public void loadModelMatrix(Matrix4f modelMatrix) {
		loadMatrix(location_modelMatrix, modelMatrix.getArray());
	}
	
}
