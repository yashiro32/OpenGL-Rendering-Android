package en.menghui.android.gameengine.bloom;

import en.menghui.android.gameengine.shaders.ShaderProgram;

public class BrightFilterShader extends ShaderProgram {
	private static final String VERTEX_FILE = "/en/menghui/android/gameengine/bloom/simple_vertex_shader.txt";
	private static final String FRAGMENT_FILE = "/en/menghui/android/gameengine/bloom/bright_filter_fragment_shader.txt";
	
	public BrightFilterShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}
	
	@Override
	protected void getAllUniformLocations() {
		
	}
	
	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
	}
}
