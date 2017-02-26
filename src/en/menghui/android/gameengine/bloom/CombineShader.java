package en.menghui.android.gameengine.bloom;

import en.menghui.android.gameengine.shaders.ShaderProgram;

public class CombineShader extends ShaderProgram {
	private static final String VERTEX_FILE = "/en/menghui/android/gameengine/bloom/simple_vertex_shader.txt";
	private static final String FRAGMENT_FILE = "/en/menghui/android/gameengine/bloom/combine_fragment_shader.txt";
	
	private int location_colourTexture;
	private int location_highlightTexture;
	
	protected CombineShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}
	
	@Override
	protected void getAllUniformLocations() {
		location_colourTexture = super.getUniformLocation("colourTexture");
		location_highlightTexture = super.getUniformLocation("highlightTexture");
	}
	
	protected void connectTextureUnits() {
		super.loadInt(location_colourTexture, 0);
		super.loadInt(location_highlightTexture, 1);
	}
	
	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
	}
}
