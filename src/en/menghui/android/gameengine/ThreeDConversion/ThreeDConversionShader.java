package en.menghui.android.gameengine.ThreeDConversion;

import en.menghui.android.gameengine.shaders.ShaderProgram;

public class ThreeDConversionShader extends ShaderProgram {
	private static final String VERTEX_FILE = "/en/menghui/android/gameengine/ThreeDConversion/3dconversion_vertex_shader.txt";
	private static final String FRAGMENT_FILE = "/en/menghui/android/gameengine/ThreeDConversion/3dconversion_fragment_shader.txt";
	
	private int location_targetWidth;
	private int location_targetHeight;
	private int location_shift;
	
	public ThreeDConversionShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}
	
	protected void loadTargetWidth(float width){
        super.loadFloat(location_targetWidth, width);
    }
	
	protected void loadTargetHeight(float height) {
		super.loadFloat(location_targetHeight, height);
	}
	
	protected void loadShift(float shift) {
		super.loadFloat(location_shift, shift);
	}
	
	@Override
	protected void getAllUniformLocations() {
		location_targetWidth = super.getUniformLocation("targetWidth");
		location_targetHeight = super.getUniformLocation("targetHeight");
		location_shift = super.getUniformLocation("shift");
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
	}
}
