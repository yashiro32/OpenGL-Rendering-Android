package en.menghui.android.gameengine.shaders;

import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.renderscript.Matrix4f;
import en.menghui.android.gameengine.R;
import en.menghui.android.gameengine.entities.Camera;
import en.menghui.android.gameengine.entities.Light;
import en.menghui.android.gameengine.toolbox.Maths;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class StaticShader extends ShaderProgram {
	private static final int MAX_LIGHTS = 4;
	
	private static final String VERTEX_FILE = "/en/menghui/android/gameengine/shaders/vertex_shader.txt";
	private static final String FRAGMENT_FILE = "/en/menghui/android/gameengine/shaders/fragment_shader.txt";
	
	// private static final int VERTEX_FILE_RES = R.raw.vertex_shader;
	// private static final int FRAGMENT_FILE_RES = R.raw.fragment_shader;
	
	private int location_transformationMatrix;
	private int location_projectionMatrix;
	private int location_viewMatrix;
	private int location_lightPosition[];
	private int location_lightColour[];
	private int location_attenuation[];
	private int location_shineDamper;
	private int location_reflectivity;
	private int location_useFakeLighting;
	private int location_skyColour;
	private int location_numberOfRows;
	private int location_offset;
	private int location_plane;
	
	private int location_toShadowMapSpace;
	private int location_shadowMap;
	
	private int location_specularMap;
	private int location_usesSpecularMap;
	private int location_modelTexture;
	
	public StaticShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}
	
	/* public StaticShader(Context context) {
		super(VERTEX_FILE_RES, FRAGMENT_FILE_RES, context);
	} */
	
	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
		super.bindAttribute(1, "textureCoordinates");
		super.bindAttribute(2, "normal");
	}

	@Override
	protected void getAllUniformLocations() {
		location_transformationMatrix = super.getUniformLocation("transformationMatrix");
		location_projectionMatrix = super.getUniformLocation("projectionMatrix");
		location_viewMatrix = super.getUniformLocation("viewMatrix");
		
		location_shineDamper = super.getUniformLocation("shineDamper");
		location_reflectivity = super.getUniformLocation("reflectivity");
		location_useFakeLighting = super.getUniformLocation("useFakeLighting");
		location_skyColour = super.getUniformLocation("skyColour");
		location_numberOfRows = super.getUniformLocation("numberOfRows");
		location_offset = super.getUniformLocation("offset");
		location_plane = super.getUniformLocation("plane");
		
		location_lightPosition = new int[MAX_LIGHTS];
		location_lightColour = new int[MAX_LIGHTS];
		location_attenuation = new int[MAX_LIGHTS];
		for (int i = 0; i < MAX_LIGHTS; i++) {
			location_lightPosition[i] = super.getUniformLocation("lightPosition[" + i + "]");
			location_lightColour[i] = super.getUniformLocation("lightColour[" + i + "]");
			location_attenuation[i] = super.getUniformLocation("attenuation[" + i + "]");
		}
		
		location_toShadowMapSpace = super.getUniformLocation("toShadowMapSpac");
		location_shadowMap = super.getUniformLocation("shadowMap");
		
		location_specularMap = super.getUniformLocation("specularMap");
		location_usesSpecularMap = super.getUniformLocation("usesSpecularMap");
		location_modelTexture = super.getUniformLocation("modelTexture");
	}
	
	public void connectTextureUnits() {
		super.loadInt(location_shadowMap, 5);
		super.loadInt(location_modelTexture, 0);
		super.loadInt(location_specularMap, 1);
	}
	
	public void loadUseSpecularMap(boolean useMap) {
		super.loadBoolean(location_usesSpecularMap, useMap);
	}
	
	public void loadToShadowMapSpaceMatrix(float[] matrix) {
		super.loadMatrix(location_toShadowMapSpace, matrix);
	}
	
	public void loadClipPlane(float[] plane) {
		super.load4DVector(location_plane, plane);
	}
	
	public void loadNumberOfRows(int numberOfRows) {
		super.loadFloat(location_numberOfRows, numberOfRows);
	}
	
	public void loadOffset(float x, float y) {
		float[] position = {x, y};
		super.load2DVector(location_offset, position);
	}
	
	public void loadSkyColour(float r, float g, float b) {
		float[] color = {r, g, b};
		super.loadVector(location_skyColour, color);
	}
	
	public void loadFakeLightingVariable(boolean useFake) {
		super.loadBoolean(location_useFakeLighting, useFake);
	}
	
	public void loadShineVariables(float damper, float reflectivity) {
		super.loadFloat(location_shineDamper, damper);
		super.loadFloat(location_reflectivity, reflectivity);
	}
	
	public void loadTransformationMatrix(float[] matrix) {
		super.loadMatrix(location_transformationMatrix, matrix);
	}
	
	public void loadLights(List<Light> lights) {
		for (int i = 0; i < MAX_LIGHTS; i++) {
			if (i < lights.size()) {
				super.loadVector(location_lightPosition[i], lights.get(i).getPosition());
				super.loadVector(location_lightColour[i], lights.get(i).getColour());
				super.loadVector(location_attenuation[i], lights.get(i).getAttenuation());
			} else {
				float[] position = {0, 0, 0};
				float[] color = {0, 0, 0};
				float[] attenuation = {1, 0, 0};
				super.loadVector(location_lightPosition[i], position);
				super.loadVector(location_lightColour[i], color);
				super.loadVector(location_attenuation[i], attenuation);
			}
		}
	}
	
	public void loadViewMatrix(Camera camera) {
		Matrix4f viewMatrix = Maths.createViewMatrix(camera);
		super.loadMatrix(location_viewMatrix, viewMatrix.getArray());
	}
	
	public void loadProjectionMatrix(float[] projection) {
		super.loadMatrix(location_projectionMatrix, projection);
	}
	
	
}
