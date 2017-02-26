package en.menghui.android.gameengine.particles;

import android.renderscript.Matrix4f;
import en.menghui.android.gameengine.shaders.ShaderProgram;

public class ParticleShader extends ShaderProgram {
	private static final String VERTEX_FILE = "/en/menghui/android/gameengine/particles/particle_vertex_shader.txt";
	private static final String FRAGMENT_FILE = "/en/menghui/android/gameengine/particles/particle_fragment_shader.txt";
	
	// private int location_modelViewMatrix;
	private int location_projectionMatrix;
	
	/* private int location_texOffset1;
	private int location_texOffset2;
	private int location_texCoordInfo; */
	
	private int location_numberOfRows;
	
	public ParticleShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}

	@Override
	protected void getAllUniformLocations() {
		// location_modelViewMatrix = super.getUniformLocation("modelViewMatrix");
		location_projectionMatrix = super.getUniformLocation("projectionMatrix");
		
		/* location_texOffset1 = super.getUniformLocation("texOffset1");
		location_texOffset2 = super.getUniformLocation("texOffset2");
		location_texCoordInfo = super.getUniformLocation("texCoordInfo"); */
		
		location_numberOfRows = super.getUniformLocation("numberOfRows");
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
		super.bindAttribute(1, "modelViewMatrix");
		super.bindAttribute(5, "texOffsets");
		super.bindAttribute(6, "blendFactor");
	}
	
	/* protected void loadTextureCoordInfo(float[] offset1, float[] offset2, float numRows, float blend) {
		super.load2DVector(location_texOffset1, offset1);
		super.load2DVector(location_texOffset2, offset2);
		float[] info = {numRows, blend};
		super.load2DVector(location_texCoordInfo, info);
	}
	
	protected void loadModelViewMatrix(float[] modelView) {
		super.loadMatrix(location_modelViewMatrix, modelView);
	} */
	
	protected void loadNumberOfRows(float numberOfRows) {
		super.loadFloat(location_numberOfRows, numberOfRows);
	}
	
	protected void loadProjectionMatrix(float[] projectionMatrix) {
		super.loadMatrix(location_projectionMatrix, projectionMatrix);
	}
	
	
}
