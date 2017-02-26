package en.menghui.android.gameengine.skybox;

import android.annotation.TargetApi;
import android.os.Build;
import android.renderscript.Matrix4f;
import en.menghui.android.gameengine.entities.Camera;
import en.menghui.android.gameengine.renderEngine.MasterRenderer;
import en.menghui.android.gameengine.shaders.ShaderProgram;
import en.menghui.android.gameengine.toolbox.Maths;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SkyboxShader extends ShaderProgram {
	private static final String VERTEX_FILE = "/en/menghui/android/gameengine/skybox/skybox_vertex_shader.txt";
	private static final String FRAGMENT_FILE = "/en/menghui/android/gameengine/skybox/skybox_fragment_shader.txt";
	
	private static final float ROTATE_SPEED = 1f;
	
	private int location_projectionMatrix;
	private int location_viewMatrix;
	private int location_fogColour;
	
	private int location_cubeMap;
	private int location_cubeMap2;
	private int location_blendFactor;
	
	private float rotation = 0;
	
	public SkyboxShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
    }
	
	public void loadProjectionMatrix(float[] matrix) {
		super.loadMatrix(location_projectionMatrix, matrix);
	}
	
	public void loadViewMatrix(Camera camera) {
		Matrix4f matrix = Maths.createViewMatrix(camera);
		matrix.set(3, 0, 0);
		matrix.set(3, 1, 0);
		matrix.set(3, 2, 0);
		rotation += ROTATE_SPEED * MasterRenderer.getFrameTimeSeconds();
		matrix.rotate(rotation, 0, 1, 0);
		super.loadMatrix(location_viewMatrix, matrix.getArray());
	}
	
	public void loadFogColour(float r, float g, float b) {
		float[] color = {r, g, b};
		super.loadVector(location_fogColour, color);
	}
	
	public void connectTextureUnits() {
		super.loadInt(location_cubeMap, 0);
		super.loadInt(location_cubeMap2, 1);
	}
	
	public void loadBlendFactor(float blend) {
		super.loadFloat(location_blendFactor, blend);
	}
	
	@Override
	protected void getAllUniformLocations() {
		location_projectionMatrix = super.getUniformLocation("projectionMatrix");
		location_viewMatrix = super.getUniformLocation("viewMatrix");
		location_fogColour = super.getUniformLocation("fogColour");
		
		location_blendFactor = super.getUniformLocation("blendFactor");
		location_cubeMap = super.getUniformLocation("cubeMap");
		location_cubeMap2 = super.getUniformLocation("cubeMap2");
	}
	
	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
	}
	

}
