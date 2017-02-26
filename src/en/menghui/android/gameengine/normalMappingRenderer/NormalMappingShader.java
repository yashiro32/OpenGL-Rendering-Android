package en.menghui.android.gameengine.normalMappingRenderer;

import java.util.List;

import android.renderscript.Matrix4f;
import en.menghui.android.gameengine.entities.Light;
import en.menghui.android.gameengine.shaders.ShaderProgram;
import en.menghui.android.gameengine.toolbox.Maths;

public class NormalMappingShader extends ShaderProgram {
	private static final int MAX_LIGHTS = 4;
     
	private static final String VERTEX_FILE = "/en/menghui/android/gameengine/normalMappingRenderer/normal_map_vertex_shader.txt";
	private static final String FRAGMENT_FILE = "/en/menghui/android/gameengine/normalMappingRenderer/normal_map_fragment_shader.txt";
    
    private int location_transformationMatrix;
    private int location_projectionMatrix;
    private int location_viewMatrix;
    private int location_lightPositionEyeSpace[];
    private int location_lightColour[];
    private int location_attenuation[];
    private int location_shineDamper;
    private int location_reflectivity;
    private int location_skyColour;
    private int location_numberOfRows;
    private int location_offset;
    private int location_plane;
    private int location_modelTexture;
    private int location_normalMap;
    
    private int location_specularMap;
	private int location_usesSpecularMap;
	
    public NormalMappingShader() {
    	super(VERTEX_FILE, FRAGMENT_FILE);
    }
    
    @Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
        super.bindAttribute(1, "textureCoordinates");
        super.bindAttribute(2, "normal");
        super.bindAttribute(3, "tangent");
	}

	@Override
	protected void getAllUniformLocations() {
		location_transformationMatrix = super.getUniformLocation("transformationMatrix");
        location_projectionMatrix = super.getUniformLocation("projectionMatrix");
        location_viewMatrix = super.getUniformLocation("viewMatrix");
        location_shineDamper = super.getUniformLocation("shineDamper");
        location_reflectivity = super.getUniformLocation("reflectivity");
        location_skyColour = super.getUniformLocation("skyColour");
        location_numberOfRows = super.getUniformLocation("numberOfRows");
        location_offset = super.getUniformLocation("offset");
        location_plane = super.getUniformLocation("plane");
        location_modelTexture = super.getUniformLocation("modelTexture");
        location_normalMap = super.getUniformLocation("normalMap");
        
        location_lightPositionEyeSpace = new int[MAX_LIGHTS];
        location_lightColour = new int[MAX_LIGHTS];
        location_attenuation = new int[MAX_LIGHTS];
        for(int i=0;i<MAX_LIGHTS;i++){
            location_lightPositionEyeSpace[i] = super.getUniformLocation("lightPositionEyeSpace[" + i + "]");
            location_lightColour[i] = super.getUniformLocation("lightColour[" + i + "]");
            location_attenuation[i] = super.getUniformLocation("attenuation[" + i + "]");
        }
        
        location_specularMap = super.getUniformLocation("specularMap");
		location_usesSpecularMap = super.getUniformLocation("usesSpecularMap");
	}
	
	protected void connectTextureUnits() {
		super.loadInt(location_modelTexture, 0);
		super.loadInt(location_normalMap, 1);
		super.loadInt(location_specularMap, 2);
	}
	
	public void loadUseSpecularMap(boolean useMap) {
		super.loadBoolean(location_usesSpecularMap, useMap);
	}
	
	protected void loadClipPlane(float[] plane) {
		super.load4DVector(location_plane, plane);
	}
	
	protected void loadNumberOfRows(int numberOfRows) {
		super.loadFloat(location_numberOfRows, numberOfRows);
	}
	
	protected void loadOffset(float x, float y) {
		float[] position = {x, y};
		super.load2DVector(location_offset, position);
	}
	
	protected void loadSkyColour(float r, float g, float b) {
		float[] color = {r, g, b};
		super.loadVector(location_skyColour, color);
	}
	
	protected void loadShineVariables(float damper, float reflectivity) {
		super.loadFloat(location_shineDamper, damper);
		super.loadFloat(location_reflectivity, reflectivity);
	}
	
	protected void loadTransformationMatrix(float[] matrix) {
		super.loadMatrix(location_transformationMatrix, matrix);
	}
	
	protected void loadLights(List<Light> lights, Matrix4f viewMatrix) {
		for(int i = 0; i < MAX_LIGHTS; i++){
            if (i < lights.size()){
                super.loadVector(location_lightPositionEyeSpace[i], getEyeSpacePosition(lights.get(i), viewMatrix));
                super.loadVector(location_lightColour[i], lights.get(i).getColour());
                super.loadVector(location_attenuation[i], lights.get(i).getAttenuation());
            } else {
            	float[] position = {0, 0, 0};
				float[] color = {0, 0, 0};
				float[] attenuation = {1, 0, 0};
                super.loadVector(location_lightPositionEyeSpace[i], position);
                super.loadVector(location_lightColour[i], color);
                super.loadVector(location_attenuation[i], attenuation);
            }
        }
	}
	
	protected void loadViewMatrix(float[] viewMatrix) {
		super.loadMatrix(location_viewMatrix, viewMatrix);
	}
	
	protected void loadProjectionMatrix(float[] projection) {
		super.loadMatrix(location_projectionMatrix, projection);
	}
	
	private float[] getEyeSpacePosition(Light light, Matrix4f viewMatrix) {
		float[] position = light.getPosition();
		float[] eyeSpacePos = {position[0], position[1], position[2], 1f};
		float[] transEyeSpacePos = Maths.transformMatrix(viewMatrix, eyeSpacePos);
		
		float[] pos = {transEyeSpacePos[0], transEyeSpacePos[1], transEyeSpacePos[2]};
		
		return pos;
	}
	
}
