package en.menghui.android.gameengine.gaussianBlur;

import en.menghui.android.gameengine.shaders.ShaderProgram;

public class VerticalBlurShader extends ShaderProgram {
	private static final String VERTEX_FILE = "/en/menghui/android/gameengine/gaussianBlur/vertical_blur_vertex_shader.txt";
	private static final String FRAGMENT_FILE = "/en/menghui/android/gameengine/gaussianBlur/blur_fragment_shader.txt";
	
	private int location_targetHeight;

	public VerticalBlurShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}
	
	protected void loadTargetHeight(float height) {
		super.loadFloat(location_targetHeight, height);
	}

	@Override
	protected void getAllUniformLocations() {
		location_targetHeight = super.getUniformLocation("targetHeight");
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
	}
	
	
}
