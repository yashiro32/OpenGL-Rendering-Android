package en.menghui.android.gameengine.postProcessing;

import en.menghui.android.gameengine.shaders.ShaderProgram;

public class ContrastShader extends ShaderProgram {
	private static final String VERTEX_FILE = "/en/menghui/android/gameengine/postProcessing/contrast_vertex_shader.txt";
	private static final String FRAGMENT_FILE = "/en/menghui/android/gameengine/postProcessing/contrast_fragment_shader.txt";

	public ContrastShader() {
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
