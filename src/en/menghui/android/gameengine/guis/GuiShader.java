package en.menghui.android.gameengine.guis;

import en.menghui.android.gameengine.shaders.ShaderProgram;

public class GuiShader extends ShaderProgram {
	private static final String VERTEX_FILE = "/en/menghui/android/gameengine/guis/gui_vertex_shader.txt";
	private static final String FRAGMENT_FILE = "/en/menghui/android/gameengine/guis/gui_fragment_shader.txt";
	
	private int location_transformationMatrix;
	
	public GuiShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}
	
	public void loadTransformation(float[] matrix) {
		super.loadMatrix(location_transformationMatrix, matrix);
	}
	
	@Override
	protected void getAllUniformLocations() {
		location_transformationMatrix = super.getUniformLocation("transformationMatrix");
	}
	
	@Override
	protected void bindAttributes() {
		super.bindAttribute(0,  "position");
	}
}
