package en.menghui.android.gameengine.environmentMapRenderer;

import en.menghui.android.gameengine.shaders.ShaderProgram;

public class SkyboxShader extends ShaderProgram {
	private static final String VERTEX_FILE = "/en/menghui/android/gameengine/environmentMapRenderer/skybox_vertex_shader.txt";
	private static final String FRAGMENT_FILE = "/en/menghui/android/gameengine/environmentMapRenderer/skybox_fragment_shader.txt";
    
    private int location_projectionViewMatrix;
      
    public SkyboxShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }
  
    public void loadProjectionViewMatrix(float[] matrix){
        super.loadMatrix(location_projectionViewMatrix, matrix);
    }
    
    @Override
    protected void getAllUniformLocations() {
        location_projectionViewMatrix = super.getUniformLocation("projectionViewMatrix");
    }
  
    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
    }
}
